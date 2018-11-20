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

public class FindDeclarationsAction extends FindAction {

	public FindDeclarationsAction(CEditor editor, String label, String tooltip) {
		super(editor);
		setText(label);
		setToolTipText(tooltip);
	}

	public FindDeclarationsAction(CEditor editor) {
		this(editor, CSearchMessages.CSearch_FindDeclarationAction_label,
				CSearchMessages.CSearch_FindDeclarationAction_tooltip);
	}

	public FindDeclarationsAction(IWorkbenchSite site) {
		this(site, CSearchMessages.CSearch_FindDeclarationAction_label,
				CSearchMessages.CSearch_FindDeclarationAction_tooltip);
	}

	public FindDeclarationsAction(IWorkbenchSite site, String label, String tooltip) {
		super(site);
		setText(label);
		setToolTipText(tooltip);
	}

	@Override
	protected ICElement[] getScope() {
		return null;
	}

	@Override
	protected String getScopeDescription() {
		return CSearchMessages.WorkspaceScope;
	}

	@Override
	protected int getLimitTo() {
		return CSearchQuery.FIND_DECLARATIONS_DEFINITIONS;
	}
}
