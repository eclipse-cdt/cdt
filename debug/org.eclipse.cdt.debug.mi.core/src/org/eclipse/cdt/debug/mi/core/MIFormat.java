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
package org.eclipse.cdt.debug.mi.core;

/**
 * Help class to specify formats.
 */
public final class MIFormat {
	public final static int HEXADECIMAL = 0;
	public final static int OCTAL = 1;
	public final static int BINARY = 2;
	public final static int DECIMAL = 3;
	public final static int RAW = 4;
	public final static int NATURAL = 5;

	public final static int FLOAT = 10;
	public final static int ADDRESS = 11;
	public final static int INSTRUCTION = 12;
	public final static int CHAR = 13;
	public final static int STRING = 14;
	public final static int UNSIGNED = 15;

	// no instanciation.
	private MIFormat() {
	}
}
