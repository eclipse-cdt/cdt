/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.model.IExecFileInfo;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;

/**
 * 
 * Enter type comment.
 * 
 * @since Jan 13, 2003
 */
public class AddAddressBreakpointActionDelegate extends AbstractListenerActionDelegate
{
	/**
	 * 
	 * Enter type comment.
	 * 
	 * @since Jan 13, 2003
	 */
	public class AddressValidator implements IInputValidator
	{
		/**
		 * Constructor for AddressValidator.
		 */
		public AddressValidator()
		{
			super();
		}

		/**
		 * @see org.eclipse.jface.dialogs.IInputValidator#isValid(String)
		 */
		public String isValid( String newText )
		{
			if ( newText.trim().length() == 0 )
				return ""; //$NON-NLS-1$
			long value = 0;
			try
			{
				value = parseValue( newText.trim() );
			}
			catch( NumberFormatException e )
			{
				return CDebugUIPlugin.getResourceString("internal.ui.actions.AddAddressBreakpointActionDelegate.Invalid_address"); //$NON-NLS-1$
			}
			return ( value > 0 ) ? null : CDebugUIPlugin.getResourceString("internal.ui.actions.AddAddressBreakpointActionDelegate.Address_can_not_be_0"); //$NON-NLS-1$
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractDebugActionDelegate#doAction(Object)
	 */
	protected void doAction( Object element ) throws DebugException
	{
		InputDialog dialog = new InputDialog( getWindow().getShell(),
											  CDebugUIPlugin.getResourceString("internal.ui.actions.AddAddressBreakpointActionDelegate.Add_Address_Breakpoint"), //$NON-NLS-1$
											  CDebugUIPlugin.getResourceString("internal.ui.actions.AddAddressBreakpointActionDelegate.Enter_address"), //$NON-NLS-1$
											  null,
											  new AddressValidator() );
		if ( dialog.open() == Window.OK )
		{
//			CDebugModel.createAddressBreakpoint( ((IExecFileInfo)getDebugTarget( element ).getAdapter( IExecFileInfo.class )).getExecFile(),
//												 parseValue( dialog.getValue().trim() ),
//												 true, 
//												 0, 
//												 "",  //$NON-NLS-1$
//												 true );
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractDebugActionDelegate#isEnabledFor(Object)
	 */
	protected boolean isEnabledFor( Object element )
	{
		if ( element != null && element instanceof IDebugElement )
		{
			IDebugTarget target = getDebugTarget( element );
			return ( target != null && !target.isTerminated() && target.getAdapter( IExecFileInfo.class ) != null );
		}
		return false;
	}
	
	protected long parseValue( String text ) throws NumberFormatException
	{
		long value = 0;
		if ( text.trim().startsWith( "0x" ) ) //$NON-NLS-1$
		{
			value = Integer.parseInt( text.substring( 2 ), 16 );
		}
		else
		{
			value = Integer.parseInt( text );
		}
		return value;
	}
	
	private IDebugTarget getDebugTarget( Object element )
	{
		if ( element != null && element instanceof IDebugElement )
		{
			return ((IDebugElement)element).getDebugTarget();
		}
		return null;
	}
}
