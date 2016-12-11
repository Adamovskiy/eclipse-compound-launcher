package info.adamovskiy.compound.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import info.adamovskiy.compound.ConfigurationUtils;

import static info.adamovskiy.compound.ConfigurationUtils.getTypeUnchecked;

class ConfigurationsSelector extends Composite {

    private final Tree tree;

    ConfigurationsSelector(Composite parent, int style) {
        super(parent, style);
        super.setLayout(new FillLayout());
        tree = new Tree(this, SWT.BORDER | SWT.CHECK);

        tree.addListener(SWT.Selection, event -> {
            if (event.detail == SWT.CHECK) {
                TreeItem item = (TreeItem) event.item;
                boolean checked = item.getChecked();
                checkItems(item, checked);
                checkPath(item.getParentItem(), checked, false);
                configurationChangesListener.run();
            }
        });
    }

    void setConfigurations(Collection<ILaunchConfiguration> configurations) {
        final Map<ILaunchConfigurationType, List<ILaunchConfiguration>> groups = configurations.stream()
                .collect(Collectors.groupingBy(ConfigurationUtils::getTypeUnchecked));
        for (Map.Entry<ILaunchConfigurationType, List<ILaunchConfiguration>> group : groups.entrySet()) {
            final TreeItem groupItem = new TreeItem(tree, SWT.NONE);
            groupItem.setText(group.getKey().getName());
            // TODO groupItem.setImage(...);
            for (ILaunchConfiguration config : group.getValue()) {
                final TreeItem configItem = new TreeItem(groupItem, SWT.NONE);
                configItem.setText(config.getName());
                configItem.setData(config);
                // TODO configItem.setImage(...);
            }
        }
    }

    // TODO allow duplicates and different modes
    List<ILaunchConfiguration> getSelected() {
        final List<ILaunchConfiguration> result = new ArrayList<>();
        for (TreeItem groupItem : tree.getItems()) {
            for (TreeItem configItem : groupItem.getItems()) {
                if (configItem.getChecked()) {
                    result.add((ILaunchConfiguration) configItem.getData());
                }
            }
        }
        return result;
    }

    /**
     * Selects given configuration. Not deselects!
     * @param selected configurations to select. Comparison will be done by name and group name only.
     */
    void setSelected(Collection<ILaunchConfiguration> selected) {
        final Map<String, Set<String>> selectedMap = new HashMap<>();
        for (ILaunchConfiguration config : selected) {
            selectedMap.computeIfAbsent(getTypeUnchecked(config).getName(), (k) -> new HashSet<>()).add(config.getName());
        }

        for (TreeItem groupItem : tree.getItems()) {
            for (TreeItem configItem : groupItem.getItems()) {
                final ILaunchConfiguration data = (ILaunchConfiguration) configItem.getData();
                if (selectedMap.getOrDefault(getTypeUnchecked(data).getName(), Collections.emptySet()).contains(data.getName())) {
                    configItem.setChecked(true);
                    checkPath(configItem.getParentItem(), true, false);
                }
            }
        }
    }

    private static void checkPath(TreeItem item, boolean checked, boolean grayed) {
        if (item == null) {
            return;
        }
        if (grayed) {
            checked = true;
        } else {
            for (TreeItem child : item.getItems()) {
                if (child.getGrayed() || checked != child.getChecked()) {
                    checked = grayed = true;
                    break;
                }
            }
        }
        item.setChecked(checked);
        item.setGrayed(grayed);
        checkPath(item.getParentItem(), checked, grayed);
    }

    private static void checkItems(TreeItem item, boolean checked) {
        item.setGrayed(false);
        item.setChecked(checked);
        for (TreeItem item1 : item.getItems()) {
            checkItems(item1, checked);
        }
    }

    // TODO use existing addListener
    private Runnable configurationChangesListener;
    void setConfigurationChangesListener(Runnable listener) {
        configurationChangesListener = listener;
    }

    void clear() {
        for (TreeItem groupItem : tree.getItems()) {
            for (TreeItem configItem : groupItem.getItems()) {
                configItem.dispose();
            }
            groupItem.dispose();
        }
        tree.clearAll(true);
    }
}
