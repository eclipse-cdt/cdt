/**********************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

/**
 * @author jcamelon
 */
public class CPPASTTemplateDeclaration extends CPPASTNode implements
        ICPPASTTemplateDeclaration {

    private boolean exported;
    private IASTDeclaration declaration;
    private ICPPTemplateScope templateScope;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration#isExported()
     */
    public boolean isExported() {
        return exported;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration#setExported(boolean)
     */
    public void setExported(boolean value) {
        exported = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration#getDeclaration()
     */
    public IASTDeclaration getDeclaration() {
        return declaration;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration#setDeclaration(org.eclipse.cdt.core.dom.ast.IASTDeclaration)
     */
    public void setDeclaration(IASTDeclaration declaration) {
        this.declaration = declaration;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration#getTemplateParameters()
     */
    public ICPPASTTemplateParameter [] getTemplateParameters() {
        if( parameters == null ) return ICPPASTTemplateParameter.EMPTY_TEMPLATEPARAMETER_ARRAY;
        return (ICPPASTTemplateParameter[]) ArrayUtil.removeNulls( ICPPASTTemplateParameter.class, parameters );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration#addTemplateParamter(org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter)
     */
    public void addTemplateParamter(ICPPASTTemplateParameter parm) {
        parameters = (ICPPASTTemplateParameter[]) ArrayUtil.append( ICPPASTTemplateParameter.class, parameters, parm );
    }

    private ICPPASTTemplateParameter [] parameters = null;
    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitDeclarations ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        
        ICPPASTTemplateParameter [] params = getTemplateParameters();
        for ( int i = 0; i < params.length; i++ ) {
            if( !params[i].accept( action ) ) return false;
        }
        
        if( declaration != null ) if( !declaration.accept( action ) ) return false;
        return true;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration#getScope()
	 */
	public ICPPTemplateScope getScope() {
		if( templateScope == null )
			templateScope = new CPPTemplateScope( this );
		return templateScope;
	}
}
