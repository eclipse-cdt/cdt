/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.ui.actions;

/**
 * Enter type comment.
 * 
 * @since Jun 19, 2003
 */
public class DisableVariablesActionDelegate extends EnableVariablesActionDelegate
{
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.EnableVariablesActionDelegate#isEnableAction()
	 */
	protected boolean isEnableAction()
	{
		return false;
	}
}
