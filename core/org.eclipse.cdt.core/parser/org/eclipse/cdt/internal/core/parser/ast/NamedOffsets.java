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
public class NamedOffsets extends Offsets  {

	private int nameEndOffset = 0;
    private int nameOffset = 0; 

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getElementNameOffset()
	 */
	public int getNameOffset() {
		return nameOffset;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#setNameOffset(int)
	 */
	public void setNameOffset(int o) {
		nameOffset = o; 
	}
	
	public int getNameEndOffset()
	{
		return nameEndOffset; 
	}

	public void setNameEndOffset( int offset )
	{
		nameEndOffset = offset;
	}

}
