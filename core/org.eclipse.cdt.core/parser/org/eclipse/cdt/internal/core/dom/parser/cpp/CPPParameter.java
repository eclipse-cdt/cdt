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
 * Created on Dec 10, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;

/**
 * @author aniefer
 */
public class CPPParameter implements IParameter {
	private IType type = null;
	private IASTName [] declarations = null;
	
	
	public CPPParameter( IASTName name ){
		this.declarations = new IASTName [] { name };
	}
	
	public CPPParameter( IType type ){
	    this.type = type;
	}
	
	public void addDeclaration( IASTName name ){
		if( declarations == null ){
		    declarations = new IASTName [] { name };
			return;
		}
		for( int i = 0; i < declarations.length; i++ ){
			if( declarations[i] == null ){
				declarations[i] = name;
				return;
			}
		}
		IASTName [] tmp = new IASTName[ declarations.length * 2 ];
		System.arraycopy( declarations, 0, tmp, 0, declarations.length );
		tmp[ declarations.length ] = name;
		declarations = tmp;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
	    if( declarations != null )
	        return declarations[0].toString();
	    return CPPSemantics.EMPTY_NAME;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
	 */
	public char[] getNameCharArray() {
	    if( declarations != null )
	        return declarations[0].toCharArray();
	    return CPPSemantics.EMPTY_NAME_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getPhysicalNode()
	 */
	public IASTNode getPhysicalNode() {
	    if( declarations != null )
	        return declarations[0];
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IVariable#getType()
	 */
	public IType getType() {
		if( type == null && declarations != null ){
			type = CPPVisitor.createType( (IASTDeclarator) declarations[0].getParent() );
		}
		return type;
	}

}
