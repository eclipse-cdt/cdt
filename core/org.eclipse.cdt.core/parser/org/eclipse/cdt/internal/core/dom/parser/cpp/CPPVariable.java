/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Ed Swartz (Nokia)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor.IS_STATIC_VARIABLE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.REF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
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
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTStructuredBindingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.DependentValue;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalBinary;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalConstructor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFunctionCall;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalMemberAccess;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalUtil;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.LookupData;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.core.runtime.PlatformObject;

public class CPPVariable extends PlatformObject implements ICPPInternalDeclaredVariable {
	private IASTName fDefinition;
	private IASTName fDeclarations[]; // Allowed to have trailing nulls. Users must check or trim!
	private IType fType;
	private IValue fInitialValue = IntegralValue.NOT_INITIALIZED;
	private boolean fAllResolved;
	private static final char[] GET_NAME = "get".toCharArray(); //$NON-NLS-1$

	/**
	 * The set of CPPVariable objects for which initial value computation is in progress on each thread.
	 * This is used to guard against recursion during initial value computation.
	 */
	private static final ThreadLocal<Set<CPPVariable>> fInitialValueInProgress = new ThreadLocal<Set<CPPVariable>>() {
		@Override
		protected Set<CPPVariable> initialValue() {
			return new HashSet<>();
		}
	};

	public CPPVariable(IASTName name) {
		boolean isDef = name != null && name.isDefinition();
		if (name instanceof ICPPASTQualifiedName) {
			name = name.getLastName();
		}

		if (isDef) {
			fDefinition = name;
		} else {
			fDeclarations = new IASTName[] { name };
		}

		// Built-in variables supply a null.
		if (name != null) {
			name.setBinding(this);
		} else {
			assert this instanceof CPPBuiltinVariable;
		}
	}

	@Override
	public void addDeclaration(IASTNode node) {
		if (!(node instanceof IASTName))
			return;
		IASTName name = (IASTName) node;
		if (fDefinition == null && name.isDefinition()) {
			fDefinition = name;
		} else if (fDeclarations == null) {
			fDeclarations = new IASTName[] { name };
		} else {
			// Keep the lowest offset declaration at the first position.
			if (fDeclarations.length > 0 && ((ASTNode) node).getOffset() < ((ASTNode) fDeclarations[0]).getOffset()) {
				fDeclarations = ArrayUtil.prepend(IASTName.class, fDeclarations, name);
			} else {
				fDeclarations = ArrayUtil.append(IASTName.class, fDeclarations, name);
			}
		}
		// An array type may be incomplete and needs to be recalculated.
		if (fType instanceof IArrayType) {
			fType = null;
		}
		// Initial value has to be recalculated.
		fInitialValue = IntegralValue.NOT_INITIALIZED;
	}

	@Override
	public IASTName[] getDeclarations() {
		return fDeclarations == null ? null : ArrayUtil.trim(fDeclarations);
	}

	@Override
	public IASTNode getDefinition() {
		return fDefinition;
	}

	@Override
	public IType getType() {
		if (fType != null) {
			return fType;
		}

		boolean allResolved = fAllResolved;
		fAllResolved = true;
		fType = VariableHelpers.createType(this, fDefinition, getDeclarations(), allResolved);

		return fType;
	}

	@Override
	public String getName() {
		return new String(getNameCharArray());
	}

	@Override
	public char[] getNameCharArray() {
		if (fDeclarations != null) {
			return fDeclarations[0].getSimpleID();
		}
		return fDefinition.getSimpleID();
	}

	@Override
	public IScope getScope() {
		return CPPVisitor.getContainingScope(fDefinition != null ? fDefinition : fDeclarations[0]);
	}

	@Override
	public String[] getQualifiedName() {
		return CPPVisitor.getQualifiedName(this);
	}

	@Override
	public char[][] getQualifiedNameCharArray() {
		return CPPVisitor.getQualifiedNameCharArray(this);
	}

	@Override
	public boolean isGloballyQualified() throws DOMException {
		IScope scope = getScope();
		while (scope != null) {
			if (scope instanceof ICPPBlockScope)
				return false;
			scope = scope.getParent();
		}
		return true;
	}

	@Override
	public void addDefinition(IASTNode node) {
		addDeclaration(node);
	}

	public boolean hasStorageClass(int storage) {
		IASTName name = (IASTName) getDefinition();
		IASTNode[] ns = getDeclarations();

		return VariableHelpers.hasStorageClass(name, ns, storage);
	}

	@Override
	public boolean isMutable() {
		// 7.1.1-8 the mutable specifier can only be applied to names of class data members.
		return false;
	}

