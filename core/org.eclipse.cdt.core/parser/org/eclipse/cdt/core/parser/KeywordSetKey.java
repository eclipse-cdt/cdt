/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
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
 *     Mike Kucera (IBM) - convert to Java 5 enum
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

public enum KeywordSetKey {
	EMPTY, DECL_SPECIFIER_SEQUENCE, DECLARATION, STATEMENT, BASE_SPECIFIER, POST_USING, FUNCTION_MODIFIER,
	NAMESPACE_ONLY, MACRO, PP_DIRECTIVE, EXPRESSION, MEMBER, ALL, KEYWORDS, TYPES,
}
