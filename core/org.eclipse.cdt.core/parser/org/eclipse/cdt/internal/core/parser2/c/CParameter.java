
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
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;

/**
 * Created on Nov 5, 2004
 * @author aniefer
 */
public class CParameter implements IParameter {
	final private IASTParameterDeclaration parameterDeclaration;
	
	public CParameter( IASTParameterDeclaration parameterDeclaration ){
		//parameterDeclaration = checkForDefinition( parameterDeclaration );
		this.parameterDeclaration = parameterDeclaration;
	}

	public IASTNode getPhysicalNode(){
	    return parameterDeclaration;
	}
//	private IASTParameterDeclaration checkForDefinition( IASTParameterDeclaration paramDecl ){
//		IASTFunctionDeclarator fnDtor = (IASTFunctionDeclarator) paramDecl.getParent();
//		if( fnDtor.getParent() instanceof IASTFunctionDefinition  )
//			return paramDecl;
//		
//		IASTFunctionDeclarator fDef = CVisitor.findDefinition( fnDtor );
//		if( fDef != null && fDef instanceof IASTFunctionDefinition ){
//			int index = fnDtor.getParameters().indexOf( paramDecl );
//			if( index >= 0 && index < fDef.getParameters().size() ) {
//				IASTParameterDeclaration pDef = (IASTParameterDeclaration) fDef.getParameters().get( index );
//				((CASTName)pDef.getDeclarator().getName()).setBinding( this );
//				paramDecl = pDef;
//			}
//		}
//		return paramDecl;
//	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IVariable#getType()
	 */
	
    public IType getType() {
		IASTDeclSpecifier declSpec = parameterDeclaration.getDeclSpecifier();
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
		return parameterDeclaration.getDeclarator().getName().toString();
	}
	public char[] getNameCharArray(){
	    return ((CASTName)parameterDeclaration.getDeclarator().getName()).toCharArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
		return CVisitor.getContainingScope( parameterDeclaration );
	}

}
