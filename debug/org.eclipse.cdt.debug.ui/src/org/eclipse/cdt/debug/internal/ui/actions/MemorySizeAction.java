/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.IFormattedMemoryBlock;
import org.eclipse.cdt.debug.internal.ui.views.memory.MemoryViewer;
import org.eclipse.jface.action.Action;
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
		super( getLabel( id ) );
		fGroup = group;
		fMemoryViewer = viewer;
		fId = id;
		setChecked( false );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update()
	{
		setEnabled( fMemoryViewer.canUpdate() );
		setChecked( fMemoryViewer.getCurrentWordSize() == fId );
/*
		if ( isChecked() )
		{
			fGroup.setCurrentSelection( this );
		}
*/
	}
	
	private static String getLabel( int id )
	{
		String label = "";
		switch( id )
		{
			case( IFormattedMemoryBlock.MEMORY_SIZE_BYTE ):
				label = "1 byte";
				break;
			case( IFormattedMemoryBlock.MEMORY_SIZE_HALF_WORD ):
				label = "2 bytes";
				break;
			case( IFormattedMemoryBlock.MEMORY_SIZE_WORD ):
				label = "4 bytes";
				break;
			case( IFormattedMemoryBlock.MEMORY_SIZE_DOUBLE_WORD ):
				label = "8 bytes";
				break;
		}
		return label;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run()
	{
		fGroup.setCurrentSelection( this );
	}
	
	public String getActionId()
	{
		return "MemorySize" + fId;
	}
}
