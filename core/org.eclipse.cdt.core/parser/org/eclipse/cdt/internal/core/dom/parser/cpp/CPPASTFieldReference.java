/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *     Bryan Wilkinson (QNX)
 *     Mike Kucera (IBM)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;
import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.glvalueType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.prvalueType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.typeFromFunctionCall;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICPPASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CVQualifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

public class CPPASTFieldReference extends ASTNode implements ICPPASTFieldReference, IASTAmbiguityParent,
		ICPPASTCompletionContext {

    private boolean isTemplate;
    private IASTExpression owner;
    private IASTName name;
    private boolean isDeref;
    
    private IASTImplicitName[] implicitNames = null;
    
    public CPPASTFieldReference() {
	}
	
	public CPPASTFieldReference(IASTName name, IASTExpression owner) {
		setFieldName(name);
		setFieldOwner(owner);
	}
	
	@Override
	public CPPASTFieldReference copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTFieldReference copy(CopyStyle style) {
		CPPASTFieldReference copy = new CPPASTFieldReference();
		copy.setFieldName(name == null ? null : name.copy(style));
		copy.setFieldOwner(owner == null ? null : owner.copy(style));
		copy.isTemplate = isTemplate;
		copy.isDeref = isDeref;
		copy.setOffsetAndLength(this);
		if (style == CopyStyle.withLocations) {
			copy.setCopyLocation(this);
		}
		return copy;
	}

	@Override
	public boolean isTemplate() {
        return isTemplate;
    }

    @Override
	public void setIsTemplate(boolean value) {
        assertNotFrozen();
        isTemplate = value;
    }

    @Override
	public IASTExpression getFieldOwner() {
        return owner;
    }

    @Override
	public void setFieldOwner(IASTExpression expression) {
        assertNotFrozen();
        owner = expression;
        if (expression != null) {
			expression.setParent(this);
			expression.setPropertyInParent(FIELD_OWNER);
		}
    }

    @Override
	public IASTName getFieldName() {
        return name;
    }

    @Override
	public void setFieldName(IASTName name) {
        assertNotFrozen();
        this.name = name;
        if (name != null) {
			name.setParent(this);
			name.setPropertyInParent(FIELD_NAME);
		}
    }

    @Override
	public boolean isPointerDereference() {
        return isDeref;
    }

    @Override
	public void setIsPointerDereference(boolean value) {
        assertNotFrozen();
        isDeref = value;
    }
    
    @Override
	public IASTImplicitName[] getImplicitNames() {
    	if (implicitNames == null) {
    		if (!isDeref)
    			return implicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
			
    		// collect the function bindings
			List<ICPPFunction> functionBindings = new ArrayList<ICPPFunction>();
			getFieldOwnerType(functionBindings);
			if (functionBindings.isEmpty())
				return implicitNames = IASTImplicitName.EMPTY_NAME_ARRAY;
			
			// create a name to wrap each binding
			implicitNames = new IASTImplicitName[functionBindings.size()];
			int i=-1;
			for (ICPPFunction op : functionBindings) {
				if (op != null && !(op instanceof CPPImplicitFunction)) {
					CPPASTImplicitName operatorName = new CPPASTImplicitName(OverloadableOperator.ARROW, this);
					operatorName.setBinding(op);
					operatorName.computeOperatorOffsets(owner, true);
					implicitNames[++i] = operatorName;
				}
			}
			implicitNames= ArrayUtil.trimAt(IASTImplicitName.class, implicitNames, i);
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
      
        if (owner != null && !owner.accept(action))
        	return false;
        
        if (action.shouldVisitImplicitNames) { 
        	for (IASTImplicitName name : getImplicitNames()) {
        		if (!name.accept(action))
        			return false;
        	}
        }
        
        if (name != null && !name.accept(action))
        	return false;
        
        if (action.shouldVisitExpressions) {
		    switch (action.leave(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        return true;
    }

	@Override
	public int getRoleForName(IASTName n) {
		if (n == name)
			return r_reference;
		return r_unclear;
	}

    @Override
	public void replace(IASTNode child, IASTNode other) {
        if (child == owner) {
            other.setPropertyInParent(child.getPropertyInParent());
            other.setParent(child.getParent());
            owner  = (IASTExpression) other;
        }
    }

    @Override
	public IType getExpressionType() {
		IASTName name= getFieldName();
		IBinding binding = name.resolvePreBinding();
		try {
			if (binding instanceof IVariable) {
				IType e2= ((IVariable) binding).getType();
				e2= SemanticUtil.getNestedType(e2, TDEF);
				if (e2 instanceof ICPPReferenceType) {
					e2= glvalueType(e2);
				} else if (binding instanceof ICPPField && !((ICPPField) binding).isStatic()) {
					IType e1= getFieldOwner().getExpressionType();
					if (isPointerDereference()) {
						e1= SemanticUtil.getNestedType(e1, TDEF | REF | CVTYPE);
						if (e1 instanceof IPointerType) {
							e1= ((IPointerType) e1).getType();
						}
					}
					e2 = addQualifiersForAccess((ICPPField) binding, e2, e1);
					if (!isPointerDereference() && owner.getValueCategory() == PRVALUE) {
						e2= prvalueType(e2);
					} else {
						e2= glvalueType(e2);
					}
				} 
                return SemanticUtil.mapToAST(e2, this);
			} 
			if (binding instanceof IEnumerator) {
				return ((IEnumerator) binding).getType();
			} 
			if (binding instanceof IFunction) {
				return SemanticUtil.mapToAST(((IFunction) binding).getType(), this);
			}  
			if (binding instanceof ICPPUnknownBinding) {
				// mstodo type of unknown.
				return CPPUnknownClass.createUnnamedInstance();
			} 
			if (binding instanceof IProblemBinding) {
				return new ProblemType(ISemanticProblem.TYPE_UNRESOLVED_NAME);
			} 
			return new ProblemType(ISemanticProblem.TYPE_UNKNOWN_FOR_EXPRESSION);
	    } catch (DOMException e) {
	        return e.getProblem();
        }
    }
    
	public static IType addQualifiersForAccess(ICPPField field, IType fieldType, IType ownerType) {
		CVQualifier cvq1 = SemanticUtil.getCVQualifier(ownerType);
		CVQualifier cvq2 = SemanticUtil.getCVQualifier(fieldType);
		if (field.isMutable()) {
			// Remove const, add union of volatile.
			if (cvq2.isConst()) {
				fieldType= SemanticUtil.getNestedType(fieldType, ALLCVQ | TDEF | REF);
			}
			fieldType= SemanticUtil.addQualifiers(fieldType, false, cvq1.isVolatile() || cvq2.isVolatile(), cvq2.isRestrict());
		} else {
			fieldType= SemanticUtil.addQualifiers(fieldType, cvq1.isConst(), cvq1.isVolatile(), cvq2.isRestrict());
		}
		return fieldType;
	}

    
	@Override
	public ValueCategory getValueCategory() {
		IASTName name= getFieldName();
		IBinding binding = name.resolvePreBinding();
		if (binding instanceof IVariable) {
			IType e2= ((IVariable) binding).getType();
			e2= SemanticUtil.getNestedType(e2, TDEF);
			if (e2 instanceof ICPPReferenceType) {
				return LVALUE;
			} 
			if (binding instanceof ICPPField && !((ICPPField) binding).isStatic()) {
				if (isPointerDereference())
					return LVALUE;

				return owner.getValueCategory();
			}
			return LVALUE;
		} 
		if (binding instanceof IFunction) {
			return LVALUE;
		}
		return PRVALUE;
	}
	
	@Override
	public boolean isLValue() {
		return getValueCategory() == LVALUE;
	}

	@Override
	public IBinding[] findBindings(IASTName n, boolean isPrefix, String[] namespaces) {
		IBinding[] bindings = CPPSemantics.findBindingsForContentAssist(n, isPrefix, namespaces);
		List<IBinding> filtered = new ArrayList<IBinding>();

		for (IBinding binding : bindings) {
			if (binding instanceof ICPPMethod) {
				ICPPMethod method = (ICPPMethod) binding;
				if (method.isImplicit()) {
					continue;
				}
			}
			filtered.add(binding);
		}
		
		return filtered.toArray(new IBinding[filtered.size()]);
	}
	
	@Override
	public IBinding[] findBindings(IASTName n, boolean isPrefix) {
		return findBindings(n, isPrefix, null);
	}
	
    /**
     * For a pointer dereference expression e1->e2, return the type that e1 ultimately evaluates to
     * after chaining overloaded class member access operators <code>operator->()</code> calls.
     */
    @Override
	public IType getFieldOwnerType() {
    	return getFieldOwnerType(null);
    }
    
    /*
     * Also collects the function bindings if requested.
     */
    private IType getFieldOwnerType(Collection<ICPPFunction> functionBindings) {
    	final IASTExpression owner = getFieldOwner();
    	if (owner == null)
    		return null;
    	
    	IType type= owner.getExpressionType();
    	if (!isPointerDereference())
    		return type;
    	
    	// bug 205964: as long as the type is a class type, recurse. 
    	// Be defensive and allow a max of 20 levels.
    	for (int j = 0; j < 20; j++) {
    		// for unknown types we cannot determine the overloaded -> operator
    		IType classType= getUltimateTypeUptoPointers(type);
    		if (classType instanceof ICPPUnknownType)
    			return CPPUnknownClass.createUnnamedInstance();

    		if (!(classType instanceof ICPPClassType)) 
    			break;
    		
    		/*
    		 * 13.5.6-1: An expression x->m is interpreted as (x.operator->())->m for a
    		 * class object x of type T
    		 * 
    		 * Construct an AST fragment for x.operator-> which the lookup routines can
    		 * examine for type information.
    		 */

    		ICPPFunction op = CPPSemantics.findOverloadedOperator(this, type, (ICPPClassType) classType);
    		if (op == null) 
    			break;

    		if (functionBindings != null)
    			functionBindings.add(op);
    		
    		type= typeFromFunctionCall(op);
			type= SemanticUtil.mapToAST(type, owner);
    	}
    	
		IType prValue=  prvalueType(type);
		if (prValue instanceof IPointerType) {
			return glvalueType(((IPointerType) prValue).getType());
		}

		return new ProblemType(ISemanticProblem.TYPE_UNKNOWN_FOR_EXPRESSION);
    }
}
