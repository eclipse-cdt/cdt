/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.views;

import org.eclipse.debug.ui.AbstractDebugView;

/**
 * 
 * A debug view that uses an event handler to update its view/viewer.
 * 
 * @since Jul 23, 2002
 */
public abstract class AbstractDebugEventHandlerView extends AbstractDebugView
{
	/**
	 * Event handler for this view
	 */
	private AbstractDebugEventHandler fEventHandler;

	/**
	 * Sets the event handler for this view
	 * 
	 * @param eventHandler event handler
	 */
	protected void setEventHandler( AbstractDebugEventHandler eventHandler )
	{
		fEventHandler = eventHandler;
	}

	/**
	 * Returns the event handler for this view
	 * 
	 * @return The event handler for this view
	 */
	protected AbstractDebugEventHandler getEventHandler()
	{
		return fEventHandler;
	}

	/**
	 * @see IWorkbenchPart#dispose()
	 */
	public void dispose()
	{
		super.dispose();
		if ( getEventHandler() != null )
		{
			getEventHandler().dispose();
		}
	}
}
