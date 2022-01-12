/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *     David Perryman (IPL Information Processing Limited)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.correction;

import java.util.Comparator;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.TreeMap;

import org.eclipse.cdt.internal.ui.editor.OverrideIndicatorManager;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationAccessExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.AnnotationPreferenceLookup;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorExtension;
import org.eclipse.ui.texteditor.SelectMarkerRulerAction;

/**
 * Action which gets triggered when selecting (annotations) in the vertical ruler.
 * based upon org.eclipse.jdt.internal.ui.javaeditor.JavaSelectMarkerRulerAction
 */
public class CSelectAnnotationRulerAction extends SelectMarkerRulerAction {

	private ITextEditor fTextEditor;
	private Position fPosition;
	private AnnotationPreferenceLookup fAnnotationPreferenceLookup;
	private IPreferenceStore fStore;
	private ResourceBundle fBundle;
	// Annotations at the ruler's current line of activity, keyed by their presentation layer,
	// in decreasing order (i.e. top to bottom).
	private static Comparator<Integer> decreasingOrder = new Comparator<Integer>() {
		@Override
		public int compare(Integer a, Integer b) {
			return b - a;
		}
	};
	private TreeMap<Integer, Annotation> fAnnotations = new TreeMap<>(decreasingOrder);
	// For each layer, whether the annotation at that layer has a correction.
	private TreeMap<Integer, Boolean> fHasCorrection = new TreeMap<>(decreasingOrder);

	public CSelectAnnotationRulerAction(ResourceBundle bundle, String prefix, ITextEditor editor,
			IVerticalRulerInfo ruler) {
		super(bundle, prefix, editor, ruler);
		fBundle = bundle;
		fTextEditor = editor;

		fAnnotationPreferenceLookup = EditorsUI.getAnnotationPreferenceLookup();
		fStore = CUIPlugin.getDefault().getCombinedPreferenceStore();
	}

	@Override
	public void run() {
		// is there an equivalent preference for the C Editor?
		// if (fStore.getBoolean(PreferenceConstants.EDITOR_ANNOTATION_ROLL_OVER))
		//     return;

		runWithEvent(null);
	}

	/*
	 * @see org.eclipse.jface.action.IAction#runWithEvent(org.eclipse.swt.widgets.Event)
	 */
	@Override
	public void runWithEvent(Event event) {
		// Give each annotation at the current line, from top to bottom, a chance to handle
		// the action. If there are no takers, fall back to the super class implementation.
		for (Integer layer : fAnnotations.keySet()) {
			Annotation annotation = fAnnotations.get(layer);
			if (annotation instanceof OverrideIndicatorManager.OverrideIndicator) {
				((OverrideIndicatorManager.OverrideIndicator) annotation).open();
				return;
			}

			if (fHasCorrection.get(layer)) {
				ITextOperationTarget operation = fTextEditor.getAdapter(ITextOperationTarget.class);
				final int opCode = ISourceViewer.QUICK_ASSIST;
				if (operation != null && operation.canDoOperation(opCode)) {
					fTextEditor.selectAndReveal(fPosition.getOffset(), fPosition.getLength());
					operation.doOperation(opCode);
				}
				return;
			}
		}

		super.run();
	}

	@Override
	public void update() {
		findCAnnotation();
		setEnabled(true);

		for (Integer layer : fAnnotations.keySet()) {
			Annotation annotation = fAnnotations.get(layer);
			if (annotation instanceof OverrideIndicatorManager.OverrideIndicator) {
				initialize(fBundle, "CSelectAnnotationRulerAction.OpenSuperImplementation."); //$NON-NLS-1$
				return;
			}
			if (fHasCorrection.get(layer)) {
				initialize(fBundle, "CSelectAnnotationRulerAction.QuickFix."); //$NON-NLS-1$
				return;
			}
		}

		initialize(fBundle, "CSelectAnnotationRulerAction.GotoAnnotation."); //$NON-NLS-1$;
		super.update();
	}

	private void findCAnnotation() {
		fPosition = null;
		fAnnotations.clear();
		fHasCorrection.clear();

		AbstractMarkerAnnotationModel model = getAnnotationModel();
		IAnnotationAccessExtension annotationAccess = getAnnotationAccessExtension();

		IDocument document = getDocument();
		if (model == null)
			return;

		Iterator<?> iter = model.getAnnotationIterator();

		while (iter.hasNext()) {
			Annotation annotation = (Annotation) iter.next();
			if (annotation.isMarkedDeleted())
				continue;

			int layer = IAnnotationAccessExtension.DEFAULT_LAYER;
			if (annotationAccess != null) {
				layer = annotationAccess.getLayer(annotation);
			}

			Position position = model.getPosition(annotation);
			if (!includesRulerLine(position, document))
				continue;

			boolean isReadOnly = fTextEditor instanceof ITextEditorExtension
					&& ((ITextEditorExtension) fTextEditor).isEditorInputReadOnly();

			if (!isReadOnly && CCorrectionProcessor.hasCorrections(annotation)) {

				fPosition = position;
				fAnnotations.put(layer, annotation);
				fHasCorrection.put(layer, true);
				continue;
			}
			AnnotationPreference preference = fAnnotationPreferenceLookup.getAnnotationPreference(annotation);
			if (preference == null)
				continue;

			String key = preference.getVerticalRulerPreferenceKey();
			if (key == null)
				continue;

			if (fStore.getBoolean(key)) {
				fPosition = position;
				fAnnotations.put(layer, annotation);
				fHasCorrection.put(layer, false);
			}
		}
	}
}
