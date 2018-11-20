/*******************************************************************************
 * Copyright (c) 2006, 2017 Red Hat, Inc.
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

import org.eclipse.cdt.internal.autotools.ui.editors.autoconf.AutoconfPresentationReconciler;
import org.eclipse.cdt.internal.autotools.ui.text.hover.AutoconfTextHover;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

public class AutoconfSourceViewerConfiguration extends TextSourceViewerConfiguration {

	private ITextHover acHover;
	private IAnnotationHover aaHover;
	private AutoconfEditor fEditor;

	public AutoconfSourceViewerConfiguration(IPreferenceStore prefs, AutoconfEditor editor) {
		super(prefs);
		fEditor = editor;
	}

	@Override
	public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
		return AutoconfEditor.AUTOCONF_PARTITIONING;
	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant assistant = new ContentAssistant();

		IContentAssistProcessor macroContentAssistProcessor = new AutoconfMacroContentAssistProcessor(fEditor);
		assistant.setContentAssistProcessor(macroContentAssistProcessor, AutoconfPartitionScanner.AUTOCONF_MACRO);
		assistant.setContentAssistProcessor(macroContentAssistProcessor, IDocument.DEFAULT_CONTENT_TYPE);
		assistant.enableAutoActivation(true);
		assistant.setAutoActivationDelay(500);
		assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		assistant.setInformationControlCreator(AutoconfTextHover.getInformationControlCreator());

		return assistant;
	}

	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] { IDocument.DEFAULT_CONTENT_TYPE, AutoconfPartitionScanner.AUTOCONF_MACRO,
				AutoconfPartitionScanner.AUTOCONF_COMMENT };
	}

	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		if (acHover == null)
			acHover = new AutoconfTextHover(fEditor);
		return acHover;
	}

	@Override
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		if (aaHover == null)
			aaHover = new AutoconfAnnotationHover();
		return aaHover;
	}

	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		MonoReconciler reconciler = new MonoReconciler(new AutoconfReconcilingStrategy(fEditor), false);
		reconciler.setDelay(1000);
		reconciler.setProgressMonitor(new NullProgressMonitor());
		return reconciler;
	}

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		return new AutoconfPresentationReconciler();
	}
}
