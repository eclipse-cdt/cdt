/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.IFormattedMemoryBlock;
import org.eclipse.cdt.debug.internal.ui.views.memory.MemoryViewer;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * 
 * Enter type comment.
 * 
 * @since Nov 3, 2002
 */
public class MemoryFormatAction extends Action implements IUpdate
{
	private MemoryActionSelectionGroup fGroup;
	private MemoryViewer fMemoryViewer;
	private int fFormat = 0;

	/**
	 * Constructor for MemoryFormatAction.
	 */
	public MemoryFormatAction( MemoryActionSelectionGroup group, 
							   MemoryViewer viewer, 
							   int format )
	{
		super( getLabel( format ) );
		fGroup = group;
		fMemoryViewer = viewer;
		fFormat = format;
		setChecked( false );
	}

	/**
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update()
	{
		setEnabled( fMemoryViewer.canChangeFormat( fFormat ) );
		setChecked( fMemoryViewer.getCurrentFormat() == fFormat );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run()
	{
		try
		{
			fMemoryViewer.setFormat( fFormat );
			fGroup.setCurrentSelection( this );
		}
		catch( DebugException e )
		{
			CDebugUIPlugin.errorDialog( "Unable to change format.", e.getStatus() );
			setChecked( false );
		}
	}

	private static String getLabel( int id )
	{
		String label = "";
		switch( id )
		{
			case( IFormattedMemoryBlock.MEMORY_FORMAT_HEX ):
				label = "Hexadecimal";
				break;
			case( IFormattedMemoryBlock.MEMORY_FORMAT_SIGNED_DECIMAL ):
				label = "Signed Decimal";
				break;
			case( IFormattedMemoryBlock.MEMORY_FORMAT_UNSIGNED_DECIMAL ):
				label = "Unsigned Decimal";
				break;
		}
		return label;
	}
	
	public String getActionId()
	{
		return "MemoryFormat" + fFormat;
	}
}
