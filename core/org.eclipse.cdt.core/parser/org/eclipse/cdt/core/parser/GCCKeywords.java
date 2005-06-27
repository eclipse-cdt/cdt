/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.parser;

/**
 * @author jcamelon
 *
 */
public class GCCKeywords {
	
	public static final String TYPEOF = "typeof"; //$NON-NLS-1$
	public static final String __ALIGNOF__ = "__alignof__"; //$NON-NLS-1$

	public static final char [] cpTYPEOF = TYPEOF.toCharArray();
	public static final char [] cp__ALIGNOF__ = __ALIGNOF__.toCharArray();

}
