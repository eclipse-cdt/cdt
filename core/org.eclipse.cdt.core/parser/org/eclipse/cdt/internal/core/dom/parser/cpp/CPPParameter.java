/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
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

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;

/**
 * @author aniefer
 */
public class CPPParameter implements IParameter, ICPPInternalBinding, ICPPVariable {
    public static class CPPParameterDelegate extends CPPDelegate implements IParameter, ICPPVariable {
        public CPPParameterDelegate( IASTName name, IParameter binding ) {
            super( name, binding );
        }
        public IType getType() throws DOMException {
            return ((IParameter)getBinding()).getType();
        }
        public boolean isStatic() throws DOMException {
            return ((IParameter)getBinding()).isStatic();
        }
    }
    
	private IType type = null;
	private IASTName [] declarations = null;
	
	
	public CPPParameter( IASTName name ){
		this.declarations = new IASTName [] { name };
	}
	
	public CPPParameter( IType type ){
	    this.type = type;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPBinding#getDeclarations()
     */
    public IASTNode[] getDeclarations() {
        return declarations;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPBinding#getDefinition()
     */
    public IASTNode getDefinition() {
        return null;
    }

	public void addDeclaration( IASTNode node ){
		if( !(node instanceof IASTName ) )
			return;
		IASTName name = (IASTName) node;
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
	
	private IASTName getPrimaryDeclaration(){
	    if( declarations != null ){
	        for( int i = 0; i < declarations.length && declarations[i] != null; i++ ){
	            IASTNode node = declarations[i].getParent();
	            while( !(node instanceof IASTDeclaration) )
	                node = node.getParent();
	            
	            if( node instanceof IASTFunctionDefinition )
	                return declarations[i];
	        }
	        return declarations[0];
	    }
	    return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
	    IASTName name = getPrimaryDeclaration();
	    if( name != null )
	        return name.toString();
	    return CPPSemantics.EMPTY_NAME;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
	 */
	public char[] getNameCharArray() {
	    IASTName name = getPrimaryDeclaration();
	    if( name != null )
	        return name.toCharArray();
	    return CPPSemantics.EMPTY_NAME_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
		return CPPVisitor.getContainingScope( getPrimaryDeclaration() );
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

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IVariable#isStatic()
     */
    public boolean isStatic() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getFullyQualifiedName()
     */
    public String[] getQualifiedName() {
        return new String [] { getName() };
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getFullyQualifiedNameCharArray()
     */
    public char[][] getQualifiedNameCharArray() {
        return new char[][]{ getNameCharArray() };
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#isGloballyQualified()
     */
    public boolean isGloballyQualified() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#createDelegate(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public ICPPDelegate createDelegate( IASTName name ) {
        return new CPPParameterDelegate( name, this );
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#addDefinition(org.eclipse.cdt.core.dom.ast.IASTNode)
	 */
	public void addDefinition(IASTNode node) {
		// TODO Auto-generated method stub
		
	}
}
