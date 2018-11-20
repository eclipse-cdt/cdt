/*******************************************************************************
 * Copyright (c) 2004, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Andrew Ferguson (Symbian)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.browser;

import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.browser.IndexModelUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Manages a search cache for types in the workspace. Instead of returning
 * objects of type <code>ICElement</code> the methods of this class returns a
 * list of the lightweight objects <code>ITypeInfo</code>.
 * <P>
 * AllTypesCache runs asynchronously using a background job to rebuild the cache
 * as needed. If the cache becomes dirty again while the background job is
 * running, the job is restarted.
 * <P>
 * If <code>getTypes</code> is called in response to a user action, a progress
 * dialog is shown. If called before the background job has finished, getTypes
 * waits for the completion of the background job.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class AllTypesCache {
	private static final boolean DEBUG = false;

	private static ITypeInfo[] getTypes(ICProject[] projects, final int[] kinds, IProgressMonitor monitor)
			throws CoreException {
		IIndex index = CCorePlugin.getIndexManager().getIndex(projects,
				IIndexManager.ADD_EXTENSION_FRAGMENTS_NAVIGATION);

		try {
			index.acquireReadLock();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return new ITypeInfo[0];
		}

		try {
			long start = System.currentTimeMillis();

			IIndexBinding[] all = index.findBindings(Pattern.compile(".*"), false, new IndexFilter() { //$NON-NLS-1$
				@Override
				public boolean acceptBinding(IBinding binding) throws CoreException {
					return IndexFilter.ALL_DECLARED_OR_IMPLICIT.acceptBinding(binding)
							&& IndexModelUtil.bindingHasCElementType(binding, kinds);
				}
			}, monitor);

			if (DEBUG) {
				System.out.println("Index search took " + (System.currentTimeMillis() - start)); //$NON-NLS-1$
				start = System.currentTimeMillis();
			}

			ITypeInfo[] result = new ITypeInfo[all.length];
			for (int i = 0; i < all.length; i++) {
				result[i] = IndexTypeInfo.create(index, all[i]);
			}

			if (DEBUG) {
				System.out.println("Wrapping as ITypeInfo took " + (System.currentTimeMillis() - start)); //$NON-NLS-1$
				start = System.currentTimeMillis();
			}

			return result;
		} finally {
			index.releaseReadLock();
		}
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
			return getTypes(scope.getEnclosingProjects(), new int[] { ICElement.C_NAMESPACE },
					new NullProgressMonitor());
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new ITypeInfo[0];
		}
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @deprecated never worked.
	 */
	@Deprecated
	public static ITypeInfo getType(ICProject project, int type, IQualifiedTypeName qualifiedName) {
		return null;
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @deprecated never worked.
	 */
	@Deprecated
	public static ITypeInfo[] getTypes(ICProject project, IQualifiedTypeName qualifiedName, boolean matchEnclosed,
			boolean ignoreCase) {
		return new ITypeInfo[0];
	}

}
