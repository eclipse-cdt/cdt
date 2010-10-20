/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

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
import org.eclipse.cdt.core.dom.ast.IFunctionType;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
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
 *  @see CPPClassType
 *  @see CPPClassTemplate
 */
public class ClassTypeHelper {
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
			while (decl instanceof ICPPASTTemplateDeclaration)
				decl = ((ICPPASTTemplateDeclaration)decl).getDeclaration();

			if (decl instanceof IASTSimpleDeclaration) {
				ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) ((IASTSimpleDeclaration)decl).getDeclSpecifier();
				if (declSpec.isFriend()) {
					IASTDeclarator[] dtors = ((IASTSimpleDeclaration)decl).getDeclarators();
					if (declSpec instanceof ICPPASTElaboratedTypeSpecifier && dtors.length == 0) {
						resultSet.put(((ICPPASTElaboratedTypeSpecifier)declSpec).getName().resolveBinding());
					} else {
						for (IASTDeclarator dtor : dtors) {
							if (dtor == null) break;
							dtor= ASTQueries.findInnermostDeclarator(dtor);
							resultSet.put(dtor.getName().resolveBinding());
						}    
					}
				}
			} else if (decl instanceof IASTFunctionDefinition) {
				ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) ((IASTFunctionDefinition)decl).getDeclSpecifier();
				if (declSpec.isFriend()) {
					IASTDeclarator dtor = ((IASTFunctionDefinition)decl).getDeclarator();
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
			for (IBinding friend : classType.getFriends()) {
				if (friend instanceof ICPPClassType && type.isSameType((IType) friend)) {
					return true;
				}
			}
		} else if (binding instanceof ICPPFunction) {
			type = ((ICPPFunction) binding).getType();
			char[] name = binding.getNameCharArray();
			for (IBinding friend : classType.getFriends()) {
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
				IASTDeclarator[] dtors = ((IASTSimpleDeclaration)decl).getDeclarators();
				for (IASTDeclarator dtor : dtors) {
					binding = ASTQueries.findInnermostDeclarator(dtor).getName().resolveBinding();
					if (binding instanceof ICPPField)
						result = (ICPPField[]) ArrayUtil.append(ICPPField.class, result, binding);
				}
			} else if (decl instanceof ICPPASTUsingDeclaration) {
				IASTName n = ((ICPPASTUsingDeclaration)decl).getName();
				binding = n.resolveBinding();
				if (binding instanceof ICPPUsingDeclaration) {
					IBinding[] bs = ((ICPPUsingDeclaration)binding).getDelegates();
					for (IBinding element : bs) {
						if (element instanceof ICPPField)
							result = (ICPPField[]) ArrayUtil.append(ICPPField.class, result, element);
					}
				} else if (binding instanceof ICPPField) {
					result = (ICPPField[]) ArrayUtil.append(ICPPField.class, result, binding);
				}
			}
		}
		return (ICPPField[]) ArrayUtil.trim(ICPPField.class, result);
	}
	
	/**
	 * Returns all direct and indirect base classes. 
	 * @param classType a class
	 * @return An array of visible base classes in arbitrary order.
	 */
	public static ICPPClassType[] getAllBases(ICPPClassType classType) {
		HashSet<ICPPClassType> result= new HashSet<ICPPClassType>();
		result.add(classType);
		getAllBases(classType, result);
		result.remove(classType);
		return result.toArray(new ICPPClassType[result.size()]);
	}
	
	private static void getAllBases(ICPPClassType classType, HashSet<ICPPClassType> result) {
		ICPPBase[] bases= classType.getBases();
		for (ICPPBase base : bases) {
			IBinding b= base.getBaseClass();
			if (b instanceof ICPPClassType) {
				final ICPPClassType baseClass = (ICPPClassType) b;
				if (result.add(baseClass)) { 
					getAllBases(baseClass, result);
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

	public static ICPPMethod[] getAllDeclaredMethods(ICPPClassType ct) {
		ICPPMethod[] methods= ct.getDeclaredMethods();
		ICPPClassType[] bases= getAllBases(ct);
		for (ICPPClassType base : bases) {
			methods = (ICPPMethod[]) ArrayUtil.addAll(ICPPMethod.class, methods, base.getDeclaredMethods());
		}
		return (ICPPMethod[]) ArrayUtil.trim(ICPPMethod.class, methods);
	}
	
	public static ICPPMethod[] getMethods(ICPPClassType ct) {
		ObjectSet<ICPPMethod> set= new ObjectSet<ICPPMethod>(4);
		set.addAll(ct.getDeclaredMethods());
		ICPPClassScope scope= (ICPPClassScope) ct.getCompositeScope();
		set.addAll(scope.getImplicitMethods());
		
		ICPPClassType[] bases= getAllBases(ct);
		for (ICPPClassType base : bases) {
			set.addAll(base.getDeclaredMethods());
			final IScope compositeScope = base.getCompositeScope();
			if (compositeScope instanceof ICPPClassScope) {
				set.addAll(((ICPPClassScope) compositeScope).getImplicitMethods());
			}
		}
		return set.keyArray(ICPPMethod.class);
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
				decl = ((ICPPASTTemplateDeclaration)decl).getDeclaration();
			if (decl instanceof IASTSimpleDeclaration) {
				final IASTSimpleDeclaration sdecl = (IASTSimpleDeclaration)decl;
				if (!((ICPPASTDeclSpecifier) sdecl.getDeclSpecifier()).isFriend()) {
					IASTDeclarator[] dtors = sdecl.getDeclarators();
					for (IASTDeclarator dtor : dtors) {
						binding = ASTQueries.findInnermostDeclarator(dtor).getName().resolveBinding();
						if (binding instanceof ICPPMethod)
							result = (ICPPMethod[]) ArrayUtil.append(ICPPMethod.class, result, binding);
					}
				}
			} else if (decl instanceof IASTFunctionDefinition) {
				final IASTFunctionDefinition fdef = (IASTFunctionDefinition)decl;
				if (!((ICPPASTDeclSpecifier) fdef.getDeclSpecifier()).isFriend()) {
					IASTDeclarator dtor = fdef.getDeclarator();
					dtor = ASTQueries.findInnermostDeclarator(dtor);
					binding = dtor.getName().resolveBinding();
					if (binding instanceof ICPPMethod) {
						result = (ICPPMethod[]) ArrayUtil.append(ICPPMethod.class, result, binding);
					}
				}
			} else if (decl instanceof ICPPASTUsingDeclaration) {
				IASTName n = ((ICPPASTUsingDeclaration)decl).getName();
				binding = n.resolveBinding();
				if (binding instanceof ICPPUsingDeclaration) {
					IBinding[] bs = ((ICPPUsingDeclaration)binding).getDelegates();
					for (IBinding element : bs) {
						if (element instanceof ICPPMethod)
							result = (ICPPMethod[]) ArrayUtil.append(ICPPMethod.class, result, element);
					}
				} else if (binding instanceof ICPPMethod) {
					result = (ICPPMethod[]) ArrayUtil.append(ICPPMethod.class, result, binding);
				}
			}
		}
		return (ICPPMethod[]) ArrayUtil.trim(ICPPMethod.class, result);
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
				decl = ((ICPPASTTemplateDeclaration)decl).getDeclaration();
			if (decl instanceof IASTSimpleDeclaration) {
				IBinding binding = null;
				IASTDeclSpecifier declSpec = ((IASTSimpleDeclaration) decl).getDeclSpecifier();
				if (declSpec instanceof ICPPASTCompositeTypeSpecifier) {
					binding = ((ICPPASTCompositeTypeSpecifier)declSpec).getName().resolveBinding();
				} else if (declSpec instanceof ICPPASTElaboratedTypeSpecifier &&
						((IASTSimpleDeclaration)decl).getDeclarators().length == 0) {
					binding = ((ICPPASTElaboratedTypeSpecifier)declSpec).getName().resolveBinding();
				}
				if (binding instanceof ICPPClassType)
					result = (ICPPClassType[])ArrayUtil.append(ICPPClassType.class, result, binding);
			} 
		}
		return (ICPPClassType[]) ArrayUtil.trim(ICPPClassType.class, result);
	}

	public static IField[] getFields(ICPPClassType ct) {
		IField[] fields = ct.getDeclaredFields();
		ICPPClassType[] bases = getAllBases(ct);
		for (ICPPClassType base : bases) {
			fields = (IField[]) ArrayUtil.addAll(IField.class, fields, base.getFields());
		}
		return (IField[]) ArrayUtil.trim(IField.class, fields);
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
	 * Returns whether {@code method} is virtual. This is the case if it is declared to be virtual or
	 * overrides another virtual method.
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
			ICPPMethod[] allMethods= mcl.getMethods();
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
		
		ICPPClassType[] bases= getAllBases(sourceClass);
		for (ICPPClassType base : bases) {
			if (base.isSameType(targetClass))
				return true;
		}
		
		return false;
	}

	/**
	 * Returns all methods that are overridden by the given {@code method}.
	 */
	public static ICPPMethod[] findOverridden(ICPPMethod method) {
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
		ICPPBase[] bases= mcl.getBases();
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

	private static final int KIND_DEFAULT_CTOR= 0;
	private static final int KIND_COPY_CTOR= 1;
	private static final int KIND_ASSIGNMENT_OP= 2;
	private static final int KIND_DTOR= 3;
	private static final int KIND_OTHER= 4;
	
	/**
	 * For implicit methods the exception specification is inherited, search it
	 */
	public static IType[] getInheritedExceptionSpecification(ICPPMethod implicitMethod) {
		// See 15.4.13
		ICPPClassType owner= implicitMethod.getClassOwner();
		if (owner == null || owner.getBases().length == 0) 
			return null;

		// we use a list as types aren't comparable, and can have duplicates (15.4.6)
		int kind= getImplicitMethodKind(owner, implicitMethod);
		if (kind == KIND_OTHER)
			return null;
		
		List<IType> inheritedTypeids = new ArrayList<IType>();
		ICPPClassType[] bases= getAllBases(owner);
		for (ICPPClassType base : bases) {
			if (!(base instanceof ICPPDeferredClassInstance)) {
				ICPPMethod  baseMethod= getMethodInClass(base, kind);
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

	private static int getImplicitMethodKind(ICPPClassType ct, ICPPMethod method) {
		if (method instanceof ICPPConstructor) {
			final IFunctionType type= method.getType();
			final IType[] params= type.getParameterTypes();
			if (params.length == 0)
				return KIND_DEFAULT_CTOR;
			if (params.length == 1) {
				IType t= SemanticUtil.getNestedType(params[0], SemanticUtil.TDEF);
				if (SemanticUtil.isVoidType(t))
					return KIND_DEFAULT_CTOR;

				if (isRefToConstClass(ct, t))
					return KIND_COPY_CTOR;
			}
			return KIND_OTHER;
		}

		if (method.isDestructor())
			return KIND_DTOR;

		if (CharArrayUtils.equals(method.getNameCharArray(), OverloadableOperator.ASSIGN.toCharArray())) {
			final IFunctionType type= method.getType();
			final IType[] params= type.getParameterTypes();
			if (params.length == 1) {
				IType t= params[0];
				if (isRefToConstClass(ct, t))
					return KIND_ASSIGNMENT_OP;
			}
			return KIND_OTHER;
		}
		return KIND_OTHER;	
	}

	private static boolean isRefToConstClass(ICPPClassType ct, IType t) {
		while (t instanceof ITypedef)
			t= ((ITypedef) t).getType();
		
		if (t instanceof ICPPReferenceType) {
			t= ((ICPPReferenceType) t).getType();
			while (t instanceof ITypedef)
				t= ((ITypedef) t).getType();
			if (t instanceof IQualifierType) {
				t= ((IQualifierType) t).getType();
				return ct.isSameType(t);
			}
		}
		return false;
	}

	private static ICPPMethod getMethodInClass(ICPPClassType ct, int kind) {
		switch(kind) {
		case KIND_DEFAULT_CTOR:
		case KIND_COPY_CTOR:
			for (ICPPConstructor ctor : ct.getConstructors()) {
				if (!ctor.isImplicit() && getImplicitMethodKind(ct, ctor) == kind)
					return ctor;
			}
			return null;
		case KIND_ASSIGNMENT_OP:
			for (ICPPMethod method : ct.getDeclaredMethods()) {
				if (method instanceof ICPPConstructor)
					continue;
				if (getImplicitMethodKind(ct, method) == kind)
					return method;
			}
			return null;
		case KIND_DTOR:
			for (ICPPMethod method : ct.getDeclaredMethods()) {
				if (method.isDestructor())
					return method;
			}
			return null;
		}
		return null;
	}

	/**
	 * 8.5.1 Aggregates [dcl.init.aggr]
	 * An aggregate is an array or a class (Clause 9) with no user-provided constructors (12.1), 
	 * no private or protected non-static data members (Clause 11), 
	 * no base classes (Clause 10), and no virtual functions (10.3).
	 */
	public static boolean isAggregateClass(ICPPClassType classTarget) {
		if (classTarget.getBases().length > 0)
			return false;
		ICPPMethod[] methods = classTarget.getDeclaredMethods();
		for (ICPPMethod m : methods) {
			if (m instanceof ICPPConstructor)
				return false;
			if (m.isVirtual()) {
				return false;
			}
		}
		ICPPField[] fields = classTarget.getDeclaredFields();
		for (ICPPField field : fields) {
			if (!(field.getVisibility() == ICPPMember.v_public || field.isStatic())) {
				return false;
			}
		}
		return true;
	}
}
