/******************************************************************************* 
 * Copyright (c) 2000, 2003 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials! 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * Contributors: 
 *   IBM Corporation - initial API and implementation 
 ************************************************************************/
package org.eclipse.cdt.internal.ui.cview;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.IContextMenuConstants;
import org.eclipse.cdt.internal.ui.editor.FileSearchAction;
import org.eclipse.cdt.internal.ui.editor.FileSearchActionInWorkingSet;
import org.eclipse.cdt.internal.ui.editor.OpenIncludeAction;
import org.eclipse.cdt.internal.ui.editor.SearchDialogAction;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.AddBookmarkAction;
import org.eclipse.ui.actions.BuildAction;
import org.eclipse.ui.actions.CloseResourceAction;
import org.eclipse.ui.actions.ExportResourcesAction;
import org.eclipse.ui.actions.ImportResourcesAction;
import org.eclipse.ui.actions.NewWizardMenu;
import org.eclipse.ui.actions.OpenFileAction;
import org.eclipse.ui.actions.OpenInNewWindowAction;
import org.eclipse.ui.actions.OpenResourceAction;
import org.eclipse.ui.actions.OpenSystemEditorAction;
import org.eclipse.ui.actions.OpenWithMenu;
import org.eclipse.ui.actions.RefreshAction;
import org.eclipse.ui.actions.WorkingSetFilterActionGroup;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.views.framelist.BackAction;
import org.eclipse.ui.views.framelist.ForwardAction;
import org.eclipse.ui.views.framelist.FrameList;
import org.eclipse.ui.views.framelist.GoIntoAction;
import org.eclipse.ui.views.framelist.UpAction;

/**
 * The main action group for the cview.
 * This contains a few actions and several subgroups.
 */
public class MainActionGroup extends CViewActionGroup {
	// Actions for Menu context.
	AddBookmarkAction addBookmarkAction;
	OpenFileAction openFileAction;
	OpenSystemEditorAction openSystemEditorAction;
	PropertyDialogAction propertyDialogAction;
	ImportResourcesAction importAction;
	ExportResourcesAction exportAction;
	RefreshAction refreshAction;

	CloseResourceAction closeProjectAction;
	OpenResourceAction openProjectAction;
	RefactorActionGroup refactorGroup;
	BuildAction buildAction;
	BuildAction rebuildAction;

	// CElement action
	OpenIncludeAction openIncludeAction;

	BackAction backAction;
	ForwardAction forwardAction;
	GoIntoAction goIntoAction;
	UpAction upAction;

	// Collapsing
	CollapseAllAction collapseAllAction;

	WorkingSetFilterActionGroup wsFilterActionGroup;

	ShowLibrariesAction clibFilterAction;

	//Search
	FileSearchAction fFileSearchAction;
	FileSearchActionInWorkingSet fFileSearchActionInWorkingSet;
	SearchDialogAction fSearchDialogAction;

	FilterSelectionAction patternFilterAction;

	// Menu tags for the build
	final String BUILD_GROUP_MARKER = "buildGroup";
	final String BUILD_GROUP_MARKER_END = "end-buildGroup";

	public MainActionGroup (CView cview) {
		super(cview);
	}

	/**
	 * Handles key events in viewer.
	 */
	public void handleKeyPressed(KeyEvent event) {
		refactorGroup.handleKeyPressed(event);
	}

