/*******************************************************************************
 * $QNXLicenseC:
 * Copyright 2008, QNX Software Systems. All Rights Reserved.
 * 
 * You must obtain a written license from and pay applicable license fees to QNX 
 * Software Systems before you may reproduce, modify or distribute this software, 
 * or any work that includes all or part of this software.   Free development 
 * licenses are available for evaluation and non-commercial purposes.  For more 
 * information visit http://licensing.qnx.com or email licensing@qnx.com.
 *  
 * This file may contain contributions from others.  Please review this entire 
 * file for other proprietary rights or license notices, as well as the QNX 
 * Development Suite License Guide at http://licensing.qnx.com/license-guide/ 
 * for other information.
 * $
 *******************************************************************************/
/*
 * Created by: Elena Laskavaia
 * Created on: 2010-05-05
 * Last modified by: $Author: elaskavaia $
 */
package org.eclipse.cdt.codan.ui;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
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
	public static void openFileURL(String fileUrl, IResource markerResource)
			throws PartInitException, BadLocationException {
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

	public static void revealLine(IEditorPart part, int line)
			throws BadLocationException {
		if (line > 0) {
			if (part instanceof ITextEditor) {
				ITextEditor textEditor = (ITextEditor) part;
				IDocument document = textEditor.getDocumentProvider()
						.getDocument(part.getEditorInput());
				textEditor.selectAndReveal(document.getLineOffset(line - 1), 0);
			}
		}
	}

	@SuppressWarnings("restriction")
	public static IEditorPart openInEditor(String file, IResource markerResource)
			throws PartInitException {
		IPath pfile = new Path(file);
		ICElement element = null;
		if (markerResource != null)
			element = CoreModel.getDefault().create(markerResource);
		IEditorPart part = EditorUtility.openInEditor(pfile, element);
		return part;
	}

	public static IEditorPart openInEditor(IMarker marker)
			throws PartInitException {
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
	 * @since 1.1
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
}
