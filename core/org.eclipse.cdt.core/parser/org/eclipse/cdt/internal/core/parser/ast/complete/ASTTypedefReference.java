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
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTypedefReference;


public class ASTTypedefReference extends ASTReference
		implements
			IASTTypedefReference {
	private IASTTypedefDeclaration referencedItem;
	/**
	 * @param offset
	 */
	public ASTTypedefReference(int offset,
			IASTTypedefDeclaration referencedItem) {
		super(offset);
		this.referencedItem = referencedItem;
	}
	/**
	 * 
	 */
	public ASTTypedefReference() {
		super( 0 );
		this.referencedItem = null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ast.IASTReference#getReferencedElement()
	 */
	public ISourceElementCallbackDelegate getReferencedElement() {
		return referencedItem;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.cdt.core.parser.ISourceElementRequestor)
	 */
	public void acceptElement(ISourceElementRequestor requestor) {
		try {
			requestor.acceptTypedefReference(this);
		} catch (Exception e) {
			/* do nothing */
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#initialize(int, org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate)
	 */
	public void initialize(int o, ISourceElementCallbackDelegate referencedElement) {
		super.initialize(o);
		referencedItem = (IASTTypedefDeclaration) referencedElement;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ReferenceCache.ASTReference#reset()
	 */
	public void reset() {
		super.resetOffset();
		referencedItem = null;
	}
}
