
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

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

/**
 * Created on Nov 8, 2004
 * @author aniefer
 */
public class CTypeDef implements ITypedef, ITypeContainer {
	private final IASTName name; 
	private IType type = null;
	
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
		if (type == null)
			type = CVisitor.createType(name);
		return type;
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
