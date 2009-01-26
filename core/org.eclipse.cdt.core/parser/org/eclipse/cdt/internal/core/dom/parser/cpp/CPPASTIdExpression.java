/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Bryan Wilkinson (QNX)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;

/**
 * @author jcamelon
 */
public class CPPASTIdExpression extends ASTNode implements IASTIdExpression, IASTCompletionContext {

	private IASTName name;


    public CPPASTIdExpression() {
	}

	public CPPASTIdExpression(IASTName name) {
		setName(name);
	}

	public CPPASTIdExpression copy() {
		CPPASTIdExpression copy = new CPPASTIdExpression(name == null ? null : name.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}
	
	public IASTName getName() {
        return name;
    }

    public void setName(IASTName name) {
        assertNotFrozen();
        this.name = name;
        if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(ID_NAME);
		}
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
		if( name == n )return r_reference;
		return r_unclear;
	}
	
	public IType getExpressionType() {
        IBinding binding = name.resolvePreBinding();
        try {
			if (binding instanceof IVariable) {
                return ((IVariable) binding).getType();
			} else if (binding instanceof IEnumerator) {
				return ((IEnumerator) binding).getType();
			} else if (binding instanceof IProblemBinding) {
				return (IType) binding;
			} else if (binding instanceof IFunction) {
				return ((IFunction) binding).getType();
			} else if (binding instanceof ICPPTemplateNonTypeParameter) {
				return ((ICPPTemplateNonTypeParameter) binding).getType();
			} else if (binding instanceof ICPPClassType) {
				return ((ICPPClassType) binding);
			} else if (binding instanceof ICPPUnknownBinding) {
				return CPPUnknownClass.createUnnamedInstance();
			}
		} catch (DOMException e) {
			return e.getProblem();
		}
		return null;
	}
	
	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		return CPPSemantics.findBindingsForContentAssist(n, isPrefix);
	}
}
