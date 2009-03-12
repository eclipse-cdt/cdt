/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Mike Kucera (IBM)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;
 
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

/**
 * @author jcamelon
 */
public class CPPASTArraySubscriptExpression extends ASTNode implements ICPPASTArraySubscriptExpression, IASTAmbiguityParent {

    private IASTExpression subscriptExp;
    private IASTExpression arrayExpression;

    private IASTImplicitName[] implicitNames = null;
    
    public CPPASTArraySubscriptExpression() {
	}

	public CPPASTArraySubscriptExpression(IASTExpression arrayExpression, IASTExpression subscriptExp) {
		setArrayExpression(arrayExpression);
		setSubscriptExpression(subscriptExp);
	}
	
	public CPPASTArraySubscriptExpression copy() {
		CPPASTArraySubscriptExpression copy = new CPPASTArraySubscriptExpression();
		copy.setArrayExpression(arrayExpression == null ? null : arrayExpression.copy());
		copy.setSubscriptExpression(subscriptExp == null ? null : subscriptExp.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}
	

	public IASTExpression getArrayExpression() {
        return arrayExpression;
    }

    public void setArrayExpression(IASTExpression expression) {
        assertNotFrozen();
        arrayExpression = expression;        
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(ARRAY);
		}
    }

    public IASTExpression getSubscriptExpression() {
        return subscriptExp;
    }

    public void setSubscriptExpression(IASTExpression expression) {
        assertNotFrozen();
        subscriptExp = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(SUBSCRIPT);
		}
    }
    
    public IASTImplicitName[] getImplicitNames() {
		if(implicitNames == null) {
			ICPPFunction overload = getOverload();
			if(overload == null)
				return implicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
			
			// create separate implicit names for the two brackets
			CPPASTImplicitName n1 = new CPPASTImplicitName(OverloadableOperator.BRACKET, this);
			n1.setBinding(overload);
			n1.computeOperatorOffsets(arrayExpression, true);

			CPPASTImplicitName n2 = new CPPASTImplicitName(OverloadableOperator.BRACKET, this);
			n2.setBinding(overload);
			n2.computeOperatorOffsets(subscriptExp, true);
			n2.setAlternate(true);
			
			implicitNames = new IASTImplicitName[] { n1, n2 };
		}
		
		return implicitNames;
	}
    
    
    public ICPPFunction getOverload() {
    	IType type1 = arrayExpression.getExpressionType();
    	IType ultimateType1 = SemanticUtil.getUltimateTypeUptoPointers(type1);
		if (ultimateType1 instanceof IProblemBinding) {
			return null;
		}
		if (ultimateType1 instanceof ICPPClassType) {
			return CPPSemantics.findOperator(this, (ICPPClassType) ultimateType1);
		}
		return null;
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
        if( arrayExpression != null ) 
            if( !arrayExpression.accept( action ) ) return false;
        
        IASTImplicitName[] implicits = action.shouldVisitImplicitNames ? getImplicitNames() : null;
        
        if(implicits != null && implicits.length > 0)
        	if(!implicits[0].accept(action)) return false;
        
        if( subscriptExp != null )   
            if( !subscriptExp.accept( action ) ) return false;
        
        if(implicits != null && implicits.length > 0)
        	if(!implicits[1].accept(action)) return false;
        
        if( action.shouldVisitExpressions ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }

    public void replace(IASTNode child, IASTNode other) {
        if( child == subscriptExp )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            subscriptExp  = (IASTExpression) other;
        }
        if( child == arrayExpression )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            arrayExpression  = (IASTExpression) other;
        }
    }

    public IType getExpressionType() {
		IType t = getArrayExpression().getExpressionType();
		t= SemanticUtil.getUltimateTypeUptoPointers(t);
		try {
			if (t instanceof ICPPUnknownType) {
				return CPPUnknownClass.createUnnamedInstance();
			}
			if (t instanceof ICPPClassType) {
				ICPPFunction op = CPPSemantics.findOperator(this, (ICPPClassType) t);
				if (op != null) {
					return op.getType().getReturnType();
				}
			}
			if (t instanceof IPointerType) {
				return ((IPointerType) t).getType();
			}
			if (t instanceof IArrayType) {
				return ((IArrayType) t).getType();
			}
		} catch (DOMException e) {
			return e.getProblem();
		}
		return null;
    }
    
}
