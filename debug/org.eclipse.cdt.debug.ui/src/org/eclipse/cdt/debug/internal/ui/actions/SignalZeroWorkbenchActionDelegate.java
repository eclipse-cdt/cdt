/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.model.IResumeWithoutSignal;
import org.eclipse.debug.core.DebugException;

/**
 * Enter type comment.
 * 
 * @since: Feb 7, 2003
 */
public class SignalZeroWorkbenchActionDelegate extends AbstractListenerActionDelegate
{
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractDebugActionDelegate#doAction(java.lang.Object)
	 */
	protected void doAction( Object element ) throws DebugException
	{
		if ( element instanceof IResumeWithoutSignal ) 
		{
			((IResumeWithoutSignal)element).resumeWithoutSignal();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractDebugActionDelegate#isEnabledFor(java.lang.Object)
	 */
	protected boolean isEnabledFor( Object element )
	{
		if ( element instanceof IResumeWithoutSignal ) 
		{
			return ((IResumeWithoutSignal)element).canResumeWithoutSignal();
		}
		return false;
	}

	/**
	 * @see AbstractDebugActionDelegate#getStatusMessage()
	 */
	protected String getStatusMessage()
	{
		return "Exceptions occurred attempting to resume without signal.";
	}

	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage()
	{
		return "Resume without signal failed.";
	}

	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogTitle()
	 */
	protected String getErrorDialogTitle()
	{
		return "Resume Without Signal";
	}
}
