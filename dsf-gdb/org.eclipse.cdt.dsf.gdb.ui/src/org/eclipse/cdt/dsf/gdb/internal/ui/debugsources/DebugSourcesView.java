/*******************************************************************************
 * Copyright (c) 2018 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Tracy Miranda / Baha El-Kassaby - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.debugsources;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceNotFoundElement;
import org.eclipse.cdt.debug.internal.core.sourcelookup.ICSourceNotFoundDescription;
import org.eclipse.cdt.debug.internal.ui.CDebugUIUtils;
import org.eclipse.cdt.debug.internal.ui.sourcelookup.CSourceNotFoundEditorInput;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerSuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.service.IDebugSourceFiles;
import org.eclipse.cdt.dsf.gdb.service.IDebugSourceFiles.IDebugSourceFileInfo;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.ViewPart;

public class DebugSourcesView extends ViewPart implements IDebugContextListener {

	public static final String ID = "org.eclipse.cdt.dsf.gdb.ui.debugsources.view"; //$NON-NLS-1$

	private DsfSession fSession;

	private TreeViewer viewer;

	private DebugSourcesViewComparator<?> comparator;

	public DebugSourcesView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final Text searchText = new Text(composite, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
		searchText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		searchText.setToolTipText(DebugSourcesMessages.DebugSourcesMessages_filter_search_tooltip);

		this.viewer = new TreeViewer(composite, SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER );
		viewer.setContentProvider(new ITreeContentProvider() {
			@Override
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof String) {
					return new String[] { (String) inputElement };
				}
				if (inputElement instanceof IDebugSourceFileInfo[]) {
					return (IDebugSourceFileInfo[]) inputElement;
				}
				return null;
			}

			@Override
			public Object[] getChildren(Object parentElement) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Object getParent(Object element) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean hasChildren(Object element) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		viewer.setUseHashlookup(true);
		Tree tree = viewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);

		comparator = new DebugSourcesViewComparator();

		// create columns
		createColumns(viewer);

		viewer.setComparator(comparator);

		viewer.setInput(DebugSourcesMessages.DebugSourcesMessages_initializing);

		// filtering
		final PatternFilter filter = new PatternFilter();
		viewer.addFilter(filter);
		searchText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (parent.isDisposed())
					return;
				filter.setPattern(searchText.getText());
				viewer.refresh();
			}
		});

		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection thisSelection = (IStructuredSelection) event.getSelection();
				Object selectedNode = thisSelection.getFirstElement();
				if (selectedNode instanceof IDebugSourceFileInfo) {
					IDebugSourceFileInfo new_name = (IDebugSourceFileInfo) selectedNode;
					openSourceFile(new_name);
				}
			}
		});

		registerForEvents();
		DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow())
				.addDebugContextListener(this);

	}

	private void createColumns(TreeViewer viewer) {
		String[] titles = { DebugSourcesMessages.DebugSourcesMessages_name_column,
				DebugSourcesMessages.DebugSourcesMessages_path_column };
		int[] bounds = { 200, 800 };
		ColumnViewerToolTipSupport.enableFor(viewer);

		// Add the first name column
		TreeViewerColumn tc = createTreeViewerColumn(titles[0], bounds[0], 0);
		tc.getColumn().setToolTipText(DebugSourcesMessages.DebugSourcesMessages_sort_name_column_tooltip);
		tc.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return getLabel(element, 0);
			}
		});

		// Add the path name column
		tc = createTreeViewerColumn(titles[1], bounds[1], 1);
		tc.getColumn().setToolTipText(DebugSourcesMessages.DebugSourcesMessages_sort_path_column_tooltip);
		tc.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return getLabel(element, 1);
			}
		});
	}

	private String getLabel(Object element, int columnIdx) {
		if (element instanceof String) {
			return (String) element;
		}
		if (element instanceof IDebugSourceFileInfo) {
			IDebugSourceFileInfo s = (IDebugSourceFileInfo) element;
			return columnIdx == 0 ? s.getName() : s.getPath();
		}
		return ""; //$NON-NLS-1$
	}

	private TreeViewerColumn createTreeViewerColumn(String title, int bound, final int colNumber) {
		final TreeViewerColumn viewerColumn = new TreeViewerColumn(viewer, SWT.NONE);
		final TreeColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		if (colNumber == 0)
			comparator.setColumn(e -> ((IDebugSourceFileInfo)e).getName(), colNumber);
		if (colNumber == 1)
			comparator.setColumn(e -> ((IDebugSourceFileInfo)e).getPath(), colNumber);

		column.addSelectionListener(getSelectionAdapter(column, colNumber));
		return viewerColumn;
	}

	private SelectionAdapter getSelectionAdapter(final TreeColumn column, final int index) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (index == 0)
					comparator.setColumn(e1 -> ((IDebugSourceFileInfo)e1).getName(), index);
				if (index == 1)
					comparator.setColumn(e1 -> ((IDebugSourceFileInfo)e1).getPath(), index);
				int dir = comparator.getDirection();
				viewer.getTree().setSortDirection(dir);
				viewer.getTree().setSortColumn(column);
				viewer.refresh();
			}
		};
		return selectionAdapter;
	}

	private void registerForEvents() {
		// Get the debug selection to know what the user is looking at in the Debug view
		IAdaptable context = DebugUITools.getDebugContext();
		if (context == null) {
			return;
		}

		// Extract the data model context to use with the DSF services
		IDMContext dmcontext = (IDMContext) context.getAdapter(IDMContext.class);
		if (dmcontext == null) {
			// Not dealing with a DSF session
			return;
		}

		// Extract DSF session id from the DM context
		String sessionId = dmcontext.getSessionId();
		// Get the full DSF session to have access to the DSF executor
		DsfSession session = DsfSession.getSession(sessionId);
		if (session == null) {
			// It could be that this session is no longer active
			return;
		}

		registerForEvents(session);

		// Show the current frame if there is one
		displaySourceFiles(session, dmcontext);
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Override
	public void dispose() {
		super.dispose();
		viewer.getControl().dispose();
	}

	/**
	 * This method registers with the specified session to receive DSF events.
	 * 
	 * @param session
	 *            The session for which we want to receive events
	 */
	private void registerForEvents(DsfSession session) {
		if (session != null) {
			fSession = session;
			fSession.getExecutor().submit(new DsfRunnable() {
				@Override
				public void run() {
					fSession.addServiceEventListener(DebugSourcesView.this, null);
				}
			});
		}
	}

	private void displaySourceFiles(DsfSession session, IDMContext dmcontext) {
		session.getExecutor().submit(new DsfRunnable() {
			@Override
			public void run() {
				// Get Stack service using a DSF services tracker object
				DsfServicesTracker tracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), session.getId());
				IDebugSourceFiles srcService = tracker.getService(IDebugSourceFiles.class);
				// Don't forgot to dispose of a tracker before it does out of scope
				tracker.dispose();

				if (srcService == null) {
					// service not available. The debug session
					// is probably terminating.
					return;
				}

				// Get the full DSF session to have access to the DSF executor
				srcService.getSources(dmcontext,
						new DataRequestMonitor<IDebugSourceFileInfo[]>(session.getExecutor(), null) {
							@Override
							protected void handleSuccess() {
								// The service called 'handleSuccess()' so we know there is no error.
								IDebugSourceFileInfo[] srcFileInfo = getData();
								// clean data
								String[] srcPaths = new String[srcFileInfo.length];
								String[] srcNames = getNameAnPathSrcInfo(srcPaths, srcFileInfo);
								// We have a frame context. It is just a 'pointer' though.
								// We need to get the data associated with it.
								Display.getDefault().asyncExec(new Runnable() {
									@Override
									public void run() {
										viewer.setInput(srcFileInfo);
										// String string = Arrays.toString(files);
										// // Pre-pend the current method:line to the log
										// fLogText.setText(string);
									}
								});

							}

							@Override
							protected void handleError() {
								// Ignore errors when we select elements
								// that don't contain frames
							}
						});
			}
		});
	}

	private String[] getNameAnPathSrcInfo(String[] srcPaths, IDebugSourceFileInfo[] srcInfo) {
		String[] srcNames = new String[srcInfo.length];
		for (int i = 0; i < srcInfo.length; i++) {
			srcNames[i] = srcInfo[i].getName();
			srcPaths[i] = srcInfo[i].getPath();
		}
		return srcNames;
	}

	// This method must be public for the DSF callback to be found
	@DsfServiceEventHandler
	public void eventReceived(ISuspendedDMEvent event) {
		// Most DSF event have a DM context
		IDMContext dmcontext = event.getDMContext();
		if (dmcontext == null) {
			return;
		}

		// Extract DSF session id from the DM context
		String sessionId = dmcontext.getSessionId();
		// Get the full DSF session to have access to the DSF executor
		DsfSession session = DsfSession.getSession(sessionId);

		// For container events (all-stop mode), extract the triggering thread
		if (event instanceof IContainerSuspendedDMEvent) {
			IExecutionDMContext[] triggers = ((IContainerSuspendedDMEvent) event).getTriggeringContexts();
			if (triggers != null && triggers.length > 0) {
				assert triggers.length == 1;
				dmcontext = triggers[0];
			}
		}

		displaySourceFiles(session, dmcontext);
	}

	@Override
	public void debugContextChanged(DebugContextEvent event) {
		if ((event.getFlags() & DebugContextEvent.ACTIVATED) > 0) {
			registerForEvents();
		}
	}

	@SuppressWarnings("restriction")
	public void openSourceFile(IDebugSourceFileInfo sourceFile) {
		if (sourceFile == null) {
			return;
		}

		String pathString = sourceFile.getPath();
		Path path = Paths.get(pathString);

		IEditorInput editorInput = null;
		String editorId = null;
		try {
			URI uriLocation = path.toUri();
			IFileStore fileStore = EFS.getStore(uriLocation);
			editorInput = new FileStoreEditorInput(fileStore);
			editorId = CDebugUIUtils.getEditorId(fileStore, false);
		} catch (CoreException e1) {
			CSourceNotFoundElement element = new CSourceNotFoundElement(new TempElement(), null, pathString);

			editorInput = new CSourceNotFoundEditorInput(element);
			editorId = ICDebugUIConstants.CSOURCENOTFOUND_EDITOR_ID;
		}

		IWorkbenchPage page = CUIPlugin.getActivePage();
		try {
			page.openEditor(editorInput, editorId);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	public class TempElement implements IAdaptable, ICSourceNotFoundDescription {

		@Override
		public <T> T getAdapter(Class<T> adapter) {
			if (adapter == ICSourceNotFoundDescription.class)
				return (T) this;
			return null;
		}

		@Override
		public String getDescription() {
			return DebugSourcesMessages.DebugSourcesMessages_unknown;
		}

		@Override
		public boolean isAddressOnly() {
			return false;
		}
	};

}
