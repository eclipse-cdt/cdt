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

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IScope;

/**
 * @author jcamelon
 */
public class CASTTranslationUnit extends CASTNode implements IASTTranslationUnit {

    /**
     * @param parent
     * @param location
     * @param offset
     */
    CASTTranslationUnit(IASTNode parent, IASTNodeLocation location, int offset) {
        super(parent, location, offset);
    }

    private List decls = Collections.EMPTY_LIST;
    private static final int DEFAULT_CHILDREN_LIST_SIZE = 8;

    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getDeclarations()
     */
    public List getDeclarations() {
        return decls;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getScope()
     */
    public IScope getScope() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTNode#getPropertyInParent()
     */
    public IASTNodeProperty getPropertyInParent() {
        return null;
    }

    void addDeclaration( IASTDeclaration d )
    {
        if( decls == Collections.EMPTY_LIST )
            decls = new ArrayList( DEFAULT_CHILDREN_LIST_SIZE );
        decls.add( d );
    }
}
