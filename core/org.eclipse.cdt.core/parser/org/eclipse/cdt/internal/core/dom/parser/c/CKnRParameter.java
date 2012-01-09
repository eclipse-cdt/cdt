/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Rational Software - Initial API and implementation 
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.core.runtime.PlatformObject;

/**
 * A K&R C parameter.
 */
public class CKnRParameter extends PlatformObject implements IParameter {
	final private IASTDeclaration declaration;
	final private IASTName name;

	public CKnRParameter(IASTDeclaration declaration, IASTName name) {
		this.declaration = declaration;
		this.name = name;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IVariable#getType()
	 */
	@Override
	public IType getType() {
		IASTDeclSpecifier declSpec = null;
		if (declaration instanceof IASTSimpleDeclaration)
			declSpec = ((IASTSimpleDeclaration)declaration).getDeclSpecifier();
			
		if( declSpec != null && declSpec instanceof ICASTTypedefNameSpecifier ){
			ICASTTypedefNameSpecifier nameSpec = (ICASTTypedefNameSpecifier) declSpec;
			return (IType) nameSpec.getName().resolveBinding();
		} else if( declSpec != null && declSpec instanceof IASTElaboratedTypeSpecifier ){
			IASTElaboratedTypeSpecifier elabTypeSpec = (IASTElaboratedTypeSpecifier) declSpec;
			return (IType) elabTypeSpec.getName().resolveBinding();
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	@Override
	public String getName() {
		return name.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
	 */
	@Override
	public char[] getNameCharArray() {
		return name.toCharArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	@Override
	public IScope getScope() {
		return CVisitor.getContainingScope( declaration );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getPhysicalNode()
	 */
	public IASTNode getPhysicalNode() {
		return declaration;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IVariable#isStatic()
     */
    @Override
	public boolean isStatic() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IVariable#isExtern()
     */
    @Override
	public boolean isExtern() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IVariable#isAuto()
     */
    @Override
	public boolean isAuto() {
        if( declaration instanceof IASTSimpleDeclaration )
            return ((IASTSimpleDeclaration)declaration).getDeclSpecifier().getStorageClass() == IASTDeclSpecifier.sc_auto;
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IVariable#isRegister()
     */
    @Override
	public boolean isRegister() {
        if( declaration instanceof IASTSimpleDeclaration )
            return ((IASTSimpleDeclaration)declaration).getDeclSpecifier().getStorageClass() == IASTDeclSpecifier.sc_register;
        return false;
    }
    
	@Override
	public ILinkage getLinkage() {
		return Linkage.C_LINKAGE;
	}
	
	@Override
	public IBinding getOwner() {
		return CVisitor.findEnclosingFunction(declaration);
	}

	@Override
	public IValue getInitialValue() {
		return null;
	}
}
