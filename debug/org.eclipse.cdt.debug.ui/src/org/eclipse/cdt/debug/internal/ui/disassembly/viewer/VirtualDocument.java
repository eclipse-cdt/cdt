/*******************************************************************************
 * Copyright (c) 2008, 2015 ARM Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * ARM Limited - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.disassembly.viewer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import org.eclipse.cdt.debug.ui.disassembly.IDocumentPresentation;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * Converts the model elements into the text content
 */
public class VirtualDocument extends Document {

	public class LinePosition extends Position {

		private int fDistance = 0;

		LinePosition(int offset, int distance) {
			super(offset);
			fDistance = distance;
		}

		LinePosition(int offset, int length, int distance) {
			super(offset, length);
			fDistance = distance;
		}

		int getDistance() {
			return fDistance;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.text.Position#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof LinePosition))
				return false;
			return (getDistance() == ((LinePosition) other).getDistance());
		}
	}

	public final static String CATEGORY_LINE = "category_line"; // "category_line"; //$NON-NLS-1$
	private static final String PENDING_LINE = ".............................."; //$NON-NLS-1$

	private Object fRoot;
	private int fCurrentOffset = 0;

	private IDocumentPresentation fPresentationContext;
	private AnnotationModel fAnnotationModel;
	private DocumentContentProvider fContentProvider;
	private DocumentLabelProvider fLabelProvider;
	private DocumentAnnotationProvider fAnnotationProvider;

	public VirtualDocument(AnnotationModel annotationModel, IDocumentPresentation presentationContext, Object root) {
		super();
		fRoot = root;
		fPresentationContext = presentationContext;
		fAnnotationModel = annotationModel;
		fContentProvider = new DocumentContentProvider(this);
		fLabelProvider = new DocumentLabelProvider(this);
		fAnnotationProvider = new DocumentAnnotationProvider(this);
		getContentProvider().init(fRoot);
	}

