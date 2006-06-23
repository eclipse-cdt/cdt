/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

public interface RelevanceConstants {
	final int CASE_MATCH_RELEVANCE = 160;
	final int EXACT_NAME_MATCH_RELEVANCE = 40;
	
	final int LOCAL_VARIABLE_TYPE_RELEVANCE = 140;
	final int FIELD_TYPE_RELEVANCE = 130;
	final int VARIABLE_TYPE_RELEVANCE = 120;
	final int METHOD_TYPE_RELEVANCE = 110;
	final int FUNCTION_TYPE_RELEVANCE = 100;
	final int CLASS_TYPE_RELEVANCE = 90;
	final int STRUCT_TYPE_RELEVANCE = 80;
	final int UNION_TYPE_RELEVANCE = 70;
	final int TYPEDEF_TYPE_RELEVANCE = 60;
	final int NAMESPACE_TYPE_RELEVANCE = 50;
	final int ENUMERATOR_TYPE_RELEVANCE = 40;
	final int ENUMERATION_TYPE_RELEVANCE = 30;
	final int MACRO_TYPE_RELEVANCE = 20;
	final int KEYWORD_TYPE_RELEVANCE = 10;
	final int DEFAULT_TYPE_RELEVANCE = 0;
}
