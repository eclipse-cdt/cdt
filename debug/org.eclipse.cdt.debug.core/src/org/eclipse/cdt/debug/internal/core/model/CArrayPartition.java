/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.core.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.event.ICDIChangedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;

/**
 *
 * A sub-range of an array.
 * 
 * @since Sep 9, 2002
 */
public class CArrayPartition extends CVariable
{
	static final protected int SLOT_SIZE = 100;

	private int fStart;
	private int fEnd;
	private List fCDIVariables;

	/**
	 * Cache of value.
	 */
	private CArrayPartitionValue fArrayPartitionValue = null;

	/**
	 * Constructor for CArrayPartition.
	 * @param target
	 */
	public CArrayPartition( CDebugElement parent, List cdiVariables, int start, int end )
	{
		super( parent, null );
		fStart = start;
		fEnd = end;
		fCDIVariables = cdiVariables;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.CVariable#retrieveValue()
	 */
	protected ICDIValue retrieveValue() throws DebugException, CDIException
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#getName()
	 */
	public String getName() throws DebugException
	{
		StringBuffer name = new StringBuffer();
		name.append( '[' );
		name.append( fStart );
		name.append( ".." );
		name.append( fEnd );
		name.append( ']' );
		return name.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvent(ICDIEvent)
	 */
	public void handleDebugEvent( ICDIEvent event )
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#getValue()
	 */
	public IValue getValue() throws DebugException
	{
		if ( fArrayPartitionValue == null )
		{
			fArrayPartitionValue = new CArrayPartitionValue( (CDebugTarget)getDebugTarget(), fCDIVariables, getStart(), getEnd() );
		}
		return fArrayPartitionValue;
	}

	static public List splitArray( CDebugTarget target, List cdiVars, int start, int end )
	{
		ArrayList children = new ArrayList();
		int perSlot = 1;
		int len = end - start;
		while( perSlot * SLOT_SIZE < len )
		{
			perSlot = perSlot * SLOT_SIZE;
		}
		
		while( start <= end )
		{
			if ( start + perSlot > end )
			{
				perSlot = end - start + 1;
			}
			CVariable var = null;
			if ( perSlot == 1 )
			{
				var = new CArrayEntryVariable( target, (ICDIVariable)cdiVars.get( start ), start );
			}
			else
			{
				var = new CArrayPartition( target, cdiVars.subList( start, start + perSlot ), start, start + perSlot - 1 );
			}
			children.add( var );
			start += perSlot;
		}
		return children;
	}
 
	protected int getStart()
	{
		return fStart;
	}

	protected int getEnd()
	{
		return fEnd;
	}
}
