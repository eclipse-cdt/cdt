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


import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.filetype.ICFileType;
import org.eclipse.cdt.core.resources.IPathEntryStore;
import org.eclipse.cdt.internal.core.model.BatchOperation;
import org.eclipse.cdt.internal.core.model.CModel;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.core.model.ContainerEntry;
import org.eclipse.cdt.internal.core.model.IncludeEntry;
import org.eclipse.cdt.internal.core.model.LibraryEntry;
import org.eclipse.cdt.internal.core.model.MacroEntry;
import org.eclipse.cdt.internal.core.model.OutputEntry;
import org.eclipse.cdt.internal.core.model.PathEntryManager;
import org.eclipse.cdt.internal.core.model.ProjectEntry;
import org.eclipse.cdt.internal.core.model.SourceEntry;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

public class CoreModel {
	private static CoreModel cmodel = null;
	private static CModelManager manager = CModelManager.getDefault();
	private static PathEntryManager pathEntryManager = PathEntryManager.getDefault();
	public final static String CORE_MODEL_ID = CCorePlugin.PLUGIN_ID + ".coremodel"; //$NON-NLS-1$

	/**
	 * Creates an ICElement form and IPath. Returns null if not found.
	 */
	public ICElement create(IPath path) {
		return manager.create(path);
	}

	/**
	 * Creates a translation form and IPath. Returns null if not found.
	 */
	public ITranslationUnit createTranslationUnitFrom(ICProject cproject, IPath path) {
		return manager.createTranslationUnitFrom(cproject, path);
	}

	/**
	 * Creates an ICElement form and IFile. Returns null if not found.
	 */
	public ICElement create(IFile file) {
		return manager.create(file, null);
	}

	/**
	 * Creates an ICElement form and IFolder. Returns null if not found.
	 */
	public ICContainer create(IFolder folder) {
		return manager.create(folder, null);
	}

	/**
	 * Creates an ICElement form and IProject. Returns null if not found.
	 */
	public ICProject create(IProject project) {
		if (project == null) {
			return null;
		}
		CModel cModel = manager.getCModel();
		return cModel.getCProject(project);
	}

	/**
	 * Creates an ICElement form and IResource. Returns null if not found.
	 */
	public ICElement create(IResource resource) {
		return manager.create(resource, null);
	}

	/**
	 * Returns the C model.
	 * 
	 * @param root the given root
	 * @return the C model, or <code>null</code> if the root is null
	 */
	public static ICModel create(IWorkspaceRoot root) {
		if (root == null) {
			return null;
		}
		return manager.getCModel();
	}
	/**
	 * Returns the default ICModel.
	 */
	public ICModel getCModel() {
		return manager.getCModel();
	}

	/**
	 * Return true if IFile is a shared library, i.e. libxx.so
	 */
	public boolean isSharedLib(IFile file) {
		ICElement celement = create(file);
		if (celement instanceof IBinary) {
			return ((IBinary)celement).isSharedLib();
		}
		return false;
	}

	/**
	 * Return true if IFile is a an object(ELF), i.e. *.o
	 */
	public boolean isObject(IFile file) {
		ICElement celement = create(file);
		if (celement instanceof IBinary) {
			return ((IBinary)celement).isObject();
		}
		return false;
	}

	/**
	 * Return true if IFile is an ELF executable
	 */
	public boolean isExecutable(IFile file) {
		ICElement celement = create(file);
		if (celement instanceof IBinary) {
			return ((IBinary)celement).isExecutable();
		}
		return false;
	}

	/**
	 * Return true if IFile is an ELF.
	 */
	public boolean isBinary(IFile file) {
		ICElement celement = create(file);
		return (celement instanceof IBinary);
	}

	/**
	 * Return true if IFile is an Achive, *.a
	 */
	public boolean isArchive(IFile file) {
		ICElement celement = create(file);
		return(celement instanceof IArchive);
	}

