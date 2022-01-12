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
 *     Mike Kucera (IBM) - convert to Java 5 enum
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser;

/**
 * @author jcamelon
 *
 */
public enum SimpleDeclarationStrategy {
	TRY_CONSTRUCTOR, TRY_FUNCTION, TRY_VARIABLE,
}
