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

import org.eclipse.cdt.core.parser.ast.IASTInclusion;

/**
 * @author jcamelon
 *
 */
public class ASTInclusion implements IASTInclusion {

	public ASTInclusion( String name, String fileName, boolean local )
	{
		this.name = name; 
		this.fileName = fileName;
		this.local = local;
	}

	private final String name, fileName;
	private final boolean local; 
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTInclusion#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTInclusion#getFullFileName()
	 */
	public String getFullFileName() {
		return fileName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTInclusion#isLocal()
	 */
	public boolean isLocal() {
		return local;
	}


	private int startingOffset = 0, nameOffset = 0, endingOffset = 0; 
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IOffsetableElement#getElementStartingOffset()
	 */
	public int getElementStartingOffset() {
		return startingOffset;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IOffsetableElement#getElementEndingOffset()
	 */
	public int getElementEndingOffset() {
		return endingOffset;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IOffsetableElement#getElementNameOffset()
	 */
	public int getElementNameOffset() {
		return nameOffset;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.IOffsetableElementRW#setStartingOffset(int)
	 */
	public void setStartingOffset(int o) {
		startingOffset = o; 
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.IOffsetableElementRW#setEndingOffset(int)
	 */
	public void setEndingOffset(int o) {
		endingOffset = o; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.IOffsetableElementRW#setNameOffset(int)
	 */
	public void setNameOffset(int o) {
		nameOffset = o;
	}

}