	/**
	 * Return true if IFile is a TranslationUnit.
	 */
	public static boolean isTranslationUnit(IFile file) {
		if (file != null) {
			IProject p = file.getProject();
			if (hasCNature(p) || hasCCNature(p)) {
				ICFileType type = CCorePlugin.getDefault().getFileType(file.getProject(), file.getName());
				return type.isTranslationUnit();
			}
		}
		return false;
	}

	/**
	 * Return true if name is a valid name for a translation unit.
	 */
	public static boolean isValidTranslationUnitName(IProject project, String name) {
		ICFileType type = CCorePlugin.getDefault().getFileType(project, name);
		return type.isTranslationUnit();
	}

	/**
	 * Return true if name is a valid name for a translation unit.
	 */
	public static boolean isValidHeaderUnitName(IProject project, String name) {
		ICFileType type = CCorePlugin.getDefault().getFileType(project, name);
		return type.isHeader();
	}

	/**
	 * Return true if name is a valid name for a translation unit.
	 */
	public static boolean isValidSourceUnitName(IProject project, String name) {
		ICFileType type = CCorePlugin.getDefault().getFileType(project, name);
		return type.isSource();
	}

	/**
	 * Return true if project has C nature.
	 */
	public static boolean hasCNature(IProject project) {
		boolean ok = false;
		try {
			ok = (project.isOpen() && project.hasNature(CProjectNature.C_NATURE_ID));
		} catch (CoreException e) {
			//throws exception if the project is not open.
			//System.out.println (e);
			//e.printStackTrace();
		}
		return ok;
	}

	/**
	 * Return true if project has C++ nature.
	 */
	public static boolean hasCCNature(IProject project) {
		boolean ok = false;
		try {
			ok = (project.isOpen() && project.hasNature(CCProjectNature.CC_NATURE_ID));
		} catch (CoreException e) {
			//throws exception if the project is not open.
			//System.out.println (e);
			//e.printStackTrace();
		}
		return ok;
	}

	/**
	 * Creates and returns a new non-exported entry of kind <code>CDT_PROJECT</code>
	 * for the project identified by the given absolute path.
	 * <p>
	 * A project entry is used to denote a prerequisite project. The
	 * exported IPathEntry[] entries of the project will be contributed.
	 * <p>
	 * The prerequisite project is referred to using an absolute path relative
	 * to the workspace root.
	 * <p>
	 * The resulting entry is not exported to dependent projects. This method
	 * is equivalent to <code>newProjectEntry(path,false)</code>.
	 * <p>
	 * 
	 * @param projectPath
	 *            the workspace-relative path of the project
	 * @return a new project entry
	 * 
	 * @see CoreModel#newProjectEntry(IPath, boolean)
	 */
	public static IProjectEntry newProjectEntry(IPath projectPath) {
		return newProjectEntry(projectPath, false);
	}

	/**
	 * Creates and returns a new entry of kind <code>CDT_PROJECT</code> for
	 * the project identified by the given workspace-relative path.
	 * <p>
	 * A project entry is used to denote a prerequisite project. All the
	 * IPathEntries of the project will be contributed as a whole. The
	 * prerequisite project is referred to using an absolute path relative to
	 * the workspace root.
	 * <p>
	 * 
	 * @param projectPath
	 *            the absolute workspace-relative path of the prerequisite project
	 * @param isExported
	 *            indicates whether this entry is contributed to dependent
	 *            projects
	 * @return a new project entry
	 */
	public static IProjectEntry newProjectEntry(IPath projectPath, boolean isExported) {
		return new ProjectEntry(projectPath, isExported);
	}

	/**
	 * Creates and returns a new entry of kind <code>CDT_CONTAINER</code> for
	 * the given path. The path of the container will be used during resolution
	 * so as to map this container entry to a set of other entries the
	 * container is acting for.
	 * <p>
	 * The resulting entry is not exported to dependent projects. This method
	 * is equivalent to <code>newContainerEntry(path,false)</code>.
	 * <p>
	 * 
	 * @param containerPath
	 *            the id of the container
	 * @return a new container entry
	 *  
	 */
	public static IContainerEntry newContainerEntry(IPath id) {
		return newContainerEntry(id, false);
	}

