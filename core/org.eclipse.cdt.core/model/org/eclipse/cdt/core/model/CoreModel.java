package org.eclipse.cdt.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.core.model.BatchOperation;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public class CoreModel {

	private static CoreModel cmodel = null;
	private static CModelManager manager = null;
	
	public final static String CORE_MODEL_ID = CCorePlugin.PLUGIN_ID + ".coremodel";

	/**
	 * Creates an ICElement form and IPath.
	 * Returns null if not found.
	 */
	public ICElement create(IPath path) {
		return manager.create(path);
	}

	/**
	 * Creates an ICElement form and IFile.
	 * Returns null if not found.
	 */
	public ICElement create(IFile file) {
		return manager.create(file);
	}

	/**
	 * Creates an ICElement form and IFolder.
	 * Returns null if not found.
	 */
	public ICContainer create(IFolder folder) {
		return manager.create(folder);
	}

	/**
	 * Creates an ICElement form and IProject.
	 * Returns null if not found.
	 */
	public ICProject create(IProject project) {
		return manager.create(project);
	}

	/**
	 * Creates an ICElement form and IResource.
	 * Returns null if not found.
	 */
	public ICElement create(IResource resource) {
		return manager.create(resource);
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
	public boolean isValidTranslationUnitName(String name){
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
	 * Returns the list of headers and sources extensions
	 */
	public String[] getTranslationUnitExtensions() {
		return manager.getTranslationUnitExtensions();
	}

	/**
	 * Return true if project has C nature.
	 */
	public boolean hasCNature(IProject project){
		return manager.hasCNature(project);
	}

	/**
	 * Return true if project has C++ nature.
	 */
	public boolean hasCCNature(IProject project){
		return manager.hasCCNature(project);
	}

	/**
	 * TODO: this is a temporary hack until, the CDescriptor manager is
	 * in place and could fire deltas of Parser change.
	 * @deprecated this function will be removed shortly.
	 */
	public void resetBinaryParser(IProject project) {
		manager.resetBinaryParser(project);
	}

	/**
	 * Return the singleton.
	 */
	public static CoreModel getDefault() {
		if (cmodel == null) {
			cmodel = new CoreModel();
			manager = CModelManager.getDefault();
		}
		return cmodel;
	}

	public void addElementChangedListener(IElementChangedListener listener) {
		manager.addElementChangedListener(listener);
	}

	/**
	 * Removes the given element changed listener.
	 * Has no affect if an identical listener is not registered.
	 *
	 * @param listener the listener
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
			// use IWorkspace.run(...) to ensure that a build will be done in autobuild mode
			workspace.run(new BatchOperation(action), monitor);
		}
	}
	
	public void startIndexing()
	{
		manager.getIndexManager().reset();	
	}
	
	public IndexManager getIndexManager(){
		return manager.getIndexManager();
	}
	
}
