/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.actions.ActionMessages;
import org.eclipse.jface.action.Action;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.TableViewer;

import org.eclipse.ui.PlatformUI;

/**
 * This action selects all entries currently showing in view.
 */
public class SelectAllAction extends Action {

	private TableViewer fViewer;

	/**
	 * Creates the action.
	 */
	public SelectAllAction(TableViewer viewer) {
		super("selectAll"); //$NON-NLS-1$
		setText(ActionMessages.SelectAllAction_label);
		setToolTipText(ActionMessages.SelectAllAction_tooltip);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, ICHelpContextIds.SELECT_ALL_ACTION);
		Assert.isNotNull(viewer);
		fViewer= viewer;
	}

	/**
	 * Selects all resources in the view.
	 */
	@Override
	public void run() {
		fViewer.getTable().selectAll();
		// force viewer selection change
		fViewer.setSelection(fViewer.getSelection());
	}
}
