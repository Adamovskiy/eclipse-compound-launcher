package info.adamovskiy.compound;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;

public class CompoundLaunchConfigurationDelegate extends LaunchConfigurationDelegate {
    @Override
    public void launch(ILaunchConfiguration launchConfiguration, String s, ILaunch launch, IProgressMonitor progressMonitor) throws CoreException {
        progressMonitor.beginTask("stub", IProgressMonitor.UNKNOWN);
        progressMonitor.done();
    }
}
