package info.adamovskiy.compound.ui;

import info.adamovskiy.compound.ConfigData;
import info.adamovskiy.compound.ConfigurationUtils;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class ConfigsTable extends Composite {
    private final TableViewer viewer;
    private final List<ConfigData> configs = new ArrayList<>();
    private final ModeColumnProvider modeColumnProvider;
    private Runnable listener;
    private String parentMode;

    ConfigsTable(Composite parent) {
        super(parent, SWT.NONE);
        setLayout(new FillLayout());

        viewer = new TableViewer(this);
        viewer.addSelectionChangedListener(event -> {
            if (!event.getSelection().isEmpty()) {
                viewer.setSelection(StructuredSelection.EMPTY);
            }
        });
        viewer.getTable().setHeaderVisible(true);
        viewer.getTable().setLinesVisible(true);
        viewer.setContentProvider(new ArrayContentProvider());

        TableColumn column = new TableColumn(viewer.getTable(), SWT.NONE);
        column.setText("Type");
        column.setWidth(200);
        final TableViewerColumn typeCol = new TableViewerColumn(viewer, column);
        typeCol.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                ConfigData data = configs.get((int) element);
                return data.identity.typeName;
            }

            @Override
            public Image getImage(Object element) {
                final ConfigData data = configs.get((Integer) element);
                final ILaunchConfiguration configuration = ConfigurationUtils.findConfiguration(data.identity);
                if (configuration != null) {
                    return ConfigurationUtils.getImage(ConfigurationUtils.getTypeUnchecked(configuration));
                }
                return null;
            }
        });

        column = new TableColumn(viewer.getTable(), SWT.NONE);
        column.setText("Name");
        column.setWidth(200);
        final TableViewerColumn nameCol = new TableViewerColumn(viewer, column);
        nameCol.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                ConfigData data = configs.get((int) element);
                return data.identity.name;
            }
        });

        column = new TableColumn(viewer.getTable(), SWT.NONE);
        column.setText("Mode override");
        column.setWidth(100);
        final TableViewerColumn modeCol = new TableViewerColumn(viewer, column);
        modeColumnProvider = new ModeColumnProvider();
        modeCol.setLabelProvider(modeColumnProvider);

        column = new TableColumn(viewer.getTable(), SWT.NONE);
        column.setText("");
        column.setWidth(100);
        final TableViewerColumn actionsCol = new TableViewerColumn(viewer, column);
        actionsCol.setLabelProvider(new ColumnLabelProvider() {
            private final Map<Object, Composite> composites = new HashMap<>();

            private void createButton(Composite group, String caption, Runnable handler) {
                Button button = new Button(group, SWT.PUSH);
                button.setLayoutData(new GridData(GridData.FILL_BOTH));
                button.setText(caption);
                button.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        if (e.widget.isDisposed()) {
                            return;
                        }
                        handler.run();
                        onChange();
                    }
                });
            }

            @Override
            public void update(ViewerCell cell) {
                TableItem item = (TableItem) cell.getItem();
                final int index = (int) item.getData();

                Composite group = composites.get(cell.getElement());
                if (group == null || group.isDisposed()) {
                    group = new Composite((Composite) cell.getViewerRow().getControl(), SWT.NONE);
                    final GridLayout layout = new GridLayout(4, true);
                    layout.horizontalSpacing = layout.marginHeight = layout.marginWidth = 0;
                    group.setLayout(layout);

                    createButton(group, "⬆", () -> ConfigurationUtils.swapListElements(configs, index, index - 1));
                    createButton(group, "⬇", () -> ConfigurationUtils.swapListElements(configs, index, index + 1));
                    createButton(group, "✎", () -> editSubConfiguration(index));
                    createButton(group, "✘", () -> configs.remove(index));

                    composites.put(cell.getElement(), group);
                }
                TableEditor editor = new TableEditor(item.getParent());
                editor.grabHorizontal = true;
                editor.grabVertical = true;
                editor.setEditor(group, item, cell.getColumnIndex());
                editor.layout();
            }


        });
    }

    private void editSubConfiguration(int index) {
        final ConfigData configData = configs.get(index);
        final ILaunchConfiguration configuration = ConfigurationUtils.findConfiguration(configData.identity);
        if (configuration == null) {
            return;
        }
        final ILaunchGroup launchGroup = DebugUITools.getLaunchGroup(configuration, configData.modeOverride != null ? configData.modeOverride : parentMode);
        if (launchGroup == null) {
            return;
        }
        DebugUITools.openLaunchConfigurationDialog(
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                configuration,
                launchGroup.getIdentifier(),
                null

        );
    }

    String getValidationError() {
        return modeColumnProvider.getValidationError();
    }

    private void onChange() {
        List<Integer> indexes = IntStream.rangeClosed(0, configs.size() - 1).boxed().collect(Collectors.toList());
        viewer.setInput(indexes);
        inputChanged();
        viewer.refresh();
        if (listener != null) {
            listener.run();
        }
    }

    private void inputChanged() {
        if (viewer.getTable() != null && viewer.getTable().getChildren() != null) {
            for (Control item : viewer.getTable().getChildren()) {
                if (item != null && !item.isDisposed()) {
                    item.dispose();
                }
            }
        }
    }

    List<ConfigData> getValues() {
        return configs;
    }

    void setValues(Collection<ConfigData> selected) {
        configs.clear();
        configs.addAll(selected);
        onChange();
    }

    void clearValues() {
        configs.clear();
        onChange();
    }

    void setChangesListener(Runnable listener) {
        this.listener = listener;
    }

    void addValues(List<ConfigData> values) {
        configs.addAll(values);
        onChange();
    }

    void setParentMode(String parentMode) {
        this.parentMode = parentMode;
    }

    private class ModeColumnProvider extends ColumnLabelProvider {
        private final Map<Object, CCombo> combos = new HashMap<>();
        private final BitSet validities = new BitSet();

        private void setValidity(int index, boolean valid) {
            validities.set(index, valid);
        }

        String getValidationError() {
            return validities.cardinality() == validities.length() ? null : "Some item configurations are invalid";
        }

        @Override
        public void update(ViewerCell cell) {
            final TableItem item = (TableItem) cell.getItem();

            final Integer index = (Integer) item.getData();
            setValidity(index, true);
            final ConfigData data = configs.get(index);

            CCombo combo = combos.get(cell.getElement());
            if (combo == null || combo.isDisposed()) {
                combo = new CCombo((Composite) cell.getViewerRow().getControl(), SWT.NONE);
                combo.setEditable(false);
                final ILaunchConfiguration configuration = ConfigurationUtils.findConfiguration(data.identity);
                if (configuration != null) {
                    // TODO what does "mode combination" means?
                    final Set<Set<String>> supportedModeCombinations =
                            ConfigurationUtils.getTypeUnchecked(configuration).getSupportedModeCombinations();
                    final List<String> values =
                            supportedModeCombinations.stream().flatMap(Collection::stream).collect(Collectors.toList());
                    values.add(0, null);
                    combo.setData(values);
                    combo.setItems(values.stream().map(s -> s == null ? "<No override>" : s).collect(Collectors.toList()).toArray(new String[0]));
                    combo.addSelectionListener(new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            if (e.widget.isDisposed()) {
                                return;
                            }
                            CCombo c = (CCombo) e.widget;
                            final String selected = values.get(c.getSelectionIndex());
                            configs.set(index, new ConfigData(data.identity, selected));
                            onChange();
                        }
                    });
                }
                combos.put(cell.getElement(), combo);
            }

            final List<String> options = (List<String>) combo.getData();

            if (options == null) {
                setValidity(index, false);
                // TODO red color
                combo.setText("*THERE IS NO SUCH CONFIGURATION ANYMORE*");
            } else {
                final int selectedIdx = options.indexOf(data.modeOverride);
                if (selectedIdx == -1) {
                    combo.setText("*SELECTED MODE IS NOT SUPPORTED ANYMORE*");
                    setValidity(index, false);
                } else {
                    combo.select(selectedIdx);
                }
            }

            final TableEditor editor = new TableEditor(item.getParent());
            editor.grabHorizontal = true;
            editor.grabVertical = true;
            editor.setEditor(combo, item, cell.getColumnIndex());
            editor.layout();
        }
    }
}
