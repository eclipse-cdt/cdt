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
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayValue;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatingPointValue;
import org.eclipse.cdt.debug.core.model.ICValue;

/**
 * The value factory for variable and expressions.
 */
public class CValueFactory {

	static public CValue createValue( CVariable parent, ICDIValue cdiValue ) {
		if ( cdiValue instanceof ICDIFloatingPointValue ) {
			return new CFloatingPointValue( parent, cdiValue );
		}
		return new CValue( parent, cdiValue );
	}

	static public CIndexedValue createIndexedValue( AbstractCVariable parent, ICDIArrayValue cdiValue, int start, int end ) {
		return new CIndexedValue( parent, cdiValue, start, end - start + 1 );
	}

	static public CValue createGlobalValue( CVariable parent, ICDIValue cdiValue ) {
		return new CGlobalValue( parent, cdiValue );
	}

	static public ICValue createValueWithError( CVariable parent, String message ) {
		return new CValue( parent, message );
	}
}