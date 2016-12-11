package info.adamovskiy.compound.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import info.adamovskiy.compound.ConfigurationKeys;
import info.adamovskiy.compound.ConfigurationUtils;

public class CompoundElementsTab extends AbstractLaunchConfigurationTab {
    private final String mode;
    private ConfigurationsSelector configurationsSelector;

    CompoundElementsTab(String mode) {
        this.mode = mode;
    }

    @Override
    public void createControl(Composite parent) {
        final Composite rootGroup = new Group(parent, SWT.BORDER);
        setControl(rootGroup);
        GridLayoutFactory.swtDefaults().numColumns(1).applyTo(rootGroup);

        configurationsSelector = new ConfigurationsSelector(rootGroup, SWT.NONE);
        configurationsSelector.setConfigurationChangesListener(this::onChange);
    }

    private void onChange() {
        setDirty(true);
        if (configurationsSelector.getSelected().isEmpty()) {
            super.setErrorMessage("Configurations are not selected");
        } else {
            super.setErrorMessage(null);
        }
        updateLaunchConfigurationDialog();
    }

    @Override
    public String getName() {
        return "Elements";
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        configurationsSelector.clear();
        final List<ILaunchConfiguration> otherConfigurations = ConfigurationUtils.getAllConfigurations().stream()
                .filter(c -> !ConfigurationUtils.equals(c, configuration)).collect(Collectors.toList());
        configurationsSelector.setConfigurations(otherConfigurations);

        try {
            final List<String> selectedString = configuration.getAttribute(ConfigurationKeys.CONFIGS_KEY, new ArrayList<>());
            final List<ILaunchConfiguration> selected = selectedString.stream().map(ConfigurationUtils::deserialize)
                    .collect(Collectors.toList());
            configurationsSelector.setSelected(selected);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
        onChange();
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        final List<String> serializedConfigs = configurationsSelector.getSelected().stream()
                .map(ConfigurationUtils::serialize).collect(Collectors.toList());
        configuration.setAttribute(ConfigurationKeys.CONFIGS_KEY, serializedConfigs);
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
    }

}
