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

public interface RelevanceConstants {
	final int CASE_MATCH_RELEVANCE = 1600;
	final int EXACT_NAME_MATCH_RELEVANCE = 400;
	
	final int LOCAL_VARIABLE_TYPE_RELEVANCE = 1400;
	final int FIELD_TYPE_RELEVANCE = 1300;
	final int VARIABLE_TYPE_RELEVANCE = 1200;
	final int METHOD_TYPE_RELEVANCE = 1100;
	final int FUNCTION_TYPE_RELEVANCE = 1000;
	final int CLASS_TYPE_RELEVANCE = 900;
	final int STRUCT_TYPE_RELEVANCE = 800;
	final int UNION_TYPE_RELEVANCE = 700;
	final int TYPEDEF_TYPE_RELEVANCE = 600;
	final int NAMESPACE_TYPE_RELEVANCE = 500;
	final int MACRO_TYPE_RELEVANCE = 400;
	final int ENUMERATION_TYPE_RELEVANCE = 300;
	final int ENUMERATOR_TYPE_RELEVANCE = 200;
	final int KEYWORD_TYPE_RELEVANCE = 100;
	final int DEFAULT_TYPE_RELEVANCE = 0;
}