	@Override
	public boolean isConstexpr() {
		return VariableHelpers.isConstexpr(fDefinition != null ? fDefinition : fDeclarations[0]);
	}

	@Override
	public boolean isStatic() {
		return hasStorageClass(IASTDeclSpecifier.sc_static);
	}

	@Override
	public boolean isExtern() {
		return hasStorageClass(IASTDeclSpecifier.sc_extern);
	}

	@Override
	public boolean isExternC() {
		return CPPVisitor.isExternC(getDefinition(), getDeclarations());
	}

	@Override
	public boolean isAuto() {
		return hasStorageClass(IASTDeclSpecifier.sc_auto);
	}

	@Override
	public boolean isRegister() {
		return hasStorageClass(IASTDeclSpecifier.sc_register);
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}

	@Override
	public IBinding getOwner() {
		IASTName node = fDefinition != null ? fDefinition : fDeclarations[0];
		return CPPVisitor.findNameOwner(node, !hasStorageClass(IASTDeclSpecifier.sc_extern));
	}

	@Override
	public IValue getInitialValue() {
		if (fInitialValue == IntegralValue.NOT_INITIALIZED) {
			fInitialValue = computeInitialValue();
		}
		return fInitialValue;
	}

	private IValue computeInitialValue() {
		Set<CPPVariable> recursionProtectionSet = fInitialValueInProgress.get();
		if (!recursionProtectionSet.add(this)) {
			return IntegralValue.UNKNOWN;
		}
		try {
			IValue initialValue = null;
			final IType nestedType = SemanticUtil.getNestedType(getType(), TDEF | REF | CVTYPE);
			if (nestedType instanceof ICPPClassType || (initialValue = VariableHelpers.getInitialValue(fDefinition,
					fDeclarations, getType())) == IntegralValue.UNKNOWN || getStructuredBindingDeclaration() != null) {
				ICPPEvaluation initEval = getInitializerEvaluation();
				if (initEval == null) {
					return null;
				}
				if (!initEval.isValueDependent()) {
					IASTNode point = fDefinition != null ? fDefinition : fDeclarations[0];
					CPPSemantics.pushLookupPoint(point);
					try {
						return initEval.getValue();
					} finally {
						CPPSemantics.popLookupPoint();
					}
				}
				return DependentValue.create(initEval);
			}
			return initialValue;
		} finally {
			recursionProtectionSet.remove(this);
		}
	}

