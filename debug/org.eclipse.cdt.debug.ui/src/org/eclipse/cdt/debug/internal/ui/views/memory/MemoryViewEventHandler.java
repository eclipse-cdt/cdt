/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.views.memory;

import org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandler;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.ui.AbstractDebugView;

/**
 * 
 * Enter type comment.
 * 
 * @since Jul 29, 2002
 */
public class MemoryViewEventHandler extends AbstractDebugEventHandler
{

	/**
	 * Constructor for MemoryViewEventHandler.
	 * @param view
	 */
	public MemoryViewEventHandler( AbstractDebugView view )
	{
		super( view );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandler#doHandleDebugEvents(DebugEvent[])
	 */
	protected void doHandleDebugEvents( DebugEvent[] events )
	{
	}
}
