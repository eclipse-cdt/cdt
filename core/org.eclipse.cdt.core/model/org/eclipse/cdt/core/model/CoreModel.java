package org.eclipse.cdt.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002. All Rights Reserved.
 */
import org.eclipse.cdt.core.CCorePlugin;
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

public class CoreModel {
	private static CoreModel cmodel = null;
	private static CModelManager manager = null;
	private static PathEntryManager pathEntryManager = null;
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
		return manager.isSharedLib(file);
	}

	/**
	 * Return true if IFile is a an object(ELF), i.e. *.o
	 */
	public boolean isObject(IFile file) {
		return manager.isObject(file);
	}

	/**
	 * Return true if IFile is an ELF executable
	 */
	public boolean isExecutable(IFile file) {
		return manager.isExecutable(file);
	}

	/**
	 * Return true if IFile is an ELF.
	 */
	public boolean isBinary(IFile file) {
		return manager.isBinary(file);
	}

	/**
	 * Return true if IFile is an Achive, *.a
	 */
	public boolean isArchive(IFile file) {
		return manager.isArchive(file);
	}

	/**
	 * Return true if IFile is a TranslationUnit.
	 */
	public boolean isTranslationUnit(IFile file) {
		return manager.isTranslationUnit(file);
	}

	/**
	 * Return true if name is a valid name for a translation unit.
	 */
	public boolean isValidTranslationUnitName(String name) {
		return manager.isValidTranslationUnitName(name);
	}

	/**
	 * Return the list of headers extensions.
	 */
	public String[] getHeaderExtensions() {
		return manager.getHeaderExtensions();
	}

	/**
	 * Returns the list of source extensions.
	 */
	public String[] getSourceExtensions() {
		return manager.getSourceExtensions();
	}

	/**
	 * Returns the list of assembly file extensions.
	 */
	public String[] getAssemblyExtensions() {
		return manager.getAssemblyExtensions();
	}

	/**
	 * Returns the list of headers and sources extensions
	 */
	public String[] getTranslationUnitExtensions() {
		return manager.getTranslationUnitExtensions();
	}

	/**
	 * Return true if project has C nature.
	 */
	public boolean hasCNature(IProject project) {
		return manager.hasCNature(project);
	}

	/**
	 * Return true if project has C++ nature.
	 */
	public boolean hasCCNature(IProject project) {
		return manager.hasCCNature(project);
	}

	/**
	 * Creates and returns a new non-exported entry of kind <code>CDT_PROJECT</code>
	 * for the project identified by the given absolute path.
	 * <p>
	 * A project entry is used to denote a prerequisite project. The
	 * IPathEntry[] entries of the project will be contributed.
	 * <p>
	 * The prerequisite project is referred to using an absolute path relative
	 * to the workspace root.
	 * <p>
	 * The resulting entry is not exported to dependent projects. This method
	 * is equivalent to <code>newProjectEntry(path,false)</code>.
	 * <p>
	 * 
	 * @param path
	 *            the absolute path of the binary archive
	 * @return a new project entry
	 * 
	 * @see CoreModel#newProjectEntry(IPath, boolean)
	 */
	public static IProjectEntry newProjectEntry(IPath path) {
		return newProjectEntry(path, false);
	}

	/**
	 * Creates and returns a new entry of kind <code>CDT_PROJECT</code> for
	 * the project identified by the given absolute path.
	 * <p>
	 * A project entry is used to denote a prerequisite project. All the
	 * IPathEntries of the project will be contributed as a whole. The
	 * prerequisite project is referred to using an absolute path relative to
	 * the workspace root.
	 * <p>
	 * 
	 * @param path
	 *            the absolute path of the prerequisite project
	 * @param isExported
	 *            indicates whether this entry is contributed to dependent
	 *            projects in addition to the output location
	 * @return a new project entry
	 */
	public static IProjectEntry newProjectEntry(IPath path, boolean isExported) {
		return new ProjectEntry(path, isExported);
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
	 * Creates and returns a new non-exported entry of kind <code>CDT_LIBRARY</code>
	 * for the archive or folder identified by the given absolute path.
	 * 
	 * Note that this operation does not attempt to validate or access the
	 * resources at the given paths.
	 * <p>
	 * The resulting entry is not exported to dependent projects. This method
	 * is equivalent to <code>newLibraryEntry(-,-,-,false)</code>.
	 * <p>
	 * 
	 * @param path
	 *            the absolute path of the library
	 * @param sourceAttachmentPath
	 *            the absolute path of the corresponding source archive or
	 *            folder, or <code>null</code> if none.
	 * @param sourceAttachmentRootPath
	 *            the location of the root within the source archive or folder
	 *            or <code>null</code>.
	 * @param sourceAttachmentPrefixMapping
	 *            prefix mapping or <code>null</code>.
	 * @return a new library entry
	 *  
	 */
	public static ILibraryEntry newLibraryEntry(IPath libraryPath, IPath sourceAttachmentPath, IPath sourceAttachmentRootPath,
			IPath sourceAttachmentPrefixMapping) {
		return newLibraryEntry(libraryPath, null, sourceAttachmentPath, sourceAttachmentRootPath, sourceAttachmentPrefixMapping, false);
	}

	/**
	 * Creates and returns a new non-exported entry of kind <code>CDT_LIBRARY</code>
	 * for the archive or folder identified by the given absolute path.
	 * 
	 * Note that this operation does not attempt to validate or access the
	 * resources at the given paths.
	 * <p>
	 * 
	 * @param path
	 *            the path of the library
	 * @param path
	 *            the base path of the library
	 * @param sourceAttachmentPath
	 *            the absolute path of the corresponding source archive or
	 *            folder, or <code>null</code> if none.
	 * @param sourceAttachmentRootPath
	 *            the location of the root within the source archive or folder
	 *            or <code>null</code>.
	 * @param sourceAttachmentPrefixMapping
	 *            prefix mapping or <code>null</code>.
	 * @return a new library entry
	 *  
	 */
	public static ILibraryEntry newLibraryEntry(IPath libraryPath, IPath basePath, IPath sourceAttachmentPath, IPath sourceAttachmentRootPath,
			IPath sourceAttachmentPrefixMapping, boolean isExported) {
		return new LibraryEntry(libraryPath, basePath, sourceAttachmentPath, sourceAttachmentRootPath, sourceAttachmentPrefixMapping, isExported);
	}

	/**
	 * Creates and returns a new entry of kind <code>CDT_OUTPUT</code> for
	 * the project's output folder
	 * <p>
	 * 
	 * @param path
	 *            the project-relative path of a binary folder
	 * @return a new source entry with not exclusion patterns
	 *  
	 */
	public static IOutputEntry newOutputEntry(IPath path) {
		return newOutputEntry(path, OutputEntry.NO_EXCLUSION_PATTERNS);
	}

	/**
	 * Creates and returns a new entry of kind <code>CDT_OUPUT</code> for
	 * the project
	 * 
	 * @param path
	 *            the absolute project-relative path of a binary folder
	 * @param exclusionPatterns
	 *            the possibly empty list of exclusion patterns represented as
	 *            relative paths
	 * @return a new source entry with the given exclusion patterns
	 */
	public static IOutputEntry newOutputEntry(IPath path, IPath[] exclusionPatterns) {
		return new OutputEntry(path, exclusionPatterns, false);
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
	 * @param path
	 *            the project-relative path of a source folder
	 * @return a new source entry with not exclusion patterns
	 *  
	 */
	public static ISourceEntry newSourceEntry(IPath path) {
		return newSourceEntry(path, SourceEntry.NO_EXCLUSION_PATTERNS);
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
	 * @param path
	 *            the absolute workspace-relative path of a source folder
	 * @param exclusionPatterns
	 *            the possibly empty list of exclusion patterns represented as
	 *            relative paths
	 * @return a new source entry with the given exclusion patterns
	 */
	public static ISourceEntry newSourceEntry(IPath path, IPath[] exclusionPatterns) {
		return new SourceEntry(path, exclusionPatterns);
	}

	/**
	 * Creates and returns a new entry of kind <code>CDT_INCLUDE</code>
	 * 
	 * @param includePath
	 *            the absolute path of the include
	 * @return IIncludeEntry
	 */
	public static IIncludeEntry newIncludeEntry(IPath includePath) {
		return newIncludeEntry(null, includePath);
	}

	/**
	 * Creates and returns a new entry of kind <code>CDT_INCLUDE</code>
	 * 
	 * @param path
	 *            the affected project-relative resource path
	 * @param includePath
	 *            the absolute path of the include
	 * @return IIncludeEntry
	 */
	public static IIncludeEntry newIncludeEntry(IPath resourcePath, IPath includePath) {
		return newIncludeEntry(resourcePath, includePath, false);
	}

	/**
	 * * Creates and returns a new entry of kind <code>CDT_INCLUDE</code>
	 * 
	 * @param path
	 *            the affected project-relative resource path
	 * @param includePath
	 *            the absolute path of the include
	 * @param isSystemInclude
	 *            whether this include path should be consider a system include path
	 * @return IIncludeEntry
	 */
	public static IIncludeEntry newIncludeEntry(IPath resourcePath, IPath includePath, boolean isSystemInclude) {
		return newIncludeEntry(resourcePath, includePath, isSystemInclude, IncludeEntry.NO_EXCLUSION_PATTERNS);
	}

	/**
	 * Creates and returns a new entry of kind <code>CDT_INCLUDE</code>
	 * 
	 * @param path
	 *            the affected project-relative resource path
	 * @param includePath
	 *            the absolute path of the include
	 * @param isSystemInclude
	 *            wheter this include path should be consider the system
	 *            include path
	 * @param exclusionPatterns
	 *            exclusion patterns in the resource if a container
	 * @return IIincludeEntry
	 */
	public static IIncludeEntry newIncludeEntry(IPath resourcePath, IPath includePath, boolean isSystemInclude, IPath[] exclusionPathterns) {
		return newIncludeEntry(resourcePath, includePath, null, isSystemInclude, exclusionPathterns);
	}
	
	/**
	 * Creates and returns a new entry of kind <code>CDT_INCLUDE</code>
	 * 
	 * @param path
	 *            the affected project-relative resource path
	 * @param includePath
	 *            the path of the include
	 * @param basePath
	 *            the base path of the include
	 * @param isSystemInclude
	 *            wheter this include path should be consider the system
	 *            include path
	 * @param isRecursive
	 *            if the resource is a folder the include applied to all
	 *            recursively
	 * @param exclusionPatterns
	 *            exclusion patterns in the resource if a container
	 * @return IIincludeEntry
	 */
	public static IIncludeEntry newIncludeEntry(IPath resourcePath, IPath includePath, IPath basePath, boolean isSystemInclude,
			 IPath[] exclusionPatterns) {
		return newIncludeEntry(resourcePath, includePath, basePath, isSystemInclude, exclusionPatterns, resourcePath == null || resourcePath.isEmpty());
	}

	/**
	 * Creates and returns a new entry of kind <code>CDT_INCLUDE</code>
	 * 
	 * @param path
	 *            the affected project-relative resource path
	 * @param includePath
	 *            the path of the include
	 * @param basePath
	 *            the base path of the include
	 * @param isSystemInclude
	 *            wheter this include path should be consider the system
	 *            include path
	 * @param exclusionPatterns
	 *            exclusion patterns in the resource if a container
	 * @param isExported
	 *            if the entry ix exported to reference projects
	 * @return IIincludeEntry
	 */
	public static IIncludeEntry newIncludeEntry(IPath resourcePath, IPath includePath, IPath basePath, boolean isSystemInclude,
			 IPath[] exclusionPatterns, boolean isExported) {
		return new IncludeEntry(resourcePath, includePath, basePath, isSystemInclude, exclusionPatterns, isExported);
	}

	/**
	 * Creates and returns an entry kind <code>CDT_MACRO</code>
	 * 
	 * @param macroName
	 *            the name of the macro
	 * @param macroValue
	 *            the value of the macro
	 * @return
	 */
	public static IMacroEntry newMacroEntry(String macroName, String macroValue) {
		return newMacroEntry(null, macroName, macroValue);
	}

	/**
	 * Creates and returns an entry kind <code>CDT_MACRO</code>
	 * 
	 * @param path
	 *            the affected workspace-relative resource path
	 * @param macroName
	 *            the name of the macro
	 * @param macroValue
	 *            the value of the macro
	 * @return
	 */
	public static IMacroEntry newMacroEntry(IPath basePath, String macroName) {
		return newMacroEntry(null, basePath, macroName, null, MacroEntry.NO_EXCLUSION_PATTERNS);
	}

	/**
	 * Creates and returns an entry kind <code>CDT_MACRO</code>
	 * 
	 * @param path
	 *            the affected workspace-relative resource path
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
	 * @param path
	 *            the affected workspace-relative resource path
	 * @param macroName
	 *            the name of the macro
	 * @param macroValue
	 *            the value of the macro
	 * @param exclusionPatterns
	 *            exclusion patterns in the resource if a container
	 * @return
	 */
	public static IMacroEntry newMacroEntry(IPath resourcePath, String macroName, String macroValue, IPath[] exclusionPatterns) {
		return newMacroEntry(resourcePath, null, macroName, macroValue, exclusionPatterns);
	}

	/**
	 * Creates and returns an entry kind <code>CDT_MACRO</code>
	 * 
	 * @param path
	 *            the affected workspace-relative resource path
	 * @param macroName
	 *            the name of the macro
	 * @param macroValue
	 *            the value of the macro
	 * @param exclusionPatterns
	 *            exclusion patterns in the resource if a container
	 * @return
	 */
	public static IMacroEntry newMacroEntry(IPath resourcePath, IPath basePath, String macroName, String macroValue, IPath[] exclusionPatterns) {
		return new MacroEntry(resourcePath, basePath, macroName, macroValue, exclusionPatterns);
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
	public IPathEntryContainer getPathEntryContainer(IPath containerPath, ICProject project) throws CModelException {
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
	public void setRawPathEntries(ICProject cproject, IPathEntry[] newEntries, IProgressMonitor monitor) throws CModelException {
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
	public IPathEntry[] getRawPathEntries(ICProject cproject) throws CModelException {
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
	public IPathEntry[] getResolvedPathEntries(ICProject cproject) throws CModelException {
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
	 * Return the singleton.
	 */
	public static CoreModel getDefault() {
		if (cmodel == null) {
			cmodel = new CoreModel();
			manager = CModelManager.getDefault();
			pathEntryManager = PathEntryManager.getDefault();
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

	public static void run(IWorkspaceRunnable action, IProgressMonitor monitor) throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		if (workspace.isTreeLocked()) {
			new BatchOperation(action).run(monitor);
		} else {
			// use IWorkspace.run(...) to ensure that a build will be done in
			// autobuild mode
			workspace.run(new BatchOperation(action), monitor);
		}
	}

	public void startIndexing() {
		manager.getIndexManager().reset();
	}

	public IndexManager getIndexManager() {
		return manager.getIndexManager();
	}
}
