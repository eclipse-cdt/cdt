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

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;

/**
 * @author jcamelon
 */
class CASTNode {

    /**
     * 
     */
    CASTNode( IASTNode parent, IASTNodeLocation location, int offset ) {
        this.parent = parent;
        this.location = location;
        this.offset = offset;
    }
    
    private int length;
    private final IASTNodeLocation location;
    private final int offset;
    private final IASTNode parent;

    public IASTNodeLocation getLocation() {
        return location;
    }

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    public IASTNode getParent() {
        return parent;
    }

    /**
     * @param length The length to set.
     */
    void setLength(int length) {
        this.length = length;
    }

}
