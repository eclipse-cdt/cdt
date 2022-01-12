/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Norbert Ploett (Siemens AG)
 *     Anton Leherbauer (Wind River Systems)
 *     Ed Swartz (Nokia)
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import java.io.ByteArrayInputStream;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.part.FileEditorInput;

public class EditorUtility {

	/**
	 * The ID of the default text editor
	 */
	public static final String DEFAULT_TEXT_EDITOR_ID = EditorsUI.DEFAULT_TEXT_EDITOR_ID;

	private EditorUtility() {
	}

	private static IEditorPart openInEditor(IEditorInput input, String editorID, boolean activate)
			throws PartInitException {
		if (input != null) {
			IWorkbenchPage p = CUIPlugin.getActivePage();
			if (p != null) {
				return p.openEditor(input, editorID, activate);
			}
		}
		return null;
	}

	private static IEditorInput getEditorInput(ICElement element) {
		while (element != null) {
			if (element instanceof ISourceReference) {
				ITranslationUnit tu = ((ISourceReference) element).getTranslationUnit();
				if (tu != null) {
					element = tu;
				}
			}
			if (element instanceof IWorkingCopy && ((IWorkingCopy) element).isWorkingCopy())
				element = ((IWorkingCopy) element).getOriginalElement();

			if (element instanceof ITranslationUnit) {
				ITranslationUnit unit = (ITranslationUnit) element;
				IResource resource = unit.getResource();
				if (resource instanceof IFile) {
					return new FileEditorInput((IFile) resource);
				}
				return new ExternalEditorInput(unit, new FileStorage(unit.getPath()));
			}

			if (element instanceof IBinary) {
				return new ExternalEditorInput(getStorage((IBinary) element), (IPath) null);
			}

			element = element.getParent();
		}

		return null;
	}

	public static IEditorInput getEditorInput(Object input) {
		if (input instanceof ICElement) {
			return getEditorInput((ICElement) input);
		}
		if (input instanceof IFile) {
			return new FileEditorInput((IFile) input);
		}
		if (input instanceof IStorage) {
			return new ExternalEditorInput((IStorage) input);
		}
		return null;
	}

	/**
	 * Utility method to open an editor for the given file system location
	 * using {@link #getEditorInputForLocation(IPath, ICElement)} to create
	 * the editor input.
	 *
	 * @param location  a file system location
	 * @param element  an element related to the target file, may be <code>null</code>
	 * @throws PartInitException
	 */
	public static IEditorPart openInEditor(IPath location, ICElement element) throws PartInitException {
		IEditorInput input = getEditorInputForLocation(location, element);
		return EditorUtility.openInEditor(input, getEditorID(input, element), true);
	}

	/**
	 * Utility method to get an editor input for the given file system location.
	 * If the location denotes a workspace file, a <code>FileEditorInput</code>
	 * is returned, otherwise, the input is an <code>IStorageEditorInput</code>
	 * assuming the location points to an existing file in the file system.
	 * The <code>ICElement</code> is used to determine the associated project
	 * in case the location can not be resolved to a workspace <code>IFile</code>.
	 *
	 * @param location  a valid file system location
	 * @param context  an element related to the target file, may be <code>null</code>
	 * @return an editor input
	 */
	public static IEditorInput getEditorInputForLocation(IPath location, ICElement context) {
		IFile resource = getWorkspaceFileAtLocation(location, context);
		if (resource != null) {
			return new FileEditorInput(resource);
		}

		if (context == null) {
			// try to synthesize a context for a location appearing on a project's
			// include paths
			try {
				ICProject[] projects = CCorePlugin.getDefault().getCoreModel().getCModel().getCProjects();
				outerFor: for (int i = 0; i < projects.length; i++) {
					IIncludeReference[] includeReferences = projects[i].getIncludeReferences();
					for (int j = 0; j < includeReferences.length; j++) {
						if (includeReferences[j].isOnIncludeEntry(location)) {
							context = projects[i];
							break outerFor;
						}
					}
				}
				if (context == null && projects.length > 0) {
					// last resort: just take any of them
					context = projects[0];
				}
			} catch (CModelException e) {
			}
		}

		if (context != null) {
			// try to get a translation unit from the location and associated element
			ICProject cproject = context.getCProject();
			if (cproject != null) {
				ITranslationUnit unit = CoreModel.getDefault().createTranslationUnitFrom(cproject, location);
				if (unit != null) {
					return new ExternalEditorInput(unit, new FileStorage(location));
				}
				// no translation unit - still try to get a sensible marker resource
				// from the associated element
				IResource markerResource = cproject.getProject();
				return new ExternalEditorInput(new FileStorage(location), markerResource);
			}
		}
		return new ExternalEditorInput(new FileStorage(location));
	}

