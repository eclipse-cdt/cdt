/**********************************************************************
 * Copyright (c) 2002-2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.token;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;

/**
 * @author jcamelon
 *
 */
public class SegmentedTokenDuple extends BasicTokenDuple {

	/**
	 * @param first
	 * @param last
	 */
	public SegmentedTokenDuple(IToken first, IToken last) {
		super(first, last);
		numSegments = calculateSegmentCount();
	}
	
	private final int numSegments;
	


}
