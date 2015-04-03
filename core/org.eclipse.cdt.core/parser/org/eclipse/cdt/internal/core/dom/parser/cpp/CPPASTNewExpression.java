/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Mike Kucera (IBM)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTExpressionList;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalTypeId;
import org.eclipse.core.runtime.Assert;

/**
 * Represents a new expression [expr.new].
 */
public class CPPASTNewExpression extends ASTNode implements ICPPASTNewExpression, IASTAmbiguityParent {
    private IASTInitializerClause[] fPlacement;
    private IASTTypeId fTypeId;
    private IASTInitializer fInitializer;
    private boolean fIsGlobal;
    private boolean fIsNewTypeId;
	
    private IASTExpression[] fCachedArraySizes;
	private ICPPEvaluation fEvaluation;
    private IASTImplicitName[] fImplicitNames;
    
    public CPPASTNewExpression() {
	}

	public CPPASTNewExpression(IASTInitializerClause[] placement, IASTInitializer initializer, IASTTypeId typeId) {
		setPlacementArguments(placement);
		setTypeId(typeId);
		setInitializer(initializer);
	}

	@Override
	public CPPASTNewExpression copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTNewExpression copy(CopyStyle style) {
		CPPASTNewExpression copy = new CPPASTNewExpression();
		copy.setIsGlobal(fIsGlobal);
		copy.setIsNewTypeId(fIsNewTypeId);
		if (fPlacement != null) {
			IASTInitializerClause[] plcmt = new IASTInitializerClause[fPlacement.length];
			for (int i = 0; i < fPlacement.length; i++) {
				plcmt[i] = fPlacement[i].copy(style);
			}
			copy.setPlacementArguments(plcmt);
		}
		copy.setTypeId(fTypeId == null ? null : fTypeId.copy(style));
		copy.setInitializer(fInitializer == null ? null : fInitializer.copy(style));
		return copy(copy, style);
	}

	@Override
	public boolean isGlobal() {
        return fIsGlobal;
    }

    @Override
	public void setIsGlobal(boolean value) {
        assertNotFrozen();
        fIsGlobal = value;
    }

    @Override
	public IASTInitializerClause[] getPlacementArguments() {
    	return fPlacement;
    }
    
    @Override
	public void setPlacementArguments(IASTInitializerClause[] args) {
        assertNotFrozen();
        fPlacement = args;
        if (args != null) {
        	for (IASTInitializerClause arg : args) {
				arg.setParent(this);
				arg.setPropertyInParent(NEW_PLACEMENT);
			}
		}
    }

    @Override
	public IASTInitializer getInitializer() {
        return fInitializer;
    }

