/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.complete;

import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.parser.ast.IASTReference;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;


public abstract class ASTReference implements IASTReference {
	protected int offset;
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static final char[] EMPTY_CHAR_ARRAY = "".toCharArray(); //$NON-NLS-1$

	public abstract void reset();
	
	protected void resetOffset() {
		offset = 0;
	}

	/**
	 * @param offset2
	 * @param re 
	 */
	public abstract void initialize(int o, ISourceElementCallbackDelegate re );


	protected void initialize(int o) {
		this.offset = o;
	}

	/**
	 *  
	 */
	public ASTReference(int offset) {
		this.offset = offset;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTReference#getOffset()
	 */
	public int getOffset() {
		return offset;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTReference#getName()
	 */
	public String getName() {
		if (getReferencedElement() instanceof IASTOffsetableNamedElement)
			return ((IASTOffsetableNamedElement) getReferencedElement())
					.getName();
		return EMPTY_STRING;
	}
	public char[] getNameCharArray() {
		if (getReferencedElement() instanceof IASTOffsetableNamedElement)
			return ((IASTOffsetableNamedElement) getReferencedElement())
					.getNameCharArray();
		return EMPTY_CHAR_ARRAY;
	}
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof IASTReference))
			return false;

		if (  CharArrayUtils.equals( ((IASTReference) obj).getNameCharArray(), getNameCharArray() )
				&& ((IASTReference) obj).getOffset() == getOffset())
			return true;
		return false;
	}

	public void enterScope(ISourceElementRequestor requestor) {
	}

	public void exitScope(ISourceElementRequestor requestor) {
	}
}
