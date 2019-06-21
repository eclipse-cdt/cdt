/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM) - Initial API and implementation
 *     Bryan Wilkinson (QNX)
 *     Markus Schorn (Wind River Systems)
 *     Thomas Corbat (IFS)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.IRecursionResolvingBinding;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.ProblemFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExecIncomplete;
import org.eclipse.core.runtime.Assert;

/**
 * Specialization of a class.
 */
public class CPPClassSpecialization extends CPPSpecialization
		implements ICPPClassSpecialization, ICPPInternalClassTypeMixinHost {

	public static class RecursionResolvingBinding extends ProblemBinding
			implements ICPPMember, IRecursionResolvingBinding {
		public static RecursionResolvingBinding createFor(IBinding original) {
			IASTNode point = CPPSemantics.getCurrentLookupPoint();
			if (original instanceof ICPPConstructor)
				return new RecursionResolvingConstructor(point, original.getNameCharArray());
			else if (original instanceof ICPPMethod)
				return new RecursionResolvingMethod(point, original.getNameCharArray());
			if (original instanceof ICPPField)
				return new RecursionResolvingField(point, original.getNameCharArray());
			return new RecursionResolvingBinding(point, original.getNameCharArray());
		}

		private RecursionResolvingBinding(IASTNode node, char[] arg) {
			super(node, IProblemBinding.SEMANTIC_RECURSION_IN_LOOKUP, arg);
			Assert.isTrue(CPPASTNameBase.sAllowRecursionBindings, getMessage());
		}

		@Override
		public int getVisibility() {
			return ICPPASTVisibilityLabel.v_public;
		}

		@Override
		public ICPPClassType getClassOwner() {
			return null;
		}
	}

	public final static class RecursionResolvingField extends RecursionResolvingBinding implements ICPPField {
		public RecursionResolvingField(IASTNode node, char[] arg) {
			super(node, arg);
		}

		@Override
		public ICompositeType getCompositeTypeOwner() {
			return null;
		}

		@Override
		public int getFieldPosition() {
			return -1;
		}
	}

	public static class RecursionResolvingMethod extends RecursionResolvingBinding implements ICPPMethod {
		public RecursionResolvingMethod(IASTNode node, char[] arg) {
			super(node, arg);
		}

		@Override
		public ICPPParameter[] getParameters() {
			return ICPPParameter.EMPTY_CPPPARAMETER_ARRAY;
		}

		@Override
		public int getRequiredArgumentCount() {
			return 0;
		}

		@Override
		public IScope getFunctionScope() {
			return null;
		}

		@Override
		public boolean isNoReturn() {
			return false;
		}

		@Override
		public boolean isDestructor() {
			return false;
		}

		@Override
		public ICPPFunctionType getDeclaredType() {
			return new ProblemFunctionType(getID());
		}

		@Override
		public ICPPFunctionType getType() {
			return new ProblemFunctionType(getID());
		}

		@Override
		public boolean isOverride() {
			return false;
		}

		@Override
		public boolean isFinal() {
			return false;
		}

		@Override
		public boolean isConstexpr() {
			return false;
		}
	}

	public final static class RecursionResolvingConstructor extends RecursionResolvingMethod
			implements ICPPConstructor {
		public RecursionResolvingConstructor(IASTNode node, char[] arg) {
			super(node, arg);
		}

		@Override
		public ICPPExecution getConstructorChainExecution(IASTNode point) {
			return getConstructorChainExecution();
		}

		@Override
		public ICPPExecution getConstructorChainExecution() {
			return ExecIncomplete.INSTANCE;
		}

	}

	private ICPPClassSpecializationScope specScope;
	private ObjectMap specializationMap = ObjectMap.EMPTY_MAP;
	private ICPPBase[] bases;
	private final ThreadLocal<Set<IBinding>> fInProgress = new ThreadLocal<Set<IBinding>>() {
		@Override
		protected Set<IBinding> initialValue() {
			return new HashSet<>();
		}
	};

	public CPPClassSpecialization(ICPPClassType specialized, IBinding owner, ICPPTemplateParameterMap argumentMap) {
		super(specialized, owner, argumentMap);
	}

	@Override
	public ICPPClassType getSpecializedBinding() {
		return (ICPPClassType) super.getSpecializedBinding();
	}

	@Override
	public IBinding specializeMember(IBinding original) {
		synchronized (this) {
			IBinding result = (IBinding) specializationMap.get(original);
			if (result != null)
				return result;
		}

		IBinding result;
		Set<IBinding> recursionProtectionSet = fInProgress.get();
		if (!recursionProtectionSet.add(original))
			return RecursionResolvingBinding.createFor(original);

		try {
			result = CPPTemplates.createSpecialization(this, original);
		} finally {
			recursionProtectionSet.remove(original);
		}

		synchronized (this) {
			IBinding concurrent = (IBinding) specializationMap.get(original);
			if (concurrent != null)
				return concurrent;
			if (specializationMap == ObjectMap.EMPTY_MAP)
				specializationMap = new ObjectMap(2);
			specializationMap.put(original, result);
			return result;
		}
	}

	@Override
	public IBinding specializeMember(IBinding original, IASTNode point) {
		return specializeMember(original);
	}

	@Override
	public void checkForDefinition() {
		// Ambiguity resolution ensures that declarations and definitions are resolved.
	}

	@Override
	public ICPPASTCompositeTypeSpecifier getCompositeTypeSpecifier() {
		IASTNode definition = getDefinition();
		if (definition != null) {
			IASTNode node = definition;
			while (node instanceof IASTName)
				node = node.getParent();
			if (node instanceof ICPPASTCompositeTypeSpecifier)
				return (ICPPASTCompositeTypeSpecifier) node;
		}
		return null;
	}

	@Override
	public ICPPBase[] getBases() {
		ICPPClassSpecializationScope scope = getSpecializationScope();
		if (scope == null) {
			if (bases == null) {
				bases = ClassTypeHelper.getBases(this);
			}
			return bases;
		}

		return scope.getBases();
	}

	@Override
	@Deprecated
	public ICPPBase[] getBases(IASTNode point) {
		return getBases();
	}

	@Override
	public ICPPField[] getDeclaredFields() {
		ICPPClassSpecializationScope scope = getSpecializationScope();
		if (scope == null)
			return ClassTypeHelper.getDeclaredFields(this);

		return scope.getDeclaredFields();
	}

	@Override
	@Deprecated
	public ICPPField[] getDeclaredFields(IASTNode point) {
		return getDeclaredFields();
	}

	@Override
	public ICPPMethod[] getDeclaredMethods() {
		ICPPClassSpecializationScope scope = getSpecializationScope();
		if (scope == null)
			return ClassTypeHelper.getDeclaredMethods(this);

		return scope.getDeclaredMethods();
	}

	@Override
	@Deprecated
	public ICPPMethod[] getDeclaredMethods(IASTNode point) {
		return getDeclaredMethods();
	}

	@Override
	public ICPPConstructor[] getConstructors() {
		ICPPClassSpecializationScope scope = getSpecializationScope();
		if (scope == null)
			return ClassTypeHelper.getConstructors(this);

		return scope.getConstructors();
	}

	@Override
	@Deprecated
	public ICPPConstructor[] getConstructors(IASTNode point) {
		return getConstructors();
	}

	@Override
	public IBinding[] getFriends() {
		ICPPClassSpecializationScope scope = getSpecializationScope();
		if (scope == null)
			return ClassTypeHelper.getFriends(this);

		return scope.getFriends();
	}

	@Override
	@Deprecated
	public IBinding[] getFriends(IASTNode point) {
		return getFriends();
	}

	@Override
	public ICPPClassType[] getNestedClasses() {
		ICPPClassSpecializationScope scope = getSpecializationScope();
		if (scope == null)
			return ClassTypeHelper.getNestedClasses(this);

		return scope.getNestedClasses();
	}

	@Override
	@Deprecated
	public ICPPClassType[] getNestedClasses(IASTNode point) {
		return getNestedClasses();
	}

	@Override
	public ICPPUsingDeclaration[] getUsingDeclarations() {
		ICPPClassSpecializationScope scope = getSpecializationScope();
		if (scope == null)
			return ClassTypeHelper.getUsingDeclarations(this);

		return scope.getUsingDeclarations();
	}

	@Override
	@Deprecated
	public ICPPUsingDeclaration[] getUsingDeclarations(IASTNode point) {
		return getUsingDeclarations();
	}

	@Override
	public IField[] getFields() {
		return ClassTypeHelper.getFields(this);
	}

	@Override
	@Deprecated
	public IField[] getFields(IASTNode point) {
		return getFields();
	}

	@Override
	public IField findField(String name) {
		return ClassTypeHelper.findField(this, name);
	}

	@Override
	public ICPPMethod[] getMethods() {
		return ClassTypeHelper.getMethods(this);
	}

	@Override
	@Deprecated
	public ICPPMethod[] getMethods(IASTNode point) {
		return getMethods();
	}

	@Override
	public ICPPMethod[] getAllDeclaredMethods() {
		return ClassTypeHelper.getAllDeclaredMethods(this);
	}

	@Override
	@Deprecated
	public ICPPMethod[] getAllDeclaredMethods(IASTNode point) {
		return getAllDeclaredMethods();
	}

	@Override
	public int getKey() {
		if (getDefinition() != null)
			return getCompositeTypeSpecifier().getKey();

		return getSpecializedBinding().getKey();
	}

	@Override
	public ICPPClassScope getCompositeScope() {
		final ICPPClassScope specScope = getSpecializationScope();
		if (specScope != null)
			return specScope;

		final ICPPASTCompositeTypeSpecifier typeSpecifier = getCompositeTypeSpecifier();
		if (typeSpecifier != null)
			return typeSpecifier.getScope();

		return null;
	}

	protected ICPPClassSpecializationScope getSpecializationScope() {
		checkForDefinition();
		if (getDefinition() != null)
			return null;

		// Implicit specialization: must specialize bindings in scope.
		if (specScope == null) {
			specScope = new CPPClassSpecializationScope(this);
		}
		return specScope;
	}

	@Override
	public boolean isSameType(IType type) {
		if (type == this)
			return true;
		if (type instanceof ITypedef)
			return type.isSameType(this);

		if (type instanceof ICPPClassSpecialization) {
			return isSameClassSpecialization(this, (ICPPClassSpecialization) type);
		}
		return false;
	}

	@Override
	public Object clone() {
		return this;
	}

	@Override
	public boolean isAnonymous() {
		if (getNameCharArray().length > 0)
			return false;

		ICPPASTCompositeTypeSpecifier spec = getCompositeTypeSpecifier();
		if (spec == null) {
			return getSpecializedBinding().isAnonymous();
		}

		IASTNode node = spec.getParent();
		if (node instanceof IASTSimpleDeclaration) {
			if (((IASTSimpleDeclaration) node).getDeclarators().length == 0) {
				return true;
			}
		}
		return false;
	}

	public static boolean isSameClassSpecialization(ICPPClassSpecialization t1, ICPPClassSpecialization t2) {
		// Exclude class template specialization or class instance.
		if (t2 instanceof ICPPTemplateInstance || t2 instanceof ICPPTemplateDefinition
				|| t2 instanceof IProblemBinding) {
			return false;
		}

		if (t1.getKey() != t2.getKey())
			return false;

		if (!CharArrayUtils.equals(t1.getNameCharArray(), t2.getNameCharArray()))
			return false;

		// The argument map is not significant for comparing specializations, the map is
		// determined by the owner of the specialization. This is different for instances,
		// which have a separate implementation for isSameType().
		final IBinding owner1 = t1.getOwner();
		final IBinding owner2 = t2.getOwner();

		// For a specialization that is not an instance the owner has to be a class-type.
		if (!(owner1 instanceof ICPPClassType) || !(owner2 instanceof ICPPClassType))
			return false;

		return ((ICPPClassType) owner1).isSameType((ICPPClassType) owner2);
	}

	@Override
	public boolean isFinal() {
		ICPPASTCompositeTypeSpecifier typeSpecifier = getCompositeTypeSpecifier();
		if (typeSpecifier != null) {
			return typeSpecifier.isFinal();
		}
		return false;
	}

	@Override
	public int getVisibility(IBinding member) {
		return ClassTypeHelper.getVisibility(this, member);
	}
}
