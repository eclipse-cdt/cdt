/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.internal.ui.views.memory.MemoryViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Enter type comment.
 * 
 * @since: Oct 18, 2002
 */
public class RefreshMemoryAction extends SelectionProviderAction
{
	/**
	 * Constructor for RefreshMemoryAction.
	 * @param provider
	 * @param text
	 */
	public RefreshMemoryAction( MemoryViewer viewer )
	{
		super( viewer, "Refresh Memory Block" );
		CDebugImages.setLocalImageDescriptors( this, CDebugImages.IMG_LCL_REFRESH_MEMORY );
		WorkbenchHelp.setHelp( this, ICDebugHelpContextIds.REFRESH_MEMORY_ACTION );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.SelectionProviderAction#selectionChanged(IStructuredSelection)
	 */
	public void selectionChanged( IStructuredSelection selection )
	{
		super.selectionChanged( selection );
	}
}
