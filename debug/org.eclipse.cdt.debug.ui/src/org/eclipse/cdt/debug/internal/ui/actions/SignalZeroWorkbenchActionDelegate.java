/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.model.IResumeWithoutSignal;
import org.eclipse.debug.core.DebugException;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;

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
		return CDebugUIPlugin.getResourceString("internal.ui.actions.SignalZeroWorkbenchActionDelegate.Exceptions_occurred_attempting_to_resume_without_signal"); //$NON-NLS-1$
	}

	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage()
	{
		return CDebugUIPlugin.getResourceString("internal.ui.actions.SignalZeroWorkbenchActionDelegate.ErrorMsg_Resume_without_signal_failed"); //$NON-NLS-1$
	}

	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogTitle()
	 */
	protected String getErrorDialogTitle()
	{
		return CDebugUIPlugin.getResourceString("internal.ui.actions.SignalZeroWorkbenchActionDelegate.ErrorMsgTitle_Resume_Without_Signal"); //$NON-NLS-1$
	}
}
