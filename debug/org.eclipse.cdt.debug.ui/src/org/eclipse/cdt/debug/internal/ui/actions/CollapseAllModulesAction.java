/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions; 

import org.eclipse.cdt.debug.internal.ui.views.modules.ModulesViewer;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionDelegate;
 
/**
 * The delegate for the "Collapse All" action of the Modules view.
 */
public class CollapseAllModulesAction extends ActionDelegate implements IViewActionDelegate {

	private IDebugView fView;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init( IViewPart view ) {
		Assert.isLegal( view instanceof IDebugView );
		fView = (IDebugView)view;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run( IAction action ) {
		Viewer viewer = getView().getViewer();
		if ( viewer instanceof ModulesViewer ) {
			viewer.getControl().setRedraw( false );
			((ModulesViewer)viewer).collapseAll();
			viewer.getControl().setRedraw( true );
		}
	}

	private IDebugView getView() {
		return fView;
	}
}
