/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.model.ICValue;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;

/**
 *
 * The value for an array partition.
 * 
 * @since Sep 9, 2002
 */
public class CArrayPartitionValue extends CDebugElement implements ICValue
{
	/**
	 * The underlying CDI variables.
	 */
	private List fCDIVariables;

	/**
	 * List of child variables.
	 */
	private List fVariables = Collections.EMPTY_LIST;

	private int fStart;

	private int fEnd;

	/**
	 * Constructor for CArrayPartitionValue.
	 * @param target
	 */
	public CArrayPartitionValue( CDebugTarget target, List cdiVariables, int start, int end )
	{
		super( target );
		fCDIVariables = cdiVariables;
		fStart = start;
		fEnd = end;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICValue#getType()
	 */
	public int getType()
	{
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICValue#getUnderlyingValue()
	 */
	public ICDIValue getUnderlyingValue()
	{
		return null;
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
		return null;
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
		if ( fVariables.isEmpty() )
		{
			fVariables = new ArrayList( getEnd() - getStart() + 1 );
			for ( int i = getStart(); i <= getEnd(); ++i ) 
			{
				fVariables.add( new CModificationVariable( this, (ICDIVariable)fCDIVariables.get( i - getStart() ) ) );
			}
		}
		return (IVariable[])fVariables.toArray( new IVariable[fVariables.size()] );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#hasVariables()
	 */
	public boolean hasVariables() throws DebugException
	{
		return true;
	}
 
	protected int getStart()
	{
		return fStart;
	}

	protected int getEnd()
	{
		return fEnd;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICValue#setChanged(boolean)
	 */
	public void setChanged( boolean changed ) throws DebugException
	{
		Iterator it = fVariables.iterator();
		while( it.hasNext() )
		{
			((CVariable)it.next()).setChanged( changed );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICValue#getUnderlyingValueString()
	 */
	public String getUnderlyingValueString()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICValue#computeDetail()
	 */
	public String evaluateAsExpression()
	{
		return null;
	}
}
