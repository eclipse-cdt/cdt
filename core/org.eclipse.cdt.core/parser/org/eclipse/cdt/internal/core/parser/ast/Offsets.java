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
package org.eclipse.cdt.internal.core.parser.ast;

/**
 * @author jcamelon
 *
 */
public class Offsets {

	protected int startingOffset = 0;

	protected int endingOffset = 0;

	public void setStartingOffset(int o) {
		startingOffset = o;
	}

	public void setEndingOffset(int o) {
		endingOffset = o;
	}

	public int getElementStartingOffset() {
		return startingOffset;
	}

	public int getElementEndingOffset() {
		return endingOffset;
	}

}
