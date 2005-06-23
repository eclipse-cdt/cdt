/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Dec 11, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

/**
 * @author aniefer
 */
public class CPPTypedef implements ITypedef, ITypeContainer, ICPPInternalBinding {
    public static class CPPTypedefDelegate extends CPPDelegate implements ITypedef, ITypeContainer {
        public CPPTypedefDelegate( IASTName name, ITypedef binding ) {
            super( name, binding );
        }
        public IType getType() throws DOMException {
            return ((ITypedef)getBinding()).getType();
        }
        public Object clone() {
            try {
                return super.clone();
            } catch ( CloneNotSupportedException e ) {
            }
            return null;
        }
        public boolean isSameType( IType type ) {
            return ((ITypedef)getBinding()).isSameType( type );
        }
		public void setType(IType type) {
			((ITypeContainer)getBinding()).setType( type );
		}
    }
	private IASTName [] declarations = null;
	private IType type = null;
	
	/**
	 * @param declarator
	 */
	public CPPTypedef(IASTName name) {
		this.declarations = new IASTName[] { name };
        if (name != null)
            name.setBinding( this );
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
        return declarations[0];
    }

    public boolean isSameType( IType o ){
        if( o == this )
            return true;
	    if( o instanceof ITypedef )
            try {
                IType t = getType();
                if( t != null )
                    return t.isSameType( ((ITypedef)o).getType());
                return false;
            } catch ( DOMException e ) {
                return false;
            }
	        
	    IType t = getType();
	    if( t != null )
	        return t.isSameType( o );
	    return false;
	}
    
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ITypedef#getType()
	 */
	public IType getType() {
	    if( type == null ){
	        type = CPPVisitor.createType( (IASTDeclarator) declarations[0].getParent() );
	    }
		return type;
	}
	
	public void setType( IType t ){
	    type = t;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		return declarations[0].toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
	 */
	public char[] getNameCharArray() {
		return declarations[0].toCharArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
		return CPPVisitor.getContainingScope( declarations[0].getParent() );
	}

    public Object clone(){
        IType t = null;
   		try {
            t = (IType) super.clone();
        } catch ( CloneNotSupportedException e ) {
            //not going to happen
        }
        return t;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getFullyQualifiedName()
     */
    public String[] getQualifiedName() {
        return CPPVisitor.getQualifiedName( this );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getFullyQualifiedNameCharArray()
     */
    public char[][] getQualifiedNameCharArray() {
        return CPPVisitor.getQualifiedNameCharArray( this );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#isGloballyQualified()
     */
    public boolean isGloballyQualified() throws DOMException {
        IScope scope = getScope();
        while( scope != null ){
            if( scope instanceof ICPPBlockScope )
                return false;
            scope = scope.getParent();
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#createDelegate(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public ICPPDelegate createDelegate( IASTName name ) {
        return new CPPTypedefDelegate( name, this );
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#addDefinition(org.eclipse.cdt.core.dom.ast.IASTNode)
	 */
	public void addDefinition(IASTNode node) {
	    addDeclaration( node );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#addDeclaration(org.eclipse.cdt.core.dom.ast.IASTNode)
	 */
	public void addDeclaration(IASTNode node) {
	    if( !(node instanceof IASTName) )
			return;
		IASTName name = (IASTName) node;

		if( declarations == null )
	        declarations = new IASTName[] { name };
	    else {
	        //keep the lowest offset declaration in [0]
			if( declarations.length > 0 && ((ASTNode)node).getOffset() < ((ASTNode)declarations[0]).getOffset() ){
				declarations = (IASTName[]) ArrayUtil.prepend( IASTName.class, declarations, name );
			} else {
				declarations = (IASTName[]) ArrayUtil.append( IASTName.class, declarations, name );
			}
	    }
	}
	
	public void removeDeclaration(IASTNode node) {
		if( declarations != null ) {
			for (int i = 0; i < declarations.length; i++) {
				if( node == declarations[i] ) {
					if( i == declarations.length - 1 )
						declarations[i] = null;
					else
						System.arraycopy( declarations, i + 1, declarations, i, declarations.length - 1 - i );
					return;
				}
			}
		}
	}
}