	/**
	 * Creates and returns a new entry of kind <code>CDT_CONTAINER</code> for
	 * the given path. The path of the container will be used during resolution
	 * so as to map this container entry to a set of other entries the
	 * container is acting for.
	 * <p>
	 * The resulting entry is not exported to dependent projects. This method
	 * is equivalent to <code>newContainerEntry(path,false)</code>.
	 * <p>
	 */
	public static IContainerEntry newContainerEntry(IPath id, boolean isExported) {
		return new ContainerEntry(id, isExported);
	}

	/**
	 * Creates and returns a new entry of kind <code>CDT_LIBRARY</code>
	 * for the archive or folder identified by the given absolute path.
	 * 
	 * @param resourcePath
	 *            the affected project-relative resource path
	 * @param baseRef
	 *            the base reference path to find the library
	 * @param libraryPath
	 *            the library name.
	 * @return a new library entry
	 *  
	 */
	public static ILibraryEntry newLibraryRefEntry(IPath resourcePath, IPath baseRef, IPath libraryPath) {
		return new LibraryEntry(resourcePath, null, baseRef, libraryPath, null, null, null, false);
	}


	/**
	 * Creates and returns a new entry of kind <code>CDT_LIBRARY</code>
	 * for the archive or folder identified by the given absolute path.
	 * 
	 * Note that this operation does not attempt to validate or access the
	 * resources at the given paths.
	 * <p>
	 * 
	 * @param resourcePath
	 *            the affected project-relative resource path
	 * @param basePath
	 *            the base path of the library
	 * @param libraryPath
	 *            the path of the library
	 * @param sourceAttachmentPath
	 *            the project-relative path of the corresponding source archive or
	 *            folder, or <code>null</code> if none.
	 * @param sourceAttachmentRootPath
	 *            the location of the root within the source archive or folder
	 *            or <code>null</code>.
	 * @param sourceAttachmentPrefixMapping
	 *            prefix mapping or <code>null</code>.
	 * @param isExported
	 *           whether the entry is exported
	 * @return a new library entry
	 *  
	 */
	public static ILibraryEntry newLibraryEntry(IPath resourcePath, IPath basePath, IPath libraryPath, IPath sourceAttachmentPath, IPath sourceAttachmentRootPath,
			IPath sourceAttachmentPrefixMapping, boolean isExported) {
		return new LibraryEntry(resourcePath, basePath, null, libraryPath, sourceAttachmentPath, sourceAttachmentRootPath, sourceAttachmentPrefixMapping, isExported);
	}

	/**
	 * Creates and returns a new entry of kind <code>CDT_OUTPUT</code> for
	 * the project's output folder
	 * <p>
	 * 
	 * @param outputPath
	 *            the project-relative path of a folder
	 * @return a new source entry with not exclusion patterns
	 *  
	 */
	public static IOutputEntry newOutputEntry(IPath outputPath) {
		return newOutputEntry(outputPath, OutputEntry.NO_EXCLUSION_PATTERNS);
	}

	/**
	 * Creates and returns a new entry of kind <code>CDT_OUPUT</code> for
	 * the project
	 * 
	 * @param outputPath
	 *            the project-relative path of a folder
	 * @param exclusionPatterns
	 *            the possibly empty list of exclusion patterns represented as
	 *            relative paths
	 * @return a new source entry with the given exclusion patterns
	 */
	public static IOutputEntry newOutputEntry(IPath outputPath, IPath[] exclusionPatterns) {
		return new OutputEntry(outputPath, exclusionPatterns, false);
	}

	/**
	 * Creates and returns a new entry of kind <code>CDT_SOURCE</code> for
	 * the project's source folder identified by the given absolute
	 * workspace-relative path.
	 * <p>
	 * The source folder is referred to using an absolute path relative to the
	 * workspace root, e.g. <code>/Project/src</code>. A project's source
	 * folders are located with that project. That is, a source entry
	 * specifying the path <code>/P1/src</code> is only usable for project
	 * <code>P1</code>.
	 * </p>
	 * </p>
	 * <p>
	 * Note that all sources/binaries inside a project are contributed as a
	 * whole through a project entry (see <code>newProjectEntry</code>).
	 * Particular source entries cannot be selectively exported.
	 * </p>
	 * 
	 * @param sourcePath
	 *            the project-relative path of a source folder
	 * @return a new source entry with not exclusion patterns
	 *  
	 */
	public static ISourceEntry newSourceEntry(IPath sourcePath) {
		return newSourceEntry(sourcePath, SourceEntry.NO_EXCLUSION_PATTERNS);
	}

