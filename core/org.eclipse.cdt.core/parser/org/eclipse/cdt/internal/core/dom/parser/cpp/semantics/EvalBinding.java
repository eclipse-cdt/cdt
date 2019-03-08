/*******************************************************************************
 * Copyright (c) 2012, 2014 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.prvalueType;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameterPackType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.DependentValue;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.core.runtime.CoreException;

public class EvalBinding extends CPPDependentEvaluation {
	/**
	 * The function owning the parameter if the binding is a function parameter, otherwise
	 * {@code null}. May be computed lazily and remains {@code null} until computed.
	 */
	private ICPPFunction fParameterOwner;
	/**
	 * The position of the parameter in the parameter list if the binding is a function parameter,
	 * otherwise -1.
	 */
	private int fParameterPosition;
	/**
	 * The binding represented by this evaluation. For a function parameter binding may be computed
	 * lazily to avoid infinite recursion during unmarshalling of the evaluation. If
	 * {@link #fBinding} is {@code null}, {@link #fParameterOwner} is guaranteed to be not {@code null}
	 * and vice versa.
	 */
	private IBinding fBinding;
	private final boolean fFixedType;

	private IType fType;
	private boolean fCheckedIsValueDependent;
	private boolean fIsValueDependent;
	private boolean fCheckedIsTypeDependent;
	private boolean fIsTypeDependent;
	private boolean fCheckedIsConstantExpression;
	private boolean fIsConstantExpression;

	public EvalBinding(IBinding binding, IType type, IASTNode pointOfDefinition) {
		this(binding, type, findEnclosingTemplate(pointOfDefinition));
	}

	public EvalBinding(IBinding binding, IType type, IBinding templateDefinition) {
		super(templateDefinition);
		fParameterPosition = -1;
		fBinding = binding;
		fType = type;
		fFixedType = type != null;
	}

	public EvalBinding(ICPPFunction parameterOwner, int parameterPosition, IType type, IASTNode pointOfDefinition) {
		this(parameterOwner, parameterPosition, type, findEnclosingTemplate(pointOfDefinition));
	}

	public EvalBinding(ICPPFunction parameterOwner, int parameterPosition, IType type, IBinding templateDefinition) {
		super(templateDefinition);
		fParameterOwner = parameterOwner;
		fParameterPosition = parameterPosition;
		fType = type;
		fFixedType = type != null;
	}

	public IBinding getBinding() {
		if (fBinding == null) {
			// fParameterOwner is guaranteed to be not null.
			ICPPParameter[] parameters = fParameterOwner.getParameters();
			fBinding = parameters[fParameterPosition];
		}
		return fBinding;
	}

	/**
	 * @return if the binding is a function parameter, returns its position in the parameter list,
	 * otherwise returns -1
	 */
	public int getFunctionParameterPosition() {
		if (fParameterPosition < 0) {
			if (fBinding instanceof CPPParameter) {
				fParameterPosition = ((CPPParameter) fBinding).getParameterPosition();
			} else {
				ICPPFunction parameterOwner = getParameterOwner();
				if (parameterOwner != null) {
					ICPPParameter[] parameters = fParameterOwner.getParameters();
					fParameterPosition = findInArray(parameters, fBinding);
				}
			}
		}
		return fParameterPosition;
	}

	/**
	 * Finds a given object in an array.
	 *
	 * @param array the array to find the object in
	 * @param obj the object to find
	 * @return the index of the object in the array, or -1 if the object is not in the array
	 */
	private static int findInArray(Object[] array, Object obj) {
		for (int i = 0; i < array.length; i++) {
			if (obj == array[i])
				return i;
		}
		return -1;
	}

	/**
	 * @return the function owning the parameter if the binding is a function parameter,
	 *     otherwise {@code null}.
	 */
	public ICPPFunction getParameterOwner() {
		if (fParameterOwner == null && fBinding instanceof ICPPParameter) {
			IBinding owner = fBinding.getOwner();
			if (owner instanceof ICPPFunction)
				fParameterOwner = (ICPPFunction) owner;
		}
		return fParameterOwner;
	}

	/**
	 * @return if the binding is a template parameter, returns its ID, otherwise returns -1
	 */
	public int getTemplateParameterID() {
		// No need to call getBinding method since fBinding cannot be null if the evaluation
		// represents a template parameter.
		return fBinding instanceof ICPPTemplateParameter ? ((ICPPTemplateParameter) fBinding).getParameterID() : -1;
	}

	public IType getFixedType() {
		return fFixedType ? fType : null;
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
			fCheckedIsTypeDependent = true;
			fIsTypeDependent = computeIsTypeDependent();
		}
		return fIsTypeDependent;
	}

	private boolean computeIsTypeDependent() {
		IType t = null;
		if (fFixedType) {
			t = fType;
		} else {
			IBinding binding = getBinding();
			if (binding instanceof IEnumerator) {
				t = ((IEnumerator) binding).getType();
			} else if (binding instanceof ICPPTemplateNonTypeParameter) {
				t = ((ICPPTemplateNonTypeParameter) binding).getType();
			} else if (binding instanceof IVariable) {
				t = ((IVariable) binding).getType();
			} else if (binding instanceof ICPPUnknownBinding) {
				return true;
			} else if (binding instanceof IFunction) {
				t = ((IFunction) binding).getType();
			} else {
				return false;
			}
		}
		return CPPTemplates.isDependentType(t);
	}

	@Override
	public boolean isValueDependent() {
		if (!fCheckedIsValueDependent) {
			fCheckedIsValueDependent = true;
			fIsValueDependent = computeIsValueDependent();
		}
		return fIsValueDependent;
	}

	private boolean computeIsValueDependent() {
		// No need to call getBinding() since a function parameter never has an initial value.
		if (fBinding instanceof IEnumerator) {
			return IntegralValue.isDependentValue(((IEnumerator) fBinding).getValue());
		}
		if (fBinding instanceof ICPPTemplateNonTypeParameter) {
			return true;
		}
		if (fBinding instanceof ICPPUnknownBinding) {
			return true;
		}
		if (fBinding instanceof IVariable) {
			return IntegralValue.isDependentValue(((IVariable) fBinding).getInitialValue());
		}
		if (fBinding instanceof IFunction) {
			return false;
		}
		return false;
	}

	@Override
	public boolean isConstantExpression() {
		if (!fCheckedIsConstantExpression) {
			fCheckedIsConstantExpression = true;
			fIsConstantExpression = computeIsConstantExpression();
		}
		return fIsConstantExpression;
	}

	private boolean computeIsConstantExpression() {
		if (fBinding instanceof IEnumerator || fBinding instanceof ICPPFunction)
			return true;
		else if (fBinding instanceof ICPPVariable) {
			if (!isConstexprValue(((IVariable) fBinding).getInitialValue()))
				return false;
			ICPPVariable var = (ICPPVariable) fBinding;
			if (var.isConstexpr())
				return true;
			IType type = SemanticUtil.getNestedType(var.getType(), SemanticUtil.TDEF | SemanticUtil.REF);
			if (ExpressionTypes.isConst(type)) {
				if (var instanceof ICPPField) {
					if (var.isStatic())
						return true;
				} else
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean isEquivalentTo(ICPPEvaluation other) {
		if (!(other instanceof EvalBinding)) {
			return false;
		}
		EvalBinding o = (EvalBinding) other;
		if (fBinding != null) {
			return fBinding == o.fBinding;
		}
		return fParameterOwner == o.fParameterOwner && fParameterPosition == o.fParameterPosition;
	}

	@Override
	public IType getType() {
		if (fType == null) {
			fType = computeType();
		}
		return fType;
	}

	private IType computeType() {
		IBinding binding = getBinding();
		if (binding instanceof IEnumerator) {
			return ((IEnumerator) binding).getType();
		}
		if (binding instanceof ICPPTemplateNonTypeParameter) {
			IType type = ((ICPPTemplateNonTypeParameter) binding).getType();
			// If the binding is a non-type parameter pack, it must have been
			// referenced from inside the expansion pattern of a pack expansion.
			// In such a context, the type of the binding is the type of each
			// parameter in the parameter pack, not the type of the pack itself.
			if (type instanceof ICPPParameterPackType)
				type = ((ICPPParameterPackType) type).getType();
			return prvalueType(type);
		}
		if (binding instanceof IVariable) {
			IType type = ((IVariable) binding).getType();
			IASTNode point = CPPSemantics.getCurrentLookupPoint();
			if (type instanceof IArrayType && ((IArrayType) type).getSize() == null && binding instanceof IIndexBinding
					&& point != null) {
				// Refine the type of the array variable by filling in missing size information.
				// This may be necessary if the variable is declared outside of the current
				// translation unit	without providing array size information, but is defined in
				// the current translation unit with such information.
				// For example:
				// header.h
				// --------
				// struct S {
				//   static const char[] c;
				// };
				//
				// source.cpp
				// ----------
				// #include "header.h"
				// const char S::c[] = "abc";
				IASTTranslationUnit ast = point.getTranslationUnit();
				IASTName[] definitions = ast.getDefinitionsInAST(binding);
				for (IASTName definition : definitions) {
					IASTDeclarator declarator = ASTQueries.findAncestorWithType(definition, IASTDeclarator.class);
					if (declarator != null) {
						IType localType = CPPVisitor.createType(declarator);
						if (localType instanceof IArrayType && ((IArrayType) localType).getSize() != null) {
							type = localType;
							break;
						}
					}
				}
			}
			return SemanticUtil.mapToAST(ExpressionTypes.glvalueType(type));
		}
		if (binding instanceof IFunction) {
			final IFunctionType type = ((IFunction) binding).getType();
			return SemanticUtil.mapToAST(type);
		}
		return ProblemType.UNKNOWN_FOR_EXPRESSION;
	}

	@Override
	public IValue getValue() {
		if (isValueDependent())
			return DependentValue.create(this);

		IValue value = null;

		if (fBinding instanceof ICPPVariable) {
			ICPPEvaluation valueEval = EvalUtil.getVariableValue((ICPPVariable) fBinding, new ActivationRecord());
			if (valueEval != null) {
				value = valueEval.getValue();
			}
		} else if (fBinding instanceof IEnumerator) {
			value = ((IEnumerator) fBinding).getValue();
		}
		if (value == null)
			value = IntegralValue.UNKNOWN;

		return value;
	}

	@Override
	public ValueCategory getValueCategory() {
		if (fBinding instanceof ICPPTemplateNonTypeParameter)
			return ValueCategory.PRVALUE;

		// fBinding can be null only when the evaluation represents a function parameter.
		if (fBinding instanceof IFunction || fBinding instanceof IVariable || fBinding == null) {
			return ValueCategory.LVALUE;
		}
		return ValueCategory.PRVALUE;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		short firstBytes = ITypeMarshalBuffer.EVAL_BINDING;
		ICPPFunction parameterOwner = getParameterOwner();
		if (parameterOwner != null) {
			// A function parameter cannot be marshalled directly. We are storing the owning
			// function and the parameter position instead.
			buffer.putShort((short) (ITypeMarshalBuffer.EVAL_BINDING | ITypeMarshalBuffer.FLAG1));
			buffer.marshalBinding(parameterOwner);
			buffer.putInt(getFunctionParameterPosition());
		} else {
			buffer.putShort(firstBytes);
			buffer.marshalBinding(fBinding);
		}
		buffer.marshalType(fFixedType ? fType : null);
		marshalTemplateDefinition(buffer);
	}

	public static ICPPEvaluation unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		if ((firstBytes & ITypeMarshalBuffer.FLAG1) != 0) {
			IBinding paramOwnerBinding = buffer.unmarshalBinding();
			if (paramOwnerBinding instanceof IProblemBinding) {
				// The parameter owner could not be stored in the index.
				// If this happens, it's almost certainly a bug, but the severity
				// is mitigated by returning a problem evaluation instead of just
				// trying to cast to ICPPFunction and throwing a ClassCastException.
				CCorePlugin.log("An EvalBinding had a parameter owner that could not be stored in the index"); //$NON-NLS-1$
				return EvalFixed.INCOMPLETE;
			}
			ICPPFunction parameterOwner = (ICPPFunction) paramOwnerBinding;
			int parameterPosition = buffer.getInt();
			IType type = buffer.unmarshalType();
			IBinding templateDefinition = buffer.unmarshalBinding();
			return new EvalBinding(parameterOwner, parameterPosition, type, templateDefinition);
		} else {
			IBinding binding = buffer.unmarshalBinding();
			IType type = buffer.unmarshalType();
			IBinding templateDefinition = buffer.unmarshalBinding();
			return new EvalBinding(binding, type, templateDefinition);
		}
	}

	@Override
	public ICPPEvaluation instantiate(InstantiationContext context, int maxDepth) {
		IBinding origBinding = getBinding();
		final CPPTemplateParameterMap tpMap = (CPPTemplateParameterMap) context.getParameterMap();
		final int packOffset = context.getPackOffset();

		IVariable newBinding = tpMap == null ? null : (IVariable) context.getInstantiatedLocal(origBinding);
		if (newBinding != null) {
			IType origType = ((IVariable) origBinding).getType();
			EvalBinding newBindingEval = null;
			if (origType instanceof ICPPParameterPackType) {
				origType = ((ICPPParameterPackType) origType).getType();
				IType instantiatedType = CPPTemplates.instantiateType(origType, context);
				if (origType != instantiatedType) {
					newBindingEval = new EvalBinding(newBinding, instantiatedType, getTemplateDefinition());
				}
			}

			if (newBindingEval == null) {
				newBindingEval = new EvalBinding(newBinding, newBinding.getType(), getTemplateDefinition());
			}
			if (context.hasPackOffset()) {
				return new EvalCompositeAccess(newBindingEval, packOffset);
			} else {
				return newBindingEval;
			}
		}

		if (origBinding instanceof ICPPTemplateNonTypeParameter) {
			ICPPTemplateArgument argument = context.getArgument((ICPPTemplateNonTypeParameter) origBinding);
			if (argument != null && argument.isNonTypeValue()) {
				return argument.getNonTypeEvaluation();
			}
		} else if (origBinding instanceof ICPPParameter) {
			ICPPParameter parameter = (ICPPParameter) origBinding;
			IType origType = parameter.getType();
			if (origType instanceof ICPPParameterPackType && context.hasPackOffset()) {
				origType = ((ICPPParameterPackType) origType).getType();
			}
			IType instantiatedType = CPPTemplates.instantiateType(origType, context);
			if (origType != instantiatedType) {
				return new EvalFixed(instantiatedType, ValueCategory.LVALUE, DependentValue.create(this));
			}
		} else {
			IBinding instantiatedBinding = instantiateBinding(origBinding, context, maxDepth);
			if (instantiatedBinding != origBinding)
				return new EvalBinding(instantiatedBinding, null, getTemplateDefinition());
		}
		return this;
	}

	@Override
	public ICPPEvaluation computeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		ICPPEvaluation eval = record.getVariable(getBinding());

		if (eval != null) {
			return eval;
		} else {
			return this;
		}
	}

	@Override
	public int determinePackSize(ICPPTemplateParameterMap tpMap) {
		IBinding binding = getBinding();
		if (binding instanceof IEnumerator) {
			return CPPTemplates.determinePackSize(((IEnumerator) binding).getValue(), tpMap);
		}
		if (binding instanceof ICPPUnknownBinding) {
			return CPPTemplates.determinePackSize((ICPPUnknownBinding) binding, tpMap);
		}
		if (binding instanceof ICPPParameter && ((ICPPParameter) binding).isParameterPack()) {
			ICPPParameterPackType type = (ICPPParameterPackType) ((ICPPParameter) binding).getType();
			return CPPTemplates.determinePackSize(type.getType(), tpMap);
		}

		if (binding instanceof ICPPSpecialization) {
			binding = ((ICPPSpecialization) binding).getSpecializedBinding();
		}

		int r = CPPTemplates.PACK_SIZE_NOT_FOUND;
		if (binding instanceof ICPPTemplateDefinition) {
			ICPPTemplateParameter[] parameters = ((ICPPTemplateDefinition) binding).getTemplateParameters();
			for (ICPPTemplateParameter param : parameters) {
				r = CPPTemplates.combinePackSize(r, CPPTemplates.determinePackSize((ICPPUnknownBinding) param, tpMap));
			}
		}

		return r;
	}

	@Override
	public boolean referencesTemplateParameter() {
		// No need to call getBinding method since fBinding cannot be null if the evaluation
		// represents a template parameter.
		return fBinding instanceof ICPPTemplateParameter;
	}

	@Override
	public String toString() {
		return getBinding().toString();
	}

	@Override
	public boolean isNoexcept() {
		return true;
	}
}
