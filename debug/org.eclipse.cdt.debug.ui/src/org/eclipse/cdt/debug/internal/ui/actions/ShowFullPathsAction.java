/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.internal.ui.CDTDebugModelPresentation;
import org.eclipse.cdt.debug.internal.ui.preferences.ICDebugPreferenceConstants;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.custom.BusyIndicator;

/**
 * 
 * Enter type comment.
 * 
 * @since Oct 4, 2002
 */
public class ShowFullPathsAction extends ToggleDelegateAction
{

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.ToggleDelegateAction#initActionId()
	 */
	protected void initActionId()
	{
		fId = CDebugUIPlugin.getUniqueIdentifier() + getView().getSite().getId() + ".ShowFullPathsAction"; //$NON-NLS-1$
	}

	protected void setAction( IAction action )
	{
		super.setAction( action );
		action.setChecked( CDebugUIPlugin.getDefault().getPreferenceStore().getBoolean( ICDebugPreferenceConstants.PREF_SHOW_FULL_PATHS ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.ToggleDelegateAction#valueChanged(boolean)
	 */
	protected void valueChanged( boolean on )
	{
		if ( getViewer().getControl().isDisposed() )
		{
			return;
		}
		ILabelProvider labelProvider = (ILabelProvider)getViewer().getLabelProvider();
		if ( labelProvider instanceof IDebugModelPresentation )
		{
			IDebugModelPresentation debugLabelProvider = (IDebugModelPresentation)labelProvider;
			debugLabelProvider.setAttribute( CDTDebugModelPresentation.DISPLAY_FULL_PATHS, ( on ? Boolean.TRUE : Boolean.FALSE ) );
			BusyIndicator.showWhile( getViewer().getControl().getDisplay(), 
									 new Runnable()
										{
											public void run()
											{
												getViewer().refresh();
											}
										} );
		}
	}
}
