/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.debug.core.DebugException;

/**
 *
 * An entry in an array.
 * 
 * @since Sep 9, 2002
 */
public class CArrayEntryVariable extends CModificationVariable
{	
	/**
	 * The index of the variable entry.
	 */
	private int fIndex;

	/**
	 * The type name of this variable. Cached lazily.
	 */
	private String fReferenceTypeName = null;

	/**
	 * Constructor for CArrayEntryVariable.
	 * @param target
	 */
	public CArrayEntryVariable( CDebugElement parent, ICDIVariable cdiVariable, int index )
	{
		super( parent, cdiVariable );
		fIndex = index;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#getName()
	 */
	public String getName() throws DebugException
	{
		return "[" + getIndex() + "]";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IVariable#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException
	{
		return stripBrackets( super.getReferenceTypeName() );
	}

	protected int getIndex()
	{
		return fIndex;
	}
	
	protected String stripBrackets( String typeName )
	{
		int index = typeName.lastIndexOf( '[' );
		if ( index < 0 )
			return typeName;
		return typeName.substring( 0, index );
	}
}
