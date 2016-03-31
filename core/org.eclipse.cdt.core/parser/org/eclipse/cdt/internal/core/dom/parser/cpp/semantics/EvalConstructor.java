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

import java.util.List;

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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
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
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPExecution;
import org.eclipse.cdt.internal.core.dom.parser.cpp.InstantiationContext;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalUtil.Pair;
import org.eclipse.core.runtime.CoreException;

public class EvalConstructor extends CPPDependentEvaluation {
	private final IType type;
	private final ICPPConstructor constructor;
	private final ICPPEvaluation[] arguments;
	private final IASTName tempName = ASTNodeFactoryFactory.getDefaultCPPNodeFactory().newName();
	
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
		return false;
	}

	@Override
	public boolean isValueDependent() {
		return false;
	}

	@Override
	public boolean isConstantExpression(IASTNode point) {
		return true;
	}

	@Override
	public IType getType(IASTNode point) {
		return null;
	}

	@Override
	public IValue getValue(IASTNode point) {
		return EvalFixed.INCOMPLETE.getValue(point);
	}

	@Override
	public ValueCategory getValueCategory(IASTNode point) {
		return null;
	}

	@Override
	public ICPPEvaluation computeForFunctionCall(ActivationRecord record, ConstexprEvaluationContext context) {
		final ICPPClassType classType = (ICPPClassType)SemanticUtil.getNestedType(type, TDEF | REF | CVTYPE);
		final CompositeValue compositeValue = CompositeValue.createForConstructor(classType);
		ICPPEvaluation[] argList = evaluateArguments(arguments, record, context);
		EvalFixed constructedObject = new EvalFixed(type, ValueCategory.PRVALUE, compositeValue);
		CPPVariable binding = new CPPVariable(tempName);
		EvalBinding implicitThis = new EvalBinding(binding, constructedObject.getType(), constructedObject.getTemplateDefinition());
		
		ActivationRecord activationRecord = EvalFunctionCall.createActivationRecord(this.constructor.getParameters(), argList, implicitThis, context.getPoint());
		activationRecord.update(binding, constructedObject);
		
		ICPPExecution exec = CPPFunction.getConstructorChainExecution(constructor);
		if(exec instanceof ExecConstructorChain) {
			ExecConstructorChain memberInitList = (ExecConstructorChain)exec;
			List<Pair<IBinding, ICPPEvaluation>> ccInitializers = memberInitList.getConstructorChainInitializers();
			for(Pair<IBinding, ICPPEvaluation> ccInitializer : ccInitializers) {
				if(ccInitializer.getFirst() instanceof ICPPConstructor) {
					ICPPClassType baseClassType = (ICPPClassType)ccInitializer.getFirst().getOwner();
					final ICPPEvaluation memberEval = ccInitializer.getSecond();
					ICPPEvaluation memberValue =  memberEval.computeForFunctionCall(record, context.recordStep());
					ICPPEvaluation[] baseClassValues = memberValue.getValue(null).getAll();	
							
					ICPPField[] baseFields = (ICPPField[])ClassTypeHelper.getFields(baseClassType, null);
					for(ICPPField baseField : baseFields) {
						int fieldPos = CPPASTFieldReference.getFieldPosition(baseField);
						new EvalCompositeAccess(constructedObject, fieldPos).update(baseClassValues[fieldPos]);
					}
				}		
			}
		}
		
		
		ICPPField[] fields = ClassTypeHelper.getDeclaredFields(classType, null);
		for (ICPPField field : fields) {
			final Pair<IBinding, ICPPEvaluation> ccInitializer = getInitializerFromMemberInitializerList(field, exec);
			
			ICPPEvaluation value = null;
			if(ccInitializer != null) {
				ExecDeclarator declaratorExec = getDeclaratorExecutionFromMemberInitializerList(ccInitializer);
				value = getFieldValue(declaratorExec, classType, activationRecord, context);
			} else {
				InstantiationContext instantiationContext = null;
				if(classType instanceof ICPPClassSpecialization) {
					final ICPPClassSpecialization classSpecType = (ICPPClassSpecialization)classType;
					instantiationContext = new InstantiationContext(classSpecType.getTemplateParameterMap(), classSpecType, null);
				}
				value = EvalUtil.getVariableValue(field, activationRecord, instantiationContext);
			}
			final int fieldPos = CPPASTFieldReference.getFieldPosition(field);
			compositeValue.set(fieldPos, value);
		}
		
		new EvalFunctionCall(argList, implicitThis, context.getPoint()).computeForFunctionCall(activationRecord, context.recordStep());
		ICPPEvaluation resultingObject = activationRecord.getVariable(binding);
		return resultingObject;
	}
	
	private Pair<IBinding, ICPPEvaluation> getInitializerFromMemberInitializerList(ICPPField field, ICPPExecution exec) {
		if(!(exec instanceof ExecConstructorChain)) {
			return null;
		}
		
		final ExecConstructorChain memberInitList = (ExecConstructorChain)exec;
		for(Pair<IBinding, ICPPEvaluation> ccInitializer : memberInitList.getConstructorChainInitializers()) {
			final IBinding member = ccInitializer.getFirst();
			if(member instanceof ICPPField && member.getName().equals(field.getName())) {
				return ccInitializer;
			}
		}
		return null;
	}
	
	private ExecDeclarator getDeclaratorExecutionFromMemberInitializerList(Pair<IBinding, ICPPEvaluation> ccInitializer) {
		final ICPPBinding member = (ICPPBinding)ccInitializer.getFirst();
		final ICPPEvaluation memberEval = ccInitializer.getSecond();
		return new ExecDeclarator(member, memberEval);
	}
	
	private ICPPEvaluation getFieldValue(ExecDeclarator declaratorExec, ICPPClassType classType, ActivationRecord record, ConstexprEvaluationContext context) {
		if(declaratorExec == null) {
			return null;
		}
		
		if(classType instanceof ICPPClassSpecialization) {
			final ICPPClassSpecialization classSpecType = (ICPPClassSpecialization)classType;
			InstantiationContext instantiationContext = new InstantiationContext(classSpecType.getTemplateParameterMap(), classSpecType, null);
			declaratorExec = (ExecDeclarator)declaratorExec.instantiate(instantiationContext, IntegralValue.MAX_RECURSION_DEPTH);
		}
		
		if(declaratorExec.executeForFunctionCall(record, context) != ExecIncomplete.INSTANCE) {
			final ICPPEvaluation value = record.getVariable(declaratorExec.getDeclaredBinding());
			return value;
		}
		return null;
	}
	
	public static ICPPEvaluation[] extractArguments(IASTInitializer initializer, ICPPConstructor constructor) {
		int paramCount = constructor.getParameters().length;
		ICPPEvaluation[] args = extractArguments(initializer);
		if(args.length == 1 && paramCount > 1 && args[0] instanceof EvalInitList) {
			EvalInitList evalInitList = (EvalInitList)args[0];
			args = evalInitList.getClauses();
		}
		return args;
	}

	public static ICPPEvaluation[] extractArguments(IASTInitializer initializer) {
		if (initializer == null) {
			return new ICPPEvaluation[0];
		} else if(initializer instanceof ICPPASTConstructorInitializer) {
			ICPPASTConstructorInitializer ctorInitializer = (ICPPASTConstructorInitializer)initializer;
			return evaluateArguments(ctorInitializer.getArguments());
		} else if(initializer instanceof ICPPASTInitializerList) {
			ICPPASTInitializerList initList = (ICPPASTInitializerList)initializer;
			return evaluateArguments(initList.getClauses());
		} else if(initializer instanceof IASTEqualsInitializer) {
			IASTEqualsInitializer equalsInitalizer = (IASTEqualsInitializer)initializer;
			IASTInitializerClause initClause = equalsInitalizer.getInitializerClause();
			return evaluateArguments(initClause);
		} else {
			throw new RuntimeException("this type of initializer is not supported"); //$NON-NLS-1$
		}
	}
	
	private static ICPPEvaluation[] evaluateArguments(IASTInitializerClause... clauses) {
		ICPPEvaluation[] args = new ICPPEvaluation[clauses.length]; 
		for (int i = 0; i < clauses.length; i++) {
			ICPPASTInitializerClause clause = (ICPPASTInitializerClause)clauses[i];
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
		return 0;
	}

	@Override
	public boolean referencesTemplateParameter() {
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
		ICPPConstructor constructor = (ICPPConstructor)buffer.unmarshalBinding();
		int len = buffer.getInt();
		ICPPEvaluation[] arguments = new ICPPEvaluation[len];
		for (int i = 0; i < arguments.length; i++) {
			arguments[i] = (ICPPEvaluation)buffer.unmarshalEvaluation();
		}
		IBinding templateDefinition = buffer.unmarshalBinding();
		return new EvalConstructor(type, constructor, arguments, templateDefinition);
	}

	@Override
	public ICPPEvaluation instantiate(InstantiationContext context, int maxDepth) {
		IType newType = CPPTemplates.instantiateType(type, context);
		
		ICPPEvaluation[] newArguments = new ICPPEvaluation[arguments.length];
		for(int i = 0; i < arguments.length; i++) {
			newArguments[i] = arguments[i].instantiate(context, maxDepth);
		}
		
		ICPPConstructor newConstructor;
		try {
			newConstructor = (ICPPConstructor)CPPTemplates.instantiateBinding(constructor, context, maxDepth);
			if(newConstructor instanceof CPPDeferredFunction) {
				ICPPFunction[] candidates = ((CPPDeferredFunction) newConstructor).getCandidates();
				if (candidates != null) {
					CPPFunctionSet functionSet = new CPPFunctionSet(candidates, new ICPPTemplateArgument[]{}, null);
					EvalFunctionSet evalFunctionSet = new EvalFunctionSet(functionSet, false, false, null, context.getPoint());
					ICPPEvaluation resolved = evalFunctionSet.resolveFunction(newArguments, context.getPoint());
					if(resolved instanceof EvalBinding) {
						EvalBinding evalBinding = (EvalBinding)resolved;
						newConstructor = (ICPPConstructor)evalBinding.getBinding();
					}
				}
			}
		} catch (DOMException e) {
			newConstructor = constructor;
		}
		
		return new EvalConstructor(newType, newConstructor, newArguments, getTemplateDefinition());
	}
}