	/**
	 * Creates and returns a new entry of kind <code>CDT_SOURCE</code> for
	 * the project's source folder identified by the given absolute
	 * workspace-relative path but excluding all source files with paths
	 * matching any of the given patterns. This specifies that all package
	 * fragments within the root will have children of type <code>ICompilationUnit</code>.
	 * <p>
	 * The source folder is referred to using an absolute path relative to the
	 * workspace root, e.g. <code>/Project/src</code>. A project's source
	 * folders are located with that project. That is, a source entry
	 * specifying the path <code>/P1/src</code> is only usable for project
	 * <code>P1</code>.
	 * </p>
	 * 
	 * @param sourcePath
	 *            the absolute project-relative path of a source folder
	 * @param exclusionPatterns
	 *            the possibly empty list of exclusion patterns represented as
	 *            relative paths
	 * @return a new source entry with the given exclusion patterns
	 */
	public static ISourceEntry newSourceEntry(IPath sourcePath, IPath[] exclusionPatterns) {
		return new SourceEntry(sourcePath, exclusionPatterns);
	}

	/**
	 * Creates and returns a new entry of kind <code>CDT_INCLUDE</code>
	 * 
	 * @param resourcePath
	 *            the affected project-relative resource path
	 * @param basePath
	 *            the base path of the includePath
	 * @param includePath
	 *            the absolute path of the include
	 * @return IIncludeEntry
	 */
	public static IIncludeEntry newIncludeEntry(IPath resourcePath, IPath basePath, IPath includePath) {
		return newIncludeEntry(resourcePath, basePath, includePath, false);
	}

	/**
	 * * Creates and returns a new entry of kind <code>CDT_INCLUDE</code>
	 * 
	 * @param resourcePath
	 *            the affected project-relative resource path
	 * @param basePath
	 *            the base path of the includePath
	 * @param includePath
	 *            the absolute path of the include
	 * @param isSystemInclude
	 *            whether this include path should be consider a system include path
	 * @return IIncludeEntry
	 */
	public static IIncludeEntry newIncludeEntry(IPath resourcePath, IPath basePath, IPath includePath, boolean isSystemInclude) {
		return newIncludeEntry(resourcePath, basePath, includePath, isSystemInclude, IncludeEntry.NO_EXCLUSION_PATTERNS);
	}

	/**
	 * Creates and returns a new entry of kind <code>CDT_INCLUDE</code>
	 * 
	 * @param resoourcePath
	 *            the affected project-relative resource path
	 * @param basePath
	 *            the base path of the includePath
	 * @param includePath
	 *            the absolute path of the include
	 * @param isSystemInclude
	 *            wheter this include path should be consider the system
	 *            include path
	 * @param exclusionPatterns
	 *            exclusion patterns in the resource if a container
	 * @return IIincludeEntry
	 */
	public static IIncludeEntry newIncludeEntry(IPath resourcePath, IPath basePath, IPath includePath, boolean isSystemInclude, IPath[] exclusionPatterns) {
		return newIncludeEntry(resourcePath, basePath, includePath, isSystemInclude, exclusionPatterns, false);
	}

