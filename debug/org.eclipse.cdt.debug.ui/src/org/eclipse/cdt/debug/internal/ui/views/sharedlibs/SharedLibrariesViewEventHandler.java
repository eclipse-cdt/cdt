/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.views.sharedlibs;

import org.eclipse.cdt.debug.core.model.ICSharedLibrary;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.viewers.TableViewer;

/**
 * Enter type comment.
 * 
 * @since: Jan 16, 2003
 */
public class SharedLibrariesViewEventHandler implements IDebugEventSetListener
{
	/**
	 * This event handler's view
	 */
	private AbstractDebugView fView;

	/**
	 * Constructor for SharedLibrariesViewEventHandler.
	 * @param view
	 */
	public SharedLibrariesViewEventHandler( AbstractDebugView view )
	{
		setView( view );
		DebugPlugin.getDefault().addDebugEventListener( this );
	}

	/**
	 * @see IDebugEventSetListener#handleDebugEvents(DebugEvent[])
	 */
	public void handleDebugEvents( final DebugEvent[] events )
	{
		if ( !isAvailable() )
		{
			return;
		}
		Runnable r = new Runnable()
		{
			public void run()
			{
				if ( isAvailable() )
				{
					doHandleDebugEvents( events );
				}
			}
		};

		getView().asyncExec( r );
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

	/**
	 * De-registers this event handler from the debug model.
	 */
	public void dispose()
	{
		DebugPlugin plugin = DebugPlugin.getDefault();
		plugin.removeDebugEventListener( this );
	}

	/**
	 * Returns the view this event handler is
	 * updating.
	 * 
	 * @return debug view
	 */
	protected AbstractDebugView getView()
	{
		return fView;
	}

	/**
	 * Sets the view this event handler is updating.
	 * 
	 * @param view debug view
	 */
	private void setView( AbstractDebugView view )
	{
		fView = view;
	}

	/**
	 * Returns the viewer this event handler is updating.
	 * 
	 * @return viewer
	 */
	protected TableViewer getTableViewer()
	{
		return (TableViewer)getView().getViewer();
	}

	/**
	 * Returns whether this event handler's viewer is
	 * currently available.
	 * 
	 * @return whether this event handler's viewer is
	 * currently available
	 */
	protected boolean isAvailable()
	{
		return getView().isAvailable();
	}
}
