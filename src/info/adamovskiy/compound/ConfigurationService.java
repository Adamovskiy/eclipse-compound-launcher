package info.adamovskiy.compound;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;

public class ConfigurationService {
	public static List<ILaunchConfiguration> getAllConfigurations() {
		try {
			return Arrays.asList(DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations());
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}
}
