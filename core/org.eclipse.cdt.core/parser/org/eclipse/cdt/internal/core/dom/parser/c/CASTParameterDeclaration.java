/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;

/**
 * @author jcamelon
 */
public class CASTParameterDeclaration extends CASTNode implements
        IASTParameterDeclaration {

    private IASTDeclSpecifier declSpec;
    private IASTDeclarator declarator;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration#getDeclSpecifier()
     */
    public IASTDeclSpecifier getDeclSpecifier() {
        return declSpec;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration#getDeclarator()
     */
    public IASTDeclarator getDeclarator() {
        return declarator;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration#setDeclSpecifier(org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier)
     */
    public void setDeclSpecifier(IASTDeclSpecifier declSpec) {
        this.declSpec = declSpec;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration#setDeclarator(org.eclipse.cdt.core.dom.ast.IASTDeclarator)
     */
    public void setDeclarator(IASTDeclarator declarator) {
        this.declarator = declarator;
    }

}
