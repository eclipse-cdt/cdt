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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

/**
 * @author jcamelon
 */
public class CPPASTLinkageSpecification extends CPPASTNode implements
        ICPPASTLinkageSpecification {

    private String literal;
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification#getLiteral()
     */
    public String getLiteral() {
        return literal;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification#setLiteral(java.lang.String)
     */
    public void setLiteral(String value) {
        this.literal = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification#getDeclarations()
     */
    public IASTDeclaration [] getDeclarations() {
        if( declarations == null ) return IASTDeclaration.EMPTY_DECLARATION_ARRAY;
        return (IASTDeclaration[]) ArrayUtil.removeNulls( IASTDeclaration.class, declarations );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification#addDeclaration(org.eclipse.cdt.core.dom.ast.IASTDeclaration)
     */
    public void addDeclaration(IASTDeclaration declaration) {
        declarations = (IASTDeclaration[]) ArrayUtil.append( IASTDeclaration.class, declarations, declaration );
    }

    private IASTDeclaration [] declarations = new IASTDeclaration[4];

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitDeclarations ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        
        IASTDeclaration [] decls = getDeclarations();
        for( int i = 0; i < decls.length; i++ )
            if( !decls[i].accept( action ) ) return false;
        return true;
    }

}
