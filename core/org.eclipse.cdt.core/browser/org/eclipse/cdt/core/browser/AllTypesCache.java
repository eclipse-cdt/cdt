/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.browser;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.browser.util.ArrayUtil;

/**
 * Manages a search cache for types in the workspace. Instead of returning
 * objects of type <code>ICElement</code> the methods of this class returns a
 * list of the lightweight objects <code>TypeInfo</code>.
 * <P>
 * AllTypesCache runs asynchronously using a background job to rebuild the cache
 * as needed. If the cache becomes dirty again while the background job is
 * running, the job is restarted.
 * <P>
 * If <code>getTypes</code> is called in response to a user action, a progress
 * dialog is shown. If called before the background job has finished, getTypes
 * waits for the completion of the background job.
 */
public class AllTypesCache {

	/**
	 * Returns all types in the workspace.
	 */
	public static ITypeInfo[] getAllTypes() {
		final Collection fAllTypes = new ArrayList();
		TypeSearchScope workspaceScope = new TypeSearchScope(true);
		ICProject[] projects = workspaceScope.getEnclosingProjects();
		ITypeInfoVisitor visitor = new ITypeInfoVisitor() {
			public boolean visit(ITypeInfo info) {
				fAllTypes.add(info);
				return true;
			}
			public boolean shouldContinue() { return true; }
		};
		for (int i = 0; i < projects.length; ++i) {
//			TypeCacheManager.getInstance().getCache(projects[i]).accept(visitor);
		}
		return (ITypeInfo[]) fAllTypes.toArray(new ITypeInfo[fAllTypes.size()]);
	}
	
	/**
	 * Returns all types in the given scope.
	 * 
	 * @param scope The search scope
	 * @param kinds Array containing CElement types: C_NAMESPACE, C_CLASS,
	 *              C_UNION, C_ENUMERATION, C_TYPEDEF
	 */
	public static ITypeInfo[] getTypes(ITypeSearchScope scope, int[] kinds) {
		final Collection fTypesFound = new ArrayList();
		final ITypeSearchScope fScope = scope;
		final int[] fKinds = kinds;
		ICProject[] projects = scope.getEnclosingProjects();
		ITypeInfoVisitor visitor = new ITypeInfoVisitor() {
			public boolean visit(ITypeInfo info) {
				if (ArrayUtil.contains(fKinds, info.getCElementType())
					&& (fScope != null && info.isEnclosed(fScope))) {
					fTypesFound.add(info);
				}
				return true;
			}
			public boolean shouldContinue() { return true; }
		};
		for (int i = 0; i < projects.length; ++i) {
//			TypeCacheManager.getInstance().getCache(projects[i]).accept(visitor);
		}
		return (ITypeInfo[]) fTypesFound.toArray(new ITypeInfo[fTypesFound.size()]);
	}
	
	/**
	 * Returns all namespaces in the given scope.
	 * 
	 * @param scope The search scope
	 * @param includeGlobalNamespace <code>true</code> if the global (default) namespace should be returned
	 */
	public static ITypeInfo[] getNamespaces(ITypeSearchScope scope, boolean includeGlobalNamespace) {
		final Collection fTypesFound = new ArrayList();
		final ITypeSearchScope fScope = scope;
		ICProject[] projects = scope.getEnclosingProjects();
		ITypeInfoVisitor visitor = new ITypeInfoVisitor() {
			public boolean visit(ITypeInfo info) {
				if (info.getCElementType() == ICElement.C_NAMESPACE
					&& (fScope != null && info.isEnclosed(fScope))) {
					fTypesFound.add(info);
				}
				return true;
			}
			public boolean shouldContinue() { return true; }
		};
		for (int i = 0; i < projects.length; ++i) {
//			ITypeCache cache = TypeCacheManager.getInstance().getCache(projects[i]);
//			cache.accept(visitor);
//			if (includeGlobalNamespace) {
//				fTypesFound.add(cache.getGlobalNamespace());
//			}
		}
		return (ITypeInfo[]) fTypesFound.toArray(new ITypeInfo[fTypesFound.size()]);
	}
	
	/** Returns first type in the cache which matches the given
	 *  type and name.  If no type is found, <code>null</code>
	 *  is returned.
	 *
	 * @param project the enclosing project
	 * @param type the ICElement type
	 * @param qualifiedName the qualified type name to match
	 * @return the matching type
	 */
	public static ITypeInfo getType(ICProject project, int type, IQualifiedTypeName qualifiedName) {
//		ITypeCache cache = TypeCacheManager.getInstance().getCache(project);
//		return cache.getType(type, qualifiedName);
		return null;
	}

	/**
	 * Returns all types matching name in the given project.
	 * 
	 * @param project the enclosing project
	 * @param qualifiedName The qualified type name
	 * @param matchEnclosed <code>true</code> if enclosed types count as matches (foo::bar == bar)
	 * @param ignoreCase <code>true</code> if case-insensitive
	 * @return Array of types
	 */
	public static ITypeInfo[] getTypes(ICProject project, IQualifiedTypeName qualifiedName, boolean matchEnclosed, boolean ignoreCase) {
//		ITypeCache cache = TypeCacheManager.getInstance().getCache(project);
//		return cache.getTypes(qualifiedName, matchEnclosed, ignoreCase);
		return new ITypeInfo[0];
	}

}
