/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

/**
 * @author hamer
 *
 */
public interface RelevanceConstants {
	final int KEYWORD_TYPE_RELEVANCE = 13;
	final int LOCAL_VARIABLE_TYPE_RELEVANCE = 12;
	final int FIELD_TYPE_RELEVANCE = 11;
	final int VARIABLE_TYPE_RELEVANCE = 10;
	final int METHOD_TYPE_RELEVANCE = 9;
	final int FUNCTION_TYPE_RELEVANCE = 8;
	final int CLASS_TYPE_RELEVANCE = 7;
	final int STRUCT_TYPE_RELEVANCE = 6;
	final int UNION_TYPE_RELEVANCE = 5;
	final int NAMESPACE_TYPE_RELEVANCE = 4;
	final int MACRO_TYPE_RELEVANCE = 3;
	final int ENUMERATION_TYPE_RELEVANCE = 2;
	final int ENUMERATOR_TYPE_RELEVANCE = 1;
	final int DEFAULT_TYPE_RELEVANCE = 0;
	
	final int CASE_MATCH_RELEVANCE = 10;
	final int EXACT_NAME_MATCH_RELEVANCE = 4;
	final int CASE_NOT_VALID_RELEVANCE = -100;
	
}
