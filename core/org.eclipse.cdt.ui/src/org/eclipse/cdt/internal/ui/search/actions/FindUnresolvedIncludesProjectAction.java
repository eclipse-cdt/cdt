/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.ui.search.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CDTUITools;
import org.eclipse.cdt.ui.IWorkingCopyManager;

import org.eclipse.cdt.internal.ui.actions.SelectionConverter;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.search.CSearchUnresolvedIncludesQuery;
import org.eclipse.cdt.internal.ui.util.StatusLineHandler;

/**
 * Searches projects for unresolved includes.
 * Could be extended to work on resource selections.
 */
public class FindUnresolvedIncludesProjectAction implements IObjectActionDelegate, IWorkbenchWindowActionDelegate {
	private ISelection fSelection;
	private IWorkbenchSite fSite;
	private boolean isEnabled;

	public FindUnresolvedIncludesProjectAction() {
	}

	@Override
	public void run(IAction action) {
		List<ICProject> projects = new ArrayList<ICProject>();
		if(fSelection instanceof IStructuredSelection) {
			IStructuredSelection cElements = SelectionConverter.convertSelectionToCElements(fSelection);
			for (Iterator<?> i = cElements.iterator(); i.hasNext();) {
				Object o= i.next();
				if (o instanceof ICProject) {
					projects.add((ICProject) o);
				}
			}
		} else if(fSelection instanceof ITextSelection) {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if(window != null) {
				IWorkbenchPart workbenchPart= window.getPartService().getActivePart();
				if (workbenchPart instanceof IEditorPart) {
					IEditorPart editorPart = (IEditorPart) workbenchPart;
					if(editorPart instanceof ITextEditor) {
						IEditorInput editorInput = ((ITextEditor)editorPart).getEditorInput();
						Object adapter = editorInput.getAdapter(IResource.class);
						if (adapter instanceof IResource) {
							IProject project = ((IResource)adapter).getProject();
							if(project != null) {
								ICProject cproject = CCorePlugin.getDefault().getCoreModel().create(project);
								if(cproject != null) {
									projects.add(cproject);
								}
							}
						}
					}
				}
			}
		}
	 	if (projects.isEmpty()) {
			StatusLineHandler.showStatusLineMessage(fSite, CSearchMessages.CSearchOperation_operationUnavailable_message);
	 		return;
	 	}

	 	ISearchQuery searchJob= new CSearchUnresolvedIncludesQuery(projects.toArray(new ICProject[projects.size()]));

		StatusLineHandler.clearStatusLine(fSite);
		NewSearchUI.activateSearchResultView();
		NewSearchUI.runQueryInBackground(searchJob);
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		fSite= targetPart.getSite();
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(ISelection selection) {
		fSelection= selection;
		isEnabled = false;
		if(selection == null || selection instanceof ITextSelection) {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if(window != null) {
				IWorkbenchPart workbenchPart = window.getPartService().getActivePart();
				if (workbenchPart instanceof IEditorPart) {
					IEditorPart editorPart= (IEditorPart) workbenchPart;
					IEditorInput editorInput = editorPart.getEditorInput();
					IWorkingCopyManager manager = CDTUITools.getWorkingCopyManager();
					IWorkingCopy tu = manager.getWorkingCopy(editorInput);
					if(tu != null) { // open file is a translation unit
						isEnabled = true;
					} else { // open file is part of a CDT project
						Object adapter = editorInput.getAdapter(IResource.class);
						if (adapter instanceof IResource) {
							IProject project = ((IResource)adapter).getProject();
							isEnabled = CoreModel.hasCNature(project);
						}
					}
				}
			}
		} else if(selection instanceof IStructuredSelection) {
			Object selectedElement = ((IStructuredSelection)selection).getFirstElement();
			if(selectedElement instanceof IProject) {
				isEnabled = CoreModel.hasCNature((IProject)selectedElement) && ((IProject)selectedElement).isOpen();
			} else if(selectedElement instanceof ITranslationUnit) {
				isEnabled = true;
			}
		}
	}

	@Override
	public void init(IWorkbenchWindow window) {
	}
	
	@Override
	public void dispose() {
	}

	/**
	 * @return {@code true} if the action is enabled or {@code false} otherwise.
	 */
	public boolean isEnabled() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			ISelection sel = StructuredSelection.EMPTY;
			IWorkbenchPage activePage = window.getActivePage();
			if (activePage != null) {
				IWorkbenchPart activePart = activePage.getActivePart();
				if (activePart != null) {
					sel = window.getSelectionService().getSelection(activePart.getSite().getId());
				}
			}
			selectionChanged(sel);
		}
		return isEnabled;
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		selectionChanged(selection);
	}
}
