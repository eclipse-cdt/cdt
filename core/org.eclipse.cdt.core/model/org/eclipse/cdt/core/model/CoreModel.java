package org.eclipse.cdt.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

// This should be done in the Plugin.

public class CoreModel {

	private static CoreModel cmodel = null;
	private static CModelManager manager = null;

	/**
	 * Plugin string id.
     */
	public final static String PLUGIN_ID = "org.eclipse.cdt.core";

	/**
	 * C nature string name, "cnature".
     */
	public final static String C_NATURE_NAME = "cnature";
	/**
	 * C nature string id, PLUGIN_ID + C_NATURE_NAME
     */
	public final static String C_NATURE_ID = PLUGIN_ID + "." + C_NATURE_NAME;

	/**
	 * C++ nature string name, "ccnature"
     */
	public final static String CC_NATURE_NAME = "ccnature";
	/**
	 * C++ nature string id, PLUGIN_ID + CC_NATURE_NAME
     */
	public final static String CC_NATURE_ID = PLUGIN_ID + "." + CC_NATURE_NAME;

	/**
	 * Returns the plugin id.
	 */
	public static String getPluginId() {
		return PLUGIN_ID;
	}

	/**
	 * Returns the C nature Name.
	 */
	public static String getCNatureName () {
		return C_NATURE_NAME;
	}

	/**
	 * Returns the C++ nature name.
	 */
	public static String getCCNatureName () {
		return CC_NATURE_NAME;
	}

	/**
	 * Returns the C nature Id.
	 */
	public static String getCNatureId () {
		return C_NATURE_ID;
	}

	/**
	 * Returns the C++ nature Id.
	 */
	public static String getCCNatureId () {
		return CC_NATURE_ID;
	}

	/**
	 * Creates an ICElement form and IPath.
	 * Returns null if not found.
	 */
	public ICResource create(IPath path) {
		return manager.create(path);
	}

	/**
	 * Creates an ICElement form and IFile.
	 * Returns null if not found.
	 */
	public ICFile create(IFile file) {
		return manager.create(file);
	}

	/**
	 * Creates an ICElement form and IFolder.
	 * Returns null if not found.
	 */
	public ICFolder create(IFolder folder) {
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
	public ICResource create(IResource resource) {
		return manager.create(resource);
	}

	/**
	 * Returns the default ICRoot.
	 */
	public ICRoot getCRoot() {
		return manager.getCRoot();
	}

	/**
	 * Return true if IFile is a shared library, i.e. libxx.so
	 */
	public static boolean isSharedLib(IFile file) {
		return manager.isSharedLib(file);
	}

	/**
	 * Return true if IFile is a an object(ELF), i.e. *.o
	 */
	public static boolean isObject(IFile file) {
		return manager.isObject(file);
	}

	/**
	 * Return true if IFile is an ELF executable
	 */
	public static boolean isExecutable(IFile file) {
		return manager.isExecutable(file);
	}

	/**
	 * Return true if IFile is an ELF.
	 */
	public static boolean isBinary(IFile file) {
		return manager.isBinary(file);
	}

	/**
	 * Return true if IFile is an Achive, *.a
	 */
	public static boolean isArchive(IFile file) {
		return manager.isArchive(file);
	}

	/**
	 * Return true if IFile is a TranslationUnit.
	 */
	public static boolean isTranslationUnit(IFile file) {
		return manager.isTranslationUnit(file);
	}

	/**
	 * Return true if name is a valid name for a translation unit.
	 */
	public static boolean isValidTranslationUnitName(String name){
		return manager.isValidTranslationUnitName(name);
	}

	/**
	 * Return true if project has C nature.
	 */
	public static boolean hasCNature(IProject project){
		return manager.hasCNature(project);
	}

	public static boolean hasCCNature(IProject project){
		return manager.hasCCNature(project);
	}

	public static void addCNature(IProject project, IProgressMonitor monitor) throws CModelException {
		manager.addCNature(project, monitor);
	}

	public static void addCCNature(IProject project, IProgressMonitor monitor) throws CModelException {
		manager.addCCNature(project, monitor);
	}

	public static void removeCNature(IProject project, IProgressMonitor monitor) throws CModelException {
		manager.removeCNature(project, monitor);
	}

	public static void removeCCNature(IProject project, IProgressMonitor monitor) throws CModelException {
		manager.removeCCNature(project, monitor);
	}

	public static void addNature(IProject project, String natureId, IProgressMonitor monitor)
		throws CModelException {
		manager.addNature(project, natureId, monitor);
	}

	public static void removeNature(IProject project, String natureId, IProgressMonitor monitor)
		throws CModelException {
		manager.removeNature(project, natureId, monitor);
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

	public static void addElementChangedListener(IElementChangedListener listener) {
		manager.addElementChangedListener(listener);
	}

	/**
	 * Removes the given element changed listener.
	 * Has no affect if an identical listener is not registered.
	 *
	 * @param listener the listener
	 */
	public static void removeElementChangedListener(IElementChangedListener listener) {
		manager.removeElementChangedListener(listener);
	}

	private CoreModel() {
	}
}
