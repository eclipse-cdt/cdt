/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.editors;

import org.eclipse.debug.core.model.IStackFrame;

/**
 * Enter type comment.
 * 
 * @since Apr 25, 2003
 */
public class NoSymbolOrSourceElement
{
	private IStackFrame fStackFrame;

	public NoSymbolOrSourceElement( IStackFrame frame )
	{
		fStackFrame = frame;
	}

	public IStackFrame getStackFrame()
	{
		return fStackFrame;
	}
}