	/**
	 * Creates and returns a new entry of kind <code>CDT_INCLUDE</code>
	 * 
	 * @param resourcePath
	 *            the affected project-relative resource path
	 * @param basePath
	 *            the base path of the include
	 * @param includePath
	 *            the path of the include
	 * @param isSystemInclude
	 *            wheter this include path should be consider the system
	 *            include path
	 * @param exclusionPatterns
	 *            exclusion patterns in the resource if a container
	 * @param isExported
	 *            if the entry ix exported to reference projects
	 * @return IIincludeEntry
	 */
	public static IIncludeEntry newIncludeEntry(IPath resourcePath, IPath basePath, IPath includePath, boolean isSystemInclude,
			 IPath[] exclusionPatterns, boolean isExported) {
		return new IncludeEntry(resourcePath, basePath, null, includePath, isSystemInclude, exclusionPatterns, isExported);
	}

	/**
	 * Creates and returns a new entry of kind <code>CDT_INCLUDE</code>
	 * 
	 * @param resourcePath
	 *            the affected project-relative resource path
	 * @param baseRef
	 *            the base reference path of the include
	 * @param includePath
	 *            the path of the include
	 * @return IIincludeEntry
	 */
	public static IIncludeEntry newIncludeRefEntry(IPath resourcePath, IPath baseRef, IPath includePath) {
		return new IncludeEntry(resourcePath, null, baseRef, includePath, false, null, false);
	}

	/**
	 * Creates and returns an entry kind <code>CDT_MACRO</code>
	 * 
	 * @param path
	 *            the affected project-relative resource path
	 * @param macroName
	 *            the name of the macro
	 * @param macroValue
	 *            the value of the macro
	 * @return
	 */
	public static IMacroEntry newMacroEntry(IPath resourcePath, String macroName, String macroValue) {
		return newMacroEntry(resourcePath, macroName, macroValue, MacroEntry.NO_EXCLUSION_PATTERNS);
	}

	/**
	 * Creates and returns an entry kind <code>CDT_MACRO</code>
	 * 
	 * @param resourcePath
	 *            the affected project-relative resource path
	 * @param macroName
	 *            the name of the macro
	 * @param macroValue
	 *            the value of the macro
	 * @param exclusionPatterns
	 *            exclusion patterns in the resource if a container
	 * @return
	 */
	public static IMacroEntry newMacroEntry(IPath resourcePath, String macroName, String macroValue, IPath[] exclusionPatterns) {
		return newMacroEntry(resourcePath, macroName, macroValue, exclusionPatterns, false);
	}

	/**
	 * Creates and returns an entry kind <code>CDT_MACRO</code>
	 * 
	 * @param resourcePath
	 *            the affected workspace-relative resource path
	 * @param macroName
	 *            the name of the macro
	 * @param macroValue
	 *            the value of the macro
	 * @param exclusionPatterns
	 *            exclusion patterns in the resource if a container
	 * @return
	 */
	public static IMacroEntry newMacroEntry(IPath resourcePath, String macroName, String macroValue, IPath[] exclusionPatterns, boolean isExported) {
		return new MacroEntry(resourcePath, null, macroName, macroValue, exclusionPatterns, isExported);
	}

	/**
	 * Creates and returns an entry kind <code>CDT_MACRO</code>
	 * 
	 * @param resourcePath
	 *            the affected workspace-relative resource path
	 * @param baseRef
	 *        the base reference path
	 * @param macroName
	 *            the name of the macro
	 * @return
	 */
	public static IMacroEntry newMacroRefEntry(IPath resourcePath, IPath baseRef, String macroName) {
		return new MacroEntry(resourcePath, baseRef, macroName, null, MacroEntry.NO_EXCLUSION_PATTERNS, false);
	}

