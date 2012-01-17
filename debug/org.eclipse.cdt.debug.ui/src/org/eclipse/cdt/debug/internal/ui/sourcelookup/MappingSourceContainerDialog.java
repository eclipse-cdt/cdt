/*******************************************************************************
 * Copyright (c) 2009 ARM Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Limited - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.sourcelookup;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.sourcelookup.MappingSourceContainer;
import org.eclipse.cdt.debug.internal.core.sourcelookup.MapEntrySourceContainer;
import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.internal.ui.SWTUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public class MappingSourceContainerDialog extends TitleAreaDialog {

    class EntryCellModifier implements ICellModifier {
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
         */
        @Override
		public boolean canModify(Object element, String property) {
            return (CP_COMPILATION_PATH.equals(property) || CP_FILE_SYSTEM_PATH.equals(property));
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
         */
        @Override
		public Object getValue(Object element, String property) {
            if (element instanceof MapEntrySourceContainer) {
                MapEntrySourceContainer entry = (MapEntrySourceContainer)element;
                if (CP_COMPILATION_PATH.equals(property))
                    return entry.getBackendPath().toOSString();
                if (CP_FILE_SYSTEM_PATH.equals(property))
                    return entry.getLocalPath().toOSString();
            }
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
         */
        @Override
		public void modify(Object element, String property, Object value) {
            MapEntrySourceContainer entry = 
                    (element instanceof Item) ? 
                        (MapEntrySourceContainer)((Item)element).getData() 
                        : (MapEntrySourceContainer)element;
            boolean isDirty = false;
            if (CP_COMPILATION_PATH.equals(property)) {
                entry.setBackendPath(new Path((String)value));
                isDirty = true;
            }
            else if (CP_FILE_SYSTEM_PATH.equals(property)) {
                entry.setLocalPath(new Path((String)value));
                isDirty = true;
            }
            if (isDirty)
                refresh();
        }
    }

    class DirectoryCellEditor extends DialogCellEditor {

        DirectoryCellEditor(Composite parent) {
            super(parent);
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.DialogCellEditor#openDialogBox(org.eclipse.swt.widgets.Control)
         */
        @Override
        protected Object openDialogBox(Control cellEditorWindow) {
            TableItem[] selection = ((Table)cellEditorWindow.getParent()).getSelection();
            DirectoryDialog dialog = new DirectoryDialog(cellEditorWindow.getShell());
            dialog.setFilterPath(selection[0].getText(1));
            return dialog.open();
        }
    }

    class EntryLabelProvider extends LabelProvider implements ITableLabelProvider {

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
         */
        @Override
		public Image getColumnImage(Object element, int columnIndex) {
            if (element instanceof MapEntrySourceContainer && columnIndex == 0) {
                return CDebugImages.get(CDebugImages.IMG_OBJS_PATH_MAP_ENTRY);
            }
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
         */
        @Override
		public String getColumnText(Object element, int columnIndex) {
            if (element instanceof MapEntrySourceContainer) {
                MapEntrySourceContainer entry = (MapEntrySourceContainer)element;
                if (columnIndex == 0)
                    return entry.getBackendPath().toOSString();
                if (columnIndex == 1)
                    return entry.getLocalPath().toOSString();
            }
            return null;
        }
    }

    // Column properties
    private static final String CP_COMPILATION_PATH = "compilationPath"; //$NON-NLS-1$
    private static final String CP_FILE_SYSTEM_PATH = "fileSystemPath"; //$NON-NLS-1$

    private MappingSourceContainer fOriginalContainer;
    private MappingSourceContainer fContainer;

    private Text fNameText;
    private TableViewer fViewer;
    private Button fAddButton;
    private Button fRemoveButton;
    private Button fUpButton;
    private Button fDownButton;

    private ControlListener fTableListener;
    
    private boolean fIsValid = true;

    public MappingSourceContainerDialog(Shell shell, MappingSourceContainer container) {
        super(shell);
        fOriginalContainer = container;
        fContainer = container.copy();
        fTableListener = new ControlListener() {
            /* (non-Javadoc)
             * @see org.eclipse.swt.events.ControlListener#controlMoved(org.eclipse.swt.events.ControlEvent)
             */
            @Override
			public void controlMoved(ControlEvent e) {
                MappingSourceContainerDialog.this.controlMoved(e);
            }

            /* (non-Javadoc)
             * @see org.eclipse.swt.events.ControlListener#controlResized(org.eclipse.swt.events.ControlEvent)
             */
            @Override
			public void controlResized(ControlEvent e) {
                MappingSourceContainerDialog.this.controlResized(e);
            }            
        };
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.TrayDialog#close()
     */
    @Override
    public boolean close() {
        fViewer.getTable().removeControlListener(fTableListener);
        fContainer.dispose();
        return super.close();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#isResizable()
     */
    @Override
    protected boolean isResizable() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(SourceLookupUIMessages.PathMappingDialog_16);
        newShell.setToolTipText(SourceLookupUIMessages.MappingSourceContainerDialog_0);
        newShell.setImage(CDebugImages.get( CDebugImages.IMG_OBJS_PATH_MAPPING));
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Control control = super.createContents(parent);
        initialize();
        return control;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite control = (Composite)super.createDialogArea(parent);
        setTitle(SourceLookupUIMessages.PathMappingDialog_0);
        setTitleImage(CDebugImages.get(CDebugImages.IMG_WIZBAN_PATH_MAPPING));

        Composite composite = new Composite(control, SWT.None);
        composite.setLayout(new GridLayout(2, false));
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createNameArea(composite);
        createViewer(composite);
        createViewerButtonBar(composite);

        PlatformUI.getWorkbench().getHelpSystem().setHelp(getShell(), ICDebugHelpContextIds.SOURCE_PATH_MAP_ENTRY_DIALOG);
        return control;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        fOriginalContainer.clear();
        fOriginalContainer.setName(fNameText.getText().trim());
        try {
            fOriginalContainer.addMapEntries((MapEntrySourceContainer[])fContainer.getSourceContainers());
        } catch (CoreException e) {
        }
        super.okPressed();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#setErrorMessage(java.lang.String)
     */
    @Override
    public void setErrorMessage(String newErrorMessage) {
        fIsValid = (newErrorMessage == null);
        super.setErrorMessage(newErrorMessage);
    }

    public MappingSourceContainer getContainer() {
        return fOriginalContainer;
    }

    private void createNameArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.None);
        composite.setLayout(new GridLayout(2, false));
        composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));

        Label label = new Label(composite, SWT.NONE);
        label.setText(SourceLookupUIMessages.PathMappingDialog_12);
        fNameText = new Text(composite, SWT.BORDER | SWT.SINGLE);
        GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
        data.widthHint = 200;
        fNameText.setLayoutData(data);
        fNameText.addModifyListener(new ModifyListener() {
            /* (non-Javadoc)
             * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
             */
            @Override
			public void modifyText(ModifyEvent e) {
            }
        });
    }

    private void createViewer(Composite parent) {
        Composite tableComp = new Composite(parent, SWT.NONE);
        tableComp.setLayout(new GridLayout());
        tableComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        fViewer = new TableViewer(tableComp, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        Table table = fViewer.getTable();
        table.setLinesVisible(true);
        table.setHeaderVisible(true);     
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.widthHint = 500;
        data.heightHint = 200;
        table.setLayoutData(data);

        table.addControlListener(fTableListener);

        TableColumn nameColumn = new TableColumn(table, SWT.NULL);
        nameColumn.setResizable(true);
        nameColumn.setText(SourceLookupUIMessages.PathMappingDialog_1);
        nameColumn.setToolTipText(SourceLookupUIMessages.MappingSourceContainerDialog_1);
        
        TableColumn valueColumn = new TableColumn(table, SWT.NULL);
        valueColumn.setResizable(true);
        valueColumn.setText(SourceLookupUIMessages.PathMappingDialog_2);
        valueColumn.setToolTipText(SourceLookupUIMessages.MappingSourceContainerDialog_2);

        fViewer.setColumnProperties(
                new String[] {
                    CP_COMPILATION_PATH,
                    CP_FILE_SYSTEM_PATH,
                });

        fViewer.setContentProvider(new IStructuredContentProvider() {
            /* (non-Javadoc)
             * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
             */
            @Override
			public Object[] getElements(Object inputElement) {
                if (inputElement instanceof MappingSourceContainer) {
                    try {
                        return ((MappingSourceContainer)inputElement).getSourceContainers();
                    } catch (CoreException e) {
                    }
                }
                return new Object[0];
            }

            /* (non-Javadoc)
             * @see org.eclipse.jface.viewers.IContentProvider#dispose()
             */
            @Override
			public void dispose() {
            }

            /* (non-Javadoc)
             * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
             */
            @Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }
        });

        fViewer.setCellEditors(new CellEditor[] {
                new TextCellEditor(table),
                new DirectoryCellEditor(table),
        });

        fViewer.setCellModifier(new EntryCellModifier());

        fViewer.setLabelProvider(new EntryLabelProvider());
        
        fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            /* (non-Javadoc)
             * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
             */
            @Override
			public void selectionChanged(SelectionChangedEvent event) {
                updateViewerButtons();
            }
        });
    }

    private void createViewerButtonBar(Composite parent) {
        Composite buttonComp = new Composite(parent, SWT.NONE);
        buttonComp.setLayout(new GridLayout());
        buttonComp.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));

        fAddButton = createPushButton(buttonComp, SourceLookupUIMessages.MappingSourceContainerDialog_3);
        fAddButton.addSelectionListener(new SelectionAdapter() {
            /* (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(SelectionEvent event) {
                addPathEntry();
            }
        });

        fRemoveButton = createPushButton(buttonComp, SourceLookupUIMessages.PathMappingDialog_15);
        fRemoveButton.addSelectionListener(new SelectionAdapter() {
            /* (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(SelectionEvent event) {
                removePathEntries();
            }
        });

        fUpButton = createPushButton(buttonComp, SourceLookupUIMessages.MappingSourceContainerDialog_4);
        fUpButton.addSelectionListener(new SelectionAdapter() {
            /* (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(SelectionEvent event) {
                move(true);
            }
        });

        fDownButton = createPushButton(buttonComp, SourceLookupUIMessages.MappingSourceContainerDialog_5);
        fDownButton.addSelectionListener(new SelectionAdapter() {
            /* (non-Javadoc)
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(SelectionEvent event) {
                move(false);
            }
        });
    }

    private void initialize() {
        fNameText.setText(fContainer.getName());
        fNameText.selectAll();
        fViewer.setInput(fContainer);
        updateViewerButtons();
    }

    private Button createPushButton(Composite parent, String label) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(label);
        GridData gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        gd.widthHint = SWTUtil.getButtonWidthHint(button);
        button.setLayoutData(gd);
        return button;
    }

    private MapEntrySourceContainer[] getSelectedEntries() {
        List<?> list = ((IStructuredSelection)fViewer.getSelection()).toList();
        return list.toArray(new MapEntrySourceContainer[list.size()]);
    }

    private void updateErrorMessage() {
        setErrorMessage(null);
        try {
            ISourceContainer[] containers = fContainer.getSourceContainers();
            if (containers.length == 0)
                return;
            for (ISourceContainer c : containers) {
                MapEntrySourceContainer entry = (MapEntrySourceContainer)c;
                IPath backendPath = entry.getBackendPath();
                if (backendPath.isEmpty()) {
                    setErrorMessage(SourceLookupUIMessages.PathMappingDialog_5);
                    break;
                }
                if (!backendPath.isValidPath(backendPath.toString())) {
                    setErrorMessage(SourceLookupUIMessages.PathMappingDialog_6);
                    break;
                }
                IPath localPath = entry.getLocalPath();
                if (localPath.isEmpty()) {
                    setErrorMessage(SourceLookupUIMessages.PathMappingDialog_7);
                    break;
                }
                if (!localPath.toFile().exists()) {
                    setErrorMessage(SourceLookupUIMessages.PathMappingDialog_8);
                    break;
                }
                if (!localPath.toFile().isDirectory()) {
                    setErrorMessage(SourceLookupUIMessages.PathMappingDialog_9);
                    break;
                }
                if (!localPath.toFile().isAbsolute()) {
                    setErrorMessage(SourceLookupUIMessages.PathMappingDialog_10);
                    break;
                }
            }
        } catch (CoreException e) {
            // ignore
        }
    }

    private void updateViewerButtons() {
        boolean ok = true;
        boolean remove = true;
        boolean up = true;
        boolean down = true;
        
        try {
            ISourceContainer[] allEntries = fContainer.getSourceContainers();
            MapEntrySourceContainer[] entries = getSelectedEntries();
            if (entries.length == 0) {
                remove = false;
            }

            if (entries.length != 1) {
                up = false;
                down = false;
            } else {
                up = (!entries[0].equals(allEntries[0]));
                down = (!entries[0].equals(allEntries[allEntries.length - 1]));
            }
            
            ok = (allEntries.length != 0 && fIsValid);
        } catch (CoreException e) {
            // ignore, shouldn't happen
        }
        getButton(IDialogConstants.OK_ID).setEnabled(ok);
        fRemoveButton.setEnabled(remove);
        fUpButton.setEnabled(up);
        fDownButton.setEnabled(down);
    }

    private void refresh() {
        ISelection s = fViewer.getSelection();
        fViewer.refresh();
        fViewer.setSelection(s);
        updateErrorMessage();
        updateViewerButtons();
    }

    void controlMoved(ControlEvent e) {
    }

    void controlResized(ControlEvent e) {
        // resize columns
        Table table = fViewer.getTable();
        int width = table.getSize().x;
        if (width > 0) {
            TableColumn[] columns = table.getColumns();
            int colWidth = width / columns.length; 
            for (TableColumn col : columns) {
                if (col.getWidth() == 0) {
                    col.setWidth(colWidth);
                }
            }
        }
    }

    void addPathEntry() {
        MapEntrySourceContainer entry = new MapEntrySourceContainer();
        fContainer.addMapEntry(entry);
        fViewer.refresh();
        fViewer.setSelection(new StructuredSelection(entry), true);
        updateViewerButtons();
        fViewer.editElement(entry, 0);
    }

    void removePathEntries() {
        MapEntrySourceContainer[] entries = getSelectedEntries();
        fContainer.removeMapEntries(entries);
        refresh();
    }

    void move(boolean up) {
        MapEntrySourceContainer[] selEntries = getSelectedEntries();
        if (selEntries.length != 1)
            return;
        MapEntrySourceContainer entry = selEntries[0];
        try {
            ISourceContainer[] containers = fContainer.getSourceContainers();
            List<MapEntrySourceContainer> list = new ArrayList<MapEntrySourceContainer>(containers.length);
            for (ISourceContainer container : containers) {
                list.add(((MapEntrySourceContainer)container).copy());
            }
            int index = list.indexOf(entry);
            list.remove(index);
            index = (up) ? index - 1 : index + 1;
            list.add(index, entry);
            fContainer.clear();
            fContainer.addMapEntries(list.toArray(new MapEntrySourceContainer[list.size()]));
            refresh();
        } catch (CoreException e) {
        } catch (IndexOutOfBoundsException e) {
        }
    }
}
