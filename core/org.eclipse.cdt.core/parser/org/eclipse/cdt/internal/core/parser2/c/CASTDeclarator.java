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

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;

/**
 * @author jcamelon
 */
public class CASTDeclarator extends CASTNode implements IASTDeclarator {

    private IASTInitializer initializer;
    private IASTName name;
    private IASTDeclarator nestedDeclarator;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclarator#getPointerOperators()
     */
    public List getPointerOperators() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclarator#getNestedDeclarator()
     */
    public IASTDeclarator getNestedDeclarator() {
        return nestedDeclarator;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclarator#getName()
     */
    public IASTName getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclarator#getInitializer()
     */
    public IASTInitializer getInitializer() {
        return initializer;
    }

    /**
     * @param initializer
     */
    public void setInitializer(IASTInitializer initializer) {
        this.initializer = initializer;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclarator#addPointerOperator(org.eclipse.cdt.core.dom.ast.IASTPointerOperator)
     */
    public void addPointerOperator(IASTPointerOperator operator) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclarator#setNestedDeclarator(org.eclipse.cdt.core.dom.ast.IASTDeclarator)
     */
    public void setNestedDeclarator(IASTDeclarator nested) {
        this.nestedDeclarator = nested;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTDeclarator#setName(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public void setName(IASTName name) {
        this.name = name;
    }

}
