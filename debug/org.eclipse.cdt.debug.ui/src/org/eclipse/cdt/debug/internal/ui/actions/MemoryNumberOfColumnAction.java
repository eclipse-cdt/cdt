/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.internal.ui.actions.MemoryActionSelectionGroup;
import org.eclipse.cdt.debug.internal.ui.views.memory.MemoryViewer;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Enter type comment.
 * 
 * @since: Oct 25, 2002
 */
public class MemoryNumberOfColumnAction extends Action implements IUpdate
{
	private MemoryActionSelectionGroup fGroup;
	private MemoryViewer fMemoryViewer;
	private int fNumberOfColumns = 0;

	/**
	 * Constructor for MemoryNumberOfColumnAction.
	 */
	public MemoryNumberOfColumnAction( MemoryActionSelectionGroup group, 
							 		   MemoryViewer viewer, 
							 		   int numberOfColumns )
	{
		super( getLabel( numberOfColumns ) );
		fGroup = group;
		fMemoryViewer = viewer;
		fNumberOfColumns = numberOfColumns;
		setChecked( false );
	}

	private static String getLabel( int numberOfColumns )
	{
		return Integer.toString( numberOfColumns) + " column" + ( ( numberOfColumns > 1 ) ? "s" : "" );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update()
	{
		setEnabled( fMemoryViewer.canUpdate() );
		setChecked( fMemoryViewer.getCurrentNumberOfColumns() == fNumberOfColumns );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run()
	{
		try
		{
			fMemoryViewer.setNumberOfColumns( fNumberOfColumns );
			fGroup.setCurrentSelection( this );
		}
		catch( DebugException e )
		{
			CDebugUIPlugin.errorDialog( e.getMessage(), (IStatus)null );
			setChecked( false );
		}
	}
	
	public String getActionId()
	{
		return "MemoryNumberOfColumns" + fNumberOfColumns;
	}
}
