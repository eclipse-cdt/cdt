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
		super( getLabel( format ), IAction.AS_CHECK_BOX );
		fGroup = group;
		fMemoryViewer = viewer;
		fFormat = format;
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
			CDebugUIPlugin.errorDialog( CDebugUIPlugin.getResourceString("internal.ui.actions.MemoryFormatAction.Unable_to_change_format"), e.getStatus() ); //$NON-NLS-1$
			setChecked( false );
		}
	}

	private static String getLabel( int id )
	{
		String label = ""; //$NON-NLS-1$
		switch( id )
		{
			case( IFormattedMemoryBlock.MEMORY_FORMAT_HEX ):
				label = CDebugUIPlugin.getResourceString("internal.ui.actions.MemoryFormatAction.Hexadecimal"); //$NON-NLS-1$
				break;
			case( IFormattedMemoryBlock.MEMORY_FORMAT_SIGNED_DECIMAL ):
				label = CDebugUIPlugin.getResourceString("internal.ui.actions.MemoryFormatAction.Signed_Decimal"); //$NON-NLS-1$
				break;
			case( IFormattedMemoryBlock.MEMORY_FORMAT_UNSIGNED_DECIMAL ):
				label = CDebugUIPlugin.getResourceString("internal.ui.actions.MemoryFormatAction.Unsigned_Decimal"); //$NON-NLS-1$
				break;
		}
		return label;
	}
	
	public String getActionId()
	{
		return "MemoryFormat" + fFormat; //$NON-NLS-1$
	}
}
