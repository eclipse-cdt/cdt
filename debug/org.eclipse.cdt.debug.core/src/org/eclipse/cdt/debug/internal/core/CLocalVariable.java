/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core;

import java.text.MessageFormat;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.event.ICChangedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICEventListener;
import org.eclipse.cdt.debug.core.cdi.model.ICObject;
import org.eclipse.cdt.debug.core.cdi.model.ICValue;
import org.eclipse.cdt.debug.core.cdi.model.ICVariable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;

/**
 * 
 * Enter type comment.
 * 
 * @since Aug 9, 2002
 */
public class CLocalVariable extends CModificationVariable
							implements ICEventListener
{
	/**
	 * The underlying CDI variable.
	 */
	private ICVariable fCDIVariable;
	
	/**
	 * The stack frame this variable is contained in.
	 */
	private CStackFrame fStackFrame;

	/**
	 * Constructor for CLocalVariable.
	 * @param target
	 */
	public CLocalVariable( CStackFrame stackFrame, ICVariable cdiVariable )
	{
		super( (CDebugTarget)stackFrame.getDebugTarget() );
		fStackFrame = stackFrame;
		fCDIVariable = cdiVariable;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.CModificationVariable#setValue(ICValue)
	 */
	protected void setValue( ICValue value ) throws DebugException
	{
		try
		{
			getCDIVariable().setValue( value );
		}
		catch( CDIException e )
		{
			targetRequestFailed( MessageFormat.format( "{0} occured modifying local variable value.", new String[] { e.toString() } ), e );
		}			
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.CVariable#retrieveValue()
	 */
	protected ICValue retrieveValue() throws DebugException, CDIException
	{
		return ( getStackFrame().isSuspended() ) ? 
					getCDIVariable().getValue() : getLastKnownValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#getName()
	 */
	public String getName() throws DebugException
	{
		String name = null;
		try
		{
			name = getCDIVariable().getName();
		}
		catch( CDIException e )
		{
			targetRequestFailed( MessageFormat.format( "{0} occured while retrieving local variable name.", new String[] { e.toString() } ), e );
		}
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException
	{
		String type = null;
		try
		{
			type = getCDIVariable().getTypeName();
		}
		catch( CDIException e )
		{
			targetRequestFailed( MessageFormat.format( "{0} occured while retrieving local variable type.", new String[] { e.toString() } ), e );
		}
		return type;
	}

	/**
	 * Returns the underlying CDI variable.
	 * 
	 * @return the underlying CDI variable
	 */
	protected ICVariable getCDIVariable()
	{
		return fCDIVariable;
	}

	/**
	 * Returns the stack frame this variable is contained in.
	 * 
	 * @return the stack frame
	 */
	protected CStackFrame getStackFrame()
	{
		return fStackFrame;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICEventListener#handleDebugEvent(ICEvent)
	 */
	public void handleDebugEvent( ICEvent event )
	{
		ICObject source = event.getSource();
		if ( source.getCDITarget().equals( getCDITarget() ) )
		{
			if ( event instanceof ICChangedEvent )
			{
				if ( source instanceof ICVariable && source.equals( getCDIVariable() ) )
				{
					handleChangedEvent( (ICChangedEvent)event );
				}
			}
		}
	}

	private void handleChangedEvent( ICChangedEvent event )
	{
		try
		{
			setValue( getCurrentValue() );
			fireChangeEvent( DebugEvent.CONTENT );
		}
		catch( DebugException e )
		{
			logError( e );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#setValue(IValue)
	 */
	public void setValue( IValue value ) throws DebugException
	{
		if ( verifyValue( value ) ) 
		{
			setValue( ((CValue)value).getUnderlyingValue() );
		}
	}
}
