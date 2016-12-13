package info.adamovskiy.compound.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import info.adamovskiy.compound.ConfigurationUtils;

class AddConfigDialog extends TitleAreaDialog {
    private final Map<ILaunchConfigurationType, List<ILaunchConfiguration>> available;

    AddConfigDialog(Shell parentShell, Map<ILaunchConfigurationType, List<ILaunchConfiguration>> available) {
        super(parentShell);
        this.available = available;

    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite superArea = (Composite) super.createDialogArea(parent);
        getShell().setText("Add configurations");
        setTitle("Add configurations");
        final Tree tree = new Tree(superArea, SWT.BORDER | SWT.MULTI);
        tree.setLayoutData(new GridData(GridData.FILL_BOTH));
        available.forEach((type, configs) -> {
            final TreeItem typeItem = new TreeItem(tree, SWT.NONE);
            typeItem.setText(type.getName());
            typeItem.setImage(ConfigurationUtils.getImage(type));
            configs.forEach(config -> {
                final TreeItem configItem = new TreeItem(typeItem, SWT.NONE);
                configItem.setText(config.getName());
                configItem.setData(config);
            });
            typeItem.setExpanded(true);
        });
        tree.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                selected = Arrays.stream(tree.getSelection()).map(item -> (ILaunchConfiguration) item.getData())
                                 .filter(Objects::nonNull).collect(Collectors.toList());
            }
        });
        return superArea;
    }

    private List<ILaunchConfiguration> selected = Collections.emptyList();

    List<ILaunchConfiguration> getSelected() {
        return selected;
    }
}