	public void dispose() {
		getContentProvider().dispose();
		getLabelProvider().dispose();
		getAnnotationProvider().dispose();
		fRoot = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.AbstractDocument#completeInitialization()
	 */
	@Override
	protected void completeInitialization() {
		super.completeInitialization();
		addPositionCategory(CATEGORY_LINE);
		addPositionUpdater(new DefaultPositionUpdater(CATEGORY_LINE));
	}

	public IDocumentPresentation getPresentationContext() {
		return fPresentationContext;
	}

	public AnnotationModel getAnnotationModel() {
		return fAnnotationModel;
	}

	public DocumentContentProvider getContentProvider() {
		return fContentProvider;
	}

	protected DocumentLabelProvider getLabelProvider() {
		return fLabelProvider;
	}

	protected DocumentAnnotationProvider getAnnotationProvider() {
		return fAnnotationProvider;
	}

	private String createPendingContent(int lineCount, int oldOffset, int offset) {
		int oldLineCount = getNumberOfLines() - 1;
		int intersectStart = Math.max(oldOffset, offset);
		int intersectEnd = Math.min(oldOffset + oldLineCount, offset + lineCount);
		int intersectCount = intersectEnd - intersectStart;
		StringBuilder sb = new StringBuilder();
		int line = 0;
		if (oldOffset > offset) { // scrolling up
			for (int i = 0; i < oldOffset - offset; ++i) {
				try {
					addPosition(CATEGORY_LINE, new LinePosition(sb.length(), offset - i));
					sb.append(PENDING_LINE).append('\n');
					++line;
				} catch (BadLocationException e) {
					// shouldn't happen
				} catch (BadPositionCategoryException e) {
					// shouldn't happen
				}
			}
		} else { // scrolling down
			line += offset - oldOffset;
		}
		for (int i = 0; i < intersectCount; ++i) {
			try {
				IRegion region = getLineInformation(line++);
				sb.append(get(region.getOffset(), region.getLength())).append('\n');
			} catch (BadLocationException e) {
				// shouldn't happen
			}
		}
		// Assuming the offset isn't changed when resizing
		int pendingLines = 0;
		if (oldLineCount < lineCount) { // resizing
			pendingLines = lineCount - oldLineCount;
		} else if (offset > oldOffset) { // scrolling down
			pendingLines = offset - oldOffset;
		}
		for (int i = 0; i < pendingLines; ++i) {
			sb.append(PENDING_LINE).append('\n');
			++line;
		}
		return sb.toString();
	}

	public int getCurrentOffset() {
		return fCurrentOffset;
	}

	public void setCurrentOffset(int offset) {
		fCurrentOffset = offset;
	}

	public Object getElementAtLine(int line) {
		return getContentProvider().getElementAtLine(line);
	}

	public void updateContent(int lineCount, int offset, boolean revealInput) {
		int oldOffset = fCurrentOffset;
		fCurrentOffset = offset;
		removePositions();
		getAnnotationModel().removeAllAnnotations();
		set(createPendingContent(lineCount, oldOffset, offset));
		setPositions();
		getContentProvider().update(getPresentationContext(), lineCount, offset, revealInput);
	}

	protected void updateElement(Object input, int index, Object element) {
		getLabelProvider().update(input, element, index, getPresentationContext());
		getAnnotationProvider().update(getContentProvider().getInput(), element, index, getPresentationContext());
	}

	@SuppressWarnings("rawtypes")
	protected void updateAnnotations(int lineNumber, Annotation[] annotations) {
		IAnnotationModel annotationModel = getAnnotationModel();
		try {
			Position[] positions = getPositions(CATEGORY_LINE);
			if (lineNumber < positions.length) {
				Iterator it = annotationModel.getAnnotationIterator();
				ArrayList<Annotation> oldAnnotations = new ArrayList<>(3);
				while (it.hasNext()) {
					Annotation ann = (Annotation) it.next();
					if (positions[lineNumber].equals(annotationModel.getPosition(ann))) {
						oldAnnotations.add(ann);
					}
				}
				for (Annotation ann : oldAnnotations) {
					annotationModel.removeAnnotation(ann);
				}
				for (Annotation ann : annotations) {
					annotationModel.addAnnotation(ann, positions[lineNumber]);
				}
			}
		} catch (BadPositionCategoryException e) {
		}
	}

	final void labelDone(Object element, int lineNumber, Properties labels) {
		try {
			String line = labels.getProperty(IDocumentPresentation.ATTR_LINE_LABEL);
			IRegion region = getLineInformation(lineNumber);
			if (get(region.getOffset(), region.getLength()).compareTo(line) != 0)
				replace(region.getOffset(), region.getLength(), line);
		} catch (BadLocationException e) {
		}
	}

	protected void removeLine(int lineNumber) {
		try {
			IRegion region = getLineInformation(lineNumber);
			replace(region.getOffset(), region.getLength(), ""); //$NON-NLS-1$
		} catch (BadLocationException e) {
		}
	}

	private void removePositions() {
		try {
			Position[] oldPositions = getPositions(CATEGORY_LINE);
			for (Position p : oldPositions) {
				removePosition(CATEGORY_LINE, p);
			}
		} catch (BadPositionCategoryException e) {
		}
	}

	private void setPositions() {
		try {
			Position[] oldPositions = getPositions(CATEGORY_LINE);
			int offset = getCurrentOffset();
			int lines = getNumberOfLines();
			for (Position p : oldPositions) {
				removePosition(CATEGORY_LINE, p);
			}
			for (int i = 0; i < lines; ++i) {
				IRegion info = getLineInformation(i);
				addPosition(CATEGORY_LINE, new LinePosition(info.getOffset(), info.getLength(), offset + i));
			}
		} catch (BadPositionCategoryException e) {
			// shouldn't happen
		} catch (BadLocationException e) {
			// shouldn't happen
		}
	}
}
