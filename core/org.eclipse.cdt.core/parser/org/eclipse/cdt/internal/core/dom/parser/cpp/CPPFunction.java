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
 * Created on Dec 1, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;

/**
 * @author aniefer
 */
public class CPPFunction implements IFunction {
	protected IASTFunctionDeclarator [] declarations;
	protected IASTFunctionDeclarator definition;
	
	public CPPFunction( ICPPASTFunctionDeclarator declarator ){
		IASTNode parent = declarator.getParent();
		if( parent instanceof IASTFunctionDefinition )
			definition = declarator;
		else
			declarations = new IASTFunctionDeclarator [] { declarator };
	}
	
	public void addDefinition( IASTFunctionDeclarator dtor ){
		definition = dtor;
	}
	public void addDeclaration( IASTFunctionDeclarator dtor ){
		if( declarations == null ){
			declarations = new IASTFunctionDeclarator [] { dtor };
			return;
		}
		for( int i = 0; i < declarations.length; i++ ){
			if( declarations[i] == null ){
				declarations[i] = dtor;
				return;
			}
		}
		IASTFunctionDeclarator [] tmp = new IASTFunctionDeclarator[ declarations.length * 2 ];
		System.arraycopy( declarations, 0, tmp, 0, declarations.length );
		tmp[ declarations.length ] = dtor;
		declarations = tmp;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#getParameters()
	 */
	public List getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IFunction#getFunctionScope()
	 */
	public IScope getFunctionScope() {
		IASTFunctionDefinition def = (IASTFunctionDefinition) definition.getParent();
		return def.getScope();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		return (definition != null ) ? definition.getName().toString() : declarations[0].getName().toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
	 */
	public char[] getNameCharArray() {
		return (definition != null ) ? definition.getName().toCharArray() : declarations[0].getName().toCharArray();	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
		return CPPVisitor.getContainingScope( definition != null ? definition : declarations[0] );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getPhysicalNode()
	 */
	public IASTNode getPhysicalNode() {
		// TODO Auto-generated method stub
		return null;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IFunction#getType()
     */
    public IFunctionType getType() {
        // TODO Auto-generated method stub
        return null;
    }

}
