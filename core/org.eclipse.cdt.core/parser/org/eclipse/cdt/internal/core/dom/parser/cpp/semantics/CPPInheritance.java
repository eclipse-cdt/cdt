/*******************************************************************************
 * Copyright (c) 2012, 2015 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.parser.util.CollectionUtils;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;

/**
 * Semantic analysis related to inheritance.
 */
public class CPPInheritance {
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
	public static class FinalOverriderMap {
		private Map<ICPPMethod, Map<Integer, List<ICPPMethod>>> fMap = new HashMap<>();

		/**
		 * Returns the completed final overrider map.
		 */
		public Map<ICPPMethod, Map<Integer, List<ICPPMethod>>> getMap() {
			return fMap;
		}

		// The methods below are meant to be used while computing the final overrider map.

		/**
		 * Adds 'overrider' as a final overrider of 'method' in subobject 'subobjectNumber'.
		 */
		void add(ICPPMethod method, int subobjectNumber, ICPPMethod overrider) {
			Map<Integer, List<ICPPMethod>> overriders = fMap.get(method);
			if (overriders == null) {
				overriders = new HashMap<>();
				fMap.put(method, overriders);
			}
			CollectionUtils.listMapGet(overriders, subobjectNumber).add(overrider);
		}

		/**
		 * For each subobject for which 'method' has been overridden, sets
		 * 'overrider' to be its (only) final overrider.
		 */
		void replaceForAllSubobjects(ICPPMethod method, ICPPMethod overrider) {
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
		 * Merges the final overriders from another FinalOverriderMap into this one.
		 */
		void addOverriders(FinalOverriderMap other) {
			for (ICPPMethod method : other.fMap.keySet()) {
				Map<Integer, List<ICPPMethod>> overriders = fMap.get(method);
				if (overriders == null) {
					overriders = new HashMap<>();
					fMap.put(method, overriders);
				}
				Map<Integer, List<ICPPMethod>> otherOverriders = other.fMap.get(method);
				for (Integer i : otherOverriders.keySet()) {
					mergeOverriders(CollectionUtils.listMapGet(overriders, i), otherOverriders.get(i));
				}
			}
		}

		/**
		 * Merges the overriders from 'source' into 'target'.
		 * Excludes methods in 'source' which themselves have an overrider in 'target'.
		 */
		private void mergeOverriders(List<ICPPMethod> target, List<ICPPMethod> source) {
			List<ICPPMethod> toAdd = new ArrayList<>();
			for (ICPPMethod candidate : source) {
				boolean superseded = false;
				for (ICPPMethod existing : target) {
					if (existing != candidate && ClassTypeHelper.isOverrider(existing, candidate)) {
						superseded = true;
					}
				}
				if (!superseded) {
					toAdd.add(candidate);
				}
			}
			target.addAll(toAdd);
		}
	}

	/**
	 * Returns the final overrider map for a class hierarchy.
	 * Final overrider maps are cached in the AST.
	 *
	 * @param classType the root of the class hierarchy
	 * @return the computed final overrider map
	 */
	public static FinalOverriderMap getFinalOverriderMap(ICPPClassType classType) {
		Map<ICPPClassType, FinalOverriderMap> cache = null;
		IASTNode point = CPPSemantics.getCurrentLookupPoint();
		if (point != null && point.getTranslationUnit() instanceof CPPASTTranslationUnit) {
			cache = ((CPPASTTranslationUnit) point.getTranslationUnit()).getFinalOverriderMapCache();
		}
		FinalOverriderMap result = null;
		if (cache != null) {
			result = cache.get(classType);
		}
		if (result == null) {
			result = FinalOverriderAnalysis.computeFinalOverriderMap(classType);
		}
		if (cache != null) {
			cache.put(classType, result);
		}
		return result;
	}

	/**
	 * If a given virtual method has a unique final overrider in the class hierarchy rooted at the
	 * given class, returns that final overrider. Otherwise, returns null.
	 */
	public static ICPPMethod getFinalOverrider(ICPPMethod method, ICPPClassType hierarchyRoot) {
		FinalOverriderMap map = getFinalOverriderMap(hierarchyRoot);
		Map<Integer, List<ICPPMethod>> finalOverriders = map.getMap().get(method);
		if (finalOverriders != null && finalOverriders.size() == 1) {
			for (Integer subobjectNumber : finalOverriders.keySet()) {
				List<ICPPMethod> overridersForSubobject = finalOverriders.get(subobjectNumber);
				if (overridersForSubobject.size() == 1) {
					return overridersForSubobject.get(0);
				}
			}
		}
		return null;
	}

