/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.internal.core.parser2.c;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * @author jcamelon
 */
class CASTNode implements IASTNode {

    
    private int length;
    private int offset;
    private IASTNode parent;
    private ASTNodeProperty property;

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    public IASTNode getParent() {
        return parent;
    }

    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTNode#getPropertyInParent()
     */
    public ASTNodeProperty getPropertyInParent() {
        return property;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTNode#setOffset(int)
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTNode#setLength()
     */
    public void setLength( int length ) {
        this.length = length;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTNode#setParent(org.eclipse.cdt.core.dom.ast.IASTNode)
     */
    public void setParent(IASTNode parent) {
        this.parent = parent;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTNode#setPropertyInParent(org.eclipse.cdt.core.dom.ast.IASTNodeProperty)
     */
    public void setPropertyInParent(ASTNodeProperty property) {
        this.property = property;
    }

}
