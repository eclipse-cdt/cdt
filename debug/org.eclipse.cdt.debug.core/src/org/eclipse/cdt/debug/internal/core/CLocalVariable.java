/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core;

import java.text.MessageFormat;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.event.ICDIChangedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
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
							implements ICDIEventListener
{
	/**
	 * The underlying CDI variable.
	 */
	private ICDIVariable fCDIVariable;
	
	/**
	 * The stack frame this variable is contained in.
	 */
	private CStackFrame fStackFrame;

	/**
	 * Constructor for CLocalVariable.
	 * @param target
	 */
	public CLocalVariable( CStackFrame stackFrame, ICDIVariable cdiVariable )
	{
		super( (CDebugTarget)stackFrame.getDebugTarget() );
		fStackFrame = stackFrame;
		fCDIVariable = cdiVariable;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.CModificationVariable#setValue(ICDIValue)
	 */
	protected void setValue( ICDIValue value ) throws DebugException
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
	protected ICDIValue retrieveValue() throws DebugException, CDIException
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
	protected ICDIVariable getCDIVariable()
	{
		return fCDIVariable;
	}

	protected void setCDIVariable( ICDIVariable newVar )
	{
		fCDIVariable = newVar;
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
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvent(ICDIEvent)
	 */
	public void handleDebugEvent( ICDIEvent event )
	{
		ICDIObject source = event.getSource();
		if (source == null)
			return;

		if ( source.getTarget().equals( getCDITarget() ) )
		{
			if ( event instanceof ICDIChangedEvent )
			{
				if ( source instanceof ICDIVariable && source.equals( getCDIVariable() ) )
				{
					handleChangedEvent( (ICDIChangedEvent)event );
				}
			}
		}
	}

	private void handleChangedEvent( ICDIChangedEvent event )
	{
		try
		{
			//setValue( getCurrentValue() );
			if ( !getValue().hasVariables() )
			{
				setChanged( true );
				getStackFrame().fireChangeEvent( DebugEvent.CONTENT );
			}
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
