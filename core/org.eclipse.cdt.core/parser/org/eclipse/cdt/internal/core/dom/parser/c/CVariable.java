
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

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;

/**
 * Created on Nov 5, 2004
 * @author aniefer
 */
public class CVariable implements IVariable, ICBinding {
    public static class CVariableProblem extends ProblemBinding implements IVariable {
        public CVariableProblem( int id, char[] arg ) {
            super( id, arg );
        }

        public IType getType() throws DOMException {
            throw new DOMException( this );
        }
        
    }
	final IASTName name;
	private IType type = null;
	
	public CVariable( IASTName name ){
		this.name = name;
	}
    public IASTNode getPhysicalNode(){
        return name;
    }	

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IVariable#getType()
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
	public char[]getNameCharArray(){
	    return ((CASTName)name).toCharArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
		IASTDeclarator declarator = (IASTDeclarator) name.getParent();
		return CVisitor.getContainingScope( declarator.getParent() );
	}
}
