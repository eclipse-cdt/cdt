/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;
import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.PRVALUE;
import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.XVALUE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.glvalueType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.prvalueType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.prvalueTypeWithResolvedTypedefs;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.typeFromFunctionCall;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.ALLCVQ;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getUltimateTypeUptoPointers;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTypeSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknownScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.OverloadableOperator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics.LookupMode;
import org.eclipse.core.runtime.CoreException;

public class EvalMemberAccess extends CPPDependentEvaluation {
	private final IType fOwnerType;
	private final IBinding fMember;
	private final ValueCategory fOwnerValueCategory;
	private final boolean fIsPointerDeref;

	private IType fType;
	private boolean fIsTypeDependent;
	private boolean fCheckedIsTypeDependent;
	private boolean fIsValueDependent;
	private boolean fCheckedIsValueDependent;

	public EvalMemberAccess(IType ownerType, ValueCategory ownerValueCat, IBinding member,
			boolean isPointerDeref, IASTNode pointOfDefinition) {
		this(ownerType, ownerValueCat, member, isPointerDeref, findEnclosingTemplate(pointOfDefinition));
	}

	public EvalMemberAccess(IType ownerType, ValueCategory ownerValueCat, IBinding member,
			boolean isPointerDeref, IBinding templateDefinition) {
		super(templateDefinition);
		fOwnerType= ownerType;
		fOwnerValueCategory= ownerValueCat;
		fMember= member;
		fIsPointerDeref= isPointerDeref;
	}

	public IType getOwnerType() {
		return fOwnerType;
	}

	public ValueCategory getOwnerValueCategory() {
		return fOwnerValueCategory;
	}

	public IBinding getMember() {
		return fMember;
	}

	public boolean isPointerDeref() {
		return fIsPointerDeref;
	}

	@Override
	public boolean isInitializerList() {
		return false;
	}

	@Override
	public boolean isFunctionSet() {
		return false;
	}

	@Override
	public boolean isTypeDependent() {
		if (!fCheckedIsTypeDependent) {
			fCheckedIsTypeDependent= true;
			fIsTypeDependent= computeIsTypeDependent();
		}
		return fIsTypeDependent;
	}

	private boolean computeIsTypeDependent() {
		IType t;
		if (fMember instanceof ICPPUnknownBinding) {
			return true;
		} else if (fMember instanceof IEnumerator) {
			t= ((IEnumerator) fMember).getType();
		} else if (fMember instanceof IVariable) {
			t = ((IVariable) fMember).getType();
		} else if (fMember instanceof IFunction) {
			t= ((IFunction) fMember).getType();
		} else {
			return false;
		}
		return CPPTemplates.isDependentType(t);
	}

	@Override
	public boolean isValueDependent() {
		if (!fCheckedIsValueDependent) {
			fCheckedIsValueDependent= true;
			fIsValueDependent= computeIsValueDependent();
		}
		return fIsValueDependent;
	}
	
	private boolean computeIsValueDependent() {
		if (fMember instanceof ICPPUnknownBinding) {
			return true;
		}
		if (fMember instanceof IEnumerator) {
			return Value.isDependentValue(((IEnumerator) fMember).getValue());
		}
		if (fMember instanceof IVariable) {
			return Value.isDependentValue(((IVariable) fMember).getInitialValue());
		}
		if (fMember instanceof IFunction) {
			return false;
		}
		return false;
	}

	@Override
	public boolean isConstantExpression(IASTNode point) {
		// TODO(nathanridge):
		//   This could be a constant expression if the field owner
		//   is a constant expression, but we don't have access to
		//   the field owner's evaluation, only its type.
		return false;
	}

	public static IType getFieldOwnerType(IType fieldOwnerExpressionType, boolean isDeref, IASTNode point, Collection<ICPPFunction> functionBindings,
			boolean returnDependent) {
    	IType type= fieldOwnerExpressionType;
    	if (!isDeref)
    		return type;

    	// Bug 205964: as long as the type is a class type, recurse.
    	// Be defensive and allow a max of 20 levels.
    	for (int j = 0; j < 20; j++) {
    		IType classType= getUltimateTypeUptoPointers(type);
    		if (!(classType instanceof ICPPClassType))
    			break;

    		IScope scope = ((ICPPClassType) classType).getCompositeScope();
    		if (scope == null || scope instanceof ICPPInternalUnknownScope)
    			break;

    		/*
    		 * 13.5.6-1: An expression x->m is interpreted as (x.operator->())->m for a
    		 * class object x of type T
    		 *
    		 * Construct an AST fragment for x.operator-> which the lookup routines can
    		 * examine for type information.
    		 */

    		ICPPEvaluation[] args= { new EvalFixed(type, LVALUE, Value.UNKNOWN) };
			ICPPFunction op= CPPSemantics.findOverloadedOperator(point, null, args, classType,
					OverloadableOperator.ARROW, LookupMode.NO_GLOBALS);
    		if (op == null)
    			break;

    		if (functionBindings != null)
    			functionBindings.add(op);

    		type= typeFromFunctionCall(op);
			type= SemanticUtil.mapToAST(type, point);
    	}

		IType prValue=  prvalueTypeWithResolvedTypedefs(type);
		if (prValue instanceof IPointerType) {
			return glvalueType(((IPointerType) prValue).getType());
		}

		if (CPPTemplates.isDependentType(type)) {
			return returnDependent 
					  // The type resulting from dereferecing 'type' 
					? new TypeOfDependentExpression(new EvalUnary(IASTUnaryExpression.op_star, 
							new EvalFixed(type, LVALUE, Value.UNKNOWN), null, point)) 
					: null;
		}

		return ProblemType.UNKNOWN_FOR_EXPRESSION;
	}

