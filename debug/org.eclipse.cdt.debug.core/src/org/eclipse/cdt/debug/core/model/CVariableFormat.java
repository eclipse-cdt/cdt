/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model; 

/**
 * Defines the variable format types.
 */
public class CVariableFormat {
	
	private final String fName;
	
	private CVariableFormat( String name ) {
		this.fName = name;
	}
	
	public String toString() {
		return this.fName;
	}

	public static CVariableFormat getFormat( int code ) {
		switch( code ) {
			case 0:
				return NATURAL;
			case 1:
				return DECIMAL;
			case 2:
				return BINARY;
			case 3:
				return OCTAL;
			case 4:
				return HEXADECIMAL;
			default:
				return NATURAL;
		}
	}

	public static final CVariableFormat NATURAL = new CVariableFormat( "natural" ); //$NON-NLS-1$
	public static final CVariableFormat DECIMAL = new CVariableFormat( "decimal" ); //$NON-NLS-1$
	public static final CVariableFormat BINARY = new CVariableFormat( "binary" ); //$NON-NLS-1$
	public static final CVariableFormat OCTAL = new CVariableFormat( "octal" ); //$NON-NLS-1$
	public static final CVariableFormat HEXADECIMAL = new CVariableFormat( "hexadecimal" ); //$NON-NLS-1$
}
