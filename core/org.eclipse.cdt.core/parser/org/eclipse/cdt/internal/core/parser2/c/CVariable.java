
/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation 
 **********************************************************************/
package org.eclipse.cdt.internal.core.parser2.c;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;

/**
 * Created on Nov 5, 2004
 * @author aniefer
 */
public class CVariable implements IVariable {
	final IASTName name;
	
	public CVariable( IASTName name ){
	    name = checkForDefinition( name );
		this.name = name;
	}
	
	private IASTName checkForDefinition( IASTName nm ){
	    IASTDeclarator dtor = (IASTDeclarator) nm.getParent();
	    IASTSimpleDeclaration dcl = (IASTSimpleDeclaration) dtor.getParent();
	    IASTDeclSpecifier declSpec = dcl.getDeclSpecifier();
	    if( declSpec.getStorageClass() == IASTDeclSpecifier.sc_extern ){
	        IASTDeclarator prev = dtor, tmp = CVisitor.findDefinition( dtor, CVisitor.AT_BEGINNING );
	        while( tmp != null && tmp != prev ){
	            CASTName n = (CASTName) tmp.getName();
	            IASTDeclSpecifier spec = ((IASTSimpleDeclaration)tmp.getParent()).getDeclSpecifier();
	            if( spec.getStorageClass() != IASTDeclSpecifier.sc_extern ){
	                nm = n;
	            }
	            n.setBinding( this );
	            prev = tmp;
	            tmp = CVisitor.findDefinition( tmp, CVisitor.AT_NEXT );
	        }
	    }
	    
	    return nm;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IVariable#getType()
	 */
	public IType getType() {
		IASTDeclarator declarator = (IASTDeclarator) name.getParent();
		IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) declarator.getParent();
		IASTDeclSpecifier declSpec = declaration.getDeclSpecifier();
		if( declSpec instanceof ICASTTypedefNameSpecifier ){
			ICASTTypedefNameSpecifier nameSpec = (ICASTTypedefNameSpecifier) declSpec;
			return (IType) nameSpec.getName().resolveBinding();
		} else if( declSpec instanceof IASTElaboratedTypeSpecifier ){
			IASTElaboratedTypeSpecifier elabTypeSpec = (IASTElaboratedTypeSpecifier) declSpec;
			return (IType) elabTypeSpec.getName().resolveBinding();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		return name.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
		IASTDeclarator declarator = (IASTDeclarator) name.getParent();
		return CVisitor.getContainingScope( (IASTDeclaration) declarator.getParent() );
	}
}
