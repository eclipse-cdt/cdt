/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.cdt.debug.internal.core.model; 

import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject;

/**
 * Provides factory methods for the variable types.
 */
public class CVariableFactory {

	public static CVariable createVariable( CDebugElement parent, ICDIVariableObject cdiVariableObject ) {
		return new CVariable( parent, cdiVariableObject );
	}

	public static CVariable createVariableWithError( CDebugElement parent, ICDIVariableObject cdiVariableObject, String message ) {
		return new CVariable( parent, cdiVariableObject, message );
	}

	public static CGlobalVariable createGlobalVariable( CDebugElement parent, ICDIVariableObject cdiVariableObject ) {
		return new CGlobalVariable( parent, cdiVariableObject );
	}
}
