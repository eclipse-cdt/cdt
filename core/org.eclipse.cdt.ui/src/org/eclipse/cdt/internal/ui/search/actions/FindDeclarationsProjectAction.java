/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search.actions;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.search.CSearchQuery;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;

public class FindDeclarationsProjectAction extends FindAction {

	public FindDeclarationsProjectAction(CEditor editor, String label, String tooltip){
		super(editor);
		setText(label); 
		setToolTipText(tooltip); 
	}
	
	public FindDeclarationsProjectAction(CEditor editor){
		this(editor,
			CSearchMessages.CSearch_FindDeclarationsProjectAction_label, 
			CSearchMessages.CSearch_FindDeclarationsProjectAction_tooltip); 
	}
	
	public FindDeclarationsProjectAction(IWorkbenchSite site){
		this(site,
			CSearchMessages.CSearch_FindDeclarationsProjectAction_label, 
			CSearchMessages.CSearch_FindDeclarationsProjectAction_tooltip); 
	}

	public FindDeclarationsProjectAction(IWorkbenchSite site, String label, String tooltip) {
		super(site);
		setText(label);
		setToolTipText(tooltip);
	}

	@Override
	protected ICElement[] getScope() {
		ICProject project = null;
		if (fEditor != null) {
			project = fEditor.getInputCElement().getCProject();			 
		} else if (fSite != null){
			ISelection selection = getSelection();
			if (selection instanceof IStructuredSelection) {
				Object element = ((IStructuredSelection)selection).getFirstElement();
				if (element instanceof IResource)
					project = CoreModel.getDefault().create(((IResource)element).getProject());
				else if (element instanceof ICElement)
					project = ((ICElement)element).getCProject();
			}
		}
		
		return project != null ? new ICElement[] { project } : null;
	}

	@Override
	protected String getScopeDescription() {
		return CSearchMessages.ProjectScope; 
	}

	@Override
	protected int getLimitTo() {
		return CSearchQuery.FIND_DECLARATIONS_DEFINITIONS;
	}

}
