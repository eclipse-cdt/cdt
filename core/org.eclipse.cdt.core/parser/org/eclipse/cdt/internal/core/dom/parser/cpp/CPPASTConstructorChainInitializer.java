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

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;

/**
 * @author jcamelon
 */
public class CPPASTConstructorChainInitializer extends CPPASTNode implements
        ICPPASTConstructorChainInitializer {

    private IASTName name;
    private IASTExpression value;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer#getMemberInitializerId()
     */
    public IASTName getMemberInitializerId() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer#setMemberInitializerId(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public void setMemberInitializerId(IASTName name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer#getInitializerValue()
     */
    public IASTExpression getInitializerValue() {
        return value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer#setInitializerValue(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setInitializerValue(IASTExpression expression) {
        value = expression;
    }

    public boolean accept( ASTVisitor action ){
        if( name != null ) if( !name.accept( action ) ) return false;
        if( value != null ) if( !value.accept( action ) ) return false;
        return true;
    }
}
