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
	 * @return Array of types
	 */
	public ITypeInfo[] getTypes(IQualifiedTypeName qualifiedName);

	/** Returns first type in the cache which matches the given
	 *  type and name.  If no type is found, <code>null</code>
	 *  is returned.
	 *
	 * @param type the ICElement type
	 * @param qualifiedName the qualified type name to match
	 * @return the matching type
	 */
	public ITypeInfo getType(int type, IQualifiedTypeName qualifiedName);

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
	
}
