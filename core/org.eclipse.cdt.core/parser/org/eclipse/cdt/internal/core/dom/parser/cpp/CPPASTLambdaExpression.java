/*******************************************************************************
 * Copyright (c) 2010, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCapture;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLambdaExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;

/**
 * Implementation for lambda expressions.
 */
public class CPPASTLambdaExpression extends ASTNode implements ICPPASTLambdaExpression {
	private static final ICPPASTCapture[] NO_CAPTURES = {};
	
	private CaptureDefault fCaptureDefault;
	private ICPPASTCapture[] fCaptures;
	private ICPPASTFunctionDeclarator fDeclarator;

	private IASTCompoundStatement fBody;
	
	private IASTImplicitName fClosureTypeName;
	private IASTImplicitName fImplicitFunctionCallName;

	private ICPPEvaluation fEvaluation;

	public CPPASTLambdaExpression() {
		fCaptureDefault= CaptureDefault.UNSPECIFIED;
	}

	@Override
	public CPPASTLambdaExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTLambdaExpression copy(CopyStyle style) {
		CPPASTLambdaExpression copy = new CPPASTLambdaExpression();
		copy.fCaptureDefault = fCaptureDefault;
		if (fCaptures != null) {
			for (ICPPASTCapture capture : fCaptures) {
				if (capture != null) {
					copy.addCapture(capture.copy(style));
				}
			}
		}
		if (fDeclarator != null) {
			copy.setDeclarator(fDeclarator.copy(style));
		}
		if (fBody != null) {
			copy.setBody(fBody.copy(style));
		}

		return copy(copy, style);
	}

	@Override
	public IASTImplicitName getClosureTypeName() {
		if (fClosureTypeName == null) {
    		final CPPClosureType closureType = getExpressionType();
			CPPASTImplicitName name = new CPPASTImplicitName(closureType.getNameCharArray(), this);
			name.setBinding(closureType);
			name.setIsDefinition(true);

			name.setOffsetAndLength(getOffset(), 1);
			fClosureTypeName= name;
    	}
		return fClosureTypeName;
	}

	@Override
	public IASTImplicitName getFunctionCallOperatorName() {
		if (fImplicitFunctionCallName == null) {
    		final CPPClosureType closureType = getExpressionType();
			ICPPFunction callOperator= closureType.getFunctionCallOperator();
			
			CPPASTImplicitName name = new CPPASTImplicitName(closureType.getNameCharArray(), this);
			name.setBinding(callOperator);
			name.setIsDefinition(true);

			if (fBody instanceof ASTNode) {
				ASTNode bodyNode= (ASTNode) fBody;
				name.setOffsetAndLength(bodyNode.getOffset(), 1);
			}		
			fImplicitFunctionCallName= name;
    	}
		return fImplicitFunctionCallName;
	}

    @Override
	public IASTImplicitName[] getImplicitNames() {
    	return new IASTImplicitName[] {getFunctionCallOperatorName()};
    }

	@Override
	public boolean accept(ASTVisitor visitor) {
        if (visitor.shouldVisitExpressions) {
		    switch (visitor.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}

		if (visitor.shouldVisitImplicitNames && !getClosureTypeName().accept(visitor))
			return false;

		if (fCaptures != null) {
			for (ICPPASTCapture cap : fCaptures) {
				if (cap != null && !cap.accept(visitor))
					return false;
			}
		}
		if (fDeclarator != null && !fDeclarator.accept(visitor))
			return false;
		
		if (visitor.shouldVisitImplicitNames && !getFunctionCallOperatorName().accept(visitor))
			return false;
		
		if (fBody != null && !fBody.accept(visitor))
			return false;

		if (visitor.shouldVisitExpressions && visitor.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;
		
        return true;
    }

	@Override
	public IASTCompoundStatement getBody() {
		return fBody;
	}

	@Override
	public CaptureDefault getCaptureDefault() {
		return fCaptureDefault;
	}

	@Override
	public ICPPASTCapture[] getCaptures() {
		if (fCaptures == null)
			return NO_CAPTURES;
		return fCaptures= ArrayUtil.trim(fCaptures);
	}

	@Override
	public ICPPASTFunctionDeclarator getDeclarator() {
		return fDeclarator;
	}

	@Override
	public void addCapture(ICPPASTCapture capture) {
		assertNotFrozen();
		capture.setParent(this);
		capture.setPropertyInParent(CAPTURE);
		if (fCaptures == null) {
			fCaptures= new ICPPASTCapture[] {capture, null};
		} else {
			fCaptures= ArrayUtil.append(fCaptures, capture);
		}
	}

	@Override
	public void setBody(IASTCompoundStatement body) {
		assertNotFrozen();
		body.setParent(this);
		body.setPropertyInParent(BODY);
		fBody= body;
	}

	@Override
	public void setCaptureDefault(CaptureDefault value) {
		fCaptureDefault= value;
	}

	@Override
	public void setDeclarator(ICPPASTFunctionDeclarator dtor) {
		assertNotFrozen();
		dtor.setParent(this);
		dtor.setPropertyInParent(DECLARATOR);
		fDeclarator= dtor;
	}

	@Override
	public ICPPEvaluation getEvaluation() {
		if (fEvaluation == null) {
			fEvaluation= new EvalFixed(new CPPClosureType(this), PRVALUE, Value.UNKNOWN);
		}
		return fEvaluation;
	}
	
	@Override
	public CPPClosureType getExpressionType() {
		return (CPPClosureType) getEvaluation().getTypeOrFunctionSet(this);
	}

	@Override
	public boolean isLValue() {
		return false;
	}
	
	@Override
	public ValueCategory getValueCategory() {
		return ValueCategory.PRVALUE;
	}
}
