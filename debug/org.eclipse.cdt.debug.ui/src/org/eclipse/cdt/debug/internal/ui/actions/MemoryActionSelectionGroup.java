/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.IAction;

/**
 * Enter type comment.
 * 
 * @since: Oct 22, 2002
 */
public class MemoryActionSelectionGroup
{
	private List fActions;
	private IAction fSelection = null;

	/**
	 * Constructor for MemoryActionSelectionGroup.
	 */
	public MemoryActionSelectionGroup()
	{
		fActions = new ArrayList();
	}

	public IAction getCurrentSelection()
	{
		return fSelection;
	}
	
	public void setCurrentSelection( IAction selection )
	{
		Iterator it = fActions.iterator();
		while( it.hasNext() )
		{
			IAction action = (IAction)it.next();
			if ( !action.equals( selection ) )
			{
				action.setChecked( false );
			}
		}
		if ( fActions.contains( selection ) )
		{
			fSelection = selection;
		}
		else
		{
			fSelection = null;
		}
	}

	public void addAction( IAction action )
	{
		fActions.add( action );
	}
	
	public void dispose()
	{
		fActions.clear();
		fSelection = null;
	}
	
	public IAction[] getActions()
	{
		return (IAction[])fActions.toArray( new IAction[fActions.size()] );
	}
}
