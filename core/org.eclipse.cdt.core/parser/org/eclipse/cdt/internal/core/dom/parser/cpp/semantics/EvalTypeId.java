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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.typeFromReturnType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.valueCategoryFromReturnType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.CompositeValue;
import org.eclipse.cdt.internal.core.dom.parser.DependentValue;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.SizeofCalculator;
import org.eclipse.cdt.internal.core.dom.parser.SizeofCalculator.SizeAndAlignment;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper.MethodKind;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.core.runtime.CoreException;

/**
 * Performs evaluation of an expression.
 */
public class EvalTypeId extends CPPDependentEvaluation {
	public static final ICPPFunction AGGREGATE_INITIALIZATION = new CPPFunction(null) {
		@Override
		public String toString() {
			return "AGGREGATE_INITIALIZATION"; //$NON-NLS-1$
		}
	};

	private final IType fInputType;
	private final ICPPEvaluation[] fArguments;
	private final boolean fRepresentsNewExpression;
	private boolean fUsesBracedInitList; // Whether the constructor call uses { ... } instead of (...).
	private IType fOutputType;

	private ICPPFunction fConstructor = CPPFunction.UNINITIALIZED_FUNCTION;
	private boolean fCheckedIsTypeDependent;
	private boolean fIsTypeDependent;
	private boolean fCheckedIsConstantExpression;
	private boolean fIsConstantExpression;

	public EvalTypeId(IType type, IASTNode pointOfDefinition, boolean usesBracedInitList, ICPPEvaluation... arguments) {
		this(type, findEnclosingTemplate(pointOfDefinition), false, usesBracedInitList, arguments);
	}

	public EvalTypeId(IType type, IBinding templateDefinition, boolean forNewExpression, boolean usesBracedInitList,
			ICPPEvaluation... arguments) {
		super(templateDefinition);
		if (arguments == null)
			throw new NullPointerException("arguments"); //$NON-NLS-1$

		if (!CPPTemplates.isDependentType(type))
			type = SemanticUtil.getNestedType(type, TDEF);
		fInputType = type;
		fArguments = arguments;
		fRepresentsNewExpression = forNewExpression;
		fUsesBracedInitList = usesBracedInitList;
	}

	public static EvalTypeId createForNewExpression(IType type, IASTNode pointOfDefinition, boolean usesBracedInitList,
			ICPPEvaluation... arguments) {
		return new EvalTypeId(type, findEnclosingTemplate(pointOfDefinition), true, usesBracedInitList, arguments);
	}

	public IType getInputType() {
		return fInputType;
	}

	public ICPPEvaluation[] getArguments() {
		return fArguments;
	}

	public boolean representsNewExpression() {
		return fRepresentsNewExpression;
	}