	private IASTDeclarator findDeclarator() {
		IASTDeclarator declarator = null;
		if (fDefinition != null) {
			declarator = VariableHelpers.findDeclarator(fDefinition);
			if (declarator != null) {
				return declarator;
			}
		}
		if (fDeclarations != null) {
			for (IASTName decl : fDeclarations) {
				if (decl == null)
					break;
				declarator = VariableHelpers.findDeclarator(decl);
				if (declarator != null) {
					return declarator;
				}
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 *
	 * Resolves the member or non-member get() function template for a type and given arguments.
	 *
	 * Returns {@code null} if the function cannot be resolved
	 */
	private static IBinding resolveGetFunction(ICompositeType classType, IASTNode point, ICPPEvaluation argument,
			ICPPEvaluation index) {
		//find member and non-member get() bindings in class scope and all parent scopes
		IBinding[] allGetBindings = CPPSemantics.findBindings(classType.getCompositeScope(), GET_NAME, false, point);
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

	/**
	 * Returns an evaluation representing the variable's initialization.
	 *
	 * If the variable has no initializer, {@code null} is returned.
	 */
	public ICPPEvaluation getInitializerEvaluation() {
		ICPPASTDeclarator declarator = (ICPPASTDeclarator) findDeclarator();
		if (declarator != null) {
			IASTInitializer initializer = declarator.getInitializer();
			ICPPConstructor constructor = getImplicitlyCalledCtor(declarator);
			if (constructor != null) {
				ICPPEvaluation[] arguments = EvalConstructor.extractArguments(initializer, constructor);
				return new EvalConstructor(getType(), constructor, arguments, declarator);
			}
			return evaluationOfInitializer(initializer);
		}

		ICPPASTStructuredBindingDeclaration structuredBinding = getStructuredBindingDeclaration();
		if (structuredBinding != null && structuredBinding.getInitializer().isPresent()) {
			return evaluationOfStructuredBindingInitializer(structuredBinding);
		}
		return EvalFixed.INCOMPLETE;
	}

	private ICPPEvaluation evaluationOfStructuredBindingInitializer(
			ICPPASTStructuredBindingDeclaration structuredBinding) {
		IASTInitializer init = structuredBinding.getInitializer().get();
		ICPPEvaluation initializerEvaluation = evaluationOfInitializer(init);
		if (initializerEvaluation != null) {
			IType type = initializerEvaluation.getType();
			IType unwrappedType = SemanticUtil.getNestedType(type, SemanticUtil.ALLCVQ | SemanticUtil.REF);
			int index = ArrayUtil.indexOf(structuredBinding.getNames(), fDefinition);
			EvalFixed indexEvaluation = new EvalFixed(CPPBasicType.UNSIGNED_INT, ValueCategory.PRVALUE,
					IntegralValue.create(index));
			if (unwrappedType instanceof IArrayType) {
				return new EvalBinary(EvalBinary.op_arrayAccess, initializerEvaluation, indexEvaluation, init);
			}
			if (unwrappedType instanceof ICPPClassType) {
				long numberOfNames = structuredBinding.getNames().length;
				ICPPClassType classType = (ICPPClassType) unwrappedType;
				IScope scope = CPPSemantics.getLookupScope(fDefinition);
				Optional<ICPPClassSpecialization> tupleSizeInstance = CPPVisitor
						.findTupleSizeWithValueMember(unwrappedType, scope, fDefinition);
				if (tupleSizeInstance.isPresent()) {
					if (CPPVisitor.hasConstexprStaticIntegralValueField(tupleSizeInstance.get(), numberOfNames)) {
						IBinding resolvedFunction = resolveGetFunction(classType, init, initializerEvaluation,
								indexEvaluation);
						if (resolvedFunction instanceof ICPPMethod) {
							ICPPEvaluation eExpressionEvaluation = new EvalMemberAccess(type,
									initializerEvaluation.getValueCategory(), resolvedFunction, initializerEvaluation,
									false, init);
							return new EvalFunctionCall(new ICPPEvaluation[] { eExpressionEvaluation }, null, init);
						} else if (resolvedFunction instanceof ICPPFunction) {
							EvalBinding functionEvaluation = new EvalBinding(resolvedFunction,
									((ICPPFunction) resolvedFunction).getType(), init);
							return new EvalFunctionCall(
									new ICPPEvaluation[] { functionEvaluation, initializerEvaluation }, null, init);
						}
					}
				} else {
					IField[] nonStaticFields = Arrays.stream(ClassTypeHelper.getFields(classType))
							.filter(IS_STATIC_VARIABLE.negate()).toArray(IField[]::new);
					//TODO (tcorbat): Add further check that all members origin from the same class/base class
					//TODO (tcorbat): classType must not have an anonymous union member
					if (index >= 0 && index < nonStaticFields.length && nonStaticFields.length == numberOfNames) {
						IField boundField = nonStaticFields[index];
						return new EvalMemberAccess(type, initializerEvaluation.getValueCategory(), boundField,
								initializerEvaluation, false, init);
					}
				}
			}
		}
		return EvalFixed.INCOMPLETE;
	}

	private static ICPPEvaluation evaluationOfInitializer(IASTInitializer initializer) {
		if (initializer instanceof IASTEqualsInitializer) {
			IASTEqualsInitializer equalsInitializer = (IASTEqualsInitializer) initializer;
			ICPPASTInitializerClause clause = (ICPPASTInitializerClause) equalsInitializer.getInitializerClause();
			return clause.getEvaluation();
		} else if (initializer instanceof ICPPASTInitializerList) {
			return ((ICPPASTInitializerClause) initializer).getEvaluation();
		} else if (initializer instanceof ICPPASTConstructorInitializer) {
			ICPPASTConstructorInitializer ctorInitializer = (ICPPASTConstructorInitializer) initializer;
			ICPPASTInitializerClause evalOwner = (ICPPASTInitializerClause) ctorInitializer.getArguments()[0];
			return evalOwner.getEvaluation();
		}
		return null;
	}

	private ICPPASTStructuredBindingDeclaration getStructuredBindingDeclaration() {
		IASTNode definition = getDefinition();
		if (definition != null) {
			IASTNode parent = definition.getParent();
			if (parent instanceof ICPPASTStructuredBindingDeclaration) {
				return (ICPPASTStructuredBindingDeclaration) parent;
			}
		}
		return null;
	}

	private static ICPPConstructor getImplicitlyCalledCtor(ICPPASTDeclarator declarator) {
		IBinding ctor = CPPSemantics.findImplicitlyCalledConstructor(declarator);
		if (ctor instanceof ICPPConstructor) {
			if (!EvalUtil.isCompilerGeneratedCtor(ctor) || EvalUtil.isDefaultConstructor((ICPPConstructor) ctor)) {
				return (ICPPConstructor) ctor;
			}
		}
		return null;
	}

	@Override
	public void allDeclarationsDefinitionsAdded() {
		fAllResolved = true;
	}
}
