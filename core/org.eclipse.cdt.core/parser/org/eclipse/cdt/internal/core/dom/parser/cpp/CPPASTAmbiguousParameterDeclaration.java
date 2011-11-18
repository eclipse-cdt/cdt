/*******************************************************************************
 * Copyright (c) 2008, 2011 IBM Wind River Systems, Inc. and others.
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
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameterPackType;
import org.eclipse.cdt.internal.core.dom.parser.ASTAmbiguousNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.Assert;

/**
 * Handles ambiguities for ellipsis in parameter declaration.
 * <br>
 * template<typename... T> void function(T ...); // is T a parameter pack?
 */
public class CPPASTAmbiguousParameterDeclaration extends ASTAmbiguousNode implements
		IASTAmbiguousParameterDeclaration, ICPPASTParameterDeclaration {

    private ICPPASTParameterDeclaration fParameterDecl;

    public CPPASTAmbiguousParameterDeclaration(ICPPASTParameterDeclaration decl) {
    	fParameterDecl= decl;
	}

	@Override
	public void addParameterDeclaration(IASTParameterDeclaration d) {
		assert false;
    }

	
    @Override
	protected final IASTNode doResolveAmbiguity(ASTVisitor resolver) {
		final IASTAmbiguityParent owner= (IASTAmbiguityParent) getParent();
		
		// Setup the ast to use the alternative
		owner.replace(this, fParameterDecl);
		fParameterDecl.accept(resolver);
		
		IType t= CPPVisitor.createType(fParameterDecl, true);
		if (!(t instanceof ICPPParameterPackType) || 
				!CPPTemplates.containsParameterPack(((ICPPParameterPackType) t).getType())) {
			final ICPPASTDeclarator dtor = fParameterDecl.getDeclarator();
			dtor.setDeclaresParameterPack(false);
			adjustOffsets(dtor);
			((ICPPASTFunctionDeclarator) fParameterDecl.getParent()).setVarArgs(true);
		}
		return fParameterDecl;
	}

	private void adjustOffsets(final ICPPASTDeclarator dtor) {
		IASTPointerOperator[] ptrOps= dtor.getPointerOperators();
		final ASTNode asNode = (ASTNode) dtor;
		if (ptrOps.length > 0) {
			final ASTNode first = (ASTNode)ptrOps[0];
			final ASTNode last = (ASTNode)ptrOps[ptrOps.length-1];
			asNode.setOffsetAndLength(first.getOffset(), last.getOffset() + last.getLength());
		} else {
			asNode.setOffsetAndLength(0, 0);
		}
	}

	@Override
	public IASTParameterDeclaration[] getParameterDeclarations() {
    	return new IASTParameterDeclaration[] {fParameterDecl};
    }

    @Override
	public IASTNode[] getNodes() {
        return getParameterDeclarations();
    }

	@Override
	public IASTDeclSpecifier getDeclSpecifier() {
		return fParameterDecl.getDeclSpecifier();
	}

	@Override
	public ICPPASTDeclarator getDeclarator() {
		return fParameterDecl.getDeclarator();
	}

	@Override
	public void setDeclSpecifier(IASTDeclSpecifier declSpec) {
		Assert.isLegal(false);
	}

	@Override
	public void setDeclarator(IASTDeclarator declarator) {
		Assert.isLegal(false);
	}

	@Override
	public ICPPASTParameterDeclaration copy() {
		Assert.isLegal(false);
		return null;
	}

	@Override
	public ICPPASTParameterDeclaration copy(CopyStyle style) {
		Assert.isLegal(false);
		return null;
	}

	@Override
	public boolean isParameterPack() {
		Assert.isLegal(false);
		return true;
	}
}
