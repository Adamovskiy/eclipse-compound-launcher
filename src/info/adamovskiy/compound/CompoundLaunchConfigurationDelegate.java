package info.adamovskiy.compound;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.debug.internal.ui.DebugUIPlugin;

public class CompoundLaunchConfigurationDelegate extends LaunchConfigurationDelegate {
    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor progressMonitor) throws CoreException {
        final List<ILaunchConfiguration> configurations = configuration
                .getAttribute(ConfigurationKeys.CONFIGS_KEY, new ArrayList<>()).stream()
                .map(ConfigurationUtils::deserialize).collect(Collectors.toList());

        progressMonitor.beginTask("Compound launch", 100 * configurations.size());
        try {
            // TODO support async launch
            for (ILaunchConfiguration subConfig : configurations) {
                // TODO allow to configure mode override for specific sub-configurations
                System.out.println(subConfig.getType().getName() + "#" + subConfig.getName());

                DebugUIPlugin.buildAndLaunch(subConfig, mode, new SubProgressMonitor(progressMonitor, 100));
            }
        } finally {
            progressMonitor.done();
        }
    }
}
