/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Mike Kucera (IBM) - implicit names
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;
import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.prvalueType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.typeFromFunctionCall;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.typeFromReturnType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.valueCategoryFromFunctionCall;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.valueCategoryFromReturnType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getNestedType;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpressionList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.LookupData;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

public class CPPASTFunctionCallExpression extends ASTNode
		implements ICPPASTFunctionCallExpression, IASTAmbiguityParent {
    private IASTExpression functionName;
    private IASTInitializerClause[] fArguments;

    private IASTImplicitName[] implicitNames;
    private ICPPFunction overload= UNINITIALIZED_FUNCTION;
    
    public CPPASTFunctionCallExpression() {
    	setArguments(null);
	}

	public CPPASTFunctionCallExpression(IASTExpression functionName, IASTInitializerClause[] args) {
		setFunctionNameExpression(functionName);
		setArguments(args);
	}

	@Override
	public CPPASTFunctionCallExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTFunctionCallExpression copy(CopyStyle style) {
		IASTInitializerClause[] args = null;
		if (fArguments.length > 0) {
			args= new IASTInitializerClause[fArguments.length];
			for (int i = 0; i < fArguments.length; i++) {
				args[i] = fArguments[i].copy(style);
			}
		}

		CPPASTFunctionCallExpression copy = new CPPASTFunctionCallExpression(null, args);
		copy.setFunctionNameExpression(functionName == null ? null : functionName.copy(style));
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}
	
    @Override
	public IASTExpression getFunctionNameExpression() {
        return functionName;
    }

	@Override
	public void setFunctionNameExpression(IASTExpression expression) {
        assertNotFrozen();
        this.functionName = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(FUNCTION_NAME);
		}
    }
	
	@Override
	public IASTInitializerClause[] getArguments() {
        return fArguments;
    }

    @Override
	public void setArguments(IASTInitializerClause[] arguments) {
        assertNotFrozen();
        if (arguments == null) {
        	fArguments= IASTExpression.EMPTY_EXPRESSION_ARRAY;
        } else {
            fArguments= arguments;
        	for (IASTInitializerClause arg : arguments) {
				arg.setParent(this);
				arg.setPropertyInParent(ARGUMENT);
			}
		}
    }

    @Override
	public IASTImplicitName[] getImplicitNames() {
    	if (implicitNames == null) {
    		ICPPFunction overload = getOperator();
			if (overload == null)
				return implicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
			
			if (isExplicitTypeConversion() != null) {
				CPPASTImplicitName n1 = new CPPASTImplicitName(overload.getNameCharArray(), this);
				n1.setOffsetAndLength((ASTNode) functionName);
				n1.setBinding(overload);
				return implicitNames= new IASTImplicitName[] {n1};
			}
			
			if (overload instanceof CPPImplicitFunction) {
				if (!(overload instanceof ICPPMethod) || ((ICPPMethod) overload).isImplicit()) {
					return implicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
				}
			}
			
			// Create separate implicit names for the two brackets
			CPPASTImplicitName n1 = new CPPASTImplicitName(OverloadableOperator.PAREN, this);
			n1.setBinding(overload);

			CPPASTImplicitName n2 = new CPPASTImplicitName(OverloadableOperator.PAREN, this);
			n2.setBinding(overload);
			n2.setAlternate(true);
			
			if (fArguments.length == 0) {
				int idEndOffset = ((ASTNode)functionName).getOffset() + ((ASTNode)functionName).getLength();
				try {
					IToken lparen = functionName.getTrailingSyntax();
					IToken rparen = lparen.getNext();
					
					if (lparen.getType() == IToken.tLPAREN) {
						n1.setOffsetAndLength(idEndOffset + lparen.getOffset(), 1);
					} else {
						n1.setOffsetAndLength(idEndOffset + lparen.getEndOffset(), 0);
					}
						
					if (rparen.getType() == IToken.tRPAREN) {
						n2.setOffsetAndLength(idEndOffset + rparen.getOffset(), 1);
					} else {
						n2.setOffsetAndLength(idEndOffset + rparen.getEndOffset(), 0);
					}
				} catch (ExpansionOverlapsBoundaryException e) {
					n1.setOffsetAndLength(idEndOffset, 0);
					n2.setOffsetAndLength(idEndOffset, 0);
				}
			} else {
				n1.computeOperatorOffsets(functionName, true);
				n2.computeOperatorOffsets(fArguments[fArguments.length-1], true);
			}
			
			implicitNames = new IASTImplicitName[] { n1, n2 };
    	}
    	return implicitNames;
    }
    
    @Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitExpressions) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
      
		if (functionName != null && !functionName.accept(action))
			return false;        
        
        IASTImplicitName[] implicits = action.shouldVisitImplicitNames ? getImplicitNames() : null;
        
		if (implicits != null && implicits.length > 0 && !implicits[0].accept(action))
			return false;
        
		for (IASTInitializerClause arg : fArguments) {
			if (!arg.accept(action))
				return false;
		}

		if (implicits != null && implicits.length > 1 && !implicits[1].accept(action))
			return false;
        
		if (action.shouldVisitExpressions && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;
		
        return true;
    }

	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (child == functionName) {
			other.setPropertyInParent(child.getPropertyInParent());
			other.setParent(child.getParent());
			functionName = (IASTExpression) other;
		}
		for (int i = 0; i < fArguments.length; ++i) {
			if (child == fArguments[i]) {
				other.setPropertyInParent(child.getPropertyInParent());
				other.setParent(child.getParent());
				fArguments[i] = (IASTExpression) other;
			}
		}
	}
    
    public ICPPFunction getOperator() {
    	if (overload == UNINITIALIZED_FUNCTION) {
    		overload= null;
    		IType t= isExplicitTypeConversion();
    		if (t != null) {
    			t = getNestedType(t, TDEF|CVTYPE|REF);
    			if (t instanceof ICPPClassType && !(t instanceof ICPPUnknownBinding)) {
    				ICPPClassType cls= (ICPPClassType) t;
    				LookupData data= CPPSemantics.createLookupData(((IASTIdExpression) functionName).getName());
					try {
						IBinding b= CPPSemantics.resolveFunction(data, cls.getConstructors(), true);
	    				if (b instanceof ICPPFunction)
	    					overload= (ICPPFunction) b;
					} catch (DOMException e) {
					}
    			}
    		} else {
    			t= SemanticUtil.getNestedType(functionName.getExpressionType(), TDEF|REF|CVTYPE);
    			if (t instanceof ICPPClassType) {
    				overload = CPPSemantics.findOverloadedOperator(this, (ICPPClassType)t);
    			}
    		}
		}
    	return overload;
    }
    
    @Override
	public IType getExpressionType() {
		// Handle explicit type conversion in functional notation.
    	IType t= isExplicitTypeConversion();
    	if (t != null) {
			if (t instanceof IProblemBinding) {
				return ProblemType.UNRESOLVED_NAME;
			}
			return prvalueType(t);
		} 
		
		t= SemanticUtil.getNestedType(functionName.getExpressionType(), TDEF|REF|CVTYPE);
		if (t instanceof ICPPClassType) {
			if (overload == UNINITIALIZED_FUNCTION) {
				overload = CPPSemantics.findOverloadedOperator(this, (ICPPClassType)t);
			}
			if (overload != null) {
				return typeFromFunctionCall(overload);
			} 
			return new ProblemType(ISemanticProblem.TYPE_UNKNOWN_FOR_EXPRESSION);
		}
		
		if (t instanceof IPointerType) {
			t= SemanticUtil.getNestedType(((IPointerType) t).getType(), TDEF | REF | CVTYPE);
		}
		if (t instanceof IFunctionType) {
			return typeFromReturnType(((IFunctionType) t).getReturnType());
		}
		if (CPPTemplates.isDependentType(t))
			return CPPUnknownClass.createUnnamedInstance();

		return new ProblemType(ISemanticProblem.TYPE_UNKNOWN_FOR_EXPRESSION);
    }
    
	private IType isExplicitTypeConversion() {
		if (functionName instanceof IASTIdExpression) {
			final IASTName name = ((IASTIdExpression) functionName).getName();
			IBinding b= name.resolvePreBinding();
			if (b instanceof IType)
				return (IType) b;
		}
		return null;
	}

	@Override
	public boolean isLValue() {
    	return getValueCategory() == LVALUE;
	}
    
	@Override
	public ValueCategory getValueCategory() {
		if (isExplicitTypeConversion() != null) 
			return PRVALUE;
		
		IType t= functionName.getExpressionType();
		if (t instanceof ICPPClassType) {
			if (overload == UNINITIALIZED_FUNCTION) {
				overload = CPPSemantics.findOverloadedOperator(this, (ICPPClassType)t);
			}
			if (overload != null) {
				return valueCategoryFromFunctionCall(overload);
			} 
		} else {
			if (t instanceof IPointerType) {
				t= SemanticUtil.getNestedType(((IPointerType) t).getType(), TDEF | REF | CVTYPE);
			}
			if (t instanceof IFunctionType) {
				return valueCategoryFromReturnType(((IFunctionType) t).getReturnType());
			}
		}
		return ValueCategory.PRVALUE;
    }
	
	@Override
	@Deprecated
    public IASTExpression getParameterExpression() {
    	if (fArguments.length == 0)
    		return null;
    	if (fArguments.length == 1) {
    		IASTInitializerClause arg = fArguments[0];
    		if (arg instanceof IASTExpression)
    			return (IASTExpression) arg;
    		return null;
    	}
    		
    	CPPASTExpressionList result= new CPPASTExpressionList();
    	for (IASTInitializerClause arg : fArguments) {
    		if (arg instanceof IASTExpression) {
    			result.addExpression(((IASTExpression) arg).copy());
    		}
    	}
    	result.setParent(this);
    	result.setPropertyInParent(ARGUMENT);
        return result;
    }

	@Override
	@Deprecated
    public void setParameterExpression(IASTExpression expression) {
        assertNotFrozen();
        if (expression == null) {
        	setArguments(null);
        } else if (expression instanceof ICPPASTExpressionList) {
        	setArguments(((ICPPASTExpressionList) expression).getExpressions());
        } else {
        	setArguments(new IASTExpression[] {expression});
        }
    }
}
