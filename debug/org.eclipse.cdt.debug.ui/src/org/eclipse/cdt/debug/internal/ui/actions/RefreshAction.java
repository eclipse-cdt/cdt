/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.ICUpdateManager;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Enter type comment.
 * 
 * @since: Feb 10, 2003
 */
public class RefreshAction extends Action implements IUpdate
{
	private Viewer fViewer = null;

	/**
	 * Constructor for RefreshAction.
	 */
	public RefreshAction( Viewer viewer, String text )
	{
		super( text );
		fViewer = viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update()
	{
		if ( fViewer != null && fViewer.getInput() instanceof IAdaptable )
		{
			ICUpdateManager uman = (ICUpdateManager)((IAdaptable)fViewer.getInput()).getAdapter( ICUpdateManager.class );
			if ( uman != null )
			{
				setEnabled( uman.canUpdate() );
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
			ICUpdateManager uman = (ICUpdateManager)((IAdaptable)fViewer.getInput()).getAdapter( ICUpdateManager.class );
			if ( uman != null )
			{
				try
				{
					uman.update();
				}
				catch( DebugException e )
				{
					CDebugUIPlugin.errorDialog( "Unable to refresh shared libraries.", e.getStatus() );
				}
			}
		}
	}
}
