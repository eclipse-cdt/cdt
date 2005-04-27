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
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * @author aniefer
 */
public class CPPParameter implements ICPPParameter, ICPPInternalBinding {
    public static class CPPParameterDelegate extends CPPDelegate implements ICPPParameter {
        public CPPParameterDelegate( IASTName name, IParameter binding ) {
            super( name, binding );
        }
        public IType getType() throws DOMException {
            return ((IParameter)getBinding()).getType();
        }
        public boolean isStatic() throws DOMException {
            return ((IParameter)getBinding()).isStatic();
        }
        public boolean isExtern() {
            return false;
        }
        public boolean isAuto() throws DOMException {
            return ((IParameter)getBinding()).isAuto();
        }
        public boolean isRegister() throws DOMException {
            return ((IParameter)getBinding()).isRegister();
        }
        public boolean isMutable() {
            return false;
        }
		public IASTInitializer getDefaultValue() {
			return ((ICPPParameter)getBinding()).getDefaultValue();
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
				}
			}
		}
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
		addDeclaration( node );
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IVariable#isExtern()
     */
    public boolean isExtern() {
        //7.1.1-5 extern can not be used in the declaration of a parameter
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable#isMutable()
     */
    public boolean isMutable() {
        //7.1.1-8 mutable can only apply to class members
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IVariable#isAuto()
     */
    public boolean isAuto() {
        return hasStorageClass( IASTDeclSpecifier.sc_auto );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IVariable#isRegister()
     */
    public boolean isRegister() {
        return hasStorageClass( IASTDeclSpecifier.sc_register );
    }
    
    public boolean hasStorageClass( int storage ){
	    IASTNode[] ns = getDeclarations();
        if( ns == null )
            return false;
        
        for( int i = 0; i < ns.length && ns[i] != null; i++ ){
            IASTNode parent = ns[i].getParent();
            while( !(parent instanceof IASTDeclaration) )
                parent = parent.getParent();
            
            if( parent instanceof IASTSimpleDeclaration ){
                IASTDeclSpecifier declSpec = ((IASTSimpleDeclaration)parent).getDeclSpecifier();
                if( declSpec.getStorageClass() == storage )
                    return true;
            }
        }
        return false;
	}

	public IASTInitializer getDefaultValue() {
		if( declarations == null )
			return null;
		for (int i = 0; i < declarations.length && declarations[i] != null; i++) {
			IASTNode parent = declarations[i].getParent();
			while( parent.getPropertyInParent() == IASTDeclarator.NESTED_DECLARATOR )
				parent = parent.getParent();
			IASTInitializer init = ((IASTDeclarator)parent).getInitializer();
			if( init != null )
				return init;
		}
		return null;
	}
}
