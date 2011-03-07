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
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTAmbiguousNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.IASTInternalScope;
import org.eclipse.core.runtime.Assert;

/**
 * Handles ambiguities for parameter declarations.
 * <br>
 * void function(const D*); // is D a type?
 * @since 5.0.1
 */
public class CASTAmbiguousParameterDeclaration extends ASTAmbiguousNode implements IASTAmbiguousParameterDeclaration {

    private IASTParameterDeclaration[] paramDecls = new IASTParameterDeclaration[2];
    private int declPos=-1;

    
    public CASTAmbiguousParameterDeclaration(IASTParameterDeclaration... decls) {
		for(IASTParameterDeclaration d : decls)
			addParameterDeclaration(d);
	}

	public void addParameterDeclaration(IASTParameterDeclaration d) {
        assertNotFrozen();
    	if (d != null) {
    		paramDecls = (IASTParameterDeclaration[]) ArrayUtil.append(IASTParameterDeclaration.class, paramDecls, ++declPos, d);
    		d.setParent(this);
			d.setPropertyInParent(SUBDECLARATION);
    	}
    }

	@Override
	protected void beforeResolution() {
		// populate containing scope, so that it will not be affected by the alternative branches.
		IScope scope= CVisitor.getContainingScope(this);
		if (scope instanceof IASTInternalScope) {
			((IASTInternalScope) scope).populateCache();
		}
	}

    public IASTParameterDeclaration[] getParameterDeclarations() {
    	paramDecls = (IASTParameterDeclaration[]) ArrayUtil.removeNullsAfter(IASTParameterDeclaration.class, paramDecls, declPos ); 
        return paramDecls;
    }

    @Override
	public IASTNode[] getNodes() {
        return getParameterDeclarations();
    }

	public IASTDeclSpecifier getDeclSpecifier() {
		return paramDecls[0].getDeclSpecifier();
	}

	public IASTDeclarator getDeclarator() {
		return paramDecls[0].getDeclarator();
	}

	public void setDeclSpecifier(IASTDeclSpecifier declSpec) {
        assertNotFrozen();
		Assert.isLegal(false);
	}

	public void setDeclarator(IASTDeclarator declarator) {
        assertNotFrozen();
		Assert.isLegal(false);
	}

	public IASTParameterDeclaration copy() {
		throw new UnsupportedOperationException();
	}
	
	public IASTParameterDeclaration copy(CopyStyle style) {
		throw new UnsupportedOperationException();
	}
}