	/**
	 * Utility method to resolve a file system location to a workspace resource.
	 * If a context element is given and there are multiple matches in the workspace,
	 * a resource with the same project of the context element are preferred.
	 *
	 * @param location  a valid file system location
	 * @param context  an element related to the target file, may be <code>null</code>
	 * @return an <code>IFile</code> or <code>null</code>
	 */
	private static IFile getWorkspaceFileAtLocation(IPath location, ICElement context) {
		IFile file = FileBuffers.getWorkspaceFileAtLocation(location);
		if (file == null) {
			// try to find a linked resource
			IProject project = null;
			if (context != null) {
				ICProject cProject = context.getCProject();
				if (cProject != null) {
					project = cProject.getProject();
				}
			}
			IFile bestMatch = null;
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IFile[] files = root.findFilesForLocationURI(URIUtil.toURI(location));
			for (int i = 0; i < files.length; i++) {
				file = files[i];
				if (file.isAccessible()) {
					if (project != null && file.getProject() == project) {
						bestMatch = file;
						break;
					}
					if (bestMatch == null) {
						bestMatch = file;
						if (project == null) {
							break;
						}
					}
				}
			}
			return bestMatch;
		}
		return file;
	}

	/**
	 * Determine the editor id from the given file name using
	 * the workspace-wide content-type definitions.
	 *
	 * @param name  the file name
	 * @return a valid editor id, never <code>null</code>
	 */
	public static String getEditorID(String name) {
		IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
		if (registry != null) {
			IEditorDescriptor descriptor = registry.getDefaultEditor(name);
			if (descriptor != null) {
				return descriptor.getId();
			}
		}
		return DEFAULT_TEXT_EDITOR_ID;
	}

	/**
	 * Determine the editor id from the given editor input and optional input object.
	 * When a translation unit can be obtained, the project-specific content-type
	 * mechanism is used to determine the correct editor id.
	 * If that fails, the editor id is determined by file name and extension using
	 * the workspace-wide content-type definitions.
	 *
	 * @param input  the editor input
	 * @param inputObject  the input object (used to create the editor input) or <code>null</code>
	 * @return a valid editor id, never <code>null</code>
	 */
	public static String getEditorID(IEditorInput input, Object inputObject) {

		ITranslationUnit tunit = null;
		if (inputObject instanceof ITranslationUnit) {
			tunit = (ITranslationUnit) inputObject;
		} else if (input instanceof IFileEditorInput) {
			IFileEditorInput editorInput = (IFileEditorInput) input;
			IFile file = editorInput.getFile();
			ICElement celement = CoreModel.getDefault().create(file);
			if (celement instanceof ITranslationUnit) {
				tunit = (ITranslationUnit) celement;
			}
		} else if (input instanceof ITranslationUnitEditorInput) {
			ITranslationUnitEditorInput editorInput = (ITranslationUnitEditorInput) input;
			tunit = editorInput.getTranslationUnit();
		}

		if (tunit != null) {
			// Choose an editor based on the content type
			String contentTypeId = tunit.getContentTypeId();
			if (contentTypeId != null) {
				IContentType contentType = Platform.getContentTypeManager().getContentType(contentTypeId);
				IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
				IEditorDescriptor desc = registry.getDefaultEditor(input.getName(), contentType);
				if (desc != null) {
					return desc.getId();
				}
			}
			// Choose an editor based on the language (obsolete?)
			if (tunit.isCLanguage()) {
				return CUIPlugin.EDITOR_ID;
			} else if (tunit.isCXXLanguage()) {
				return CUIPlugin.EDITOR_ID;
			} else if (tunit.isASMLanguage()) {
				return "org.eclipse.cdt.ui.editor.asm.AsmEditor"; //$NON-NLS-1$
			}
		}

		// Choose an editor based on filename/extension
		String editorId = getEditorID(input.getName());

		return editorId;
	}

	public static IStorage getStorage(IBinary bin) {
		IStorage store = null;
		try {
			IBuffer buffer = bin.getBuffer();
			if (buffer != null) {
				store = new FileStorage(new ByteArrayInputStream(buffer.getContents().getBytes()), bin.getPath());
			}
		} catch (CModelException e) {
			// nothing;
		}
		return store;
	}

}
