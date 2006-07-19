/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.text;

/**
 * Definition of C partitioning and its partitions.
 */
public interface ICPartitions {

	/**
	 * The identifier of the C partitioning.
	 */
	String C_PARTITIONING= "___c_partitioning";  //$NON-NLS-1$

	/**
	 * The identifier of the single-line end comment partition content type.
	 */
	String C_SINGLE_LINE_COMMENT= "__c_singleline_comment"; //$NON-NLS-1$

	/**
	 * The identifier multi-line comment partition content type.
	 */
	String C_MULTI_LINE_COMMENT= "__c_multiline_comment"; //$NON-NLS-1$

	/**
	 * The identifier of the C string partition content type.
	 */
	String C_STRING= "__c_string"; //$NON-NLS-1$

	/**
	 * The identifier of the C character partition content type.
	 */
	String C_CHARACTER= "__c_character";  //$NON-NLS-1$
}
