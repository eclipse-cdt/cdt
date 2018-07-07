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
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceNotFoundElement;
import org.eclipse.cdt.debug.internal.core.sourcelookup.ICSourceNotFoundDescription;
import org.eclipse.cdt.debug.internal.ui.CDebugUIUtils;
import org.eclipse.cdt.debug.internal.ui.sourcelookup.CSourceNotFoundEditorInput;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.internal.ui.debugsources.actions.DebugSourcesCollapseAction;
import org.eclipse.cdt.dsf.gdb.internal.ui.debugsources.actions.DebugSourcesExpandAction;
import org.eclipse.cdt.dsf.gdb.internal.ui.debugsources.actions.DebugSourcesFlattendedTree;
import org.eclipse.cdt.dsf.gdb.internal.ui.debugsources.actions.DebugSourcesNormalTree;
import org.eclipse.cdt.dsf.gdb.internal.ui.debugsources.actions.DebugSourcesShowExistingFilesOnly;
import org.eclipse.cdt.dsf.gdb.internal.ui.debugsources.tree.DebugTree;
import org.eclipse.cdt.dsf.gdb.service.IDebugSourceFiles;
import org.eclipse.cdt.dsf.gdb.service.IDebugSourceFiles.IDebugSourceFileInfo;
import org.eclipse.cdt.dsf.gdb.service.IDebugSourceFiles.IDebugSourceFilesChangedEvent;
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
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.ViewPart;

public class DebugSourcesView extends ViewPart implements IDebugContextListener {

	public static final String ID = "org.eclipse.cdt.dsf.gdb.ui.debugsources.view"; //$NON-NLS-1$

	private DsfSession fSession;

	private TreeViewer viewer;

	private DebugSourcesViewComparator<DebugTree> comparator;

	private TreeViewerColumn[] viewerColumns;

	private IContainerDMContext dmcontext;

	public DebugSourcesView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final DebugPatternFilter filter = new DebugPatternFilter();

		FilteredTree tree = new FilteredTree(composite,
				SWT.MULTI | SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, filter, true);
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tree.getFilterControl().setToolTipText(DebugSourcesMessages.DebugSourcesMessages_filter_search_tooltip);
		viewer = tree.getViewer();
		viewer.getTree().setLinesVisible(true);
		viewer.getTree().setHeaderVisible(true);
		viewer.setContentProvider(new DebugSourcesTreeContentProvider(true, true));
		viewer.setUseHashlookup(true);

		comparator = new DebugSourcesViewComparator<DebugTree>();

		// create columns
		viewerColumns = createColumns(viewer);

