/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Andrew Ferguson (Symbian)
 *    Bryan Wilkinson (QNX)
 *    Sergey Prigogin (Google)
 *******************************************************************************/ 
package org.eclipse.cdt.core.index;

import java.util.regex.Pattern;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Interface for accessing the index for one or more projects.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * 
 * @since 4.0
 */
public interface IIndex {
	/**
	 * Constant to specify infinite depth. 
	 * @see #findIncludedBy(IIndexFile, int) 
	 * @see #findIncludes(IIndexFile, int)
	 */
	final int DEPTH_INFINITE = -1;

	/**
	 * Constant to find direct includes, only. 
	 * @see #findIncludedBy(IIndexFile, int) 
	 * @see #findIncludes(IIndexFile, int)
	 */
	final int DEPTH_ZERO = 0;

	/** 
	 * Constant to search for declarations. This does not include definitions.
	 */
	final int FIND_DECLARATIONS = 0x1;
	/** 
	 * Constant to search for definitions. This does not include declarations.
	 */
	final int FIND_DEFINITIONS  = 0x2;
	/** 
	 * Constant to search for references. This does not include declarations or definitions.
	 */
	final int FIND_REFERENCES   = 0x4;
	/**
	 * Constant to search for occurrences across language boundaries. 
	 * You can use it to find the occurrences of a c++-function declared with 'extern "C"' within
	 * the c-linkage.
	 */
	final int SEARCH_ACROSS_LANGUAGE_BOUNDARIES= 0x8;
	/** 
	 * Constant to search for all declarations including definitions.
	 */
	final int FIND_DECLARATIONS_DEFINITIONS = FIND_DECLARATIONS | FIND_DEFINITIONS;
	/** 
	 * Constant to search for all occurrences of a binding. This includes declarations, definitions and references.
	 */
	final int FIND_ALL_OCCURRENCES = FIND_DECLARATIONS | FIND_DEFINITIONS | FIND_REFERENCES;

	/**
	 * Before making calls to an index you have to obtain a lock. The objects 
	 * returned by an index become invalid as soon as the indexer writes to the
	 * index. You may obtain nested read locks. Make sure you release the lock.
	 * @see #getLastWriteAccess()
	 * <pre>
	 * index.acquireReadLock();
	 * try {
	 *    ....
	 * }
	 * finally {
	 *    index.releaseReadLock();
	 * }
	 * </pre>
	 */
	public void acquireReadLock() throws InterruptedException;
	
	/**
	 * Any lock obtained by {@link #acquireReadLock()} must be released.
	 */
	public void releaseReadLock();
	
	/**
	 * @return <code>true</code> if there are threads waiting for read locks.
	 * @since 5.2
	 */
	public boolean hasWaitingReaders();

	/**
	 * Returns a timestamp of when the index was last written to. This can
	 * be used to figure out whether information read from the index is 
	 * still reliable or not.
	 *
	 * <pre>
	 * long timestamp;
	 * IBinding binding= null;
	 * index.acquireReadLock();
	 * try {
	 *    timestamp= index.getLastWriteAccess();
	 *    binding= index.findBinding(...);
	 * }
	 * finally {
	 *    index.releaseReadLock();
	 * }
	 * ...
	 * index.acqureReadLock();
	 * try {
	 *    if (index.getLastWriteAccess() != timestamp) {
	 *       // don't use binding, it's not valid anymore
	 *       binding= index.findBinding(...);
	 *    }
	 *    String name= binding.getName();
	 *    ...
	 * }
	 * finally {
	 *    index.releaseReadLock();
	 * }
	 */
	public long getLastWriteAccess();
	
	/**
	 * Returns the file-object for the given location and linkage or returns 
	 * <code>null</code> if the file was not indexed in this linkage.
	 * @param location an IIndexFileLocation representing the location of the file
	 * @return the file in the index or <code>null</code>
	 * @throws CoreException
	 */
	public IIndexFile getFile(int linkageID, IIndexFileLocation location) throws CoreException;

	/**
	 * Returns the file-objects for the given location in any linkage. 
	 * @param location an IIndexFileLocation representing the location of the file
	 * @return an array of file-objects.
	 * @throws CoreException
	 */
	public IIndexFile[] getFiles(IIndexFileLocation location) throws CoreException;

	/**
	 * Looks for include relations originated by the given file.
	 * This is the same as <pre> findIncludes(file, DEPTH_ZERO); </pre> 
	 * @param file the file containing the include directives
	 * @return an array of include relations
	 * @throws CoreException
	 */
	public IIndexInclude[] findIncludes(IIndexFile file) throws CoreException;

	/**
	 * Looks for include relations pointing to the given file. 
	 * This is the same as <pre> findIncludedBy(file, DEPTH_ZERO); </pre> 
	 * @param file the file included by the directives to be found
	 * @return an array of include relations
	 * @throws CoreException
	 */
	public IIndexInclude[] findIncludedBy(IIndexFile file) throws CoreException;

