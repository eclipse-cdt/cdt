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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;

/**
 * @author jcamelon
 */
public class CASTSimpleDeclaration extends CASTNode implements
        IASTSimpleDeclaration {

    private List declarators = Collections.EMPTY_LIST;
    private static final int DEFAULT_DECLARATORS_LIST_SIZE = 4;
    private IASTDeclSpecifier declSpecifier;

    /**
     * @param parent
     * @param location
     * @param offset
     */
    CASTSimpleDeclaration(IASTNode parent, IASTNodeLocation location, int offset) {
        super(parent, location, offset);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration#getDeclSpecifier()
     */
    public IASTDeclSpecifier getDeclSpecifier() {
        return declSpecifier;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration#getDeclarators()
     */
    public List getDeclarators() {
        return declarators;
    }
    
    void addDeclarator( IASTDeclarator d )
    {
        if( declarators == Collections.EMPTY_LIST )
            declarators = new ArrayList( DEFAULT_DECLARATORS_LIST_SIZE );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTNode#getPropertyInParent()
     */
    public IASTNodeProperty getPropertyInParent() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param declSpecifier The declSpecifier to set.
     */
    public void setDeclSpecifier(IASTDeclSpecifier declSpecifier) {
        this.declSpecifier = declSpecifier;
    }
}
