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
public class ShowAsciiAction extends SelectionProviderAction implements IUpdate
{
	private MemoryViewer fMemoryViewer;

	/**
	 * Constructor for ShowAsciiAction.
	 * @param provider
	 * @param text
	 */
	public ShowAsciiAction( MemoryViewer viewer )
	{
		super( viewer, "Show ASCII" );
		fMemoryViewer = viewer;
		CDebugImages.setLocalImageDescriptors( this, CDebugImages.IMG_LCL_SHOW_ASCII );
		setDescription( "Show ASCII" );
		setToolTipText( "Show ASCII" );
		WorkbenchHelp.setHelp( this, ICDebugHelpContextIds.MEMORY_SHOW_ASCII_ACTION );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update()
	{
		setEnabled( fMemoryViewer.canShowAscii() );
		setChecked( fMemoryViewer.showAscii() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run()
	{
		fMemoryViewer.setShowAscii( isChecked() );
	}
}
