/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

/**
 * @author jcamelon
 */
public class CASTCompositeTypeSpecifier extends CASTBaseDeclSpecifier implements
        ICASTCompositeTypeSpecifier {

    private int key;
    private IASTName name;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier#getKey()
     */
    public int getKey() {
        return key;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier#setKey(int)
     */
    public void setKey(int key) {
        this.key = key;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier#getName()
     */
    public IASTName getName() {
        return name;
    }
    
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier#setName(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public void setName(IASTName name) {
        this.name = name;
    }

    
    private IASTDeclaration [] declarations = null;
    private IScope scope = null;
    

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier#getMembers()
     */
    public IASTDeclaration [] getMembers() {
        if( declarations == null ) return IASTDeclaration.EMPTY_DECLARATION_ARRAY;
        return (IASTDeclaration[]) ArrayUtil.removeNulls( IASTDeclaration.class, declarations );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier#addMemberDeclaration(org.eclipse.cdt.core.dom.ast.IASTDeclaration)
     */
    public void addMemberDeclaration(IASTDeclaration declaration) {
        declarations = (IASTDeclaration[]) ArrayUtil.append( IASTDeclaration.class, declarations, declaration );
    }
    

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier#getScope()
     */
    public IScope getScope() {
        if( scope == null )
            scope = new CCompositeTypeScope( this );
        return scope;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier#getRawSignature()
     */
    public String getRawSignature() {
       return getName().toString() == null ? "" : getName().toString(); //$NON-NLS-1$
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
           
        IASTDeclaration [] decls = getMembers();
        for( int i = 0; i < decls.length; i++ )
            if( !decls[i].accept( action ) ) return false;
            
        return true;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTNameOwner#getRoleForName(org.eclipse.cdt.core.dom.ast.IASTName)
	 */
	public int getRoleForName(IASTName n) {
		if( n == this.name )
			return r_definition;
		return r_unclear;
	}
}
