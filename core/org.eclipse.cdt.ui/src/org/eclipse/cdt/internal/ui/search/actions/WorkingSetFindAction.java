/*******************************************************************************
 * Copyright (c) 2005, 2008 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
/*
 * Created on Aug 31, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.cdt.internal.ui.search.actions;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.ui.IWorkbenchSite;

public class WorkingSetFindAction extends FindAction {

	private FindAction findAction;

	public WorkingSetFindAction(CEditor editor, FindAction action, String string) {
		super(editor);
		this.findAction = action;
		setText(string);
	}

	public WorkingSetFindAction(IWorkbenchSite site, FindAction action, String string) {
		super(site);
		this.findAction = action;
		setText(string);
	}

	@Override
	protected String getScopeDescription() {
		return findAction.getScopeDescription();
	}

	@Override
	protected ICElement[] getScope() {
		return findAction.getScope();
	}

	@Override
	protected int getLimitTo() {
		return findAction.getLimitTo();
	}

	@Override
	public void run() {
		findAction.run();
	}

}
