/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.cview;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.IContextMenuConstants;
import org.eclipse.cdt.internal.ui.actions.SelectionConverter;
import org.eclipse.cdt.internal.ui.editor.OpenIncludeAction;
import org.eclipse.cdt.internal.ui.search.actions.SelectionSearchGroup;
import org.eclipse.cdt.ui.actions.CustomFiltersActionGroup;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.AddBookmarkAction;
import org.eclipse.ui.actions.AddTaskAction;
import org.eclipse.ui.actions.ExportResourcesAction;
import org.eclipse.ui.actions.ImportResourcesAction;
import org.eclipse.ui.actions.NewWizardMenu;
import org.eclipse.ui.actions.WorkingSetFilterActionGroup;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.ide.IDEActionFactory;

/**
 * The main action group for the cview. This contains a few actions and several
 * subgroups.
 */
public class MainActionGroup extends CViewActionGroup {

	// Actions for Menu context.
	AddBookmarkAction addBookmarkAction;
	AddTaskAction addTaskAction;
	PropertyDialogAction propertyDialogAction;

	ImportResourcesAction importAction;
	ExportResourcesAction exportAction;

	// CElement action
	OpenIncludeAction openIncludeAction;

	// Collapsing
	CollapseAllAction collapseAllAction;
	ToggleLinkingAction toggleLinkingAction;


	BuildGroup buildGroup;
	OpenFileGroup openFileGroup;
	GotoActionGroup gotoGroup;
	RefactorActionGroup refactorGroup;
	OpenProjectGroup openProjectGroup;
	WorkingSetFilterActionGroup workingSetGroup;
	CustomFiltersActionGroup fCustomFiltersActionGroup;	

	SelectionSearchGroup selectionSearchGroup;

    private NewWizardMenu newWizardMenu;

	public MainActionGroup(CView cview) {
		super(cview);
	}

	/**
	 * Handles key events in viewer.
	 */
	public void handleKeyPressed(KeyEvent event) {
		refactorGroup.handleKeyPressed(event);
		openFileGroup.handleKeyPressed(event);
		openProjectGroup.handleKeyPressed(event);
		gotoGroup.handleKeyPressed(event);
		buildGroup.handleKeyPressed(event);
	}

	/**
	 * Handles key events in viewer.
	 */
	public void handleKeyReleased(KeyEvent event) {
		refactorGroup.handleKeyReleased(event);
		openFileGroup.handleKeyReleased(event);
		openProjectGroup.handleKeyReleased(event);
		gotoGroup.handleKeyReleased(event);
		buildGroup.handleKeyReleased(event);
	}

	protected void makeActions() {
		final Viewer viewer = getCView().getViewer();
		IShellProvider shellProvider = getCView().getViewSite();
		Shell shell = shellProvider.getShell();

		openFileGroup = new OpenFileGroup(getCView());
		openProjectGroup = new OpenProjectGroup(getCView());
		gotoGroup = new GotoActionGroup(getCView());
		buildGroup = new BuildGroup(getCView());
		refactorGroup = new RefactorActionGroup(getCView());

        newWizardMenu = new NewWizardMenu(getCView().getSite().getWorkbenchWindow());

		openIncludeAction = new OpenIncludeAction(viewer);

		//sortByNameAction = new SortViewAction(this, false);
		//sortByTypeAction = new SortViewAction(this, true);

		IPropertyChangeListener workingSetUpdater = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				String property = event.getProperty();
                                 
				if (WorkingSetFilterActionGroup.CHANGE_WORKING_SET.equals(property)) {
					Object newValue = event.getNewValue();
                                         
					if (newValue instanceof IWorkingSet) {
						getCView().setWorkingSet((IWorkingSet) newValue);
					} else if (newValue == null) {
						getCView().setWorkingSet(null);
					}
				}
			}
		};
		workingSetGroup = new WorkingSetFilterActionGroup(shell, workingSetUpdater);
		workingSetGroup.setWorkingSet(getCView().getWorkingSet());
		fCustomFiltersActionGroup= new CustomFiltersActionGroup(getCView(), getCView().getViewer());

		addBookmarkAction = new AddBookmarkAction(shell);
		addTaskAction = new AddTaskAction(shell);
		propertyDialogAction = new PropertyDialogAction(shellProvider, viewer);

		// Importing/exporting.
		importAction = new ImportResourcesAction(getCView().getSite().getWorkbenchWindow());
		exportAction = new ExportResourcesAction(getCView().getSite().getWorkbenchWindow());

		collapseAllAction = new CollapseAllAction(getCView());

		toggleLinkingAction = new ToggleLinkingAction(getCView()); //$NON-NLS-1$
		toggleLinkingAction.setToolTipText(CViewMessages.getString("ToggleLinkingAction.toolTip")); //$NON-NLS-1$
		toggleLinkingAction.setImageDescriptor(getImageDescriptor("elcl16/synced.gif"));//$NON-NLS-1$
