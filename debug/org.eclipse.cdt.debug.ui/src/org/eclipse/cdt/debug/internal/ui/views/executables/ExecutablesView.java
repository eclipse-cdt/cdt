/*******************************************************************************
 * Copyright (c) 2008, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 * IBM Corporation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.views.executables;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.debug.core.executables.Executable;
import org.eclipse.cdt.debug.core.executables.ExecutablesManager;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * ExecutablesView displays a list of executable files either in the workspace
 * or created by projects in the workspace. The list of executables comes from
 * the ExecutablesManager. This view has two subviews: one that shows the list
 * of executables and another that shows the list of source files in the
 * selected executable.
 * 
 */
public class ExecutablesView extends ViewPart {

	/**
	 * Settings for the view including the sorted column for the sub views and
	 * the list of visible columns.
	 */

	/**
	 * Constants for the columns.
	 */
	public final static int NAME = 0x0;
	public final static int PROJECT = 0x1;
	public final static int LOCATION = 0x2;
	public final static int SIZE = 0x3;
	public final static int MODIFIED = 0x4;
	public final static int TYPE = 0x5;
	public final static int ORG_LOCATION = 0x6;

	/**
	 * Constants for the column sort order.
	 */

	public static int ASCENDING = 1;
	public static int DESCENDING = -1;

	/**
	 * Display constants and icons.
	 */

