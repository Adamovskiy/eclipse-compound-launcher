package info.adamovskiy.compound.ui;

import info.adamovskiy.compound.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class CompoundElementsTab extends AbstractLaunchConfigurationTab {
    private ConfigsTable table;
    private Button asyncCheckbox;
    private Map<ILaunchConfigurationType, List<ILaunchConfiguration>> availableConfigs;
    private ConfigurationIdentity thisIdentity;

    @Override
    public void createControl(Composite parent) {
        final Composite rootGroup = new Group(parent, SWT.BORDER);
        setControl(rootGroup);
        rootGroup.setLayout(new FillLayout());

        Composite configurationsSelector = new Composite(rootGroup, SWT.NONE);
        configurationsSelector.setLayout(new GridLayout(1, false));
        table = new ConfigsTable(configurationsSelector);
        final Composite buttons = new Composite(configurationsSelector, SWT.NONE);
        buttons.setLayout(new RowLayout());
        buttons.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        asyncCheckbox = new Button(buttons, SWT.CHECK);
        asyncCheckbox.setText("Asynchronous execution");
        asyncCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onChange();
            }
        });
        final Button addNewButton = new Button(buttons, SWT.PUSH);
        addNewButton.setText("Add...");
        addNewButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                showAddDialog();
            }
        });

        final Button clearButton = new Button(buttons, SWT.PUSH);
        clearButton.setText("Clear");
        clearButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                table.clearValues();
            }
        });

        table.setChangesListener(this::onChange);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
    }

    private void validate() {
        if (table.getValues().isEmpty()) {
            setErrorMessage("Configurations are not selected");
        } else {
            List<ConfigurationIdentity> selfNesting = ConfigurationValidator.findSelfNesting(thisIdentity, table
                    .getValues(), availableConfigs);
            if (selfNesting != null) {
                setErrorMessage("Self-nesting found. Path:\n" +
                        selfNesting.stream().map(id -> id.typeName + "#" + id.name).collect(Collectors.joining("\n")));
            } else {
                setErrorMessage(table.getValidationError());
            }
        }

    }

    private void updateDialog() {
        validate();
        updateLaunchConfigurationDialog();
    }

    private void onChange() {
        setDirty(true);
        updateDialog();
    }

    @Override
    public String getName() {
        return "Elements";
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        thisIdentity = new ConfigurationIdentity(configuration.getName(),
                ConfigurationUtils.getTypeUnchecked(configuration).getName());
        table.setParentMode(getLaunchConfigurationDialog().getMode());
        availableConfigs = ConfigurationUtils.getAllConfigurations().stream().collect(Collectors.groupingBy(
                ConfigurationUtils::getTypeUnchecked));

        try {
            final List<String> selectedString = configuration.getAttribute(ConfigurationKeys.CONFIGS_KEY,
                    new ArrayList<>());
            final List<ConfigData> selected = selectedString.stream().map(ConfigurationUtils::deserialize)
                    .collect(Collectors.toList());
            table.setValues(selected);
            asyncCheckbox.setSelection(configuration.getAttribute(ConfigurationKeys.ASYNC, false));

        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
        updateDialog();
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        final List<String> serializedConfigs = table.getValues().stream()
                .map(ConfigurationUtils::serialize).collect(Collectors.toList());
        configuration.setAttribute(ConfigurationKeys.CONFIGS_KEY, serializedConfigs);
        configuration.setAttribute(ConfigurationKeys.ASYNC, asyncCheckbox.getSelection());
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
    }


    private void showAddDialog() {
        final AddConfigDialog dialog = new AddConfigDialog(this.getShell(), availableConfigs);
        if (dialog.open() == Dialog.OK) {
            List<ILaunchConfiguration> addedConfigs = dialog.getSelected();
            if (addedConfigs == null || addedConfigs.isEmpty()) {
                return;
            }
            table.addValues(addedConfigs
                    .stream()
                    .map(c -> new ConfigData(new ConfigurationIdentity(c.getName(),
                            ConfigurationUtils.getTypeUnchecked(c).getName()), null))
                    .collect(Collectors.toList()));
        }
    }
}
