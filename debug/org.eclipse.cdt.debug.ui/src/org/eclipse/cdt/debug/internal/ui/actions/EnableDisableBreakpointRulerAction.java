/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.internal.ui.IInternalCDebugUIConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.IWorkbenchPart;

public class EnableDisableBreakpointRulerAction extends AbstractBreakpointRulerAction {

	/**
	 * Creates the action to enable/disable breakpoints
	 */
	public EnableDisableBreakpointRulerAction( IWorkbenchPart part, IVerticalRulerInfo info ) {
		setInfo( info );
		setTargetPart( part );
		setText( ActionMessages.getString( "EnableDisableBreakpointRulerAction.Enable_Breakpoint_1" ) ); //$NON-NLS-1$
		part.getSite().getWorkbenchWindow().getWorkbench().getHelpSystem().setHelp( this, ICDebugHelpContextIds.ENABLE_DISABLE_BREAKPOINT_ACTION );
		setId( IInternalCDebugUIConstants.ACTION_ENABLE_DISABLE_BREAKPOINT );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		if ( getBreakpoint() != null ) {
			try {
				getBreakpoint().setEnabled( !getBreakpoint().isEnabled() );
			}
			catch( CoreException e ) {
				ErrorDialog.openError( getTargetPart().getSite().getShell(), ActionMessages.getString( "EnableDisableBreakpointRulerAction.Enabling_disabling_breakpoints_1" ), //$NON-NLS-1$
									   ActionMessages.getString( "EnableDisableBreakpointRulerAction.Exceptions_occurred_enabling_or_disabling_breakpoint_1" ), //$NON-NLS-1$
									   e.getStatus() );
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		setBreakpoint( determineBreakpoint() );
		if ( getBreakpoint() == null ) {
			setEnabled( false );
			return;
		}
		setEnabled( true );
		try {
			boolean enabled = getBreakpoint().isEnabled();
			setText( enabled ? ActionMessages.getString( "EnableDisableBreakpointRulerAction.Disable_Breakpoint_1" ) : ActionMessages.getString( "EnableDisableBreakpointRulerAction.Enable_Breakpoint_1" ) ); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch( CoreException e ) {
			DebugPlugin.log( e );
		}
	}
}
