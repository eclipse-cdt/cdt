/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;


import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A C project represents a view of a project resource in terms of C 
 * elements such as , ICContainer, ITranslationUnit ....
 * <code>CCore.create(project)</code>.
 * </p>
 *
 * @see CCore#create(org.eclipse.core.resources.IProject)
 * @see IBuildEntry
 */
public interface ICProject extends IParent, IOpenable, ICElement {

	/**
	 * Returns the <code>ICElement</code> corresponding to the given
	 * path, or <code>null</code> if no such 
	 * <code>ICElement</code> is found.
	 *
	 * @exception CModelException if the given path is <code>null</code>
	 *  or absolute
	 */
	ICElement findElement(IPath path) throws CModelException;

	/**
	 * Return the ArchiveContainer of this Project.
	 * @return
	 * @throws CModelException
	 */
	IArchiveContainer getArchiveContainer() throws CModelException;

	/**
	 * Return the BinaryContainer of this Project.
	 * @return
	 * @throws CModelException
	 */
	IBinaryContainer getBinaryContainer() throws CModelException;

	/**
	 * Returns the source root folders of the project.
	 * 
	 * <p>NOTE: This is equivalent to <code>getChildren()</code>.
	 * 
	 * @return ISourceRoot - root folders
	 * @exception CModelException
	 */
	ISourceRoot[] getSourceRoots() throws CModelException;

	/**
	 * Returns all of the existing source roots that exist
	 * on the pathentry, in the order they are defined by the ".cdtproject".
	 *
	 * @return all of the existing package fragment roots that exist
	 * on the classpath
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource
	 */
	ISourceRoot[] getAllSourceRoots() throws CModelException;

	/**
	 * 
	 * @param entry
	 * @return ISourceRoot
	 * @throws CModelException
	 */
	ISourceRoot getSourceRoot(ISourceEntry entry) throws CModelException;

	ISourceRoot findSourceRoot(IResource resource);
	
	ISourceRoot findSourceRoot(IPath path);

	/**
	 * Return the output entries.
	 * 
	 * @return
	 * @throws CModelException
	 */
	public IOutputEntry[] getOutputEntries() throws CModelException;

	/**
	 * @param resource
	 * @return
	 */
	boolean isOnOutputEntry(IResource resource);

	/**
	 * @param resource
	 * @return
	 */
	boolean isOnSourceRoot(IResource resource);

	/**
	 * @param element
	 * @return
	 */
	boolean isOnSourceRoot(ICElement element);

	/**
	 * Return the library references for this project.
	 * 
	 * @return [] ILibraryReference
	 */
	ILibraryReference[] getLibraryReferences() throws CModelException;

	/**
	 * Return the include paths set on the project.
	 * 
	 * @return
	 * @throws CModelException
	 */
	IIncludeReference[] getIncludeReferences() throws CModelException;

	/**
	 * Returns the names of the projects that are directly required by this
	 * project. A project is required if it is in its cpath entries.
	 * <p>
	 * The project names are returned in the order they appear on the cpathentries.
	 *
	 * @return the names of the projects that are directly required by this project
	 * @exception CModelException if this element does not exist or if an
	 *              exception occurs while accessing its corresponding resource
	 */
	String[] getRequiredProjectNames() throws CModelException;

	/**
	 * 
	 * @return IProject
	 */
	IProject getProject();

	/**
	 * Helper method for returning one option value only. Equivalent to <code>(String)this.getOptions(inheritCCoreOptions).get(optionName)</code>
	 * Note that it may answer <code>null</code> if this option does not exist, or if there is no custom value for it.
	 * <p>
	 * For a complete description of the configurable options, see <code>CCorePlugin#getDefaultOptions</code>.
	 * </p>
	 * 
	 * @param optionName the name of an option
	 * @param inheritCCoreOptions - boolean indicating whether CCorePlugin options should be inherited as well
	 * @return the String value of a given option
	 * @see CCorePlugin#getDefaultOptions
	 */
	String getOption(String optionName, boolean inheritCCoreOptions);

	/**
	 * Returns the table of the current custom options for this project. Projects remember their custom options,
	 * in other words, only the options different from the the CCorePlugin global options for the workspace.
	 * A boolean argument allows to directly merge the project options with global ones from <code>CCorePlugin</code>.
	 * <p>
	 * For a complete description of the configurable options, see <code>CCorePlugin#getDefaultOptions</code>.
	 * </p>
	 * 
	 * @param inheritCCoreOptions - boolean indicating whether CCorePlugin options should be inherited as well
	 * @return table of current settings of all options 
	 *   (key type: <code>String</code>; value type: <code>String</code>)
	 * @see CCorePlugin#getDefaultOptions
	 */
	Map getOptions(boolean inheritCCoreOptions);

	/**
	 * Helper method for setting one option value only. Equivalent to <code>Map options = this.getOptions(false); map.put(optionName, optionValue); this.setOptions(map)</code>
	 * <p>
	 * For a complete description of the configurable options, see <code>CCorePlugin#getDefaultOptions</code>.
	 * </p>
	 * 
	 * @param optionName the name of an option
	 * @param optionValue the value of the option to set
	 * @see CCorePlugin#getDefaultOptions
	 */
	void setOption(String optionName, String optionValue);

	/**
	 * Sets the project custom options. All and only the options explicitly included in the given table 
	 * are remembered; all previous option settings are forgotten, including ones not explicitly
	 * mentioned.
	 * <p>
	 * For a complete description of the configurable options, see <code>CCorePlugin#getDefaultOptions</code>.
	 * </p>
	 * 
	 * @param newOptions the new options (key type: <code>String</code>; value type: <code>String</code>),
	 *   or <code>null</code> to flush all custom options (clients will automatically get the global CCorePlugin options).
	 * @see CCorePlugin#getDefaultOptions
	 */
	void setOptions(Map newOptions);

	/**
	 * Returns the list of entries for the project. This corresponds to the exact set
	 * of entries which were assigned using <code>setCPathEntries</code>.
	 * <p>
	 *
	 * @return the list of entries for the project.
	 * @exception CModelException if this element does not exist or if an
	 *              exception occurs while accessing its corresponding resource
	 */
	IPathEntry[] getResolvedPathEntries() throws CModelException;

	/**
	 * Returns the list of entries for the project. This corresponds to the exact set
	 * of entries which were assigned using <code>setCPathEntries</code>.
	 * <p>
	 *
	 * @return the list of entries for the project.
	 * @exception CModelException if this element does not exist or if an
	 *              exception occurs while accessing its corresponding resource
	 */
	IPathEntry[] getRawPathEntries() throws CModelException;

	/**
	 * Sets the entries for this project.
	 *
	 * @param entries a list of IPathEntry[] entries
	 * @param monitor the given progress monitor
	 * @exception CModelException if the entries could not be set. Reasons include:
	 * <ul>
	 * <li> This C/C++ element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * <li> The entries are being modified during resource change event notification (CORE_EXCEPTION)
	 * </ul>
	 */
	void setRawPathEntries(IPathEntry[] entries, IProgressMonitor monitor) throws CModelException;

	/**
	 * Returns an array of non-C resources directly contained in this project.
	 * It does not transitively answer non-C resources contained in folders;
	 * these would have to be explicitly iterated over.
	 * <p>
	 * Non-C resources includes files, folders, projects  not accounted for.
	 * </p>
	 * 
	 * @return an array of non-C resources directly contained in this project
	 * @exception JavaModelException if this element does not exist or if an
	 *              exception occurs while accessing its corresponding resource
	 */
	Object[] getNonCResources() throws CModelException;

}
