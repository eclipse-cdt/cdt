/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.ui;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Utility tools to open editor and highlight the line
 */
public class CodanEditorUtility {
	/**
	 * @param fileUrl - file "url", like file:/tmp/a.c#22
	 * @throws PartInitException
	 * @throws BadLocationException
	 */
	public static void openFileURL(String fileUrl, IResource markerResource) throws PartInitException, BadLocationException {
		String file = getFileFromURL(fileUrl);
		IEditorPart part = openInEditor(file, markerResource);
		int line = getLineFromURL(fileUrl);
		revealLine(part, line);
	}

	/**
	 * Line is the part the follows # in this URL
	 * 
	 * @return -1 if not line found in URL, and line number if there is
	 */
	public static int getLineFromURL(String fileUrl) {
		String sline = fileUrl.replaceAll(".*#(\\d+)$", "$1"); //$NON-NLS-1$ //$NON-NLS-2$
		int line = -1;
		try {
			line = Integer.parseInt(sline);
		} catch (NumberFormatException e2) {
			// no line
		}
		return line;
	}

	public static String getFileFromURL(String link) {
		String file = link.replaceFirst("^file:", ""); //$NON-NLS-1$ //$NON-NLS-2$
		file = file.replaceAll("#\\d+$", ""); //$NON-NLS-1$//$NON-NLS-2$
		return file;
	}

	public static void revealLine(IEditorPart part, int line) throws BadLocationException {
		if (line > 0) {
			if (part instanceof ITextEditor) {
				ITextEditor textEditor = (ITextEditor) part;
				IDocument document = textEditor.getDocumentProvider().getDocument(part.getEditorInput());
				textEditor.selectAndReveal(document.getLineOffset(line - 1), 0);
			}
		}
	}

	public static IEditorPart openInEditor(String file, IResource markerResource) throws PartInitException {
		//		ICElement element = null;
		//		if (markerResource != null)
		//			element = CoreModel.getDefault().create(markerResource);
		IFile efile = null;
		if (markerResource instanceof IFile)
			efile = (IFile) markerResource;
		if (efile != null) {
			IWorkbenchPage page = getActivePage();
			IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file);
			IEditorPart part = page.openEditor(new FileEditorInput(efile), desc.getId());
			return part;
		}
		File fileToOpen = new File(file);
		if (fileToOpen.exists() && fileToOpen.isFile()) {
			IFileStore fileStore = EFS.getLocalFileSystem().getStore(fileToOpen.toURI());
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			try {
				return IDE.openEditorOnFileStore(page, fileStore);
			} catch (PartInitException e) {
				//Put your exception handler here if you wish to
			}
		}
		return null;
	}

	public static IEditorPart openInEditor(IMarker marker) throws PartInitException {
		String href = getLocationHRef(marker);
		String file = getFileFromURL(href);
		return openInEditor(file, marker.getResource());
	}

	public static String getLocationHRef(IMarker marker) {
		String loc = marker.getResource().getLocationURI().toString();
		String loc2 = marker.getAttribute(IMarker.LOCATION, ""); //$NON-NLS-1$
		int line = marker.getAttribute(IMarker.LINE_NUMBER, 0);
		if (loc2.length() > 0) {
			loc = "file:" + loc2.replaceAll("[^:]*: ", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		String href = loc + "#" + line; //$NON-NLS-1$
		return href;
	}

	/**
	 * @param marker
	 * @return
	 * @since 2.0
	 */
	public static String getLocation(IMarker marker) {
		String loc = marker.getResource().getFullPath().toPortableString();
		String loc2 = marker.getAttribute(IMarker.LOCATION, ""); //$NON-NLS-1$
		int line = marker.getAttribute(IMarker.LINE_NUMBER, 0);
		if (loc2.length() > 0) {
			loc = loc2.replaceAll("[^:]*: ", ""); //$NON-NLS-1$ //$NON-NLS-2$ 
		}
		return loc + ":" + line; //$NON-NLS-1$
	}

	/**
	 * @since 2.0
	 */
	public static boolean isResourceOpenInEditor(IResource resource, IEditorPart editor) {
		if (editor == null)
			return false;
		IResource realResource = ResourceUtil.getResource(editor.getEditorInput());
		return resource.equals(realResource);
	}

	/**
	 * @since 2.0
	 */
	public static IEditorPart getActiveEditor() {
		IWorkbenchPage activePage = getActivePage();
		if (activePage == null)
			return null;
		IEditorPart e = activePage.getActiveEditor();
		return e;
	}

	/**
	 * Returns the active workbench page.
	 * @return the active workbench page, or {@code null} if none can be found.
	 * @since 2.1
	 */
	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow == null)
			return null;
		IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
		return activePage;
	}
}