	public boolean usesBracedInitList() {
		return fUsesBracedInitList;
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
	public IType getType() {
		if (fOutputType == null) {
			fOutputType = computeType();
		}
		return fOutputType;
	}

	private IType computeType() {
		if (isTypeDependent())
			return new TypeOfDependentExpression(this);

		IType type = typeFromReturnType(fInputType);
		if (fRepresentsNewExpression)
			return new CPPPointerType(type);
		return type;
	}

	@Override
	public IValue getValue() {
		if (isValueDependent())
			return DependentValue.create(this);
		if (isTypeDependent())
			return DependentValue.create(this);
		if (fRepresentsNewExpression)
			return IntegralValue.UNKNOWN;

		IType inputType = SemanticUtil.getNestedType(fInputType, CVTYPE);
		if (inputType instanceof ICPPClassType) {
			ICPPClassType classType = (ICPPClassType) inputType;
			IBinding ctor = getConstructor();
			if (EvalUtil.isCompilerGeneratedCtor(ctor)) {
				return CompositeValue.create(classType);
			} else if (ctor == AGGREGATE_INITIALIZATION) {
				return CompositeValue.create(new EvalInitList(fArguments, getTemplateDefinition()), classType);
			} else if (ctor instanceof ICPPConstructor) {
				EvalConstructor evalCtor = new EvalConstructor(classType, (ICPPConstructor) ctor, fArguments,
						getTemplateDefinition());
				ICPPEvaluation computedEvalCtor = evalCtor.computeForFunctionCall(new ActivationRecord(),
						new ConstexprEvaluationContext());
				return computedEvalCtor.getValue();
			} else {
				return IntegralValue.ERROR;
			}
		}
		if (fArguments.length == 0 || isEmptyInitializerList(fArguments)) {
			if (inputType instanceof ICPPBasicType) {
				switch (((ICPPBasicType) inputType).getKind()) {
				case eInt:
				case eInt128:
				case eDouble:
				case eBoolean:
				case eFloat:
				case eFloat128:
				case eNullPtr:
				case eChar:
				case eChar16:
				case eChar32:
				case eWChar:
					return IntegralValue.create(0l);
				case eUnspecified:
				case eVoid:
				default:
					return IntegralValue.UNKNOWN;
				}
			}
		}
		if (fArguments.length == 1) {
			IValue argVal = fArguments[0].getValue();
			if (argVal instanceof IntegralValue && argVal.numberValue() != null) {
				// Cast signed integer to unsigned.
				Long val = argVal.numberValue().longValue();
				if (val < 0 && inputType instanceof ICPPBasicType && ((ICPPBasicType) inputType).isUnsigned()) {
					SizeAndAlignment sizeAndAlignment = SizeofCalculator.getSizeAndAlignment(inputType);
					if (sizeAndAlignment != null) {
						long sizeof = sizeAndAlignment.size;
						if (sizeof > 4) {
							// Java's "long" can't represent the full range of an 64-bit unsigned integer
							// in C++.
							sizeof = 4;
						}
						long range = (1L << (sizeof * 8 - 1));
						val += range;
						return IntegralValue.create(val);
					}
				}
			}
			return argVal;
		}
		return IntegralValue.UNKNOWN;
	}

	private boolean isEmptyInitializerList(ICPPEvaluation[] arguments) {
		return arguments.length == 1 && arguments[0] instanceof EvalInitList
				&& ((EvalInitList) arguments[0]).getClauses().length == 0;
	}

	@Override
	public boolean isTypeDependent() {
		if (!fCheckedIsTypeDependent) {
			fCheckedIsTypeDependent = true;
			fIsTypeDependent = CPPTemplates.isDependentType(fInputType) || containsDependentType(fArguments);
		}
		return fIsTypeDependent;
	}

	@Override
	public boolean isValueDependent() {
		if (CPPTemplates.isDependentType(fInputType))
			return true;
		for (ICPPEvaluation arg : fArguments) {
			if (arg.isValueDependent())
				return true;
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
		if (getConstructor() == null && fArguments.length == 1) {
			// maybe EvalTypeID represents a conversion
			ICPPEvaluation conversionEval = maybeApplyConversion(fArguments[0], fInputType, false, false);
			if (!conversionEval.isConstantExpression())
				return false;
		}
		return !fRepresentsNewExpression && areAllConstantExpressions(fArguments)
				&& isNullOrConstexprFunc(getConstructor());
	}

	@Override
	public boolean isEquivalentTo(ICPPEvaluation other) {
		if (!(other instanceof EvalTypeId)) {
			return false;
		}
		EvalTypeId o = (EvalTypeId) other;
		return fInputType.isSameType(o.fInputType) && areEquivalentEvaluations(fArguments, o.fArguments)
				&& fRepresentsNewExpression == o.fRepresentsNewExpression
				&& fUsesBracedInitList == o.fUsesBracedInitList;
	}

	@Override
	public ValueCategory getValueCategory() {
		return valueCategoryFromReturnType(fInputType);
	}

	public ICPPFunction getConstructor() {
		if (fConstructor == CPPFunction.UNINITIALIZED_FUNCTION) {
			fConstructor = computeConstructor();
		}
		return fConstructor;
	}

	private static boolean allConstructorsAreCompilerGenerated(ICPPConstructor[] constructors) {
		for (ICPPConstructor constructor : constructors) {
			if (!EvalUtil.isCompilerGeneratedCtor(constructor))
				return false;
		}
		return true;
	}

	private ICPPFunction computeConstructor() {
		if (isTypeDependent())
			return null;

		IType simplifiedType = SemanticUtil.getNestedType(fInputType, TDEF | CVTYPE);
		if (simplifiedType instanceof ICPPClassType) {
			ICPPClassType classType = (ICPPClassType) simplifiedType;
			ICPPEvaluation[] arguments = fArguments;
			ICPPConstructor[] constructors = classType.getConstructors();
			if (arguments.length == 1 && arguments[0] instanceof EvalInitList && !fUsesBracedInitList) {
				// List-initialization of a class (dcl.init.list-3).
				if (TypeTraits.isAggregateClass(classType)) {
					// Pretend that aggregate initialization is calling the default constructor.
					return findDefaultConstructor(classType, constructors);
				}
				if (((EvalInitList) arguments[0]).getClauses().length == 0) {
					ICPPMethod ctor = findDefaultConstructor(classType, constructors);
					if (ctor != null)
						return ctor;
				}
				ICPPConstructor[] ctors = getInitializerListConstructors(constructors);
				if (ctors.length != 0) {
					constructors = ctors;
				} else {
					arguments = ((EvalInitList) arguments[0]).getClauses();
				}
			}

			LookupData data = new LookupData(classType.getNameCharArray(), null, CPPSemantics.getCurrentLookupPoint());
			data.foundItems = constructors;
			data.setFunctionArguments(false, arguments);
			try {
				IBinding binding = CPPSemantics.resolveFunction(data, constructors, true, false);
				if (binding instanceof ICPPFunction) {
					return (ICPPFunction) binding;
				}
				if (fUsesBracedInitList && allConstructorsAreCompilerGenerated(constructors)) {
					Cost cost = AggregateInitialization.check(classType,
							new EvalInitList(arguments, getTemplateDefinition()));
					if (cost.converts())
						return AGGREGATE_INITIALIZATION;
					else
						return new CPPFunction.CPPFunctionProblem(null, ISemanticProblem.BINDING_NOT_FOUND,
								classType.getNameCharArray());
				}
				if (binding instanceof IProblemBinding && !(binding instanceof ICPPFunction))
					return new CPPFunction.CPPFunctionProblem(null, ((IProblemBinding) binding).getID(),
							classType.getNameCharArray());
			} catch (DOMException e) {
				CCorePlugin.log(e);
			}
		}
		return null;
	}

	private ICPPConstructor findDefaultConstructor(ICPPClassType classType, ICPPConstructor[] constructors) {
		for (ICPPConstructor ctor : constructors) {
			if (ctor.isImplicit() && ClassTypeHelper.getMethodKind(classType, ctor) == MethodKind.DEFAULT_CTOR)
				return ctor;
		}
		return null;
	}

	/**
	 * Returns constructors that can be called by passing a single {@code std::initializer_list}
	 * as an argument.
	 */
	private ICPPConstructor[] getInitializerListConstructors(ICPPConstructor[] constructors) {
		ICPPConstructor[] result = ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
		ICPPClassTemplate template = CPPVisitor.get_initializer_list();
		if (template == null)
			return result;

		for (ICPPConstructor ctor : constructors) {
			if (ctor.getRequiredArgumentCount() <= 1) {
				IType[] parameterTypes = ctor.getType().getParameterTypes();
				if (parameterTypes.length != 0) {
					IType type = parameterTypes[0];
					if (type instanceof ICPPSpecialization) {
						IBinding specialized = ((ICPPSpecialization) type).getSpecializedBinding();
						if (specialized instanceof ICPPClassTemplate && template.isSameType((IType) specialized)) {
							result = ArrayUtil.append(result, ctor);
						}
					}
				}
			}
		}
		return ArrayUtil.trim(result);
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		short firstBytes = ITypeMarshalBuffer.EVAL_TYPE_ID;
		if (fRepresentsNewExpression)
			firstBytes |= ITypeMarshalBuffer.FLAG1;
		if (fUsesBracedInitList)
			firstBytes |= ITypeMarshalBuffer.FLAG2;

		buffer.putShort(firstBytes);
		buffer.marshalType(fInputType);
		buffer.putInt(fArguments.length);
		for (ICPPEvaluation arg : fArguments) {
			buffer.marshalEvaluation(arg, includeValue);
		}
		marshalTemplateDefinition(buffer);
	}

	public static ICPPEvaluation unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		IType type = buffer.unmarshalType();
		ICPPEvaluation[] args = null;
		int len = buffer.getInt();
		args = new ICPPEvaluation[len];
		for (int i = 0; i < args.length; i++) {
			args[i] = buffer.unmarshalEvaluation();
		}
		IBinding templateDefinition = buffer.unmarshalBinding();
		boolean forNewExpression = (firstBytes & ITypeMarshalBuffer.FLAG1) != 0;
		boolean usesBracedInitList = (firstBytes & ITypeMarshalBuffer.FLAG2) != 0;
		EvalTypeId result = new EvalTypeId(type, templateDefinition, forNewExpression, usesBracedInitList, args);
		return result;
	}

	@Override
	public ICPPEvaluation instantiate(InstantiationContext context, int maxDepth) {
		ICPPEvaluation[] args = instantiateExpressions(fArguments, context, maxDepth);
		IType type = CPPTemplates.instantiateType(fInputType, context);
		if (args == fArguments && type == fInputType)
			return this;

		EvalTypeId result = new EvalTypeId(type, getTemplateDefinition(), fRepresentsNewExpression, fUsesBracedInitList,
				args);

		if (!result.isTypeDependent()) {
			IType simplifiedType = SemanticUtil.getNestedType(type, SemanticUtil.TDEF);
			if (simplifiedType instanceof ICPPClassType) {
				// Check the constructor call and return EvalFixed.INCOMPLETE to indicate a substitution
				// failure if the call cannot be resolved.
				ICPPFunction constructor = result.getConstructor();
				if (constructor == null || constructor instanceof IProblemBinding || constructor.isDeleted()) {
					return EvalFixed.INCOMPLETE;
				}
			}
		}

		return result;
	}

	@Override
	public ICPPEvaluation computeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		ICPPFunction constructor = getConstructor();
		if (constructor != null && constructor instanceof ICPPConstructor) {
			return new EvalConstructor(fInputType, (ICPPConstructor) constructor, fArguments, getTemplateDefinition())
					.computeForFunctionCall(record, context);
		}

		ICPPEvaluation[] args = fArguments;
		for (int i = 0; i < fArguments.length; i++) {
			ICPPEvaluation arg = fArguments[i].computeForFunctionCall(record, context.recordStep());
			if (arg != fArguments[i]) {
				if (args == fArguments) {
					args = new ICPPEvaluation[fArguments.length];
					System.arraycopy(fArguments, 0, args, 0, fArguments.length);
				}
				args[i] = arg;
			}
		}

		if (args == fArguments) {
			return this;
		}
		EvalTypeId evalTypeId = new EvalTypeId(fInputType, getTemplateDefinition(), fRepresentsNewExpression,
				fUsesBracedInitList, args);
		return evalTypeId;
	}

	@Override
	public int determinePackSize(ICPPTemplateParameterMap tpMap) {
		int r = CPPTemplates.determinePackSize(fInputType, tpMap);
		for (ICPPEvaluation arg : fArguments) {
			r = CPPTemplates.combinePackSize(r, arg.determinePackSize(tpMap));
		}
		return r;
	}

	@Override
	public boolean referencesTemplateParameter() {
		for (ICPPEvaluation arg : fArguments) {
			if (arg.referencesTemplateParameter())
				return true;
		}
		return false;
	}

	@Override
	public boolean isNoexcept() {
		if (getConstructor() instanceof CPPFunction) {
			CPPFunction f = (CPPFunction) getConstructor();
			if (f != AGGREGATE_INITIALIZATION) {
				if (!EvalUtil.evaluateNoexceptSpecifier(f.getType().getNoexceptSpecifier()))
					return false;
			}
		} else if (fArguments.length == 1) {
			// maybe EvalTypeID represents a conversion
			ICPPEvaluation conversionEval = maybeApplyConversion(fArguments[0], fInputType, false, false);
			return conversionEval.isNoexcept();
		}
		for (ICPPEvaluation arg : fArguments) {
			if (!arg.isNoexcept())
				return false;
		}
		return true;
	}
}
