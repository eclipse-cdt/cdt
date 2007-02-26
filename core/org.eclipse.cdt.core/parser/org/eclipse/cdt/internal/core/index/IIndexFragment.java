/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Bryan Wilkinson (QNX)
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.index;

import java.util.regex.Pattern;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexLinkage;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Interface for the implementation of the actual index storage mechanism.
 * 
 * @since 4.0
 */
public interface IIndexFragment {
	/**
	 * @see IIndex#FIND_DECLARATIONS
	 */
	final int FIND_DECLARATIONS = IIndex.FIND_DECLARATIONS;
	/**
	 * @see IIndex#FIND_DEFINITIONS
	 */
	final int FIND_DEFINITIONS  = IIndex.FIND_DEFINITIONS;
	/**
	 * @see IIndex#FIND_REFERENCES
	 */
	final int FIND_REFERENCES   = IIndex.FIND_REFERENCES;
	/**
	 * @see IIndex#FIND_DECLARATIONS_DEFINITIONS
	 */
	final int FIND_DECLARATIONS_DEFINITIONS = IIndex.FIND_DECLARATIONS_DEFINITIONS;
	/**
	 * @see IIndex#FIND_ALL_OCCURENCES
	 */
	final int FIND_ALL_OCCURENCES = 		  IIndex.FIND_ALL_OCCURENCES;
	
	/**
	 * Returns the file for the given location. May return <code>null</code>, if no such file exists.
	 * This method may only return files that are actually managed by this fragement.
	 * @param location the IIndexFileLocation representing the location of the file
	 * @return the file for the location
	 * @throws CoreException
	 */
	IIndexFragmentFile getFile(IIndexFileLocation location) throws CoreException;

	/**
	 * Returns all include directives that point to the given file. The input file may belong to 
	 * another fragment. All of the include directives returned must belong to files managed by 
	 * this fragment.
	 * @param file a file to search for includes pointing to it
	 * @return an array of inlucde directives managed by this fragment
	 * @throws CoreException
	 */
	IIndexFragmentInclude[] findIncludedBy(IIndexFragmentFile file) throws CoreException;
	
	/**
	 * Looks for a binding matching the given one. May return <code>null</code>, if no
	 * such binding exists. The binding may belong to an AST or another index fragment.
	 * @param binding the binding to look for.
	 * @return the binding, or <code>null</code>
	 * @throws CoreException 
	 */
	IIndexFragmentBinding adaptBinding(IBinding binding) throws CoreException;

	/**
	 * Looks for a proxy binding matching the given one. May return <code>null</code>, if no
	 * such binding exists. The binding may belong to another index fragment.
	 * @param proxy the binding to look for.
	 * @return the binding, or <code>null</code>
	 * @throws CoreException 
	 */
	IIndexFragmentBinding adaptBinding(IIndexFragmentBinding proxy) throws CoreException;

	/**
	 * Looks for a binding of the given name from the AST. May return <code>null</code>, if no
	 * such binding exists.
	 * @param astName the name for looking up the binding
	 * @return the binding for the name, or <code>null</code>
	 * @throws CoreException
	 */
	IIndexFragmentBinding findBinding(IASTName astName) throws CoreException;
	
	/**
	 * Searches for all bindings with qualified names that seen as an array of simple names match the given array 
	 * of patterns. In case a binding exists in multiple projects, no duplicate bindings are returned.
	 * You can search with an array of patterns that specifies a partial qualification only. 
	 * @param patterns an array of patterns the names of the qualified name of the bindings have to match.
	 * @param isFullyQualified if <code>true</code>, the array of pattern specifies the fully qualified name
	 * @param filter a filter that allows for skipping parts of the index 
	 * @param monitor a monitor to report progress
	 * @return an array of bindings matching the pattern
	 * @throws CoreException
	 */
	IIndexFragmentBinding[] findBindings(Pattern[] patterns, boolean isFullyQualified, IndexFilter filter, IProgressMonitor monitor) throws CoreException;

	/**
	 * Searches for all bindings with qualified names that seen as an array of simple names equals
	 * the given array of names. 
	 * @param names an array of names the qualified name of the bindings have to match.
	 * @param filter a filter that allows for skipping parts of the index 
	 * @param monitor a monitor to report progress
	 * @return an array of bindings matching the pattern
	 * @throws CoreException
	 */
	IIndexFragmentBinding[] findBindings(char[][] names, IndexFilter filter, IProgressMonitor monitor) throws CoreException;

	/**
	 * Searches for all names that resolve to the given binding. You can limit the result to references, declarations
	 * or definitions, or a combination of those.
	 * @param binding a binding for which names are searched for
	 * @param flags a combination of {@link #FIND_DECLARATIONS}, {@link #FIND_DEFINITIONS} and {@link #FIND_REFERENCES}
	 * @return an array of names
	 * @throws CoreException
	 */
	IIndexFragmentName[] findNames(IIndexFragmentBinding binding, int flags) throws CoreException;
	
	/**
	 * Acquires a read lock.
	 * @throws InterruptedException
	 */
	void acquireReadLock() throws InterruptedException;

	/**
	 * Releases a read lock.
	 */
	void releaseReadLock();
	
	/**
	 * Returns the timestamp of the last modification to the index.
	 */
	long getLastWriteAccess();

	/**
	 * Returns all bindings with the given prefix, accepted by the given filter
	 */
	IIndexFragmentBinding[] findBindingsForPrefix(char[] prefix, boolean filescope, IndexFilter filter) throws CoreException;
	
	/**
	 * Returns the linkages that are contained in this fragment
	 * @return
	 */
	IIndexLinkage[] getLinkages();
}
