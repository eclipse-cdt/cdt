/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui;

import org.eclipse.cdt.debug.core.CDebugModel;
import org.eclipse.cdt.debug.core.model.IExecFileInfo;
import org.eclipse.cdt.debug.internal.ui.actions.AbstractListenerActionDelegate;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;

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
				return "";
			long value = 0;
			try
			{
				value = parseValue( newText.trim() );
			}
			catch( NumberFormatException e )
			{
				return "Invalid address.";
			}
			return ( value > 0 ) ? null : "Address can not be 0.";
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractDebugActionDelegate#doAction(Object)
	 */
	protected void doAction( Object element ) throws DebugException
	{
		InputDialog dialog = new InputDialog( getPage().getWorkbenchWindow().getShell(),
											  "Add Address Breakpoint",
											  "Enter address:",
											  null,
											  new AddressValidator() );
		if ( dialog.open() == dialog.OK )
		{
			CDebugModel.createAddressBreakpoint( ((IExecFileInfo)getDebugTarget( element ).getAdapter( IExecFileInfo.class )).getExecFile(),
												 parseValue( dialog.getValue().trim() ),
												 true, 
												 0, 
												 "", 
												 true );
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
		if ( text.trim().startsWith( "0x" ) )
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
