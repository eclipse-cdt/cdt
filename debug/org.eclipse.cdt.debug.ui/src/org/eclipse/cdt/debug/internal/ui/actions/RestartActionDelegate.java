/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.model.IRestart;
import org.eclipse.debug.core.DebugException;

/**
 * 
 * Enter type comment.
 * 
 * @since Aug 23, 2002
 */
public class RestartActionDelegate extends AbstractListenerActionDelegate
{
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractDebugActionDelegate#doAction(Object)
	 */
	protected void doAction( Object element ) throws DebugException
	{
		if ( element instanceof IRestart ) 
		{
			((IRestart)element).restart();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractDebugActionDelegate#isEnabledFor(Object)
	 */
	protected boolean isEnabledFor( Object element )
	{
		if ( element instanceof IRestart ) 
		{
			return checkCapability( (IRestart)element );
		}
		return false;
	}

	protected boolean checkCapability( IRestart element )
	{
		return element.canRestart();
	}

	/**
	 * @see AbstractDebugActionDelegate#enableForMultiSelection()
	 */
	protected boolean enableForMultiSelection()
	{
		return false;
	}

	/**
	 * @see AbstractDebugActionDelegate#getStatusMessage()
	 */
	protected String getStatusMessage()
	{
		return "Exceptions occurred attempting to restart.";
	}

	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage()
	{
		return "Restart failed.";
	}

	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogTitle()
	 */
	protected String getErrorDialogTitle()
	{
		return "Restart";
	}
}