	/**
	 * Answers the project specific value for a given container. In case this
	 * container path could not be resolved, then will answer <code>null</code>.
	 * Both the container path and the project context are supposed to be
	 * non-null.
	 * <p>
	 * The containerPath is a formed by a first ID segment followed with extra
	 * segments, which can be used as additional hints for resolution. If no
	 * container was ever recorded for this container path onto this project
	 * (using <code>setPathEntryContainer</code>, then a <code>PathEntryContainerInitializer</code>
	 * will be activated if any was registered for this container ID onto the
	 * extension point "org.eclipse.cdt.core.PathEntryContainerInitializer".
	 * <p>
	 * PathEntry container values are persisted locally to the workspace, but
	 * are not preserved from a session to another. It is thus highly
	 * recommended to register a <code>PathEntryContainerInitializer</code>
	 * for each referenced container (through the extension point
	 * "org.eclipse.cdt.core.PathEntryContainerInitializer").
	 * <p>
	 * 
	 * @param containerPath
	 *            the name of the container, which needs to be resolved
	 * @param project
	 *            a specific project in which the container is being resolved
	 * @return the corresponding container or <code>null</code> if unable to
	 *         find one.
	 * 
	 * @exception CModelException
	 *                if an exception occurred while resolving the container,
	 *                or if the resolved container contains illegal entries
	 *                (contains CDT_CONTAINER entries or null entries).
	 * 
	 * @see PathEntryContainerInitializer
	 * @see IPathEntryContainer
	 * @see #setPathEntryContainer(IPath, ICProject[], IPathEntryContainer,
	 *      IProgressMonitor)
	 */
	public static IPathEntryContainer getPathEntryContainer(IPath containerPath, ICProject project) throws CModelException {
		return pathEntryManager.getPathEntryContainer(containerPath, project);
	}

	/**
	 * Bind a container reference path to some actual containers (<code>IPathEntryContainer</code>).
	 * This API must be invoked whenever changes in container need to be
	 * reflected onto the CModel.
	 * <p>
	 * In reaction to changing container values, the CModel will be updated to
	 * reflect the new state of the updated container.
	 * <p>
	 * This functionality cannot be used while the resource tree is locked.
	 * <p>
	 * PathEntry container values are persisted locally to the workspace, but
	 * are not preserved from a session to another. It is thus highly
	 * recommended to register a <code>PathEntryContainerInitializer</code>
	 * for each referenced container (through the extension point
	 * "org.eclipse.cdt.core.PathEntryContainerInitializer").
	 * <p>
	 * Note: setting a container to <code>null</code> will cause it to be
	 * lazily resolved again whenever its value is required. In particular,
	 * this will cause a registered initializer to be invoked again.
	 * <p>
	 * 
	 * @param affectedProjects -
	 *            the set of projects for which this container is being bound
	 * @param newContainer -
	 *            the container for the affected projects
	 * @param monitor
	 *            a monitor to report progress
	 * @throws CModelException
	 * @see PathEntryContainerInitializer
	 * @see #getPathEntryContainer(IPath, IJavaProject)
	 * @see IPathEntryContainer
	 */
	public void setPathEntryContainer(ICProject[] affectedProjects, IPathEntryContainer container, IProgressMonitor monitor)
			throws CModelException {
		pathEntryManager.setPathEntryContainer(affectedProjects, container, monitor);
	}

	/**
	 * Sets the pathentries of this project using a list of entries.
	 * <p>
	 * Setting the pathentries to <code>null</code> specifies a default
	 * classpath (the project root). Setting the pathentry to an empty array
	 * specifies an empty pathentry.
	 * <p>
	 * 
	 * @param entries
	 *            a list of entries
	 * @param monitor
	 *            the given progress monitor
	 * @exception CModelException
	 *                if the entries could not be set. Reasons include:
	 */
	public static void setRawPathEntries(ICProject cproject, IPathEntry[] newEntries, IProgressMonitor monitor) throws CModelException {
		pathEntryManager.setRawPathEntries(cproject, newEntries, monitor);
	}

	/**
	 * Returns the raw pathentries for the project. This corresponds to the
	 * exact set of entries which were assigned using <code>setRawPathEntries</code>
	 * <p>
	 * 
	 * @return the raw entires for the project
	 * @exception CModelException
	 *                if this element does not exist or if an exception occurs
	 *                while accessing its corresponding resource
	 * @see IPathEntry
	 */
	public static IPathEntry[] getRawPathEntries(ICProject cproject) throws CModelException {
		return pathEntryManager.getRawPathEntries(cproject);
	}

