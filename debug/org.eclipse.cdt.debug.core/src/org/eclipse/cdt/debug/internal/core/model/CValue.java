/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.model.ICValue;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;

/**
 * 
 * The value of a variable.
 * 
 * @since Aug 9, 2002
 */
public class CValue extends CDebugElement implements ICValue
{
	/**
	 * Parent variable.
	 */
	private CVariable fParent = null;

	/**
	 * Cached value.
	 */
	private String fValueString = null;

	/**
	 * Underlying CDI value.
	 */
	private ICDIValue fCDIValue;

	/**
	 * List of child variables.
	 */
	private List fVariables = Collections.EMPTY_LIST;

	/**
	 * Constructor for CValue.
	 * @param target
	 */
	public CValue( CVariable parent, ICDIValue cdiValue )
	{
		super( (CDebugTarget)parent.getDebugTarget() );
		fParent = parent;
		fCDIValue = cdiValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException
	{
		String typeName = null;
		try
		{
			if ( fCDIValue != null )
			{
				typeName = fCDIValue.getTypeName();
			}
		}
		catch( CDIException e )
		{
			logError( e );
		}
		return typeName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getValueString()
	 */
	public String getValueString() throws DebugException
	{
		if ( fValueString == null && getUnderlyingValue() != null )
		{
			try
			{
//				fValueString = processCDIValue( getUnderlyingValue().getValueString() );
				fValueString = getUnderlyingValue().getValueString();
			}
			catch( CDIException e )
			{
				fValueString = e.getMessage();
			}
		}
		return ( fValueString != null ) ? processCDIValue( fValueString ) : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#isAllocated()
	 */
	public boolean isAllocated() throws DebugException
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getVariables()
	 */
	public IVariable[] getVariables() throws DebugException
	{
		List list = getVariables0();
		return (IVariable[])list.toArray( new IVariable[list.size()] );
	}

	protected synchronized List getVariables0() throws DebugException 
	{
		if ( !isAllocated() || !hasVariables() )
			return Collections.EMPTY_LIST;
		if ( fVariables.size() == 0 )
		{
			List vars = getCDIVariables();

			if ( vars.size() > 1 )
				fVariables = CArrayPartition.splitArray( this, vars, 0, vars.size() - 1 );
			else
			{
				fVariables = new ArrayList( vars.size() );
				Iterator it = vars.iterator();
				while( it.hasNext() )
				{
					fVariables.add( new CModificationVariable( this, (ICDIVariable)it.next() ) );
				}
			}
		}
		return fVariables;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#hasVariables()
	 */
	public boolean hasVariables() throws DebugException
	{
		try
		{
			ICDIValue value = getUnderlyingValue();
			if ( value != null )
				return value.getChildrenNumber() > 0;
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), null );
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICValue#getUnderlyingValue()
	 */
	public ICDIValue getUnderlyingValue()
	{
		return fCDIValue;
	}
	
	protected List getCDIVariables() throws DebugException
	{
		ICDIVariable[] vars = null;
		try
		{
			ICDIValue value = getUnderlyingValue();
			if ( value != null )
			{
				vars = value.getVariables();
				// Quick fix. 
				// getVariables should return an empty array instead of null.
				if ( vars == null )
				{
					vars = new ICDIVariable[0];
				}
			}
		}
		catch( CDIException e )
		{
			vars = new ICDIVariable[0];
			infoMessage( e );
		}
		return Arrays.asList( vars );
	}
	
	protected int getNumberOfChildren() throws DebugException
	{
		int result = 0;
		try
		{
			result = getUnderlyingValue().getChildrenNumber();
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), null );
		}
		return result;
	}
	
	protected String processCDIValue( String cdiValue )
	{
		String result = null;
		if ( cdiValue != null )
		{
			result = cdiValue.trim();
			if ( result.startsWith( "@" ) ) // Reference
			{
				int end = result.indexOf( ':' );
				if ( end == -1 )
					end = result.length();
				result = result.substring( 1, end );
			}
			else if ( result.startsWith( "0x" ) )
			{
				int end = result.indexOf( ' ' );
				if ( end == -1 )
					end = result.length();
				result = result.substring( 0, end );
			}
			else if ( result.endsWith( "\'" ) )
			{
				int start = result.indexOf( '\'' );
				if ( start != -1 && result.length() - start == 3 )
				{
					result = result.substring( start );
				}
				else
				{
					result = null;
				}
			}
		}
		return result;
	}

	public synchronized void setChanged( boolean changed ) throws DebugException
	{
		if ( changed )
		{
			fValueString = null;
		}
		Iterator it = fVariables.iterator();
		while( it.hasNext() )
		{
			((CVariable)it.next()).setChanged( changed );
		}
	}

	protected void dispose()
	{
		Iterator it = fVariables.iterator();
		while( it.hasNext() )
		{
			((CVariable)it.next()).dispose();
		}
	}

	protected CVariable getParentVariable()
	{
		return fParent;
	}

	public String getUnderlyingValueString()
	{
		if ( fValueString == null && getUnderlyingValue() != null )
		{
			try
			{
				fValueString = getUnderlyingValue().getValueString();
			}
			catch( CDIException e )
			{
				fValueString = e.getMessage();
			}
		}
		return fValueString;
	}
	
	public boolean isCharPointer()
	{
		String value = getUnderlyingValueString();
		if ( value != null )
		{
			value = value.trim();
			return ( value.startsWith( "0x" ) && value.indexOf( ' ' ) != -1 );
		}
		return false;
	}

	public boolean isCharacter()
	{
		String value = getUnderlyingValueString();
		if ( value != null )
		{
			return ( value.trim().endsWith( "\'" ) );
		}
		return false;
	}
}
