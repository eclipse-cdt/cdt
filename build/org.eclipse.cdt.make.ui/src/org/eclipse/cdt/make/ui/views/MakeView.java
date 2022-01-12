/*******************************************************************************
 * Copyright (c) 2000, 2021 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Andrew Gvozdev (Quoin Inc.)
 *     Gaetano Santoro (STMicroelectronics)
 *******************************************************************************/
package org.eclipse.cdt.make.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetManager;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.internal.ui.dnd.AbstractContainerAreaDropAdapter;
import org.eclipse.cdt.make.internal.ui.dnd.AbstractSelectionDragAdapter;
import org.eclipse.cdt.make.internal.ui.dnd.FileTransferDropTargetListener;
import org.eclipse.cdt.make.internal.ui.dnd.LocalTransferDragSourceListener;
import org.eclipse.cdt.make.internal.ui.dnd.LocalTransferDropTargetListener;
import org.eclipse.cdt.make.internal.ui.dnd.MakeTargetTransfer;
import org.eclipse.cdt.make.internal.ui.dnd.MakeTargetTransferDragSourceListener;
import org.eclipse.cdt.make.internal.ui.dnd.MakeTargetTransferDropTargetListener;
import org.eclipse.cdt.make.internal.ui.dnd.TextTransferDragSourceListener;
import org.eclipse.cdt.make.internal.ui.dnd.TextTransferDropTargetListener;
import org.eclipse.cdt.make.ui.IMakeHelpContextIds;
import org.eclipse.cdt.make.ui.MakeContentProvider;
import org.eclipse.cdt.make.ui.MakeLabelProvider;
import org.eclipse.cdt.make.ui.TargetSourceContainer;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.bindings.BindingManagerEvent;
import org.eclipse.jface.bindings.IBindingManagerListener;
import org.eclipse.jface.util.DelegatingDragAdapter;
import org.eclipse.jface.util.DelegatingDropAdapter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ResourceWorkingSetFilter;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.TextActionHandler;
import org.eclipse.ui.actions.WorkingSetFilterActionGroup;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

