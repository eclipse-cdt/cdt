/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.core.model;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayValue;
import org.eclipse.cdt.debug.core.model.ICType;
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
	private ICDIVariableObject fCDIVariableObject;
	private ICDIVariable fCDIVariable;
	private ICType fType = null;
	private String fQualifiedName = null;

	/**
	 * Cache of value.
	 */
	private CArrayPartitionValue fArrayPartitionValue = null;

	/**
	 * Constructor for CArrayPartition.
	 * @param target
	 */
	public CArrayPartition( CDebugElement parent, ICDIVariable cdiVariable, int start, int end )
	{
		super( parent, null );
		fStart = start;
		fEnd = end;
		fCDIVariable = cdiVariable;
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
		name.append( ".." ); //$NON-NLS-1$
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
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvents(ICDIEvent)
	 */
	public void handleDebugEvents( ICDIEvent[] events )
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#getValue()
	 */
	public IValue getValue() throws DebugException
	{
		if ( fArrayPartitionValue == null )
		{
			fArrayPartitionValue = new CArrayPartitionValue( this, fCDIVariable, getStart(), getEnd() );
		}
		return fArrayPartitionValue;
	}

	static public List splitArray( CDebugElement parent, ICDIVariable cdiVariable, int start, int end ) throws DebugException
	{
		ArrayList children = new ArrayList();
		int len = end - start + 1;
		int perSlot = 1;
		while( len > perSlot * SLOT_SIZE )
		{
			perSlot *= SLOT_SIZE;
		}
		if ( perSlot == 1 )
		{
			try
			{
				ICDIValue value = cdiVariable.getValue();
				if ( value instanceof ICDIArrayValue )
				{
					ICDIVariable[] cdiVars = ((ICDIArrayValue)value).getVariables( start, len );
					for ( int i = 0; i < cdiVars.length; ++i )
						children.add( new CModificationVariable( parent, cdiVars[i] ) );
				}
			}
			catch( CDIException e )
			{
				children.add( new CModificationVariable( parent, new CVariable.ErrorVariable( null, e ) ) );
			}
		}
		else
		{
			int pos = start;
			while( pos <= end )
			{
				if ( pos + perSlot > end )
				{
					perSlot = end - pos + 1;
				}
				children.add( new CArrayPartition( parent, cdiVariable, pos, pos + perSlot - 1 ) );
				pos += perSlot;
			}
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#canEnableDisable()
	 */
	public boolean canEnableDisable()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#getType()
	 */
	public ICType getType() throws DebugException
	{
		if ( fType == null )
		{
			try
			{
				ICDIVariableObject varObject = getVariableObject();
				if ( varObject != null )
					fType = new CType( varObject.getType() );
			}
			catch (CDIException e)
			{
				requestFailed( CoreModelMessages.getString( "CArrayPartition.0" ), e ); //$NON-NLS-1$
			}
		}
		return fType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICVariable#hasChildren()
	 */
	public boolean hasChildren()
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.CVariable#getQualifiedName()
	 */
	protected String getQualifiedName() throws DebugException
	{
		if ( fQualifiedName == null )
		{
			try
			{
				if ( getVariableObject() != null )
				{
					fQualifiedName = getVariableObject().getQualifiedName();
				}
			}
			catch( CDIException e )
			{
				requestFailed( CoreModelMessages.getString( "CArrayPartition.1" ), e ); //$NON-NLS-1$
			}
		}
		return fQualifiedName;
	}

	private ICDIVariableObject getVariableObject() throws CDIException
	{
		if ( fCDIVariableObject == null )
		{
			fCDIVariableObject = getCDISession().getVariableManager().getVariableObjectAsArray( fCDIVariable, getStart(), getEnd() - getStart() + 1 );
		}
		return fCDIVariableObject;
	}
}
