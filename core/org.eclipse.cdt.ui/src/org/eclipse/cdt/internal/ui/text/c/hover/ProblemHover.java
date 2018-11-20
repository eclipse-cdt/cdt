/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.c.hover;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.editor.ICAnnotation;
import org.eclipse.cdt.internal.ui.text.contentassist.CCompletionProposalComparator;
import org.eclipse.cdt.internal.ui.text.correction.CCorrectionProcessor;
import org.eclipse.cdt.internal.ui.text.correction.CorrectionContext;
import org.eclipse.cdt.internal.ui.text.correction.ProblemLocation;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICCompletionProposal;
import org.eclipse.cdt.ui.text.IProblemLocation;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.spelling.SpellingAnnotation;

/**
 * This annotation hover shows the description of the
 * selected java annotation.
 *
 * XXX: Currently this problem hover only works for spelling problems.
 *		see: https://bugs.eclipse.org/bugs/show_bug.cgi?id=62081
 *
 * @since 5.0
 */
public class ProblemHover extends AbstractAnnotationHover {

	protected static class ProblemInfo extends AnnotationInfo {
		private static final ICompletionProposal[] NO_PROPOSALS = new ICompletionProposal[0];

		public ProblemInfo(Annotation annotation, Position position, ITextViewer textViewer) {
			super(annotation, position, textViewer);
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.text.java.hover.AbstractAnnotationHover.AnnotationInfo#getCompletionProposals()
		 */
		@Override
		public ICompletionProposal[] getCompletionProposals() {
			if (annotation instanceof ICAnnotation) {
				return getCAnnotationFixes((ICAnnotation) annotation);
			} else if (annotation instanceof MarkerAnnotation) {
				return getMarkerAnnotationFixes((MarkerAnnotation) annotation);
			}

			return NO_PROPOSALS;
		}

		private ICompletionProposal[] getCAnnotationFixes(ICAnnotation cAnnotation) {
			ProblemLocation location = new ProblemLocation(position.getOffset(), position.getLength(), cAnnotation);
			ITranslationUnit tu = cAnnotation.getTranslationUnit();

			ISourceViewer sourceViewer = null;
			if (viewer instanceof ISourceViewer)
				sourceViewer = (ISourceViewer) viewer;

			CorrectionContext context = new CorrectionContext(tu, sourceViewer, location.getOffset(),
					location.getLength());
			if (!SpellingAnnotation.TYPE.equals(cAnnotation.getType()))
				return NO_PROPOSALS;

			List<ICCompletionProposal> proposals = new ArrayList<>();
			CCorrectionProcessor.collectCorrections(context, new IProblemLocation[] { location }, proposals);
			Collections.sort(proposals, new CCompletionProposalComparator());

			return proposals.toArray(new ICompletionProposal[proposals.size()]);
		}

		private ICompletionProposal[] getMarkerAnnotationFixes(MarkerAnnotation markerAnnotation) {
			if (markerAnnotation.isQuickFixableStateSet() && !markerAnnotation.isQuickFixable())
				return NO_PROPOSALS;

			IMarker marker = markerAnnotation.getMarker();

			IEditorInput input = null;
			try {
				input = EditorUtility.getEditorInput(marker.getResource());
			} catch (CModelException e) {
			}
			if (input == null)
				return NO_PROPOSALS;

			ITranslationUnit tu = getTranslationUnit(input);
			if (tu == null)
				return NO_PROPOSALS;

			IAnnotationModel model = CUIPlugin.getDefault().getDocumentProvider().getAnnotationModel(input);
			if (model == null)
				return NO_PROPOSALS;

			ISourceViewer sourceViewer = null;
			if (viewer instanceof ISourceViewer)
				sourceViewer = (ISourceViewer) viewer;

			CorrectionContext context = new CorrectionContext(tu, sourceViewer, position.getOffset(),
					position.getLength());

			List<ICCompletionProposal> proposals = new ArrayList<>();
			CCorrectionProcessor.collectProposals(context, model, new Annotation[] { markerAnnotation }, true, false,
					proposals);

			return proposals.toArray(new ICompletionProposal[proposals.size()]);
		}

		private static ITranslationUnit getTranslationUnit(IEditorInput input) {
			return CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(input);
		}
	}

	public ProblemHover() {
		super(false);
	}

	@Override
	protected AnnotationInfo createAnnotationInfo(Annotation annotation, Position position, ITextViewer textViewer) {
		return new ProblemInfo(annotation, position, textViewer);
	}
}
