/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.core.model;

import java.text.MessageFormat;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArrayValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStringValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStructureValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;

/**
 * 
 * Common functionality for variables that support value modification
 * 
 * @since Aug 9, 2002
 */
public abstract class CModificationVariable extends CVariable
{
	private static final String ERROR_MESSAGE = "Value modification failed - unable to generate value from expression: {0}";

	/**
	 * Constructor for CModificationVariable.
	 * @param target
	 */
	public CModificationVariable( CDebugTarget target )
	{
		super( target );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#supportsValueModification()
	 */
	public boolean supportsValueModification()
	{
		try
		{
			ICDIValue currentValue = getCurrentValue();
			if ( currentValue != null )
			{
				return !( currentValue instanceof ICDIArrayValue || 
					 	  currentValue instanceof ICDIStructureValue ||
					 	  currentValue instanceof ICDIStringValue );
			}
		}
		catch( DebugException e )
		{
			logError( e );
		}
		return false;
	}

	/**
	 * @see IValueModification#verifyValue(String)
	 */
	public boolean verifyValue( String expression )
	{
		try
		{
			ICDIValue vmValue = generateValue( expression );
			return vmValue != null;
		}
		catch( DebugException e )
		{
			logError( e );
			return false;
		}
	}

	protected ICDIValue generateValue( String expression ) throws DebugException
	{
		ICDIValue value = null;
		try
		{
			value = getCDITarget().evaluateExpressionToValue( expression );
		}
		catch( CDIException e )
		{
			targetRequestFailed( MessageFormat.format( ERROR_MESSAGE, new String[] { expression } ), null );
		}
		return value;
	}

	/**
	 * @see IValueModification#verifyValue(IValue)
	 */
	public boolean verifyValue( IValue value )
	{
		return value.getDebugTarget().equals( getDebugTarget() );
	}

	/**
	 * @see IValueModification#setValue(String)
	 */
	public final void setValue( String expression ) throws DebugException
	{
		ICDIValue value = generateValue( expression );

		if ( value == null )
		{
			targetRequestFailed( MessageFormat.format( ERROR_MESSAGE, new String[] { expression } ), null );
		}

		setValue( value );
		fireChangeEvent( DebugEvent.CONTENT );
	}

	/**
	 * Set this variable's value to the given value
	 */
	protected abstract void setValue( ICDIValue value ) throws DebugException;
}