//		toggleLinkingAction.setHoverImageDescriptor(getImageDescriptor("clcl16/synced.gif"));//$NON-NLS-1$

		selectionSearchGroup = new SelectionSearchGroup(getCView().getSite());
	}

	/**
	 * Called when the context menu is about to open. Override to add your own
	 * context dependent menu contributions.
	 */
	public void fillContextMenu(IMenuManager menu) {
		IStructuredSelection celements = (IStructuredSelection) getCView().getViewer().getSelection();
		IStructuredSelection resources = SelectionConverter.convertSelectionToResources(celements);

		addNewMenu(menu, resources);

		if (resources.isEmpty()) {
			menu.add(new Separator(IContextMenuConstants.GROUP_REORGANIZE));
			importAction.selectionChanged(resources);
			menu.add(importAction);
			exportAction.selectionChanged(resources);
			menu.add(exportAction);
			//Can be added once support for manually adding external files to index is established
			/*menu.add(new Separator());
			menu.add(addToIndexAction);*/
			menu.add(new Separator(org.eclipse.search.ui.IContextMenuConstants.GROUP_SEARCH));
			addSearchMenu(menu, celements);
			menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
			menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "-end")); //$NON-NLS-1$
			menu.add(new Separator());
			return;
		}

		menu.add(new Separator());
		gotoGroup.fillContextMenu(menu);
		menu.add(new Separator());
		openFileGroup.fillContextMenu(menu);
		menu.add(new Separator());
		buildGroup.fillContextMenu(menu);
		menu.add(new Separator());
		refactorGroup.fillContextMenu(menu);
		menu.add(new Separator());
		importAction.selectionChanged(resources);
		menu.add(importAction);
		exportAction.selectionChanged(resources);
		menu.add(exportAction);
		menu.add(new Separator());
		openProjectGroup.fillContextMenu(menu);
		addBookMarkMenu(menu, resources);
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "-end")); //$NON-NLS-1$
		addPropertyMenu(menu, resources);
		
	}
	/**
	 * Extends the superclass implementation to set the context in the
	 * subgroups.
	 */
	public void setContext(ActionContext context) {
		super.setContext(context);
		gotoGroup.setContext(context);
		openFileGroup.setContext(context);
		openProjectGroup.setContext(context);
		refactorGroup.setContext(context);
		buildGroup.setContext(context);
		//sortAndFilterGroup.setContext(context);
		//workspaceGroup.setContext(context);
	}

	void addNewMenu(IMenuManager menu, IStructuredSelection selection) {
		MenuManager newMenu = new MenuManager(CViewMessages.getString("NewWizardsActionGroup.new")); //$NON-NLS-1$
        menu.add(newMenu);
        newMenu.add(newWizardMenu);
	}

	void addBookMarkMenu(IMenuManager menu, IStructuredSelection selection) {
		Object obj = selection.getFirstElement();
		if (obj instanceof IAdaptable) {
			IAdaptable element = (IAdaptable) obj;
			IResource resource = (IResource) element.getAdapter(IResource.class);
			if (resource instanceof IFile) {
				addBookmarkAction.selectionChanged(selection);
				menu.add(addBookmarkAction);
			}
		}
	}

	void addPropertyMenu(IMenuManager menu, IStructuredSelection selection) {
		propertyDialogAction.selectionChanged(selection);
		if (propertyDialogAction.isApplicableForSelection()) {
			menu.add(propertyDialogAction);
		}
	}

	void addSearchMenu(IMenuManager menu, IStructuredSelection selection) {
		IAdaptable element = (IAdaptable) selection.getFirstElement();

		if (element instanceof ITranslationUnit || element instanceof ICProject) {
			return;
		}

		if (SelectionSearchGroup.canActionBeAdded(selection)){
			selectionSearchGroup.fillContextMenu(menu);
		}
	}
	
	public void runDefaultAction(IStructuredSelection selection) {
		openFileGroup.runDefaultAction(selection);
		openProjectGroup.runDefaultAction(selection);
		gotoGroup.runDefaultAction(selection);
		buildGroup.runDefaultAction(selection);
		refactorGroup.runDefaultAction(selection);
		//workingSetGroup.runDefaultAction(selection);
	}

	/**
	 * Updates all actions with the given selection. Necessary when popping up
	 * a menu, because some of the enablement criteria may have changed, even
	 * if the selection in the viewer hasn't. E.g. A project was opened or
	 * closed.
	 */
	public void updateActionBars() {
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();

		propertyDialogAction.setEnabled(selection.size() == 1);
		//sortByTypeAction.selectionChanged(selection);
		//sortByNameAction.selectionChanged(selection);
		addBookmarkAction.selectionChanged(selection);
		addTaskAction.selectionChanged(selection);

		openFileGroup.updateActionBars();
		openProjectGroup.updateActionBars();
		gotoGroup.updateActionBars();
		buildGroup.updateActionBars();
		refactorGroup.updateActionBars();
		workingSetGroup.updateActionBars();
		fCustomFiltersActionGroup.updateActionBars();
	}

	public void fillActionBars(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(IDEActionFactory.BOOKMARK.getId(), addBookmarkAction);
		actionBars.setGlobalActionHandler(IDEActionFactory.ADD_TASK.getId(), addTaskAction);
        actionBars.setGlobalActionHandler(ActionFactory.PROPERTIES.getId(), propertyDialogAction);

		workingSetGroup.fillActionBars(actionBars);
		fCustomFiltersActionGroup.fillActionBars(actionBars);
		gotoGroup.fillActionBars(actionBars);
		refactorGroup.fillActionBars(actionBars);
		openFileGroup.fillActionBars(actionBars);
		openProjectGroup.fillActionBars(actionBars);
		buildGroup.fillActionBars(actionBars);

		IToolBarManager toolBar = actionBars.getToolBarManager();
		toolBar.add(new Separator());
		toolBar.add(collapseAllAction);
		toolBar.add(toggleLinkingAction);

		IMenuManager menu = actionBars.getMenuManager();
		//menu.add (clibFilterAction);
		//menu.add(fCustomFiltersActionGroup);
		menu.add(toggleLinkingAction);
	}

	//---- Persistent state -----------------------------------------------------------------------

	public void restoreFilterAndSorterState(IMemento memento) {
		//fWorkingSetFilterActionGroup.restoreState(memento);
		fCustomFiltersActionGroup.restoreState(memento);
	}
	
	public void saveFilterAndSorterState(IMemento memento) {
		//fWorkingSetFilterActionGroup.saveState(memento);
		fCustomFiltersActionGroup.saveState(memento);
	}

	public CustomFiltersActionGroup getCustomFilterActionGroup() {
	    return fCustomFiltersActionGroup;
	}

	public void dispose() {
		importAction.dispose();
		exportAction.dispose();
		refactorGroup.dispose();
		openFileGroup.dispose();
		openProjectGroup.dispose();
		gotoGroup.dispose();
		buildGroup.dispose();
		newWizardMenu.dispose();
		super.dispose();
	}

}
