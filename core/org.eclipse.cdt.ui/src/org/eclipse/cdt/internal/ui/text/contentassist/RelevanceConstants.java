/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Rational Software - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

public interface RelevanceConstants {
	/**
	 * For inclusion proposals only, relevance increment for matches
	 * with a file type known to be a header file.
	 */
	final int FILE_TYPE_RELEVANCE = 2000;

	/** Relevance increment for same case matches */
	final int CASE_MATCH_RELEVANCE = 1000;

	/** Relevance increment for exact name matches (disregarding case) */
	final int EXACT_NAME_MATCH_RELEVANCE = 40;

	/** Relevance constant for proposals contributed by help provider */
	final int HELP_TYPE_RELEVANCE = 200;

	// parsing-based relevance constants
	final int LOCAL_VARIABLE_TYPE_RELEVANCE = 140;
	final int FIELD_TYPE_RELEVANCE = 130;
	final int METHOD_TYPE_RELEVANCE = 120;
	final int VARIABLE_TYPE_RELEVANCE = 110;
	final int FUNCTION_TYPE_RELEVANCE = 100;
	final int CLASS_TYPE_RELEVANCE = 90;
	final int STRUCT_TYPE_RELEVANCE = 80;
	final int UNION_TYPE_RELEVANCE = 70;
	final int TYPEDEF_TYPE_RELEVANCE = 60;
	final int NAMESPACE_TYPE_RELEVANCE = 50;
	final int ENUMERATOR_TYPE_RELEVANCE = 40;
	final int ENUMERATION_TYPE_RELEVANCE = 30;
	final int DEFAULT_TYPE_RELEVANCE = 20;
	final int MACRO_TYPE_RELEVANCE = 15;

	/** Relevance constant for (key-)word proposals */
	final int KEYWORD_TYPE_RELEVANCE = 10;

	/** Relevance constant for editor template proposals */
	final int TEMPLATE_TYPE_RELEVANCE = 5;

}
