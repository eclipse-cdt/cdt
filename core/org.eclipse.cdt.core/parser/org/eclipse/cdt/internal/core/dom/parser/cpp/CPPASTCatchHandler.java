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
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;

/**
 * @author jcamelon
 */
public class CPPASTCatchHandler extends CPPASTNode implements
        ICPPASTCatchHandler {

    private boolean isCatchAll;
    private IASTStatement body;
    private IASTDeclaration declaration;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler#setIsCatchAll(boolean)
     */
    public void setIsCatchAll(boolean isEllipsis) {
        isCatchAll = isEllipsis;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler#isCatchAll()
     */
    public boolean isCatchAll() {
        return isCatchAll;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler#setCatchBody(org.eclipse.cdt.core.dom.ast.IASTStatement)
     */
    public void setCatchBody(IASTStatement compoundStatement) {
        body = compoundStatement;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler#getCatchBody()
     */
    public IASTStatement getCatchBody() {
        return body;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler#setDeclaration(org.eclipse.cdt.core.dom.ast.IASTDeclaration)
     */
    public void setDeclaration(IASTDeclaration decl) {
        declaration = decl;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler#getDeclaration()
     */
    public IASTDeclaration getDeclaration() {
        return declaration;
    }

}