/**
 * Implementation of Make Target View.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class MakeView extends ViewPart {
	private static final String TARGET_BUILD_LAST_COMMAND = "org.eclipse.cdt.make.ui.targetBuildLastCommand"; //$NON-NLS-1$
	// Persistance tags.
	private static final String TAG_SELECTION = "selection"; //$NON-NLS-1$
	private static final String TAG_EXPANDED = "expanded"; //$NON-NLS-1$
	private static final String TAG_CONTAINER = "container"; //$NON-NLS-1$
	private static final String TAG_PATH = "path"; //$NON-NLS-1$
	private static final String TAG_MAKE_TARGET = "makeTarget"; //$NON-NLS-1$
	private static final String TAG_SOURCE_CONTAINER = "sourceContainer"; //$NON-NLS-1$
	private static final String TAG_NAME = "name"; //$NON-NLS-1$
	private static final String TAG_VERTICAL_POSITION = "verticalPosition"; //$NON-NLS-1$
	private static final String TAG_HORIZONTAL_POSITION = "horizontalPosition"; //$NON-NLS-1$
	private static final String TAG_WORKINGSET = "workingSet"; //$NON-NLS-1$

	private Clipboard clipboard;

	private BuildTargetAction buildTargetAction;
	private RebuildLastTargetAction buildLastTargetAction;
	private EditTargetAction editTargetAction;
	private DeleteTargetAction deleteTargetAction;
	private AddTargetAction newTargetAction;
	private CopyTargetAction copyTargetAction;
	private PasteTargetAction pasteTargetAction;
	private TreeViewer fViewer;
	private DrillDownAdapter drillDownAdapter;
	private FilterEmtpyFoldersAction trimEmptyFolderAction;
	private IBindingService bindingService;
	private ResourceWorkingSetFilter workingSetFilter = new ResourceWorkingSetFilter();
	private WorkingSetFilterActionGroup workingSetGroup;
	private IMemento memento;

	private IPropertyChangeListener workingSetListener = ev -> {
		String property = ev.getProperty();
		Object newValue = ev.getNewValue();
		Object oldValue = ev.getOldValue();
		IWorkingSet filterWorkingSet = workingSetFilter.getWorkingSet();

		if (property == null) {
			return;
		}
		if (IWorkingSetManager.CHANGE_WORKING_SET_REMOVE.equals(property) && oldValue == filterWorkingSet) {
			setWorkingSet(null);
		} else if (IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE.equals(property)
				&& newValue == filterWorkingSet) {
			fViewer.refresh();
		}
	};

	IPropertyChangeListener workingSetUpdater = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			String property = event.getProperty();

			if (WorkingSetFilterActionGroup.CHANGE_WORKING_SET.equals(property)) {
				Object newValue = event.getNewValue();

				if (newValue instanceof IWorkingSet) {
					setWorkingSet((IWorkingSet) newValue);
				} else if (newValue == null) {
					setWorkingSet(null);
				}
			}
		}
	};

	public MakeView() {
		super();
	}

	@Override
	public void setFocus() {
		fViewer.getTree().setFocus();
	}

	@Override
	public void createPartControl(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IMakeHelpContextIds.MAKE_VIEW);
		fViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		fViewer.setUseHashlookup(true);
		fViewer.setContentProvider(new MakeContentProvider());
		fViewer.setLabelProvider(new MakeLabelProvider());
		initFilters(fViewer);

		initDragAndDrop();

		drillDownAdapter = new DrillDownAdapter(fViewer);

		fViewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick(event);
			}
		});
		fViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				handleSelectionChanged(event);
			}
		});

		fViewer.setComparator(new ViewerComparator() {
			@Override
			public int category(Object element) {
				if (element instanceof TargetSourceContainer) {
					return 1;
				} else if (element instanceof IResource) {
					return 2;
				}
				return 3;
			}
		});
		fViewer.setInput(ResourcesPlugin.getWorkspace().getRoot());

		if (memento != null) {
			// The working set selection needs to be restored prior to making the actions
			// and connecting to the manager
			restoreStateWorkingSetSelection(memento);
		}

		//Add the property changes after all of the UI work has been done.
		IWorkingSetManager wsmanager = getViewSite().getWorkbenchWindow().getWorkbench().getWorkingSetManager();
		wsmanager.addPropertyChangeListener(workingSetListener);

		getSite().setSelectionProvider(fViewer);

		makeActions();
		hookContextMenu();
		contributeToActionBars();

		updateActions((IStructuredSelection) fViewer.getSelection());

		bindingService = PlatformUI.getWorkbench().getService(IBindingService.class);
		if (bindingService != null) {
			bindingService.addBindingManagerListener(bindingManagerListener);
		}

		if (memento != null) {
			restoreState(memento);
		}
		memento = null;
	}

	/**
	 * Initialize drag and drop operations.
	 */
	private void initDragAndDrop() {
		int opers = DND.DROP_COPY | DND.DROP_MOVE;

		// LocalSelectionTransfer is used inside Make Target View
		// TextTransfer is used to drag outside the View or eclipse
		Transfer[] dragTransfers = { LocalSelectionTransfer.getTransfer(), MakeTargetTransfer.getInstance(),
				TextTransfer.getInstance(), };
		AbstractSelectionDragAdapter[] dragListeners = { new LocalTransferDragSourceListener(fViewer),
				new MakeTargetTransferDragSourceListener(fViewer), new TextTransferDragSourceListener(fViewer), };

		DelegatingDragAdapter delegatingDragAdapter = new DelegatingDragAdapter();
		for (AbstractSelectionDragAdapter dragListener : dragListeners) {
			delegatingDragAdapter.addDragSourceListener(dragListener);
		}
		fViewer.addDragSupport(opers, dragTransfers, delegatingDragAdapter);

		Transfer[] dropTransfers = { LocalSelectionTransfer.getTransfer(), MakeTargetTransfer.getInstance(),
				FileTransfer.getInstance(), TextTransfer.getInstance(), };
		AbstractContainerAreaDropAdapter[] dropListeners = { new LocalTransferDropTargetListener(fViewer),
				new MakeTargetTransferDropTargetListener(fViewer), new FileTransferDropTargetListener(fViewer),
				new TextTransferDropTargetListener(fViewer), };
		DelegatingDropAdapter delegatingDropAdapter = new DelegatingDropAdapter();
		for (AbstractContainerAreaDropAdapter dropListener : dropListeners) {
			delegatingDropAdapter.addDropTargetListener(dropListener);
		}
		fViewer.addDropSupport(opers | DND.DROP_DEFAULT, dropTransfers, delegatingDropAdapter);
	}

	private void makeActions() {
		Shell shell = fViewer.getControl().getShell();

		clipboard = new Clipboard(shell.getDisplay());

		buildTargetAction = new BuildTargetAction(shell);
		buildLastTargetAction = new RebuildLastTargetAction();
		newTargetAction = new AddTargetAction(shell);
		copyTargetAction = new CopyTargetAction(shell, clipboard, pasteTargetAction);
		pasteTargetAction = new PasteTargetAction(shell, clipboard);
		deleteTargetAction = new DeleteTargetAction(shell);
		editTargetAction = new EditTargetAction(shell);
		trimEmptyFolderAction = new FilterEmtpyFoldersAction(fViewer);
		workingSetGroup = new WorkingSetFilterActionGroup(shell, workingSetUpdater);
		workingSetGroup.setWorkingSet(getWorkingSet());
	}

	private void contributeToActionBars() {
		IActionBars actionBars = getViewSite().getActionBars();
		fillLocalPullDown(actionBars.getMenuManager());
		fillLocalToolBar(actionBars.getToolBarManager());

		TextActionHandler textActionHandler = new TextActionHandler(actionBars); // hooks handlers
		textActionHandler.setCopyAction(copyTargetAction);
		textActionHandler.setPasteAction(pasteTargetAction);
		textActionHandler.setDeleteAction(deleteTargetAction);

		actionBars.setGlobalActionHandler(ActionFactory.RENAME.getId(), editTargetAction);

		workingSetGroup.fillActionBars(actionBars);
	}

	private void fillLocalToolBar(IToolBarManager toolBar) {
		toolBar.add(newTargetAction);
		toolBar.add(editTargetAction);
		toolBar.add(buildTargetAction);
		toolBar.add(new Separator());
		drillDownAdapter.addNavigationActions(toolBar);
		toolBar.add(trimEmptyFolderAction);
	}

	private void fillLocalPullDown(IMenuManager manager) {
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {

			@Override
			public void menuAboutToShow(IMenuManager manager) {
				MakeView.this.fillContextMenu(manager);
				updateActions((IStructuredSelection) fViewer.getSelection());
			}
		});
		Menu menu = menuMgr.createContextMenu(fViewer.getControl());
		fViewer.getControl().setMenu(menu);
		// getSite().registerContextMenu(menuMgr, viewer);
	}

	protected void fillContextMenu(IMenuManager manager) {
		manager.add(newTargetAction);
		manager.add(editTargetAction);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		manager.add(new Separator());
		manager.add(copyTargetAction);
		manager.add(pasteTargetAction);
		manager.add(deleteTargetAction);
		manager.add(new Separator());
		manager.add(buildTargetAction);
		manager.add(buildLastTargetAction);

		// Other plug-ins can contribute there actions here
		// manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	protected void handleDoubleClick(DoubleClickEvent event) {
		buildTargetAction.run();
	}

	void handleSelectionChanged(SelectionChangedEvent event) {
		IStructuredSelection sel = (IStructuredSelection) event.getSelection();
		updateActions(sel);
	}

	void updateActions(IStructuredSelection sel) {
		newTargetAction.selectionChanged(sel);
		buildTargetAction.selectionChanged(sel);
		buildLastTargetAction.selectionChanged(sel);
		deleteTargetAction.selectionChanged(sel);
		editTargetAction.selectionChanged(sel);
		copyTargetAction.selectionChanged(sel);
		pasteTargetAction.selectionChanged(sel);
		workingSetGroup.updateActionBars();
	}

	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		if (clipboard != null) {
			clipboard.dispose();
			clipboard = null;
		}

		if (bindingService != null) {
			bindingService.removeBindingManagerListener(bindingManagerListener);
			bindingService = null;
		}

		IWorkingSetManager wsmanager = getViewSite().getWorkbenchWindow().getWorkbench().getWorkingSetManager();
		if (workingSetListener != null) {
			wsmanager.removePropertyChangeListener(workingSetListener);
			workingSetListener = null;
		}

		super.dispose();
	}

	private IBindingManagerListener bindingManagerListener = new IBindingManagerListener() {

		@Override
		public void bindingManagerChanged(BindingManagerEvent event) {

			if (event.isActiveBindingsChanged()) {
				String keyBinding = bindingService
						.getBestActiveBindingFormattedFor(IWorkbenchCommandConstants.FILE_RENAME);
				if (keyBinding != null)
					editTargetAction
							.setText(MakeUIPlugin.getResourceString("EditTargetAction.label") + "\t" + keyBinding); //$NON-NLS-1$ //$NON-NLS-2$

				keyBinding = bindingService.getBestActiveBindingFormattedFor(IWorkbenchCommandConstants.EDIT_COPY);
				if (keyBinding != null)
					copyTargetAction
							.setText(MakeUIPlugin.getResourceString("CopyTargetAction.label") + "\t" + keyBinding); //$NON-NLS-1$ //$NON-NLS-2$

				keyBinding = bindingService.getBestActiveBindingFormattedFor(IWorkbenchCommandConstants.EDIT_PASTE);
				if (keyBinding != null)
					pasteTargetAction
							.setText(MakeUIPlugin.getResourceString("PasteTargetAction.label") + "\t" + keyBinding); //$NON-NLS-1$ //$NON-NLS-2$

				keyBinding = bindingService.getBestActiveBindingFormattedFor(IWorkbenchCommandConstants.EDIT_DELETE);
				if (keyBinding != null)
					deleteTargetAction
							.setText(MakeUIPlugin.getResourceString("DeleteTargetAction.label") + "\t" + keyBinding); //$NON-NLS-1$ //$NON-NLS-2$

				keyBinding = bindingService.getBestActiveBindingFormattedFor(TARGET_BUILD_LAST_COMMAND);
				if (keyBinding != null)
					buildLastTargetAction
							.setText(MakeUIPlugin.getResourceString("BuildLastTargetAction.label") + "\t" + keyBinding); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	};

	/**
	 * Returns the working set filter for this view.
	 *
	 * @return the working set
	 * @since 8.1
	 */
	public IWorkingSet getWorkingSet() {
		return workingSetFilter.getWorkingSet();
	}

	/**
	 * Adds the filters to the viewer.
	 *
	 * @param viewer
	 *            the viewer
	 */
	void initFilters(TreeViewer viewer) {
		viewer.addFilter(workingSetFilter);
	}

	private void saveState(IMemento memento, Object[] elements) {
		for (Object element : elements) {
			if (element instanceof IMakeTarget) {
				IMakeTarget makeTarget = (IMakeTarget) element;
				IContainer c = makeTarget.getContainer();
				if (c.getFullPath() != null) {
					IMemento elementMem = memento.createChild(TAG_MAKE_TARGET);
					elementMem.putString(TAG_PATH, c.getFullPath().toPortableString());
					elementMem.putString(TAG_NAME, makeTarget.getName());
				}
			} else if (element instanceof TargetSourceContainer) {
				TargetSourceContainer targetSourceContainer = (TargetSourceContainer) element;
				IContainer c = targetSourceContainer.getContainer();
				if (c.getFullPath() != null) {
					IMemento elementMem = memento.createChild(TAG_SOURCE_CONTAINER);
					elementMem.putString(TAG_PATH, c.getFullPath().toPortableString());
				}
			} else if (element instanceof IContainer) {
				IContainer c = (IContainer) element;
				if (c.getLocation() != null) {
					IMemento elementMem = memento.createChild(TAG_CONTAINER);
					elementMem.putString(TAG_PATH, c.getFullPath().toPortableString());
				}
			} else {
				// Unexpected/unknown type, expansion or selection of it will not be restored
			}
		}
	}

	private List<Object> restoreStateElements(IMemento memento) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IMakeTargetManager makeTargetManager = MakeCorePlugin.getDefault().getTargetManager();

		List<Object> elements = new ArrayList<>();
		IMemento[] makeTargetMementos = memento.getChildren(TAG_MAKE_TARGET);
		for (IMemento mem : makeTargetMementos) {
			String path = mem.getString(TAG_PATH);
			String name = mem.getString(TAG_NAME);
			if (path == null || name == null) {
				continue;
			}
			IResource res = root.findMember(Path.fromPortableString(path));
			if (res instanceof IContainer) {
				IContainer container = (IContainer) res;
				try {
					IMakeTarget makeTarget = makeTargetManager.findTarget(container, name);
					if (makeTarget != null) {
						elements.add(makeTarget);
					}
				} catch (CoreException e) {
					continue;
				}
			}
		}

		IMemento[] sourceContainerMementos = memento.getChildren(TAG_SOURCE_CONTAINER);
		for (IMemento mem : sourceContainerMementos) {
			String path = mem.getString(TAG_PATH);
			if (path == null) {
				continue;
			}
			IResource res = root.findMember(Path.fromPortableString(path));
			if (res == null) {
				continue;
			}
			IProject project = res.getProject();
			ICSourceEntry[] srcEntries = getSourceEntries(project);
			for (ICSourceEntry srcEntry : srcEntries) {
				if (srcEntry.getFullPath().equals(res.getFullPath())) {
					elements.add(new TargetSourceContainer(srcEntry));
				}
			}
		}

		IMemento[] containerMementos = memento.getChildren(TAG_CONTAINER);
		for (IMemento mem : containerMementos) {
			String path = mem.getString(TAG_PATH);
			if (path == null) {
				continue;
			}
			IResource res = root.findMember(Path.fromPortableString(path));
			if (res == null) {
				continue;
			}
			elements.add(res);
		}

		return elements;
	}

	@Override
	public void saveState(IMemento memento) {
		if (fViewer == null) {
			if (this.memento != null) { //Keep the old state;
				memento.putMemento(this.memento);
			}
			return;
		}

		//save expanded elements
		Tree tree = fViewer.getTree();
		Object[] expandedElements = fViewer.getExpandedElements();
		if (expandedElements.length > 0) {
			IMemento expandedMem = memento.createChild(TAG_EXPANDED);
			saveState(expandedMem, expandedElements);
		}

		//save selection
		Object[] elements = ((IStructuredSelection) fViewer.getSelection()).toArray();
		if (elements.length > 0) {
			IMemento selectionMem = memento.createChild(TAG_SELECTION);
			saveState(selectionMem, elements);
		}

		//save vertical position
		ScrollBar bar = tree.getVerticalBar();
		int position = bar != null ? bar.getSelection() : 0;
		memento.putInteger(TAG_VERTICAL_POSITION, position);
		//save horizontal position
		bar = tree.getHorizontalBar();
		position = bar != null ? bar.getSelection() : 0;
		memento.putInteger(TAG_HORIZONTAL_POSITION, position);

		//Save the working set away
		if (workingSetFilter.getWorkingSet() != null) {
			String wsname = workingSetFilter.getWorkingSet().getName();
			if (wsname != null) {
				memento.putString(TAG_WORKINGSET, wsname);
			}
		}
	}

	private void restoreState(IMemento memento) {

		IMemento expandedMemento = memento.getChild(TAG_EXPANDED);
		if (expandedMemento != null) {
			fViewer.setExpandedElements(restoreStateElements(expandedMemento).toArray());
		}
		IMemento selectionMemento = memento.getChild(TAG_SELECTION);
		if (selectionMemento != null) {
			fViewer.setSelection(new StructuredSelection(restoreStateElements(selectionMemento)));
		}

		Tree tree = fViewer.getTree();
		ScrollBar bar = tree.getVerticalBar();
		if (bar != null) {
			Integer position = memento.getInteger(TAG_VERTICAL_POSITION);
			if (position != null) {
				bar.setSelection(position);
			}
		}
		bar = tree.getHorizontalBar();
		if (bar != null) {
			Integer position = memento.getInteger(TAG_HORIZONTAL_POSITION);
			if (position != null) {
				bar.setSelection(position);
			}
		}
	}

	private void restoreStateWorkingSetSelection(IMemento memento) {
		String wsname = memento.getString(TAG_WORKINGSET);
		if (wsname != null && !wsname.isEmpty()) {
			IWorkingSetManager wsmanager = getViewSite().getWorkbenchWindow().getWorkbench().getWorkingSetManager();
			IWorkingSet workingSet = wsmanager.getWorkingSet(wsname);
			if (workingSet != null) {
				workingSetFilter.setWorkingSet(workingSet);
			}
		}
	}

	/**
	 * @since 8.1
	 */
	public void setWorkingSet(IWorkingSet workingSet) {
		Object[] expanded = fViewer.getExpandedElements();
		ISelection selection = fViewer.getSelection();

		workingSetFilter.setWorkingSet(workingSet);
		fViewer.refresh();
		fViewer.setExpandedElements(expanded);
		if (selection.isEmpty() == false && selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			fViewer.reveal(structuredSelection.getFirstElement());
		}
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		this.memento = memento;
	}

	/**
	 * Get source entries for default setting configuration (i.e. configuration shown in UI).
	 */
	private static ICSourceEntry[] getSourceEntries(IProject project) {
		ICProjectDescriptionManager mgr = CCorePlugin.getDefault().getProjectDescriptionManager();
		ICProjectDescription prjDescription = mgr.getProjectDescription(project, false);
		if (prjDescription != null) {
			ICConfigurationDescription cfgDescription = prjDescription.getDefaultSettingConfiguration();
			if (cfgDescription != null) {
				ICSourceEntry[] srcEntries = cfgDescription.getResolvedSourceEntries();
				return srcEntries;
			}
		}

		return new ICSourceEntry[0];
	}

}
