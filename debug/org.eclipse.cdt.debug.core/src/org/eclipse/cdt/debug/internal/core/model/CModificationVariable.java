/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;

/**
 * 
 * Common functionality for variables that support value modification
 * 
 * @since Aug 9, 2002
 */
public class CModificationVariable extends CVariable
{
	/**
	 * Constructor for CModificationVariable.
	 * @param parent
	 * @param cdiVariable
	 */
	public CModificationVariable( CDebugElement parent, ICDIVariable cdiVariable )
	{
		super( parent, cdiVariable );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#supportsValueModification()
	 */
	public boolean supportsValueModification()
	{
		CDebugTarget target = (CDebugTarget)getDebugTarget().getAdapter( CDebugTarget.class );
		return ( target != null && !target.isCoreDumpTarget() && isEditable() );
	}

	/**
	 * @see IValueModification#verifyValue(String)
	 */
	public boolean verifyValue( String expression )
	{
		return true;
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
		String newExpression = processExpression( expression );
		ICDIVariable cdiVariable = getCDIVariable();
		if ( cdiVariable == null )
		{
			logError( "Error in IValueModification#setValue: no cdi variable." );
			requestFailed( "Unable to set value.", null );
			return;
		}
		try
		{
			cdiVariable.setValue( newExpression );
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), null );
		}
	}

	/**
	 * Set this variable's value to the given value
	 */
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.CModificationVariable#setValue(ICDIValue)
	 */
	protected void setValue( ICDIValue value ) throws DebugException
	{
		ICDIVariable cdiVariable = getCDIVariable();
		if ( cdiVariable == null )
		{
			logError( "Error in IValueModification#setValue: no cdi variable." );
			requestFailed( "Unable to set value.", null );
			return;
		}
		try
		{
			cdiVariable.setValue( value );
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), null );
		}			
	}
	
	private String processExpression( String oldExpression ) throws DebugException
	{
		return oldExpression;
	}
}
