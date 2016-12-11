package info.adamovskiy.compound;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;

public class CompoundLaunchConfigurationDelegate extends LaunchConfigurationDelegate {
    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor progressMonitor) throws CoreException {
        progressMonitor.beginTask("stub", IProgressMonitor.UNKNOWN);
        final List<ILaunchConfiguration> configurations = configuration
                .getAttribute(ConfigurationKeys.CONFIGS_KEY, new ArrayList<>()).stream()
                .map(ConfigurationUtils::deserialize).collect(Collectors.toList());

        for (ILaunchConfiguration subConfig : configurations) {
            System.out.println(subConfig.getType().getName() + "#" + subConfig.getName());
        }

        progressMonitor.done();
    }
}
