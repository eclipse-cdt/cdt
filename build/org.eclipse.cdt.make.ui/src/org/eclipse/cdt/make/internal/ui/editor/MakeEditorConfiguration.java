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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class MakeEditorConfiguration extends SourceViewerConfiguration {

	private IMakeColorManager colorManager = null;
	private MakeCodeScanner codeScanner = null;

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
	public MakeEditorConfiguration(IMakeColorManager colorManager) {
		super();
		this.colorManager = colorManager;
	}

	/**
	 * @see SourceViewerConfiguration#getConfiguredContentTypes(ISourceViewer)
	 */
	public String[] getConfiguredContentTypes(ISourceViewer v) {
		return new String[] {
			IDocument.DEFAULT_CONTENT_TYPE,
			MakeTextEditor.MAKE_COMMENT,
			MakeTextEditor.MAKE_KEYWORD,
			MakeTextEditor.MAKE_MACRO_VAR,
			MakeTextEditor.MAKE_META_DATA };

	}

	protected IMakeColorManager getColorManager() {
		if (null == colorManager)
			colorManager = new MakeColorManager();
		return colorManager;
	}

	protected MakeCodeScanner getCodeScanner() {
		if (null == codeScanner)
			codeScanner = new MakeCodeScanner(getColorManager());
		return codeScanner;

	}

	public IPresentationReconciler getPresentationReconciler(ISourceViewer v) {

		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getCodeScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		dr = new DefaultDamagerRepairer(getCodeScanner());
		reconciler.setDamager(dr, MakePartitionScanner.MAKE_INTERNAL);
		reconciler.setRepairer(dr, MakePartitionScanner.MAKE_INTERNAL);

		dr = new DefaultDamagerRepairer(getCodeScanner());
		reconciler.setDamager(dr, MakePartitionScanner.MAKE_COMMENT);
		reconciler.setRepairer(dr, MakePartitionScanner.MAKE_COMMENT);

		dr = new DefaultDamagerRepairer(getCodeScanner());
		reconciler.setDamager(dr, MakePartitionScanner.MAKE_MACRO_ASSIGNEMENT);
		reconciler.setRepairer(dr, MakePartitionScanner.MAKE_MACRO_ASSIGNEMENT);

		dr = new DefaultDamagerRepairer(getCodeScanner());
		reconciler.setDamager(dr, MakePartitionScanner.MAKE_INCLUDE_BLOCK);
		reconciler.setRepairer(dr, MakePartitionScanner.MAKE_INCLUDE_BLOCK);

		dr = new DefaultDamagerRepairer(getCodeScanner());
		reconciler.setDamager(dr, MakePartitionScanner.MAKE_IF_BLOCK);
		reconciler.setRepairer(dr, MakePartitionScanner.MAKE_IF_BLOCK);

		dr = new DefaultDamagerRepairer(getCodeScanner());
		reconciler.setDamager(dr, MakePartitionScanner.MAKE_DEF_BLOCK);
		reconciler.setRepairer(dr, MakePartitionScanner.MAKE_DEF_BLOCK);

		dr = new DefaultDamagerRepairer(getCodeScanner());
		reconciler.setDamager(dr, MakePartitionScanner.MAKE_OTHER);
		reconciler.setRepairer(dr, MakePartitionScanner.MAKE_OTHER);
		return reconciler;
	}

}
