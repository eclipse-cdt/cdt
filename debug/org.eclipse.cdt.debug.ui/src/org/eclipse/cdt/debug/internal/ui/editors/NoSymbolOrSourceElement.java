/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

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
