/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation 
 *******************************************************************************/

package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;

/**
 * Created on Nov 5, 2004
 * @author aniefer
 */
public class CVariable implements IVariable, ICInternalBinding {
    public static class CVariableProblem extends ProblemBinding implements IVariable {
        public CVariableProblem( IASTNode node, int id, char[] arg ) {
            super( node, id, arg );
        }

        public IType getType() throws DOMException {
            throw new DOMException( this );
        }
        public boolean isStatic() throws DOMException {
            throw new DOMException( this );
        }
        public boolean isExtern() throws DOMException {
            throw new DOMException( this );
        }
        public boolean isAuto() throws DOMException {
            throw new DOMException( this );
        }
        public boolean isRegister() throws DOMException {
            throw new DOMException( this );
        }
    }
	private IASTName [] declarations = null;
	private IType type = null;
	
	public CVariable( IASTName name ){
		declarations = new IASTName [] { name };
	}
    public IASTNode getPhysicalNode(){
        return declarations[0];
    }	

    public void addDeclaration( IASTName name ){
        declarations = (IASTName[]) ArrayUtil.append( IASTName.class, declarations, name );
    }
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IVariable#getType()
	 */
	public IType getType() {
		if (type == null && declarations[0].getParent() instanceof IASTDeclarator)
			type = CVisitor.createType((IASTDeclarator)declarations[0].getParent());
		return type;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		return declarations[0].toString();
	}
	public char[]getNameCharArray(){
	    return declarations[0].toCharArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
		IASTDeclarator declarator = (IASTDeclarator) declarations[0].getParent();
		return CVisitor.getContainingScope( declarator.getParent() );
	}
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IVariable#isStatic()
     */
    public boolean isStatic() {
        return hasStorageClass( IASTDeclSpecifier.sc_static );
    }
    
    public boolean hasStorageClass( int storage ){
        if( declarations == null )
            return false;
        for( int i = 0; i < declarations.length && declarations[i] != null; i++ ){
            IASTNode parent = declarations[i].getParent();
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
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IVariable#isExtern()
     */
    public boolean isExtern() {
        return hasStorageClass( IASTDeclSpecifier.sc_extern );
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
}