	/**
	 * Looks recursively for include relations originated by the given file.
	 * @param file the file containing the include directives
	 * @param depth depth to which includes are followed, should be one of 
	 * {@link #DEPTH_ZERO} or {@link #DEPTH_INFINITE}
	 * @return an array of include relations
	 * @throws CoreException
	 */
	public IIndexInclude[] findIncludes(IIndexFile file, int depth) throws CoreException;

	/**
	 * Looks recursively for include relations pointing to the given file.
	 * @param file the file the include directives point to
	 * @param depth depth to which includes are followed, should be one of 
	 * {@link #DEPTH_ZERO} or {@link #DEPTH_INFINITE}
	 * @return an array of include relations
	 * @throws CoreException
	 */
	public IIndexInclude[] findIncludedBy(IIndexFile file, int depth) throws CoreException;

	/**
	 * Resolves the file that is included by the given include directive. May return <code>null</code>
	 * in case the file cannot be found. This is usually more efficient than using:
	 * <pre>
	 * getFile(include.getIncludesLocation())
	 * </pre>
	 * @param include
	 * @return the file included or <code>null</code>.
	 * @throws CoreException
	 * @since 4.0
	 */
	public IIndexFile resolveInclude(IIndexInclude include) throws CoreException;
	
	/**
	 * Searches for all macros with a given name.
	 *
	 * @param name a name, that has to be matched by the macros.
	 * @param filter a filter that allows for skipping parts of the index 
	 * @param monitor a monitor to report progress, may be <code>null</code>.
	 * @return an array of macros matching the name.
	 * @throws CoreException
	 * @since 4.0.2
	 */
	public IIndexMacro[] findMacros(char[] name, IndexFilter filter, IProgressMonitor monitor) throws CoreException;

	/**
	 * Searches for all macros with names that start with the given prefix.
	 * 
	 * @param prefix the prefix with which all returned macros must start
	 * @param filter a filter that allows for skipping parts of the index
	 * @param monitor a monitor for progress reporting and cancellation, may be <code>null</code>
	 * @return an array of bindings with the prefix
	 * @throws CoreException
	 * @since 4.0.2
	 */
	public IIndexMacro[] findMacrosForPrefix(char[] prefix, IndexFilter filter, IProgressMonitor monitor) throws CoreException;

	/**
	 * Searches for the binding of a name. The name may be originated by
	 * an AST or by a search in an index. May return <code>null</code>.
	 * @param name a name to find the binding for
	 * @return the binding or <code>null</code>
	 * @throws CoreException
	 */
	public IIndexBinding findBinding(IName name) throws CoreException;
	
	/**
	 * Searches for all bindings with simple names that match the given pattern. In case a binding exists 
	 * in multiple projects, no duplicate bindings are returned.
	 * This is fully equivalent to 
	 * <pre>
	 * findBindings(new Pattern[]{pattern}, isFullyQualified, filter, monitor);
	 * </pre> 
	 * @param pattern the pattern the name of the binding has to match.
	 * @param isFullyQualified if <code>true</code>, binding must be in global scope
	 * @param filter a filter that allows for skipping parts of the index 
	 * @param monitor a monitor to report progress, may be <code>null</code>.
	 * @return an array of bindings matching the pattern
	 * @throws CoreException
	 */
	public IIndexBinding[] findBindings(Pattern pattern, boolean isFullyQualified, IndexFilter filter, IProgressMonitor monitor) throws CoreException;

