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

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;

/**
 * @author jcamelon
 */
public class CPPASTSwitchStatement extends CPPASTNode implements
        IASTSwitchStatement {

    private IASTExpression controller;
    private IASTStatement body;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTSwitchStatement#getController()
     */
    public IASTExpression getController() {
        return controller;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTSwitchStatement#setController(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setController(IASTExpression controller) {
        this.controller = controller;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTSwitchStatement#getBody()
     */
    public IASTStatement getBody() {
        return body;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTSwitchStatement#setBody(org.eclipse.cdt.core.dom.ast.IASTStatement)
     */
    public void setBody(IASTStatement body) {
        this.body = body;
    }

}
