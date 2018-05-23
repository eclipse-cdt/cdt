/*******************************************************************************
 * Copyright (c) 2019 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Thomas Corbat (IFS) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor.IS_STATIC_VARIABLE;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTStructuredBindingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalBinary;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFunctionCall;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalMemberAccess;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.LookupData;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

/**
 * Represents the implicit object created for the initializer of a structured binding declaration.
 * It is used for caching the evaluation strategy used to determine the <code>ICPPEvaluation</code> of
 * the names introduced by the declaration.
 *
 */
public class CPPStructuredBindingComposite extends CPPImplicitVariable {

	private static final char[] GET_NAME = "get".toCharArray(); //$NON-NLS-1$
	private static final IStructuredBindingNameEvaluationStrategy INCOMPLETE_INITIALIZER = new IStructuredBindingNameEvaluationStrategy() {
		@Override
		public ICPPEvaluation getEvaluation(int nameIndex) {
			return EvalFixed.INCOMPLETE;
		}
	};

	private IStructuredBindingNameEvaluationStrategy evaluationStrategy = null;
	private ICPPASTStructuredBindingDeclaration declaration;

	public CPPStructuredBindingComposite(IASTImplicitName name, ICPPEvaluation initializerEvaluation) {
		super(name, initializerEvaluation);
		IASTNode nameParent = name.getParent();
		assert (nameParent instanceof ICPPASTStructuredBindingDeclaration);
		this.declaration = (ICPPASTStructuredBindingDeclaration) nameParent;
	}

	public ICPPEvaluation getEvaluationForName(IASTName name) {
		int index = ArrayUtil.indexOf(declaration.getNames(), name);
		return getNameEvaluationStrategy().getEvaluation(index);
	}

	private IStructuredBindingNameEvaluationStrategy getNameEvaluationStrategy() {
		if (evaluationStrategy == null) {
			evaluationStrategy = createEvaluationStrategy(declaration);
		}
		return evaluationStrategy;
	}

	private IStructuredBindingNameEvaluationStrategy createEvaluationStrategy(
			ICPPASTStructuredBindingDeclaration structuredBinding) {
		Optional<IASTInitializer> initializer = structuredBinding.getInitializer();
		ICPPEvaluation initializerEvaluation = getInitializerEvaluation();
		IType initializerType = getType();
		if (initializerEvaluation != null) {
			IType unwrappedType = SemanticUtil.getNestedType(initializerType, SemanticUtil.ALLCVQ | SemanticUtil.REF);
			if (unwrappedType instanceof IArrayType) {
				return new ArrayElement(initializer.get(), initializerEvaluation);
			}
			if (unwrappedType instanceof ICPPClassType) {
				IASTName[] definedNames = structuredBinding.getNames();
				long numberOfNames = definedNames.length;
				ICPPClassType classType = (ICPPClassType) unwrappedType;
				if (numberOfNames > 0) {
					IScope scope = CPPSemantics.getLookupScope(definedNames[0]);
					Optional<ICPPClassSpecialization> tupleSizeInstance = CPPVisitor
							.findTupleSizeWithValueMember(unwrappedType, scope, structuredBinding);
					if (tupleSizeInstance.isPresent()) {
						if (CPPVisitor.hasConstexprStaticIntegralValueField(tupleSizeInstance.get(), numberOfNames)) {
							return new TupleElement(initializer.get(), initializerEvaluation, classType);
						}
					} else {
						return new MemberElement(initializer.get(), initializerEvaluation, classType);
					}
				}
			}
		}
		return INCOMPLETE_INITIALIZER;
	}

	private abstract static class IStructuredBindingNameEvaluationStrategy {
		abstract public ICPPEvaluation getEvaluation(int nameIndex);
	}

	/**
	 * Name evaluation strategy for the case in which the initializer has array type.
	 */
	private class ArrayElement extends IStructuredBindingNameEvaluationStrategy {
		private IASTInitializer initializer;
		private ICPPEvaluation initializerEvaluation;

		private ArrayElement(IASTInitializer initializer, ICPPEvaluation initializerEvaluation) {
			this.initializer = initializer;
			this.initializerEvaluation = initializerEvaluation;
		}

		@Override
		public ICPPEvaluation getEvaluation(int nameIndex) {
			EvalFixed indexEvaluation = new EvalFixed(CPPBasicType.UNSIGNED_INT, ValueCategory.PRVALUE,
					IntegralValue.create(nameIndex));
			return new EvalBinary(EvalBinary.op_arrayAccess, initializerEvaluation, indexEvaluation, initializer);
		}
	}

