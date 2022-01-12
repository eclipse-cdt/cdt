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
package org.eclipse.cdt.autotools.ui.editors;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.autotools.ui.editors.parser.IAutoconfErrorHandler;
import org.eclipse.cdt.autotools.ui.editors.parser.ParseException;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.quickassist.IQuickFixableAnnotation;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.ui.IEditorInput;

public class AutoconfErrorHandler implements IAutoconfErrorHandler {

	public static final String CDT_ANNOTATION_INFO = "org.eclipse.cdt.ui.info"; //$NON-NLS-1$
	public static final String CDT_ANNOTATION_WARNING = "org.eclipse.cdt.ui.warning"; //$NON-NLS-1$
	public static final String CDT_ANNOTATION_ERROR = "org.eclipse.cdt.ui.error"; //$NON-NLS-1$

	private int CDT_WARNING = 1;
	private int CDT_ERROR = 2;

	private Map<Position, Annotation> annotations = new HashMap<>();
	private AnnotationModel fAnnotationModel;

	public AutoconfErrorHandler(IEditorInput input) {
		this.fAnnotationModel = (AnnotationModel) AutoconfEditor.getAutoconfDocumentProvider()
				.getAnnotationModel(input);
	}

	// TODO: no quickfixes yet implemented, but maybe in the future
	private static class AutoconfAnnotation extends Annotation implements IQuickFixableAnnotation {
		public AutoconfAnnotation(String annotationType, boolean persist, String message) {
			super(annotationType, persist, message);
		}

		@Override
		public void setQuickFixable(boolean state) {
			// do nothing
		}

		@Override
		public boolean isQuickFixableStateSet() {
			return true;
		}

		@Override
		public boolean isQuickFixable() throws AssertionFailedException {
			return false;
		}

	}

	@Override
	public void handleError(ParseException e) {
		Integer charStart = Integer.valueOf(e.getStartOffset());
		Integer charEnd = Integer.valueOf(e.getEndOffset());

		String annotationType = CDT_ANNOTATION_INFO;
		if (e.getSeverity() == CDT_ERROR)
			annotationType = CDT_ANNOTATION_ERROR;
		else if (e.getSeverity() == CDT_WARNING)
			annotationType = CDT_ANNOTATION_WARNING;
		Annotation annotation = new AutoconfAnnotation(annotationType, true, e.getLocalizedMessage());
		Position p = new Position(charStart.intValue(), charEnd.intValue() - charStart.intValue());
		fAnnotationModel.addAnnotation(annotation, p);
		annotations.put(p, annotation);
	}

	public void removeAllExistingMarkers() {
		fAnnotationModel.removeAllAnnotations();
		annotations.clear();
	}

	public void removeExistingMarkers(int offset, int length) {
		Iterator<Annotation> i = fAnnotationModel.getAnnotationIterator();
		while (i.hasNext()) {
			Annotation annotation = i.next();
			Position p = fAnnotationModel.getPosition(annotation);
			int pStart = p.getOffset();
			if (pStart >= offset && pStart < (offset + length)) {
				// Remove directly from model instead of using
				// iterator so position will be removed from document.
				fAnnotationModel.removeAnnotation(annotation);
			}
		}
	}
}
