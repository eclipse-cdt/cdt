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

import org.eclipse.cdt.internal.core.parser.pst.ISymbol;


/**
 * @author jcamelon
 *
 */
public class ASTASMDefinition implements IASTASMDefinition {

	private final String body; 
	
	public ASTASMDefinition( ISymbol s, String body )
	{
		this.symbol = s;
		this.body = body;
	}
	
	private int startingOffset, endingOffset;
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTASMDefinition#getBody()
	 */
	public String getBody() {
		return body;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IOffsetableElement#setStartingOffset(int)
	 */
	public void setStartingOffset(int o) {
		startingOffset = o;
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IOffsetableElement#setEndingOffset(int)
	 */
	public void setEndingOffset(int o) {
		endingOffset = o;
		
	}
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
	 * @see org.eclipse.cdt.internal.core.parser.ast.ISymbolTableExtension#getSymbol()
	 */
	
	private final ISymbol symbol; 
	public ISymbol getSymbol() {
		return symbol;
	}
}
