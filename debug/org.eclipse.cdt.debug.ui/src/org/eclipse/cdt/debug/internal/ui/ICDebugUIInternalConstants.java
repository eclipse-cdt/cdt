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

package org.eclipse.cdt.debug.internal.ui;

/**
 * 
 * Definitions of the internal constants for C/C++ Debug UI plug-in.
 * 
 * @since Jul 25, 2002
 */
public interface ICDebugUIInternalConstants
{
	/*
	 * Memory view constants.
	 */
	public static final int MEMORY_SIZE_BYTE = 1;
	public static final int MEMORY_SIZE_HALF_WORD = 2;
	public static final int MEMORY_SIZE_WORD = 4;
	public static final int MEMORY_SIZE_DOUBLE_WORD = 8;
	public static final int MEMORY_SIZE_FLOAT = 8;
	public static final int MEMORY_SIZE_DOUBLE_FLOAT = 16;

	public static final int MEMORY_FORMAT_HEX = 0;
	public static final int MEMORY_FORMAT_BINARY = 1;
	public static final int MEMORY_FORMAT_OCTAL = 2;
	public static final int MEMORY_FORMAT_SIGNED_DECIMAL = 3;
	public static final int MEMORY_FORMAT_UNSIGNED_DECIMAL = 4;

	public static final int MEMORY_BYTES_PER_ROW_4 = 4;
	public static final int MEMORY_BYTES_PER_ROW_8 = 8;
	public static final int MEMORY_BYTES_PER_ROW_16 = 16;
	public static final int MEMORY_BYTES_PER_ROW_32 = 32;
	public static final int MEMORY_BYTES_PER_ROW_64 = 64;
	public static final int MEMORY_BYTES_PER_ROW_128 = 128;

	public static final int DEFAULT_MEMORY_NUMBER_OF_BYTES = 512;
	public static final int DEFAULT_MEMORY_SIZE = MEMORY_SIZE_BYTE;
	public static final int DEFAULT_MEMORY_FORMAT = MEMORY_FORMAT_HEX;
	public static final int DEFAULT_MEMORY_BYTES_PER_ROW = MEMORY_BYTES_PER_ROW_16;
	public static final boolean DEFAULT_MEMORY_DISPLAY_ASCII = true;
}
