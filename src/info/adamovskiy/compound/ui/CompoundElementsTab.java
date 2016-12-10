package info.adamovskiy.compound.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import info.adamovskiy.compound.ConfigurationService;

public class CompoundElementsTab extends AbstractLaunchConfigurationTab {
    private final String mode;
    private ILaunchConfiguration configuration;

    CompoundElementsTab(String mode) {
        this.mode = mode;
    }

    @Override
    public void createControl(Composite parent) {
        final Composite rootGroup = new Group(parent, SWT.BORDER);
        setControl(rootGroup);
        GridLayoutFactory.swtDefaults().numColumns(1).applyTo(rootGroup);

        for (ILaunchConfiguration conf : ConfigurationService.getAllConfigurations()) {
            if (conf == configuration) { // TODO not works, fix it
                continue;
            }
            try {
                final Label label = new Label(rootGroup, SWT.NONE);
                label.setText(conf.getType().getName() + "#" + conf.getName());
                GridDataFactory.swtDefaults().applyTo(label);
                GridDataFactory.fillDefaults().grab(true, false).applyTo(label);
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getName() {
        return "Elements";
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        // TODO Auto-generated method stub

    }

}
