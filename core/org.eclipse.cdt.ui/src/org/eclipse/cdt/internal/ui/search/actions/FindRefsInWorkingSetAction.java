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
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search.actions;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.search.CSearchQuery;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkingSet;

public class FindRefsInWorkingSetAction extends FindInWorkingSetAction {

	public FindRefsInWorkingSetAction(CEditor editor, IWorkingSet[] workingSets) {
		super(editor, CSearchMessages.CSearch_FindReferencesInWorkingSetAction_label,
				CSearchMessages.CSearch_FindReferencesInWorkingSetAction_tooltip, workingSets);
	}

	public FindRefsInWorkingSetAction(IWorkbenchSite site, IWorkingSet[] workingSets) {
		super(site, CSearchMessages.CSearch_FindReferencesInWorkingSetAction_label,
				CSearchMessages.CSearch_FindReferencesInWorkingSetAction_tooltip, workingSets);
	}

	@Override
	protected int getLimitTo() {
		return CSearchQuery.FIND_REFERENCES;
	}
}
