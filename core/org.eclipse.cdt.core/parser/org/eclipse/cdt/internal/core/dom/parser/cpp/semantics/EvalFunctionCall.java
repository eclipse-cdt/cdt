/*******************************************************************************
 * Copyright (c) 2012, 2016 Wind River Systems, Inc. and others.
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

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.valueCategoryFromFunctionCall;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExpressionTypes.valueCategoryFromReturnType;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import java.util.Arrays;

import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.CompositeValue;
import org.eclipse.cdt.internal.core.dom.parser.DependentValue;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClosureType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPExecution;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.cdt.internal.core.dom.parser.cpp.OverloadableOperator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics.LookupMode;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;

public final class EvalFunctionCall extends CPPDependentEvaluation {
	private final ICPPEvaluation[] fArguments;
	private ICPPFunction fOverload = CPPFunction.UNINITIALIZED_FUNCTION;
	private IType fType;
	private boolean fCheckedIsConstantExpression;
	private boolean fIsConstantExpression;

	private final ICPPEvaluation fImplicitThis;

	public EvalFunctionCall(ICPPEvaluation[] args, ICPPEvaluation owner, IBinding templateDefinition) {
		super(templateDefinition);
		for (int i = 0; i < args.length; i++) {
			Assert.isNotNull(args[i]);
		}
		fArguments = args;
		fImplicitThis = getImplicitThis() == owner ? null : owner;
	}

	public EvalFunctionCall(ICPPEvaluation[] args, ICPPEvaluation owner, IASTNode pointOfDefinition) {
		this(args, owner, findEnclosingTemplate(pointOfDefinition));
	}

	/**
	 * Returns arguments of the function call. The first argument is the function name, the rest are arguments
	 * passed to the function.
	 */
	public ICPPEvaluation[] getArguments() {
		return fArguments;
	}

	private ICPPEvaluation getImplicitThis() {
		if (fImplicitThis != null)
			return fImplicitThis;

		if (fArguments.length > 0) {
			if (fArguments[0] instanceof EvalMemberAccess) {
				return ((EvalMemberAccess) fArguments[0]).getOwnerEval();
			} else if (fArguments[0] instanceof EvalID) {
				return ((EvalID) fArguments[0]).getFieldOwner();
			}
		}

		return null;
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
		return containsDependentType(fArguments);
	}

	@Override
	public boolean isValueDependent() {
		return containsDependentValue(fArguments) || !CPPTemplates.isFullyInstantiated(resolveFunctionBinding());
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
		return areAllConstantExpressions(fArguments) && isNullOrConstexprFunc(resolveFunctionBinding());
	}

	@Override
	public boolean isEquivalentTo(ICPPEvaluation other) {
		if (!(other instanceof EvalFunctionCall)) {
			return false;
		}
		EvalFunctionCall o = (EvalFunctionCall) other;
		return areEquivalentEvaluations(fArguments, o.fArguments);
	}

	public ICPPFunction getOverload() {
		if (fOverload == CPPFunction.UNINITIALIZED_FUNCTION) {
			fOverload = computeOverload();
		}
		return fOverload;
	}

	private ICPPFunction computeOverload() {
		if (isTypeDependent())
			return null;

		IType t = SemanticUtil.getNestedType(fArguments[0].getType(), TDEF | REF | CVTYPE);
		if (t instanceof ICPPClassType) {
			return CPPSemantics.findOverloadedOperator(getTemplateDefinitionScope(), fArguments, t,
					OverloadableOperator.PAREN, LookupMode.NO_GLOBALS);
		}
		return null;
	}

	@Override
	public IType getType() {
		if (fType == null)
			fType = computeType();
		return fType;
	}

	private IType computeType() {
		if (isTypeDependent())
			return new TypeOfDependentExpression(this);

		ICPPFunction overload = getOverload();
		if (overload != null)
			return ExpressionTypes.typeFromFunctionCall(overload);

		ICPPEvaluation function = fArguments[0];
		IType result = ExpressionTypes.typeFromFunctionCall(function.getType());
		if (function instanceof EvalMemberAccess) {
			result = ExpressionTypes.restoreTypedefs(result, ((EvalMemberAccess) function).getOwnerType());
		}
		return result;
	}

	@Override
	public IValue getValue() {
		ICPPEvaluation eval = evaluateFunctionBody(new ConstexprEvaluationContext());
		if (eval == this) {
			return DependentValue.create(eval);
		}
		return eval.getValue();
	}

	@Override
	public ValueCategory getValueCategory() {
		ICPPFunction overload = getOverload();
		if (overload != null)
			return valueCategoryFromFunctionCall(overload);

		IType t = fArguments[0].getType();
		if (t instanceof IPointerType) {
			t = SemanticUtil.getNestedType(((IPointerType) t).getType(), TDEF | REF | CVTYPE);
		}
		if (t instanceof IFunctionType) {
			return valueCategoryFromReturnType(((IFunctionType) t).getReturnType());
		}
		return ValueCategory.PRVALUE;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.EVAL_FUNCTION_CALL);
		buffer.putInt(fArguments.length);
		for (ICPPEvaluation arg : fArguments) {
			buffer.marshalEvaluation(arg, includeValue);
		}
		buffer.marshalEvaluation(fImplicitThis, includeValue);
		marshalTemplateDefinition(buffer);
	}

	public static ICPPEvaluation unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		int len = buffer.getInt();
		ICPPEvaluation[] args = new ICPPEvaluation[len];
		for (int i = 0; i < args.length; i++) {
			args[i] = buffer.unmarshalEvaluation();
		}
		ICPPEvaluation implicitThis = buffer.unmarshalEvaluation();
		IBinding templateDefinition = buffer.unmarshalBinding();
		return new EvalFunctionCall(args, implicitThis, templateDefinition);
	}

	@Override
	public ICPPEvaluation instantiate(InstantiationContext context, int maxDepth) {
		ICPPEvaluation[] args = instantiateExpressions(fArguments, context, maxDepth);
		if (args == fArguments)
			return this;

		ICPPEvaluation implicitThis = fImplicitThis;

		if (args[0] instanceof EvalFunctionSet && getOverload() == null) {
			// Resolve the function using the parameters of the function call.
			EvalFunctionSet functionSet = (EvalFunctionSet) args[0];
			args[0] = functionSet.resolveFunction(Arrays.copyOfRange(args, 1, args.length));

			// Propagate instantiation errors for SFINAE purposes.
			if (args[0] == EvalFixed.INCOMPLETE) {
				return args[0];
			}

			// For functions sets of member functions, EvalFunctionSet does not store
			// the value of the object on which the member function is called.
			// If this value was previously elided (not stored explicitly in
			// fImplicitThis to avoid duplication with the copy stored in fArguments[0]),
			// starts storing it in fImplicitThis because *someone* needs to store
			// the value for correct constexpr evaluation.
			if (implicitThis == null) {
				implicitThis = getImplicitThis();
			}
		}

		ICPPEvaluation newImplicitThis = implicitThis != null ? implicitThis.instantiate(context, maxDepth) : null;
		return new EvalFunctionCall(args, newImplicitThis, getTemplateDefinition());
	}

	@Override
	public ICPPEvaluation computeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		if (context.getStepsPerformed() >= ConstexprEvaluationContext.MAX_CONSTEXPR_EVALUATION_STEPS) {
			return EvalFixed.INCOMPLETE;
		}

		ICPPFunction functionBinding = resolveFunctionBinding();
		if (functionBinding == null)
			return EvalFixed.INCOMPLETE;

		ICPPEvaluation[] args = new ICPPEvaluation[fArguments.length];
		System.arraycopy(fArguments, 0, args, 0, fArguments.length);

		ICPPParameter[] parameters = functionBinding.getParameters();
		for (int i = 0; i < fArguments.length; i++) {
			ICPPEvaluation arg = fArguments[i].computeForFunctionCall(record, context.recordStep());
			if (0 < i && i <= parameters.length && isReference(parameters[i - 1])
					&& fArguments[i] instanceof EvalBinding) {
				final EvalBinding evalBinding = (EvalBinding) fArguments[i];
				IBinding binding = evalBinding.getBinding();
				// If the binding being referenced isn't present in the activation record,
				// we won't be able to evaluate the function call.
				if (record.getVariable(binding) == null)
					return EvalFixed.INCOMPLETE;
				arg = new EvalReference(record, binding, evalBinding.getTemplateDefinition());
			} else if (0 < i && i <= parameters.length && !isReference(parameters[i - 1])) {
				IValue copiedValue = arg.getValue().clone();
				arg = new EvalFixed(arg.getType(), arg.getValueCategory(), copiedValue);
			}
			if (arg == EvalFixed.INCOMPLETE)
				return EvalFixed.INCOMPLETE;
			args[i] = arg;
		}

		ICPPEvaluation implicitThis = getImplicitThis();
		ICPPEvaluation owner = null;
		if (functionBinding instanceof ICPPMethod) {
			if (implicitThis instanceof EvalBinding) {
				IBinding ownerBinding = ((EvalBinding) implicitThis).getBinding();
				if (record.getVariable(ownerBinding) != null) {
					owner = new EvalReference(record, ownerBinding, implicitThis.getTemplateDefinition());
				} else {
					owner = implicitThis;
				}
			} else if (implicitThis != null) {
				owner = implicitThis.computeForFunctionCall(record, context);
			} else {
				owner = record.getImplicitThis();
			}
		}

		return new EvalFunctionCall(args, owner, getTemplateDefinition()).evaluateFunctionBody(context.recordStep());
	}

	private ICPPEvaluation evaluateFunctionBody(ConstexprEvaluationContext context) {
		if (isValueDependent()) {
			return this;
		}

		// If the arguments are not all constant expressions, there is
		// no point trying to substitute them into the return expression.
		if (!areAllConstantExpressions(fArguments, 1, fArguments.length))
			return EvalFixed.INCOMPLETE;

		ICPPFunction function = resolveFunctionBinding();
		if (function == null)
			return this;

		if (!function.isConstexpr())
			return EvalFixed.INCOMPLETE;

		ActivationRecord record = createActivationRecord(function.getParameters(), fArguments, getImplicitThis());
		ICPPExecution bodyExec = CPPFunction.getFunctionBodyExecution(function);
		if (bodyExec == null) {
			if (!(function instanceof ICPPTemplateInstance)
					|| ((ICPPTemplateInstance) function).isExplicitSpecialization()) {
				return EvalFixed.INCOMPLETE;
			}
			ICPPTemplateInstance functionInstance = (ICPPTemplateInstance) function;
			IBinding specialized = functionInstance.getSpecializedBinding();
			if (!(specialized instanceof ICPPFunction))
				return this;
			bodyExec = CPPFunction.getFunctionBodyExecution((ICPPFunction) specialized);
		}
		if (bodyExec != null) {
			bodyExec = bodyExec.executeForFunctionCall(record, context.recordStep());

			// If the function exited via a return statement, bodyExec.executeForFunctionCall() will have
			// just returned the ExecReturn, which needs to be executed separately.
			if (bodyExec != null) {
				bodyExec = bodyExec.executeForFunctionCall(record, context.recordStep());
				if (bodyExec instanceof ExecReturn) {
					ExecReturn execReturn = (ExecReturn) bodyExec;

					ICPPEvaluation returnValueEval = execReturn.getReturnValueEvaluation();
					// TODO(nathanridge): ExecReturn.executeForFunctionCall() already calls
					// computeForFunctionCall() on the return value evaluation. Why do we
					// need to do it again, and only if it's an EvalBinding?
					if (returnValueEval instanceof EvalBinding) {
						returnValueEval = returnValueEval.computeForFunctionCall(record, context.recordStep());
					}
					return returnValueEval;
				} else if (bodyExec == ExecIncomplete.INSTANCE) {
					return EvalFixed.INCOMPLETE;
				}
			}
		}
		return EvalFixed.INCOMPLETE;
	}

	private ICPPFunction resolveFunctionBinding() {
		ICPPFunction function = getOverload();
		if (function == null) {
			ICPPEvaluation funcEval = fArguments[0];
			if (funcEval instanceof EvalFunctionSet) {
				EvalFunctionSet funcEvalFunctionSet = (EvalFunctionSet) funcEval;
				funcEval = funcEvalFunctionSet.resolveFunction(Arrays.copyOfRange(fArguments, 1, fArguments.length));
			}

			IBinding binding = null;
			if (funcEval instanceof EvalBinding) {
				EvalBinding funcEvalBinding = (EvalBinding) funcEval;
				binding = funcEvalBinding.getBinding();
			} else if (funcEval instanceof EvalMemberAccess) {
				EvalMemberAccess funcEvalMemberAccess = (EvalMemberAccess) funcEval;
				binding = funcEvalMemberAccess.getMember();
			}

			if (binding instanceof ICPPFunction) {
				function = (ICPPFunction) binding;
			}
		}
		return function;
	}

	private boolean isReference(IBinding binding) {
		return binding instanceof IVariable && (((IVariable) binding).getType() instanceof ICPPReferenceType
				|| ((IVariable) binding).getType() instanceof IPointerType);
	}

	public static ActivationRecord createActivationRecord(ICPPParameter[] parameters, ICPPEvaluation[] arguments,
			ICPPEvaluation implicitThis) {
		ActivationRecord record = new ActivationRecord(parameters, implicitThis);

		// We start at arguments[1] because arguments[0] is the function's evaluation.
		int j = 1;
		for (ICPPParameter param : parameters) {
			if (param.isParameterPack() || isSpecializedParameterPack(param)) {
				// The parameter pack consumes all remaining arguments.
				int paramPackLen = arguments.length - j;
				ICPPEvaluation[] values = new ICPPEvaluation[paramPackLen];
				IType[] types = new IType[paramPackLen];
				for (int i = 0; i < paramPackLen; i++) {
					ICPPEvaluation arg = arguments[j + i];
					values[i] = arg;
					types[i] = arg.getType();
				}

				IValue paramPackValue = new CompositeValue(null, values);
				IType paramPackType = new ParameterPackType(types);
				EvalFixed paramPack = new EvalFixed(paramPackType, ValueCategory.PRVALUE, paramPackValue);
				record.update(param, paramPack);
				break;
			} else {
				if (j < arguments.length) {
					ICPPEvaluation argument = maybeApplyConversion(arguments[j++], param.getType(), false, true);
					record.update(param, argument);
				} else if (param.hasDefaultValue()) {
					IValue value = param.getDefaultValue();
					ICPPEvaluation eval = value.getEvaluation();
					if (eval == null) {
						eval = new EvalFixed(param.getType(), ValueCategory.PRVALUE, value);
					}
					record.update(param, eval);
				}
			}
		}
		return record;
	}

	private static boolean isSpecializedParameterPack(ICPPParameter param) {
		if (param instanceof ICPPSpecialization) {
			ICPPSpecialization paramSpecialization = (ICPPSpecialization) param;
			IBinding specializedBinding = paramSpecialization.getSpecializedBinding();
			if (specializedBinding instanceof ICPPParameter) {
				ICPPParameter specializedParam = (ICPPParameter) specializedBinding;
				return specializedParam.isParameterPack();
			}
		}
		return false;
	}

	@Override
	public int determinePackSize(ICPPTemplateParameterMap tpMap) {
		int r = CPPTemplates.PACK_SIZE_NOT_FOUND;
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

	public static class ParameterPackType implements IType {
		private final IType[] types;

		public ParameterPackType(IType[] types) {
			this.types = types;
		}

		public IType[] getTypes() {
			return types;
		}

		@Override
		public boolean isSameType(IType type) {
			return false;
		}

		@Override
		public Object clone() {
			try {
				return super.clone();
			} catch (CloneNotSupportedException e) {
			}
			return null;
		}
	}

	ICPPFunctionType resolveFunctionType() {
		ICPPFunction function = resolveFunctionBinding();
		if (function != null) {
			return function.getType();
		}
		IType result = fArguments[0].getType();
		if (result instanceof IPointerType) {
			result = ((IPointerType) result).getType();
		} else if (result instanceof CPPClosureType) {
			result = ((CPPClosureType) result).getFunctionCallOperator().getType();
		}
		if (result instanceof ICPPFunctionType) {
			return (ICPPFunctionType) result;
		}
		return null;
	}

	@Override
	public boolean isNoexcept() {
		ICPPFunctionType fctType = resolveFunctionType();
		if (fctType != null) {
			if (!EvalUtil.evaluateNoexceptSpecifier(fctType.getNoexceptSpecifier()))
				return false;
		}

		for (int i = 1; i < fArguments.length; i++) {
			ICPPEvaluation eval = fArguments[i];
			if (!eval.isNoexcept())
				return false;
		}
		return true;
	}
}
