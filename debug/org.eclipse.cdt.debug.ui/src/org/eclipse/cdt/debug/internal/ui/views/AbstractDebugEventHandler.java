/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.ui.views;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * 
 * Handles debug events, updating a view and viewer.
 * 
 * @since Jul 23, 2002
 */
public abstract class AbstractDebugEventHandler implements IDebugEventSetListener
{
	/**
	 * This event handler's view
	 */
	private AbstractDebugView fView;

	/**
	 * Constructs an event handler for the given view.
	 * 
	 * @param view debug view
	 */
	public AbstractDebugEventHandler( AbstractDebugView view )
	{
		setView( view );
		DebugPlugin plugin = DebugPlugin.getDefault();
		plugin.addDebugEventListener( this );
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

	/**
	 * Implementation specific handling of debug events.
	 * Subclasses should override.
	 */
	protected abstract void doHandleDebugEvents( DebugEvent[] events );

	/**
	 * Helper method for inserting the given element - must be called in UI thread
	 */
	protected void insert( Object element )
	{
		if ( isAvailable() )
		{
			final Object parent = ((ITreeContentProvider)getTreeViewer().getContentProvider()).getParent( element );
			// a parent can be null for a debug target or process that has not yet been associated
			// with a launch
			if ( parent != null )
			{
				getView().showViewer();
				getTreeViewer().add( parent, element );
			}
		}
	}

	/**
	 * Helper method to remove the given element - must be called in UI thread.
	 */
	protected void remove( Object element )
	{
		if ( isAvailable() )
		{
			getView().showViewer();
			getTreeViewer().remove( element );
		}
	}

	/**
	 * Helper method to update the label of the given element - must be called in UI thread
	 */
	protected void labelChanged( Object element )
	{
		if ( isAvailable() )
		{
			getView().showViewer();
			getTreeViewer().update( element,
									new String[] { IBasicPropertyConstants.P_TEXT } );
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
			getTreeViewer().refresh( element );
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
			getTreeViewer().refresh();
		}
	}

	/**
	 * Helper method to select and reveal the given element - must be called in UI thread
	 */
	protected void selectAndReveal( Object element )
	{
		if ( isAvailable() )
		{
			getViewer().setSelection( new StructuredSelection( element ), true );
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
	protected Viewer getViewer()
	{
		return getView().getViewer();
	}

	/**
	 * Returns this event handler's viewer as a tree
	 * viewer or <code>null</code> if none.
	 * 
	 * @return this event handler's viewer as a tree
	 * viewer or <code>null</code> if none
	 */
	protected TreeViewer getTreeViewer()
	{
		if ( getViewer() instanceof TreeViewer )
		{
			return (TreeViewer)getViewer();
		}
		return null;
	}

	/**
	 * Returns this event handler's viewer as a table
	 * viewer or <code>null</code> if none.
	 * 
	 * @return this event handler's viewer as a tree
	 * viewer or <code>null</code> if none
	 */
	protected TableViewer getTableViewer()
	{
		if ( getViewer() instanceof TableViewer )
		{
			return (TableViewer)getViewer();
		}
		return null;
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
