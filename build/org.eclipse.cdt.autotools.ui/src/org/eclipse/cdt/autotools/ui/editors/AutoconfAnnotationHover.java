/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.editors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.internal.autotools.ui.HTMLPrinter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHoverExtension;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.swt.widgets.Shell;

public class AutoconfAnnotationHover implements IAnnotationHover, IAnnotationHoverExtension {

	/**
	 * Returns the distance to the ruler line.
	 */
	protected int compareRulerLine(Position position, IDocument document, int line) {

		if (position.getOffset() > -1 && position.getLength() > -1) {
			try {
				int markerLine = document.getLineOfOffset(position.getOffset());
				if (line == markerLine)
					return 1;
				if (markerLine <= line && line <= document.getLineOfOffset(position.getOffset() + position.getLength()))
					return 2;
			} catch (BadLocationException x) {
			}
		}

		return 0;
	}

	/**
	 * Selects a set of markers from the two lists. By default, it just returns
	 * the set of exact matches.
	 */
	protected List<Annotation> select(List<Annotation> exactMatch, List<Annotation> including) {
		return exactMatch;
	}

	/**
	 * Returns one marker which includes the ruler's line of activity.
	 */
	protected List<Annotation> getAnnotationsForLine(ISourceViewer viewer, int line) {

		IDocument document = viewer.getDocument();
		IAnnotationModel model = viewer.getAnnotationModel();

		if (model == null)
			return null;

		List<Annotation> exact = new ArrayList<>();
		List<Annotation> including = new ArrayList<>();

		Iterator<?> e = model.getAnnotationIterator();
		while (e.hasNext()) {
			Object o = e.next();
			if (o instanceof Annotation) {
				Annotation a = (Annotation) o;
				switch (compareRulerLine(model.getPosition(a), document, line)) {
				case 1:
					exact.add(a);
					break;
				case 2:
					including.add(a);
					break;
				}
			}
		}

		return select(exact, including);
	}

	@Override
	public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
		List<Annotation> annotations = getAnnotationsForLine(sourceViewer, lineNumber);
		if (annotations != null && annotations.size() > 0) {

			if (annotations.size() == 1) {

				// optimization
				Annotation annotation = annotations.get(0);
				String message = annotation.getText();
				if (message != null && message.trim().length() > 0)
					return formatSingleMessage(message);

			} else {

				List<String> messages = new ArrayList<>();

				Iterator<Annotation> e = annotations.iterator();
				while (e.hasNext()) {
					Annotation annotation = e.next();
					String message = annotation.getText();
					if (message != null && message.trim().length() > 0)
						messages.add(message.trim());
				}

				if (messages.size() == 1)
					return formatSingleMessage(messages.get(0));

				if (messages.size() > 1)
					return formatMultipleMessages(messages);
			}
		}

		return null;
	}

	/*
	 * Formats a message as HTML text.
	 */
	private String formatSingleMessage(String message) {
		StringBuilder buffer = new StringBuilder();
		HTMLPrinter.addPageProlog(buffer);
		HTMLPrinter.addParagraph(buffer, HTMLPrinter.convertToHTMLContent(message));
		HTMLPrinter.addPageEpilog(buffer);
		return buffer.toString();
	}

	/*
	 * Formats several message as HTML text.
	 */
	private String formatMultipleMessages(List<String> messages) {
		StringBuilder buffer = new StringBuilder();
		HTMLPrinter.addPageProlog(buffer);
		HTMLPrinter.addParagraph(buffer, HTMLPrinter
				.convertToHTMLContent(AutoconfEditorMessages.getString("AutoconfAnnotationHover.multipleMarkers"))); //$NON-NLS-1$

		HTMLPrinter.startBulletList(buffer);
		Iterator<String> e = messages.iterator();
		while (e.hasNext())
			HTMLPrinter.addBullet(buffer, HTMLPrinter.convertToHTMLContent(e.next()));
		HTMLPrinter.endBulletList(buffer);

		HTMLPrinter.addPageEpilog(buffer);
		return buffer.toString();
	}

	// IAnnotationHoverExtension members
	// We need to use the extension to get a Hover Control Creator which
	// handles html.

	@Override
	public IInformationControlCreator getHoverControlCreator() {
		return new IInformationControlCreator() {
			@Override
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent, false);
			}
		};
	}

	@Override
	public boolean canHandleMouseCursor() {
		return false;
	}

	@Override
	public ILineRange getHoverLineRange(ISourceViewer viewer, int lineNumber) {
		return new LineRange(lineNumber, 1);
	}

	@Override
	public Object getHoverInfo(ISourceViewer sourceViewer, ILineRange lineRange, int visibleNumberOfLines) {
		return getHoverInfo(sourceViewer, lineRange.getStartLine());
	}

}
