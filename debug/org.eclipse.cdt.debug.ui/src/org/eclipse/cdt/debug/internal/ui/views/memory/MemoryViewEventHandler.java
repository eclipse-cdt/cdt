/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.views.memory;

import org.eclipse.cdt.debug.core.IFormattedMemoryBlock;
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
		for( int i = 0; i < events.length; i++ )
		{
			DebugEvent event = events[i];
			switch( event.getKind() )
			{
				case DebugEvent.CHANGE:
					if ( event.getSource() instanceof IFormattedMemoryBlock && event.getDetail() == DebugEvent.CONTENT )
					{
						refresh( event.getSource() );
						return;
					}
					break;
			}
		}
	}

	/**
	 * Refresh the given element in the viewer - must be called in UI thread.
	 */
	protected void refresh( Object element )
	{
		if ( isAvailable() )
		{
			getView().showViewer();
			((MemoryViewer)getViewer()).refresh( element );
		}
	}

	/**
	 * Refresh the viewer - must be called in UI thread.
	 */
	public void refresh()
	{
		if ( isAvailable() )
		{
			getView().showViewer();
			getViewer().refresh();
		}
	}
}
