/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

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
