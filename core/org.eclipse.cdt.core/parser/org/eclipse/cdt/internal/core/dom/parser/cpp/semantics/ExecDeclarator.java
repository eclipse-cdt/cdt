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
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.CompositeValue;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableExecution;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPQualifierType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation.ConstexprEvaluationContext;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPExecution;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.core.runtime.CoreException;

public class ExecDeclarator implements ICPPExecution {
	private final ICPPBinding declaredBinding;
	private final ICPPEvaluation initializerEval;

	public ExecDeclarator(ICPPBinding declaredBinding, ICPPEvaluation initializerEval) {
		this.declaredBinding = declaredBinding;
		this.initializerEval = initializerEval;
	}

	@Override
	public ICPPExecution executeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		if(!(declaredBinding instanceof ICPPVariable)) {
			return this;
		}
		
		ICPPVariable declaredVariable = (ICPPVariable)declaredBinding; 
		IType type = declaredVariable.getType();
		ICPPEvaluation initialValue = createInitialValue(type, record, context);
		if(initialValue == null || initialValue == EvalFixed.INCOMPLETE) {
			return ExecIncomplete.INSTANCE;
		}
		
		record.update(declaredBinding, initialValue);
		return this;
	}
	
	public ICPPBinding getDeclaredBinding() {
		return declaredBinding;
	}
	
	private ICPPEvaluation createInitialValue(IType type, ActivationRecord record, ConstexprEvaluationContext context) {
		if(initializerEval == null) {
			return createDefaultInitializedCompositeValue(type);
		}
		
		ICPPEvaluation computedInitializerEval = initializerEval.computeForFunctionCall(record, context.recordStep());

		//if a compositevalue with only one member is initialized with an initializer list
		//it evaluates to a EvalFixed with a Value instead of a CompositeValue because the initializer list 
		//doesn't know that it is initializing a composite. 
		IType nestedType = SemanticUtil.getNestedType(type, TDEF | REF | CVTYPE);
		if(isClassType(nestedType) && computedInitializerEval instanceof EvalFixed) {
			EvalFixed evalFixed = (EvalFixed) computedInitializerEval;
			IValue val = evalFixed.getValue();
			if(!(val instanceof CompositeValue)) {
				CompositeValue compVal = new CompositeValue(initializerEval, new ICPPEvaluation[]{evalFixed});
				computedInitializerEval = new EvalFixed(type, computedInitializerEval.getValueCategory(context.getPoint()), compVal);
			}
		}
		
		if (isReferenceType(type)) {
			return createReferenceValue(record, context, computedInitializerEval);
		} else if (isPointerType(nestedType) && !isCStringType(nestedType)) {
			return createPointerValue(record, context, computedInitializerEval);
		} else if(isArrayType(nestedType) && !isCStringType(nestedType)) {
			IValue value = CompositeValue.create((EvalInitList)computedInitializerEval,(IArrayType) (type));
			return new EvalFixed(type, computedInitializerEval.getValueCategory(context.getPoint()), value);
		} else if (isValueInitialization(computedInitializerEval)){
			ICPPEvaluation defaultValue = new EvalTypeId(type, context.getPoint(), false, new ICPPEvaluation[]{});
			return new EvalFixed(type, defaultValue.getValueCategory(context.getPoint()), defaultValue.getValue(context.getPoint()));
		} else {
			return new EvalFixed(type, computedInitializerEval.getValueCategory(context.getPoint()), computedInitializerEval.getValue(context.getPoint()));
		}
	}
	
	private static ICPPEvaluation createDefaultInitializedCompositeValue(IType type) {
		if(!isClassType(type)) {
			return EvalFixed.INCOMPLETE;
		}
		ICPPClassType classType = (ICPPClassType)type;
		IValue compositeValue = CompositeValue.create(classType);
		EvalFixed initialValue = new EvalFixed(type, ValueCategory.PRVALUE, compositeValue);
		return initialValue;
	}
	
	private ICPPEvaluation createReferenceValue(ActivationRecord record, ConstexprEvaluationContext context, ICPPEvaluation computedInitializerEval) {
		ICPPEvaluation initValue = initializerEval;
		if(!(initValue instanceof EvalBinding)) {
			initValue = initializerEval.getValue(context.getPoint()).get(0);
		}
		
		if(initValue instanceof EvalBinding) {
			return createReferenceFromBinding(record, context, (EvalBinding) initValue);
		} else if(initValue instanceof EvalBinary && computedInitializerEval instanceof EvalCompositeAccess) {
			return createReferenceFromCompositeAccess(record, context, (EvalCompositeAccess) computedInitializerEval);
		} else {
			return EvalFixed.INCOMPLETE;
		}
	}
	
	private ICPPEvaluation createPointerValue(ActivationRecord record, ConstexprEvaluationContext context, ICPPEvaluation computedInitializerEval) {
		ICPPEvaluation initValue = initializerEval.getValue(context.getPoint()).get(0);
		if (isPointerToArray(initValue, context)) {
			EvalCompositeAccess arrayPointer = new EvalCompositeAccess(computedInitializerEval, 0);
			return createPointerFromCompositeAccess(record, context, arrayPointer);
		} else if (computedInitializerEval instanceof EvalPointer) {
			EvalPointer pointer = (EvalPointer)computedInitializerEval;
			return pointer.copy();
		}
		return EvalFixed.INCOMPLETE;
	}

	private static boolean isValueInitialization(ICPPEvaluation eval) {
		if(eval instanceof EvalInitList) {
			EvalInitList evalInitList = (EvalInitList)eval;
			return evalInitList.getClauses().length == 0;
		}
		return false;
	}

	private static boolean isPointerToArray(ICPPEvaluation eval, ConstexprEvaluationContext context) {
		return eval.getType(context.getPoint()) instanceof IArrayType;
	}

	private static ICPPEvaluation createReferenceFromBinding(ActivationRecord record, ConstexprEvaluationContext context, EvalBinding evalBinding) {
		return new EvalReference(record, evalBinding.getBinding(), context.getPoint());
	}
	
	private static ICPPEvaluation createReferenceFromCompositeAccess(ActivationRecord record, ConstexprEvaluationContext context, EvalCompositeAccess evalCompAccess) {
		return new EvalReference(record, evalCompAccess, context.getPoint());
	}
	
	private static ICPPEvaluation createPointerFromCompositeAccess(ActivationRecord record, ConstexprEvaluationContext context, EvalCompositeAccess evalCompAccess) {
		return new EvalPointer(record, evalCompAccess, context.getPoint());
	}

	private static boolean isReferenceType(IType type) {
		return type instanceof ICPPReferenceType;
	}
	
	private static boolean isPointerType(IType type) {
		return type instanceof IPointerType;
	}
	
	private static boolean isArrayType(IType type) {
		return type instanceof IArrayType;
	}
	
	private static boolean isCStringType(IType type) {
		IType nestedType = null;
		if(type instanceof IArrayType) {
			nestedType = ((IArrayType)type).getType();
		} else if(type instanceof IPointerType) {
			nestedType = ((IPointerType)type).getType();
		}
		
		if(nestedType != null) {
			return nestedType.isSameType(new CPPQualifierType(CPPBasicType.CHAR, true, false));
		}
		return false;
	}
	
	private static boolean isClassType(IType type) {
		return type instanceof ICPPClassType;
	}
	
	@Override
	public ICPPExecution instantiate(InstantiationContext context, int maxDepth) {
		ICPPBinding newDeclaredBinding;
		if(declaredBinding instanceof ICPPVariable) {
			ICPPVariable declaredVariable = (ICPPVariable)declaredBinding;
			newDeclaredBinding = CPPTemplates.createVariableSpecialization(declaredVariable, context.getContextSpecialization(), context.getPoint());	
		} else {
			newDeclaredBinding = (ICPPBinding)CPPTemplates.createSpecialization(context.getContextSpecialization(), declaredBinding, context.getPoint());
		}
		
		ICPPEvaluation newInitializerEval = initializerEval != null ? initializerEval.instantiate(context, maxDepth) : null;
		return new ExecDeclarator(newDeclaredBinding, newInitializerEval);
	}
	
	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.EXEC_DECLARATOR);
		buffer.marshalBinding(declaredBinding);
		buffer.marshalEvaluation(initializerEval, includeValue);
	}
	
	public static ISerializableExecution unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		ICPPBinding declaredBinding = (ICPPBinding)buffer.unmarshalBinding();
		ICPPEvaluation initializerEval = (ICPPEvaluation)buffer.unmarshalEvaluation();
		return new ExecDeclarator(declaredBinding, initializerEval);
	}
}
