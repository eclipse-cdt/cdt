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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope;

/**
 * @author jcamelon
 */
public class CPPASTTemplateSpecialization extends CPPASTNode implements
        ICPPASTTemplateSpecialization, ICPPASTTemplateDeclaration {

    private IASTDeclaration declaration;
    private ICPPTemplateScope templateScope;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization#getDeclaration()
     */
    public IASTDeclaration getDeclaration() {
        return declaration;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization#setDeclaration(org.eclipse.cdt.core.dom.ast.IASTDeclaration)
     */
    public void setDeclaration(IASTDeclaration declaration) {
        this.declaration = declaration;
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitDeclarations ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        
        if( declaration != null ) if( !declaration.accept( action ) ) return false;
        return true;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration#isExported()
	 */
	public boolean isExported() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration#setExported(boolean)
	 */
	public void setExported(boolean value) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration#getTemplateParameters()
	 */
	public ICPPASTTemplateParameter[] getTemplateParameters() {
		return ICPPASTTemplateParameter.EMPTY_TEMPLATEPARAMETER_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration#addTemplateParamter(org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter)
	 */
	public void addTemplateParamter(ICPPASTTemplateParameter parm) {
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
