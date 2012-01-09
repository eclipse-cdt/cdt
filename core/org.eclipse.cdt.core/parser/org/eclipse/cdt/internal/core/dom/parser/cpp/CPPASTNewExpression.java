/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Mike Kucera (IBM)
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
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTExpressionList;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.Assert;


public class CPPASTNewExpression extends ASTNode implements ICPPASTNewExpression, IASTAmbiguityParent {
    private IASTInitializerClause[] placement;
    private IASTTypeId typeId;
    private IASTInitializer initializer;
    private IASTImplicitName[] implicitNames = null;
    private boolean isGlobal;
    private boolean isNewTypeId;
	
    private IASTExpression[] cachedArraySizes;
    
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
		copy.setIsGlobal(isGlobal);
		copy.setIsNewTypeId(isNewTypeId);
		if (placement != null) {
			IASTInitializerClause[] plcmt = new IASTInitializerClause[placement.length];
			for (int i = 0; i < placement.length; i++) {
				plcmt[i] = placement[i].copy(style);
			}
			copy.setPlacementArguments(plcmt);
		}
		copy.setTypeId(typeId == null ? null : typeId.copy(style));
		copy.setInitializer(initializer == null ? null : initializer.copy(style));
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	@Override
	public boolean isGlobal() {
        return isGlobal;
    }

    @Override
	public void setIsGlobal(boolean value) {
        assertNotFrozen();
        isGlobal = value;
    }

    @Override
	public IASTInitializerClause[] getPlacementArguments() {
    	return placement;
    }
    
    @Override
	public void setPlacementArguments(IASTInitializerClause[] args) {
        assertNotFrozen();
        placement = args;
        if (args != null) {
        	for (IASTInitializerClause arg : args) {
				arg.setParent(this);
				arg.setPropertyInParent(NEW_PLACEMENT);
			}
		}
    }

    @Override
	public IASTInitializer getInitializer() {
        return initializer;
    }

    @Override
	public void setInitializer(IASTInitializer expression) {
        assertNotFrozen();
        initializer = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(NEW_INITIALIZER);
		}
    }

    @Override
	public IASTTypeId getTypeId() {
        return typeId;
    }

    @Override
	public void setTypeId(IASTTypeId typeId) {
        assertNotFrozen();
        this.typeId = typeId;
        if (typeId != null) {
			typeId.setParent(this);
			typeId.setPropertyInParent(TYPE_ID);
		}
    }

    @Override
	public boolean isNewTypeId() {
        return isNewTypeId;
    }

    @Override
	public void setIsNewTypeId(boolean value) {
        assertNotFrozen();
        isNewTypeId = value;
    }
    
    /**
     * @see org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner#getImplicitNames()
     */
    @Override
	public IASTImplicitName[] getImplicitNames() {
    	if (implicitNames == null) {
			ICPPFunction operatorFunction = CPPSemantics.findOverloadedOperator(this);
			if (operatorFunction == null || operatorFunction instanceof CPPImplicitFunction) {
				implicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
			} else {
				CPPASTImplicitName operatorName = new CPPASTImplicitName(operatorFunction.getNameCharArray(), this);
				operatorName.setOperator(true);
				operatorName.setBinding(operatorFunction);
				operatorName.setOffsetAndLength(getOffset(), 3);
				implicitNames = new IASTImplicitName[] { operatorName };
			}
    	}
    	
    	return implicitNames;  
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
        
		if (placement != null) {
			for (IASTInitializerClause arg : placement) {
				if (!arg.accept(action))
					return false;
			}
		}
		if (typeId != null && !typeId.accept(action))
			return false;

		if (initializer != null && !initializer.accept(action))
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
		if (placement != null) {
			for (int i = 0; i < placement.length; ++i) {
				if (child == placement[i]) {
					other.setPropertyInParent(child.getPropertyInParent());
					other.setParent(child.getParent());
					placement[i] = (IASTExpression) other;
				}
			}
		}
	}
    
    @Override
	public IType getExpressionType() {
		IType t= CPPVisitor.createType(getTypeId());
		if (t instanceof IArrayType) {
			t= ((IArrayType) t).getType();
		}
		return new CPPPointerType(t);
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
		if (cachedArraySizes == null) {
			if (typeId != null) {
				IASTDeclarator dtor = ASTQueries.findInnermostDeclarator(typeId.getAbstractDeclarator());
				if (dtor instanceof IASTArrayDeclarator) {
					IASTArrayDeclarator ad = (IASTArrayDeclarator) dtor;
					IASTArrayModifier[] ams = ad.getArrayModifiers();
					cachedArraySizes = new IASTExpression[ams.length];
					for (int i = 0; i < ams.length; i++) {
						IASTArrayModifier am = ams[i];
						cachedArraySizes[i] = am.getConstantExpression();
					}
					return cachedArraySizes;
				}
			}
			cachedArraySizes = IASTExpression.EMPTY_EXPRESSION_ARRAY;
		}
		return cachedArraySizes;
	}

    @Override
	@Deprecated
    public void addNewTypeIdArrayExpression(IASTExpression expression) {
        assertNotFrozen();
    	Assert.isNotNull(typeId);
    	IASTDeclarator dtor= ASTQueries.findInnermostDeclarator(typeId.getAbstractDeclarator());
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
	@Deprecated
    public IASTExpression getNewPlacement() {
    	if (placement == null || placement.length == 0)
    		return null;
    	if (placement.length == 1) {
    		if (placement[0] instanceof IASTExpression)
    			return (IASTExpression) placement[0];
    		return null;
    	}
    		
    	CASTExpressionList result= new CASTExpressionList();
    	for (IASTInitializerClause arg : placement) {
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
        if (initializer == null || initializer instanceof IASTExpression) {
        	return (IASTExpression) initializer;
        }
        if (initializer instanceof ICPPASTConstructorInitializer) {
       		IASTExpression expr= ((ICPPASTConstructorInitializer) initializer).getExpression();
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