		viewer.setComparator(comparator);
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection thisSelection = (IStructuredSelection) event.getSelection();
				Object selectedNode = thisSelection.getFirstElement();
				if (selectedNode instanceof IDebugSourceFileInfo) {
					IDebugSourceFileInfo newName = (IDebugSourceFileInfo) selectedNode;
					openSourceFile(newName.getPath(), true);
				}
				if (selectedNode instanceof DebugTree) {
					DebugTree<?> node = (DebugTree<?>) selectedNode;
					// only leafs can be opened!
					if (!node.hasChildren()) {
						openSourceFile((String) node.getLeafData(), node.getExists());
					}
				}
			}
		});

		createActions(viewer, viewerColumns);

		registerForEvents();
		DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow())
				.addDebugContextListener(this);

	}

	private TreeViewerColumn[] createColumns(TreeViewer viewer) {
		String[] titles = { DebugSourcesMessages.DebugSourcesMessages_name_column,
				DebugSourcesMessages.DebugSourcesMessages_path_column };
		String[] tooltips = { DebugSourcesMessages.DebugSourcesMessages_sort_name_column_tooltip,
				DebugSourcesMessages.DebugSourcesMessages_sort_path_column_tooltip };
		int[] bounds = { 300, 800 };
		ColumnViewerToolTipSupport.enableFor(viewer);

		TreeViewerColumn[] columns = new TreeViewerColumn[titles.length];
		for (int i = 0; i < titles.length; i++) {
			TreeViewerColumn tc = createTreeViewerColumn(viewer, titles[i], bounds[i], i);
			tc.getColumn().setToolTipText(tooltips[i]);
			tc.setLabelProvider(DebugSourcesLabelProvider.FLATTENED[i]);
			columns[i] = tc;
		}
		return columns;
	}

	private TreeViewerColumn createTreeViewerColumn(TreeViewer viewer, String title, int bound, final int colNumber) {
		final TreeViewerColumn viewerColumn = new TreeViewerColumn(viewer, SWT.NONE);
		final TreeColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		if (colNumber == 0)
			comparator.setColumn(e -> e.getData(), colNumber);
		if (colNumber == 1)
			comparator.setColumn(e -> e.getLeafData() != null ? e.getLeafData() : e.getData(), colNumber);

		column.addSelectionListener(getSelectionAdapter(column, colNumber));
		return viewerColumn;
	}

	private SelectionAdapter getSelectionAdapter(final TreeColumn column, final int index) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (index == 0)
					comparator.setColumn(e1 -> e1.getData(), index);
				if (index == 1)
					comparator.setColumn(e1 -> e1.getLeafData() != null ? e1.getLeafData() : e1.getData(), index);
				int dir = comparator.getDirection();
				viewer.getTree().setSortDirection(dir);
				viewer.getTree().setSortColumn(column);
				viewer.refresh();
				// This is due to the tree collapsing to level 2 at every comparison
				viewer.expandAll();
			}
		};
		return selectionAdapter;
	}

	private void createActions(TreeViewer viewer, TreeViewerColumn[] viewerColumns) {
		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager toolBar = actionBars.getToolBarManager();
		toolBar.add(new DebugSourcesExpandAction(viewer));
		toolBar.add(new DebugSourcesCollapseAction(viewer));
		toolBar.add(new DebugSourcesFlattendedTree(viewer, viewerColumns));
		toolBar.add(new DebugSourcesNormalTree(viewer, viewerColumns));
		toolBar.add(new DebugSourcesShowExistingFilesOnly(viewer));
	}

	private DsfSession getSession() {
		if (viewer == null || viewer.getControl().isDisposed()) {
			return null;
		}

		// Get the debug selection to know what the user is looking at in the Debug view
		IAdaptable context = DebugUITools.getDebugContext();
		if (context == null) {
			return null;
		}

		// Extract the data model context to use with the DSF services
		IDMContext dmcontext = context.getAdapter(IDMContext.class);
		if (dmcontext == null) {
			// Not dealing with a DSF session
			return null;
		}

		// Extract DSF session id from the DM context
		String sessionId = dmcontext.getSessionId();
		// Get the full DSF session to have access to the DSF executor
		DsfSession session = DsfSession.getSession(sessionId);
		if (session == null) {
			// It could be that this session is no longer active
			return null;
		}

		if (!session.isActive() || session.getExecutor().isShutdown()) {
			return null;
		}

		return session;
	}

	private void asyncExecRegisterForEvents() {
		if (getSite() == null || getSite().getShell() == null || getSite().getShell().getDisplay() == null
				|| getSite().getShell().getDisplay().isDisposed()) {
			return;
		}
		getSite().getShell().getDisplay().asyncExec(this::registerForEvents);
	}

	private void registerForEvents() {
		DsfSession session = getSession();
		if (session == null) {
			return;
		}

		// Get the debug selection to know what the user is looking at in the Debug view
		IAdaptable context = DebugUITools.getDebugContext();
		if (context == null) {
			return;
		}

		// Extract the data model context to use with the DSF services
		IDMContext dmcontext = context.getAdapter(IDMContext.class);
		if (dmcontext == null) {
			// Not dealing with a DSF session
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
		DebugUITools.getDebugContextManager().getContextService(getSite().getWorkbenchWindow())
				.removeDebugContextListener(this);
		if (fSession != null) {
			DsfSession lastSession = fSession;
			if (!lastSession.getExecutor().isShutdown()) {
				lastSession.getExecutor().submit(new DsfRunnable() {
					@Override
					public void run() {
						lastSession.removeServiceEventListener(DebugSourcesView.this);
					}
				});
			}
		}
	}

	/**
	 * This method registers with the specified session to receive DSF events.
	 * 
	 * @param session
	 *            The session for which we want to receive events
	 */
	private void registerForEvents(DsfSession session) {
		if (session != null) {
			if (fSession != session) {
				if (fSession != null) {
					DsfSession lastSession = fSession;
					if (!lastSession.getExecutor().isShutdown()) {
						lastSession.getExecutor().submit(new DsfRunnable() {
							@Override
							public void run() {
								lastSession.removeServiceEventListener(DebugSourcesView.this);
							}
						});
					}
				}
				fSession = session;
				fSession.getExecutor().submit(new DsfRunnable() {
					@Override
					public void run() {
						fSession.addServiceEventListener(DebugSourcesView.this, null);
					}
				});
			}
		}
	}

	private void displaySourceFiles(DsfSession session, IDMContext dmcontext) {
		if (session.getExecutor().isShutdown()) {
			// can't do anything
			return;
		}

		IContainerDMContext containerDMContext = DMContexts.getAncestorOfType(dmcontext, IContainerDMContext.class);
		if (containerDMContext == null || Objects.equals(containerDMContext, this.dmcontext)) {
			return;
		}
		this.dmcontext = containerDMContext;
		session.getExecutor().submit(new DsfRunnable() {
			@Override
			public void run() {
				DsfServicesTracker tracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), session.getId());
				IDebugSourceFiles srcService = tracker.getService(IDebugSourceFiles.class);
				// Don't forgot to dispose of a tracker before it goes out of scope
				tracker.dispose();

				if (srcService == null) {
					// service not available. The debug session
					// is probably terminating.
					return;
				}

				// Get the full DSF session to have access to the DSF executor
				srcService.getSources(containerDMContext,
						new DataRequestMonitor<IDebugSourceFileInfo[]>(session.getExecutor(), null) {
							@Override
							protected void handleSuccess() {
								// The service called 'handleSuccess()' so we know there is no error.
								IDebugSourceFileInfo[] srcFileInfo = getData();
								// We have a frame context. It is just a 'pointer' though.
								// We need to get the data associated with it.
								DebugTree<?> populateTree = populateTree(srcFileInfo);
								Display.getDefault().asyncExec(new Runnable() {
									@Override
									public void run() {
										if (!viewer.getControl().isDisposed()) {
											populateTree.toString();
											viewer.setInput(populateTree);
											// TODO: Expanding all when there are 15k files
											// is noticeably slow.
											// viewer.expandAll();
										}
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

	private DebugTree<?> populateTree(IDebugSourceFileInfo[] srcFileInfo) {
		DebugTree<String> debugTree = new DebugTree<String>("", false); //$NON-NLS-1$
		DebugTree<String> current = debugTree;
		for (int i = 0; i < srcFileInfo.length; i++) {
			DebugTree<String> root = current;
			DebugTree<String> parent = root;
			String path = srcFileInfo[i].getPath();
			// Use Path API to clean the path
			try {
				Path p = Paths.get(path);
				// Run a task of checking if file exists specified by a Supplier object asynchronously
				CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(new Supplier<Boolean>() {
					@Override
					public Boolean get() {
						boolean exists = true;
						exists = Files.exists(p);
						return exists;
					}
				});

				// Block and get the result of the Future
				boolean exists = true;
				try {
					exists = future.get();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
				
				Path filename = p.getFileName();
				// add root
				Path pRoot = p.getRoot();
				if (pRoot == null || !p.isAbsolute()) {
					current = current.addLeaf(DebugSourcesMessages.DebugSourcesView_unrooted, p.toString(), exists);
				} else if (pRoot.equals(filename)) {
					current = current.addLeaf(srcFileInfo[i].getName(), p.toString(), exists);
				} else {
					current = current.addNode(pRoot.toString(), exists);
				}
				parent = current;
				// Add each sub-path
				Path normalizedPath = p.normalize();
				for (Path subpath : normalizedPath) {
					if (subpath.equals(filename)) { // this is a leaf
						current = current.addLeaf(srcFileInfo[i].getName(), p.toString(), exists);
					} else {
						current = current.addNode(subpath.toString(), exists);
					}
					current.setParent(parent);
					parent = current;
				}
				current = root;
				parent = root;
			} catch (InvalidPathException e) {
				// do nothing
				System.out.println(e.getMessage());
			}
		}
		return current;
	}

	// This method must be public for the DSF callback to be found
	@DsfServiceEventHandler
	public void eventReceived(ISuspendedDMEvent event) {
		asyncExecRegisterForEvents();
	}

	// This method must be public for the DSF callback to be found
	@DsfServiceEventHandler
	public void eventReceived(IDebugSourceFilesChangedEvent event) {
		asyncExecRegisterForEvents();
	}

	public boolean canRefresh() {
		return getSession() != null;
	}

	public void refresh() {
		this.dmcontext = null; // force the refresh
		DsfSession session = getSession();
		if (session == null) {
			return;
		}
		session.getExecutor().submit(new DsfRunnable() {

			@Override
			public void run() {
				DsfServicesTracker tracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), session.getId());
				IDebugSourceFiles srcService = tracker.getService(IDebugSourceFiles.class);
				// Don't forgot to dispose of a tracker before it goes out of scope
				tracker.dispose();

				if (srcService instanceof ICachingService) {
					ICachingService cache = (ICachingService) srcService;
					cache.flushCache(dmcontext);
				}
			}
		});
	}

	@Override
	public void debugContextChanged(DebugContextEvent event) {
		if ((event.getFlags() & DebugContextEvent.ACTIVATED) > 0) {
			registerForEvents();
		}
	}

	private void openSourceFile(String fullPath, boolean exist) {
		if (fullPath == null) {
			return;
		}
		Path path = Paths.get(fullPath);
		IEditorInput editorInput = null;
		String editorId = null;
		if (exist) {
			try {
				URI uriLocation = path.toUri();
				IFileStore fileStore = EFS.getStore(uriLocation);
				editorInput = new FileStoreEditorInput(fileStore);
				editorId = CDebugUIUtils.getEditorId(fileStore, false);
			} catch (CoreException e1) {
				CSourceNotFoundElement element = new CSourceNotFoundElement(new TempElement(), null, fullPath);
				editorInput = new CSourceNotFoundEditorInput(element);
				editorId = ICDebugUIConstants.CSOURCENOTFOUND_EDITOR_ID;
			}
		} else {
			CSourceNotFoundElement element = new CSourceNotFoundElement(new TempElement(), null, fullPath);
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

	private class TempElement implements IAdaptable, ICSourceNotFoundDescription {

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

	class DebugPatternFilter extends PatternFilter {

		@Override
		protected boolean isLeafMatch(Viewer viewer, Object element) {
			String name = (String) ((DebugTree<?>) element).getData();
			String path = (String) ((DebugTree<?>) element).getLeafData();
			return wordMatches(path) || wordMatches(name);
		}
	}

}
