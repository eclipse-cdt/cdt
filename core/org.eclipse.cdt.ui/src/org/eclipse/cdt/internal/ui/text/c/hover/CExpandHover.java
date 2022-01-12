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
 *     William Riley (Renesas) - Adapted for CDT
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.c.hover;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.internal.ui.editor.CDocumentProvider.ProblemAnnotation;
import org.eclipse.cdt.internal.ui.editor.CMarkerAnnotation;
import org.eclipse.cdt.internal.ui.editor.ICAnnotation;
import org.eclipse.cdt.internal.ui.text.c.hover.AnnotationExpansionControl.AnnotationHoverInput;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationAccessExtension;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationPresentation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ImageUtilities;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.AnnotationPreferenceLookup;

/**
 * Originally copied from org.eclipse.jdt.internal.ui.text.java.hover.JavaExpandHover
 * @since 6.1
 */
public class CExpandHover extends AnnotationExpandHover {

	/** Id of CDT Breakpoint annotation type */
	private static final String ANNOTATION_TYPE_BREAKPOINT = "org.eclipse.cdt.debug.core.breakpoint"; //$NON-NLS-1$

	/** Id of the no breakpoint fake annotation */
	public static final String NO_BREAKPOINT_ANNOTATION = "org.eclipse.cdt.internal.ui.NoBreakpointAnnotation"; //$NON-NLS-1$

	private static class NoBreakpointAnnotation extends Annotation implements IAnnotationPresentation {

		public NoBreakpointAnnotation() {
			super(NO_BREAKPOINT_ANNOTATION, false, CHoverMessages.CExpandHover_tooltip_noBreakpoint);
		}

		/*
		 * @see org.eclipse.jface.text.source.IAnnotationPresentation#paint(org.eclipse.swt.graphics.GC, org.eclipse.swt.widgets.Canvas, org.eclipse.swt.graphics.Rectangle)
		 */
		@Override
		public void paint(GC gc, Canvas canvas, Rectangle bounds) {
			// draw affordance so the user know she can click here to get a breakpoint
			Image fImage = CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_PUBLIC_FIELD);
			ImageUtilities.drawImage(fImage, gc, canvas, bounds, SWT.CENTER);
		}

		/*
		 * @see org.eclipse.jface.text.source.IAnnotationPresentation#getLayer()
		 */
		@Override
		public int getLayer() {
			return IAnnotationPresentation.DEFAULT_LAYER;
		}
	}

	private AnnotationPreferenceLookup fLookup = new AnnotationPreferenceLookup();
	private IPreferenceStore fStore = CUIPlugin.getDefault().getCombinedPreferenceStore();

	public CExpandHover(CompositeRuler ruler, IAnnotationAccess access, IDoubleClickListener doubleClickListener) {
		super(ruler, access, doubleClickListener);
	}

	/*
	 * @see org.eclipse.ui.internal.texteditor.AnnotationExpandHover#getHoverInfoForLine(org.eclipse.jface.text.source.ISourceViewer, int)
	 */
	@Override
	protected Object getHoverInfoForLine(final ISourceViewer viewer, final int line) {
		//Use EDITOR_EVALUATE_TEMPORARY_PROBLEMS rather than EDITOR_CORRECTION_INDICATION as EDITOR_CORRECTION_INDICATION is not used in CDT
		final boolean showTemporaryProblems = PreferenceConstants.getPreferenceStore()
				.getBoolean(PreferenceConstants.EDITOR_EVALUATE_TEMPORARY_PROBLEMS);
		IAnnotationModel model = viewer.getAnnotationModel();
		IDocument document = viewer.getDocument();

		if (model == null)
			return null;

		List<Annotation> exact = new ArrayList<>();
		HashMap<Position, Object> messagesAtPosition = new HashMap<>();

		Iterator<Annotation> e = model.getAnnotationIterator();
		while (e.hasNext()) {
			Annotation annotation = e.next();

			if (fAnnotationAccess instanceof IAnnotationAccessExtension)
				if (!((IAnnotationAccessExtension) fAnnotationAccess).isPaintable(annotation))
					continue;

			if (annotation instanceof ICAnnotation && !isIncluded((ICAnnotation) annotation, showTemporaryProblems))
				continue;

			AnnotationPreference pref = fLookup.getAnnotationPreference(annotation);
			if (pref != null) {
				String key = pref.getVerticalRulerPreferenceKey();
				if (key != null && !fStore.getBoolean(key))
					continue;
			}

			Position position = model.getPosition(annotation);
			if (position == null)
				continue;

			if (compareRulerLine(position, document, line) == 1) {

				if (isDuplicateMessage(messagesAtPosition, position, annotation.getText()))
					continue;

				exact.add(annotation);
			}
		}

		sort(exact, model);

		if (exact.size() > 0)
			setLastRulerMouseLocation(viewer, line);

		if (exact.size() > 0) {
			if (!containsBreakpointAnnotation(exact)) {
				//Add dummy annotation last if no breakpoint present
				exact.add(new NoBreakpointAnnotation());
			}
		}

		if (exact.size() <= 1)
			return null;

		AnnotationHoverInput input = new AnnotationHoverInput();
		input.fAnnotations = exact.toArray(new Annotation[0]);
		input.fViewer = viewer;
		input.fRulerInfo = fCompositeRuler;
		input.fAnnotationListener = fgListener;
		input.fDoubleClickListener = fDblClickListener;
		input.redoAction = new AnnotationExpansionControl.ICallback() {

			@Override
			public void run(IInformationControlExtension2 control) {
				control.setInput(getHoverInfoForLine(viewer, line));
			}

		};
		input.model = model;

		return input;
	}

	private boolean isIncluded(ICAnnotation annotation, boolean showTemporaryProblems) {

		// XXX: see https://bugs.eclipse.org/bugs/show_bug.cgi?id=138601
		if (annotation instanceof ProblemAnnotation
				&& CMarkerAnnotation.TASK_ANNOTATION_TYPE.equals(annotation.getType()))
			return false;

		if (!annotation.isProblem())
			return true;

		if (annotation.isMarkedDeleted() && !annotation.hasOverlay())
			return true;

		if (annotation.hasOverlay() && !annotation.isMarkedDeleted())
			return true;

		if (annotation.hasOverlay())
			return (!isIncluded(annotation.getOverlay(), showTemporaryProblems));

		//JDT only shows annotations problems corrections, for CDT show all.
		return showTemporaryProblems;
	}

	@Override
	protected int getAnnotationOffsetForSort(IAnnotationModel model, Annotation a) {
		if (this.isBreakpointAnnotation(a)) {
			return Integer.MAX_VALUE; //Force breakpoints to end
		} else {
			return model.getPosition(a).offset;
		}
	}

	/*
	 * @see org.eclipse.ui.internal.texteditor.AnnotationExpandHover#getOrder(org.eclipse.jface.text.source.Annotation)
	 */
	@Override
	protected int getOrder(Annotation annotation) {
		if (isBreakpointAnnotation(annotation))
			return 0;//Force breakpoints to end. Usability improvement over JDT based on feedback
		else
			return super.getOrder(annotation);
	}

	private boolean isBreakpointAnnotation(Annotation a) {
		return a.getType().equals(ANNOTATION_TYPE_BREAKPOINT);
	}

	private boolean containsBreakpointAnnotation(List<Annotation> annotations) {
		for (Annotation a : annotations) {
			if (isBreakpointAnnotation(a)) {
				return true;
			}
		}

		return false;
	}
}
