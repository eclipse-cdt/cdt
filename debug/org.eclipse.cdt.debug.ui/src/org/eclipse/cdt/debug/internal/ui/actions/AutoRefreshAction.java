/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.ICUpdateManager;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Enter type comment.
 * 
 * @since: Feb 10, 2003
 */
public class AutoRefreshAction extends Action implements IUpdate
{
	private Viewer fViewer = null;

	/**
	 * Constructor for AutoRefreshAction.
	 */
	public AutoRefreshAction( Viewer viewer, String text )
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
				setChecked( uman.getAutoModeEnabled() );
				return;
			}
		}
		setEnabled( false );
		setChecked( false );
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
				uman.setAutoModeEnabled( isChecked() );
			}
		}
	}
}
