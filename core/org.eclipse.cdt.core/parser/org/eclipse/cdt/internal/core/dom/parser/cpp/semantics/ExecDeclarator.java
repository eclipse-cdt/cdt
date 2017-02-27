/*******************************************************************************
* Copyright (c) 2016 Institute for Software, HSR Hochschule fuer Technik
* Rapperswil, University of applied sciences and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
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

	private static ICPPEvaluation maybeUnwrapInitList(ICPPEvaluation eval, IType targetType, IASTNode point) {
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
		if (targetType instanceof ICPPClassType && !clause.getType(point).isSameType(targetType))
			return eval;
		
		// Otherwise unwrap.
		return clause;
	}
	
	private ICPPEvaluation createInitialValue(IType type, ActivationRecord record,
			ConstexprEvaluationContext context) {
		if (initializerEval == null)
			return createDefaultInitializedCompositeValue(type, context.getPoint());

		IType nestedType = SemanticUtil.getNestedType(type, TDEF | REF | CVTYPE);

		ICPPEvaluation computedInitializerEval =
				initializerEval.computeForFunctionCall(record, context.recordStep());

		// In some contexts, unwrap 1-element initializer lists.
		computedInitializerEval = maybeUnwrapInitList(computedInitializerEval, nestedType, context.getPoint());

		if (type instanceof ICPPReferenceType)
			return createReferenceValue(record, context, computedInitializerEval);

		if (nestedType instanceof IPointerType && !isCStringType(nestedType))
			return createPointerValue(record, context, computedInitializerEval);

		if (nestedType instanceof IArrayType && !isCStringType(nestedType)) {
			if (computedInitializerEval instanceof EvalInitList) {
				IValue value = CompositeValue.create((EvalInitList) computedInitializerEval, 
						(IArrayType) nestedType, context.getPoint());
				return new EvalFixed(type, computedInitializerEval.getValueCategory(context.getPoint()), value);
			}
			// TODO(sprigogin): Should something else be done here?
			return EvalFixed.INCOMPLETE;
		}

		if (isValueInitialization(computedInitializerEval)) {
			ICPPEvaluation defaultValue =
					new EvalTypeId(type, context.getPoint(), false, ICPPEvaluation.EMPTY_ARRAY);
			return new EvalFixed(type, defaultValue.getValueCategory(context.getPoint()),
					defaultValue.getValue(context.getPoint()));
		}

		return new EvalFixed(type, computedInitializerEval.getValueCategory(context.getPoint()),
				computedInitializerEval.getValue(context.getPoint()));
	}

	private static ICPPEvaluation createDefaultInitializedCompositeValue(IType type, IASTNode point) {
		if (!(type instanceof ICPPClassType)) {
			return EvalFixed.INCOMPLETE;
		}
		ICPPClassType classType = (ICPPClassType) type;
		// TODO(nathanridge): CompositeValue.create() only consider default member initializers, not
		// constructors. Should we be considering constructors here as well?
		IValue compositeValue = CompositeValue.create(classType, point);
		EvalFixed initialValue = new EvalFixed(type, ValueCategory.PRVALUE, compositeValue);
		return initialValue;
	}

	private ICPPEvaluation createReferenceValue(ActivationRecord record, ConstexprEvaluationContext context,
			ICPPEvaluation computedInitializerEval) {
		ICPPEvaluation initValue = initializerEval;
		if (initValue instanceof EvalInitList) {
			initValue = ((EvalInitList) initValue).getClauses()[0];
		} else if (!(initValue instanceof EvalBinding)) {
			initValue = initializerEval.getValue(context.getPoint()).getSubValue(0);
		}

		if (initValue instanceof EvalBinding)
			return createReferenceFromBinding(record, context, (EvalBinding) initValue);

		if (initValue instanceof EvalBinary && computedInitializerEval instanceof EvalCompositeAccess)
			return createReferenceFromCompositeAccess(record, context, (EvalCompositeAccess) computedInitializerEval);

		return EvalFixed.INCOMPLETE;
	}

	private ICPPEvaluation createPointerValue(ActivationRecord record, ConstexprEvaluationContext context,
			ICPPEvaluation computedInitializerEval) {
		ICPPEvaluation initValue = initializerEval.getValue(context.getPoint()).getSubValue(0);
		if (isPointerToArray(initValue, context)) {
			EvalCompositeAccess arrayPointer = new EvalCompositeAccess(computedInitializerEval, 0);
			return createPointerFromCompositeAccess(record, context, arrayPointer);
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

	private static boolean isPointerToArray(ICPPEvaluation eval, ConstexprEvaluationContext context) {
		return eval.getType(context.getPoint()) instanceof IArrayType;
	}

	private static ICPPEvaluation createReferenceFromBinding(ActivationRecord record,
			ConstexprEvaluationContext context, EvalBinding evalBinding) {
		return new EvalReference(record, evalBinding.getBinding(), context.getPoint());
	}

	private static ICPPEvaluation createReferenceFromCompositeAccess(ActivationRecord record,
			ConstexprEvaluationContext context, EvalCompositeAccess evalCompAccess) {
		return new EvalReference(record, evalCompAccess, context.getPoint());
	}

	private static ICPPEvaluation createPointerFromCompositeAccess(ActivationRecord record,
			ConstexprEvaluationContext context, EvalCompositeAccess evalCompAccess) {
		return new EvalPointer(record, evalCompAccess, context.getPoint());
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
			newDeclaredBinding = (ICPPBinding) CPPTemplates.createSpecialization(
					context.getContextSpecialization(), declaredBinding, context.getPoint());
		}

		ICPPEvaluation newInitializerEval =
				initializerEval == null ? null : initializerEval.instantiate(context, maxDepth);
		return new ExecDeclarator(newDeclaredBinding, newInitializerEval);
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.EXEC_DECLARATOR);
		buffer.marshalBinding(declaredBinding);
		buffer.marshalEvaluation(initializerEval, includeValue);
	}

	public static ICPPExecution unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		ICPPBinding declaredBinding = (ICPPBinding) buffer.unmarshalBinding();
		ICPPEvaluation initializerEval = buffer.unmarshalEvaluation();
		return new ExecDeclarator(declaredBinding, initializerEval);
	}
}
