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
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypenameExpression;

/**
 * @author jcamelon
 */
public class CPPASTTypenameExpression extends CPPASTNode implements
        ICPPASTTypenameExpression {

    private boolean isTemplate;
    private IASTName name;
    private IASTExpression init;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypenameExpression#setIsTemplate(boolean)
     */
    public void setIsTemplate(boolean templateTokenConsumed) {
        isTemplate = templateTokenConsumed;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypenameExpression#isTemplate()
     */
    public boolean isTemplate() {
        return isTemplate;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypenameExpression#setName(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public void setName(IASTName name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypenameExpression#getName()
     */
    public IASTName getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypenameExpression#setInitialValue(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setInitialValue(IASTExpression expressionList) {
        init = expressionList;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypenameExpression#getInitialValue()
     */
    public IASTExpression getInitialValue() {
        return init;
    }

}
