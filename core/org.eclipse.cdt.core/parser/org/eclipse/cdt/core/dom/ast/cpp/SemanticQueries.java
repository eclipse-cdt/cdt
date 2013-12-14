/*******************************************************************************
 * Copyright (c) 2012 Nathan Ridge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nathan Ridge - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.parser.util.CollectionUtils;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

/**
 * This class exposes semantic queries about C++ code to clients such
 * as code analysis.
 * 
 * @since 5.5
 */
public class SemanticQueries {

	public static boolean isCopyOrMoveConstructor(ICPPConstructor constructor) {
		return isCopyOrMoveConstructor(constructor, CopyOrMoveConstructorKind.COPY_OR_MOVE);
	}

	public static boolean isMoveConstructor(ICPPConstructor constructor) {
		return isCopyOrMoveConstructor(constructor, CopyOrMoveConstructorKind.MOVE);
	}

	public static boolean isCopyConstructor(ICPPConstructor constructor) {
		return isCopyOrMoveConstructor(constructor, CopyOrMoveConstructorKind.COPY);
	}

	private enum CopyOrMoveConstructorKind { COPY, MOVE, COPY_OR_MOVE }

	private static boolean isCopyOrMoveConstructor(ICPPConstructor constructor, CopyOrMoveConstructorKind kind) {
		// 12.8/2-3 [class.copy]:
		// "A non-template constructor for class X is a copy [move] constructor
		//  if its first parameter is of type X&[&], const X&[&], volatile X&[&]
		//  or const volatile X&[&], and either there are no other parametrs or
		//  else all other parametrs have default arguments."
		if (constructor instanceof ICPPFunctionTemplate)
			return false;
		if (!isCallableWithNumberOfArguments(constructor, 1))
			return false;
		IType firstArgumentType = constructor.getType().getParameterTypes()[0];
		firstArgumentType = SemanticUtil.getNestedType(firstArgumentType, TDEF);
		if (!(firstArgumentType instanceof ICPPReferenceType))
			return false;
		ICPPReferenceType firstArgReferenceType = (ICPPReferenceType) firstArgumentType;
		boolean isRvalue = firstArgReferenceType.isRValueReference();
		if (isRvalue && kind == CopyOrMoveConstructorKind.COPY)
			return false;
		if (!isRvalue && kind == CopyOrMoveConstructorKind.MOVE)
			return false;
		firstArgumentType = firstArgReferenceType.getType();
		firstArgumentType = SemanticUtil.getNestedType(firstArgumentType, CVTYPE);
		ICPPClassType classType = constructor.getClassOwner();
		if (classType instanceof ICPPClassTemplate)
			classType = CPPTemplates.createDeferredInstance((ICPPClassTemplate) classType);
		return firstArgumentType.isSameType(classType);
	}

	private static boolean isCallableWithNumberOfArguments(ICPPFunction function, int numArguments) {
		return function.getParameters().length >= numArguments
			&& function.getRequiredArgumentCount() <= numArguments;
	}

	/**
	 * Returns all pure virtual methods of a class. Inherited pure virtual methods
	 * that have not been implemented are also returned. 
	 *
	 * NOTE: The method produces complete results for template instantiations
	 * but doesn't take into account base classes and methods dependent on unspecified
	 * template parameters.
	 * 
	 * @param classType the class whose pure virtual methods should be returned
	 * @param point the point of template instantiation, if applicable
	 * @return an array containing all pure virtual methods of the class
	 * @since 5.6
	 */
	public static ICPPMethod[] getPureVirtualMethods(ICPPClassType classType, IASTNode point) {
		return new PureVirtualMethodCollector().collect(classType, point);
	}
	
	/** Helper class for {@link #getPureVirtualMethods(ICPPClassType, IASTNode)} */
	private static class PureVirtualMethodCollector {
		/**
		 * This class represents a mapping of virtual methods in a class hierarchy
		 * to their final overriders (see [class.virtual] p2). Since a class hierarchy
		 * can contain multiple subobjects of the same type (if multiple, non-virtual
		 * inheritance is used), and the pure virtual methods of each subobject must
		 * be implemented independently, we give each subobject of a given type a
		 * number, and for each method we keep track of the final overriders for each
		 * subobject number. Generally, there should be only one final overrider per
		 * subobject (in fact the program is ill-formed if there is more than one),
		 * but to accurately detect pure virtual methods that haven't been overridden,
		 * we need to be able to keep track of more than one at a time.
		 */
		private static class FinalOverriderMap {
			private Map<ICPPMethod, Map<Integer, List<ICPPMethod>>> fMap
					= new HashMap<ICPPMethod, Map<Integer, List<ICPPMethod>>>();
			
			/**
			 * Add 'overrider' as a final ovverider of 'method' in subobject
			 * 'subobjectNumber'.
			 */
			public void add(ICPPMethod method, int subobjectNumber, ICPPMethod overrider) {
				Map<Integer, List<ICPPMethod>> overriders = fMap.get(method);
				if (overriders == null) {
					overriders = new HashMap<Integer, List<ICPPMethod>>();
					fMap.put(method, overriders);
				}
				CollectionUtils.listMapGet(overriders, subobjectNumber).add(overrider);
			}
			
			/**
			 * For each subobject for which 'method' has been overridden, set
			 * 'overrider' to be its (only) final overrider.
			 */
			public void replaceForAllSubobjects(ICPPMethod method, ICPPMethod overrider) {
				Map<Integer, List<ICPPMethod>> overriders = fMap.get(method);
				if (overriders == null)
					return;
				for (Integer i : overriders.keySet()) {
					List<ICPPMethod> overridersForSubobject = CollectionUtils.listMapGet(overriders, i);
					overridersForSubobject.clear();
					overridersForSubobject.add(overrider);
				}
			}
			