	@Override
	public IType getTypeOrFunctionSet(IASTNode point) {
		if (fType == null) {
			fType= computeType(point);
		}
		return fType;
	}

	private IType computeType(IASTNode point) {
		if (fMember instanceof ICPPUnknownBinding) {
			return new TypeOfDependentExpression(this);
		}
		if (fMember instanceof IEnumerator) {
			return ((IEnumerator) fMember).getType();
		}
		if (fMember instanceof IVariable) {
			IType e2 = ((IVariable) fMember).getType();
			e2= SemanticUtil.getNestedType(e2, TDEF);
			if (e2 instanceof ICPPReferenceType) {
				e2= glvalueType(e2);
			} else if (fMember instanceof ICPPField && !((ICPPField) fMember).isStatic()) {
				e2 = addQualifiersForAccess((ICPPField) fMember, e2, fOwnerType);
				if (!fIsPointerDeref && fOwnerValueCategory == PRVALUE) {
					e2= prvalueType(e2);
				} else {
					e2= glvalueType(e2);
				}
			}
		    return SemanticUtil.mapToAST(e2, point);
		}
		if (fMember instanceof IFunction) {
			return SemanticUtil.mapToAST(((IFunction) fMember).getType(), point);
		}
		return ProblemType.UNKNOWN_FOR_EXPRESSION;
	}

	private IType addQualifiersForAccess(ICPPField field, IType fieldType, IType ownerType) {
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
	public IValue getValue(IASTNode point) {
		if (fMember instanceof IEnumerator) {
			return ((IEnumerator) fMember).getValue();
		}
		if (fMember instanceof IVariable) {
			IValue initialValue = ((IVariable) fMember).getInitialValue();
			return initialValue == null ? Value.UNKNOWN : initialValue;
		}
		if (fMember instanceof IFunction) {
			return Value.UNKNOWN;
		}
		return Value.create(this);
	}

	@Override
	public ValueCategory getValueCategory(IASTNode point) {
		if (fMember instanceof IVariable) {
			IType e2= ((IVariable) fMember).getType();
			e2= SemanticUtil.getNestedType(e2, TDEF);
			if (e2 instanceof ICPPReferenceType) {
				return LVALUE;
			}
			if (fMember instanceof ICPPField && !((ICPPField) fMember).isStatic()) {
				if (fIsPointerDeref)
					return LVALUE;

				return fOwnerValueCategory;
			}
			return LVALUE;
		}
		if (fMember instanceof IFunction) {
			return LVALUE;
		}
		return PRVALUE;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		short firstBytes = ITypeMarshalBuffer.EVAL_MEMBER_ACCESS;
		if (fIsPointerDeref)
			firstBytes |= ITypeMarshalBuffer.FLAG1;
		if (fOwnerValueCategory == LVALUE) {
			firstBytes |= ITypeMarshalBuffer.FLAG2;
		} else if (fOwnerValueCategory == XVALUE) {
			firstBytes |= ITypeMarshalBuffer.FLAG3;
		}

		buffer.putShort(firstBytes);
		buffer.marshalType(fOwnerType);
		buffer.marshalBinding(fMember);
		marshalTemplateDefinition(buffer);
	}

	public static ISerializableEvaluation unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		boolean isDeref= (firstBytes & ITypeMarshalBuffer.FLAG1) != 0;
		ValueCategory ownerValueCat;
		if ((firstBytes & ITypeMarshalBuffer.FLAG2) != 0) {
			ownerValueCat= LVALUE;
		} else if ((firstBytes & ITypeMarshalBuffer.FLAG3) != 0) {
			ownerValueCat= XVALUE;
		} else {
			ownerValueCat= PRVALUE;
		}

		IType ownerType= buffer.unmarshalType();
		IBinding member= buffer.unmarshalBinding();
		IBinding templateDefinition= buffer.unmarshalBinding();
		return new EvalMemberAccess(ownerType, ownerValueCat, member, isDeref, templateDefinition);
	}

	@Override
	public ICPPEvaluation instantiate(ICPPTemplateParameterMap tpMap, int packOffset,
			ICPPTypeSpecialization within, int maxdepth, IASTNode point) {
		IType ownerType = CPPTemplates.instantiateType(fOwnerType, tpMap, packOffset, within, point);
		if (ownerType == fOwnerType)
			return this;

		IBinding member = fMember;
		IType ownerClass = SemanticUtil.getNestedType(ownerType, ALLCVQ);
		if (ownerClass instanceof ICPPClassSpecialization) {
			member = CPPTemplates.createSpecialization((ICPPClassSpecialization) ownerClass, fMember, point);
		}
		return new EvalMemberAccess(ownerType, fOwnerValueCategory, member, fIsPointerDeref, getTemplateDefinition());
	}

	@Override
	public ICPPEvaluation computeForFunctionCall(CPPFunctionParameterMap parameterMap,
			ConstexprEvaluationContext context) {
		return this;
	}

	@Override
	public int determinePackSize(ICPPTemplateParameterMap tpMap) {
		return CPPTemplates.determinePackSize(fOwnerType, tpMap);
	}

	@Override
	public boolean referencesTemplateParameter() {
		return false;
	}
}
