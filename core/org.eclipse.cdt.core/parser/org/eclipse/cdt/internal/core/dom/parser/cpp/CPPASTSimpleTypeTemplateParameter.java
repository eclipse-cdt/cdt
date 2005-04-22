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
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;

/**
 * @author jcamelon
 */
public class CPPASTSimpleTypeTemplateParameter extends CPPASTNode implements
        ICPPASTSimpleTypeTemplateParameter {

    private int type;
    private IASTName name;
    private IASTTypeId typeId;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter#getParameterType()
     */
    public int getParameterType() {
        return type;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter#setParameterType(int)
     */
    public void setParameterType(int value) {
        this.type = value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter#getName()
     */
    public IASTName getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter#setName(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public void setName(IASTName name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter#getDefaultType()
     */
    public IASTTypeId getDefaultType() {
        return typeId;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter#setDefaultType(org.eclipse.cdt.core.dom.ast.IASTTypeId)
     */
    public void setDefaultType(IASTTypeId typeId) {
        this.typeId = typeId;
    }
    public boolean accept( ASTVisitor action ){
        if( action instanceof CPPASTVisitor &&
            ((CPPASTVisitor)action).shouldVisitTemplateParameters ){
		    switch( ((CPPASTVisitor)action).visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        
        if( name != null ) if( !name.accept( action ) ) return false;
        if( typeId != null ) if( !typeId.accept( action ) ) return false;
        
        return true;
    }
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTNameOwner#getRoleForName(org.eclipse.cdt.core.dom.ast.IASTName)
	 */
	public int getRoleForName(IASTName n) {
		if( n == name )
			return r_declaration;
		return r_unclear;
	}
}
