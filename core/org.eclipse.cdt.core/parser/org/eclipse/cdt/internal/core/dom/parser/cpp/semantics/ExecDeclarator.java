/*******************************************************************************
* Copyright (c) 2016 Institute for Software, HSR Hochschule fuer Technik
* Rapperswil, University of applied sciences and others
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.CompositeValue;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation.ConstexprEvaluationContext;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPExecution;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.core.runtime.CoreException;

public final class ExecDeclarator implements ICPPExecution {
	private final ICPPBinding declaredBinding;
	private final ICPPEvaluation initializerEval;

	public ExecDeclarator(ICPPBinding declaredBinding, ICPPEvaluation initializerEval) {
		this.declaredBinding = declaredBinding;
		this.initializerEval = initializerEval;
	}

	@Override
	public ICPPExecution executeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		if (!(declaredBinding instanceof ICPPVariable))
			return this;

		ICPPVariable declaredVariable = (ICPPVariable) declaredBinding;
		IType type = declaredVariable.getType();
		ICPPEvaluation initialValue = createInitialValue(type, record, context);
		if (initialValue == null || initialValue == EvalFixed.INCOMPLETE)
			return ExecIncomplete.INSTANCE;

		record.update(declaredBinding, initialValue);
		return this;
	}

	public ICPPBinding getDeclaredBinding() {
		return declaredBinding;
	}

	private static ICPPEvaluation maybeUnwrapInitList(ICPPEvaluation eval, IType targetType) {
		// Only 1-element initializer lists are eligible for unwrapping.
		if (!(eval instanceof EvalInitList))
			return eval;

		EvalInitList initList = (EvalInitList) eval;
		ICPPEvaluation[] clauses = initList.getClauses();
		if (clauses.length != 1)
			return eval;

		// Never unwrap initializers for array types.
		if (targetType instanceof IArrayType)
			return eval;

		// Only unwrap initializers for class types if the type of the initializer
		// element matches the class type, indicating that we're calling the
		// implicit copy constructor (as opposed to doing memberwise initialization).
		ICPPEvaluation clause = clauses[0];
		if (targetType instanceof ICPPClassType && !clause.getType().isSameType(targetType))
			return eval;

		// Otherwise unwrap.
		return clause;
	}

	private ICPPEvaluation createInitialValue(IType type, ActivationRecord record, ConstexprEvaluationContext context) {
		if (initializerEval == null)
			return createDefaultInitializedCompositeValue(type);

		IType nestedType = SemanticUtil.getNestedType(type, TDEF | REF | CVTYPE);

		ICPPEvaluation computedInitializerEval = initializerEval.computeForFunctionCall(record, context.recordStep());

		// In some contexts, unwrap 1-element initializer lists.
		computedInitializerEval = maybeUnwrapInitList(computedInitializerEval, nestedType);

		if (type instanceof ICPPReferenceType)
			return createReferenceValue(record, context, computedInitializerEval);

		if (nestedType instanceof IPointerType && !isCStringType(nestedType))
			return createPointerValue(record, context, computedInitializerEval);

		if (nestedType instanceof IArrayType && !isCStringType(nestedType)) {
			if (computedInitializerEval instanceof EvalInitList) {
				IValue value = CompositeValue.create((EvalInitList) computedInitializerEval, (IArrayType) nestedType);
				return new EvalFixed(type, computedInitializerEval.getValueCategory(), value);
			}
			// TODO(sprigogin): Should something else be done here?
			return EvalFixed.INCOMPLETE;
		}

		if (isValueInitialization(computedInitializerEval)) {
			ICPPEvaluation defaultValue = new EvalTypeId(type, computedInitializerEval.getTemplateDefinition(), false,
					false, ICPPEvaluation.EMPTY_ARRAY);
			return new EvalFixed(type, defaultValue.getValueCategory(), defaultValue.getValue());
		}

		return new EvalFixed(type, computedInitializerEval.getValueCategory(), computedInitializerEval.getValue());
	}

	private static ICPPEvaluation createDefaultInitializedCompositeValue(IType type) {
		if (!(type instanceof ICPPClassType)) {
			return EvalFixed.INCOMPLETE;
		}
		ICPPClassType classType = (ICPPClassType) type;
		// TODO(nathanridge): CompositeValue.create() only consider default member initializers, not
		// constructors. Should we be considering constructors here as well?
		IValue compositeValue = CompositeValue.create(classType);
		EvalFixed initialValue = new EvalFixed(type, ValueCategory.PRVALUE, compositeValue);
		return initialValue;
	}

	private ICPPEvaluation createReferenceValue(ActivationRecord record, ConstexprEvaluationContext context,
			ICPPEvaluation computedInitializerEval) {
		ICPPEvaluation initValue = initializerEval;
		if (initValue instanceof EvalInitList) {
			initValue = ((EvalInitList) initValue).getClauses()[0];
		} else if (!(initValue instanceof EvalBinding)) {
			initValue = initializerEval.getValue().getSubValue(0);
		}

		IBinding templateDefinition = initializerEval.getTemplateDefinition();
		if (initValue instanceof EvalBinding)
			return createReferenceFromBinding(record, templateDefinition, (EvalBinding) initValue);

		if (initValue instanceof EvalBinary && computedInitializerEval instanceof EvalCompositeAccess)
			return createReferenceFromCompositeAccess(record, templateDefinition,
					(EvalCompositeAccess) computedInitializerEval);

		return EvalFixed.INCOMPLETE;
	}

	private ICPPEvaluation createPointerValue(ActivationRecord record, ConstexprEvaluationContext context,
			ICPPEvaluation computedInitializerEval) {
		ICPPEvaluation initValue = initializerEval.getValue().getSubValue(0);
		if (isPointerToArray(initValue)) {
			EvalCompositeAccess arrayPointer = new EvalCompositeAccess(computedInitializerEval, 0);
			return createPointerFromCompositeAccess(record, initializerEval.getTemplateDefinition(), arrayPointer);
		}

		if (computedInitializerEval instanceof EvalPointer)
			return ((EvalPointer) computedInitializerEval).copy();

		return EvalFixed.INCOMPLETE;
	}

	private static boolean isValueInitialization(ICPPEvaluation eval) {
		if (eval instanceof EvalInitList) {
			EvalInitList evalInitList = (EvalInitList) eval;
			return evalInitList.getClauses().length == 0;
		}

		return false;
	}

	private static boolean isPointerToArray(ICPPEvaluation eval) {
		return eval.getType() instanceof IArrayType;
	}

	private static ICPPEvaluation createReferenceFromBinding(ActivationRecord record, IBinding templateDefinition,
			EvalBinding evalBinding) {
		return new EvalReference(record, evalBinding.getBinding(), templateDefinition);
	}

	private static ICPPEvaluation createReferenceFromCompositeAccess(ActivationRecord record,
			IBinding templateDefinition, EvalCompositeAccess evalCompAccess) {
		return new EvalReference(record, evalCompAccess, templateDefinition);
	}

	private static ICPPEvaluation createPointerFromCompositeAccess(ActivationRecord record, IBinding templateDefinition,
			EvalCompositeAccess evalCompAccess) {
		return new EvalPointer(record, evalCompAccess, templateDefinition);
	}

	private static boolean isCStringType(IType type) {
		IType nestedType = null;
		if (type instanceof IArrayType) {
			nestedType = ((IArrayType) type).getType();
		} else if (type instanceof IPointerType) {
			nestedType = ((IPointerType) type).getType();
		}

		if (nestedType instanceof IQualifierType) {
			IQualifierType qualifierType = (IQualifierType) nestedType;
			if (qualifierType.isConst() && !qualifierType.isVolatile())
				return qualifierType.getType().isSameType(CPPBasicType.CHAR);
		}
		return false;
	}

	@Override
	public ICPPExecution instantiate(InstantiationContext context, int maxDepth) {
		ICPPBinding newDeclaredBinding;
		if (declaredBinding instanceof ICPPVariable) {
			ICPPVariable declaredVariable = (ICPPVariable) declaredBinding;
			newDeclaredBinding = CPPTemplates.createVariableSpecialization(context, declaredVariable);
		} else {
			ICPPSpecialization owner = context.getContextSpecialization();
			if (owner instanceof ICPPClassSpecialization) {
				newDeclaredBinding = (ICPPBinding) ((ICPPClassSpecialization) owner).specializeMember(declaredBinding);
			} else {
				// TODO: Non-class owners should also have a specializeMember() function which
				//       implements a caching mechanism.
				newDeclaredBinding = (ICPPBinding) CPPTemplates.createSpecialization(owner, declaredBinding);
			}
		}

		ICPPEvaluation newInitializerEval = initializerEval == null ? null
				: initializerEval.instantiate(context, maxDepth);
		return new ExecDeclarator(newDeclaredBinding, newInitializerEval);
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.EXEC_DECLARATOR);
		buffer.marshalBinding(declaredBinding);
		buffer.marshalEvaluation(initializerEval, includeValue);
	}

	public static ICPPExecution unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		IBinding declaredBinding = buffer.unmarshalBinding();
		if (declaredBinding instanceof IProblemBinding) {
			// The declared binding could not be stored in the index.
			// If this happens, it's almost certainly a bug, but the severity
			// is mitigated by returning a problem evaluation instead of just
			// trying to cast to ICPPBinding and throwing a ClassCastException.
			return ExecIncomplete.INSTANCE;
		}
		ICPPEvaluation initializerEval = buffer.unmarshalEvaluation();
		return new ExecDeclarator((ICPPBinding) declaredBinding, initializerEval);
	}
}
