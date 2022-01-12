/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

public class FindRefsProjectAction extends FindAction {

	public FindRefsProjectAction(CEditor editor, String label, String tooltip) {
		super(editor);
		setText(label);
		setToolTipText(tooltip);
	}

	public FindRefsProjectAction(CEditor editor) {
		this(editor, CSearchMessages.CSearch_FindReferencesProjectAction_label,
				CSearchMessages.CSearch_FindReferencesProjectAction_tooltip);
	}

	public FindRefsProjectAction(IWorkbenchSite site) {
		this(site, CSearchMessages.CSearch_FindReferencesProjectAction_label,
				CSearchMessages.CSearch_FindReferencesProjectAction_tooltip);
	}

	public FindRefsProjectAction(IWorkbenchSite site, String label, String tooltip) {
		super(site);
		setText(label);
		setToolTipText(tooltip);
	}

	@Override
	protected ICElement[] getScope() {
		ICProject project = null;
		if (fEditor != null) {
			project = fEditor.getTranslationUnit().getCProject();
		} else if (fSite != null) {
			ISelection selection = getSelection();
			if (selection instanceof IStructuredSelection) {
				Object element = ((IStructuredSelection) selection).getFirstElement();
				if (element instanceof IResource)
					project = CoreModel.getDefault().create(((IResource) element).getProject());
				else if (element instanceof ICElement)
					project = ((ICElement) element).getCProject();
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
		return CSearchQuery.FIND_REFERENCES;
	}

}
