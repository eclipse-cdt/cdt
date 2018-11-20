/*******************************************************************************
 * Copyright (c) 2006, 2014 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Andrew Ferguson (Symbian)
 *     Bryan Wilkinson (QNX)
 *     Sergey Prigogin (Google)
 *     Jens Elmenthaler - http://bugs.eclipse.org/173458 (camel case completion)
 *******************************************************************************/
package org.eclipse.cdt.core.index;

import java.util.regex.Pattern;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.parser.ISignificantMacros;
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
	final int FIND_DEFINITIONS = 0x2;
	/**
	 * Constant to search for references. This does not include declarations or definitions.
	 */
	final int FIND_REFERENCES = 0x4;
	/**
	 * Constant to search for occurrences across language boundaries.
	 * Can be used to find the occurrences of a C++-function declared with 'extern "C"' within
	 * the c-linkage.
	 */
	final int SEARCH_ACROSS_LANGUAGE_BOUNDARIES = 0x8;
	/**
	 * Constant to include potential matches in the results of a search.
	 * An example of a potential match might be a function definition that does match
	 * a declaration exactly in signature.
	 * @since 6.5
	 */
	final int FIND_POTENTIAL_MATCHES = 0x10;
	/**
	 * Constant to search for all declarations including definitions.
	 */
	final int FIND_DECLARATIONS_DEFINITIONS = FIND_DECLARATIONS | FIND_DEFINITIONS;
	/**
	 * Constant to search for all occurrences of a binding. This includes declarations, definitions
	 * and references.
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
	 * } finally {
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
	 * @return {@code true} if there are threads waiting for read locks.
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
	 * } finally {
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
	 * } finally {
	 *    index.releaseReadLock();
	 * }
	 */
	public long getLastWriteAccess();

	/**
	 * @deprecated Use {@link #getFile(int, IIndexFileLocation, ISignificantMacros)} or
	 *     {@link #getFiles(int, IIndexFileLocation)}.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public IIndexFile getFile(int linkageID, IIndexFileLocation location) throws CoreException;

	/**
	 * Returns the file for the given location, linkage, and significant macros
	 * May return {@code null}, if no such file exists.
	 *
	 * @param linkageID the id of the linkage in which the file has been parsed.
	 * @param location the IIndexFileLocation representing the location of the file
	 * @param macroDictionary The names and definitions of the macros used to disambiguate between
	 *     variants of the file contents corresponding to different inclusion points.
	 * @return the file for the location, or {@code null} if the file is not present in
	 *     the index
	 * @throws CoreException
	 * @since 5.4
	 */
	IIndexFile getFile(int linkageID, IIndexFileLocation location, ISignificantMacros significantMacros)
			throws CoreException;

	/**
	 * Returns the file objects for the given location and linkage.
	 * Multiple files are returned when a header file is stored in the index in multiple variants
	 * for different sets of macro definitions.
	 * This method may only return files that are actually managed by this fragment.
	 * This method returns files without content, also.
	 *
	 * @param linkageID the id of the linkage in which the file has been parsed.
	 * @param location the IIndexFileLocation representing the location of the file
	 * @return the files for the location and the linkage.
	 * @throws CoreException
	 * @since 5.4
	 */
	IIndexFile[] getFiles(int linkageID, IIndexFileLocation location) throws CoreException;

	/**
	 * Returns the file objects for the given location in any linkage.
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
	 * Resolves the file that is included by the given include directive. May return {@code null}
	 * in case the file cannot be found. This is usually more efficient than using:
	 * <pre>
	 * getFiles(include.getIncludesLocation())
	 * </pre>
	 * @param include
	 * @return the file included or {@code null}.
	 * @throws CoreException
	 * @since 4.0
	 */
	public IIndexFile resolveInclude(IIndexInclude include) throws CoreException;

	/**
	 * Searches for all macros with a given name.
	 *
	 * @param name a name, that has to be matched by the macros.
	 * @param filter a filter that allows for skipping parts of the index
	 * @param monitor a monitor to report progress, may be {@code null}.
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
	 * @param monitor a monitor for progress reporting and cancellation, may be {@code null}
	 * @return an array of bindings with the prefix
	 * @throws CoreException
	 * @since 4.0.2
	 */
	public IIndexMacro[] findMacrosForPrefix(char[] prefix, IndexFilter filter, IProgressMonitor monitor)
			throws CoreException;

	/**
	 * Searches for the binding of a name. The name may be originated by
	 * an AST or by a search in an index. May return {@code null}.
	 *
	 * @param name a name to find the binding for
	 * @return the binding or {@code null}
	 * @throws CoreException
	 */
	public IIndexBinding findBinding(IName name) throws CoreException;

	/**
	 * Searches for all bindings with simple names that match the given pattern. In case a binding
	 * exists in multiple projects, no duplicate bindings are returned.
	 * This is fully equivalent to
	 * <pre>
	 * findBindings(new Pattern[] {pattern}, isFullyQualified, filter, monitor);
	 * </pre>
	 * @param pattern the pattern the name of the binding has to match.
	 * @param isFullyQualified if {@code true}, binding must be in global scope
	 * @param filter a filter that allows for skipping parts of the index
	 * @param monitor a monitor to report progress, may be {@code null}.
	 * @return an array of bindings matching the pattern
	 * @throws CoreException
	 */
	public IIndexBinding[] findBindings(Pattern pattern, boolean isFullyQualified, IndexFilter filter,
			IProgressMonitor monitor) throws CoreException;

	/**
	 * Searches for all bindings with qualified names that seen as an array of simple names match
	 * the given array of patterns. In case a binding exists in multiple projects, no duplicate
	 * bindings are returned. You can search with an array of patterns that specifies a partial
	 * qualification only.
	 *
	 * @param patterns an array of patterns the components of the qualified name of the bindings
	 *     have to match.
	 * @param isFullyQualified if {@code true}, the array of pattern specifies the fully
	 *     qualified name
	 * @param filter a filter that allows for skipping parts of the index
	 * @param monitor a monitor to report progress, may be {@code null}.
	 * @return an array of bindings matching the pattern
	 * @throws CoreException
	 */
	public IIndexBinding[] findBindings(Pattern[] patterns, boolean isFullyQualified, IndexFilter filter,
			IProgressMonitor monitor) throws CoreException;

	/**
	 * Searches for all macro containers (one for macros with the same name) with names that
	 * match the given pattern. In case a binding exists in multiple projects, no duplicate
	 * bindings are returned.
	 *
	 * @param pattern a pattern the name of the bindings have to match.
	 * @param filter a filter that allows for skipping parts of the index
	 * @param monitor a monitor to report progress, may be {@code null}
	 * @return an array of bindings matching the pattern
	 * @throws CoreException
	 */
	IIndexBinding[] findMacroContainers(Pattern pattern, IndexFilter filter, IProgressMonitor monitor)
			throws CoreException;

	/**
	 * Searches for all bindings in global scope with a given name. In case a binding exists in
	 * multiple projects, no duplicate bindings are returned. This method makes use of the BTree
	 * and is faster than the methods using patterns.
	 *
	 * @param names an array of names, which has to be matched by the qualified name of
	 *     the bindings.
	 * @param filter a filter that allows for skipping parts of the index
	 * @param monitor a monitor to report progress, may be {@code null}.
	 * @return an array of bindings matching the pattern
	 * @throws CoreException
	 */
	public IIndexBinding[] findBindings(char[][] names, IndexFilter filter, IProgressMonitor monitor)
			throws CoreException;

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
	 * @param monitor a monitor to report progress, may be {@code null}.
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
	 * @param monitor a monitor to report progress, may be {@code null}.
	 * @return an array of bindings matching the pattern
	 * @throws CoreException
	 */
	public IIndexBinding[] findBindings(char[] name, boolean fileScopeOnly, IndexFilter filter,
			IProgressMonitor monitor) throws CoreException;

	/**
	 * Searches for all bindings with names that start with the given prefix.
	 *
	 * @param prefix the prefix with which all returned bindings must start
	 * @param fileScopeOnly if true, only bindings at file scope are returned
	 * @param filter a filter that allows for skipping parts of the index
	 * @param monitor a monitor for progress reporting and cancellation, may be {@code null}
	 * @return an array of bindings with the prefix
	 * @throws CoreException
	 */
	public IIndexBinding[] findBindingsForPrefix(char[] prefix, boolean fileScopeOnly, IndexFilter filter,
			IProgressMonitor monitor) throws CoreException;

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public IIndexBinding[] findBindingsForContentAssist(char[] prefix, boolean fileScopeOnly, IndexFilter filter,
			IProgressMonitor monitor) throws CoreException;

	/**
	 * Searches for all names that resolve to the given binding. The search can be limited to
	 * references, declarations or definitions, or a combination of those.
	 *
	 * @param binding a binding for which names are searched for
	 * @param flags a combination of {@link #FIND_DECLARATIONS}, {@link #FIND_DEFINITIONS},
	 * {@link #FIND_REFERENCES}, {@link #SEARCH_ACROSS_LANGUAGE_BOUNDARIES}, and
	 * {@link #FIND_POTENTIAL_MATCHES}.
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
	 * Returns an {@link IIndexBinding} for this index that is equivalent to the specified binding,
	 * or null if such a binding does not exist in this index. This is useful for adapting
	 * bindings obtained from IIndex objects that might have been created for a different scope
	 * or for IBinding objects obtained directly from the AST.
	 *
	 * @param binding an AST or an index binding
	 * @return an IIndexBinding for this index that is equivalent to the specified binding
	 */
	public IIndexBinding adaptBinding(IBinding binding);

	/**
	 * Creates a file-set that can be used with this index as long as the caller holds a read-lock.
	 */
	public IIndexFileSet createFileSet();

	/**
	 * Returns an array of all files that are part of this index. If a file is parsed in two
	 * linkages or in multiple fragments, only one of the files will be returned.
	 */
	public IIndexFile[] getAllFiles() throws CoreException;

	/**
	 * Returns an array of files that were indexed with I/O errors.
	 * @noreference This method is not intended to be referenced by clients.
	 * @since 5.4
	 */
	public IIndexFile[] getDefectiveFiles() throws CoreException;

	/**
	 * Returns an array of files containing unresolved includes.
	 * @noreference This method is not intended to be referenced by clients.
	 * @since 5.4
	 */
	public IIndexFile[] getFilesWithUnresolvedIncludes() throws CoreException;

	/**
	 * Returns the global inline c++ namespaces.
	 * @throws CoreException
	 * @since 5.3
	 */
	public IScope[] getInlineNamespaces() throws CoreException;

	/**
	 * Returns {@code true} if the index is fully initialized. An index may not be fully initialized
	 * during Eclipse startup, or soon after adding a new project to the workspace.
	 * @since 5.4
	 */
	public boolean isFullyInitialized();
}
