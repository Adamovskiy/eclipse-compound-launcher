package info.adamovskiy.compound;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.swt.widgets.Display;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CompoundLaunchConfigurationDelegate extends LaunchConfigurationDelegate {
    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor
            progressMonitor) throws CoreException {
        final List<ConfigData> configurations = configuration
                .getAttribute(ConfigurationKeys.CONFIGS_KEY, new ArrayList<>()).stream()
                .map(ConfigurationUtils::deserialize).collect(Collectors.toList());
        final boolean async = configuration.getAttribute(ConfigurationKeys.ASYNC, false);

        progressMonitor.beginTask("Compound launch", 100 * configurations.size());
        try {
            for (ConfigData data : configurations) {
                System.out.println("Launching " + data.identity.typeName + "#" + data.identity.name);

                final ILaunchConfiguration subConfig = ConfigurationUtils.findConfiguration(data.identity);
                if (subConfig == null) {
                    throw new IllegalStateException("Configuration " + data.identity.name + " of type " + data.identity.typeName + "no longer exist");
                }
                final String effectiveMode = data.modeOverride == null ? mode : data.modeOverride;
                final SubProgressMonitor monitor = new SubProgressMonitor(progressMonitor, 100);

                if (async) {
                    Display.getDefault().asyncExec(() -> {
                        try {
                            DebugUIPlugin.buildAndLaunch(subConfig, effectiveMode, monitor);
                        } catch (CoreException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } else {
                    DebugUIPlugin.buildAndLaunch(subConfig, effectiveMode, monitor);
                }
            }
        } finally {
            progressMonitor.done();
        }
    }
}
