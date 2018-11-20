/*******************************************************************************
 *  Copyright (c) 2000, 2009 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.ui.text;

/**
 * Definition of C partitioning and its partitions.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 4.0
 */
public interface ICPartitions {

	/**
	 * The identifier of the C partitioning.
	 */
	String C_PARTITIONING = "___c_partitioning"; //$NON-NLS-1$

	/**
	 * The identifier of the single-line comment partition content type.
	 */
	String C_SINGLE_LINE_COMMENT = "__c_singleline_comment"; //$NON-NLS-1$

	/**
	 * The identifier multi-line comment partition content type.
	 */
	String C_MULTI_LINE_COMMENT = "__c_multiline_comment"; //$NON-NLS-1$

	/**
	 * The identifier of the C string partition content type.
	 */
	String C_STRING = "__c_string"; //$NON-NLS-1$

	/**
	 * The identifier of the C character partition content type.
	 */
	String C_CHARACTER = "__c_character"; //$NON-NLS-1$

	/**
	 * The identifier of the C preprocessor partition content type.
	 */
	String C_PREPROCESSOR = "__c_preprocessor"; //$NON-NLS-1$

	/**
	 * The identifier of the single-line documentation tool comment partition content type.
	 * @since 5.0
	 */
	String C_SINGLE_LINE_DOC_COMMENT = "__c_singleline_doc_comment"; //$NON-NLS-1$

	/**
	 * The identifier multi-line comment documentation tool partition content type.
	 * @since 5.0
	 */
	String C_MULTI_LINE_DOC_COMMENT = "__c_multiline_doc_comment"; //$NON-NLS-1$

	/**
	 * All defined CDT editor partitions.
	 * @since 5.0
	 */
	String[] ALL_CPARTITIONS = { ICPartitions.C_MULTI_LINE_COMMENT, ICPartitions.C_SINGLE_LINE_COMMENT,
			ICPartitions.C_STRING, ICPartitions.C_CHARACTER, ICPartitions.C_PREPROCESSOR,
			ICPartitions.C_SINGLE_LINE_DOC_COMMENT, ICPartitions.C_MULTI_LINE_DOC_COMMENT };

	/**
	 * Array of all assembly partitions.
	 * @since 5.1
	 */
	String[] ALL_ASM_PARTITIONS = new String[] { ICPartitions.C_MULTI_LINE_COMMENT, ICPartitions.C_SINGLE_LINE_COMMENT,
			ICPartitions.C_STRING, ICPartitions.C_CHARACTER, ICPartitions.C_PREPROCESSOR };

}
