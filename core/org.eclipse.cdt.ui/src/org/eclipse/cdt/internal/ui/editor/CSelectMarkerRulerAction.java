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
package org.eclipse.cdt.internal.ui.editor;

import java.util.ResourceBundle;

import org.eclipse.cdt.internal.ui.text.c.hover.CExpandHover;
import org.eclipse.cdt.internal.ui.text.correction.CCorrectionProcessor;
import org.eclipse.cdt.internal.ui.text.correction.QuickAssistLightBulbUpdater.AssistAnnotation;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.VerticalRulerEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.texteditor.SelectAnnotationRulerAction;

/**
 * A special select marker ruler action which activates quick fix if clicked on a quick fixable problem.
 * <p>
 * Originally copied from org.eclipse.jdt.internal.ui.javaeditor.JavaSelectMarkerRulerAction
 */
public class CSelectMarkerRulerAction extends SelectAnnotationRulerAction {

	public CSelectMarkerRulerAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
		super(bundle, prefix, editor);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IVerticalRulerListener#annotationDefaultSelected(org.eclipse.ui.texteditor.VerticalRulerEvent)
	 */
	@Override
	public void annotationDefaultSelected(VerticalRulerEvent event) {
		Annotation annotation = event.getSelectedAnnotation();
		IAnnotationModel model = getAnnotationModel();

		if (isOverrideIndicator(annotation)) {
			((OverrideIndicatorManager.OverrideIndicator) annotation).open();
			return;
		}

		if (isBreakpoint(annotation))
			triggerAction(ITextEditorActionConstants.RULER_DOUBLE_CLICK, event.getEvent());

		Position position = model.getPosition(annotation);
		if (position == null)
			return;

		if (isQuickFixTarget(annotation)) {
			ITextOperationTarget operation = getTextEditor().getAdapter(ITextOperationTarget.class);
			final int opCode = ISourceViewer.QUICK_ASSIST;
			if (operation != null && operation.canDoOperation(opCode)) {
				getTextEditor().selectAndReveal(position.getOffset(), position.getLength());
				operation.doOperation(opCode);
				return;
			}
		}

		// default:
		super.annotationDefaultSelected(event);
	}

	/**
	 * Tells whether the given annotation is an override annotation.
	 *
	 * @param annotation the annotation
	 * @return <code>true</code> iff the annotation is an override annotation
	 */
	private boolean isOverrideIndicator(Annotation annotation) {
		return annotation instanceof OverrideIndicatorManager.OverrideIndicator;
	}

	/**
	 * Checks whether the given annotation is a breakpoint annotation.
	 *
	 * @param annotation the annotation
	 * @return <code>true</code> if the annotation is a breakpoint annotation
	 */
	private boolean isBreakpoint(Annotation annotation) {
		return annotation.getType().equals("org.eclipse.cdt.debug.core.breakpoint") //$NON-NLS-1$
				|| annotation.getType().equals(CExpandHover.NO_BREAKPOINT_ANNOTATION);
	}

	private boolean isQuickFixTarget(Annotation a) {
		return CCorrectionProcessor.hasCorrections(a) || a instanceof AssistAnnotation;
	}

	private void triggerAction(String actionID, Event event) {
		IAction action = getTextEditor().getAction(actionID);
		if (action != null) {
			if (action instanceof IUpdate)
				((IUpdate) action).update();
			// hack to propagate line change
			if (action instanceof ISelectionListener) {
				((ISelectionListener) action).selectionChanged(null, null);
			}
			if (action.isEnabled()) {
				if (event == null) {
					action.run();
				} else {
					event.type = SWT.MouseDoubleClick;
					event.count = 2;
					action.runWithEvent(event);
				}
			}
		}
	}

}
