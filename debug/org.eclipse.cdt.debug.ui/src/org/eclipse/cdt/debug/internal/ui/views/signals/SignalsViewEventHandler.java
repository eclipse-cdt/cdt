/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.views.signals;

import org.eclipse.cdt.debug.core.model.ICSignal;
import org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandler;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.ui.AbstractDebugView;

/**
 * Enter type comment.
 * 
 * @since: Jan 30, 2003
 */
public class SignalsViewEventHandler extends AbstractDebugEventHandler
{
	/**
	 * Constructor for SignalsViewEventHandler.
	 * @param view
	 */
	public SignalsViewEventHandler( AbstractDebugView view )
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
				case DebugEvent.CREATE:
				case DebugEvent.TERMINATE:
					if ( event.getSource() instanceof IDebugTarget ||
						 event.getSource() instanceof ICSignal )
						refresh();
					break;
				case DebugEvent.CHANGE :
					if ( event.getSource() instanceof ICSignal )
						refresh( event.getSource() );
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
			getTableViewer().refresh( element );
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
			getTableViewer().refresh();
		}
	}
}
