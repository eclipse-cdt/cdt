/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.Assert;

/**
 * @author jcamelon
 */
public class CPPASTNewExpression extends ASTNode implements
        ICPPASTNewExpression, IASTAmbiguityParent {

    private boolean global;
    private IASTExpression placement;
    private IASTExpression initializer;
    private IASTTypeId typeId;
    private boolean isNewTypeId;
    
    private IASTExpression [] arrayExpressions = null;


    
    public CPPASTNewExpression() {
	}

	public CPPASTNewExpression(IASTExpression placement,
			IASTExpression initializer, IASTTypeId typeId) {
		setNewPlacement(placement);
		setNewInitializer(initializer);
		setTypeId(typeId);
	}

	public boolean isGlobal() {
        return global;
    }

    public void setIsGlobal(boolean value) {
        assertNotFrozen();
        global = value;
    }

    public IASTExpression getNewPlacement() {
        return placement;
    }

    public void setNewPlacement(IASTExpression expression) {
        assertNotFrozen();
        placement = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(NEW_PLACEMENT);
		}
    }

    public IASTExpression getNewInitializer() {
        return initializer;
    }

    public void setNewInitializer(IASTExpression expression) {
        assertNotFrozen();
        initializer = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(NEW_INITIALIZER);
		}
    }

    public IASTTypeId getTypeId() {
        return typeId;
    }

    public void setTypeId(IASTTypeId typeId) {
        assertNotFrozen();
        this.typeId = typeId;
        if (typeId != null) {
			typeId.setParent(this);
			typeId.setPropertyInParent(TYPE_ID);
		}
    }

    public boolean isNewTypeId() {
        return isNewTypeId;
    }

    public void setIsNewTypeId(boolean value) {
        assertNotFrozen();
        isNewTypeId = value;
    }

    public IASTExpression [] getNewTypeIdArrayExpressions() {
        if( arrayExpressions == null ) {
        	if (typeId != null) {
        		IASTDeclarator dtor= CPPVisitor.findInnermostDeclarator(typeId.getAbstractDeclarator());
        		if (dtor instanceof IASTArrayDeclarator) {
        			IASTArrayDeclarator ad= (IASTArrayDeclarator) dtor;
        			IASTArrayModifier[] ams= ad.getArrayModifiers();
        			arrayExpressions= new IASTExpression[ams.length];
        			for (int i = 0; i < ams.length; i++) {
        				IASTArrayModifier am = ams[i];
        				arrayExpressions[i]= am.getConstantExpression();
        			}
        			return arrayExpressions;
        		} 
        	}
        	arrayExpressions= IASTExpression.EMPTY_EXPRESSION_ARRAY;
        }
        return arrayExpressions;
    }

    public void addNewTypeIdArrayExpression(IASTExpression expression) {
        assertNotFrozen();
    	Assert.isNotNull(typeId);
    	IASTDeclarator dtor= CPPVisitor.findInnermostDeclarator(typeId.getAbstractDeclarator());
    	if (dtor instanceof IASTArrayDeclarator == false) {
    		Assert.isNotNull(dtor);
    		Assert.isTrue(dtor.getParent() == typeId);
    		IASTArrayDeclarator adtor= new CPPASTArrayDeclarator(dtor.getName());
    		IASTPointerOperator[] ptrOps= dtor.getPointerOperators();
    		for (IASTPointerOperator ptr : ptrOps) {
        		adtor.addPointerOperator(ptr);				
			}
    		typeId.setAbstractDeclarator(adtor);
    		dtor= adtor;
    	}
    	IASTArrayModifier mod= new CPPASTArrayModifier(expression);
    	((ASTNode) mod).setOffsetAndLength((ASTNode)expression);
    	((IASTArrayDeclarator) dtor).addArrayModifier(mod);
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
        
        if( placement != null ) if( !placement.accept( action ) ) return false;
        if( typeId != null ) if( !typeId.accept( action ) ) return false;
        if( initializer != null ) if( !initializer.accept( action ) ) return false;
        
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
        if( child == placement )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            placement  = (IASTExpression) other;
        }
        if( child == initializer )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            initializer  = (IASTExpression) other;
        }
    }
    
    public IType getExpressionType() {
    	return CPPVisitor.getExpressionType(this);
    }
    
}
