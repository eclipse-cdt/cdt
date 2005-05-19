/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CPPASTCompositeTypeSpecifier extends CPPASTBaseDeclSpecifier
        implements ICPPASTCompositeTypeSpecifier, IASTAmbiguityParent {

    private int k;
    private IASTName n;
    private IScope scope;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier#getRawSignature()
     */
    public String getRawSignature() {
       return getName().toString() == null ? "" : getName().toString(); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier#getBaseSpecifiers()
     */
    public ICPPASTBaseSpecifier[] getBaseSpecifiers() {
        if( baseSpecs == null ) return ICPPASTBaseSpecifier.EMPTY_BASESPECIFIER_ARRAY;
        return (ICPPASTBaseSpecifier[]) ArrayUtil.removeNulls( ICPPASTBaseSpecifier.class, baseSpecs );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier#addBaseSpecifier(org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier)
     */
    public void addBaseSpecifier(ICPPASTBaseSpecifier baseSpec) {
        baseSpecs = (ICPPASTBaseSpecifier[]) ArrayUtil.append( ICPPASTBaseSpecifier.class, baseSpecs, baseSpec );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier#getKey()
     */
    public int getKey() {
        return k;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier#setKey(int)
     */
    public void setKey(int key) {
        k = key;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier#getName()
     */
    public IASTName getName() {
        return n;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier#setName(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public void setName(IASTName name) {
        this.n = name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier#getMembers()
     */
    public IASTDeclaration[] getMembers() {
        if( declarations == null ) return IASTDeclaration.EMPTY_DECLARATION_ARRAY;
        return (IASTDeclaration[]) ArrayUtil.removeNulls( IASTDeclaration.class, declarations );

    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier#addMemberDeclaration(org.eclipse.cdt.core.dom.ast.IASTDeclaration)
     */
    public void addMemberDeclaration(IASTDeclaration declaration) {
        declarations = (IASTDeclaration[]) ArrayUtil.append( IASTDeclaration.class, declarations, declaration );
    }


    private IASTDeclaration [] declarations = new IASTDeclaration[4];
    private ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier [] baseSpecs = null;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier#getScope()
     */
    public IScope getScope() {
    	if( scope == null )
    		scope = new CPPClassScope( this );
    	
        return scope;
    }
    
    public void setScope( IScope scope ){
        this.scope = scope;
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitDeclSpecifiers ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        if( n != null ) if( !n.accept( action ) ) return false;
        ICPPASTBaseSpecifier[] bases = getBaseSpecifiers();
        for( int i = 0; i < bases.length; i++ )   
            if( !bases[i].accept( action ) ) return false;
           
        IASTDeclaration [] decls = getMembers();
        for( int i = 0; i < decls.length; i++ )
            if( !decls[i].accept( action ) ) return false;
            
        return true;
    }
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTNameOwner#getRoleForName(org.eclipse.cdt.core.dom.ast.IASTName)
	 */
	public int getRoleForName(IASTName name) {
		if( name == this.n )
			return r_definition;
		return r_unclear;
	}

    public void replace(IASTNode child, IASTNode other) {
        if( declarations == null ) return;
        for( int i = 0; i < declarations.length; ++i )
        {
           if( declarations[i] == null ) continue;
           if( declarations[i] == child )
           {
               other.setParent( child.getParent() );
               other.setPropertyInParent( child.getPropertyInParent() );
               declarations[i] = (IASTDeclaration) other;
           }
        }
    }
}
