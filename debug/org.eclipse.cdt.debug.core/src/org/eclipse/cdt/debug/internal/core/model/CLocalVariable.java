/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;

/**
 * 
 * Enter type comment.
 * 
 * @since Aug 9, 2002
 */
public class CLocalVariable extends CModificationVariable
{
	/**
	 * Constructor for CLocalVariable.
	 * @param target
	 */
	public CLocalVariable( CDebugElement parent, ICDIVariable cdiVariable )
	{
		super( parent, cdiVariable );
	}
}
