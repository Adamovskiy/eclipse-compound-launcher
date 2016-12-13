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
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor
            progressMonitor) throws CoreException {
        final List<ConfigData> configurations = configuration
                .getAttribute(ConfigurationKeys.CONFIGS_KEY, new ArrayList<>()).stream()
                .map(ConfigurationUtils::deserialize).collect(Collectors.toList());

        progressMonitor.beginTask("Compound launch", 100 * configurations.size());
        try {
            // TODO support async launch
            for (ConfigData data : configurations) {
                System.out.println(data.typeName + "#" + data.name);

                final ILaunchConfiguration subConfig = ConfigurationUtils.findConfiuration(data.typeName, data.name);
                if (subConfig == null){
                    throw new IllegalStateException("Configuration " + data.name + " of type " + data.typeName +
                            "no longer exist");
                }
                final String effectiveMode = data.modeOverride == null ? mode : data.modeOverride;
                DebugUIPlugin.buildAndLaunch(subConfig, effectiveMode, new SubProgressMonitor(progressMonitor, 100));
            }
        } finally {
            progressMonitor.done();
        }
    }
}
