/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.views.registers;

import org.eclipse.cdt.debug.core.ICRegisterManager;
import org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandler;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.AbstractDebugView;

/**
 * 
 * Updates the registers view
 * 
 * @since Jul 23, 2002
 */
public class RegistersViewEventHandler extends AbstractDebugEventHandler
{

	/**
	 * Constructor for RegistersViewEventHandler.
	 * @param view
	 */
	public RegistersViewEventHandler( AbstractDebugView view )
	{
		super( view );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandler#doHandleDebugEvents(DebugEvent[])
	 */
	protected void doHandleDebugEvents( DebugEvent[] events )
	{
		for( int i = 0; i < events.length; i++ )
		{
			DebugEvent event = events[i];
			switch( event.getKind() )
			{
				case DebugEvent.TERMINATE :
					if ( event.getSource() instanceof IDebugTarget &&
						 ((IDebugTarget)event.getSource()).getAdapter( ICRegisterManager.class ) != null )
					{
						getRegistersView().clearExpandedRegisters( (ICRegisterManager)(((IDebugTarget)event.getSource()).getAdapter( ICRegisterManager.class )) );
					}
					break;
				case DebugEvent.SUSPEND :
					if ( event.getDetail() != DebugEvent.EVALUATION_IMPLICIT )
					{
						// Don't refresh everytime an implicit evaluation finishes
						refresh();
						// return since we've done a complete refresh
						return;
					}
					break;
				case DebugEvent.CHANGE :
					if ( event.getDetail() == DebugEvent.STATE )
					{
						// only process variable state changes
						if ( event.getSource() instanceof IVariable )
						{
							refresh( event.getSource() );
						}
					}
					else
					{
						refresh();
						// return since we've done a complete refresh
						return;
					}
					break;
			}
		}		
	}

	protected RegistersView getRegistersView()
	{
		return (RegistersView)getView();
	}
}
