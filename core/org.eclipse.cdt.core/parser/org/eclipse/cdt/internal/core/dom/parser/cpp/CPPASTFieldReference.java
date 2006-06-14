/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CPPASTFieldReference extends CPPASTNode implements
        ICPPASTFieldReference, IASTAmbiguityParent {

    private boolean isTemplate;
    private IASTExpression owner;
    private IASTName name;
    private boolean isDeref;

    public boolean isTemplate() {
        return isTemplate;
    }

    public void setIsTemplate(boolean value) {
        isTemplate = value;
    }

    public IASTExpression getFieldOwner() {
        return owner;
    }

    public void setFieldOwner(IASTExpression expression) {
        owner = expression;
    }

    public IASTName getFieldName() {
        return name;
    }

    public void setFieldName(IASTName name) {
        this.name =name;
    }

    public boolean isPointerDereference() {
        return isDeref;
    }

    public void setIsPointerDereference(boolean value) {
        isDeref = value;
    }
    
    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitExpressions ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
      
        if( owner != null ) if( !owner.accept( action ) ) return false;
        if( name != null )  if( !name.accept( action ) ) return false;
        return true;
    }

	public int getRoleForName(IASTName n) {
		if( n == name )
			return r_reference;
		return r_unclear;
	}

    public void replace(IASTNode child, IASTNode other) {
        if( child == owner )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            owner  = (IASTExpression) other;
        }
    }

    public IType getExpressionType() {
    	return CPPVisitor.getExpressionType(this);
    }
    
}
