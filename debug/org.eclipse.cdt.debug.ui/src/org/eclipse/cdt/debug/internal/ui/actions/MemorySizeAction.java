/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.model.IFormattedMemoryBlock;
import org.eclipse.cdt.debug.internal.ui.views.memory.MemoryViewer;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Enter type comment.
 * 
 * @since: Oct 22, 2002
 */
public class MemorySizeAction extends Action implements IUpdate
{
	private MemoryActionSelectionGroup fGroup;
	private MemoryViewer fMemoryViewer;
	private int fId = 0;

	/**
	 * Constructor for MemorySizeAction.
	 */
	public MemorySizeAction( MemoryActionSelectionGroup group, 
							 MemoryViewer viewer, 
							 int id )
	{
		super( getLabel( id ), IAction.AS_CHECK_BOX );
		fGroup = group;
		fMemoryViewer = viewer;
		fId = id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update()
	{
		setEnabled( fMemoryViewer.canUpdate() );
		setChecked( fMemoryViewer.getCurrentWordSize() == fId );
	}
	
	private static String getLabel( int id )
	{
		String label = ""; //$NON-NLS-1$
		
		switch( id )
		{
			case( IFormattedMemoryBlock.MEMORY_SIZE_BYTE ):
			case( IFormattedMemoryBlock.MEMORY_SIZE_HALF_WORD ):
			case( IFormattedMemoryBlock.MEMORY_SIZE_WORD ):
			case( IFormattedMemoryBlock.MEMORY_SIZE_DOUBLE_WORD ):
				// English value of key is "{0, number} {0, choice, 1#byte|2#bytes}"
				label = CDebugUIPlugin.getFormattedString("internal.ui.actions.MemorySizeAction.byte_bytes", new Integer(id)); //$NON-NLS-1$
			    break;
			
		}
		return label;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run()
	{
		try
		{
			fMemoryViewer.setWordSize( fId );
			fGroup.setCurrentSelection( this );
		}
		catch( DebugException e )
		{
			CDebugUIPlugin.errorDialog( CDebugUIPlugin.getResourceString("MemorySizeAction.Unable_to_change_memory_unit_size"), e.getStatus() ); //$NON-NLS-1$
			setChecked( false );
		}
	}
	
	public String getActionId()
	{
		return "MemorySize" + fId; //$NON-NLS-1$
	}
}