	/**
	 * This method returs the resolved pathentries for the project All
	 * pathEntry.CDT_CONTAINER entries in the project's will be replaced by the
	 * entries they resolve to.
	 * <p>
	 * The resulting resolved entries are accurate for the given point in time.
	 * If the project's raw entries are later modified they can become out of
	 * date. Because of this, hanging on resolved pathentries is not
	 * recommended.
	 * </p>
	 * 
	 * @return the resolved entries for the project
	 * @exception CModelException
	 * @see IPathEntry
	 */
	public static IPathEntry[] getResolvedPathEntries(ICProject cproject) throws CModelException {
		return pathEntryManager.getResolvedPathEntries(cproject);
	}

	/**
	 * Helper method finding the pathentry container initializer registered for
	 * a given container ID or <code>null</code> if none was found while
	 * iterating over the contributions to extension point to the extension
	 * point "org.eclipse.cdt.core.PathEntryContainerInitializer".
	 * <p>
	 * A containerID is the first segment of any container path, used to
	 * identify the registered container initializer.
	 * <p>
	 * 
	 * @param containerID -
	 *            a containerID identifying a registered initializer
	 * @return ClasspathContainerInitializer - the registered classpath
	 *         container initializer or <code>null</code> if none was found.
	 */
	public static PathEntryContainerInitializer getPathEntryContainerInitializer(String containerID) {
		return pathEntryManager.getPathEntryContainerInitializer(containerID);
	}

	/**
	 * Return the IPathEntryStore of the project.
	 * 
	 * @param project
	 * @return
	 * @throws CoreException
	 */
	public static IPathEntryStore getPathEntryStore(IProject project) throws CoreException {
		return pathEntryManager.getPathEntryStore(project, true);
	}

	/**
	 * Set in the map the store, but not persisted.
	 * 
	 * @param project
	 * @param store
	 */
	public static void setPathEntryStore(IProject project, IPathEntryStore store) {
		pathEntryManager.setPathEntryStore(project, store);
	}

	/**
	 * Validate a given path entries for a project, using the following rules:
	 * <ul>
	 *   <li> Entries cannot collide with each other; that is, all entry paths must be unique.
	 *   <li> The output entry location path can be empty, if not they must be located inside the project.
	 *   <li> Source entry location can be null, if not they must be located inside the project,
	 *   <li> A project entry cannot refer to itself directly (that is, a project cannot prerequisite itself).
     *   <li> Source entries or output locations cannot coincidate or be nested in each other, except for the following scenarii listed below:
	 *      <ul><li> A source folder can coincidate with its own output location, in which case this output can then contain library archives. 
	 *                     However, a specific output location cannot coincidate with any library or a distinct source folder than the one referring to it. </li> 
	 *              <li> A source/library folder can be nested in any source folder as long as the nested folder is excluded from the enclosing one. </li>
	 * 			<li> An output location can be nested in a source folder, if the source folder coincidates with the project itself, or if the output
	 * 					location is excluded from the source folder. </li>
	 *      </ul>
	 * </ul>
	 * 
	 *  Note that the entries are not validated automatically. Only bound variables or containers are considered 
	 *  in the checking process (this allows to perform a consistency check on an entry which has references to
	 *  yet non existing projects, folders, ...).
	 *  <p>
	 *  This validation is intended to anticipate issues prior to assigning it to a project. In particular, it will automatically
	 *  be performed during the setting operation (if validation fails, the classpath setting will not complete)
	 *  and during getResolvedPathEntries.
	 *  <p>
	 * @param cProject the given C project
	 * @param PathEntry entries
	 * @return a status object with code <code>IStatus.OK</code> if
	 *		the entries location are compatible, otherwise a status 
	 *		object indicating what is wrong with them
	 */
	public static ICModelStatus validatePathEntries(ICProject cProject, IPathEntry[] entries) {
		return pathEntryManager.validatePathEntry(cProject, entries);
	}

