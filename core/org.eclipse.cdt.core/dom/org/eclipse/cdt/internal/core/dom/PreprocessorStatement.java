/**********************************************************************
 * Created on Apr 7, 2003
 *
 * Copyright (c) 2002,2003 IBM/Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Ltd. - Rational Software - Initial API and implementation
************************************************************************/
package org.eclipse.cdt.internal.core.dom;

/**
 * @author jcamelon
 *
 */
public class PreprocessorStatement implements IOffsetable {
	
	private int startingOffset, totalLength;
	private final int nameOffset;
	private final String name; 
	
	public PreprocessorStatement( String name, int nameOffset, int startingOffset, int totalLength )
	{
		this.name =name;
		this.nameOffset = nameOffset;
		this.startingOffset = startingOffset;
		this.totalLength = totalLength;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IOffsettable#getStartingOffset()
	 */
	public int getStartingOffset() {
		return startingOffset;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.IOffsettable#getTotalLength()
	 */
	public int getTotalLength() {
		return totalLength;
	}


	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return
	 */
	public int getNameOffset() {
		return nameOffset;
	}
	
	public int getNameLength() {
		return name.length();
	}
	/**
	 * @see org.eclipse.cdt.internal.core.dom.IOffsettable#setStartingOffset(int)
	 */
	public void setStartingOffset(int i) {
		startingOffset = i;
	}
	/**
	 * @see org.eclipse.cdt.internal.core.dom.IOffsettable#setTotalLength(int)
	 */
	public void setTotalLength(int i) {
		totalLength = i;
	}

}
