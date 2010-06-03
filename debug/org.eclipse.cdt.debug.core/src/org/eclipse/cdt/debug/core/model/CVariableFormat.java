/*******************************************************************************
 * Copyright (c) 2004, 2008 QNX Software Systems and others.
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
	private final int fNum;
	
	private CVariableFormat( String name, int num ) {
		this.fName = name;
		this.fNum= num;
	}
	
	public String toString() {
		return this.fName;
	}
	
	public int getFormatNumber() {
		return this.fNum;
	}

	public static CVariableFormat getFormat( int code ) {
		if ( code == NATURAL.getFormatNumber() ) {
			return NATURAL;
		} else if ( code == DECIMAL.getFormatNumber() ) {
			return DECIMAL;
		} else if ( code == BINARY.getFormatNumber() ) {
			return BINARY;
		} else if ( code == OCTAL.getFormatNumber() ) {
			return OCTAL;
		} else if ( code == HEXADECIMAL.getFormatNumber() ) {
			return HEXADECIMAL;
		} else {
			// unexpected value, mapping to NATURAL
			return NATURAL;
		}
	}

	public static final CVariableFormat NATURAL = new CVariableFormat( "natural", 0 ); //$NON-NLS-1$
	public static final CVariableFormat DECIMAL = new CVariableFormat( "decimal", 1 ); //$NON-NLS-1$
	public static final CVariableFormat BINARY = new CVariableFormat( "binary", 2 ); //$NON-NLS-1$
	public static final CVariableFormat OCTAL = new CVariableFormat( "octal", 3 ); //$NON-NLS-1$
	public static final CVariableFormat HEXADECIMAL = new CVariableFormat( "hexadecimal", 4 ); //$NON-NLS-1$
}
