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

import java.util.Map;
import java.util.Map.Entry;

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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.CompositeValue;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPDeferredFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluationOwner;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPExecution;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.core.runtime.CoreException;

public class EvalConstructor extends CPPDependentEvaluation {
	private final IType type;
	private final ICPPConstructor constructor;
	private final ICPPEvaluation[] arguments;
	private boolean checkedIsTypeDependent;
	private boolean isTypeDependent;
	static private final IASTName tempName = ASTNodeFactoryFactory.getDefaultCPPNodeFactory().newName();

	public EvalConstructor(IType type, ICPPConstructor constructor, ICPPEvaluation[] arguments, IBinding templateDefinition) {
		super(templateDefinition);
		this.type = type;
		this.constructor = constructor;
		this.arguments = arguments != null ? arguments : new ICPPEvaluation[0];
	}

	public EvalConstructor(IType type, ICPPConstructor constructor, ICPPEvaluation[] arguments, IASTNode pointOfDefinition) {
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
		if (!checkedIsTypeDependent) {
			checkedIsTypeDependent = true;
			isTypeDependent = CPPTemplates.isDependentType(type) || containsDependentType(arguments);
		}
		return isTypeDependent;
	}

	@Override
	public boolean isValueDependent() {
		if (CPPTemplates.isDependentType(type))
			return true;
		for (ICPPEvaluation arg : arguments) {
			if (arg.isValueDependent())
				return true;
		}
		return false;
	}

	@Override
	public boolean isConstantExpression(IASTNode point) {
		return true;
	}

	@Override
	public IType getType(IASTNode point) {
		return type;
	}

	@Override
	public IValue getValue(IASTNode point) {
		// An EvalConstructor is never used to directly represent the evaluation of an expression.
		// It only comes up while evaluating other evaluations. As such, its getValue() doesn't
		// do anything; computeForFunctionCall() must be called on it to obtain a useful result.
		return IntegralValue.ERROR;
	}

	@Override
	public ValueCategory getValueCategory(IASTNode point) {
		return null;
	}

