/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * 
 * An action that toggles the state of a viewer to show/hide type names 
 * of registers.
 * 
 * @since Sep 16, 2002
 */
public class ShowRegisterTypesAction extends Action
{
	private IDebugView fView;

	/**
	 * Constructor for ShowRegisterTypesAction.
	 */
	public ShowRegisterTypesAction( IDebugView view )
	{
		super( "Show &Type Names", IAction.AS_CHECK_BOX );
		setView( view );
		setToolTipText( "Show Type Names" );
		CDebugImages.setLocalImageDescriptors( this, CDebugImages.IMG_LCL_TYPE_NAMES );
		setId( CDebugUIPlugin.getUniqueIdentifier() + ".ShowTypesAction" ); //$NON-NLS-1$
		WorkbenchHelp.setHelp( this, ICDebugHelpContextIds.SHOW_TYPES_ACTION );
	}

	/**
	 * @see Action#run()
	 */
	public void run()
	{
		valueChanged( isChecked() );
	}

	private void valueChanged( boolean on )
	{
		if ( getViewer().getControl().isDisposed() )
		{
			return;
		}
		IDebugModelPresentation debugLabelProvider = (IDebugModelPresentation)getView().getAdapter( IDebugModelPresentation.class );
		if ( debugLabelProvider != null ) 
		{
			debugLabelProvider.setAttribute( IDebugModelPresentation.DISPLAY_VARIABLE_TYPE_NAMES, ( on ? Boolean.TRUE : Boolean.FALSE ) );			
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

	/**
	 * @see Action#setChecked(boolean)
	 */
	public void setChecked( boolean value )
	{
		super.setChecked( value );
		valueChanged( value );
	}

	protected StructuredViewer getViewer()
	{
		if ( getView() != null && getView().getViewer() instanceof StructuredViewer )
			return (StructuredViewer)getView().getViewer();
		return null;
	}

	protected IDebugView getView()
	{
		return fView;
	}

	public void setView( IDebugView view )
	{
		fView = view;
	}
}
