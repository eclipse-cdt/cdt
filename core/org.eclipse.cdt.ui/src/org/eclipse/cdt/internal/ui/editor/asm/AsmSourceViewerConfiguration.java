package org.eclipse.cdt.internal.ui.editor.asm;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.internal.ui.text.ICColorConstants;
import org.eclipse.cdt.internal.ui.text.util.CColorManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.RuleBasedDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.custom.StyleRange;


public class AsmSourceViewerConfiguration extends SourceViewerConfiguration {


	private AsmTextEditor fEditor;
	private AsmTextTools fTextTools;
	
	/**
	 * Constructor for AsmSourceViewerConfiguration
	 */
	public AsmSourceViewerConfiguration(AsmTextTools tools, AsmTextEditor editor) {
		super();
		fEditor = editor;
		fTextTools = tools;
	}
	
	/**
	 * @see ISourceViewerConfiguration#getPresentationReconciler(ISourceViewer)
	 */
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {



		CColorManager manager= fTextTools.getColorManager();
		PresentationReconciler reconciler= new PresentationReconciler();



		RuleBasedDamagerRepairer dr= new RuleBasedDamagerRepairer(
			/*
			 * Override addRange in the default RuleBasedDamagerRepairer, it forgets to pass in text style
	 		 * More code here only until we move to 2.0...
	 		 */
			fTextTools.getCodeScanner(),
			new TextAttribute(manager.getColor(ICColorConstants.C_DEFAULT))) {
				protected void addRange(TextPresentation presentation, int offset, int length, TextAttribute attr) {
					presentation.addStyleRange(new StyleRange(offset, length, attr.getForeground(), attr.getBackground(), attr.getStyle()));
				}
			};
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);



		dr= new RuleBasedDamagerRepairer(
			null,
			new TextAttribute(manager.getColor(ICColorConstants.C_MULTI_LINE_COMMENT))
		);		
		reconciler.setDamager(dr, AsmPartitionScanner.C_MULTILINE_COMMENT);
		reconciler.setRepairer(dr, AsmPartitionScanner.C_MULTILINE_COMMENT);



		return reconciler;
	}
	


}


