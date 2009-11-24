/*******************************************************************************
 *  Copyright (c) 2004, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypenameExpression;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

public class CPPASTTypenameExpression extends ASTNode implements
        ICPPASTTypenameExpression, IASTAmbiguityParent {

    private boolean isTemplate;
    private IASTName name;
    private IASTExpression init;

    
    public CPPASTTypenameExpression() {
	}

	public CPPASTTypenameExpression(IASTName name, IASTExpression init) {
		this(name, init, false);
	}
	
	public CPPASTTypenameExpression(IASTName name, IASTExpression init, boolean isTemplate) {
		setName(name);
		setInitialValue(init);
		this.isTemplate = isTemplate;
	}

	public CPPASTTypenameExpression copy() {
		CPPASTTypenameExpression copy = new CPPASTTypenameExpression();
		copy.setName(name == null ? null : name.copy());
		copy.setInitialValue(init == null ? null : init.copy());
		copy.isTemplate = isTemplate;
		copy.setOffsetAndLength(this);
		return copy;
	}
	
	
	public void setIsTemplate(boolean templateTokenConsumed) {
        assertNotFrozen();
        isTemplate = templateTokenConsumed;
    }

    public boolean isTemplate() {
        return isTemplate;
    }

    public void setName(IASTName name) {
        assertNotFrozen();
        this.name = name;
        if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(TYPENAME);
		}
    }

    public IASTName getName() {
        return name;
    }

    public void setInitialValue(IASTExpression expressionList) {
        assertNotFrozen();
        init = expressionList;
        if (expressionList != null) {
			expressionList.setParent(this);
			expressionList.setPropertyInParent(INITIAL_VALUE);
		}
    }

    public IASTExpression getInitialValue() {
        return init;
    }

    @Override
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
        
        if( action.shouldVisitExpressions ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
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
		IBinding binding = getName().resolvePreBinding();
		if (binding instanceof IType) {
			return (IType) binding;
	    }
		return null;
    }

	public boolean isLValue() {
		return false;
	}
}
