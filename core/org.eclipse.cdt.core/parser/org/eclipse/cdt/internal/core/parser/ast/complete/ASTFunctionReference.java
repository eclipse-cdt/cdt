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
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTFunctionReference;
import org.eclipse.cdt.core.parser.ast.IASTReference;


public class ASTFunctionReference extends ASTReference
		implements
			IASTReference,
			IASTFunctionReference {
	private IASTFunction declaration;
	/**
	 * @param offset
	 */
	public ASTFunctionReference(int offset,
			IASTFunction referencedDeclaration) {
		super(offset);
		this.declaration = referencedDeclaration;
	}
	/**
	 * 
	 */
	public ASTFunctionReference() {
		super(0);
		declaration = null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTReference#getReferencedElement()
	 */
	public ISourceElementCallbackDelegate getReferencedElement() {
		return declaration;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
	 */
	public void acceptElement(ISourceElementRequestor requestor) {
		try {
			requestor.acceptFunctionReference(this);
		} catch (Exception e) {
			/* do nothing */
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#initialize(int, org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate)
	 */
	public void initialize(int o, ISourceElementCallbackDelegate referencedElement) {
		super.initialize(o);
		this.declaration = (IASTFunction) referencedElement;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#reset()
	 */
	public void reset() {
		resetOffset();
		this.declaration = null;			
	}
}
