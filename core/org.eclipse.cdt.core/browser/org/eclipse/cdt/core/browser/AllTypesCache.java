/*******************************************************************************
 * Copyright (c) 2004, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.core.browser;

import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.browser.util.IndexModelUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

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
	private static final boolean DEBUG = false;
	
	private static ITypeInfo[] getTypes(ICProject[] projects, final int[] kinds, IProgressMonitor monitor) throws CoreException {
		IIndex index = CCorePlugin.getIndexManager().getIndex(projects);
		
		try {
			index.acquireReadLock();
			
			long start = System.currentTimeMillis();
			
			IIndexBinding[] all =
				index.findBindings(Pattern.compile(".*"), false, new IndexFilter() { //$NON-NLS-1$
					public boolean acceptBinding(IBinding binding) {
						return IndexModelUtil.bindingHasCElementType(binding, kinds);
					}},
					monitor
				);
			
			if(DEBUG) {
				System.out.println("Index search took "+(System.currentTimeMillis() - start)); //$NON-NLS-1$
				start = System.currentTimeMillis();
			}
			
			ITypeInfo[] result = new ITypeInfo[all.length];
			for(int i=0; i<all.length; i++) {
				IIndexBinding ib = (IIndexBinding) all[i];				
				result[i] = new IndexTypeInfo(ib.getQualifiedName(), IndexModelUtil.getElementType(ib), index);
			}

			if(DEBUG) {
				System.out.println("Wrapping as ITypeInfo took "+(System.currentTimeMillis() - start)); //$NON-NLS-1$
				start = System.currentTimeMillis();
			}
			
			return result;
		} catch(InterruptedException ie) {
			ie.printStackTrace();
		} finally {
			index.releaseReadLock();
		}
		return new ITypeInfo[0];
	}

	/**
	 * Returns all types in the workspace.
	 */
	public static ITypeInfo[] getAllTypes() {
		return getAllTypes(new NullProgressMonitor());
	}
	
	/**
	 * Returns all types in the workspace.
	 */
	public static ITypeInfo[] getAllTypes(IProgressMonitor monitor) {
		try {
			ICProject[] projects = CoreModel.getDefault().getCModel().getCProjects();
			return getTypes(projects, ITypeInfo.KNOWN_TYPES, monitor);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new ITypeInfo[0];
		}
	}

	/**
	 * Returns all types in the given scope.
	 * 
	 * @param scope The search scope
	 * @param kinds Array containing CElement types: C_NAMESPACE, C_CLASS,
	 *              C_UNION, C_ENUMERATION, C_TYPEDEF
	 */
	public static ITypeInfo[] getTypes(ITypeSearchScope scope, int[] kinds) {
		try {
			return getTypes(scope.getEnclosingProjects(), kinds, new NullProgressMonitor());
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new ITypeInfo[0];
		}
	}

	/**
	 * Returns all namespaces in the given scope.
	 * 
	 * @param scope The search scope
	 * @param includeGlobalNamespace <code>true</code> if the global (default) namespace should be returned
	 */
	public static ITypeInfo[] getNamespaces(ITypeSearchScope scope, boolean includeGlobalNamespace) {
		try {
			return getTypes(scope.getEnclosingProjects(), new int[] {ICElement.C_NAMESPACE}, new NullProgressMonitor());
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new ITypeInfo[0];
		}
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
		// TODO - seems to be only used when a namespace name is changed
		// which would be pretty slow against the PDOM.
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
		// TODO - seems to be only used when a class or namespace name is changed
		// which would be pretty slow against the PDOM.
		return new ITypeInfo[0];
	}

}
