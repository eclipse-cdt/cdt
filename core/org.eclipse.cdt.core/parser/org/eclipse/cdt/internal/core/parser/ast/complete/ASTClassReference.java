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
package org.eclipse.cdt.internal.core.parser.ast.complete;

import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ast.IASTClassReference;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;


public class ASTClassReference extends ASTReference
		implements
			IASTClassReference {
	private IASTTypeSpecifier reference;
	/**
	 * @param i
	 * @param specifier
	 */
	public ASTClassReference(int i, IASTTypeSpecifier specifier) {
		super(i);
		reference = specifier;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ASTReference#initialize(int)
	 */
	public void initialize(int o, ISourceElementCallbackDelegate specifier) {
		super.initialize(o);
		reference = (IASTTypeSpecifier) specifier;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ASTReference#reset()
	 */
	public void reset() {
		super.resetOffset();
		reference = null;
	}
	/**
	 *  
	 */
	public ASTClassReference() {
		super(0);
		reference = null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTReference#getReferencedElement()
	 */
	public ISourceElementCallbackDelegate getReferencedElement() {
		return (ISourceElementCallbackDelegate) reference;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
	 */
	public void acceptElement(ISourceElementRequestor requestor) {
		try {
			requestor.acceptClassReference(this);
		} catch (Exception e) {
			/* do nothing */
		}
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTClassReference#isResolved()
	 */
	public boolean isResolved() {
		return (reference instanceof IASTClassSpecifier);
	}
}
