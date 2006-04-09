/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypenameExpression;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CPPASTTypenameExpression extends CPPASTNode implements
        ICPPASTTypenameExpression, IASTAmbiguityParent {

    private boolean isTemplate;
    private IASTName name;
    private IASTExpression init;

    public void setIsTemplate(boolean templateTokenConsumed) {
        isTemplate = templateTokenConsumed;
    }

    public boolean isTemplate() {
        return isTemplate;
    }

    public void setName(IASTName name) {
        this.name = name;
    }

    public IASTName getName() {
        return name;
    }

    public void setInitialValue(IASTExpression expressionList) {
        init = expressionList;
    }

    public IASTExpression getInitialValue() {
        return init;
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitExpressions ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        
        if( name != null ) if( !name.accept( action ) ) return false;
        if( init != null ) if( !init.accept( action ) ) return false;
        return true;
    }
	
	public int getRoleForName(IASTName n) {
		if( n == name )
			return r_reference;
		return r_unclear;
	}

    public void replace(IASTNode child, IASTNode other) {
        if( child == init )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            init  = (IASTExpression) other;
        }
        
    }
    
    public IType getExpressionType() {
    	return CPPVisitor.getExpressionType(this);
    }
    
}
