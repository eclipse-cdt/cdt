/*******************************************************************************
 * Copyright (c) 2006, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Markus Schorn - initial API and implementation
 *      Bryan Wilkinson (QNX)
 *      Andrew Ferguson (Symbian)
 *      Sergey Prigogin (Google)
 *      Jens Elmenthaler - http://bugs.eclipse.org/173458 (camel case completion)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

import java.util.regex.Pattern;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexLinkage;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.parser.ISignificantMacros;
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
	 * @see IIndex#SEARCH_ACROSS_LANGUAGE_BOUNDARIES
	 */
	final int SEARCH_ACROSS_LANGUAGE_BOUNDARIES = IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES;
	/**
	 * @see IIndex#FIND_DECLARATIONS_DEFINITIONS
	 */
	final int FIND_DECLARATIONS_DEFINITIONS = IIndex.FIND_DECLARATIONS_DEFINITIONS;
	/**
	 * @see IIndex#FIND_ALL_OCCURRENCES
	 */
	final int FIND_ALL_OCCURRENCES = IIndex.FIND_ALL_OCCURRENCES;

	final int FIND_NON_LOCAL_ONLY= 0x10000;
	/**
	 * Property key for the fragment ID. The fragment ID should uniquely identify the fragments
	 * usage within a logical index.
	 * @since 4.0
	 */
	public static final String PROPERTY_FRAGMENT_ID= "org.eclipse.cdt.internal.core.index.fragment.id"; //$NON-NLS-1$

	/**
	 * Property key for the fragment format ID. The fragment format ID should uniquely identify
	 * a format of an index fragment up to version information. That is, as a particular format
	 * changes version its ID should remain the same.
	 * @since 4.0.1
	 */
	public static final String PROPERTY_FRAGMENT_FORMAT_ID= "org.eclipse.cdt.internal.core.index.fragment.format.id"; //$NON-NLS-1$

	/**
	 * Property key for the fragment format's version. Version numbers belonging to the same format
	 * (identified by format ID) should be comparable with each other. The version scheme exposed
	 * should be compatible with the OSGi framework's version scheme - i.e. it should be
	 * successfully parsed by org.osgi.framework.Version.parseVersion(String). A null value
	 * for this property is interpreted as Version(0.0.0)
	 * @since 4.0.1
	 */
	public static final String PROPERTY_FRAGMENT_FORMAT_VERSION= "org.eclipse.cdt.internal.core.index.fragment.format.version"; //$NON-NLS-1$

	/**
	 * Property key for storing whether indexer has to resume or not.
	 */
	public static final String PROPERTY_RESUME_INDEXER= "org.eclipse.cdt.internal.core.index.resume"; //$NON-NLS-1$

	/**
	 * Returns the file for the given location and linkage.
	 * May return <code>null</code>, if no such file exists.
	 * This method may only return files that are actually managed by this fragment.
	 * This method returns files without content, also.
	 * <p>
	 * When a header file is stored in the index in multiple variants for different sets of macro
	 * definitions, this method will return an arbitrary one of these variants.
	 *  
	 * @param linkageID the id of the linkage in which the file has been parsed.
	 * @param location the IIndexFileLocation representing the location of the file
	 * @return the file for the location, or <code>null</code> if the file is not present in
	 *     the index
	 * @throws CoreException
	 * @deprecated Use {@link #getFile(int, IIndexFileLocation, ISignificantMacros)} or
	 *     {@link #getFiles(int, IIndexFileLocation)}.
	 */
	@Deprecated
	IIndexFragmentFile getFile(int linkageID, IIndexFileLocation location) throws CoreException;

	/**
	 * Returns the file for the given location, linkage, and a set of macro definitions.
	 * May return <code>null</code>, if no such file exists.
	 * This method may only return files that are actually managed by this fragment.
	 * This method returns files without content, also.
	 *  
	 * @param linkageID the id of the linkage in which the file has been parsed.
	 * @param location the IIndexFileLocation representing the location of the file
	 * @param macroDictionary The names and definitions of the macros used to disambiguate between
	 *     variants of the file contents corresponding to different inclusion points.
	 * @return the file for the location, or <code>null</code> if the file is not present in
	 *     the index
	 * @throws CoreException
	 */
	IIndexFragmentFile getFile(int linkageID, IIndexFileLocation location,
			ISignificantMacros significantMacros) throws CoreException;

	/**
	 * Returns the files for the given location and linkage.
	 * Multiple files are returned when a header file is stored in the index in multiple variants
	 * for different sets of macro definitions.
	 * This method may only return files that are actually managed by this fragment.
	 * This method returns files without content, also.
	 * <p>
	 * When a header file is stored in the index in multiple variants for different sets of macro
	 * definitions, this method will return an arbitrary one of these variants.
	 *  
	 * @param linkageID the id of the linkage in which the file has been parsed.
	 * @param location the IIndexFileLocation representing the location of the file
	 * @return the files for the location and the linkage.
	 * @throws CoreException
	 */
	IIndexFragmentFile[] getFiles(int linkageID, IIndexFileLocation location) throws CoreException;

	/**
	 * Returns the files in all linkages for the given location.
	 * This method may only return files that are actually managed by this fragment.
	 * @param location the IIndexFileLocation representing the location of the file
	 * @return the file for the location, or <code>null</code> if the file is not present in
	 *     the index
	 * @throws CoreException
	 */
	IIndexFragmentFile[] getFiles(IIndexFileLocation location) throws CoreException;

	/**
	 * Returns all include directives that point to the given file. The input file may belong to
	 * another fragment. All of the include directives returned must belong to files managed by
	 * this fragment.
	 * @param file a file to search for includes pointing to it
	 * @return an array of include directives managed by this fragment
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
	 * Looks for a binding of the given name from the AST. May return <code>null</code>, if no
	 * such binding exists.
	 * @param astName the name for looking up the binding
	 * @return the binding for the name, or <code>null</code>
	 * @throws CoreException
	 */
	IIndexFragmentBinding findBinding(IASTName astName) throws CoreException;

	/**
	 * Searches for all bindings with qualified names that seen as an array of simple names match
	 * the given array of patterns. In case a binding exists in multiple projects, no duplicate
	 * bindings are returned.
	 * You can search with an array of patterns that specifies a partial qualification only.
	 * @param patterns an array of patterns the names of the qualified name of the bindings have
	 *     to match.
	 * @param isFullyQualified if <code>true</code>, the array of pattern specifies the fully
	 *     qualified name
	 * @param filter a filter that allows for skipping parts of the index
	 * @param monitor a monitor to report progress, may be <code>null</code>
	 * @return an array of bindings matching the pattern
	 * @throws CoreException
	 */
	IIndexFragmentBinding[] findBindings(Pattern[] patterns, boolean isFullyQualified, IndexFilter filter, IProgressMonitor monitor) throws CoreException;

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
	IIndexFragmentBinding[] findMacroContainers(Pattern pattern, IndexFilter filter, IProgressMonitor monitor) throws CoreException;

	/**
	 * Searches for all bindings with qualified names that seen as an array of simple names equals
	 * the given array of names.
	 * @param names an array of names the qualified name of the bindings have to match.
	 * @param filter a filter that allows for skipping parts of the index
	 * @param monitor a monitor to report progress, may be <code>null</code>
	 * @return an array of bindings matching the pattern
	 * @throws CoreException
	 */
	IIndexFragmentBinding[] findBindings(char[][] names, IndexFilter filter, IProgressMonitor monitor) throws CoreException;

	/**
	 * Searches for all names that resolve to the given binding. You can limit the result to
	 * references, declarations or definitions, or a combination of those.
	 * @param binding a binding for which names are searched for
	 * @param flags a combination of {@link #FIND_DECLARATIONS}, {@link #FIND_DEFINITIONS}, 
	 *     {@link #FIND_REFERENCES} and {@link #FIND_NON_LOCAL_ONLY}
	 * @return an array of names
	 * @throws CoreException
	 */
	IIndexFragmentName[] findNames(IBinding binding, int flags) throws CoreException;

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
	 * @return <code>true</code> if there are threads waiting for read locks.
	 */
	public boolean hasWaitingReaders();

	/**
	 * Returns the timestamp of the last modification to the index.
	 */
	long getLastWriteAccess();

	/**
	 * Returns all bindings with the given name, accepted by the given filter
	 * @param monitor to report progress, may be <code>null</code>
	 */
	IIndexFragmentBinding[] findBindings(char[] name, boolean filescope, IndexFilter filter, IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns all bindings with the given prefix, accepted by the given filter
	 * @param monitor to report progress, may be <code>null</code>
	 */
	IIndexFragmentBinding[] findBindingsForPrefix(char[] prefix, boolean filescope, IndexFilter filter, IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns all bindings that would be a valid completion for the given text.
	 * @param monitor to report progress, may be <code>null</code>
	 */
	IIndexFragmentBinding[] findBindingsForContentAssist(char[] prefix, boolean filescope, IndexFilter filter, IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns all macros with the given prefix or name, accepted by the given filter
	 * @param monitor to report progress, may be <code>null</code>
	 */
	IIndexMacro[] findMacros(char[] name, boolean isPrefix, boolean caseSensitive, IndexFilter filter, IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns the linkages that are contained in this fragment
	 */
	IIndexLinkage[] getLinkages();

	/**
	 * Read the named property in this fragment. All fragments are expected to return a non-null
	 * value for
	 *    <ul>
	 *    <li>PROPERTY_FRAGMENT_ID</li>
	 *    <li>PROPERTY_FRAGMENT_FORMAT_ID</li>
	 *    <li>PROPERTY_FRAGMENT_FORMAT_VERSION</li>
	 *    </ul>
	 * @param propertyName a case-sensitive identifier for a property, or null
	 * @return the value associated with the key, or null if either no such property is set,
	 *     or the specified key was null
	 * @throws CoreException
	 * @see IIndexFragment#PROPERTY_FRAGMENT_ID
	 * @see IIndexFragment#PROPERTY_FRAGMENT_FORMAT_ID
	 * @see IIndexFragment#PROPERTY_FRAGMENT_FORMAT_VERSION
	 */
	public String getProperty(String propertyName) throws CoreException;

	/**
	 * Resets the counters for cache-hits and cache-misses.
	 */
	void resetCacheCounters();

	/**
	 * Returns cache hits since last reset of counters.
	 */
	long getCacheHits();

	/**
	 * Returns cache misses since last reset of counters.
	 */
	long getCacheMisses();

	/**
	 * Creates an empty file set for this fragment
	 * @since 5.0
	 */
	IIndexFragmentFileSet createFileSet();

	/**
	 * @return an array of all files contained in this index.
	 */
	IIndexFragmentFile[] getAllFiles() throws CoreException;

	/**
	 * Caches an object with the key, the cache must be cleared at latest when the fragment no
	 * longer holds a locks.
	 * @param replace if <code>false</code> an existing entry will not be replaced.
	 * @return the value that is actually stored.
	 */
	Object putCachedResult(Object key, Object value, boolean replace);

	/**
	 * Returns a previously cached object, the cache is cleared at latest when the fragment no
	 * longer holds a locks.
	 */
	Object getCachedResult(Object key);

	/**
	 * Clears the result cache.
	 */
	void clearResultCache();

	/**
	 * Returns the global inline namespaces.
	 * @throws CoreException
	 */
	IIndexScope[] getInlineNamespaces() throws CoreException;
}
