
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
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.c.ICASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;

/**
 * Created on Nov 8, 2004
 * @author aniefer
 */
public class CTypeDef implements ITypedef {
	private final IASTName name; 
	
	public CTypeDef( IASTName name ){
		this.name = name;
	}
	
    public IASTNode getPhysicalNode(){
        return name;
    }
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ITypedef#getType()
	 */
	public IType getType() {
		IASTDeclarator declarator = (IASTDeclarator) name.getParent();
		IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) declarator.getParent();
		
		IASTDeclSpecifier declSpec = declaration.getDeclSpecifier();
		if( declSpec instanceof ICASTTypedefNameSpecifier ){
			IType lastType = null;
			ICASTTypedefNameSpecifier nameSpec = (ICASTTypedefNameSpecifier) declSpec;
			lastType = (IType) nameSpec.getName().resolveBinding();			
			
			IType pointerChain = setupPointerChain(declarator.getPointerOperators(), lastType);

			if (pointerChain != null) return pointerChain;
			
			return lastType;
		} else if( declSpec instanceof IASTElaboratedTypeSpecifier ){
			IType lastType = null;
			IASTElaboratedTypeSpecifier elabTypeSpec = (IASTElaboratedTypeSpecifier) declSpec;
			lastType = (IType) elabTypeSpec.getName().resolveBinding();
			
			IType pointerChain = setupPointerChain(declarator.getPointerOperators(), lastType);

			if (pointerChain != null) return pointerChain;
			
			return lastType;
		} else if( declSpec instanceof IASTCompositeTypeSpecifier ){
			IType lastType = null;
			IASTCompositeTypeSpecifier compTypeSpec = (IASTCompositeTypeSpecifier) declSpec;
			lastType = (IType) compTypeSpec.getName().resolveBinding();
			
			IType pointerChain = setupPointerChain(declarator.getPointerOperators(), lastType);

			if (pointerChain != null) return pointerChain;
			
			return lastType;
		} else if (declSpec instanceof ICASTSimpleDeclSpecifier) {
			IType lastType = null;
			if (declSpec.isConst() || declSpec.isVolatile() || ((ICASTSimpleDeclSpecifier)declSpec).isRestrict())
				lastType = new CQualifierType((ICASTDeclSpecifier)declSpec);
			else						
				lastType = new CBasicType((ICASTSimpleDeclSpecifier)declSpec);
			
			IType pointerChain = setupPointerChain(declarator.getPointerOperators(), lastType);

			if (pointerChain != null) return pointerChain;

			return lastType;
		}
		return null;
	}

	private IType setupPointerChain(IASTPointerOperator[] ptrs, IType lastType) {
		CPointerType pointerType = null;
		
		if ( ptrs != null && ptrs.length > 0 ) {
			pointerType = new CPointerType();
											
			if (ptrs.length == 1) {
				pointerType.setType(lastType);
				pointerType.setPointer((ICASTPointer)ptrs[0]);
			} else {
				CPointerType tempType = new CPointerType();
				pointerType.setType(tempType);
				pointerType.setPointer((ICASTPointer)ptrs[0]);
				for (int i=1; i<ptrs.length - 1; i++) {
					tempType.setType(new CPointerType());
					tempType.setPointer((ICASTPointer)ptrs[i]);
					tempType = (CPointerType)tempType.getType();
				}					
				tempType.setType(lastType);
			}
			
			return pointerType;
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		return name.toString();
	}
	public char[] getNameCharArray(){
	    return ((CASTName) name).toCharArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
		IASTDeclarator declarator = (IASTDeclarator) name.getParent();
		return CVisitor.getContainingScope( (IASTDeclaration) declarator.getParent() );
	}

}