    @Override
	public void setInitializer(IASTInitializer expression) {
        assertNotFrozen();
        fInitializer = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(NEW_INITIALIZER);
		}
    }

    @Override
	public IASTTypeId getTypeId() {
        return fTypeId;
    }

    @Override
	public void setTypeId(IASTTypeId typeId) {
        assertNotFrozen();
        fTypeId = typeId;
        if (typeId != null) {
			typeId.setParent(this);
			typeId.setPropertyInParent(TYPE_ID);
		}
    }

    @Override
	public boolean isNewTypeId() {
        return fIsNewTypeId;
    }

    @Override
	public void setIsNewTypeId(boolean value) {
        assertNotFrozen();
        fIsNewTypeId = value;
    }
    
    /**
     * @see org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner#getImplicitNames()
     */
    @Override
	public IASTImplicitName[] getImplicitNames() {
    	if (fImplicitNames == null) {
    		CPPASTImplicitName operatorName = null;
			ICPPFunction operatorFunction = CPPSemantics.findOverloadedOperator(this);
			if (operatorFunction != null && !(operatorFunction instanceof CPPImplicitFunction)) {
				operatorName = new CPPASTImplicitName(operatorFunction.getNameCharArray(), this);
				operatorName.setOperator(true);
				operatorName.setBinding(operatorFunction);
				operatorName.setOffsetAndLength(getOffset(), 3);
			}

			CPPASTImplicitName constructorName = null;
			IBinding constructor = CPPSemantics.findImplicitlyCalledConstructor(this);
			if (constructor != null) {
				constructorName = new CPPASTImplicitName(constructor.getNameCharArray(), this);
				constructorName.setBinding(constructor);
				constructorName.setOffsetAndLength((ASTNode) getTypeId());
			}

			if (operatorName != null) {
				if (constructorName != null) {
					fImplicitNames = new IASTImplicitName[] { operatorName, constructorName };
				} else {
					fImplicitNames = new IASTImplicitName[] { operatorName };
				}
			} else {
				if (constructorName != null) {
					fImplicitNames = new IASTImplicitName[] { constructorName };
				} else {
					fImplicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
				}
			}
    	}
    	
    	return fImplicitNames;  
    }

    /**
	 * Returns true if this expression is allocating an array.
	 * @since 5.1
	 */
	@Override
	public boolean isArrayAllocation() {
		IASTTypeId typeId= getTypeId();
		if (typeId != null) {
			IASTDeclarator dtor= typeId.getAbstractDeclarator();
			if (dtor != null) {
				dtor= ASTQueries.findTypeRelevantDeclarator(dtor);
				return dtor instanceof IASTArrayDeclarator;
			}
		}
		return false;
	}

    @Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitExpressions) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP:  return true;
	            default: break;
	        }
		}
        
        if (action.shouldVisitImplicitNames) { 
        	for (IASTImplicitName name : getImplicitNames()) {
        		if (!name.accept(action)) return false;
        	}
        }
        
		if (fPlacement != null) {
			for (IASTInitializerClause arg : fPlacement) {
				if (!arg.accept(action))
					return false;
			}
		}
		if (fTypeId != null && !fTypeId.accept(action))
			return false;

		if (fInitializer != null && !fInitializer.accept(action))
			return false;       
        
        if (action.shouldVisitExpressions) {
		    switch (action.leave(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP:  return true;
	            default: break;
	        }
		}
        return true;
    }

	@Override
	public void replace(IASTNode child, IASTNode other) {
		if (fPlacement != null) {
			for (int i = 0; i < fPlacement.length; ++i) {
				if (child == fPlacement[i]) {
					other.setPropertyInParent(child.getPropertyInParent());
					other.setParent(child.getParent());
					fPlacement[i] = (IASTExpression) other;
				}
			}
		}
	}
    
	@Override
	public ICPPEvaluation getEvaluation() {
		if (fEvaluation == null) {
			IType t = fTypeId != null ? CPPVisitor.createType(fTypeId) : ProblemType.UNKNOWN_FOR_EXPRESSION;
			if (t instanceof IArrayType) {
				t = ((IArrayType) t).getType();
			}
			ICPPEvaluation[] arguments = ICPPEvaluation.EMPTY_ARRAY;
			if (fInitializer instanceof ICPPASTConstructorInitializer) {
				IASTInitializerClause[] args = ((ICPPASTConstructorInitializer) fInitializer).getArguments();
				arguments= new ICPPEvaluation[args.length];
				for (int i = 0; i < arguments.length; i++) {
					arguments[i] = ((ICPPASTInitializerClause) args[i]).getEvaluation();
				}
			}
			fEvaluation = EvalTypeId.createForNewExpression(t, this, arguments);
		}
		return fEvaluation;
	}
	
    @Override
	public IType getExpressionType() {
    	return getEvaluation().getTypeOrFunctionSet(this);
    }

	@Override
	public boolean isLValue() {
		return false;
	}
	
	@Override
	public ValueCategory getValueCategory() {
		return PRVALUE;
	}

	@Override
	@Deprecated
	public IASTExpression[] getNewTypeIdArrayExpressions() {
		if (fCachedArraySizes == null) {
			if (fTypeId != null) {
				IASTDeclarator dtor = ASTQueries.findInnermostDeclarator(fTypeId.getAbstractDeclarator());
				if (dtor instanceof IASTArrayDeclarator) {
					IASTArrayDeclarator ad = (IASTArrayDeclarator) dtor;
					IASTArrayModifier[] ams = ad.getArrayModifiers();
					fCachedArraySizes = new IASTExpression[ams.length];
					for (int i = 0; i < ams.length; i++) {
						IASTArrayModifier am = ams[i];
						fCachedArraySizes[i] = am.getConstantExpression();
					}
					return fCachedArraySizes;
				}
			}
			fCachedArraySizes = IASTExpression.EMPTY_EXPRESSION_ARRAY;
		}
		return fCachedArraySizes;
	}

    @Override
	@Deprecated
    public void addNewTypeIdArrayExpression(IASTExpression expression) {
        assertNotFrozen();
    	Assert.isNotNull(fTypeId);
    	IASTDeclarator dtor= ASTQueries.findInnermostDeclarator(fTypeId.getAbstractDeclarator());
    	if (dtor instanceof IASTArrayDeclarator == false) {
    		Assert.isNotNull(dtor);
    		Assert.isTrue(dtor.getParent() == fTypeId);
    		IASTArrayDeclarator adtor= new CPPASTArrayDeclarator(dtor.getName());
    		IASTPointerOperator[] ptrOps= dtor.getPointerOperators();
    		for (IASTPointerOperator ptr : ptrOps) {
        		adtor.addPointerOperator(ptr);				
			}
    		fTypeId.setAbstractDeclarator(adtor);
    		dtor= adtor;
    	}
    	IASTArrayModifier mod= new CPPASTArrayModifier(expression);
    	((ASTNode) mod).setOffsetAndLength((ASTNode)expression);
    	((IASTArrayDeclarator) dtor).addArrayModifier(mod);
    }

	@Override
	@Deprecated
    public IASTExpression getNewPlacement() {
    	if (fPlacement == null || fPlacement.length == 0)
    		return null;
    	if (fPlacement.length == 1) {
    		if (fPlacement[0] instanceof IASTExpression)
    			return (IASTExpression) fPlacement[0];
    		return null;
    	}
    		
    	CASTExpressionList result= new CASTExpressionList();
    	for (IASTInitializerClause arg : fPlacement) {
    		if (arg instanceof IASTExpression) {
    			result.addExpression(((IASTExpression) arg).copy());
    		}
    	}
    	result.setParent(this);
    	result.setPropertyInParent(NEW_PLACEMENT);
        return result;
    }
	
	@Override
	@Deprecated
    public void setNewPlacement(IASTExpression expression) {
        assertNotFrozen();
        if (expression == null) {
        	setPlacementArguments(null);
        } else if (expression instanceof IASTExpressionList) {
        	setPlacementArguments(((IASTExpressionList) expression).getExpressions());
        } else {
        	setPlacementArguments(new IASTExpression[] {expression});
        }
    }
    
	@Override
	@Deprecated
    public IASTExpression getNewInitializer() {
        if (fInitializer == null || fInitializer instanceof IASTExpression) {
        	return (IASTExpression) fInitializer;
        }
        if (fInitializer instanceof ICPPASTConstructorInitializer) {
       		IASTExpression expr= ((ICPPASTConstructorInitializer) fInitializer).getExpression();
       		if (expr == null) {
       			expr= new CPPASTExpressionList();
       		} else {
       			expr= expr.copy();
       		}
       		expr.setParent(this);
       		expr.setPropertyInParent(NEW_INITIALIZER);
       		return expr;
        }
        return null;
    }

	@Override
	@Deprecated
    public void setNewInitializer(IASTExpression expression) {
        assertNotFrozen();
        if (expression == null) {
        	setInitializer(null);
        } else if (expression instanceof IASTInitializer) {
        	setInitializer((IASTInitializer) expression);
        } else {
        	CPPASTConstructorInitializer ctorInit= new CPPASTConstructorInitializer();
        	ctorInit.setExpression(expression);
        	ctorInit.setOffsetAndLength((ASTNode) expression);
        	setInitializer(ctorInit);
        }
    }
}
