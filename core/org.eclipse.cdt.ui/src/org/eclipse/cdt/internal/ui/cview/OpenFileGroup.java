/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.cview;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.internal.ui.actions.SelectionConverter;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.OpenFileAction;
import org.eclipse.ui.actions.OpenInNewWindowAction;
import org.eclipse.ui.actions.OpenWithMenu;

/**
 * This is the action group for the open actions.
 */
public class OpenFileGroup extends CViewActionGroup {

	private OpenFileAction openFileAction;

	public OpenFileGroup(CView cview) {
		super(cview);
	}

	protected void makeActions() {
		openFileAction = new OpenFileAction(getCView().getSite().getPage());
	}

	public void fillContextMenu(IMenuManager menu) {
                IStructuredSelection celements = (IStructuredSelection) getContext().getSelection();
		IStructuredSelection selection = SelectionConverter.convertSelectionToResources(celements);
		boolean anyResourceSelected = !selection.isEmpty()
				&& SelectionConverter.allResourcesAreOfType(selection, IResource.PROJECT | IResource.FOLDER | IResource.FILE);
		boolean onlyFilesSelected = !selection.isEmpty() && SelectionConverter.allResourcesAreOfType(selection, IResource.FILE);

		if (onlyFilesSelected) {
			openFileAction.selectionChanged(selection);
			menu.add(openFileAction);
			fillOpenWithMenu(menu, selection);
		}

		if (anyResourceSelected) {
			addNewWindowAction(menu, selection);
		}
	}

	/**
	 * Adds the OpenWith submenu to the context menu.
	 * 
	 * @param menu
	 *            the context menu
	 * @param selection
	 *            the current selection
	 */
	private void fillOpenWithMenu(IMenuManager menu, IStructuredSelection selection) {
		// Only supported if exactly one file is selected.
		if (selection.size() != 1) {
			return;
		}
		Object element = selection.getFirstElement();
		if (!(element instanceof IFile)) {
			return;
		}

		MenuManager submenu = new MenuManager(CViewMessages.getString("OpenWithMenu.label")); //$NON-NLS-1$
		submenu.add(new OpenWithMenu(getCView().getSite().getPage(), (IFile) element));
		menu.add(submenu);
	}

	/**
	 * Adds the Open in New Window action to the context menu.
	 * 
	 * @param menu
	 *            the context menu
	 * @param selection
	 *            the current selection
	 */
	private void addNewWindowAction(IMenuManager menu, IStructuredSelection selection) {

		// Only supported if exactly one container (i.e open project or folder) is selected.
		if (selection.size() != 1) {
			return;
		}
		Object element = selection.getFirstElement();
		if (!(element instanceof IContainer)) {
			return;
		}
		if (element instanceof IProject && !(((IProject) element).isOpen())) {
			return;
		}

		menu.add(new OpenInNewWindowAction(getCView().getSite().getWorkbenchWindow(), (IContainer) element));
	}

    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
     */
    public void fillActionBars(IActionBars actionBars) {
    }
    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.ActionGroup#updateActionBars()
     */
    public void updateActionBars() {
    }

	/**
	 * Runs the default action (open file).
	 */
	public void runDefaultAction(IStructuredSelection selection) {
		Object obj = selection.getFirstElement();
		if (obj instanceof ICElement) {
			ICElement celement = (ICElement) obj;
			//System.out.println ("Double click on " + element);
			try {
				IEditorPart part = EditorUtility.openInEditor(celement);
				if (part != null) {
					IWorkbenchPage page = getCView().getSite().getPage();
					page.bringToTop(part);
					if (celement instanceof ISourceReference) {
						EditorUtility.revealInEditor(part, celement);
					}
				}
			} catch (Exception e) {
			}
		} else if (obj instanceof IAdaptable) {
			IResource element = (IResource)((IAdaptable)obj).getAdapter(IResource.class);
			if (element instanceof IFile) {
				openFileAction.selectionChanged(selection);
				openFileAction.run();
			}
		}
	}

}
