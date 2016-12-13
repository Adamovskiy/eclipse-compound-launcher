package info.adamovskiy.compound.ui;

import info.adamovskiy.compound.ConfigData;
import info.adamovskiy.compound.ConfigurationKeys;
import info.adamovskiy.compound.ConfigurationUtils;
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
    private Map<ILaunchConfigurationType, List<ILaunchConfiguration>> availableConfigs;

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

    private void updateDialog() {
        if (table.getValues().isEmpty()) {
            super.setErrorMessage("Configurations are not selected");
        } else {
            super.setErrorMessage(null);
        }
        updateLaunchConfigurationDialog();
    }

    // TODO suppress setDirty() in some cases of non-user update
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
        table.clearValues();
        // TODO filter out current
        availableConfigs = ConfigurationUtils.getAllConfigurations().stream().collect(Collectors.groupingBy(
                ConfigurationUtils::getTypeUnchecked));

        try {
            final List<String> selectedString = configuration.getAttribute(ConfigurationKeys.CONFIGS_KEY,
                    new ArrayList<>());
            final List<ConfigData> selected = selectedString.stream().map(ConfigurationUtils::deserialize)
                    .collect(Collectors.toList());
            table.setValues(selected);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
        onChange();
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        final List<String> serializedConfigs = table.getValues().stream()
                .map(ConfigurationUtils::serialize).collect(Collectors.toList());
        configuration.setAttribute(ConfigurationKeys.CONFIGS_KEY, serializedConfigs);
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
            table.addValues(addedConfigs.stream().map(c -> new ConfigData(c.getName(), ConfigurationUtils
                    .getTypeUnchecked(c).getName(), null)).collect(Collectors.toList()));
        }
    }
}
