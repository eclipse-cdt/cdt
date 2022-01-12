/*******************************************************************************
 * Copyright (c) 2000, 2020 QNX Software Systems and others.
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
 *     Alexander Fedorov (ArSysOp) - Bug 561993 - Remove dependency to com.ibm.icu from CDT UI
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.internal.core.model.ExternalTranslationUnit;
import org.eclipse.cdt.internal.core.model.Include;
import org.eclipse.cdt.internal.core.model.WorkingCopy;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.cdt.internal.ui.ICStatusConstants;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.CEditorMessages;
import org.eclipse.cdt.internal.ui.editor.ITranslationUnitEditorInput;
import org.eclipse.cdt.internal.ui.text.LineComparator;
import org.eclipse.cdt.ui.CDTUITools;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.UNCPathConverter;
import org.eclipse.compare.rangedifferencer.IRangeComparator;
import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifferencer;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class EditorUtility {
	/**
	 * The ID of the default text editor
	 */
	public static final String DEFAULT_TEXT_EDITOR_ID = EditorsUI.DEFAULT_TEXT_EDITOR_ID;

	private EditorUtility() {
	}

	/**
	 * Tests if a cu is currently shown in an editor
	 * @return the IEditorPart if shown, null if element is not open in an editor
	 */
	public static IEditorPart isOpenInEditor(Object inputElement) {
		IEditorInput input = null;

		try {
			input = getEditorInput(inputElement);
		} catch (CModelException x) {
			//CUIPlugin.log(x.getStatus());
		}

		if (input != null) {
			IWorkbenchPage p = CUIPlugin.getActivePage();
			if (p != null) {
				return p.findEditor(input);
			}
		}

		return null;
	}

	/**
	 * Opens an editor for an element such as {@code ICElement},
	 * {@code IFile}, or {@code IStorage}.
	 * The editor is activated by default.
	 * @return the IEditorPart or null if wrong element type or opening failed
	 */
	public static IEditorPart openInEditor(Object inputElement) throws CModelException, PartInitException {
		return openInEditor(inputElement, true);
	}

	/**
	 * Opens an editor for an element (ICElement, IFile, IStorage...)
	 * @return the IEditorPart or null if wrong element type or opening failed
	 */
	public static IEditorPart openInEditor(Object inputElement, boolean activate)
			throws CModelException, PartInitException {
		if (inputElement instanceof IFile) {
			return openInEditor((IFile) inputElement, activate);
		}

		IEditorInput input = getEditorInput(inputElement);

		if (input != null) {
			return openInEditor(input, getEditorID(input, inputElement), activate);
		}

		return null;
	}

	/**
	 * Selects a C Element in an editor
	 */
	public static void revealInEditor(IEditorPart part, ICElement element) {
		if (element == null) {
			return;
		}
		if (part instanceof CEditor) {
			((CEditor) part).setSelection(element);
		} else if (part instanceof ITextEditor) {
			if (element instanceof ISourceReference && !(element instanceof ITranslationUnit)) {
				ISourceReference reference = (ISourceReference) element;
				try {
					ISourceRange range = reference.getSourceRange();
					((ITextEditor) part).selectAndReveal(range.getIdStartPos(), range.getIdLength());
				} catch (CModelException exc) {
					CUIPlugin.log(exc.getStatus());
				}
			}
		}
	}

	private static IEditorPart openInEditor(IFile file, boolean activate) throws PartInitException {
		if (file == null)
			return null;
		if (!file.getProject().isAccessible()) {
			closedProject(file.getProject());
			return null;
		}

		try {
			if (!file.isLinked(IResource.CHECK_ANCESTORS)) {
				File tempFile = file.getRawLocation().toFile();

				if (tempFile != null) {
					String canonicalPath = null;
					try {
						canonicalPath = tempFile.getCanonicalPath();
					} catch (IOException e) {
					}

					if (canonicalPath != null) {
						IPath path = new Path(canonicalPath);
						file = CUIPlugin.getWorkspace().getRoot().getFileForLocation(path);
					}
				}
			}

			IEditorInput input = getEditorInput(file);
			if (input != null) {
				return openInEditor(input, getEditorID(input, file), activate);
			}
		} catch (CModelException e) {
		}
		return null;
	}

	/**
	 * @deprecated use IResource.isLinked(IResource.CHECK_ANCESTORS) instead.
	 */
	@Deprecated
	public static boolean isLinked(IFile file) {
		if (file.isLinked())
			return true;

		IPath path = file.getLocation();

		while (path.segmentCount() > 0) {
			path = path.removeLastSegments(1);
			IContainer[] containers = ResourcesPlugin.getWorkspace().getRoot().findContainersForLocation(path);

			for (IContainer container : containers) {
				if (container instanceof IFolder && ((IFolder) container).isLinked()) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Open error dialog about closed project.
	 * @param project
	 */
	private static void closedProject(IProject project) {
		MessageBox errorMsg = new MessageBox(CUIPlugin.getActiveWorkbenchShell(), SWT.ICON_ERROR | SWT.OK);
		errorMsg.setText(CUIPlugin.getResourceString("EditorUtility.closedproject")); //$NON-NLS-1$
		String desc = CUIPlugin.getResourceString("Editorutility.closedproject.description"); //$NON-NLS-1$
		errorMsg.setMessage(MessageFormat.format(desc, new Object[] { project.getName() }));
		errorMsg.open();
	}

	private static IEditorPart openInEditor(IEditorInput input, String editorID, boolean activate)
			throws PartInitException {
		if (input != null) {
			IWorkbenchPage p = CUIPlugin.getActivePage();
			if (p != null) {
				IEditorPart editorPart = p.openEditor(input, editorID, activate);
				return editorPart;
			}
		}
		return null;
	}

	private static IEditorInput getEditorInput(ICElement element) throws CModelException {
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
				return new ExternalEditorInput(unit);
			}

			if (element instanceof IBinary) {
				IResource resource = element.getResource();
				if (resource instanceof IFile) {
					return new FileEditorInput((IFile) resource);
				}
			}

			element = element.getParent();
		}

		return null;
	}

	public static IEditorInput getEditorInput(Object input) throws CModelException {
		if (input instanceof ICElement) {
			return getEditorInput((ICElement) input);
		}
		if (input instanceof IFile) {
			return new FileEditorInput((IFile) input);
		}
		if (input instanceof IStorage) {
			final IPath location = ((IStorage) input).getFullPath();
			if (location != null) {
				return new ExternalEditorInput(location);
			}
		}
		return null;
	}

	/**
	 * Utility method to open an editor for the given file system location
	 * using {@link #getEditorInputForLocation(IPath, ICElement)} to create
	 * the editor input.
	 *
	 * @param location  a file system location
	 * @param element  an element related to the target file, may be {@code null}
	 * @throws PartInitException
	 */
	public static IEditorPart openInEditor(IPath location, ICElement element) throws PartInitException {
		return openInEditor(location, element, true);
	}

	public static IEditorPart openInEditor(IPath location, ICElement element, boolean activate)
			throws PartInitException {
		IEditorInput input = getEditorInputForLocation(location, element);
		return EditorUtility.openInEditor(input, getEditorID(input, element), activate);
	}

	public static IEditorPart openInEditor(URI locationURI, ICElement element) throws PartInitException {
		IEditorInput input = getEditorInputForLocation(locationURI, element);
		return EditorUtility.openInEditor(input, getEditorID(input, element), true);
	}

	/**
	 * Utility method to get an editor input for the given file system location.
	 * If the location denotes a workspace file, a {@code FileEditorInput}
	 * is returned, otherwise, the input is an {@code IURIEditorInput}
	 * assuming the location points to an existing file in the file system.
	 * The {@code ICElement} is used to determine the associated project
	 * in case the location can not be resolved to a workspace {@code IFile}.
	 *
	 * @param locationURI  a valid file system location
	 * @param context  an element related to the target file, may be {@code null}
	 * @return an editor input
	 */
	public static IEditorInput getEditorInputForLocation(URI locationURI, ICElement context) {
		IFile resource = getWorkspaceFileAtLocation(locationURI, context);
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
						// crecoskie test
						// TODO FIXME
						// include entries don't handle URIs yet, so fake it out for now
						IPath path = URIUtil.toPath(locationURI);
						if (path == null)
							path = new Path(locationURI.getPath());

						if (includeReferences[j].isOnIncludeEntry(path)) {
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
				ITranslationUnit unit = CoreModel.getDefault().createTranslationUnitFrom(cproject, locationURI);
				if (unit != null) {
					IFileStore fileStore = null;
					try {
						fileStore = EFS.getStore(locationURI);
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					}

					if (fileStore != null)
						return new ExternalEditorInput(unit);
				}
				// no translation unit - still try to get a sensible marker resource
				// from the associated element
				IResource markerResource = cproject.getProject();
				return new ExternalEditorInput(locationURI, markerResource);
			}
		}
		return new ExternalEditorInput(locationURI);
	}

	public static IEditorInput getEditorInputForLocation(IPath location, ICElement context) {
		IFile resource = getWorkspaceFileAtLocation(location, context);
		if (resource != null) {
			return new FileEditorInput(resource);
		}
		if (location.isUNC()) {
			return getEditorInputForLocation(UNCPathConverter.getInstance().toURI(location), context);
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
				if (unit == null && (context instanceof Include) && location.toFile().exists()) {
					ICElement parent = context.getParent();
					if (parent instanceof WorkingCopy) {
						WorkingCopy copy = (WorkingCopy) parent;
						if (copy.isCLanguage()) {
							unit = new ExternalTranslationUnit(cproject, URIUtil.toURI(location),
									CCorePlugin.CONTENT_TYPE_CHEADER);
						} else if (copy.isCXXLanguage()) {
							unit = new ExternalTranslationUnit(cproject, URIUtil.toURI(location),
									CCorePlugin.CONTENT_TYPE_CXXHEADER);
						}
					}
				}
				if (unit != null) {
					return new ExternalEditorInput(unit);
				}
				// no translation unit - still try to get a sensible marker resource
				// from the associated element
				IResource markerResource = cproject.getProject();
				return new ExternalEditorInput(location, markerResource);
			}
		}
		return new ExternalEditorInput(location);
	}

	/**
	 * Utility method to resolve a file system location to a workspace resource.
	 * If a context element is given and there are multiple matches in the workspace,
	 * a resource with the same project of the context element are preferred.
	 *
	 * @param location  a valid file system location
	 * @param context  an element related to the target file, may be {@code null}
	 * @return an {@code IFile} or {@code null}
	 */
	public static IFile getWorkspaceFileAtLocation(IPath location, ICElement context) {
		IProject project = null;
		if (context != null) {
			ICProject cProject = context.getCProject();
			if (cProject != null) {
				project = cProject.getProject();
			}
		}
		IFile file = ResourceLookup.selectFileForLocation(location, project);
		if (file != null && file.isAccessible())
			return file;

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		// workaround http://bugs.eclipse.org/233939
		file = root.getFileForLocation(location);
		if (file != null && file.isAccessible())
			return file;

		// try workspace relative path
		if (location.segmentCount() >= 2) {
			// @see IContainer#getFile for the required number of segments
			file = root.getFile(location);
			if (file != null && file.isAccessible())
				return file;
		}
		return null;
	}

	/**
	 * Utility method to resolve a file system location to a workspace resource.
	 * If a context element is given and there are multiple matches in the workspace,
	 * a resource with the same project of the context element are preferred.
	 *
	 * @param locationURI  a valid Eclipse file system URI
	 * @param context  an element related to the target file, may be {@code null}
	 * @return an {@code IFile} or {@code null}
	 */
	public static IFile getWorkspaceFileAtLocation(URI locationURI, ICElement context) {
		IProject project = null;
		if (context != null) {
			ICProject cProject = context.getCProject();
			if (cProject != null) {
				project = cProject.getProject();
			}
		}

		IFile file = ResourceLookup.selectFileForLocationURI(locationURI, project);
		if (file != null && file.isAccessible())
			return file;

		return null;
	}

	/**
	 * If the current active editor edits a c element return it, else
	 * return null
	 */
	public static ICElement getActiveEditorCInput() {
		IWorkbenchPage page = CUIPlugin.getActivePage();
		if (page != null) {
			IEditorPart part = page.getActiveEditor();
			if (part != null) {
				return getEditorInputCElement(part);
			}
		}
		return null;
	}

	public static ICElement getEditorInputCElement(IEditorPart part) {
		IEditorInput editorInput = part.getEditorInput();
		if (editorInput == null) {
			return null;
		}
		return editorInput.getAdapter(ICElement.class);
	}

	/**
	 * Gets the working copy of an translation unit opened in an editor
	 *
	 * @param tu the original translation unit (or another working copy)
	 * @return the working copy of the translation unit, or null if not found
	*/
	public static ITranslationUnit getWorkingCopy(ITranslationUnit tu) {
		if (tu == null)
			return null;
		if (tu.isWorkingCopy())
			return tu;

		return CDTUITools.getWorkingCopyManager().findSharedWorkingCopy(tu);
	}

	/**
	 * Determine the editor id from the given file name using
	 * the workspace-wide content-type definitions.
	 *
	 * @param name  the file name
	 * @return a valid editor id, never {@code null}
	 */
	public static String getEditorID(String name) {
		try {
			IEditorDescriptor descriptor = IDE.getEditorDescriptor(name, true, false);
			if (descriptor != null) {
				return descriptor.getId();
			}
		} catch (PartInitException exc) {
			// ignore
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
	 * @param inputObject  the input object (used to create the editor input) or {@code null}
	 * @return a valid editor id, never {@code null}
	 */
	public static String getEditorID(IEditorInput input, Object inputObject) {
		ICElement cElement = null;
		if (input instanceof IFileEditorInput) {
			IFileEditorInput editorInput = (IFileEditorInput) input;
			IFile file = editorInput.getFile();
			// Try file specific editor.
			try {
				String editorID = file.getPersistentProperty(IDE.EDITOR_KEY);
				if (editorID != null) {
					IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
					IEditorDescriptor desc = registry.findEditor(editorID);
					if (desc != null) {
						return editorID;
					}
				}
			} catch (CoreException e) {
				// do nothing
			}
			cElement = CoreModel.getDefault().create(file);
		} else if (input instanceof ITranslationUnitEditorInput) {
			ITranslationUnitEditorInput editorInput = (ITranslationUnitEditorInput) input;
			cElement = editorInput.getTranslationUnit();
			if (cElement == null && input instanceof ExternalEditorInput && inputObject instanceof ICElement) {
				cElement = ((ICElement) inputObject).getAncestor(ICElement.C_UNIT);
			}
		} else if (inputObject instanceof ICElement) {
			cElement = (ICElement) inputObject;
		}

		// Choose an editor based on the content type
		IContentType contentType = null;
		if (cElement instanceof ITranslationUnit) {
			String contentTypeId = ((ITranslationUnit) cElement).getContentTypeId();
			if (contentTypeId != null) {
				contentType = Platform.getContentTypeManager().getContentType(contentTypeId);
			}
		}
		if (contentType == null) {
			IProject project = null;
			if (cElement != null) {
				project = cElement.getCProject().getProject();
			} else {
				IFile file = ResourceUtil.getFile(input);
				if (file != null) {
					project = file.getProject();
				}
			}
			contentType = CCorePlugin.getContentType(project, input.getName());
		}
		// handle binary files without content-type (e.g. executables without extension)
		if (contentType == null && cElement != null) {
			final int elementType = cElement.getElementType();
			if (elementType == ICElement.C_ARCHIVE || elementType == ICElement.C_BINARY) {
				contentType = Platform.getContentTypeManager().getContentType(CCorePlugin.CONTENT_TYPE_BINARYFILE);
			}
		}
		IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
		IEditorDescriptor desc = registry.getDefaultEditor(input.getName(), contentType);
		if (desc != null) {
			String editorID = desc.getId();
			if (input instanceof IFileEditorInput) {
				IFile file = ((IFileEditorInput) input).getFile();
				IDE.setDefaultEditor(file, editorID);
			}
			return editorID;
		}

		return DEFAULT_TEXT_EDITOR_ID;
	}

	/**
	 * Maps the localized modifier name to a code in the same
	 * manner as #findModifier.
	 *
	 * @return the SWT modifier bit, or {@code 0} if no match was found
	 */
	public static int findLocalizedModifier(String token) {
		if (token == null)
			return 0;

		if (token.equalsIgnoreCase(Action.findModifierString(SWT.CTRL)))
			return SWT.CTRL;
		if (token.equalsIgnoreCase(Action.findModifierString(SWT.SHIFT)))
			return SWT.SHIFT;
		if (token.equalsIgnoreCase(Action.findModifierString(SWT.ALT)))
			return SWT.ALT;
		if (token.equalsIgnoreCase(Action.findModifierString(SWT.COMMAND)))
			return SWT.COMMAND;

		return 0;
	}

	/**
	 * Returns the modifier string for the given SWT modifier
	 * modifier bits.
	 *
	 * @param stateMask	the SWT modifier bits
	 * @return the modifier string
	 * @since 2.1.1
	 */
	public static String getModifierString(int stateMask) {
		String modifierString = ""; //$NON-NLS-1$
		if ((stateMask & SWT.CTRL) == SWT.CTRL)
			modifierString = appendModifierString(modifierString, SWT.CTRL);
		if ((stateMask & SWT.ALT) == SWT.ALT)
			modifierString = appendModifierString(modifierString, SWT.ALT);
		if ((stateMask & SWT.SHIFT) == SWT.SHIFT)
			modifierString = appendModifierString(modifierString, SWT.SHIFT);
		if ((stateMask & SWT.COMMAND) == SWT.COMMAND)
			modifierString = appendModifierString(modifierString, SWT.COMMAND);

		return modifierString;
	}

	/**
	 * Appends to modifier string of the given SWT modifier bit
	 * to the given modifierString.
	 *
	 * @param modifierString	the modifier string
	 * @param modifier			an int with SWT modifier bit
	 * @return the concatenated modifier string
	 * @since 2.1.1
	 */
	private static String appendModifierString(String modifierString, int modifier) {
		if (modifierString == null)
			modifierString = ""; //$NON-NLS-1$
		String newModifierString = Action.findModifierString(modifier);
		if (modifierString.length() == 0)
			return newModifierString;
		return NLS.bind(CEditorMessages.EditorUtility_concatModifierStrings,
				new String[] { modifierString, newModifierString });
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

	/**
	 * Returns the C project for a given editor input or {@code null} if no corresponding
	 * C project exists.
	 *
	 * @param input the editor input
	 * @return the corresponding C project
	 *
	 * @since 5.0
	 */
	public static ICProject getCProject(IEditorInput input) {
		ICProject cProject = null;
		if (input instanceof IFileEditorInput) {
			IProject project = ((IFileEditorInput) input).getFile().getProject();
			if (project != null) {
				cProject = CoreModel.getDefault().create(project);
				if (!cProject.exists())
					cProject = null;
			}
		} else if (input instanceof ITranslationUnitEditorInput) {
			final ITranslationUnit tu = ((ITranslationUnitEditorInput) input).getTranslationUnit();
			if (tu != null) {
				cProject = tu.getCProject();
			} else if (input instanceof ExternalEditorInput) {
				IResource resource = ((ExternalEditorInput) input).getMarkerResource();
				if (resource instanceof IProject) {
					cProject = CoreModel.getDefault().create((IProject) resource);
				}
			}
		}
		return cProject;
	}

	/**
	 * Returns an array of all editors that have an unsaved content. If the identical content is
	 * presented in more than one editor, only one of those editor parts is part of the result.
	 * @param skipNonResourceEditors if {@code true}, editors whose inputs do not adapt to {@link IResource}
	 * are not saved
	 *
	 * @return an array of dirty editor parts
	 * @since 5.3
	 */
	public static IEditorPart[] getDirtyEditors(boolean skipNonResourceEditors) {
		Set<IEditorInput> inputs = new HashSet<>();
		List<IEditorPart> result = new ArrayList<>();
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
		for (IWorkbenchWindow window : windows) {
			IWorkbenchPage[] pages = window.getPages();
			for (IWorkbenchPage page : pages) {
				IEditorPart[] editors = page.getDirtyEditors();
				for (IEditorPart ep : editors) {
					IEditorInput input = ep.getEditorInput();
					if (inputs.add(input)) {
						if (!skipNonResourceEditors || isResourceEditorInput(input)) {
							result.add(ep);
						}
					}
				}
			}
		}
		return result.toArray(new IEditorPart[result.size()]);
	}

	private static boolean isResourceEditorInput(IEditorInput input) {
		if (input instanceof MultiEditorInput) {
			IEditorInput[] inputs = ((MultiEditorInput) input).getInput();
			for (IEditorInput input2 : inputs) {
				if (input2.getAdapter(IResource.class) != null) {
					return true;
				}
			}
		} else if (input.getAdapter(IResource.class) != null) {
			return true;
		}
		return false;
	}

	/**
	 * Returns the editors to save before performing global C-related
	 * operations.
	 *
	 * @param saveUnknownEditors {@code true} iff editors with unknown buffer management should also be saved
	 * @return the editors to save
	 * @since 5.3
	 */
	public static IEditorPart[] getDirtyEditorsToSave(boolean saveUnknownEditors) {
		Set<IEditorInput> inputs = new HashSet<>();
		List<IEditorPart> result = new ArrayList<>();
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
		for (IWorkbenchWindow window : windows) {
			IWorkbenchPage[] pages = window.getPages();
			for (IWorkbenchPage page : pages) {
				IEditorPart[] editors = page.getDirtyEditors();
				for (IEditorPart editor : editors) {
					IEditorPart ep = editor;
					IEditorInput input = ep.getEditorInput();
					if (!mustSaveDirtyEditor(ep, input, saveUnknownEditors))
						continue;

					if (inputs.add(input))
						result.add(ep);
				}
			}
		}
		return result.toArray(new IEditorPart[result.size()]);
	}

	private static boolean mustSaveDirtyEditor(IEditorPart ep, IEditorInput input, boolean saveUnknownEditors) {
		/*
		 * Goal: save all editors that could interfere with refactoring operations.
		 *
		 * Always save all editors for translation units that are not working copies.
		 * (Leaving them dirty would cause problems, since the file buffer could have been
		 * modified but the C model is not reconciled.)
		 *
		 * If {@code saveUnknownEditors} is {@code true}, save all editors
		 * whose implementation is probably not based on file buffers.
		 */
		IResource resource = input.getAdapter(IResource.class);
		if (resource == null)
			return saveUnknownEditors;

		ICElement element = CCorePlugin.getDefault().getCoreModel().create(resource);
		if (element instanceof ITranslationUnit) {
			ITranslationUnit tu = (ITranslationUnit) element;
			if (getWorkingCopy(tu) == null) {
				return true;
			}
		}

		ITextEditor textEditor = getTextEditor(ep);
		if (textEditor == null)
			return saveUnknownEditors;

		IDocumentProvider documentProvider = textEditor.getDocumentProvider();
		if (!(documentProvider instanceof TextFileDocumentProvider))
			return saveUnknownEditors;

		return false;
	}

	/**
	 * Return the regions of all lines which have changed in the given buffer since the
	 * last save occurred. Each region in the result spans over the size of at least one line.
	 * If successive lines have changed a region spans over the size of all successive lines.
	 * The regions include line delimiters.
	 *
	 * @param buffer the buffer to compare contents from
	 * @param monitor to report progress to
	 * @return the regions of the changed lines
	 * @throws CoreException
	 * @since 5.1
	 */
	public static IRegion[] calculateChangedLineRegions(final ITextFileBuffer buffer, final IProgressMonitor monitor)
			throws CoreException {
		final IRegion[][] result = new IRegion[1][];
		final IStatus[] errorStatus = new IStatus[] { Status.OK_STATUS };

		try {
			SafeRunner.run(new ISafeRunnable() {
				@Override
				public void handleException(Throwable exception) {
					CUIPlugin.log(new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID,
							ICStatusConstants.EDITOR_CHANGED_REGION_CALCULATION, exception.getLocalizedMessage(),
							exception));
					String msg = Messages.EditorUtility_error_calculatingChangedRegions;
					errorStatus[0] = new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID,
							ICStatusConstants.EDITOR_CHANGED_REGION_CALCULATION, msg, exception);
					result[0] = null;
				}

				@Override
				public void run() throws Exception {
					SubMonitor progress = SubMonitor.convert(monitor,
							Messages.EditorUtility_calculatingChangedRegions_message, 4);
					IFileStore fileStore = buffer.getFileStore();

					ITextFileBufferManager fileBufferManager = FileBuffers.createTextFileBufferManager();
					fileBufferManager.connectFileStore(fileStore, progress.split(3));
					try {
						IDocument currentDocument = buffer.getDocument();
						IDocument oldDocument = ((ITextFileBuffer) fileBufferManager.getFileStoreFileBuffer(fileStore))
								.getDocument();

						result[0] = getChangedLineRegions(oldDocument, currentDocument);
					} finally {
						fileBufferManager.disconnectFileStore(fileStore, progress.split(1));
					}
				}

				/**
				 * Return regions of all lines which differ comparing {@code oldDocument}s content
				 * with {@code currentDocument}s content. Successive lines are merged into one region.
				 *
				 * @param oldDocument a document containing the old content
				 * @param currentDocument a document containing the current content
				 * @return the changed regions
				 * @throws BadLocationException
				 */
				private IRegion[] getChangedLineRegions(IDocument oldDocument, IDocument currentDocument) {
					/*
					 * Do not change the type of those local variables. We use Object
					 * here in order to prevent loading of the Compare plug-in at load
					 * time of this class.
					 */
					Object leftSide = new LineComparator(oldDocument);
					Object rightSide = new LineComparator(currentDocument);

					RangeDifference[] differences = RangeDifferencer.findDifferences((IRangeComparator) leftSide,
							(IRangeComparator) rightSide);

					// It holds that:
					// 1. Ranges are sorted:
					//     forAll r1,r2 element differences: indexOf(r1) < indexOf(r2) -> r1.rightStart() < r2.rightStart();
					// 2. Successive changed lines are merged into on RangeDifference
					//     forAll r1,r2 element differences: r1.rightStart() < r2.rightStart() -> r1.rightEnd() < r2.rightStart

					List<IRegion> regions = new ArrayList<>();
					final int numberOfLines = currentDocument.getNumberOfLines();
					for (RangeDifference curr : differences) {
						if (curr.kind() == RangeDifference.CHANGE) {
							int startLine = Math.min(curr.rightStart(), numberOfLines - 1);
							int endLine = curr.rightEnd() - 1;

							IRegion startLineRegion;
							try {
								startLineRegion = currentDocument.getLineInformation(startLine);
								if (startLine >= endLine) {
									// startLine > endLine indicates a deletion of one or more lines.
									// Deletions are ignored except at the end of the document.
									if (startLine == endLine || startLineRegion.getOffset()
											+ startLineRegion.getLength() == currentDocument.getLength()) {
										regions.add(startLineRegion);
									}
								} else {
									IRegion endLineRegion = currentDocument.getLineInformation(endLine);
									int startOffset = startLineRegion.getOffset();
									int endOffset = endLineRegion.getOffset() + endLineRegion.getLength();
									regions.add(new Region(startOffset, endOffset - startOffset));
								}
							} catch (BadLocationException e) {
								CUIPlugin.log(e);
							}
						}
					}

					return regions.toArray(new IRegion[regions.size()]);
				}
			});
		} finally {
			if (!errorStatus[0].isOK())
				throw new CoreException(errorStatus[0]);
		}

		return result[0];
	}

	/**
	 * Returns the project contains the resource, which is currently open in the active editor.
	 * If the active part is no ITextEditor or if the editorInput is no FileEditorInput,
	 * {@code null} is returned.
	 *
	 * @return the project which the selected editor input belongs to or null
	 */
	public static IProject getProjectForActiveEditor() {
		IProject project = null;
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage activePage = window.getActivePage();
			if (activePage != null) {
				ITextEditor activeEditor = getTextEditor(activePage.getActiveEditor());
				if (activeEditor != null) {
					IEditorInput editorInput = activeEditor.getEditorInput();
					IFile file = ResourceUtil.getFile(editorInput);
					if (file != null) {
						project = file.getProject();
					}
				}
			}
		}
		return project;
	}

	/**
	 * Tries to convert the given editor to an implementation of ITextEditor.  Returns that implementation
	 * if possible and null otherwise.
	 *
	 * @param editor The editor to be converted or null if there is nothing to convert.
	 */
	public static ITextEditor getTextEditor(IEditorPart editor) {
		return editor == null ? null : editor.getAdapter(ITextEditor.class);
	}
}