	/**
	 * Create the KeyListener for doing the refresh on the viewer.
	 */
	void initRefreshKey() {
		final Viewer viewer = getCView().getViewer();
		viewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent event) {
				if (event.keyCode == SWT.F5) {
					refreshAction.selectionChanged(
							(IStructuredSelection)viewer.getSelection());
					if (refreshAction.isEnabled())
						refreshAction.run();
				}
			}
		});
	}

	protected void makeActions() {
		final Viewer viewer = getCView().getViewer();
		FrameList framelist = getCView().getFrameList();
		Shell shell = getCView().getViewSite().getShell();
		openIncludeAction = new OpenIncludeAction (viewer);
		openFileAction = new OpenFileAction(getCView().getSite().getPage());
		openSystemEditorAction = new OpenSystemEditorAction(getCView().getSite().getPage());
		
		refreshAction = new RefreshAction(shell);
		initRefreshKey();

		buildAction = new BuildAction(shell, IncrementalProjectBuilder.INCREMENTAL_BUILD);
		rebuildAction = new BuildAction(shell, IncrementalProjectBuilder.FULL_BUILD);
		refactorGroup = new RefactorActionGroup(getCView());
		
		IWorkspace workspace = CUIPlugin.getWorkspace();

		openProjectAction = new OpenResourceAction(shell);
		workspace.addResourceChangeListener(openProjectAction, IResourceChangeEvent.POST_CHANGE);
		closeProjectAction = new CloseResourceAction(shell);
		workspace.addResourceChangeListener(closeProjectAction, IResourceChangeEvent.POST_CHANGE);

		//sortByNameAction = new SortViewAction(this, false);
		//sortByTypeAction = new SortViewAction(this, true);
		patternFilterAction = new FilterSelectionAction(shell, getCView(), "Filters...");
		clibFilterAction = new ShowLibrariesAction(shell, getCView(), "Show Referenced Libs");

		//wsFilterActionGroup = new WorkingSetFilterActionGroup(getCView().getViewSite().getShell(), workingSetListener);

		goIntoAction = new GoIntoAction(framelist);
		backAction = new BackAction(framelist);
		forwardAction = new ForwardAction(framelist);
		upAction = new UpAction(framelist);

		addBookmarkAction = new AddBookmarkAction(shell);
		//propertyDialogAction = new PropertyDialogAction(shell, viewer);
		propertyDialogAction = new PropertyDialogAction(shell,
				new ISelectionProvider () {
			public void addSelectionChangedListener(ISelectionChangedListener listener)  {
				viewer.addSelectionChangedListener (listener);
			}
			public ISelection getSelection()  {
				return convertSelection (viewer.getSelection ());
			}
			public void removeSelectionChangedListener(ISelectionChangedListener listener) {
				viewer.removeSelectionChangedListener (listener);
			}
			public void setSelection(ISelection selection)  {
				viewer.setSelection (selection);
			}
		});

		IActionBars actionBars = getCView().getViewSite().getActionBars();
		actionBars.setGlobalActionHandler(IDEActionFactory.BOOKMARK.getId(), addBookmarkAction);
		actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);
		actionBars.setGlobalActionHandler(IDEActionFactory.BUILD_PROJECT.getId(), buildAction);
		actionBars.setGlobalActionHandler(IDEActionFactory.REBUILD_PROJECT.getId(), rebuildAction);
		actionBars.setGlobalActionHandler(IDEActionFactory.OPEN_PROJECT.getId(), openProjectAction);
		actionBars.setGlobalActionHandler(IDEActionFactory.CLOSE_PROJECT.getId(), closeProjectAction);

		importAction = new ImportResourcesAction(getCView().getSite().getWorkbenchWindow());
		exportAction = new ExportResourcesAction(getCView().getSite().getWorkbenchWindow());
 
		collapseAllAction = new CollapseAllAction(getCView());

		fFileSearchAction = new FileSearchAction(viewer);
		fFileSearchActionInWorkingSet = new	FileSearchActionInWorkingSet(viewer);
		fSearchDialogAction = new SearchDialogAction(viewer, getCView().getViewSite().getWorkbenchWindow());
	}	


	/**
	 * Called when the context menu is about to open.
	 * Override to add your own context dependent menu contributions.
	 */
	public void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection= (IStructuredSelection) getCView().getViewer().getSelection();
		
		if (selection.isEmpty()) {
			new NewWizardMenu(menu, getCView().getSite().getWorkbenchWindow(), false);
			menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
			menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS+"-end"));//$NON-NLS-1$
			return;
		}

		updateActions (convertSelection(selection));
		//updateActions (selection);
		addNewMenu(menu, selection);
		menu.add(new Separator());
		addOpenMenu(menu, selection);
		menu.add(new Separator());
		addBuildMenu(menu, selection);
		menu.add(new Separator ());
		refactorGroup.fillContextMenu(menu);
		menu.add(new Separator());
		importAction.selectionChanged(selection);
		exportAction.selectionChanged(selection);
		menu.add(importAction);
		menu.add(exportAction);
		menu.add(new Separator());
		addRefreshMenu (menu, selection);
		menu.add(new Separator());
		addCloseMenu(menu, selection);
		menu.add(new Separator());
		addBookMarkMenu (menu, selection);
		menu.add(new Separator());
		addSearchMenu(menu, selection);
		//menu.add(new Separator());
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS+"-end"));//$NON-NLS-1$
		addPropertyMenu(menu, selection);
	}

	/**
	 * Extends the superclass implementation to set the context in the subgroups.
	 */
	public void setContext(ActionContext context) {
		super.setContext(context);
		//gotoGroup.setContext(context);
		//openGroup.setContext(context);
		refactorGroup.setContext(context);
		//sortAndFilterGroup.setContext(context);
		//workspaceGroup.setContext(context);
	}

	void addNewMenu (IMenuManager menu, IStructuredSelection selection) {
		
		
		MenuManager newMenu = new MenuManager("New");
		IAdaptable element = (IAdaptable)selection.getFirstElement();
		IResource resource = (IResource)element.getAdapter(IResource.class);
		
		newMenu.add(goIntoAction);		
		
		new NewWizardMenu(newMenu, getCView().getSite().getWorkbenchWindow(), false);

		menu.add(newMenu);

		if (resource == null)
			return;

		menu.add (new Separator ());
		if (selection.size() == 1 && resource instanceof IContainer) {
			menu.add(goIntoAction);
		}

		MenuManager gotoMenu = new MenuManager("GoTo");
		menu.add(gotoMenu);
		if (getCView().getViewer().isExpandable(element)) {
			gotoMenu.add(backAction);
			gotoMenu.add(forwardAction);
			gotoMenu.add(upAction);
		}

	}

	void addOpenMenu(IMenuManager menu, IStructuredSelection selection) {
		IAdaptable element = (IAdaptable)selection.getFirstElement();
		IResource resource = (IResource)element.getAdapter(IResource.class);
		if (resource == null)
			return;

		// Create a menu flyout.
		//MenuManager submenu= new MenuManager("Open With"); //$NON-NLS-1$
		//submenu.add(new OpenWithMenu(getSite().getPage(), (IFile) resource));
		//menu.add(submenu);
		if (resource instanceof IFile)
			menu.add(openFileAction);

		fillOpenWithMenu(menu, selection);
		fillOpenToMenu(menu, selection);
	}


	void addBuildMenu(IMenuManager menu, IStructuredSelection selection) {
		IAdaptable element = (IAdaptable)selection.getFirstElement();
		IResource resource = (IResource)element.getAdapter(IResource.class);
		if (resource == null) {
			return;
		}
		
		menu.add(new GroupMarker(BUILD_GROUP_MARKER));
		if (resource instanceof IProject && hasBuilder((IProject) resource)) {
			buildAction.selectionChanged(selection);
			menu.add(buildAction);
			rebuildAction.selectionChanged(selection);
			menu.add(rebuildAction);
		}
		
		menu.add(new GroupMarker(BUILD_GROUP_MARKER_END));
	}

	void addRefreshMenu (IMenuManager menu, IStructuredSelection selection) {
		menu.add(refreshAction);
	}

	void addCloseMenu (IMenuManager menu, IStructuredSelection selection) {
		IAdaptable element = (IAdaptable)selection.getFirstElement();
		IResource resource = (IResource)element.getAdapter(IResource.class);
		if (resource == null)
			return;

		if (resource instanceof IProject) {
			menu.add(closeProjectAction);
		}

	}

	void addBookMarkMenu (IMenuManager menu, IStructuredSelection selection) {
		IAdaptable element = (IAdaptable)selection.getFirstElement();
		IResource resource = (IResource)element.getAdapter(IResource.class);
		if (resource == null)
			return;
		if (resource instanceof IFile) {
			menu.add(addBookmarkAction);
		}
	}

	void addPropertyMenu (IMenuManager menu, IStructuredSelection selection) {
		propertyDialogAction.selectionChanged(convertSelection(selection));
		if (propertyDialogAction.isApplicableForSelection()) {
			menu.add(propertyDialogAction);
		}
	}


	/**
	 * Add "open with" actions to the context sensitive menu.
	 * @param menu the context sensitive menu
	 * @param selection the current selection in the project explorer
	 */
	void fillOpenWithMenu(IMenuManager menu, IStructuredSelection selection) {
		IAdaptable element = (IAdaptable)selection.getFirstElement();
		IResource resource = (IResource)element.getAdapter(IResource.class);
		if (resource == null)
			return;

		// If one file is selected get it.
		// Otherwise, do not show the "open with" menu.
		if (selection.size() != 1)
			return;

		if (!(resource instanceof IFile))
			return;

		// Create a menu flyout.
		MenuManager submenu = new MenuManager("Open With"); //$NON-NLS-1$
		submenu.add(new OpenWithMenu(getCView().getSite().getPage(), (IFile) resource));

		// Add the submenu.
		menu.add(submenu);
	}

	/**
	 * Add "open to" actions to the context sensitive menu.
	 * @param menu the context sensitive menu
	 * @param selection the current selection in the project explorer
	 */
	void fillOpenToMenu(IMenuManager menu, IStructuredSelection selection)
	{
		IAdaptable element = (IAdaptable)selection.getFirstElement();
		IResource resource = (IResource)element.getAdapter(IResource.class);
		if (resource == null)
			return;

		// If one file is selected get it.
		// Otherwise, do not show the "open with" menu.
		if (selection.size() != 1)
			return;

		if (!(resource instanceof IContainer))
			return;

		menu.add(new OpenInNewWindowAction(getCView().getSite().getWorkbenchWindow(), resource));
	}

	void addSearchMenu(IMenuManager menu, IStructuredSelection selection) {	
		IAdaptable element = (IAdaptable)selection.getFirstElement();

		if (element instanceof ITranslationUnit ||
				element instanceof ICProject)
			return;
		
		MenuManager search = new MenuManager("Search", IContextMenuConstants.GROUP_SEARCH); //$NON-NLS-1$
		
		if (SearchDialogAction.canActionBeAdded(selection)){
			search.add(fSearchDialogAction);
		}
		
		if (FileSearchAction.canActionBeAdded(selection)) {
			MenuManager fileSearch = new MenuManager("File Search");
			fileSearch.add(fFileSearchAction);
			fileSearch.add(fFileSearchActionInWorkingSet);
			search.add(fileSearch);
		}
		
		menu.add(search);
	}
	
	boolean hasBuilder(IProject project) {
		try {
			ICommand[] commands = project.getDescription().getBuildSpec();
			if (commands.length > 0)
				return true;
		}
		catch (CoreException e) {
			// Cannot determine if project has builders. Project is closed 
			// or does not exist. Fall through to return false.
		}
		return false;
	}

	public void runDefaultAction(IStructuredSelection selection) {
		updateActions(convertSelection(selection));
		updateGlobalActions(convertSelection(selection));
	}

	/**
	 * Updates all actions with the given selection.
	 * Necessary when popping up a menu, because some of the enablement criteria
	 * may have changed, even if the selection in the viewer hasn't.
	 * E.g. A project was opened or closed.
	 */
	void updateActions(IStructuredSelection selection) {
		goIntoAction.update();
		refreshAction.selectionChanged(selection);
		openFileAction.selectionChanged(selection);
		openSystemEditorAction.selectionChanged(selection);
		propertyDialogAction.selectionChanged(selection);
		importAction.selectionChanged(selection);
		exportAction.selectionChanged(selection);
		refactorGroup.updateActions(selection);
		//sortByTypeAction.selectionChanged(selection);
		//sortByNameAction.selectionChanged(selection); 
	}
	
	/**
	 * Updates the global actions with the given selection.
	 * Be sure to invoke after actions objects have updated, since can* methods delegate to action objects.
	 */
	void updateGlobalActions(IStructuredSelection selection) {
		addBookmarkAction.selectionChanged(selection);

		// Ensure Copy global action targets correct action,
		// either copyProjectAction or copyResourceAction,
		// depending on selection.
		IActionBars actionBars = getCView().getViewSite().getActionBars();
		actionBars.updateActionBars();
		
		refreshAction.selectionChanged(selection);
		buildAction.selectionChanged(selection);
		rebuildAction.selectionChanged(selection);
		openProjectAction.selectionChanged(selection);
		closeProjectAction.selectionChanged(selection);

	}

	public void fillActionBars(IActionBars actionBars) { 
		IToolBarManager toolBar = actionBars.getToolBarManager();
		toolBar.add(backAction);
		toolBar.add(forwardAction);
		toolBar.add(upAction);
		toolBar.add(new Separator());
		toolBar.add(collapseAllAction);
		actionBars.updateActionBars();

		//wsFilterActionGroup.fillActionBars(actionBars);

		IMenuManager menu = actionBars.getMenuManager();
		
		//menu.add (clibFilterAction);
		menu.add (patternFilterAction);
		refactorGroup.fillActionBars(actionBars);
	}
	
	public void dispose() {
		IWorkspace workspace = CUIPlugin.getWorkspace();
		workspace.removeResourceChangeListener(closeProjectAction);
		workspace.removeResourceChangeListener(openProjectAction);
		refactorGroup.dispose();
	}

	static IStructuredSelection convertSelection(ISelection s) {
		List converted = new ArrayList();
		if (s instanceof StructuredSelection) {
			Object[] elements= ((StructuredSelection)s).toArray();
			for (int i= 0; i < elements.length; i++) {
				Object e = elements[i];
				if (e instanceof IAdaptable) {
					IResource r = (IResource)((IAdaptable)e).getAdapter(IResource.class);
					if (r != null)
						converted.add(r);
				}
			}
		}
		return new StructuredSelection(converted.toArray());
	}
}
