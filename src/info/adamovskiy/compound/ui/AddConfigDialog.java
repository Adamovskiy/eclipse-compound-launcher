package info.adamovskiy.compound.ui;

import info.adamovskiy.compound.ConfigurationUtils;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

class AddConfigDialog extends TitleAreaDialog {
    private final Map<ILaunchConfigurationType, List<ILaunchConfiguration>> available;
    private List<ILaunchConfiguration> selected = Collections.emptyList();

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

    List<ILaunchConfiguration> getSelected() {
        return selected;
    }
}
