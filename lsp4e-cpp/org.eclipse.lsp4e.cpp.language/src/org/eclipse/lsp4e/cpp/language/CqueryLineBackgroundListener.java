/*******************************************************************************
 * Copyright (c) 2018 Manish Khurana , Nathan Ridge and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.lsp4e.cpp.language;


import java.net.URI;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.lsp4j.Range;

@SuppressWarnings("restriction")
public class CqueryLineBackgroundListener implements LineBackgroundListener {
	private List<Range> inactiveRegions;
	private IDocument currentDocument;
	private URI currentDocumentUri;
	private Color lineBackgroundColor;

	// TODO: Remove mappings if not required
	public static ConcurrentMap<URI, List<Range>> fileInactiveRegionsMap = new ConcurrentHashMap<>(16, 0.75f, 1);

	public void setCurrentDocument(IDocument currentDocument) {
		this.currentDocument = currentDocument;
	}

	@Override
	public void lineGetBackground(LineBackgroundEvent event) {
		lineBackgroundColor  = new Color(Display.getCurrent(), PreferenceConverter.getColor(CUIPlugin.getDefault().getPreferenceStore(), CEditor.INACTIVE_CODE_COLOR));
		if(currentDocument == null) {
			return;
		}
		currentDocumentUri = LSPEclipseUtils.toUri(LSPEclipseUtils.getFile(currentDocument));
		inactiveRegions = fileInactiveRegionsMap.get(currentDocumentUri);

		if (this.inactiveRegions == null) {
			return;
		}

		try {
			for (Range eachInactiveRange : this.inactiveRegions) {
				int regionStartLine = eachInactiveRange.getStart().getLine();
				int regionEndLine = eachInactiveRange.getEnd().getLine();
				if (event.lineOffset >= currentDocument.getLineOffset(regionStartLine)
						&& event.lineOffset <= currentDocument.getLineOffset(regionEndLine)) {
					event.lineBackground = lineBackgroundColor;
					break;
				}
			}
		} catch (BadLocationException e) {
			Activator.log(e);
		}
	}
}
