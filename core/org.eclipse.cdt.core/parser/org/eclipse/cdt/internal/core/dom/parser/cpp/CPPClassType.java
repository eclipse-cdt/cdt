/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Nov 29, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;

/**
 * @author aniefer
 */
public class CPPClassType implements ICPPClassType {
	private ICPPASTCompositeTypeSpecifier definition;
	private ICPPASTElaboratedTypeSpecifier [] declarations;
	
	public CPPClassType( IASTDeclSpecifier declSpec ){
		if( declSpec instanceof ICPPASTCompositeTypeSpecifier )
			definition = (ICPPASTCompositeTypeSpecifier) declSpec;
		else 
			declarations = new ICPPASTElaboratedTypeSpecifier[] { (ICPPASTElaboratedTypeSpecifier) declSpec };
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getFields()
	 */
	public List getFields() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#findField(java.lang.String)
	 */
	public IField findField(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		return ( definition != null ) ? definition.getName().toString() : declarations[0].getName().toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
	 */
	public char[] getNameCharArray() {
		return ( definition != null ) ? definition.getName().toCharArray() : declarations[0].getName().toCharArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
		return (definition != null ) ? CPPVisitor.getContainingScope( definition ) : CPPVisitor.getContainingScope( declarations[0] );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getCompositeScope()
	 */
	public IScope getCompositeScope() {
		return (definition != null ) ? definition.getScope() : null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getPhysicalNode()
	 */
	public IASTNode getPhysicalNode() {
		return (definition != null ) ? (IASTNode) definition : declarations[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getKey()
	 */
	public int getKey() {
		return (definition != null ) ? definition.getKey() : declarations[0].getKind();
	}

	public void addDefinition( ICPPASTCompositeTypeSpecifier compSpec ){
		definition = compSpec;
	}
	public void addDeclaration( ICPPASTElaboratedTypeSpecifier elabSpec ) {
		if( declarations == null ){
			declarations = new ICPPASTElaboratedTypeSpecifier [] { elabSpec };
			return;
		}

        for( int i = 0; i < declarations.length; i++ ){
            if( declarations[i] == null ){
                declarations[i] = elabSpec;
                return;
            }
        }
        ICPPASTElaboratedTypeSpecifier tmp [] = new ICPPASTElaboratedTypeSpecifier[ declarations.length * 2 ];
        System.arraycopy( declarations, 0, tmp, 0, declarations.length );
        tmp[ declarations.length ] = elabSpec;
        declarations = tmp;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getBases()
	 */
	public ICPPBase [] getBases() {
		// TODO Auto-generated method stub
		return ICPPBase.EMPTY_BASE_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getDeclaredFields()
	 */
	public List getDeclaredFields() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getMethods()
	 */
	public List getMethods() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getAllDeclaredMethods()
	 */
	public List getAllDeclaredMethods() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getDeclaredMethods()
	 */
	public List getDeclaredMethods() {
		// TODO Auto-generated method stub
		return null;
	}
}
