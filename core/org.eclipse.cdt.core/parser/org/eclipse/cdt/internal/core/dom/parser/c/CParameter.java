
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
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;

/**
 * Created on Nov 5, 2004
 * @author aniefer
 */
public class CParameter implements IParameter {
    public static class CParameterProblem extends ProblemBinding implements IParameter {
        public CParameterProblem( int id, char[] arg ) {
            super( id, arg );
        }

        public IType getType() throws DOMException {
            throw new DOMException( this );
        }

        public boolean isStatic() throws DOMException {
            throw new DOMException( this );
        }
    }
    
	private IASTName [] declarations;
	
	public CParameter( IASTName parameterName ){
		this.declarations = new IASTName [] { parameterName };
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IVariable#getType()
	 */
	
    public IType getType() {
        return CVisitor.createType( declarations[0] );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		return declarations[0].toString();
	}
	public char[] getNameCharArray(){
	    return declarations[0].toCharArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
	    //IASTParameterDeclaration or IASTSimpleDeclaration
	    for( int i = 0; i < declarations.length; i++ ){
	        IASTNode parent = declarations[i].getParent();
	        if( parent instanceof ICASTKnRFunctionDeclarator ){
	            parent = parent.getParent();
	            return ((IASTCompoundStatement)((IASTFunctionDefinition)parent).getBody()).getScope();
	        }
	        
	        IASTFunctionDeclarator fdtor = (IASTFunctionDeclarator) parent.getParent().getParent();
			parent = fdtor.getParent();
			if( parent instanceof IASTFunctionDefinition ) {
				return ((IASTCompoundStatement)((IASTFunctionDefinition)parent).getBody()).getScope();
			}
	    }
		//TODO: if not definition, find definition
		return null;
	}

    /**
     * @param name
     */
    public void addDeclaration( CASTName name ) {
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
     * @see org.eclipse.cdt.core.dom.ast.IVariable#isStatic()
     */
    public boolean isStatic(){
        return false;
    }

}
