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

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

/**
 * @author jcamelon
 */
public class CASTIfStatement extends CASTNode implements IASTIfStatement {

    private IASTExpression condition;
    private IASTStatement thenClause;
    private IASTStatement elseClause;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTIfStatement#getCondition()
     */
    public IASTExpression getCondition() {
        return condition;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTIfStatement#setCondition(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setCondition(IASTExpression condition) {
        this.condition = condition;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTIfStatement#getThenClause()
     */
    public IASTStatement getThenClause() {
        return thenClause;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTIfStatement#setThenClause(org.eclipse.cdt.core.dom.ast.IASTStatement)
     */
    public void setThenClause(IASTStatement thenClause) {
        this.thenClause = thenClause;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTIfStatement#getElseClause()
     */
    public IASTStatement getElseClause() {
        return elseClause;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTIfStatement#setElseClause(org.eclipse.cdt.core.dom.ast.IASTStatement)
     */
    public void setElseClause(IASTStatement elseClause) {
        this.elseClause = elseClause;
    }

}