	public final static String ICONS_PATH = "icons/"; //$NON-NLS-1$
	public static final String EXECUTABLES_VIEW_CONTEXT = "org.eclipse.cdt.debug.ui.executables_View_context"; //$NON-NLS-1$;
	private static final String PATH_LCL = ICONS_PATH + "elcl16/"; //$NON-NLS-1$
	private static final String PATH_LCL_DISABLED = ICONS_PATH + "dlcl16/"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_REFRESH = create(PATH_LCL, "refresh.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_REFRESH_DISABLED = create(PATH_LCL_DISABLED, "refresh.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_IMPORT = create(PATH_LCL, "import.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_IMPORT_DISABLED = create(PATH_LCL_DISABLED, "import.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_COLUMNS = create(PATH_LCL, "columns.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_COLUMNS_DISABLED = create(PATH_LCL_DISABLED, "columns.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_REMOVE = create(PATH_LCL, "rem_co.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_REMOVE_DISABLED = create(PATH_LCL_DISABLED, "rem_co.gif"); //$NON-NLS-1$
	public static final int COLUMN_WIDTH_PADDING = 24;

	private static final String SEPARATOR = System.getProperty("line.separator"); //$NON-NLS-1$

	private static ImageDescriptor create(String prefix, String name) {
		return ImageDescriptor.createFromURL(makeIconURL(prefix, name));
	}

	private static URL makeIconURL(String prefix, String name) {
		String path = "$nl$/" + prefix + name; //$NON-NLS-1$
		return FileLocator.find(CDebugUIPlugin.getDefault().getBundle(), new Path(path), null);
	}

	/**
	 * Complete list of column names for both sub views. These are not display
	 * names and should not be localized. Display names are set when the columns
	 * are created in the sub views.
	 */
	private String[] columnNames = { Messages.ExecutablesView_ExeName, Messages.ExecutablesView_ExeProject, Messages.ExecutablesView_ExeLocation, Messages.ExecutablesView_ExeSize, Messages.ExecutablesView_ExeData,
			Messages.ExecutablesView_ExeType, Messages.ExecutablesView_SrcName, Messages.ExecutablesView_SrcLocation, Messages.ExecutablesView_SrcOrgLocation, Messages.ExecutablesView_SrcSize, Messages.ExecutablesView_SrcDate,
			Messages.ExecutablesView_SrcType };

	/**
	 * Not all the columns are visible by default. Here are the ones that are.
	 */
	private TreeColumn[] allColumns = new TreeColumn[columnNames.length];

	/**
	 * Configures the list of columns show in the view.
	 */
	public class ConfigureColumnsAction extends Action {

		public static final String CONFIGURE_COLUMNS_DIALOG = "org.eclipse.cdt.debug.ui.configure_columns_dialog_context"; //$NON-NLS-1$;
		public static final String CONFIGURE_COLUMNS_ACTION = "org.eclipse.cdt.debug.ui.configure_columns_action_context"; //$NON-NLS-1$;

		class ColumnContentProvider implements IStructuredContentProvider {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
			 */
			@Override
			public Object[] getElements(Object inputElement) {
				return columnNames;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
			 */
			@Override
			public void dispose() {
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
			 *      java.lang.Object, java.lang.Object)
			 */
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

		}

		class ColumnLabelProvider extends LabelProvider {

			@Override
			public String getText(Object element) {
				return (String) element;
			}

		}

		public ConfigureColumnsAction() {
			setText(Messages.ExecutablesView_ConfigureColumns);
			setId(CDebugUIPlugin.getUniqueIdentifier() + ".ConfigureColumnsAction"); //$NON-NLS-1$
			PlatformUI.getWorkbench().getHelpSystem().setHelp(this, CONFIGURE_COLUMNS_ACTION);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.action.Action#run()
		 */
		@Override
		public void run() {
			ListSelectionDialog dialog = new ListSelectionDialog(ExecutablesView.this.getExecutablesViewer().getTree().getShell(), this,
					new ColumnContentProvider(), new ColumnLabelProvider(), Messages.ExecutablesView_SelectColumns);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(this, CONFIGURE_COLUMNS_DIALOG);
			String[] visibleColumns = getVisibleColumns();
			List<String> initialSelection = new ArrayList<String>(visibleColumns.length);
			for (int i = 0; i < visibleColumns.length; i++) {
				initialSelection.add(visibleColumns[i]);
			}
			dialog.setTitle(Messages.ExecutablesView_ConfigureColumns);
			dialog.setInitialElementSelections(initialSelection);
			if (dialog.open() == Window.OK) {
				Object[] result = dialog.getResult();
				String[] ids = new String[result.length];
				System.arraycopy(result, 0, ids, 0, result.length);
				setVisibleColumns(ids);
			}

		}

	}

	/**
	 * Sub viewers and trees
	 */
	private SourceFilesViewer sourceFilesViewer;
	private ExecutablesViewer executablesViewer;

	/**
	 * Associated Actions
	 */
	Action refreshAction;
	Action importAction;
	Action removeAction;
	private Action copyAction;
	private Action configureColumnsAction;

	private IMemento memento;
	
	private IStructuredSelection oldSelection;
	private ISelectionProvider focusedViewer;

	/**
	 * Create contents of the Executables View
	 * 
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());

		final SashForm sashForm = new SashForm(container, SWT.NONE);

		// Create the two sub viewers.
		executablesViewer = new ExecutablesViewer(this, sashForm, SWT.FULL_SELECTION | SWT.BORDER | SWT.MULTI);
		focusedViewer = executablesViewer;
		sourceFilesViewer = new SourceFilesViewer(this, sashForm, SWT.BORDER | SWT.MULTI);

		executablesViewer.getTree().addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {}
			
			@Override
			public void focusGained(FocusEvent e) {
				focusedViewer = executablesViewer;
			}
		});

		sourceFilesViewer.getTree().addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {}
			
			@Override
			public void focusGained(FocusEvent e) {
				focusedViewer = sourceFilesViewer;
			}
		});
		
		ExecutablesView.this.getViewSite().setSelectionProvider(new ISelectionProvider() {
			
			@Override
			public void setSelection(ISelection selection) {
				getFocusedViewer().setSelection(selection);
			}
			
			@Override
			public void removeSelectionChangedListener(
					ISelectionChangedListener listener) {
				executablesViewer.removeSelectionChangedListener(listener);
				sourceFilesViewer.removeSelectionChangedListener(listener);
			}
			
			@Override
			public ISelection getSelection() {
				return getFocusedViewer().getSelection();
			}
			
			@Override
			public void addSelectionChangedListener(ISelectionChangedListener listener) {
				executablesViewer.addSelectionChangedListener(listener);
				sourceFilesViewer.addSelectionChangedListener(listener);
			}
		});

		sashForm.setWeights(new int[] { 1, 1 });

		// Keep a combined list of all the columns so
		// we can easily operate on them all.
		allColumns[0] = executablesViewer.nameColumn;
		allColumns[1] = executablesViewer.projectColumn;
		allColumns[2] = executablesViewer.locationColumn;
		allColumns[3] = executablesViewer.sizeColumn;
		allColumns[4] = executablesViewer.modifiedColumn;
		allColumns[5] = executablesViewer.typeColumn;
		allColumns[6] = sourceFilesViewer.nameColumn;
		allColumns[7] = sourceFilesViewer.locationColumn;
		allColumns[8] = sourceFilesViewer.originalLocationColumn;
		allColumns[9] = sourceFilesViewer.sizeColumn;
		allColumns[10] = sourceFilesViewer.modifiedColumn;
		allColumns[11] = sourceFilesViewer.typeColumn;

		readSettings();

		createActions();

		// When the selection changes in the executables list
		// update the source files viewer
		executablesViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection newSelection = event.getSelection();
				if (newSelection instanceof IStructuredSelection) {
					
					if (oldSelection == null || !oldSelection.equals(newSelection))
					{
						// update the remove action
						removeAction.setEnabled(!newSelection.isEmpty());

						// just immediately do this work: the source files content provider
						// will do the work in the background
						final Object firstElement = ((IStructuredSelection) newSelection).getFirstElement();

						sourceFilesViewer.setInput(firstElement);
						
						oldSelection = (IStructuredSelection) newSelection;
					}
				}
			}
		});

		executablesViewer.packColumns();
		sourceFilesViewer.packColumns();

		PlatformUI.getWorkbench().getHelpSystem().setHelp(container, EXECUTABLES_VIEW_CONTEXT);
	}

	protected ISelectionProvider getFocusedViewer() {
		return focusedViewer;
	}

	private void setVisibleColumns(String[] ids) {
		List<String> visibleNames = Arrays.asList(ids);
		for (int i = 0; i < columnNames.length; i++) {
			makeColumnVisible(visibleNames.contains(columnNames[i]), allColumns[i]);
		}
	}

	private void makeColumnVisible(boolean visible, TreeColumn column) {
		boolean isVisible = column.getWidth() > 0;
		if (isVisible != visible) {
			if (visible) {
				column.setResizable(true);
				column.pack();
				column.setWidth(column.getWidth() + COLUMN_WIDTH_PADDING);
			} else {
				column.setWidth(0);
				column.setResizable(false);
			}
		}
	}

	private String[] getVisibleColumns() {
		ArrayList<String> visibleNames = new ArrayList<String>();

		for (int i = 0; i < columnNames.length; i++) {
			if (allColumns[i].getWidth() > 0)
				visibleNames.add(columnNames[i]);
		}

		return visibleNames.toArray(new String[visibleNames.size()]);
	}

	/**
	 * Create the actions to refresh, import, and configure the columns
	 */
	private void createActions() {
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager toolBarManager = bars.getToolBarManager();

		refreshAction = createRefreshAction();
		toolBarManager.add(refreshAction);

		importAction = createImportAction();
		toolBarManager.add(importAction);
		
		removeAction = createRemoveAction();
		toolBarManager.add(removeAction);

		configureColumnsAction = createConfigureColumnsAction();
		toolBarManager.add(configureColumnsAction);
		
		copyAction = createCopyAction();
		bars.setGlobalActionHandler(ActionFactory.COPY.getId(), copyAction );

	}

	private Action createCopyAction() {
		Action action = new Action("Copy") { //$NON-NLS-1$
			
		};
		return action;
		
	}
	
	private Action createRemoveAction() {
		Action action = new Action(Messages.ExecutablesView_Remove) {
			
			@Override
			public void run() {				
				ISelection selection = getExecutablesViewer().getSelection();
				if (selection instanceof IStructuredSelection)
				{
					Object[] selectedObjects = ((IStructuredSelection)selection).toArray();
					ArrayList<Executable> selectedExes = new ArrayList<Executable>();
					for (Object object : selectedObjects) {
						if (object instanceof Executable)
							selectedExes.add((Executable) object);						
					}
					final Executable[] selectedExesArray = selectedExes.toArray(new Executable[selectedExes.size()]);
					
					boolean confirm = MessageDialog.openConfirm(getSite().getShell(), Messages.ExecutablesView_ConfirmRemoveExe, Messages.ExecutablesView_ConfirmRemoveSelected);
					
					if (confirm)
					{
						Job removeJob = new WorkbenchJob(Messages.ExecutablesView_RemoveExes) {

						@Override
						public IStatus runInUIThread(IProgressMonitor monitor) {
								IStatus result = ExecutablesManager.getExecutablesManager().removeExecutables(selectedExesArray, monitor);					
								if (result.getSeverity() != IStatus.OK)
								{
									StringBuffer message = new StringBuffer(result.getMessage());
									if (result.isMultiStatus()) {
										IStatus[] children = result.getChildren();
										for (int i = 0; i < children.length && i < 6; i++) {
											message.append(SEPARATOR);
											message.append(children[i].getMessage()); 
										}
									}
									MessageDialog.openWarning(getSite().getShell(), Messages.ExecutablesView_RemoveExes, message.toString());
								}
								return result;
							}
						};
						removeJob.schedule();						
					}
				}
				
			}
		};
		action.setToolTipText(Messages.ExecutablesView_RemoveSelectedExes);
		action.setImageDescriptor(ExecutablesView.DESC_REMOVE);
		action.setDisabledImageDescriptor(ExecutablesView.DESC_REMOVE_DISABLED);
		action.setEnabled(false);
		return action;
	}

	private Action createConfigureColumnsAction() {
		ConfigureColumnsAction action = new ConfigureColumnsAction();
		action.setToolTipText(Messages.ExecutablesView_Columns);
		action.setImageDescriptor(ExecutablesView.DESC_COLUMNS);
		action.setDisabledImageDescriptor(ExecutablesView.DESC_COLUMNS_DISABLED);
		action.setEnabled(true);
		return action;
	}

	protected void importExecutables(final String[] fileNames) {
		if (fileNames.length > 0) {

			Job importJob = new Job(Messages.ExecutablesView_ImportExecutables) {

				@Override
				public IStatus run(IProgressMonitor monitor) {
					ExecutablesManager.getExecutablesManager().importExecutables(fileNames, monitor);
					return Status.OK_STATUS;
				}
			};
			importJob.schedule();
		}
	}

	private Action createImportAction() {
		Action action = new Action(Messages.ExecutablesView_Import) {
			@Override
			public void run() {
				FileDialog dialog = new FileDialog(getViewSite().getShell(), SWT.NONE);
				dialog.setText(Messages.ExecutablesView_SelectExeFile);
				String res = dialog.open();
				if (res != null) {
					if (Platform.getOS().equals(Platform.OS_MACOSX) && res.endsWith(".app")) { //$NON-NLS-1$
						// On Mac OS X the file dialog will let you select the
						// package but not the executable inside.
						Path macPath = new Path(res);
						res = res + "/Contents/MacOS/" + macPath.lastSegment(); //$NON-NLS-1$
						res = res.substring(0, res.length() - 4);
					}
					importExecutables(new String[] { res });
				}
			}
		};
		action.setToolTipText(Messages.ExecutablesView_ImportExe);
		action.setImageDescriptor(ExecutablesView.DESC_IMPORT);
		action.setDisabledImageDescriptor(ExecutablesView.DESC_IMPORT_DISABLED);
		action.setEnabled(true);
		return action;
	}

	private Action createRefreshAction() {
		Action action = new Action(Messages.ExecutablesView_Refresh) {
			@Override
			public void run() {
				ExecutablesManager.getExecutablesManager().refresh(null);
				sourceFilesViewer.restartCanceledExecutableParse();
			}
		};
		action.setToolTipText(Messages.ExecutablesView_RefreshList);
		action.setImageDescriptor(ExecutablesView.DESC_REFRESH);
		action.setDisabledImageDescriptor(ExecutablesView.DESC_REFRESH_DISABLED);
		action.setEnabled(true);
		return action;
	}

	@Override
	public void setFocus() {
		// Set the focus
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		if (memento == null)
			this.memento = XMLMemento.createWriteRoot("EXECUTABLESVIEW"); //$NON-NLS-1$
		else
			this.memento = memento;
		super.init(site, memento);
	}

	private Preferences getViewPreferences() {
		return CDebugUIPlugin.getDefault().getPluginPreferences();
	}

	private void readSettings() {
		Preferences prefs = getViewPreferences();
		getExecutablesViewer().restoreColumnSettings(prefs);
		getSourceFilesViewer().restoreColumnSettings(prefs);
	}

	private void writeSettings() {
		Preferences prefs = getViewPreferences();
		getExecutablesViewer().saveColumnSettings(prefs);
		getSourceFilesViewer().saveColumnSettings(prefs);
	}

	@Override
	public void saveState(IMemento memento) {
		if (this.memento == null || memento == null)
			return;
		memento.putMemento(this.memento);
		writeSettings();
	}

	public SourceFilesViewer getSourceFilesViewer() {
		return sourceFilesViewer;
	}

	public ExecutablesViewer getExecutablesViewer() {
		return executablesViewer;
	}

	@Override
	public void dispose() {
		super.dispose();
	}

}
