/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */

package org.eclipse.cdt.internal.core.parser.ast.complete;

import java.util.List;

import org.eclipse.cdt.core.parser.ast.ASTUtil;

/**
 * @author jcamelon
 *
 */
public class ASTEmptyExpression extends ASTExpression {
	/**
	 * @param kind
	 * @param references
	 */
	public ASTEmptyExpression(Kind kind, List references) {
		super( kind, references );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#freeReferences(org.eclipse.cdt.core.parser.ast.IReferenceManager)
	 */
	public void freeReferences() {
	}
	
	public String toString(){
		return ASTUtil.getExpressionString( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setStartingOffsetAndLineNumber(int, int)
	 */
	public void setStartingOffsetAndLineNumber(int offset, int lineNumber) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setEndingOffsetAndLineNumber(int, int)
	 */
	public void setEndingOffsetAndLineNumber(int offset, int lineNumber) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getStartingOffset()
	 */
	public int getStartingOffset() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getEndingOffset()
	 */
	public int getEndingOffset() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getStartingLine()
	 */
	public int getStartingLine() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getEndingLine()
	 */
	public int getEndingLine() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getFilename()
	 */
	public char[] getFilename() {
		// TODO Auto-generated method stub
		return null;
	}
}
