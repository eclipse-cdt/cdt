/*******************************************************************************
 * Copyright (c) 2007, 2015 Red Hat, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.editors.parser;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.autotools.ui.AutotoolsUIPlugin;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.texteditor.MarkerUtilities;

public class AutoconfOutlineErrorHandler {

	public static final String PARSE_ERROR_MARKER_ID = AutotoolsUIPlugin.PLUGIN_ID + ".outlineparsefileerror"; //$NON-NLS-1$

	private IFile file;
	private IDocument document;

	public AutoconfOutlineErrorHandler(IStorageEditorInput input, IDocument document) {
		this.document = document;
		IWorkspaceRoot root = CCorePlugin.getWorkspace().getRoot();
		try {
			IPath absPath = input.getStorage().getFullPath();
			IPath rootPath = root.getLocation();
			IPath relPath = new Path("");

			for (int i = 0; i < rootPath.segmentCount(); ++i) {
				relPath = relPath.append("../"); //$NON-NLS-1$
			}
			relPath = relPath.append(absPath);
			this.file = root.getFileForLocation(relPath);
			if (this.file == null) {
				this.file = root.getFile(relPath);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public IDocument getDocument() {
		return document;
	}

	public void handleError(ParseException e) {
		if (!file.exists())
			return;

		int lineNumber = e.getLineNumber();

		Map<String, Object> map = new HashMap<>();
		MarkerUtilities.setLineNumber(map, lineNumber);
		MarkerUtilities.setMessage(map, e.getMessage());
		map.put(IMarker.MESSAGE, e.getMessage());
		map.put(IMarker.LOCATION, file.getFullPath().toString());

		Integer charStart = getCharOffset(lineNumber, e.getStartColumn());
		if (charStart != null) {
			map.put(IMarker.CHAR_START, charStart);
		}
		Integer charEnd = getCharOffset(lineNumber, e.getEndColumn());
		if (charEnd != null) {
			map.put(IMarker.CHAR_END, charEnd);
		}

		// FIXME:  add severity level
		map.put(IMarker.SEVERITY, Integer.valueOf(e.getSeverity()));

		try {
			MarkerUtilities.createMarker(file, map, PARSE_ERROR_MARKER_ID);
		} catch (CoreException ee) {
			ee.printStackTrace();
		}
		return;
	}

	public void removeAllExistingMarkers() {
		if (!file.exists())
			return;

		try {
			file.deleteMarkers(PARSE_ERROR_MARKER_ID, true, IResource.DEPTH_ZERO);
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
	}

	public void removeExistingMarkers(int offset, int length) {
		if (!file.exists())
			return;

		try {
			IMarker[] markers = file.findMarkers(PARSE_ERROR_MARKER_ID, true, IResource.DEPTH_ZERO);
			// Delete all markers that start in the given document range.
			for (int i = 0; i < markers.length; ++i) {
				IMarker marker = markers[i];
				int charEnd = MarkerUtilities.getCharEnd(marker);
				int charStart = MarkerUtilities.getCharStart(marker);
				if (charStart >= offset && charEnd <= (offset + length))
					marker.delete();
			}
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
	}

	private Integer getCharOffset(int lineNumber, int columnNumber) {
		try {
			return Integer.valueOf(document.getLineOffset(lineNumber) + columnNumber);
		} catch (BadLocationException e) {
			e.printStackTrace();
			return null;
		}
	}

}
