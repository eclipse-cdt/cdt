/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Rational Software - Initial API and implementation
 *     Mike Kucera (IBM) - convert to Java 5 enum
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

public enum KeywordSetKey {
	EMPTY,
	DECL_SPECIFIER_SEQUENCE,
	DECLARATION,
	STATEMENT,
	BASE_SPECIFIER,
	POST_USING,
	FUNCTION_MODIFIER,
	NAMESPACE_ONLY,
	MACRO,
	PP_DIRECTIVE,
	EXPRESSION,
	MEMBER,
	ALL,
	KEYWORDS,
	TYPES,
}
