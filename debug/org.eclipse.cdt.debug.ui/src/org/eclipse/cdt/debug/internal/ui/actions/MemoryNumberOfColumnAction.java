/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.internal.ui.views.memory.MemoryViewer;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
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
		super( getLabel( numberOfColumns ), IAction.AS_CHECK_BOX );
		fGroup = group;
		fMemoryViewer = viewer;
		fNumberOfColumns = numberOfColumns;
	}

	private static String getLabel( int numberOfColumns )
	{
		//String col = CDebugUIPlugin.getResourceString("internal.ui.actions.MemoryNumberOfColumnsAction.column");
		//String cols = CDebugUIPlugin.getResourceString("internal.ui.actions.MemoryNumberOfColumnsAction.columns");
		//ChoiceFormat myform = new ChoiceFormat("0# "+cols+"|1# "+col+"|2# "+cols);
		//return Integer.toString( numberOfColumns) + myform.format(numberOfColumns);
		return CDebugUIPlugin.getFormattedString("internal.ui.actions.MemoryNumberOfColumnsAction.number_of_columns", new Integer(numberOfColumns)); //$NON-NLS-1$
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
			CDebugUIPlugin.errorDialog( CDebugUIPlugin.getResourceString("internal.ui.actions.MemoryNumberOfColumnAction.Unable_to_change_the_column_number."), e.getStatus() ); //$NON-NLS-1$
			setChecked( false );
		}
	}
	
	public String getActionId()
	{
		return "MemoryNumberOfColumns" + fNumberOfColumns; //$NON-NLS-1$
	}
}
