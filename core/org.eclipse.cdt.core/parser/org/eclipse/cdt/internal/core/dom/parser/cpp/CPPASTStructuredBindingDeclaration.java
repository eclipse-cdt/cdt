/*******************************************************************************
 * Copyright (c) 2018 Institute for Software, HSR Hochschule fuer Technik
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

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator.RefQualifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTStructuredBindingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
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
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExecDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExecIncomplete;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExecSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.LookupData;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

public class CPPASTStructuredBindingDeclaration extends CPPASTSimpleDeclaration
		implements ICPPASTStructuredBindingDeclaration {
	private static final char[] GET_NAME = "get".toCharArray(); //$NON-NLS-1$

	private RefQualifier refQualifier;
	private IASTName[] names;
	private IASTInitializer initializer;
	private INameEvaluationStrategy nameEvaluationStrategy = null;

	private static final INameEvaluationStrategy INCOMPLETE_INITIALIZER = index -> EvalFixed.INCOMPLETE;

	public CPPASTStructuredBindingDeclaration() {
	}

	public CPPASTStructuredBindingDeclaration(IASTDeclSpecifier declSpecifier, RefQualifier refQualifier,
			IASTName[] names, IASTInitializer initializer) {
		super(declSpecifier);
		this.refQualifier = refQualifier;
		for (IASTName name : names) {
			addName(name);
		}
		setInitializer(initializer);
	}

	@Override
	public Optional<RefQualifier> getRefQualifier() {
		return Optional.ofNullable(refQualifier);
	}

	public void setRefQualifier(RefQualifier refQualifier) {
		assertNotFrozen();
		this.refQualifier = refQualifier;
	}

	@Override
	public IASTName[] getNames() {
		if (names == null) {
			return IASTName.EMPTY_NAME_ARRAY;
		}
		names = ArrayUtil.trim(names);
		return names;
	}

	@Override
	public Optional<IASTInitializer> getInitializer() {
		return Optional.ofNullable(initializer);
	}

	@Override
	public boolean accept(ASTVisitor action) {
		if (action.shouldVisitDeclarations) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}

		IASTDeclSpecifier declSpecifier = getDeclSpecifier();
		if (declSpecifier != null && !declSpecifier.accept(action)) {
			return false;
		}

		for (IASTName name : getNames()) {
			if (!name.accept(action)) {
				return false;
			}
		}

		if (initializer != null && !initializer.accept(action)) {
			return false;
		}

		if (action.shouldVisitDeclarations) {
			switch (action.leave(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		return true;
	}

	protected void addName(IASTName name) {
		assertNotFrozen();
		if (name != null) {
			names = ArrayUtil.append(IASTName.class, names, name);
			name.setParent(this);
			name.setPropertyInParent(IDENTIFIER);
		}
	}

	protected void setInitializer(IASTInitializer initializer) {
		assertNotFrozen();
		if (initializer != null) {
			this.initializer = initializer;
			initializer.setParent(this);
			initializer.setPropertyInParent(INITIALIZER);
		}
	}

	@Override
	public CPPASTStructuredBindingDeclaration copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTStructuredBindingDeclaration copy(CopyStyle style) {
		CPPASTStructuredBindingDeclaration copy = new CPPASTStructuredBindingDeclaration();
		return copy(copy, style);
	}

	protected <T extends CPPASTStructuredBindingDeclaration> T copy(T copy, CopyStyle style) {
		copy.setRefQualifier(refQualifier);
		if (initializer != null) {
			copy.setInitializer(initializer.copy(style));
		}

		for (IASTName name : names) {
			if (name == null) {
				break;
			}
			copy.addName(name.copy(style));
		}
		return super.copy(copy, style);
	}

	@Override
	public ICPPExecution getExecution() {
		IASTName[] names = getNames();
		ICPPExecution[] nameExecutions = Arrays.stream(names).map(name -> {
			IBinding binding = name.resolveBinding();
			if (binding instanceof CPPVariable) {
				CPPVariable variable = (CPPVariable) binding;
				ICPPEvaluation initializerEval = variable.getInitializerEvaluation();
				return new ExecDeclarator((ICPPBinding) binding, initializerEval);
			}
			return ExecIncomplete.INSTANCE;
		}).toArray(ICPPExecution[]::new);
		return new ExecSimpleDeclaration(nameExecutions);
	}

	@Override
	public int getRoleForName(IASTName name) {
		return r_definition;
	}

	private class ArrayElement implements INameEvaluationStrategy {
		private ICPPEvaluation initializerEvaluation;

		private ArrayElement(ICPPEvaluation initializerEvaluation) {
			this.initializerEvaluation = initializerEvaluation;
		}

		@Override
		public ICPPEvaluation getEvaluation(int nameIndex) {
			EvalFixed indexEvaluation = new EvalFixed(CPPBasicType.UNSIGNED_INT, ValueCategory.PRVALUE,
					IntegralValue.create(nameIndex));
			IASTNode init = getInitializer().get();
			return new EvalBinary(EvalBinary.op_arrayAccess, initializerEvaluation, indexEvaluation, init);
		}
	}

	private class TupleElement implements INameEvaluationStrategy {
		private ICPPEvaluation initializerEvaluation;
		private ICPPClassType initializerType;

		private TupleElement(ICPPEvaluation initializerEvaluation, ICPPClassType initializerType) {
			this.initializerEvaluation = initializerEvaluation;
			this.initializerType = initializerType;
		}

		@Override
		public ICPPEvaluation getEvaluation(int nameIndex) {
			IASTNode init = getInitializer().get();
			EvalFixed indexEvaluation = new EvalFixed(CPPBasicType.UNSIGNED_INT, ValueCategory.PRVALUE,
					IntegralValue.create(nameIndex));
			IBinding resolvedFunction = resolveGetFunction(initializerType, init, initializerEvaluation,
					indexEvaluation);
			if (resolvedFunction instanceof ICPPMethod) {
				ICPPEvaluation eExpressionEvaluation = new EvalMemberAccess(initializerType,
						initializerEvaluation.getValueCategory(), resolvedFunction, initializerEvaluation, false, init);
				return new EvalFunctionCall(new ICPPEvaluation[] { eExpressionEvaluation }, null, init);
			} else if (resolvedFunction instanceof ICPPFunction) {
				EvalBinding functionEvaluation = new EvalBinding(resolvedFunction,
						((ICPPFunction) resolvedFunction).getType(), init);
				return new EvalFunctionCall(new ICPPEvaluation[] { functionEvaluation, initializerEvaluation }, null,
						init);
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

	private class MemberElement implements INameEvaluationStrategy {
		private ICPPEvaluation initializerEvaluation;
		private ICPPClassType initializerType;

		private MemberElement(ICPPEvaluation initializerEvaluation, ICPPClassType initializerType) {
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
				IASTInitializer init = getInitializer().get();
				return new EvalMemberAccess(initializerType, initializerEvaluation.getValueCategory(), boundField,
						initializerEvaluation, false, init);
			}
			return EvalFixed.INCOMPLETE;
		}
	}

	private INameEvaluationStrategy getInitializerEvaluationStrategy() {
		ICPPEvaluation initializerEvaluation = CPPVariable.evaluationOfInitializer(initializer);
		if (initializerEvaluation != null) {
			IType type = initializerEvaluation.getType();
			IType unwrappedType = SemanticUtil.getNestedType(type, SemanticUtil.ALLCVQ | SemanticUtil.REF);
			if (unwrappedType instanceof IArrayType) {
				return new ArrayElement(initializerEvaluation);
			}
			if (unwrappedType instanceof ICPPClassType) {
				IASTName[] definedNames = getNames();
				long numberOfNames = definedNames.length;
				ICPPClassType classType = (ICPPClassType) unwrappedType;
				if (numberOfNames > 0) {
					IScope scope = CPPSemantics.getLookupScope(definedNames[0]);
					Optional<ICPPClassSpecialization> tupleSizeInstance = CPPVisitor
							.findTupleSizeWithValueMember(unwrappedType, scope, this);
					if (tupleSizeInstance.isPresent()) {
						if (CPPVisitor.hasConstexprStaticIntegralValueField(tupleSizeInstance.get(), numberOfNames)) {
							return new TupleElement(initializerEvaluation, classType);
						}
					} else {
						return new MemberElement(initializerEvaluation, classType);
					}
				}
			}
		}
		return INCOMPLETE_INITIALIZER;
	}

	public INameEvaluationStrategy getInitializerEvaluation() {
		if (nameEvaluationStrategy == null) {
			nameEvaluationStrategy = getInitializerEvaluationStrategy();
		}
		return nameEvaluationStrategy;
	}

}
