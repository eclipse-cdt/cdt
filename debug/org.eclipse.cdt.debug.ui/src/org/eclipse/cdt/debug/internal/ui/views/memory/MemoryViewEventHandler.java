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
package org.eclipse.cdt.debug.internal.ui.views.memory;

import org.eclipse.cdt.debug.core.model.IFormattedMemoryBlock;
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
						getView().updateObjects();
						return;
					}
					break;
				case DebugEvent.TERMINATE:
					if ( event.getSource() instanceof IFormattedMemoryBlock )
					{
						remove( event.getSource() );
						getView().updateObjects();
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

	protected void remove( Object element )
	{
		((MemoryViewer)getViewer()).remove( element );
	}
}
