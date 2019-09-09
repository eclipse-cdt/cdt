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

import java.util.Map;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.CompositeValue;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPDeferredFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPExecution;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.core.runtime.CoreException;

public final class EvalConstructor extends CPPDependentEvaluation {
	private final IType fType;
	private final ICPPConstructor fConstructor;
	private final ICPPEvaluation[] fArguments;
	private boolean fCheckedIsTypeDependent;
	private boolean fIsTypeDependent;
	private boolean fCheckedIsConstantExpression;
	private boolean fIsConstantExpression;
	private static final IASTName TEMP_NAME = ASTNodeFactoryFactory.getDefaultCPPNodeFactory().newName();

	public EvalConstructor(IType type, ICPPConstructor constructor, ICPPEvaluation[] arguments,
			IBinding templateDefinition) {
		super(templateDefinition);
		fType = type;
		fConstructor = constructor;
		fArguments = arguments != null ? arguments : ICPPEvaluation.EMPTY_ARRAY;
	}

	public EvalConstructor(IType type, ICPPConstructor constructor, ICPPEvaluation[] arguments,
			IASTNode pointOfDefinition) {
		this(type, constructor, arguments, findEnclosingTemplate(pointOfDefinition));
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
			fIsTypeDependent = CPPTemplates.isDependentType(fType) || containsDependentType(fArguments);
		}
		return fIsTypeDependent;
	}

	@Override
	public boolean isValueDependent() {
		if (CPPTemplates.isDependentType(fType))
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
		return fConstructor.isConstexpr() && areAllConstantExpressions(fArguments);
	}

	@Override
	public boolean isEquivalentTo(ICPPEvaluation other) {
		if (!(other instanceof EvalConstructor)) {
			return false;
		}
		EvalConstructor o = (EvalConstructor) other;
		return fType.isSameType(o.fType) && fConstructor == o.fConstructor
				&& areEquivalentEvaluations(fArguments, o.fArguments);
	}

	@Override
	public IType getType() {
		return fType;
	}

	public ICPPConstructor getConstructor() {
		return fConstructor;
	}

	public ICPPEvaluation[] getArguments() {
		return fArguments;
	}

	@Override
	public IValue getValue() {
		ICPPEvaluation computed = computeForFunctionCall(new ActivationRecord(), new ConstexprEvaluationContext());
		if (computed == this)
			return IntegralValue.ERROR;

		return computed.getValue();
	}

	@Override
	public ValueCategory getValueCategory() {
		return null;
	}

	@Override
	public ICPPEvaluation computeForFunctionCall(ActivationRecord callSiteRecord, ConstexprEvaluationContext context) {
		final IType unwrappedType = SemanticUtil.getNestedType(fType, TDEF | REF | CVTYPE);
		if (!(unwrappedType instanceof ICPPClassType)) {
			return this;
		}
		final ICPPClassType classType = (ICPPClassType) unwrappedType;
		final CompositeValue compositeValue = CompositeValue.create(classType);
		ICPPEvaluation[] argList = evaluateArguments(fArguments, callSiteRecord, context);
		EvalFixed constructedObject = new EvalFixed(fType, ValueCategory.PRVALUE, compositeValue);
		CPPVariable binding = new CPPVariable(TEMP_NAME);

		ActivationRecord localRecord = EvalFunctionCall.createActivationRecord(fConstructor.getParameters(), argList,
				constructedObject);
		localRecord.update(binding, constructedObject);

		ICPPExecution exec = fConstructor.getConstructorChainExecution();
		if (exec instanceof ExecConstructorChain) {
			ExecConstructorChain memberInitList = (ExecConstructorChain) exec;
			Map<IBinding, ICPPEvaluation> ccInitializers = memberInitList.getConstructorChainInitializers();
			for (Map.Entry<IBinding, ICPPEvaluation> ccInitializer : ccInitializers.entrySet()) {
				if (ccInitializer.getKey() instanceof ICPPConstructor) {
					ICPPClassType baseClassType = (ICPPClassType) ccInitializer.getKey().getOwner();
					final ICPPEvaluation memberEval = ccInitializer.getValue();
					ICPPEvaluation memberValue = memberEval.computeForFunctionCall(localRecord, context.recordStep());
					ICPPEvaluation[] baseClassValues = memberValue.getValue().getAllSubValues();

					ICPPField[] baseFields = ClassTypeHelper.getFields(baseClassType);
					for (ICPPField baseField : baseFields) {
						// TODO: This has the same problem with multiple inheritance as
						//       CompositeValue.create(ICPPClassType).
						int fieldPos = CPPASTFieldReference.getFieldPosition(baseField);
						constructedObject.getValue().setSubValue(fieldPos, baseClassValues[fieldPos]);
					}
				}
			}
		}

		ICPPField[] fields = classType.getDeclaredFields();
		for (ICPPField field : fields) {
			if (field.isStatic()) {
				continue;
			}
			final Map.Entry<IBinding, ICPPEvaluation> initializer = getInitializerFromMemberInitializerList(field,
					exec);

			ICPPEvaluation value = null;
			if (initializer != null) {
				ExecDeclarator declaratorExec = getDeclaratorExecutionFromMemberInitializerList(initializer);
				value = getFieldValue(declaratorExec, classType, localRecord, context);
			} else {
				value = EvalUtil.getVariableValue(field, localRecord);
			}
			final int fieldPos = CPPASTFieldReference.getFieldPosition(field);
			compositeValue.setSubValue(fieldPos, value);
		}

		// TODO(nathanridge): EvalFunctionCall.computeForFunctionCall() will:
		//    - evaluate the arguments again
		//    - create another ActivationRecord (inside evaluateFunctionBody())
		// Are these necessary?
		new EvalFunctionCall(argList, constructedObject, CPPSemantics.getCurrentLookupPoint())
				.computeForFunctionCall(localRecord, context.recordStep());
		return localRecord.getVariable(binding);
	}

	private Map.Entry<IBinding, ICPPEvaluation> getInitializerFromMemberInitializerList(ICPPField field,
			ICPPExecution exec) {
		if (!(exec instanceof ExecConstructorChain)) {
			return null;
		}

		final ExecConstructorChain initList = (ExecConstructorChain) exec;
		for (Map.Entry<IBinding, ICPPEvaluation> init : initList.getConstructorChainInitializers().entrySet()) {
			final IBinding member = init.getKey();
			if (member instanceof ICPPField && member.getName().equals(field.getName())) {
				return init;
			}
		}
		return null;
	}

	private ExecDeclarator getDeclaratorExecutionFromMemberInitializerList(
			Map.Entry<IBinding, ICPPEvaluation> ccInitializer) {
		final ICPPBinding member = (ICPPBinding) ccInitializer.getKey();
		final ICPPEvaluation memberEval = ccInitializer.getValue();
		return new ExecDeclarator(member, memberEval);
	}

	private ICPPEvaluation getFieldValue(ExecDeclarator declaratorExec, ICPPClassType classType,
			ActivationRecord record, ConstexprEvaluationContext context) {
		if (declaratorExec == null) {
			return null;
		}

		if (declaratorExec.executeForFunctionCall(record, context) != ExecIncomplete.INSTANCE) {
			final ICPPEvaluation value = record.getVariable(declaratorExec.getDeclaredBinding());
			return value;
		}
		return null;
	}

	public static ICPPEvaluation[] extractArguments(IASTInitializer initializer, ICPPConstructor constructor) {
		ICPPEvaluation[] args = extractArguments(initializer);
		if (initializer instanceof ICPPASTInitializerList) {
			ICPPASTInitializerList initList = (ICPPASTInitializerList) initializer;
			args = evaluateArguments(initList.getClauses());
		}
		if (args.length == 1 && constructor.getParameters().length > 1 && args[0] instanceof EvalInitList) {
			EvalInitList evalInitList = (EvalInitList) args[0];
			args = evalInitList.getClauses();
		}
		return args;
	}

	public static ICPPEvaluation[] extractArguments(IASTInitializer initializer) {
		if (initializer == null) {
			return ICPPEvaluation.EMPTY_ARRAY;
		} else if (initializer instanceof ICPPASTConstructorInitializer) {
			ICPPASTConstructorInitializer ctorInitializer = (ICPPASTConstructorInitializer) initializer;
			return evaluateArguments(ctorInitializer.getArguments());
		} else if (initializer instanceof ICPPASTInitializerList) {
			ICPPASTInitializerList initList = (ICPPASTInitializerList) initializer;
			ICPPEvaluation[] args = evaluateArguments(initList.getClauses());
			// the function name is misleading now (doesn't extract...)
			return new EvalInitList[] { new EvalInitList(args, initList) };
		} else if (initializer instanceof IASTEqualsInitializer) {
			IASTEqualsInitializer equalsInitalizer = (IASTEqualsInitializer) initializer;
			IASTInitializerClause initClause = equalsInitalizer.getInitializerClause();
			return evaluateArguments(initClause);
		} else {
			throw new IllegalArgumentException(
					initializer.getClass().getSimpleName() + " type of initializer is not supported"); //$NON-NLS-1$
		}
	}

	private static ICPPEvaluation[] evaluateArguments(IASTInitializerClause... clauses) {
		ICPPEvaluation[] args = new ICPPEvaluation[clauses.length];
		for (int i = 0; i < clauses.length; i++) {
			ICPPASTInitializerClause clause = (ICPPASTInitializerClause) clauses[i];
			args[i] = clause.getEvaluation();
		}
		return args;
	}

	private ICPPEvaluation[] evaluateArguments(ICPPEvaluation[] arguments, ActivationRecord record,
			ConstexprEvaluationContext context) {
		ICPPEvaluation[] argList = new ICPPEvaluation[arguments.length + 1];
		EvalBinding constructorBinding = new EvalBinding(fConstructor, fConstructor.getType(), getTemplateDefinition());
		argList[0] = constructorBinding;
		for (int i = 0; i < arguments.length; i++) {
			ICPPEvaluation evaluatedClause = arguments[i].computeForFunctionCall(record, context.recordStep());
			argList[i + 1] = evaluatedClause;
		}
		return argList;
	}

	@Override
	public int determinePackSize(ICPPTemplateParameterMap tpMap) {
		int r = CPPTemplates.determinePackSize(fType, tpMap);
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
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.EVAL_CONSTRUCTOR);
		buffer.marshalType(fType);
		buffer.marshalBinding(fConstructor);
		buffer.putInt(fArguments.length);
		for (ICPPEvaluation arg : fArguments) {
			buffer.marshalEvaluation(arg, includeValue);
		}
		marshalTemplateDefinition(buffer);
	}

	public static ICPPEvaluation unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		IType type = buffer.unmarshalType();
		ICPPConstructor constructor = (ICPPConstructor) buffer.unmarshalBinding();
		int len = buffer.getInt();
		ICPPEvaluation[] arguments = new ICPPEvaluation[len];
		for (int i = 0; i < arguments.length; i++) {
			arguments[i] = buffer.unmarshalEvaluation();
		}
		IBinding templateDefinition = buffer.unmarshalBinding();
		return new EvalConstructor(type, constructor, arguments, templateDefinition);
	}

	@Override
	public ICPPEvaluation instantiate(InstantiationContext context, int maxDepth) {
		ICPPEvaluation[] newArguments = new ICPPEvaluation[fArguments.length];
		for (int i = 0; i < fArguments.length; i++) {
			newArguments[i] = fArguments[i].instantiate(context, maxDepth);
		}

		IType newType = null;
		ICPPConstructor newConstructor;
		try {
			newConstructor = (ICPPConstructor) CPPTemplates.instantiateBinding(fConstructor, context, maxDepth);
			if (newConstructor != null) {
				newType = newConstructor.getClassOwner();
			}
			if (newConstructor instanceof CPPDeferredFunction) {
				ICPPFunction[] candidates = ((CPPDeferredFunction) newConstructor).getCandidates();
				if (candidates != null) {
					CPPFunctionSet functionSet = new CPPFunctionSet(candidates, new ICPPTemplateArgument[] {}, null);
					EvalFunctionSet evalFunctionSet = new EvalFunctionSet(functionSet, false, false, newType,
							CPPSemantics.getCurrentLookupPoint());
					ICPPEvaluation resolved = evalFunctionSet.resolveFunction(newArguments);
					if (resolved instanceof EvalBinding) {
						EvalBinding evalBinding = (EvalBinding) resolved;
						newConstructor = (ICPPConstructor) evalBinding.getBinding();
						if (newConstructor != null) {
							newType = newConstructor.getClassOwner();
						}
					}
				}
			}
		} catch (DOMException e) {
			newConstructor = fConstructor;
		}

		// Only instantiate fType separately if we couldn't get the instantiated type
		// via newConstructor.getClassOwner() for some reason. This is not just for
		// efficiency; instantiating fType directly will not work if fType is a
		// CPPClassTemplate because CPPTemplates.instantiateType() does not instantiate
		// CPPClassTemplates, only CPPDeferredClassInstances (TODO: why?).
		if (newType == null) {
			newType = CPPTemplates.instantiateType(fType, context);
		}

		return new EvalConstructor(newType, newConstructor, newArguments, getTemplateDefinition());
	}

	@Override
	public boolean isNoexcept() {
		return EvalUtil.evaluateNoexceptSpecifier(fConstructor.getType().getNoexceptSpecifier());
	}
}
