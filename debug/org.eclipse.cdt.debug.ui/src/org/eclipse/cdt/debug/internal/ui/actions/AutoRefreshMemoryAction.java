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

/**
 * Enter type comment.
 * 
 * @since: Oct 21, 2002
 */
public class AutoRefreshMemoryAction extends SelectionProviderAction implements IUpdate
{
	private MemoryViewer fMemoryViewer;

	/**
	 * Constructor for AutoRefreshMemoryAction.
	 * @param provider
	 * @param text
	 */
	public AutoRefreshMemoryAction( MemoryViewer viewer )
	{
		super( viewer, "Auto-Refresh" );
		fMemoryViewer = viewer;
		CDebugImages.setLocalImageDescriptors( this, CDebugImages.IMG_LCL_AUTO_REFRESH );
		setDescription( "Automatically Refresh Memory Block" );
		setToolTipText( "Auto-Refresh" );
		WorkbenchHelp.setHelp( this, ICDebugHelpContextIds.AUTO_REFRESH_MEMORY_ACTION );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update()
	{
		setEnabled( fMemoryViewer.canUpdate() );
		setChecked( !fMemoryViewer.isFrozen() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run()
	{
		fMemoryViewer.setFrozen( !isChecked() );
	}
}
