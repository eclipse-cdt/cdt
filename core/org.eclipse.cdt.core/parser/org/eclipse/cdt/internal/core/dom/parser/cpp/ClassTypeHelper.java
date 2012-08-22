/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Bryan Wilkinson (QNX)
 *     Sergey Prigogin (Google)
 *     Andrew Ferguson (Symbian)
 *     Anton Gorenkov
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.core.runtime.CoreException;

/**
 * Holds common implementation of methods for ICPPClassType implementations that have
 * a corresponding textual definition in the source code.
 *
 * @see CPPClassType
 * @see CPPClassTemplate
 */
public class ClassTypeHelper {
	private static final String DESTRUCTOR_OVERRIDE_KEY = "~"; //$NON-NLS-1$

	public static IBinding[] getFriends(ICPPInternalClassTypeMixinHost host) {
		if (host.getDefinition() == null) {
			host.checkForDefinition();
			if (host.getDefinition() == null) {
				ICPPClassType backup= getBackupDefinition(host);
				if (backup != null)
					return backup.getFriends();
				IASTNode[] declarations= host.getDeclarations();
				IASTNode node = (declarations != null && declarations.length > 0) ? declarations[0] : null;
				return new IBinding[] { new ProblemBinding(node, IProblemBinding.SEMANTIC_DEFINITION_NOT_FOUND, host.getNameCharArray()) };
			}
		}
		ObjectSet<IBinding> resultSet = new ObjectSet<IBinding>(2);
		IASTDeclaration[] members = host.getCompositeTypeSpecifier().getMembers();
		for (IASTDeclaration decl : members) {
			while (decl instanceof ICPPASTTemplateDeclaration) {
				decl = ((ICPPASTTemplateDeclaration) decl).getDeclaration();
			}

			if (decl instanceof IASTSimpleDeclaration) {
				ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) ((IASTSimpleDeclaration) decl).getDeclSpecifier();
				if (declSpec.isFriend()) {
					IASTDeclarator[] dtors = ((IASTSimpleDeclaration) decl).getDeclarators();
					if (declSpec instanceof ICPPASTElaboratedTypeSpecifier && dtors.length == 0) {
						resultSet.put(((ICPPASTElaboratedTypeSpecifier) declSpec).getName().resolveBinding());
					} else {
						for (IASTDeclarator dtor : dtors) {
							if (dtor == null) break;
							dtor= ASTQueries.findInnermostDeclarator(dtor);
							resultSet.put(dtor.getName().resolveBinding());
						}
					}
				}
			} else if (decl instanceof IASTFunctionDefinition) {
				ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) ((IASTFunctionDefinition) decl).getDeclSpecifier();
				if (declSpec.isFriend()) {
					IASTDeclarator dtor = ((IASTFunctionDefinition) decl).getDeclarator();
					dtor= ASTQueries.findInnermostDeclarator(dtor);
					resultSet.put(dtor.getName().resolveBinding());
				}
			}
		}

		return resultSet.keyArray(IBinding.class);
	}

	/**
	 * Checks if a binding is a friend of a class. Only classes and functions can be friends of a class.
	 * A class is considered a friend of itself.
	 * @param binding a binding.
	 * @param classType a class.
	 * @return <code>true</code> if <code>binding</code> is a friend of <code>classType</code>.
	 */
	public static boolean isFriend(IBinding binding, ICPPClassType classType) {
		IType type;
		if (binding instanceof ICPPClassType) {
			type = (IType) binding;
			if (type.isSameType(classType)) {
				return true;
			}
			for (IBinding friend : getFriends(classType, null)) {
				if (friend instanceof ICPPClassType && type.isSameType((IType) friend)) {
					return true;
				}
			}
		} else if (binding instanceof ICPPFunction) {
			type = ((ICPPFunction) binding).getType();
			char[] name = binding.getNameCharArray();
			for (IBinding friend : getFriends(classType, null)) {
				if (friend instanceof ICPPFunction &&
						CharArrayUtils.equals(name, friend.getNameCharArray()) &&
						SemanticUtil.isSameOwner(binding.getOwner(), friend.getOwner()) &&
						type.isSameType(((ICPPFunction) friend).getType())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * A host maybe backed up with a definition from the index.
	 */
	private static ICPPClassType getBackupDefinition(ICPPInternalClassTypeMixinHost host) {
		ICPPClassScope scope = host.getCompositeScope();
		if (scope != null) {
			ICPPClassType b = scope.getClassType();
			if (!(b instanceof ICPPInternalClassTypeMixinHost))
				return b;
		}
		return null;
	}

	public static ICPPBase[] getBases(ICPPInternalClassTypeMixinHost host) {
		if (host.getDefinition() == null) {
			host.checkForDefinition();
			if (host.getDefinition() == null) {
				ICPPClassType backup= getBackupDefinition(host);
				if (backup != null)
					return backup.getBases();

				return ICPPBase.EMPTY_BASE_ARRAY;
			}
		}
		ICPPASTBaseSpecifier[] bases = host.getCompositeTypeSpecifier().getBaseSpecifiers();
		if (bases.length == 0)
			return ICPPBase.EMPTY_BASE_ARRAY;

		ICPPBase[] bindings = new ICPPBase[bases.length];
		for (int i = 0; i < bases.length; i++) {
			bindings[i] = new CPPBaseClause(bases[i]);
		}

		return bindings;
	}

	public static ICPPField[] getDeclaredFields(ICPPInternalClassTypeMixinHost host) {
		if (host.getDefinition() == null) {
			host.checkForDefinition();
			if (host.getDefinition() == null) {
				ICPPClassType backup= getBackupDefinition(host);
				if (backup != null)
					return backup.getDeclaredFields();

				return ICPPField.EMPTY_CPPFIELD_ARRAY;
			}
		}
		IBinding binding = null;
		ICPPField[] result = null;

		IASTDeclaration[] decls = host.getCompositeTypeSpecifier().getMembers();
		for (IASTDeclaration decl : decls) {
			if (decl instanceof IASTSimpleDeclaration) {
				IASTDeclarator[] dtors = ((IASTSimpleDeclaration) decl).getDeclarators();
				for (IASTDeclarator dtor : dtors) {
					binding = ASTQueries.findInnermostDeclarator(dtor).getName().resolveBinding();
					if (binding instanceof ICPPField)
						result = ArrayUtil.append(ICPPField.class, result, (ICPPField) binding);
				}
			} else if (decl instanceof ICPPASTUsingDeclaration) {
				IASTName n = ((ICPPASTUsingDeclaration) decl).getName();
				binding = n.resolveBinding();
				if (binding instanceof ICPPUsingDeclaration) {
					IBinding[] bs = ((ICPPUsingDeclaration) binding).getDelegates();
					for (IBinding element : bs) {
						if (element instanceof ICPPField)
							result = ArrayUtil.append(ICPPField.class, result, (ICPPField) element);
					}
				} else if (binding instanceof ICPPField) {
					result = ArrayUtil.append(ICPPField.class, result, (ICPPField) binding);
				}
			}
		}
		return ArrayUtil.trim(ICPPField.class, result);
	}

	public static ICPPBase[] getBases(ICPPClassType classType, IASTNode point) {
		if (classType instanceof ICPPClassSpecialization)
			return ((ICPPClassSpecialization) classType).getBases(point);
		return classType.getBases();
	}

	public static ICPPConstructor[] getConstructors(ICPPClassType classType, IASTNode point) {
		if (classType instanceof ICPPClassSpecialization)
			return ((ICPPClassSpecialization) classType).getConstructors(point);
		return classType.getConstructors();
	}

	public static ICPPField[] getDeclaredFields(ICPPClassType classType, IASTNode point) {
		if (classType instanceof ICPPClassSpecialization)
			return ((ICPPClassSpecialization) classType).getDeclaredFields(point);
		return classType.getDeclaredFields();
	}

	public static ICPPMethod[] getDeclaredMethods(ICPPClassType classType, IASTNode point) {
		if (classType instanceof ICPPClassSpecialization)
			return ((ICPPClassSpecialization) classType).getDeclaredMethods(point);
		return classType.getDeclaredMethods();
	}

	public static IBinding[] getFriends(ICPPClassType classType, IASTNode point) {
		if (classType instanceof ICPPClassSpecialization)
			return ((ICPPClassSpecialization) classType).getFriends(point);
		return classType.getFriends();
	}

	public static ICPPClassType[] getNestedClasses(ICPPClassType classType, IASTNode point) {
		if (classType instanceof ICPPClassSpecialization)
			return ((ICPPClassSpecialization) classType).getNestedClasses(point);
		return classType.getNestedClasses();
	}

	/**
	 * Returns all direct and indirect base classes.
	 * @param classType a class
	 * @return An array of visible base classes in arbitrary order.
	 */
	public static ICPPClassType[] getAllBases(ICPPClassType classType, IASTNode point) {
		HashSet<ICPPClassType> result= new HashSet<ICPPClassType>();
		result.add(classType);
		getAllBases(classType, result, point);
		result.remove(classType);
		return result.toArray(new ICPPClassType[result.size()]);
	}

	private static void getAllBases(ICPPClassType classType, HashSet<ICPPClassType> result, IASTNode point) {
		ICPPBase[] bases= ClassTypeHelper.getBases(classType, point);
		for (ICPPBase base : bases) {
			IBinding b= base.getBaseClass();
			if (b instanceof ICPPClassType) {
				final ICPPClassType baseClass = (ICPPClassType) b;
				if (result.add(baseClass)) {
					getAllBases(baseClass, result, point);
				}
			}
		}
	}

	/**
	 * Checks inheritance relationship between two classes.
	 * @return <code>true</code> if {@code subclass} is a subclass of {@code superclass}.
	 */
	public static boolean isSubclass(ICPPClassType subclass, ICPPClassType superclass) {
		ICPPBase[] bases= subclass.getBases();
		for (ICPPBase base : bases) {
			IBinding b= base.getBaseClass();
			if (b instanceof ICPPClassType) {
				ICPPClassType baseClass = (ICPPClassType) b;
				if (baseClass.isSameType(superclass)) {
					return true;
				}
				if (isSubclass(baseClass, superclass)) {
					return true;
				}
			}
		}
		return false;
	}

	public static ICPPMethod[] getAllDeclaredMethods(ICPPClassType ct, IASTNode point) {
		ICPPMethod[] methods= getDeclaredMethods(ct, point);
		ICPPClassType[] bases= getAllBases(ct, point);
		for (ICPPClassType base : bases) {
			methods = ArrayUtil.addAll(ICPPMethod.class, methods, getDeclaredMethods(base, point));
		}
		return ArrayUtil.trim(ICPPMethod.class, methods);
	}

	public static ICPPMethod[] getMethods(ICPPClassType ct, IASTNode point) {
		ObjectSet<ICPPMethod> set = getOwnMethods(ct, point);

		ICPPClassType[] bases= getAllBases(ct, point);
		for (ICPPClassType base : bases) {
			set.addAll(getDeclaredMethods(base, point));
			set.addAll(getImplicitMethods(base, point));
		}
		return set.keyArray(ICPPMethod.class);
	}

	/**
	 * Returns methods either declared by the given class or generated by the compiler. Does not
	 * include methods declared in base classes.
	 */
	private static ObjectSet<ICPPMethod> getOwnMethods(ICPPClassType classType, IASTNode point) {
		ObjectSet<ICPPMethod> set= new ObjectSet<ICPPMethod>(4);
		set.addAll(ClassTypeHelper.getDeclaredMethods(classType, point));
		set.addAll(getImplicitMethods(classType, point));
		return set;
	}

	public static ICPPMethod[] getImplicitMethods(ICPPClassType classType, IASTNode point) {
		IScope scope = classType.getCompositeScope();
		if (scope instanceof ICPPClassSpecializationScope) {
			return ((ICPPClassSpecializationScope) scope).getImplicitMethods(point);
		} else if (scope instanceof ICPPClassScope) {
			return ((ICPPClassScope) scope).getImplicitMethods();
		}
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	public static ICPPMethod[] getDeclaredMethods(ICPPInternalClassTypeMixinHost host) {
		if (host.getDefinition() == null) {
			host.checkForDefinition();
			if (host.getDefinition() == null) {
				ICPPClassType backup= getBackupDefinition(host);
				if (backup != null)
					return backup.getDeclaredMethods();

				return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
			}
		}
		IBinding binding = null;
		ICPPMethod[] result = null;

		IASTDeclaration[] decls = host.getCompositeTypeSpecifier().getMembers();
		for (IASTDeclaration decl : decls) {
			while (decl instanceof ICPPASTTemplateDeclaration)
				decl = ((ICPPASTTemplateDeclaration) decl).getDeclaration();
			if (decl instanceof IASTSimpleDeclaration) {
				final IASTSimpleDeclaration sdecl = (IASTSimpleDeclaration) decl;
				if (!((ICPPASTDeclSpecifier) sdecl.getDeclSpecifier()).isFriend()) {
					IASTDeclarator[] dtors = sdecl.getDeclarators();
					for (IASTDeclarator dtor : dtors) {
						binding = ASTQueries.findInnermostDeclarator(dtor).getName().resolveBinding();
						if (binding instanceof ICPPMethod)
							result = ArrayUtil.append(ICPPMethod.class, result, (ICPPMethod) binding);
					}
				}
			} else if (decl instanceof IASTFunctionDefinition) {
				final IASTFunctionDefinition fdef = (IASTFunctionDefinition) decl;
				if (!((ICPPASTDeclSpecifier) fdef.getDeclSpecifier()).isFriend()) {
					IASTDeclarator dtor = fdef.getDeclarator();
					dtor = ASTQueries.findInnermostDeclarator(dtor);
					binding = dtor.getName().resolveBinding();
					if (binding instanceof ICPPMethod) {
						result = ArrayUtil.append(ICPPMethod.class, result, (ICPPMethod) binding);
					}
				}
			} else if (decl instanceof ICPPASTUsingDeclaration) {
				IASTName n = ((ICPPASTUsingDeclaration) decl).getName();
				binding = n.resolveBinding();
				if (binding instanceof ICPPUsingDeclaration) {
					IBinding[] bs = ((ICPPUsingDeclaration) binding).getDelegates();
					for (IBinding element : bs) {
						if (element instanceof ICPPMethod)
							result = ArrayUtil.append(ICPPMethod.class, result, (ICPPMethod) element);
					}
				} else if (binding instanceof ICPPMethod) {
					result = ArrayUtil.append(ICPPMethod.class, result, (ICPPMethod) binding);
				}
			}
		}
		return ArrayUtil.trim(ICPPMethod.class, result);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getConstructors()
	 */
	public static ICPPConstructor[] getConstructors(ICPPInternalClassTypeMixinHost host) {
		ICPPClassScope scope = host.getCompositeScope();
		if (scope == null) {
			return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
		}
		return scope.getConstructors();
	}

	public static ICPPClassType[] getNestedClasses(ICPPInternalClassTypeMixinHost host) {
		if (host.getDefinition() == null) {
			host.checkForDefinition();
			if (host.getDefinition() == null) {
				ICPPClassType backup= getBackupDefinition(host);
				if (backup != null)
					return backup.getNestedClasses();

				return ICPPClassType.EMPTY_CLASS_ARRAY;
			}
		}

		ICPPClassType[] result = null;

		IASTDeclaration[] decls = host.getCompositeTypeSpecifier().getMembers();
		for (IASTDeclaration decl : decls) {
			while (decl instanceof ICPPASTTemplateDeclaration)
				decl = ((ICPPASTTemplateDeclaration) decl).getDeclaration();
			if (decl instanceof IASTSimpleDeclaration) {
				IBinding binding = null;
				IASTDeclSpecifier declSpec = ((IASTSimpleDeclaration) decl).getDeclSpecifier();
				if (declSpec instanceof ICPPASTCompositeTypeSpecifier) {
					binding = ((ICPPASTCompositeTypeSpecifier) declSpec).getName().resolveBinding();
				} else if (declSpec instanceof ICPPASTElaboratedTypeSpecifier &&
						((IASTSimpleDeclaration) decl).getDeclarators().length == 0) {
					binding = ((ICPPASTElaboratedTypeSpecifier) declSpec).getName().resolveBinding();
				}
				if (binding instanceof ICPPClassType)
					result = ArrayUtil.append(ICPPClassType.class, result, (ICPPClassType) binding);
			}
		}
		return ArrayUtil.trim(ICPPClassType.class, result);
	}

	public static IField[] getFields(ICPPClassType ct, IASTNode point) {
		IField[] fields = getDeclaredFields(ct, point);
		ICPPClassType[] bases = getAllBases(ct, point);
		for (ICPPClassType base : bases) {
			fields = ArrayUtil.addAll(IField.class, fields, getDeclaredFields(base, point));
		}
		return ArrayUtil.trim(IField.class, fields);
	}

	public static IField findField(ICPPClassType ct, String name) {
		IBinding[] bindings = CPPSemantics.findBindings(ct.getCompositeScope(), name, true);
		IField field = null;
		for (IBinding binding : bindings) {
			if (binding instanceof IField) {
				if (field == null) {
					field = (IField) binding;
				} else {
					IASTNode[] decls= ASTInternal.getDeclarationsOfBinding(ct);
					IASTNode node= (decls != null && decls.length > 0) ? decls[0] : null;
					return new CPPField.CPPFieldProblem(ct, node, IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, name.toCharArray());
				}
			}
		}
		return field;
	}

	/**
	 * Returns whether {@code method} is virtual. This is the case if it is declared to be virtual
	 * or overrides another virtual method.
	 */
	public static boolean isVirtual(ICPPMethod m) {
		if (m instanceof ICPPConstructor)
			return false;
		if (m.isVirtual())
			return true;

		final char[] mname= m.getNameCharArray();
		final ICPPClassType mcl= m.getClassOwner();
		if (mcl != null) {
			final ICPPFunctionType mft= m.getType();
			IASTNode point = null; // Instantiation of dependent expressions may not work
			ICPPMethod[] allMethods= ClassTypeHelper.getMethods(mcl, point);
			for (ICPPMethod method : allMethods) {
				if (CharArrayUtils.equals(mname, method.getNameCharArray()) && functionTypesAllowOverride(mft, method.getType())) {
					if (method.isVirtual()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Checks if the function types are consistent enough to be considered overrides.
	 */
	private static boolean functionTypesAllowOverride(ICPPFunctionType a, ICPPFunctionType b) {
        if (a.isConst() != b.isConst() || a.isVolatile() != b.isVolatile() || a.takesVarArgs() != b.takesVarArgs()) {
            return false;
        }

        IType[] paramsA = a.getParameterTypes();
        IType[] paramsB = b.getParameterTypes();

        if (paramsA.length == 1 && paramsB.length == 0) {
			if (!SemanticUtil.isVoidType(paramsA[0]))
				return false;
		} else if (paramsB.length == 1 && paramsA.length == 0) {
			if (!SemanticUtil.isVoidType(paramsB[0]))
				return false;
		} else if (paramsA.length != paramsB.length) {
		    return false;
		} else {
			for (int i = 0; i < paramsA.length; i++) {
		        if (paramsA[i] == null || ! paramsA[i].isSameType(paramsB[i]))
		            return false;
		    }
		}
		return true;
	}

	/**
	 * Returns {@code true} if {@code source} overrides {@code target}.
	 */
	public static boolean isOverrider(ICPPMethod source, ICPPMethod target) {
		if (source instanceof ICPPConstructor || target instanceof ICPPConstructor)
			return false;
		if (!isVirtual(target))
			return false;
		if (!functionTypesAllowOverride(source.getType(), target.getType()))
			return false;

		final ICPPClassType sourceClass= source.getClassOwner();
		final ICPPClassType targetClass= target.getClassOwner();
		if (sourceClass == null || targetClass == null)
			return false;

		ICPPClassType[] bases= getAllBases(sourceClass, null);
		for (ICPPClassType base : bases) {
			if (base.isSameType(targetClass))
				return true;
		}

		return false;
	}

	/**
	 * Returns all methods that are overridden by the given {@code method}.
	 */
	public static ICPPMethod[] findOverridden(ICPPMethod method, IASTNode point) {
		if (method instanceof ICPPConstructor)
			return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;

		final char[] mname= method.getNameCharArray();
		final ICPPClassType mcl= method.getClassOwner();
		if (mcl == null)
			return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;

		final ArrayList<ICPPMethod> result= new ArrayList<ICPPMethod>();
		final HashMap<ICPPClassType, Boolean> virtualInClass= new HashMap<ICPPClassType, Boolean>();
		final ICPPFunctionType mft= method.getType();

		virtualInClass.put(mcl, method.isVirtual());
		ICPPBase[] bases= getBases(mcl, point);
		for (ICPPBase base : bases) {
			IBinding b= base.getBaseClass();
			if (b instanceof ICPPClassType) {
				findOverridden((ICPPClassType) b, mname, mft, virtualInClass, result);
			}
		}

		// list is filled from most derived up to here, reverse it
		Collections.reverse(result);
		return result.toArray(new ICPPMethod[result.size()]);
	}

	/**
	 * Searches for overridden methods starting in {@code cl}. The map {@code virtualInClass} contains a mapping
	 * of classes that have been visited to the information whether they (or a base-class) contain an overridden
	 * method.
	 * Returns whether {@code cl} contains an overridden method.
	 */
	private static boolean findOverridden(ICPPClassType cl, char[] mname, ICPPFunctionType mft,
			HashMap<ICPPClassType, Boolean> virtualInClass, ArrayList<ICPPMethod> result) {
		Boolean visitedBefore= virtualInClass.get(cl);
		if (visitedBefore != null)
			return visitedBefore;

		ICPPMethod[] methods= cl.getDeclaredMethods();
		ICPPMethod candidate= null;
		boolean hasOverridden= false;
		for (ICPPMethod method : methods) {
			if (CharArrayUtils.equals(mname, method.getNameCharArray()) && functionTypesAllowOverride(mft,method.getType())) {
				candidate= method;
				hasOverridden= method.isVirtual();
				break;
			}
		}

		// prevent recursion
		virtualInClass.put(cl, hasOverridden);
		ICPPBase[] bases= cl.getBases();
		for (ICPPBase base : bases) {
			IBinding b= base.getBaseClass();
			if (b instanceof ICPPClassType) {
				if (findOverridden((ICPPClassType) b, mname, mft, virtualInClass, result)) {
					hasOverridden= true;
				}
			}
		}
		if (hasOverridden) {
			// the candidate is virtual
			if (candidate != null)
				result.add(candidate);
			virtualInClass.put(cl, hasOverridden);
		}
		return hasOverridden;
	}

	/**
	 * Returns all methods found in the index, that override the given {@code method}.
	 * @throws CoreException
	 */
	public static ICPPMethod[] findOverriders(IIndex index, ICPPMethod method) throws CoreException {
		if (!isVirtual(method))
			return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;

		final ICPPClassType mcl= method.getClassOwner();
		if (mcl == null)
			return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;

		ICPPClassType[] subclasses= getSubClasses(index, mcl);
		return findOverriders(subclasses, method);
	}

	/**
	 * Returns all methods belonging to the given set of classes that override the given {@code method}.
	 */
	public static ICPPMethod[] findOverriders(ICPPClassType[] subclasses, ICPPMethod method) {
		final char[] mname= method.getNameCharArray();
		final ICPPFunctionType mft= method.getType();
		final ArrayList<ICPPMethod> result= new ArrayList<ICPPMethod>();
		for (ICPPClassType subClass : subclasses) {
			ICPPMethod[] methods= subClass.getDeclaredMethods();
			for (ICPPMethod candidate : methods) {
				if (CharArrayUtils.equals(mname, candidate.getNameCharArray()) &&
						functionTypesAllowOverride(mft, candidate.getType())) {
					result.add(candidate);
				}
			}
		}
		return result.toArray(new ICPPMethod[result.size()]);
	}

	private static ICPPClassType[] getSubClasses(IIndex index, ICPPClassType mcl) throws CoreException {
		List<ICPPBinding> result= new LinkedList<ICPPBinding>();
		HashSet<String> handled= new HashSet<String>();
		getSubClasses(index, mcl, result, handled);
		result.remove(0);
		return result.toArray(new ICPPClassType[result.size()]);
	}

	private static void getSubClasses(IIndex index, ICPPBinding classOrTypedef, List<ICPPBinding> result, HashSet<String> handled) throws CoreException {
		if (!(classOrTypedef instanceof IType))
			return;

		final String key = ASTTypeUtil.getType((IType) classOrTypedef, true);
		if (!handled.add(key)) {
			return;
		}

		if (classOrTypedef instanceof ICPPClassType) {
			result.add(classOrTypedef);
		}

		IIndexName[] names= index.findNames(classOrTypedef, IIndex.FIND_REFERENCES | IIndex.FIND_DEFINITIONS);
		for (IIndexName indexName : names) {
			if (indexName.isBaseSpecifier()) {
				IIndexName subClassDef= indexName.getEnclosingDefinition();
				if (subClassDef != null) {
					IBinding subClass= index.findBinding(subClassDef);
					if (subClass instanceof ICPPBinding) {
						getSubClasses(index, (ICPPBinding) subClass, result, handled);
					}
				}
			}
		}
	}

	public enum MethodKind {
		DEFAULT_CTOR,
		COPY_CTOR,
		MOVE_CTOR,
		COPY_ASSIGNMENT_OP,
		MOVE_ASSIGNMENT_OP,
		DTOR,
		OTHER
	}

	public static MethodKind getMethodKind(ICPPClassType classType, ICPPMethod method) {
		if (method instanceof ICPPConstructor) {
			final List<IType> params= getTypesOfRequiredParameters(method);
			if (params.isEmpty())
				return MethodKind.DEFAULT_CTOR;
			if (params.size() == 1) {
				IType t= SemanticUtil.getNestedType(params.get(0), SemanticUtil.TDEF);
				if (SemanticUtil.isVoidType(t))
					return MethodKind.DEFAULT_CTOR;

				ICPPReferenceType refToClass = getRefToClass(classType, t);
				if (refToClass != null)
					return refToClass.isRValueReference() ? MethodKind.MOVE_CTOR : MethodKind.COPY_CTOR;
			}
			return MethodKind.OTHER;
		}

		if (method.isDestructor())
			return MethodKind.DTOR;

		if (CharArrayUtils.equals(method.getNameCharArray(), OverloadableOperator.ASSIGN.toCharArray())) {
			final List<IType> params= getTypesOfRequiredParameters(method);
			if (params.size() == 1) {
				IType t= params.get(0);
				ICPPReferenceType refToClass = getRefToClass(classType, t);
				if (refToClass != null)
					return refToClass.isRValueReference() ? MethodKind.MOVE_ASSIGNMENT_OP : MethodKind.COPY_ASSIGNMENT_OP;
			}
			return MethodKind.OTHER;
		}
		return MethodKind.OTHER;
	}

	/**
	 * Returns types of method parameters that don't have defaults.
	 */
	private static List<IType> getTypesOfRequiredParameters(ICPPMethod method) {
		ICPPParameter[] parameters = method.getParameters();
		if (parameters.length == 0)
			return Collections.emptyList();
		List<IType> types = new ArrayList<IType>(parameters.length);
		for (ICPPParameter parameter : parameters) {
			if (!parameter.hasDefaultValue() && !parameter.isParameterPack())
				types.add(parameter.getType());
		}
		return types;
	}

	/**
	 * For implicit methods the exception specification is inherited, search it.
	 */
	public static IType[] getInheritedExceptionSpecification(ICPPMethod implicitMethod, IASTNode point) {
		// See 15.4.13
		ICPPClassType owner= implicitMethod.getClassOwner();
		if (owner == null || ClassTypeHelper.getBases(owner, point).length == 0)
			return null;

		// We use a list as types aren't comparable, and can have duplicates (15.4.6)
		MethodKind kind= getMethodKind(owner, implicitMethod);
		if (kind == MethodKind.OTHER)
			return null;

		List<IType> inheritedTypeids = new ArrayList<IType>();
		ICPPClassType[] bases= getAllBases(owner, point);
		for (ICPPClassType base : bases) {
			if (!(base instanceof ICPPDeferredClassInstance)) {
				ICPPMethod  baseMethod= getMethodInClass(base, kind, point);
				if (baseMethod != null) {
					IType[] baseExceptionSpec= baseMethod.getExceptionSpecification();
					if (baseExceptionSpec == null)
						return null;
					for (IType baseTypeId : baseMethod.getExceptionSpecification()) {
						inheritedTypeids.add(baseTypeId);
					}
				}
			}
		}
		return inheritedTypeids.toArray(new IType[inheritedTypeids.size()]);
	}

	/**
	 * If {@code type} is a, possibly qualified, reference type referring to {@code classType},
	 * returns that reference type. Otherwise returns {@code null}.
	 */
	private static ICPPReferenceType getRefToClass(ICPPClassType classType, IType type) {
		while (type instanceof ITypedef) {
			type= ((ITypedef) type).getType();
		}

		if (type instanceof ICPPReferenceType) {
			ICPPReferenceType refType = (ICPPReferenceType) type;
			type= refType.getType();
			while (type instanceof ITypedef) {
				type= ((ITypedef) type).getType();
			}
			if (type instanceof IQualifierType) {
				type= ((IQualifierType) type).getType();
				if (classType.isSameType(type))
					return refType;
			}
		}
		return null;
	}

	private static ICPPMethod getMethodInClass(ICPPClassType ct, MethodKind kind, IASTNode point) {
		switch (kind) {
		case DEFAULT_CTOR:
		case COPY_CTOR:
		case MOVE_CTOR:
			for (ICPPConstructor ctor : getConstructors(ct, point)) {
				if (!ctor.isImplicit() && getMethodKind(ct, ctor) == kind)
					return ctor;
			}
			return null;
		case COPY_ASSIGNMENT_OP:
		case MOVE_ASSIGNMENT_OP:
			for (ICPPMethod method : getDeclaredMethods(ct, point)) {
				if (method instanceof ICPPConstructor)
					continue;
				if (getMethodKind(ct, method) == kind)
					return method;
			}
			return null;
		case DTOR:
			for (ICPPMethod method : getDeclaredMethods(ct, point)) {
				if (method.isDestructor())
					return method;
			}
			return null;
		case OTHER:
			break;
		}
		return null;
	}

	/**
	 * Checks whether class is abstract, i.e. has pure virtual functions that were
	 * not implemented in base after declaration.
	 *
	 * NOTE: The method produces complete results for template instantiations
	 * but doesn't take into account base classes and methods dependent on unspecified
	 * template parameters.
	 */
	public static ICPPMethod[] getPureVirtualMethods(ICPPClassType classType, IASTNode point) {
		Map<String, List<ICPPMethod>> result= collectPureVirtualMethods(classType,
				new HashMap<ICPPClassType, Map<String, List<ICPPMethod>>>(), point);

		int resultArraySize = 0;
		for (List<ICPPMethod> methods : result.values()) {
			resultArraySize += methods.size();
		}
		ICPPMethod[] resultArray = new ICPPMethod[resultArraySize];
		int resultArrayIdx = 0;
		for (List<ICPPMethod> methods : result.values()) {
			for (ICPPMethod method : methods) {
				resultArray[resultArrayIdx++] = method;
			}
		}
		return resultArray;
	}

	private static Map<String, List<ICPPMethod>> collectPureVirtualMethods(ICPPClassType classType,
			Map<ICPPClassType, Map<String, List<ICPPMethod>>> cache, IASTNode point) {
		Map<String, List<ICPPMethod>> result = cache.get(classType);
		if (result != null)
			return result;

		result= new HashMap<String, List<ICPPMethod>>();
		cache.put(classType, result);

		// Look at the pure virtual methods of the base classes
		Set<IBinding> handledBaseClasses= new HashSet<IBinding>();
		for (ICPPBase base : ClassTypeHelper.getBases(classType, point)) {
			final IBinding baseClass = base.getBaseClass();
			if (baseClass instanceof ICPPClassType && handledBaseClasses.add(baseClass)) {
				Map<String, List<ICPPMethod>> pureVirtuals = collectPureVirtualMethods((ICPPClassType) baseClass, cache, point);
				// Merge derived pure virtual methods
				for (String key : pureVirtuals.keySet()) {
					List<ICPPMethod> list = result.get(key);
					if (list == null) {
						list= new ArrayList<ICPPMethod>();
						result.put(key, list);
					}
					list.addAll(pureVirtuals.get(key));
				}
			}
		}

		// Remove overridden pure-virtual methods and add in new pure virtuals.
		final ObjectSet<ICPPMethod> methods = getOwnMethods(classType, point);
		for (ICPPMethod method : methods) {
			String key= getMethodNameForOverrideKey(method);
			List<ICPPMethod> list = result.get(key);
			if (list != null) {
				final ICPPFunctionType methodType = method.getType();
				for (Iterator<ICPPMethod> it= list.iterator(); it.hasNext(); ) {
					ICPPMethod pureVirtual = it.next();
					if (functionTypesAllowOverride(methodType, pureVirtual.getType())) {
						it.remove();
					}
				}
			}
			if (method.isPureVirtual()) {
				if (list == null) {
					list= new ArrayList<ICPPMethod>();
					result.put(key, list);
				}
				list.add(method);
			} else if (list != null && list.isEmpty()) {
				result.remove(key);
			}
		}
		return result;
	}

	private static String getMethodNameForOverrideKey(ICPPMethod method) {
		if (method.isDestructor()) {
			// Destructor's names may differ but they will override each other.
			return DESTRUCTOR_OVERRIDE_KEY;
		} else {
			return method.getName();
		}
	}
}