	private static class FinalOverriderAnalysis {
		/**
		 * Computes the final overrider map for a class hierarchy.
		 *
		 * @param classType the root of the class hierarchy
		 * @return the computed final overrider map
		 */
		public static FinalOverriderMap computeFinalOverriderMap(ICPPClassType classType) {
			return new FinalOverriderAnalysis().collectFinalOverriders(classType, false, new HashSet<ICPPClassType>(),
					CPPSemantics.MAX_INHERITANCE_DEPTH);
		}

		// The last subobject number used for each type in the hierarchy. This is used to
		// assign subobject numbers to subobjects. Virtual subobjects get a subobject
		// number of zero, while non-virtual subobjects are number starting from one.
		private Map<ICPPClassType, Integer> subobjectNumbers = new HashMap<>();

		// Cache of final overrider maps for virtual base subobjects. Since such subobjects
		// only occur once in the hierarchy, we can cache the final overrider maps we
		// compute for them.
		private Map<ICPPClassType, FinalOverriderMap> virtualBaseCache = new HashMap<>();

		/**
		 * Recursive helper function for computeFinalOverriderMap() which computes the final overrider map
		 * for a subtree of a class hierarchy.
		 *
		 * @param classType the root of the subtree in question
		 * @param isVirtualBase whether 'classType' is inherited virtually
		 * @param inheritanceChain the chain of classes from the entire hierarchy's root to 'classType'.
		 *                         This is used to guard against circular inheritance.
		 * @return the computed final overrider map for the subtree
		 */
		private FinalOverriderMap collectFinalOverriders(ICPPClassType classType, boolean isVirtualBase,
				Set<ICPPClassType> inheritanceChain, int maxdepth) {
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
			for (ICPPBase base : classType.getBases()) {
				IBinding baseClass = base.getBaseClass();
				if (!(baseClass instanceof ICPPClassType))
					continue;
				ICPPClassType baseType = (ICPPClassType) baseClass;

				// Guard against circular inheritance.
				if (inheritanceChain.contains(baseType))
					continue;

				// Guard against infinite recursion in inheritance
				// for example A<I> deriving from A<I - 1> without
				// a base case to end the recursion.
				if (maxdepth <= 0)
					continue;

				// Collect final overrider information from the base class.
				// If it's a virtual base class and we've already processed it
				// in this class hierarchy, don't process it again.
				FinalOverriderMap baseOverriderMap;
				if (base.isVirtual()) {
					baseOverriderMap = virtualBaseCache.get(baseType);
					if (baseOverriderMap == null) {
						baseOverriderMap = collectFinalOverriders(baseType, true, inheritanceChain, maxdepth - 1);
						virtualBaseCache.put(baseType, baseOverriderMap);
					}
				} else {
					baseOverriderMap = collectFinalOverriders(baseType, false, inheritanceChain, maxdepth - 1);
				}

				// Merge final overrider information from base class into this class.
				result.addOverriders(baseOverriderMap);
			}

			// Go through our own methods.
			for (ICPPMethod method : ClassTypeHelper.getOwnMethods(classType)) {
				// Skip methods that don't actually belong to us, such as methods brought
				// into scope via a using-declaration.
				if (!(method.getOwner() instanceof ICPPClassType
						&& ((ICPPClassType) method.getOwner()).isSameType(classType))) {
					continue;
				}

				// For purposes of this computation, every virtual method is
				// deemed for override itself.
				result.add(method, subobjectNumber, method);

				// Find all methods overridden by this method, and set their final overrider
				// to be this method.
				ICPPMethod[] overriddenMethods = ClassTypeHelper.findOverridden(method);
				for (ICPPMethod overriddenMethod : overriddenMethods)
					result.replaceForAllSubobjects(overriddenMethod, method);
			}

			inheritanceChain.remove(classType);

			return result;
		}
	}
}
