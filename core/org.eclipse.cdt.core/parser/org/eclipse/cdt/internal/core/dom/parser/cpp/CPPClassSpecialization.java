/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM) - Initial API and implementation
 *     Bryan Wilkinson (QNX)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.core.runtime.Assert;

/**
 * Specialization of a class.
 */
public class CPPClassSpecialization extends CPPSpecialization 
		implements ICPPClassSpecialization, ICPPInternalClassTypeMixinHost {

	public final static class RecursionResolvingBinding extends ProblemBinding {
		public RecursionResolvingBinding(IASTNode node, char[] arg) {
			super(node, IProblemBinding.SEMANTIC_RECURSION_IN_LOOKUP, arg);
			Assert.isTrue(CPPASTNameBase.sAllowRecursionBindings, getMessage());
		}
	}

	private ICPPClassSpecializationScope specScope;
	private ObjectMap specializationMap= ObjectMap.EMPTY_MAP;
	private final ThreadLocal<Set<IBinding>> fInProgress= new ThreadLocal<Set<IBinding>>();

	public CPPClassSpecialization(ICPPClassType specialized, IBinding owner,
			ICPPTemplateParameterMap argumentMap) {
		super(specialized, owner, argumentMap);
	}

	@Override
	public ICPPClassType getSpecializedBinding() {
		return (ICPPClassType) super.getSpecializedBinding();
	}
	
	@Override
	public IBinding specializeMember(IBinding original) {
		return specializeMember(original, null);
	}

	@Override
	public IBinding specializeMember(IBinding original, IASTNode point) {		
		Set<IBinding> set;
		synchronized (this) {
			IBinding result= (IBinding) specializationMap.get(original);
			if (result != null) 
				return result;
			
			set= fInProgress.get();
			if (set == null) {
				set= new HashSet<IBinding>();
				fInProgress.set(set);
			} 
			if (!set.add(original)) 
				return new RecursionResolvingBinding(null, null);
		}
		
		IBinding result= CPPTemplates.createSpecialization(this, original, point);
		set.remove(original);
		synchronized (this) {
			IBinding concurrent= (IBinding) specializationMap.get(original);
			if (concurrent != null) 
				return concurrent;
			if (specializationMap == ObjectMap.EMPTY_MAP)
				specializationMap = new ObjectMap(2);
			specializationMap.put(original, result);
			return result;
		}
	}
	
	@Override
	public void checkForDefinition() {
		// Ambiguity resolution ensures that declarations and definitions are resolved.
	}

	@Override
	public ICPPASTCompositeTypeSpecifier getCompositeTypeSpecifier() {
		IASTNode definition= getDefinition();
		if (definition != null) {
			IASTNode node= definition;
			while (node instanceof IASTName)
				node= node.getParent();
			if (node instanceof ICPPASTCompositeTypeSpecifier)
				return (ICPPASTCompositeTypeSpecifier) node;
		}
		return null;
	}
	
	@Override
	public ICPPBase[] getBases() {
		return getBases(null);
	}

	@Override
	public ICPPBase[] getBases(IASTNode point) {
		ICPPClassSpecializationScope scope= getSpecializationScope();
		if (scope == null)
			return ClassTypeHelper.getBases(this);

		return scope.getBases(point);
	}

	@Override
	public ICPPField[] getDeclaredFields() {
		return getDeclaredFields(null); // Instantiation of dependent expression may not work.
	}

	@Override
	public ICPPField[] getDeclaredFields(IASTNode point) {
		ICPPClassSpecializationScope scope= getSpecializationScope();
		if (scope == null)
			return ClassTypeHelper.getDeclaredFields(this);

		return scope.getDeclaredFields(point);
	}

	@Override
	public ICPPMethod[] getDeclaredMethods() {
		return getDeclaredMethods(null); // Instantiation of dependent expression may not work.
	}

	@Override
	public ICPPMethod[] getDeclaredMethods(IASTNode point) {
		ICPPClassSpecializationScope scope= getSpecializationScope();
		if (scope == null)
			return ClassTypeHelper.getDeclaredMethods(this);

		return scope.getDeclaredMethods(point);
	}

	@Override
	public ICPPConstructor[] getConstructors() {
		return getConstructors(null);
	}

	@Override
	public ICPPConstructor[] getConstructors(IASTNode point) {
		ICPPClassSpecializationScope scope= getSpecializationScope();
		if (scope == null)
			return ClassTypeHelper.getConstructors(this);

		return scope.getConstructors(point);
	}

	@Override
	public IBinding[] getFriends() {
		return getFriends(null); // Instantiation of dependent expression may not work.
	}

	@Override
	public IBinding[] getFriends(IASTNode point) {
		ICPPClassSpecializationScope scope= getSpecializationScope();
		if (scope == null)
			return ClassTypeHelper.getFriends(this);

		return scope.getFriends(point);
	}
	
	@Override
	public ICPPClassType[] getNestedClasses() {
		return getNestedClasses(null); // Instantiation of dependent expression may not work.
	}

	@Override
	public ICPPClassType[] getNestedClasses(IASTNode point) {
		ICPPClassSpecializationScope scope= getSpecializationScope();
		if (scope == null)
			return ClassTypeHelper.getNestedClasses(this);

		return scope.getNestedClasses(point);
	}

	@Override
	public IField[] getFields() {
		return ClassTypeHelper.getFields(this);
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
	public ICPPMethod[] getAllDeclaredMethods() {
		return ClassTypeHelper.getAllDeclaredMethods(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getKey()
	 */
	@Override
	public int getKey() {
		if (getDefinition() != null)
			return getCompositeTypeSpecifier().getKey();
		
		return getSpecializedBinding().getKey();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getCompositeScope()
	 */
	@Override
	public ICPPClassScope getCompositeScope() {
		final ICPPClassScope specScope= getSpecializationScope();
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
		
		ICPPASTCompositeTypeSpecifier spec= getCompositeTypeSpecifier(); 
		if (spec == null) {
			return getSpecializedBinding().isAnonymous();
		}

		IASTNode node= spec.getParent();
		if (node instanceof IASTSimpleDeclaration) {
			if (((IASTSimpleDeclaration) node).getDeclarators().length == 0) {
				return true;
			}
		}
		return false;
	}

	public static boolean isSameClassSpecialization(ICPPClassSpecialization t1, ICPPClassSpecialization t2) {
		// exclude class template specialization or class instance
		if (t2 instanceof ICPPTemplateInstance || t2 instanceof ICPPTemplateDefinition || 
				t2 instanceof IProblemBinding) {
			return false;
		}
		
		if (t1.getKey() != t2.getKey()) 
			return false;
		
		if (!CharArrayUtils.equals(t1.getNameCharArray(), t2.getNameCharArray()))
			return false;
		
		// the argument map is not significant for comparing specializations, the map is
		// determined by the owner of the specialization. This is different for instances,
		// which have a separate implementation for isSameType().
		final IBinding owner1= t1.getOwner();
		final IBinding owner2= t2.getOwner();
		
		// for a specialization that is not an instance the owner has to be a class-type
		if (!(owner1 instanceof ICPPClassType) || !(owner2 instanceof ICPPClassType))
			return false;

		return ((ICPPClassType) owner1).isSameType((ICPPClassType) owner2);
	}
}
