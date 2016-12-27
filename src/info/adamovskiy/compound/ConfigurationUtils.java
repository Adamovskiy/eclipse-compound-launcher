package info.adamovskiy.compound;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.Image;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

public class ConfigurationUtils {
    private static final String SERIAL_SEPARATOR = ":";

    private ConfigurationUtils() {
    }

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
        return a == b || !(a == null || b == null) && Objects.equals(a.getName(), b.getName()) &&
                Objects.equals(getTypeUnchecked(a).getName(), getTypeUnchecked(b).getName());
    }

    private static String encode(String part) {
        return part == null ? "" : Base64.getEncoder().encodeToString(part.getBytes());
    }

    private static String decode(String part) {
        final String result = new String(Base64.getDecoder().decode(part));
        return result.isEmpty() ? null : result;
    }

    @Nullable
    public static ILaunchConfiguration findConfiguration(ConfigurationIdentity needle) {
        for (ILaunchConfiguration configuration : getAllConfigurations()) {
            if (Objects.equals(configuration.getName(), needle.name) &&
                    Objects.equals(getTypeUnchecked(configuration).getName(), needle.typeName)) {
                return configuration;
            }
        }
        return null;
    }

    public static String serialize(ConfigData configData) {
        return encode(configData.identity.name) + SERIAL_SEPARATOR + encode(configData.identity.typeName) +
                (configData.modeOverride == null ? "" : SERIAL_SEPARATOR + encode(configData.modeOverride));
    }

    public static ConfigData deserialize(String string) {
        final String[] parts = string.split(SERIAL_SEPARATOR);
        if (parts.length == 3) {
            return new ConfigData(new ConfigurationIdentity(decode(parts[0]), decode(parts[1])), decode(parts[2]));
        }
        if (parts.length == 2) {
            return new ConfigData(new ConfigurationIdentity(decode(parts[0]), decode(parts[1])), null);
        }
        throw new IllegalArgumentException("Illegal configuration value: " + string);
    }

    public static ILaunchConfigurationType getTypeUnchecked(ILaunchConfiguration configuration) {
        try {
            return configuration.getType();
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    public static Image getImage(ILaunchConfigurationType type) {
        return DebugPluginImages.getImage(type.getIdentifier());
    }

    public static <T> void swapListElements(List<T> list, int i, int j) {
        if (i < 0 || j < 0 || i > list.size() - 1 || j > list.size() - 1) {
            return;
        }
        T buff = list.get(i);
        list.set(i, list.get(j));
        list.set(j, buff);

    }
}
