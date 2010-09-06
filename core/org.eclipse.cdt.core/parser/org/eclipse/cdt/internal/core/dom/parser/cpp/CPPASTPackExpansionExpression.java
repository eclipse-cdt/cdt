/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPackExpansionExpression;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;

/**
 * Implementation of pack expansion expression.
 */
public class CPPASTPackExpansionExpression extends ASTNode implements ICPPASTPackExpansionExpression, IASTAmbiguityParent {

	private IASTExpression fPattern;

	public CPPASTPackExpansionExpression(IASTExpression pattern) {
		setPattern(pattern);
	}

	public void setPattern(IASTExpression pattern) {
		assertNotFrozen();
		
		fPattern= pattern;
		if (pattern != null) {
			pattern.setParent(this);
			pattern.setPropertyInParent(ICPPASTPackExpansionExpression.PATTERN);
		}
	}

	public IASTExpression getPattern() {
		return fPattern;
	}
	
	public CPPASTPackExpansionExpression copy() {
		CPPASTPackExpansionExpression copy = new CPPASTPackExpansionExpression(fPattern.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}

	public IType getExpressionType() {
		final IType type = fPattern.getExpressionType();
		if (type == null)
			return new ProblemBinding(this, IProblemBinding.SEMANTIC_INVALID_TYPE, getRawSignatureChars());
		
		return new CPPParameterPackType(type);
	}

	public boolean isLValue() {
		return fPattern.isLValue();
	}
	
	public ValueCategory getValueCategory() {
		return fPattern.getValueCategory();
	}

	@Override
	public boolean accept(ASTVisitor visitor) {
        if (visitor.shouldVisitExpressions) {
		    switch (visitor.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP:  return true;
	            default : break;
	        }
		}
        if (!fPattern.accept(visitor)) {
        	return false;
        }
        if (visitor.shouldVisitExpressions && visitor.leave(this) == ASTVisitor.PROCESS_ABORT) {
        	return false;
        }
        return true;
    }

	public void replace(IASTNode child, IASTNode other) {
		if (child == fPattern) {
            other.setPropertyInParent(child.getPropertyInParent());
            other.setParent(child.getParent());
            fPattern = (IASTExpression) other;
		}
	}
}
