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
import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;

/**
 * @author jcamelon
 */
public class CPPASTASMDeclaration extends CPPASTNode implements
        IASTASMDeclaration {
    char [] assembly = null;
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTASMDeclaration#getAssembly()
     */
    public String getAssembly() {
        if( assembly == null ) return ""; //$NON-NLS-1$
        return new String( assembly );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTASMDeclaration#setAssembly(java.lang.String)
     */
    public void setAssembly(String assembly) {
        this.assembly = assembly.toCharArray();
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitDeclarations ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }

}
