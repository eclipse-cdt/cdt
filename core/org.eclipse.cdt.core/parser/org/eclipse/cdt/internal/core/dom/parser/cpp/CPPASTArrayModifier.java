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

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

/**
 * @author jcamelon
 */
public class CPPASTArrayModifier extends CPPASTNode implements
        IASTArrayModifier {

    private IASTExpression exp;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTArrayModifier#getConstantExpression()
     */
    public IASTExpression getConstantExpression() {
        return exp;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTArrayModifier#setConstantExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setConstantExpression(IASTExpression expression) {
        exp = expression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTNode#getTranslationUnit()
     */
    public IASTTranslationUnit getTranslationUnit() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTNode#getParent()
     */
    public IASTNode getParent() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTNode#setParent(org.eclipse.cdt.core.dom.ast.IASTNode)
     */
    public void setParent(IASTNode node) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTNode#getPropertyInParent()
     */
    public ASTNodeProperty getPropertyInParent() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTNode#setPropertyInParent(org.eclipse.cdt.core.dom.ast.ASTNodeProperty)
     */
    public void setPropertyInParent(ASTNodeProperty property) {
        // TODO Auto-generated method stub

    }

}
