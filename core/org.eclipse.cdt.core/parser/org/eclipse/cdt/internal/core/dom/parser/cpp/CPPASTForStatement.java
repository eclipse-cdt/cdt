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

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;

/**
 * @author jcamelon
 */
public class CPPASTForStatement extends CPPASTNode implements IASTForStatement {

    private IASTExpression initialExpression;
    private IASTDeclaration initDeclaration;
    private IASTExpression condition;
    private IASTExpression iterationExpression;
    private IASTStatement body;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTForStatement#getInitExpression()
     */
    public IASTExpression getInitExpression() {
        return initialExpression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTForStatement#setInit(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setInit(IASTExpression expression) {
        this.initialExpression = expression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTForStatement#getInitDeclaration()
     */
    public IASTDeclaration getInitDeclaration() {
        return initDeclaration;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTForStatement#setInit(org.eclipse.cdt.core.dom.ast.IASTDeclaration)
     */
    public void setInit(IASTDeclaration declaration) {
        this.initDeclaration = declaration;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTForStatement#getCondition()
     */
    public IASTExpression getCondition() {
        return condition;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTForStatement#setCondition(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setCondition(IASTExpression condition) {
        this.condition = condition;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTForStatement#getIterationExpression()
     */
    public IASTExpression getIterationExpression() {
        return iterationExpression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTForStatement#setIterationExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setIterationExpression(IASTExpression iterator) {
        this.iterationExpression = iterator;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTForStatement#getBody()
     */
    public IASTStatement getBody() {
        return body;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTForStatement#setBody(org.eclipse.cdt.core.dom.ast.IASTStatement)
     */
    public void setBody(IASTStatement statement) {
        body = statement;

    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTForStatement#getScope()
     */
    public IScope getScope() {
        // TODO Auto-generated method stub
        return null;
    }

}