	/**
	 * Searches for all bindings with qualified names that seen as an array of simple names match the given array 
	 * of patterns. In case a binding exists in multiple projects, no duplicate bindings are returned.
	 * You can search with an array of patterns that specifies a partial qualification only. 
	 * @param patterns an array of patterns the names of the qualified name of the bindings have to match.
	 * @param isFullyQualified if <code>true</code>, the array of pattern specifies the fully qualified name
	 * @param filter a filter that allows for skipping parts of the index 
	 * @param monitor a monitor to report progress, may be <code>null</code>.
	 * @return an array of bindings matching the pattern
	 * @throws CoreException
	 */
	public IIndexBinding[] findBindings(Pattern[] patterns, boolean isFullyQualified, IndexFilter filter, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Searches for all macro containers (one for macros with the same name) with names that 
	 * match the given pattern. In case a binding exists in multiple projects, no duplicate bindings 
	 * are returned.
	 * @param pattern a pattern the name of the bindings have to match.
	 * @param filter a filter that allows for skipping parts of the index 
	 * @param monitor a monitor to report progress, may be <code>null</code>
	 * @return an array of bindings matching the pattern
	 * @throws CoreException
	 */
	IIndexBinding[] findMacroContainers(Pattern pattern, IndexFilter filter, IProgressMonitor monitor) throws CoreException;

	/**
	 * Searches for all bindings in global scope with a given name. In case a binding exists in multiple projects, no duplicate bindings are returned.
	 * This method makes use of the BTree and is faster than the methods using patterns.
	 * <p>
	 * @param names an array of names, which has to be matched by the qualified name of the bindings.
	 * @param filter a filter that allows for skipping parts of the index 
	 * @param monitor a monitor to report progress, may be <code>null</code>.
	 * @return an array of bindings matching the pattern
	 * @throws CoreException
	 */
	public IIndexBinding[] findBindings(char[][] names, IndexFilter filter, IProgressMonitor monitor) throws CoreException;

	/**
	 * Searches the global scope for all bindings with a given name.
	 * In case a binding exists in multiple projects, no duplicate bindings are returned.
	 * This method makes use of the BTree and is faster than the methods using patterns.
	 *
 	 * This is fully equivalent to 
	 * <pre>
	 * findBindings(name, true, filter, monitor); 
	 * </pre> 
	 * @param name a name, which has to be matched by the qualified name of the bindings.
	 * @param filter a filter that allows for skipping parts of the index 
	 * @param monitor a monitor to report progress, may be <code>null</code>.
	 * @return an array of bindings matching the pattern
	 * @throws CoreException
	 */
	public IIndexBinding[] findBindings(char[] name, IndexFilter filter, IProgressMonitor monitor) throws CoreException;

	/**
	 * Searches the global scope and optionally all other scopes for bindings with a given name.
	 * In case a binding exists in multiple projects, no duplicate bindings are returned.
	 * This method makes use of the BTree and is faster than the methods using patterns.
	 *
	 * @param name a name, which has to be matched by the qualified name of the bindings.
	 * @param fileScopeOnly if true, only bindings at file scope are returned
	 * @param filter a filter that allows for skipping parts of the index 
	 * @param monitor a monitor to report progress, may be <code>null</code>.
	 * @return an array of bindings matching the pattern
	 * @throws CoreException
	 */
	public IIndexBinding[] findBindings(char[] name, boolean fileScopeOnly, IndexFilter filter, IProgressMonitor monitor) throws CoreException;

	/**
	 * Searches for all bindings with names that start with the given prefix.
	 * @param prefix the prefix with which all returned bindings must start
	 * @param fileScopeOnly if true, only bindings at file scope are returned
	 * @param filter a filter that allows for skipping parts of the index
	 * @param monitor a monitor for progress reporting and cancellation, may be <code>null</code>
	 * @return an array of bindings with the prefix
	 * @throws CoreException
	 */
	public IIndexBinding[] findBindingsForPrefix(char[] prefix, boolean fileScopeOnly, IndexFilter filter, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Searches for all names that resolve to the given binding. You can limit the result to references, declarations
	 * or definitions, or a combination of those.
	 * @param binding a binding for which names are searched for
	 * @param flags a combination of {@link #FIND_DECLARATIONS}, {@link #FIND_DEFINITIONS},
	 * {@link #FIND_REFERENCES} and {@link #SEARCH_ACROSS_LANGUAGE_BOUNDARIES}.
	 * @return an array of names
	 * @throws CoreException
	 */
	public IIndexName[] findNames(IBinding binding, int flags) throws CoreException;

	/**
	 * Searches for all references that resolve to the given binding. 
	 * This is fully equivalent to 
	 * <pre>
	 * findNames(binding, IIndex.FIND_REFERENCES);
	 * </pre> 
	 * @param binding a binding for which references are searched for
	 * @return an array of names
	 * @throws CoreException
	 */
	public IIndexName[] findReferences(IBinding binding) throws CoreException;

	/**
	 * Searches for all declarations and definitions that resolve to the given binding. 
	 * This is fully equivalent to 
	 * <pre>
	 * findNames(binding, IIndex.FIND_DECLARATIONS_DEFINITIONS);
	 * </pre> 
	 * @param binding a binding for which declarations are searched for
	 * @return an array of names
	 * @throws CoreException
	 */
	public IIndexName[] findDeclarations(IBinding binding) throws CoreException;

	/**
	 * Searches for all definitions that resolve to the given binding. 
	 * This is fully equivalent to 
	 * <pre>
	 * findNames(binding, IIndex.FIND_DEFINITIONS);
	 * </pre> 
	 * @param binding a binding for which declarations are searched for
	 * @return an array of names
	 * @throws CoreException
	 */
	public IIndexName[] findDefinitions(IBinding binding) throws CoreException;

	/**
	 * Returns an IIndexBinding for this IIndex that is equivalent to the specified binding,
	 * or null if such a binding does not exist in this index. This is useful for adapting
	 * bindings obtained from IIndex objects that might have been created for a different scope
     * or for IBinding objects obtained direct from the AST.
	 * @param binding
	 * @return an IIndexBinding for this IIndex that is equivalent to the specified binding
	 */
	public IIndexBinding adaptBinding(IBinding binding);
	
	/**
	 * Creates a file-set that can be used with this index as long as you hold a read-lock.
	 */
	public IIndexFileSet createFileSet();

	/**
	 * Returns an array of all files that are part of this index. If a file is parsed in two
	 * linkages, or in multiple fragments only one of the files will be returned.
	 */
	public IIndexFile[] getAllFiles() throws CoreException;

	/**
	 * Returns the global inline c++ namespaces.
	 * @throws CoreException 
	 * @since 5.3
	 */
	public IScope[] getInlineNamespaces() throws CoreException;
}
