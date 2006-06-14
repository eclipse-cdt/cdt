/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
import org.eclipse.cdt.internal.ui.search.PDOMSearchQuery;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;

public class FindRefsProjectAction extends FindAction {

	public FindRefsProjectAction(CEditor editor, String label, String tooltip){
		super(editor);
		setText(label); //$NON-NLS-1$
		setToolTipText(tooltip); //$NON-NLS-1$
	}
	
	public FindRefsProjectAction(CEditor editor){
		this(editor,
			CSearchMessages.getString("CSearch.FindReferencesProjectAction.label"), //$NON-NLS-1$
			CSearchMessages.getString("CSearch.FindReferencesProjectAction.tooltip")); //$NON-NLS-1$
	}
	
	public FindRefsProjectAction(IWorkbenchSite site){
		this(site,
			CSearchMessages.getString("CSearch.FindReferencesProjectAction.label"), //$NON-NLS-1$
			CSearchMessages.getString("CSearch.FindReferencesProjectAction.tooltip")); //$NON-NLS-1$
	}

	public FindRefsProjectAction(IWorkbenchSite site, String label, String tooltip) {
		super(site);
		setText(label);
		setToolTipText(tooltip);
	}

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

	protected String getScopeDescription() {
		return CSearchMessages.getString("ProjectScope"); //$NON-NLS-1$
	}

	protected int getLimitTo() {
		return PDOMSearchQuery.FIND_REFERENCES;
	}

}
