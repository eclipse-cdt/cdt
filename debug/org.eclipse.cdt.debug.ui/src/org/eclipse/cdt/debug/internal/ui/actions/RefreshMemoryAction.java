/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.internal.ui.views.memory.MemoryViewer;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;

/**
 * Enter type comment.
 * 
 * @since: Oct 18, 2002
 */
public class RefreshMemoryAction extends SelectionProviderAction implements IUpdate
{
	private MemoryViewer fMemoryViewer;

	/**
	 * Constructor for RefreshMemoryAction.
	 * @param provider
	 * @param text
	 */
	public RefreshMemoryAction( MemoryViewer viewer )
	{
		super( viewer, "Refresh" ); //$NON-NLS-1$
		fMemoryViewer = viewer;
		CDebugImages.setLocalImageDescriptors( this, CDebugImages.IMG_LCL_REFRESH );
		setDescription( CDebugUIPlugin.getResourceString("internal.ui.actions.RefreshMemoryAction.Refresh_Memory_Block") ); //$NON-NLS-1$
		setToolTipText( CDebugUIPlugin.getResourceString("internal.ui.actions.RefreshMemoryAction.Refresh") ); //$NON-NLS-1$
		WorkbenchHelp.setHelp( this, ICDebugHelpContextIds.REFRESH_MEMORY_ACTION );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update()
	{
		setEnabled( fMemoryViewer.canUpdate() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run()
	{
		fMemoryViewer.refreshMemoryBlock();
	}
}
