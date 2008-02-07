/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
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

/**
 * @author jcamelon
 *
 */
public enum ParserMode  {
	
	// do not follow inclusions, do not parse function/method bodies
	QUICK_PARSE,

	//follow inclusions, do not parse function/method bodies
	STRUCTURAL_PARSE,
	
	// follow inclusions, parse function/method bodies
	COMPLETE_PARSE,
	
	// follow inclusions, parse function/method bodies, stop at particular offset
	// provide optimized lookup capability for querying symbols
	COMPLETION_PARSE,

	// follow inclusions, parse function/method bodies, stop at particular offset
	// provide specific semantic information about an offset range or selection
	SELECTION_PARSE,
	
}
