package info.adamovskiy.compound;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;

public class ConfigurationUtils {
	private ConfigurationUtils() {}

	public static List<ILaunchConfiguration> getAllConfigurations() {
		try {
			return Arrays.asList(DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations());
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Optimistic comparision of configurations. Most parameters like mode and attributes are ignored.
	 *
	 * @param a
	 * @param b
	 * @return true if a and b have the same name and type.
	 */
	public static boolean equals(ILaunchConfiguration a, ILaunchConfiguration b) {
		return a == b || !(a == null || b == null)
				&& Objects.equals(a.getName(), b.getName())
				&& Objects.equals(getTypeUnchecked(a).getName(), getTypeUnchecked(b).getName());
	}

	private static final String SERIAL_SEPARATOR = ":";

	private static String encode(String part) {
		return Base64.getEncoder().encodeToString(part.getBytes());
	}

	private static String decode(String part) {
		return new String(Base64.getDecoder().decode(part));
	}

	private static ILaunchConfiguration findConfiuration(String typeName, String name) {
		for (ILaunchConfiguration configuration : getAllConfigurations()) {
			if (Objects.equals(configuration.getName(), name)
					&& Objects.equals(getTypeUnchecked(configuration).getName(), typeName)) {
				return configuration;
			}
		}
		return null;
	}

	public static String serialize(ILaunchConfiguration configuration) {
		return encode(getTypeUnchecked(configuration).getName()) + SERIAL_SEPARATOR
				+ encode(configuration.getName());
	}

	public static ILaunchConfiguration deserialize(String string) {
		final String[] parts = string.split(SERIAL_SEPARATOR);
		if (parts.length != 2) {
			throw new IllegalArgumentException("Illegal configuration value: " + string);
		}
		return findConfiuration(decode(parts[0]), decode(parts[1]));
	}

	public static ILaunchConfigurationType getTypeUnchecked(ILaunchConfiguration configuration) {
		try {
			return configuration.getType();
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}
}
