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

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;

/**
 * @author jcamelon
 */
public class CPPASTTypeId extends CPPASTNode implements IASTTypeId {

    private IASTDeclSpecifier declSpec;
    private IASTDeclarator absDecl;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTTypeId#getDeclSpecifier()
     */
    public IASTDeclSpecifier getDeclSpecifier() {
        return declSpec;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTTypeId#setDeclSpecifier(org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier)
     */
    public void setDeclSpecifier(IASTDeclSpecifier declSpec) {
        this.declSpec = declSpec;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTTypeId#getAbstractDeclarator()
     */
    public IASTDeclarator getAbstractDeclarator() {
        return absDecl;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTTypeId#setAbstractDeclarator(org.eclipse.cdt.core.dom.ast.IASTDeclarator)
     */
    public void setAbstractDeclarator(IASTDeclarator abstractDeclarator) {
        this.absDecl = abstractDeclarator;
    }

}
