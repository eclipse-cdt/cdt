/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.ui.editor;

import org.eclipse.cdt.make.internal.ui.text.IMakefileColorManager;
import org.eclipse.cdt.make.internal.ui.text.MakefileColorManager;
import org.eclipse.cdt.make.internal.ui.text.makefile.MakefileCodeScanner;
import org.eclipse.cdt.make.internal.ui.text.makefile.MakefileCompletionProcessor;
import org.eclipse.cdt.make.internal.ui.text.makefile.MakefilePartitionScanner;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.graphics.RGB;

public class MakefileSourceConfiguration extends SourceViewerConfiguration {

	private IMakefileColorManager colorManager;
	private MakefileCodeScanner codeScanner;
	private MakefileEditor fEditor;

	/**
	 * Single token scanner.
	 */
	static class SingleTokenScanner extends BufferedRuleBasedScanner {
		public SingleTokenScanner(TextAttribute attribute) {
			setDefaultReturnToken(new Token(attribute));
		}
	};

	/**
	 * Constructor for MakeConfiguration
	 */
	public MakefileSourceConfiguration(IMakefileColorManager colorManager, MakefileEditor editor) {
		super();
		fEditor = editor;
		this.colorManager = colorManager;
	}

	/**
	 * @see SourceViewerConfiguration#getConfiguredContentTypes(ISourceViewer)
	 */
	public String[] getConfiguredContentTypes(ISourceViewer v) {
		return new String[] {
			IDocument.DEFAULT_CONTENT_TYPE,
			MakefileEditor.MAKE_COMMENT,
			MakefileEditor.MAKE_KEYWORD,
			MakefileEditor.MAKE_MACRO_VAR,
			MakefileEditor.MAKE_META_DATA
		};

	}

        /**
         * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getContentAssistant(ISourceViewer)
         */
        public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
                ContentAssistant assistant = new ContentAssistant();
                assistant.setContentAssistProcessor(new MakefileCompletionProcessor(fEditor), IDocument.DEFAULT_CONTENT_TYPE);
                assistant.setContentAssistProcessor(new MakefileCompletionProcessor(fEditor), MakefileEditor.MAKE_MACRO_VAR);
                assistant.enableAutoActivation(true);
                assistant.setAutoActivationDelay(500);
                assistant.setProposalPopupOrientation(IContentAssistant.CONTEXT_INFO_BELOW);
                assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_BELOW);
                //Set to Carolina blue
                assistant.setContextInformationPopupBackground(getColorManager().getColor(new RGB(0, 191, 255)));
                                                                                                                             
                return assistant;
        }


	protected IMakefileColorManager getColorManager() {
		if (null == colorManager)
			colorManager = new MakefileColorManager();
		return colorManager;
	}

	protected MakefileCodeScanner getCodeScanner() {
		if (null == codeScanner)
			codeScanner = new MakefileCodeScanner(getColorManager());
		return codeScanner;

	}

	public IPresentationReconciler getPresentationReconciler(ISourceViewer v) {

		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getCodeScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		dr = new DefaultDamagerRepairer(getCodeScanner());
		dr = new DefaultDamagerRepairer(getCodeScanner());
		reconciler.setDamager(dr, MakefilePartitionScanner.MAKE_COMMENT);
		reconciler.setRepairer(dr, MakefilePartitionScanner.MAKE_COMMENT);

		dr = new DefaultDamagerRepairer(getCodeScanner());
		reconciler.setDamager(dr, MakefilePartitionScanner.MAKE_MACRO_ASSIGNEMENT);
		reconciler.setRepairer(dr, MakefilePartitionScanner.MAKE_MACRO_ASSIGNEMENT);

		dr = new DefaultDamagerRepairer(getCodeScanner());
		reconciler.setDamager(dr, MakefilePartitionScanner.MAKE_INCLUDE_BLOCK);
		reconciler.setRepairer(dr, MakefilePartitionScanner.MAKE_INCLUDE_BLOCK);

		dr = new DefaultDamagerRepairer(getCodeScanner());
		reconciler.setDamager(dr, MakefilePartitionScanner.MAKE_IF_BLOCK);
		reconciler.setRepairer(dr, MakefilePartitionScanner.MAKE_IF_BLOCK);

		dr = new DefaultDamagerRepairer(getCodeScanner());
		reconciler.setDamager(dr, MakefilePartitionScanner.MAKE_DEF_BLOCK);
		reconciler.setRepairer(dr, MakefilePartitionScanner.MAKE_DEF_BLOCK);

		dr = new DefaultDamagerRepairer(getCodeScanner());
		reconciler.setDamager(dr, MakefilePartitionScanner.MAKE_OTHER);
		reconciler.setRepairer(dr, MakefilePartitionScanner.MAKE_OTHER);
		return reconciler;
	}

}
