/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.ICSharedLibraryManager;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Enter type comment.
 * 
 * @since: Feb 11, 2003
 */
public class LoadSymbolsForAllAction extends Action implements IUpdate
{
	private Viewer fViewer = null;

	/**
	 * Constructor for LoadSymbolsForAllAction.
	 */
	public LoadSymbolsForAllAction( Viewer viewer )
	{
		super( "Load Symbols For All" );
		fViewer = viewer;
		CDebugImages.setLocalImageDescriptors( this, CDebugImages.IMG_LCL_LOAD_ALL_SYMBOLS );
		setDescription( "Load symbols for all shared libraries." );
		setToolTipText( "Load Symbols For All" );
		WorkbenchHelp.setHelp( this, ICDebugHelpContextIds.LOAD_SYMBOLS_FOR_ALL );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update()
	{
		if ( fViewer != null && fViewer.getInput() instanceof IAdaptable )
		{
			ICDebugTarget target = (ICDebugTarget)((IAdaptable)fViewer.getInput()).getAdapter( ICDebugTarget.class );
			if ( target != null )
			{
				setEnabled( target.isSuspended() );
				return;
			}
		}
		setEnabled( false );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run()
	{
		if ( fViewer != null && fViewer.getInput() instanceof IAdaptable )
		{
			ICSharedLibraryManager slm = (ICSharedLibraryManager)((IAdaptable)fViewer.getInput()).getAdapter( ICSharedLibraryManager.class );
			if ( slm != null )
			{
				try
				{
					slm.loadSymbolsForAll();
				}
				catch( DebugException e )
				{
					CDebugUIPlugin.errorDialog( "Unable to load symbols.", e.getStatus() );
				}
			}
		}
	}
}
