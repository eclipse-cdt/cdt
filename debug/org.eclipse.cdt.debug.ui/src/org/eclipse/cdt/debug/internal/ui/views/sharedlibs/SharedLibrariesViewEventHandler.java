/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.views.sharedlibs;

import org.eclipse.cdt.debug.core.model.ICSharedLibrary;
import org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandler;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.ui.AbstractDebugView;

/**
 * Enter type comment.
 * 
 * @since: Jan 21, 2003
 */
public class SharedLibrariesViewEventHandler extends AbstractDebugEventHandler
{
	/**
	 * Constructor for SharedLibrariesViewEventHandler.
	 * @param view
	 */
	public SharedLibrariesViewEventHandler( AbstractDebugView view )
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
						 event.getSource() instanceof ICSharedLibrary )
						refresh();
					break;
				case DebugEvent.CHANGE :
					if ( event.getSource() instanceof ICSharedLibrary )
						refresh( event.getSource() );
					break;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandler#refresh()
	 */
	public void refresh()
	{
		if ( isAvailable() )
		{
			getView().showViewer();
			getTableTreeViewer().refresh();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandler#refresh(java.lang.Object)
	 */
	protected void refresh( Object element )
	{
		if ( isAvailable() )
		{
			getView().showViewer();
			getTableTreeViewer().refresh( element );
		}
	}
}
