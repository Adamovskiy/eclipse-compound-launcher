package info.adamovskiy.compound;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
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

        progressMonitor.beginTask(Messages.CompoundLaunchConfigurationDelegate_task_name, 100 * configurations.size());
        try {
            for (ConfigData data : configurations) {
                System.out.println(String.format(Messages.CompoundLaunchConfigurationDelegate_subtask, data.identity.typeName + "#" + data.identity.name)); //$NON-NLS-2$

                final ILaunchConfiguration subConfig = ConfigurationUtils.findConfiguration(data.identity);
                if (subConfig == null) {
                    throw new IllegalStateException(String.format(Messages.CompoundLaunchConfigurationDelegate_no_config_error, data.identity.name, data.identity.typeName));
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