			/**
			 * Merge the final overriders from another FinalOverriderMap into this one.
			 */
			public void addOverriders(FinalOverriderMap other) {
				for (ICPPMethod method : other.fMap.keySet()) {
					Map<Integer, List<ICPPMethod>> overriders = fMap.get(method);
					if (overriders == null) {
						overriders = new HashMap<Integer, List<ICPPMethod>>();
						fMap.put(method, overriders);
					}
					Map<Integer, List<ICPPMethod>> otherOverriders = other.fMap.get(method);
					for (Integer i : otherOverriders.keySet()) {
						CollectionUtils.listMapGet(overriders, i).addAll(otherOverriders.get(i));
					}
				}
			}
			
			/**
			 * Go through the final overrider map and find functions which are
			 * pure virtual in the hierarchy's root. These are functions which
			 * are declared pure virtual, and which have a single final overrider
			 * which is themself, meaning they have not been overridden.
			 */
			public ICPPMethod[] collectPureVirtualMethods() {
				List<ICPPMethod> pureVirtualMethods = new ArrayList<ICPPMethod>();
				for (ICPPMethod method : fMap.keySet()) {
					if (method.isPureVirtual()) {
						Map<Integer, List<ICPPMethod>> finalOverriders = fMap.get(method);
						for (Integer subobjectNumber : finalOverriders.keySet()) {
							List<ICPPMethod> overridersForSubobject = finalOverriders.get(subobjectNumber);
							if (overridersForSubobject.size() == 1 && overridersForSubobject.get(0) == method) {
								pureVirtualMethods.add(method);
							}
						}
					}
				}
				return pureVirtualMethods.toArray(new ICPPMethod[pureVirtualMethods.size()]);
			}
		}
		
		// The last subobject number used for each type in the hierarchy. This is used to
		// assign subobject numbers to subobjects. Virtual subobjects get a subobject
		// number of zero, while non-virtual subobjects are number starting from one.
		private Map<ICPPClassType, Integer> subobjectNumbers = new HashMap<ICPPClassType, Integer>();
		
		// Cache of final overrider maps for virtual base subobjects. Since such subobjects
		// only occur once in the hierarchy, we can cache the final overrider maps we
		// compute for them.
		private Map<ICPPClassType, FinalOverriderMap> virtualBaseCache = new HashMap<ICPPClassType, FinalOverriderMap>();
		
		public ICPPMethod[] collect(ICPPClassType root, IASTNode point) {
			FinalOverriderMap finalOverriderMap = collectFinalOverriders(root, false, new HashSet<ICPPClassType>(), point);
			return finalOverriderMap.collectPureVirtualMethods();
		}

		/**
		 * Compute the final overrider map for a subtree in a class hierarchy.
		 * 
		 * @param classType the root of the subtree in question 
		 * @param isVirtualBase whether 'classType' is inherited virtually
		 * @param inheritanceChain the chain of classes from the entire hierarchy's root to 'classType'.
		 *                         This is used to guard against circular inheritance.
		 * @param point the point of template instantiation, if applicable
		 * @return the computed final overrider map
		 */
		private FinalOverriderMap collectFinalOverriders(ICPPClassType classType, boolean isVirtualBase, 
				Set<ICPPClassType> inheritanceChain, IASTNode point) {
			FinalOverriderMap result = new FinalOverriderMap();
			
			inheritanceChain.add(classType);
			
			// Determine the subobject number for the current class.
			int subobjectNumber = 0;
			if (!isVirtualBase) {
				Integer lastNumber = subobjectNumbers.get(classType);
				subobjectNumber = (lastNumber == null ? 0 : lastNumber) + 1;
				subobjectNumbers.put(classType, subobjectNumber);
			}
			
			// Go through our base classes.
			for (ICPPBase base : ClassTypeHelper.getBases(classType, point)) {
				IBinding baseClass = base.getBaseClass();
				if (!(baseClass instanceof ICPPClassType))
					continue;
				ICPPClassType baseType = (ICPPClassType) baseClass;
				
				// Guard against circular inheritance.
				if (inheritanceChain.contains(baseType))
					continue;
				
				// Collect final overrider information from the base class.
				// If it's a virtual base class and we've already processed it
				// in this class hierarchy, don't process it again.
				FinalOverriderMap baseOverriderMap;
				if (base.isVirtual()) {
					baseOverriderMap = virtualBaseCache.get(baseType);
					if (baseOverriderMap == null) {
						baseOverriderMap = collectFinalOverriders(baseType, true, inheritanceChain, point);
						virtualBaseCache.put(baseType, baseOverriderMap);
					}
				} else {
					baseOverriderMap = collectFinalOverriders(baseType, false, inheritanceChain, point);
				}
			
				// Merge final overrider information from base class into this class.
				result.addOverriders(baseOverriderMap);
			}
			
			// Go through our own methods.
			for (ICPPMethod method : ClassTypeHelper.getOwnMethods(classType, point)) {
				// For purposes of this computation, every virtual method is
				// deemed for override itself.
				result.add(method, subobjectNumber, method);
				
				// Find all methods overridden by this method, and set their final overrider
				// to be this method.
				ICPPMethod[] overriddenMethods = ClassTypeHelper.findOverridden(method, point);
				for (ICPPMethod overriddenMethod : overriddenMethods)
					result.replaceForAllSubobjects(overriddenMethod, method);
			}
			
			inheritanceChain.remove(classType);
			
			return result;
		}
	}
}
