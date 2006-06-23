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

import org.eclipse.cdt.debug.core.ICGlobalVariableManager;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionDelegate;
 
/**
 * A delegate for the "Remove All Globals" action.
 */
public class RemoveAllGlobalsActionDelegate extends ActionDelegate implements IViewActionDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init( IViewPart view ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run( IAction action ) {
		IAdaptable context = DebugUITools.getDebugContext();
		if ( context instanceof IDebugElement ) {
			final ICGlobalVariableManager gvm = (ICGlobalVariableManager)((IDebugElement)context).getDebugTarget().getAdapter( ICGlobalVariableManager.class );
			if ( gvm != null ) {
				DebugPlugin.getDefault().asyncExec( 
						new Runnable() {
							public void run() {
								gvm.removeAllGlobals();
							}
						} );
			}
		}
	}
}