	/**
	 * Name evaluation strategy for the case in which the initializer has class type for which std::tuple_size is specialized.
	 */
	private class TupleElement extends IStructuredBindingNameEvaluationStrategy {
		private IASTInitializer initializer;
		private ICPPEvaluation initializerEvaluation;
		private ICPPClassType initializerType;

		private TupleElement(IASTInitializer initializer, ICPPEvaluation initializerEvaluation,
				ICPPClassType initializerType) {
			this.initializer = initializer;
			this.initializerEvaluation = initializerEvaluation;
			this.initializerType = initializerType;
		}

		@Override
		public ICPPEvaluation getEvaluation(int nameIndex) {
			EvalFixed indexEvaluation = new EvalFixed(CPPBasicType.UNSIGNED_INT, ValueCategory.PRVALUE,
					IntegralValue.create(nameIndex));
			IBinding resolvedFunction = resolveGetFunction(initializerType, initializer, initializerEvaluation,
					indexEvaluation);
			if (resolvedFunction instanceof ICPPMethod) {
				ICPPEvaluation eExpressionEvaluation = new EvalMemberAccess(initializerType,
						initializerEvaluation.getValueCategory(), resolvedFunction, initializerEvaluation, false,
						initializer);
				return new EvalFunctionCall(new ICPPEvaluation[] { eExpressionEvaluation }, null, initializer);
			} else if (resolvedFunction instanceof ICPPFunction) {
				EvalBinding functionEvaluation = new EvalBinding(resolvedFunction,
						((ICPPFunction) resolvedFunction).getType(), initializer);
				return new EvalFunctionCall(new ICPPEvaluation[] { functionEvaluation, initializerEvaluation }, null,
						initializer);
			}
			return EvalFixed.INCOMPLETE;
		}

		/**
		 *
		 * Resolves the member or non-member get() function template for a type and given arguments.
		 *
		 * Returns {@code null} if the function cannot be resolved
		 */
		private IBinding resolveGetFunction(ICompositeType classType, IASTNode point, ICPPEvaluation argument,
				ICPPEvaluation index) {
			//find member and non-member get() bindings in class scope and all parent scopes
			IBinding[] allGetBindings = CPPSemantics.findBindings(classType.getCompositeScope(), GET_NAME, false,
					point);
			ICPPFunction[] functions = Arrays.stream(allGetBindings).filter(IFunction.class::isInstance)
					.map(IFunction.class::cast).toArray(ICPPFunction[]::new);
			ICPPTemplateArgument[] arguments = new ICPPTemplateArgument[] { new CPPTemplateNonTypeArgument(index) };
			LookupData lookupGet = new LookupData(GET_NAME, arguments, point);
			//LookupData::containsImpliedObject is ignored for non-member functions in CPPSemantics.resolveFunction
			//and will therefore find the the free get function using the arguments
			lookupGet.setFunctionArguments(true, argument);
			try {
				return CPPSemantics.resolveFunction(lookupGet, functions, true, true);
			} catch (DOMException e) {
				return null;
			}
		}
	}

	/**
	 * Name evaluation strategy for the case in which the initializer has class type with public members.
	 */
	private class MemberElement extends IStructuredBindingNameEvaluationStrategy {
		private IASTInitializer initializer;
		private ICPPEvaluation initializerEvaluation;
		private ICPPClassType initializerType;

		private MemberElement(IASTInitializer initializer, ICPPEvaluation initializerEvaluation,
				ICPPClassType initializerType) {
			this.initializer = initializer;
			this.initializerEvaluation = initializerEvaluation;
			this.initializerType = initializerType;
		}

		@Override
		public ICPPEvaluation getEvaluation(int nameIndex) {
			IField[] nonStaticFields = Arrays.stream(ClassTypeHelper.getFields(initializerType))
					.filter(IS_STATIC_VARIABLE.negate()).toArray(IField[]::new);
			//TODO (tcorbat): Further restrictions to structured bindings that are not checked. Maybe add a Codan checker.
			// * Add further check that all members origin from the same class/base class
			// * classType must not have an anonymous union member
			// * Check accessibility of the members
			if (nameIndex >= 0 && nameIndex < nonStaticFields.length) {
				IField boundField = nonStaticFields[nameIndex];
				return new EvalMemberAccess(initializerType, initializerEvaluation.getValueCategory(), boundField,
						initializerEvaluation, false, initializer);
			}
			return EvalFixed.INCOMPLETE;
		}
	}
}
