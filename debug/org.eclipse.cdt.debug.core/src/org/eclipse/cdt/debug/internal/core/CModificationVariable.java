/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.core;

import java.text.MessageFormat;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICArrayValue;
import org.eclipse.cdt.debug.core.cdi.model.ICExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICStringValue;
import org.eclipse.cdt.debug.core.cdi.model.ICStructureValue;
import org.eclipse.cdt.debug.core.cdi.model.ICValue;
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
			ICValue currentValue = getCurrentValue();
			if ( currentValue != null )
			{
				return !( currentValue instanceof ICArrayValue || 
					 	  currentValue instanceof ICStructureValue ||
					 	  currentValue instanceof ICStringValue );
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
			ICValue vmValue = generateValue( expression );
			return vmValue != null;
		}
		catch( DebugException e )
		{
			logError( e );
			return false;
		}
	}

	protected ICValue generateValue( String expression ) throws DebugException
	{
		ICValue value = null;
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
		ICValue value = generateValue( expression );

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
	protected abstract void setValue( ICValue value ) throws DebugException;
}
