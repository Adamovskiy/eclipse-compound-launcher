package info.adamovskiy.compound;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.jdt.annotation.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigurationValidator {


    private static Collection<ILaunchConfiguration> dataToConfigurations(Stream<ConfigData> configDatas, Collection<ILaunchConfiguration> availableCompoundConfigs) {
        final Set<ConfigurationIdentity> nextIdentities = configDatas.map(c -> c.identity).collect(Collectors.toSet());

        return availableCompoundConfigs.stream().filter(c -> nextIdentities.contains(new ConfigurationIdentity(c
                .getName(), ConfigurationUtils.getTypeUnchecked(c).getName()))).collect(Collectors.toList());
    }

    @Nullable
    private static List<ConfigurationIdentity> findRecursive(Collection<ILaunchConfiguration> currentLevel,
                                                             ConfigurationIdentity toFind,
                                                             Set<ConfigurationIdentity> alreadyProcessed,
                                                             List<ILaunchConfiguration> availableCompoundConfigs)
            throws CoreException {
        for (ILaunchConfiguration config : currentLevel) {
            ConfigurationIdentity identity =
                    new ConfigurationIdentity(config.getName(), ConfigurationUtils.getTypeUnchecked(config).getName());
            if (!identity.typeName.equals(toFind.typeName)) { // process only CompoundConfigurations
                continue;
            }
            if (alreadyProcessed.contains(identity)) {
                continue;
            }
            alreadyProcessed.add(identity); // no need to add not CompoundConfigurations
            if (identity.equals(toFind)) {
                ArrayList<ConfigurationIdentity> result = new ArrayList<>();
                result.add(identity);
                return result;
            }
            final List<String> itemsString = config.getAttribute(ConfigurationKeys.CONFIGS_KEY, new ArrayList<>());

            final Collection<ILaunchConfiguration> nextLevel = dataToConfigurations(itemsString.stream().map(ConfigurationUtils::deserialize), availableCompoundConfigs);
            final List<ConfigurationIdentity> recursiveResult =
                    findRecursive(nextLevel, toFind, alreadyProcessed, availableCompoundConfigs);
            if (recursiveResult != null) {
                recursiveResult.add(0, identity);
                return recursiveResult;
            }
        }
        return null;
    }

    /**
     * Find if there is some path of nesting current configuration into itself.
     *
     * @return first found path upside down if any, {@code null} otherwise.
     */
    @Nullable
    public static List<ConfigurationIdentity> findSelfNesting(ConfigurationIdentity thisIdentity,
                                                              List<ConfigData> values,
                                                              Map<ILaunchConfigurationType, List<ILaunchConfiguration>> availableConfigs) {
        if (thisIdentity == null) {
            return null; // not inited yet
        }

        final List<ILaunchConfiguration> availableCompoundConfigs = availableConfigs
                .entrySet().stream()
                .filter(e -> thisIdentity.typeName.equals(e.getKey().getName()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseGet(Collections::<ILaunchConfiguration>emptyList);


        Collection<ILaunchConfiguration> topLevel = dataToConfigurations(values.stream(), availableCompoundConfigs);

        try {
            return findRecursive(topLevel, thisIdentity, new HashSet<>(), availableCompoundConfigs);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }
}
