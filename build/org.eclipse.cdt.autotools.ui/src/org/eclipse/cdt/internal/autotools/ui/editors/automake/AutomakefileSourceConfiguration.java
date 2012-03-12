/*******************************************************************************
 * Copyright (c) 2000, 2006, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Red Hat Inc. - Modified from MakefileSourceConfiguration to support Automake
 *******************************************************************************/

package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.PropertyChangeEvent;


public class AutomakefileSourceConfiguration extends
		MakefileSourceConfiguration {
	
	AutomakeEditor editor;
	AutomakefileCodeScanner codeScanner;
	AutomakeTextHover amHover;
	
	/**
	 * Constructor for MakeConfiguration
	 */
	public AutomakefileSourceConfiguration(IPreferenceStore preferenceStore) {
		super(preferenceStore, null);
	}
	
	public AutomakefileSourceConfiguration(IPreferenceStore preferenceStore, AutomakeEditor editor) {
		super(preferenceStore, editor);
		this.editor = editor;
	}
	
	public AutomakefileCodeScanner getAutomakeCodeScanner() {
		if (null == codeScanner)
			codeScanner = new AutomakefileCodeScanner();
		return codeScanner;

	}
	
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		if (amHover == null)
			amHover = new AutomakeTextHover(editor);
		return amHover;
	}
	
	/**
	 * @param event
	 */
	public void adaptToPreferenceChange(PropertyChangeEvent event) {
		AutomakefileCodeScanner scanner = getAutomakeCodeScanner();
		scanner.adaptToPreferenceChange(event);
	}
	
	/**
	 * @param event
	 * @return
	 */
	public boolean affectsBehavior(PropertyChangeEvent event) {
		AutomakefileCodeScanner scanner = getAutomakeCodeScanner();
		return scanner.affectsBehavior(event);
	}
	
	public IPresentationReconciler getPresentationReconciler(ISourceViewer v) {

		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getAutomakeCodeScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		dr = new DefaultDamagerRepairer(getAutomakeCodeScanner());
		reconciler.setDamager(dr, MakefilePartitionScanner.MAKEFILE_COMMENT_PARTITION);
		reconciler.setRepairer(dr, MakefilePartitionScanner.MAKEFILE_COMMENT_PARTITION);

		dr = new DefaultDamagerRepairer(getAutomakeCodeScanner());
		reconciler.setDamager(dr, MakefilePartitionScanner.MAKEFILE_MACRO_ASSIGNEMENT_PARTITION);
		reconciler.setRepairer(dr, MakefilePartitionScanner.MAKEFILE_MACRO_ASSIGNEMENT_PARTITION);

		dr = new DefaultDamagerRepairer(getAutomakeCodeScanner());
		reconciler.setDamager(dr, MakefilePartitionScanner.MAKEFILE_INCLUDE_BLOCK_PARTITION);
		reconciler.setRepairer(dr, MakefilePartitionScanner.MAKEFILE_INCLUDE_BLOCK_PARTITION);

		dr = new DefaultDamagerRepairer(getAutomakeCodeScanner());
		reconciler.setDamager(dr, MakefilePartitionScanner.MAKEFILE_IF_BLOCK_PARTITION);
		reconciler.setRepairer(dr, MakefilePartitionScanner.MAKEFILE_IF_BLOCK_PARTITION);

		dr = new DefaultDamagerRepairer(getAutomakeCodeScanner());
		reconciler.setDamager(dr, MakefilePartitionScanner.MAKEFILE_DEF_BLOCK_PARTITION);
		reconciler.setRepairer(dr, MakefilePartitionScanner.MAKEFILE_DEF_BLOCK_PARTITION);

		dr = new DefaultDamagerRepairer(getAutomakeCodeScanner());
		reconciler.setDamager(dr, MakefilePartitionScanner.MAKEFILE_OTHER_PARTITION);
		reconciler.setRepairer(dr, MakefilePartitionScanner.MAKEFILE_OTHER_PARTITION);
		return reconciler;
	}

	
	/**
	 * @see SourceViewerConfiguration#getReconciler(ISourceViewer)
	 */
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		if (editor != null && editor.isEditable()) {
			MonoReconciler reconciler= new MonoReconciler(new AutomakefileReconcilingStrategy(editor), false);
			reconciler.setDelay(1000);
			reconciler.setProgressMonitor(new NullProgressMonitor());
			return reconciler;
		}
		return null;
	}
	
	/**
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getContentAssistant(ISourceViewer)
	 */
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant assistant = new ContentAssistant();
		assistant.setContentAssistProcessor(new AutomakeCompletionProcessor(editor), IDocument.DEFAULT_CONTENT_TYPE);
		assistant.setContentAssistProcessor(new AutomakeCompletionProcessor(editor), MakefilePartitionScanner.MAKEFILE_COMMENT_PARTITION);
		assistant.setContentAssistProcessor(new AutomakeCompletionProcessor(editor), MakefilePartitionScanner.MAKEFILE_DEF_BLOCK_PARTITION);
		assistant.setContentAssistProcessor(new AutomakeCompletionProcessor(editor), MakefilePartitionScanner.MAKEFILE_IF_BLOCK_PARTITION);
		assistant.setContentAssistProcessor(new AutomakeCompletionProcessor(editor), MakefilePartitionScanner.MAKEFILE_INCLUDE_BLOCK_PARTITION);
		assistant.setContentAssistProcessor(new AutomakeCompletionProcessor(editor), MakefilePartitionScanner.MAKEFILE_MACRO_ASSIGNEMENT_PARTITION);

		assistant.enableAutoActivation(true);
		assistant.setAutoActivationDelay(500);

		assistant.setProposalPopupOrientation(IContentAssistant.CONTEXT_INFO_BELOW);
		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_BELOW);

		return assistant;
	}
}
