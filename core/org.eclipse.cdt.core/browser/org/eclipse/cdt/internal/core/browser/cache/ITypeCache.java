/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.browser.cache;

import org.eclipse.cdt.core.browser.IQualifiedTypeName;
import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.ITypeInfoVisitor;
import org.eclipse.cdt.core.browser.ITypeReference;
import org.eclipse.cdt.core.browser.ITypeSearchScope;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

public interface ITypeCache extends ISchedulingRule {
	
	/** Returns whether cache contains any types.
	 * 
	 * @return <code>true</code> if cache is empty
	 */
	public boolean isEmpty();
	
	/** Returns whether cache is complete.
	 * 
	 * @return <code>true</code> if cache is up to date.
	 */
	public boolean isUpToDate();

	/** Inserts type into cache.
	 * 
	 * @param info
	 */
	public void insert(ITypeInfo info);

	/** Adds subtype to type.
	 * 
	 * @param type
	 * @param subtype
	 */
	public void addSubtype(ITypeInfo type, ITypeInfo subtype);

	/** Adds supertype to type.
	 * 
	 * @param type the type
	 * @param supertype the supertype
	 * @param the access visibility (PUBLIC, PROTECTED, PRIVATE)
	 * @param isVirtual <code>true</code> if virtual base class
	 */
	public void addSupertype(ITypeInfo type, ITypeInfo supertype, ASTAccessVisibility access, boolean isVirtual);

	/** Removes type from cache.
	 * 
	 * @param info
	 */
	public void remove(ITypeInfo info);
	
	/** Removes all types in the given scope.
	 * 
	 * @param scope
	 */
	public void flush(ITypeSearchScope scope);

	/** Removes all types referenced by the given path.
	 * 
	 * @param path
	 */
	public void flush(IPath path);

	/** Removes all types from the cache.
	 */
	public void flushAll();
	
	/** Returns all paths in the cache which are enclosed by
	 *  the given scope.  If no paths are found, an empty
	 *  array is returned.
	 * 
	 * @param scope the scope to search, or <code>null</code> to
	 * search the entire cache.
	 * @return A collection of paths in the given scope.
	 */
	public IPath[] getPaths(ITypeSearchScope scope);

	/** Returns all types in the cache which are enclosed by
	 *  the given scope.  If no types are found, an empty array
	 *  is returned.
	 * 
	 * @param scope the scope to search, or <code>null</code> to
	 * search the entire cache.
	 * @return Array of types in the given scope
	 */
	public ITypeInfo[] getTypes(ITypeSearchScope scope);
	
	/** Returns all types in the cache which match the given
	 *  name.  If no types are found, an empty array is returned.
	 *
	 * @param qualifiedName the qualified type name to match
	 * @param matchEnclosed <code>true</code> if enclosed types count as matches (foo::bar == bar)
	 * @param ignoreCase <code>true</code> if case-insensitive
	 * @return Array of types
	 */
	public ITypeInfo[] getTypes(IQualifiedTypeName qualifiedName, boolean matchEnclosed, boolean ignoreCase);

	/** Returns first type in the cache which matches the given
	 *  type and name.  If no type is found, <code>null</code>
	 *  is returned.
	 *
	 * @param type the ICElement type
	 * @param qualifiedName the qualified type name to match
	 * @return the matching type
	 */
	public ITypeInfo getType(int type, IQualifiedTypeName qualifiedName);

	/** Gets the first enclosing type which matches one of the given kinds.
	 * 
	 * @param info the given type
	 * @param kinds Array containing CElement types: C_NAMESPACE, C_CLASS, C_STRUCT
	 *
	 * @return the enclosing type, or <code>null</code> if not found.
	 */
	public ITypeInfo getEnclosingType(ITypeInfo info, int[] kinds);
	
	/** Returns the enclosing namespace for the given type, or
	 *  <code>null</code> if none exists.
	 *
	 * @param type the ICElement type
	 * @param includeGlobalNamespace <code>true</code> if the global (default) namespace should be returned
	 * @return the enclosing namespace, or <code>null</code> if not found.
	 */
	public ITypeInfo getEnclosingNamespace(ITypeInfo info, boolean includeGlobalNamespace);

	/** Gets the root namespace of which encloses the given type.
	 *
	 * @param info the given type
	 * @param includeGlobalNamespace <code>true</code> if the global (default) namespace should be returned
	 * @return the enclosing namespace, or <code>null</code> if not found.
	 */
	public ITypeInfo getRootNamespace(ITypeInfo info, boolean includeGlobalNamespace);
	
	/** Returns whether any types are enclosed by the given type.
	 *
	 * @param info the given type
	 * @return <code>true</code> if the given type encloses other types.
	 */
	public boolean hasEnclosedTypes(ITypeInfo info);

	/** Gets the types which are enclosed by the given type.
	 *
	 * @param info the given type
	 * @param kinds Array containing CElement types: C_NAMESPACE, C_CLASS,
	 *              C_UNION, C_ENUMERATION, C_TYPEDEF
	 * @return the enclosed types, or an empty array if not found.
	 */
	public ITypeInfo[] getEnclosedTypes(ITypeInfo info, int kinds[]);
	
	/** Returns all types in the cache are supertypes of the given type.
	 * 
	 * @param info the given type
	 * @return Array of supertypes, or <code>null</code> if none found.
	 */
	public ITypeInfo[] getSupertypes(ITypeInfo info);
	
	/** Returns the supertype access visiblity.
	 * 
	 * @param type the given type
	 * @param supertype the supertype
	 * @return the visibility (PRIVATE, PROTECTED, PUBLIC) or <code>null</code> if none found.
	 */
	public ASTAccessVisibility getSupertypeAccess(ITypeInfo type, ITypeInfo superType);
	

	/** Returns all types in the cache which extend the given type.
	 * 
	 * @param info the given type
	 * @return Array of subtypes, or <code>null</code> if none found.
	 */
	public ITypeInfo[] getSubtypes(ITypeInfo info);

	/** Returns the project associated with this cache.
	 * 
	 * @return the project
	 */
	public IProject getProject();
	
	/** Accepts a visitor and iterates over all types in the cache.
	 * 
	 * @param visitor
	 */
	public void accept(ITypeInfoVisitor visitor);
	
	public void addDelta(TypeCacheDelta delta);
	public void reconcile(boolean enableIndexing, int priority, int delay);
	public void reconcileAndWait(boolean enableIndexing, int priority, IProgressMonitor monitor);
	public void cancelJobs();

	public void locateType(ITypeInfo info, int priority, int delay);
	public ITypeReference locateTypeAndWait(ITypeInfo info, int priority, IProgressMonitor monitor);
	
	public void locateSupertypes(ITypeInfo info, int priority, int delay);
	public ITypeInfo[] locateSupertypesAndWait(ITypeInfo info, int priority, IProgressMonitor monitor);

	public void locateSubtypes(ITypeInfo info, int priority, int delay);
	public ITypeInfo[] locateSubtypesAndWait(ITypeInfo info, int priority, IProgressMonitor monitor);

	public ITypeInfo getGlobalNamespace();
}
