package org.eclipse.cdt.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
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
	 * Return true if project has C nature.
	 */
	public boolean hasCNature(IProject project){
		return manager.hasCNature(project);
	}

	public boolean hasCCNature(IProject project){
		return manager.hasCCNature(project);
	}

	/**
	 * Return the binaryParser of the Project.
	 */
	public String getBinaryParserFormat(IProject project) {
		return manager.getBinaryParserFormat(project);
	}

	/**
	 * Set the binaryParser of the Project.
	 */
	public void setBinaryParserFormat(IProject project, String format, IProgressMonitor monitor) {
		manager.setBinaryParserFormat(project, format, monitor);
	}

	/**
	 * Return the default BinaryParser format
	 */
	public String getDefaultBinaryParserFormat() {
		return manager.getDefaultBinaryParserFormat();
	}

	/**
	 * Set the default binaryParser.
	 */
	public void setDefaultBinaryParserFormat(String format) {
		manager.setDefaultBinaryParserFormat(format);
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
