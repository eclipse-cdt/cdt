/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
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
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.*;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpressionList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;


public class CPPASTFunctionCallExpression extends ASTNode implements
        ICPPASTFunctionCallExpression, IASTAmbiguityParent {
	
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

	public CPPASTFunctionCallExpression copy() {
		IASTInitializerClause[] args = null;
		if (fArguments.length > 0) {
			args= new IASTInitializerClause[fArguments.length];
			for (int i = 0; i < fArguments.length; i++) {
				args[i]= fArguments[i].copy();
			}
		}

		CPPASTFunctionCallExpression copy = new CPPASTFunctionCallExpression(null, args);
		copy.setFunctionNameExpression(functionName == null ? null : functionName.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}
	
    public IASTExpression getFunctionNameExpression() {
        return functionName;
    }

	public void setFunctionNameExpression(IASTExpression expression) {
        assertNotFrozen();
        this.functionName = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(FUNCTION_NAME);
		}
    }
	
	public IASTInitializerClause[] getArguments() {
        return fArguments;
    }

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

    public IASTImplicitName[] getImplicitNames() {
    	if (implicitNames == null) {
    		ICPPFunction overload = getOperator();
			if (overload == null || overload instanceof CPPImplicitFunction) {
				if (!(overload instanceof ICPPMethod) || ((ICPPMethod) overload).isImplicit()) {
					return implicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
				}
			}
			
			// create separate implicit names for the two brackets
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
					
					if (lparen.getType() == IToken.tLPAREN)
						n1.setOffsetAndLength(idEndOffset + lparen.getOffset(), 1);
					else
						n1.setOffsetAndLength(idEndOffset + lparen.getEndOffset(), 0);
						
					if (rparen.getType() == IToken.tRPAREN)
						n2.setOffsetAndLength(idEndOffset + rparen.getOffset(), 1);
					else
						n2.setOffsetAndLength(idEndOffset + rparen.getEndOffset(), 0);
				} catch(ExpansionOverlapsBoundaryException e) {
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

		if (implicits != null && implicits.length > 0 && !implicits[1].accept(action))
			return false;
        
		if (action.shouldVisitExpressions && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;
		
        return true;
    }

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
    		IType t= functionName.getExpressionType();
    		t= SemanticUtil.getNestedType(t, TDEF | REF | CVTYPE);
    		if (t instanceof ICPPClassType) {
    			overload = CPPSemantics.findOverloadedOperator(this, (ICPPClassType)t);
    		}
		}
    	return overload;
    }
    
    public IType getExpressionType() {
    	if (functionName instanceof IASTIdExpression) {
			// Handle misused id-expression in functional type conversions or simple type initializers.
			final IBinding binding= ((IASTIdExpression) functionName).getName().resolvePreBinding();
			if (binding instanceof ICPPConstructor) {
				IBinding owner= binding.getOwner();
				if (owner instanceof ICPPClassType) {
					return (ICPPClassType) owner;
				} 
				return new ProblemBinding(this, IProblemBinding.SEMANTIC_BAD_SCOPE, binding.getNameCharArray());
			} 
			if (binding instanceof IProblemBinding) {
				return (IProblemBinding) binding;
			}
			if (binding instanceof IType) {
				return prvalueType((IType) binding);
			} 
		} 
		
		IType t= SemanticUtil.getNestedType(functionName.getExpressionType(), TDEF|REF|CVTYPE);
		if (t instanceof ICPPClassType) {
			if (overload == UNINITIALIZED_FUNCTION) {
				overload = CPPSemantics.findOverloadedOperator(this, (ICPPClassType)t);
			}
			if (overload != null) {
				return typeFromFunctionCall(overload);
			} 
			// mstodo problem type
			return null;
		}
		
		if (t instanceof IPointerType) {
			t= SemanticUtil.getNestedType(((IPointerType) t).getType(), TDEF | REF | CVTYPE);
		}
		if (t instanceof IFunctionType) {
			return typeFromReturnType(((IFunctionType) t).getReturnType());
		}

		// mstodo problem type
		return null;
    }
    
    public boolean isLValue() {
    	return getValueCategory() == LVALUE;
	}
    
	public ValueCategory getValueCategory() {
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
