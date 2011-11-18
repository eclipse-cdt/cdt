/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.internal.core.dom.parser.ASTAmbiguousNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * Handles ambiguities for simple declarations.
 * <br>
 * class C {
 *    C(D);  // if D a type we have a constructor, otherwise this declares the field D.
 * };
 */
public class CPPASTAmbiguousSimpleDeclaration extends ASTAmbiguousNode implements IASTAmbiguousSimpleDeclaration {

    private IASTSimpleDeclaration fSimpleDecl;
    private IASTDeclSpecifier fAltDeclSpec;
    private IASTDeclarator fAltDtor;
    
    public CPPASTAmbiguousSimpleDeclaration(IASTSimpleDeclaration decl, IASTDeclSpecifier declSpec, IASTDeclarator dtor) {
    	fSimpleDecl= decl;
    	fAltDeclSpec= declSpec;
    	fAltDtor= dtor;
	}

	@Override
	protected void beforeResolution() {
		// populate containing scope, so that it will not be affected by the alternative branches.
		IScope scope= CPPVisitor.getContainingScope(this);
		if (scope instanceof IASTInternalScope) {
			((IASTInternalScope) scope).populateCache();
		}
	}

    @Override
	public IASTNode[] getNodes() {
        return new IASTNode[] {fSimpleDecl, fAltDeclSpec, fAltDtor};
    }

	@Override
	public IASTSimpleDeclaration copy() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IASTSimpleDeclaration copy(CopyStyle style) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addDeclarator(IASTDeclarator declarator) {
		fSimpleDecl.addDeclarator(declarator);
	}

	@Override
	public IASTDeclSpecifier getDeclSpecifier() {
		return fSimpleDecl.getDeclSpecifier();
	}

	@Override
	public IASTDeclarator[] getDeclarators() {
		return fSimpleDecl.getDeclarators();
	}

	@Override
	public void setDeclSpecifier(IASTDeclSpecifier declSpec) {
		fSimpleDecl.setDeclSpecifier(declSpec);
	}
	
	@Override
	protected final IASTNode doResolveAmbiguity(ASTVisitor resolver) {
		final IASTAmbiguityParent owner= (IASTAmbiguityParent) getParent();
		IASTNode nodeToReplace= this;

		// handle nested ambiguities first
		owner.replace(nodeToReplace, fSimpleDecl);
		IASTDeclarator dtor= fSimpleDecl.getDeclarators()[0];
		dtor.accept(resolver);
		

		// find nested names
		final NameCollector nameCollector= new NameCollector();
		dtor.accept(nameCollector);
		final IASTName[] names= nameCollector.getNames();

		// resolve names 
		boolean hasIssue= false;
		for (IASTName name : names) {
			try {
				IBinding b = name.resolveBinding();
				if (b instanceof IProblemBinding) {
					hasIssue= true;
					break;
				}
			} catch (Exception t) {
				hasIssue= true;
				break;
			}
		}
		if (hasIssue) {
			// use the alternate version
			final IASTAmbiguityParent parent = (IASTAmbiguityParent) fSimpleDecl;
			parent.replace(fSimpleDecl.getDeclSpecifier(), fAltDeclSpec);
			parent.replace(dtor, fAltDtor);
		}
			
		// resolve further nested ambiguities
		fSimpleDecl.accept(resolver);
		return fSimpleDecl;
	}
}
