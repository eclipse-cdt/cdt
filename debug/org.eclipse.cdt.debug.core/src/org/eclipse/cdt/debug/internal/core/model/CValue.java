/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

/**
 * 
 * The value of a variable.
 * 
 * @since Aug 9, 2002
 */
public class CValue extends CDebugElement implements IValue
{
	/**
	 * Underlying CDI value.
	 */
	private ICDIValue fValue;

	/**
	 * Constructor for CValue.
	 * @param target
	 */
	public CValue( CDebugTarget target, ICDIValue cdiValue )
	{
		super( target );
		fValue = cdiValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getValueString()
	 */
	public String getValueString() throws DebugException
	{
		String result = null;
		if ( getUnderlyingValue() != null )
		{
			try
			{
				result = getUnderlyingValue().getValueString();
			}
			catch( CDIException e )
			{
				// change this
				requestFailed( "", e );
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#isAllocated()
	 */
	public boolean isAllocated() throws DebugException
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getVariables()
	 */
	public IVariable[] getVariables() throws DebugException
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#hasVariables()
	 */
	public boolean hasVariables() throws DebugException
	{
		return false;
	}

	/**
	 * Creates the appropriate kind of value, or <code>null</code>.
	 * 
	 */
	public static CValue createValue( CDebugTarget target, ICDIValue value ) 
	{
		return new CValue( target, value );
	}

	/**
	 * Returns this value's underlying CDI value
	 */
	protected ICDIValue getUnderlyingValue()
	{
		return fValue;
	}
}
