/*******************************************************************************
 * Copyright (c) 2004, 2007 QNX Software Systems and others.
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
import org.eclipse.cdt.debug.core.model.IGlobalVariableDescriptor;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionDelegate;
 
/**
 * A delegate for the "Remove All Globals" action.
 */
public class RemoveAllGlobalsActionDelegate extends ActionDelegate implements IViewActionDelegate, IDebugEventSetListener {

	private IAction fAction;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	@Override
	public void init( IViewPart view ) {
		DebugPlugin.getDefault().addDebugEventListener(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionDelegate#init(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void init( IAction action ) {
		DebugPlugin.getDefault().addDebugEventListener(this);
		fAction = action;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		update();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionDelegate#dispose()
	 */
	@Override
	public void dispose() {
		DebugPlugin.getDefault().removeDebugEventListener(this);
		fAction = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run( IAction action ) {
		IAdaptable context = DebugUITools.getDebugContext();
		if ( context instanceof IDebugElement ) {
			final ICGlobalVariableManager gvm = (ICGlobalVariableManager)((IDebugElement)context).getDebugTarget().getAdapter( ICGlobalVariableManager.class );
			if ( gvm != null ) {
				DebugPlugin.getDefault().asyncExec( 
						new Runnable() {
							@Override
							public void run() {
								gvm.removeAllGlobals();
							}
						} );
			}
		}
	}

	/**
	 * Enables/disables the action based on whether there are any globals in the
	 * variables view.
	 */
	private void update() {
		final IAction action = fAction;
		if (action != null) {
			final IAdaptable context = DebugUITools.getDebugContext();
			boolean enabled = false;
			if (context instanceof IDebugElement) {
				final ICGlobalVariableManager gvm = (ICGlobalVariableManager) ((IDebugElement) context)
						.getDebugTarget().getAdapter(
								ICGlobalVariableManager.class);
				if (gvm != null) {
					final IGlobalVariableDescriptor[] globals = gvm
							.getDescriptors();
					enabled = globals != null && globals.length > 0;
				}
			}
			action.setEnabled(enabled);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	@Override
	public void handleDebugEvents( DebugEvent[] events ) {
		// The ICGlobalVariableManager will fire a target content-changed 
		// event when a global is added or removed. Update the enable/disable 
		// state of this action accordingly
		
		if (fAction != null) {
			for (int i = 0; i < events.length; i++) {
				final DebugEvent event = events[i];
				if (event.getSource() instanceof IDebugTarget
						&& event.getKind() == DebugEvent.CHANGE
						&& event.getDetail() == DebugEvent.CONTENT ) {
					update();
					break;
				}
			}
		}
	}
}
