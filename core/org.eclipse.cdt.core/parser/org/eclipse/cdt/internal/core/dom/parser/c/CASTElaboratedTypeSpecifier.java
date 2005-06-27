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

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.c.ICASTElaboratedTypeSpecifier;

/**
 * @author jcamelon
 */
public class CASTElaboratedTypeSpecifier extends CASTBaseDeclSpecifier implements
        ICASTElaboratedTypeSpecifier {

    private int kind;
    private IASTName name;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier#getKind()
     */
    public int getKind() {
        return kind;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier#setKind(int)
     */
    public void setKind(int value) {
        this.kind = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier#getName()
     */
    public IASTName getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier#setName(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public void setName(IASTName name) {
        this.name = name;
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitDeclSpecifiers ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        if( name != null ) if( !name.accept( action ) ) return false;
        return true;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTNameOwner#getRoleForName(org.eclipse.cdt.core.dom.ast.IASTName)
	 */
	public int getRoleForName(IASTName n ) {
		if( n != name ) return r_unclear;
		
		IASTNode parent = getParent();
		if( !( parent instanceof IASTDeclaration ) )
			return r_reference;
		
		if( parent instanceof IASTSimpleDeclaration ){
			IASTDeclarator [] dtors = ((IASTSimpleDeclaration)parent).getDeclarators(); 
			if( dtors.length == 0 )
				return r_declaration;
		}
		
		//can't tell, resolve the binding
		IBinding binding = name.resolveBinding();
		if( binding instanceof ICInternalBinding ){
			IASTNode node = ((ICInternalBinding)binding).getPhysicalNode();
			if( node == name ) 
				return r_declaration;
		}
		return r_reference;
	}
}
