/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.debug.core.ICValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.debug.core.DebugException;

/**
 *
 * Generates variable values.
 * 
 * @since Sep 9, 2002
 */
public class CValueFactory
{
	/**
	 * Creates the appropriate kind of value, or <code>null</code>.
	 * 
	 */
	static public ICValue createValue( CVariable parent, ICDIValue cdiValue ) throws DebugException
	{
		return new CValue( parent, cdiValue );
	}
}
