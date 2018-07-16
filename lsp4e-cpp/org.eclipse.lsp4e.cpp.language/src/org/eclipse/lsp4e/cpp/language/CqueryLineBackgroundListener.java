/*******************************************************************************
 * Copyright (c) 2018 Manish Khurana , Nathan Ridge and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.lsp4e.cpp.language;


import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	public static Map<URI, List<Range>> fileInactiveRegionsMap = new HashMap<>();

	public void setCurrentDocument(IDocument currentDocument) {
		this.currentDocument = currentDocument;
	}

	@Override
	public void lineGetBackground(LineBackgroundEvent event) {
		Color lineBackgroundColor = new Color(Display.getCurrent(), 225, 225, 225);
		currentDocumentUri = LSPEclipseUtils.toUri(LSPEclipseUtils.getFile(currentDocument));
		inactiveRegions = fileInactiveRegionsMap.get(currentDocumentUri);
		try {

			if (this.inactiveRegions == null) {
				return;
			}

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