	@Override
	public ICPPEvaluation computeForFunctionCall(ActivationRecord callSiteRecord, ConstexprEvaluationContext context) {
		final ICPPClassType classType = (ICPPClassType)SemanticUtil.getNestedType(type, TDEF | REF | CVTYPE);
		final CompositeValue compositeValue = CompositeValue.create(classType);
		ICPPEvaluation[] argList = evaluateArguments(arguments, callSiteRecord, context);
		EvalFixed constructedObject = new EvalFixed(type, ValueCategory.PRVALUE, compositeValue);
		CPPVariable binding = new CPPVariable(tempName);

		ActivationRecord localRecord = EvalFunctionCall.createActivationRecord(
				this.constructor.getParameters(), argList, constructedObject, context.getPoint());
		localRecord.update(binding, constructedObject);

		ICPPExecution exec = constructor.getConstructorChainExecution(context.getPoint());
		if (exec instanceof ExecConstructorChain) {
			ExecConstructorChain memberInitList = (ExecConstructorChain) exec;
			Map<IBinding, ICPPEvaluation> ccInitializers = memberInitList.getConstructorChainInitializers();
			for (Entry<IBinding, ICPPEvaluation> ccInitializer : ccInitializers.entrySet()) {
				if (ccInitializer.getKey() instanceof ICPPConstructor) {
					ICPPClassType baseClassType = (ICPPClassType) ccInitializer.getKey().getOwner();
					final ICPPEvaluation memberEval = ccInitializer.getValue();
					ICPPEvaluation memberValue =  memberEval.computeForFunctionCall(localRecord, context.recordStep());
					ICPPEvaluation[] baseClassValues = memberValue.getValue(context.getPoint()).getAllSubValues();

					ICPPField[] baseFields = (ICPPField[])ClassTypeHelper.getFields(baseClassType, context.getPoint());
					for (ICPPField baseField : baseFields) {
						// TODO: This has the same problem with multiple inheritance as
						//       CompositeValue.create(ICPPClassType).
						int fieldPos = CPPASTFieldReference.getFieldPosition(baseField);
						constructedObject.getValue(context.getPoint()).setSubValue(fieldPos,
								baseClassValues[fieldPos]);
					}
				}
			}
		}


		ICPPField[] fields = ClassTypeHelper.getDeclaredFields(classType, context.getPoint());
		for (ICPPField field : fields) {
			final Entry<IBinding, ICPPEvaluation> ccInitializer = getInitializerFromMemberInitializerList(field, exec);

			ICPPEvaluation value = null;
			if (ccInitializer != null) {
				ExecDeclarator declaratorExec = getDeclaratorExecutionFromMemberInitializerList(ccInitializer);
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
		new EvalFunctionCall(argList, constructedObject, context.getPoint()).computeForFunctionCall(
				localRecord, context.recordStep());
		ICPPEvaluation resultingObject = localRecord.getVariable(binding);
		return resultingObject;
	}

	private Entry<IBinding, ICPPEvaluation> getInitializerFromMemberInitializerList(ICPPField field, ICPPExecution exec) {
		if (!(exec instanceof ExecConstructorChain)) {
			return null;
		}

		final ExecConstructorChain memberInitList = (ExecConstructorChain) exec;
		for (Entry<IBinding, ICPPEvaluation> ccInitializer : memberInitList.getConstructorChainInitializers().entrySet()) {
			final IBinding member = ccInitializer.getKey();
			if (member instanceof ICPPField && member.getName().equals(field.getName())) {
				return ccInitializer;
			}
		}
		return null;
	}

	private ExecDeclarator getDeclaratorExecutionFromMemberInitializerList(Entry<IBinding, ICPPEvaluation> ccInitializer) {
		final ICPPBinding member = (ICPPBinding) ccInitializer.getKey();
		final ICPPEvaluation memberEval = ccInitializer.getValue();
		return new ExecDeclarator(member, memberEval);
	}

	private ICPPEvaluation getFieldValue(ExecDeclarator declaratorExec, ICPPClassType classType, ActivationRecord record, ConstexprEvaluationContext context) {
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
		if (args.length == 1 && constructor.getParameters().length > 1 && args[0] instanceof EvalInitList) {
			EvalInitList evalInitList = (EvalInitList) args[0];
			args = evalInitList.getClauses();
		}
		return args;
	}

	public static ICPPEvaluation[] extractArguments(IASTInitializer initializer) {
		if (initializer == null) {
			return new ICPPEvaluation[0];
		} else if (initializer instanceof ICPPASTConstructorInitializer) {
			ICPPASTConstructorInitializer ctorInitializer = (ICPPASTConstructorInitializer) initializer;
			return evaluateArguments(ctorInitializer.getArguments());
		} else if (initializer instanceof ICPPASTInitializerList) {
			ICPPASTInitializerList initList = (ICPPASTInitializerList) initializer;
			return evaluateArguments(initList.getClauses());
		} else if (initializer instanceof IASTEqualsInitializer) {
			IASTEqualsInitializer equalsInitalizer = (IASTEqualsInitializer) initializer;
			IASTInitializerClause initClause = equalsInitalizer.getInitializerClause();
			return evaluateArguments(initClause);
		} else {
			throw new RuntimeException("this type of initializer is not supported"); //$NON-NLS-1$
		}
	}

	private static ICPPEvaluation[] evaluateArguments(IASTInitializerClause... clauses) {
		ICPPEvaluation[] args = new ICPPEvaluation[clauses.length];
		for (int i = 0; i < clauses.length; i++) {
			ICPPEvaluationOwner clause = (ICPPEvaluationOwner) clauses[i];
			args[i] = clause.getEvaluation();
		}
		return args;
	}

	private ICPPEvaluation[] evaluateArguments(ICPPEvaluation[] arguments, ActivationRecord record,	ConstexprEvaluationContext context) {
		ICPPEvaluation[] argList = new ICPPEvaluation[arguments.length + 1];
		EvalBinding constructorBinding = new EvalBinding(constructor, constructor.getType(), getTemplateDefinition());
		argList[0] = constructorBinding;
		for (int i = 0; i < arguments.length; i++) {
			ICPPEvaluation evaluatedClause = arguments[i].computeForFunctionCall(record, context.recordStep());
			argList[i+1] = evaluatedClause;
		}
		return argList;
	}


	@Override
	public int determinePackSize(ICPPTemplateParameterMap tpMap) {
		int r = CPPTemplates.determinePackSize(type, tpMap);
		for (ICPPEvaluation arg : arguments) {
			r = CPPTemplates.combinePackSize(r, arg.determinePackSize(tpMap));
		}
		return r;
	}

	@Override
	public boolean referencesTemplateParameter() {
		for (ICPPEvaluation arg : arguments) {
			if (arg.referencesTemplateParameter())
				return true;
		}
		return false;
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer, boolean includeValue) throws CoreException {
		buffer.putShort(ITypeMarshalBuffer.EVAL_CONSTRUCTOR);
		buffer.marshalType(type);
		buffer.marshalBinding(constructor);
		buffer.putInt(arguments.length);
		for (ICPPEvaluation arg : arguments) {
			buffer.marshalEvaluation(arg, includeValue);
		}
		marshalTemplateDefinition(buffer);
	}

	public static ISerializableEvaluation unmarshal(short firstBytes, ITypeMarshalBuffer buffer) throws CoreException {
		IType type = buffer.unmarshalType();
		ICPPConstructor constructor = (ICPPConstructor) buffer.unmarshalBinding();
		int len = buffer.getInt();
		ICPPEvaluation[] arguments = new ICPPEvaluation[len];
		for (int i = 0; i < arguments.length; i++) {
			arguments[i] = (ICPPEvaluation) buffer.unmarshalEvaluation();
		}
		IBinding templateDefinition = buffer.unmarshalBinding();
		return new EvalConstructor(type, constructor, arguments, templateDefinition);
	}

	@Override
	public ICPPEvaluation instantiate(InstantiationContext context, int maxDepth) {
		IType newType = CPPTemplates.instantiateType(type, context);

		ICPPEvaluation[] newArguments = new ICPPEvaluation[arguments.length];
		for (int i = 0; i < arguments.length; i++) {
			newArguments[i] = arguments[i].instantiate(context, maxDepth);
		}

		ICPPConstructor newConstructor;
		try {
			newConstructor = (ICPPConstructor)CPPTemplates.instantiateBinding(constructor, context, maxDepth);
			if (newConstructor instanceof CPPDeferredFunction) {
				ICPPFunction[] candidates = ((CPPDeferredFunction) newConstructor).getCandidates();
				if (candidates != null) {
					CPPFunctionSet functionSet = new CPPFunctionSet(candidates, new ICPPTemplateArgument[]{}, null);
					EvalFunctionSet evalFunctionSet = new EvalFunctionSet(functionSet, false, false, newType,
							context.getPoint());
					ICPPEvaluation resolved = evalFunctionSet.resolveFunction(newArguments, context.getPoint());
					if (resolved instanceof EvalBinding) {
						EvalBinding evalBinding = (EvalBinding) resolved;
						newConstructor = (ICPPConstructor) evalBinding.getBinding();
					}
				}
			}
		} catch (DOMException e) {
			newConstructor = constructor;
		}

		return new EvalConstructor(newType, newConstructor, newArguments, getTemplateDefinition());
	}
}
