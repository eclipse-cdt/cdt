/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * @author jcamelon
 */
public abstract class CPPASTNode extends ASTNode implements IASTNode {

    private IASTNode parent;
    private ASTNodeProperty property;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTNode#getTranslationUnit()
     */
    public IASTTranslationUnit getTranslationUnit() {
        if( this instanceof IASTTranslationUnit ) return (IASTTranslationUnit) this;
        IASTNode node = getParent();
        while(node != null && !(node instanceof IASTTranslationUnit))
            node = node.getParent();
		
        return (IASTTranslationUnit) node;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTNode#getParent()
     */
    public IASTNode getParent() {
        return parent;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTNode#setParent(org.eclipse.cdt.core.dom.ast.IASTNode)
     */
    public void setParent(IASTNode node) {
        this.parent = node;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTNode#getPropertyInParent()
     */
    public ASTNodeProperty getPropertyInParent() {
        return property;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTNode#setPropertyInParent(org.eclipse.cdt.core.dom.ast.ASTNodeProperty)
     */
    public void setPropertyInParent(ASTNodeProperty property) {
        this.property = property;
    }
    
}
