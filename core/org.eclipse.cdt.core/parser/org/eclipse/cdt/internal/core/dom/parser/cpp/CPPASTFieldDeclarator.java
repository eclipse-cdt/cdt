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
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CPPASTFieldDeclarator extends CPPASTDeclarator implements
        IASTFieldDeclarator, IASTAmbiguityParent {

    private IASTExpression bitField;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator#getBitFieldSize()
     */
    public IASTExpression getBitFieldSize() {
        return bitField;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator#setBitFieldSize(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setBitFieldSize(IASTExpression size) {
        this.bitField = size;
    }

    protected boolean postAccept( ASTVisitor action ){
        if( bitField != null ) if( !bitField.accept( action ) ) return false;
        
        IASTInitializer initializer = getInitializer();
        if( initializer != null ) if( !initializer.accept( action ) ) return false;
        return true;
    }

    public void replace(IASTNode child, IASTNode other) {
        if( child == bitField )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            bitField  = (IASTExpression) other;
        }
        
    }

}
