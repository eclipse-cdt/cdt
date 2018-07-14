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
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.lsp4j.Range;

public class CqueryLineBackgroundPainter implements LineBackgroundListener {
	private List<Range> inactiveRegions;
	private ITextViewer textViewer;
	public static Map<URI, CqueryLineBackgroundPainter> fileLineBackgroundPainterMap = new HashMap<>();

	public void setTextViewer(ITextViewer textViewer) {
		this.textViewer = textViewer;
	}

	public void setInactiveRegions(List<Range> regions) {
		this.inactiveRegions = regions;
	}

	@Override
	public void lineGetBackground(LineBackgroundEvent event) {
		Color lineBackgroundColor = new Color(Display.getCurrent(), 225, 225, 225);
		IDocument doc = textViewer.getDocument();

		fileLineBackgroundPainterMap.put(LSPEclipseUtils.toUri(LSPEclipseUtils.getFile(textViewer.getDocument())), this);

		try {

			if (this.inactiveRegions == null) {
				return;
			}

			for (Range eachInactiveRange : this.inactiveRegions) {
				int regionStartLine = eachInactiveRange.getStart().getLine();
				int regionEndLine = eachInactiveRange.getEnd().getLine();
				if (event.lineOffset >= doc.getLineOffset(regionStartLine)
						&& event.lineOffset <= doc.getLineOffset(regionEndLine)) {
					event.lineBackground = lineBackgroundColor;
				}
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

}
