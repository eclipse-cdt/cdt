/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.debug.core.DebugException;

/**
 *
 * Generates values for variable and expressions.
 * 
 * @since Sep 9, 2002
 */
public class CValueFactory
{
	static public CValue createValue( CVariable parent, ICDIValue cdiValue ) throws DebugException
	{
		return new CValue( parent, cdiValue );
	}

	static public CValue createGlobalValue( CVariable parent, ICDIValue cdiValue ) throws DebugException
	{
		return new CGlobalValue( parent, cdiValue );
	}
}
