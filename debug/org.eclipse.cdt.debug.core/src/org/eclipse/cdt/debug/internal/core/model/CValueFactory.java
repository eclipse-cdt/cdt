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

import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
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

	static public CArrayPartitionValue createArrayValue( CVariable parent, ICDIVariable cdiVariable, int start, int end ) throws DebugException
	{
		return new CArrayPartitionValue( parent, cdiVariable, start, end );
	}

	static public CValue createGlobalValue( CVariable parent, ICDIValue cdiValue ) throws DebugException
	{
		return new CGlobalValue( parent, cdiValue );
	}
}
