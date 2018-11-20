/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search.actions;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.search.CSearchQuery;
import org.eclipse.ui.IWorkbenchSite;

public class FindRefsAction extends FindAction {

	public FindRefsAction(CEditor editor) {
		this(editor, CSearchMessages.CSearch_FindReferencesAction_label,
				CSearchMessages.CSearch_FindReferencesAction_tooltip);
	}

	public FindRefsAction(IWorkbenchSite site) {
		this(site, CSearchMessages.CSearch_FindReferencesAction_label,
				CSearchMessages.CSearch_FindReferencesAction_tooltip);
	}

	public FindRefsAction(CEditor editor, String label, String tooltip) {
		super(editor);
		setText(label);
		setToolTipText(tooltip);
	}

	public FindRefsAction(IWorkbenchSite site, String label, String tooltip) {
		super(site);
		setText(label);
		setToolTipText(tooltip);
	}

	@Override
	protected String getScopeDescription() {
		return CSearchMessages.WorkspaceScope;
	}

	@Override
	protected ICElement[] getScope() {
		return null;
	}

	@Override
	protected int getLimitTo() {
		return CSearchQuery.FIND_REFERENCES;
	}
}
