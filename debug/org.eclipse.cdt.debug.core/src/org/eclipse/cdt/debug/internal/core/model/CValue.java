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

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICValue;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.internal.core.CDebugUtils;
import org.eclipse.cdt.debug.internal.core.ICDebugInternalConstants;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
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
	 * Type (simple, array, structure or string) of this value.
	 */
	private int fType = TYPE_SIMPLE;

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
				fValueString = processCDIValue( getUnderlyingValue().getValueString() );
			}
			catch( CDIException e )
			{
				logError( e );
				fValueString = e.getMessage();
			}
		}
		return fValueString;
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
		if ( !isAllocated() )
			return Collections.EMPTY_LIST;
		if ( fVariables.size() == 0 )
		{
			List vars = getCDIVariables();
			if ( getType() == ICValue.TYPE_ARRAY )
			{
				int length = getNumberOfChildren();
				if ( length > 0 )
					fVariables = CArrayPartition.splitArray( (CDebugTarget)getDebugTarget(), vars, 0, length - 1 );
			}
			else
			{
				fVariables = new ArrayList( vars.size() );
				Iterator it = vars.iterator();
				while( it.hasNext() )
				{
					fVariables.add( new CLocalVariable( this, (ICDIVariable)it.next() ) );
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
	
	protected void calculateType( String stringValue )
	{
		if ( stringValue != null )
		{
			stringValue = stringValue.trim();
			if ( stringValue.length() == 0 )
			{
				fType = TYPE_KEYWORD;
			}
			else if ( stringValue.charAt( stringValue.length() - 1 ) == '\'' )
			{
				fType = TYPE_CHAR;
			}
			else if ( stringValue.charAt( 0 ) == '[' )
			{
				fType = TYPE_ARRAY;
			}
			else if ( stringValue.charAt( 0 ) == '{' )
			{
				fType = TYPE_STRUCTURE;
			}
			else if ( stringValue.startsWith( "0x" ) )
			{
				fType = TYPE_POINTER;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICValue#getType()
	 */
	public int getType()
	{
		return fType;
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
			calculateType( result );
			if ( getType() == TYPE_CHAR )
			{
				result = getCharValue( result );
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
	
	private String getCharValue( String value )
	{
		char result = '.';
		int index = value.indexOf( ' ' );
		if ( index > 0 )
		{
			try
			{
				short shortValue = Short.parseShort( value.substring( 0, index ), 10 );
				if ( shortValue >= 0 )
				{
					result = (char)shortValue;
					if ( Character.isISOControl( result ) )
					{
						result = '.';
					}
				}
			}
			catch( NumberFormatException e )
			{
			}
		}
		return String.valueOf( result );
	}
	
	protected CVariable getParentVariable()
	{
		return fParent;
	}
}