	/**
	 * Returns a C model status describing the problem related to this entry if any, 
	 * a status object with code <code>IStatus.OK</code> if the entry is fine (that is, if the
	 * given entry denotes a valid element).
	 * 
	 * @param cProject the given C project
	 * @param entry the given entry
	 * @param checkSourceAttachment a flag to determine if source attachement should be checked
	 * @param recurseInContainers flag indicating whether validation should be applied to container entries recursively
	 * @return a c model status describing the problem related to this entry if any, a status object with code <code>IStatus.OK</code> if the entry is fine
	 */
	public static ICModelStatus validatePathEntry(ICProject cProject, IPathEntry entry, boolean checkSourceAttachment, boolean recurseInContainers){
		return pathEntryManager.validatePathEntry(cProject, entry, checkSourceAttachment, recurseInContainers);
	}

	/**
	 * Return the singleton.
	 */
	public static CoreModel getDefault() {
		if (cmodel == null) {
			cmodel = new CoreModel();
		}
		return cmodel;
	}

	public void addElementChangedListener(IElementChangedListener listener) {
		manager.addElementChangedListener(listener);
	}

	/**
	 * Removes the given element changed listener. Has no affect if an
	 * identical listener is not registered.
	 * 
	 * @param listener
	 *            the listener
	 */
	public void removeElementChangedListener(IElementChangedListener listener) {
		manager.removeElementChangedListener(listener);
	}

	/**
	 * @see Plugin#startup
	 */
	public void startup() {
		manager.startup();
	}

	public void shutdown() {
		manager.shutdown();
	}

	private CoreModel() {
	}

	
	/**
	 * Runs the given action as an atomic C model operation.
	 * <p>
	 * After running a method that modifies C elements,
	 * registered listeners receive after-the-fact notification of
	 * what just transpired, in the form of a element changed event.
	 * This method allows clients to call a number of
	 * methods that modify C elements and only have element
	 * changed event notifications reported at the end of the entire
	 * batch.
	 * </p>
	 * <p>
	 * If this method is called outside the dynamic scope of another such
	 * call, this method runs the action and then reports a single
	 * element changed event describing the net effect of all changes
	 * done to C elements by the action.
	 * </p>
	 * <p>
	 * If this method is called in the dynamic scope of another such
	 * call, this method simply runs the action.
	 * </p>
	 *
	 * @param action the action to perform
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @exception CoreException if the operation failed.
	 * @since 2.1
	 */
	public static void run(IWorkspaceRunnable action, IProgressMonitor monitor) throws CoreException {
		run(action, ResourcesPlugin.getWorkspace().getRoot(), monitor);
	}
	/**
	 * Runs the given action as an atomic C model operation.
	 * <p>
	 * After running a method that modifies C elements,
	 * registered listeners receive after-the-fact notification of
	 * what just transpired, in the form of a element changed event.
	 * This method allows clients to call a number of
	 * methods that modify C elements and only have element
	 * changed event notifications reported at the end of the entire
	 * batch.
	 * </p>
	 * <p>
	 * If this method is called outside the dynamic scope of another such
	 * call, this method runs the action and then reports a single
	 * element changed event describing the net effect of all changes
	 * done to C elements by the action.
	 * </p>
	 * <p>
	 * If this method is called in the dynamic scope of another such
	 * call, this method simply runs the action.
	 * </p>
	 * <p>
 	 * The supplied scheduling rule is used to determine whether this operation can be
	 * run simultaneously with workspace changes in other threads. See 
	 * <code>IWorkspace.run(...)</code> for more details.
 	 * </p>
	 *
	 * @param action the action to perform
	 * @param rule the scheduling rule to use when running this operation, or
	 * <code>null</code> if there are no scheduling restrictions for this operation.
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @exception CoreException if the operation failed.
	 * @since 3.0
	 */
	public static void run(IWorkspaceRunnable action, ISchedulingRule rule, IProgressMonitor monitor) throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		if (workspace.isTreeLocked()) {
			new BatchOperation(action).run(monitor);
		} else {
			// use IWorkspace.run(...) to ensure that a build will be done in autobuild mode
			workspace.run(new BatchOperation(action), rule, IWorkspace.AVOID_UPDATE, monitor);
		}
	}	
	
	public void startIndexing() {
		manager.getIndexManager().reset();
	}

	public IndexManager getIndexManager() {
		return manager.getIndexManager();
	}
}
