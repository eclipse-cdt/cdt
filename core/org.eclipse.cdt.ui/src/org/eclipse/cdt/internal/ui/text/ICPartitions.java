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

package org.eclipse.cdt.internal.ui.text;

/**
 * 
 * The name of the C partitioning.
 * @since 3.0
 */
public interface ICPartitions {

	String SKIP= "__skip"; //$NON-NLS-1$
	/**
	 * The identifier multi-line (JLS2: TraditionalComment) comment partition content type.
	 */
	String C_MULTILINE_COMMENT= "c_multi_line_comment"; //$NON-NLS-1$

	/**
	 * The identifier of the single-line (JLS2: EndOfLineComment) end comment partition content type.
	 */
	String C_SINGLE_LINE_COMMENT= "c_single_line_comment"; //$NON-NLS-1$
	
	/**
	 * The identifier of the C string partition content type.
	 */
	String C_STRING= "c_string"; //$NON-NLS-1$
	
	/**
	 * The identifier of the C character partition content type.
	 */
	String C_CHARACTER= "c_character";  //$NON-NLS-1$

}
