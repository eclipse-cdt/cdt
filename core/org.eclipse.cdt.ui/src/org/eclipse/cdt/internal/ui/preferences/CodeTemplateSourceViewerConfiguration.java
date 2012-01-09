/*******************************************************************************
 *  Copyright (c) 2000, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import java.util.Iterator;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateVariableResolver;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.IColorManager;

import org.eclipse.cdt.internal.corext.template.c.CodeTemplateContextType;

import org.eclipse.cdt.internal.ui.text.CWordFinder;
import org.eclipse.cdt.internal.ui.text.SimpleCSourceViewerConfiguration;
import org.eclipse.cdt.internal.ui.text.contentassist.ContentAssistPreference;
import org.eclipse.cdt.internal.ui.text.template.TemplateVariableProcessor;


public class CodeTemplateSourceViewerConfiguration extends SimpleCSourceViewerConfiguration {

	private static class TemplateVariableTextHover implements ITextHover {

		private TemplateVariableProcessor fProcessor;

		/**
		 * @param processor the template variable processor
		 */
		public TemplateVariableTextHover(TemplateVariableProcessor processor) {
			fProcessor= processor;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.text.ITextHover#getHoverInfo(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
		 */
		@Override
		public String getHoverInfo(ITextViewer textViewer, IRegion subject) {
			try {
				IDocument doc= textViewer.getDocument();
				int offset= subject.getOffset();
				if (offset >= 2 && "${".equals(doc.get(offset-2, 2))) { //$NON-NLS-1$
					String varName= doc.get(offset, subject.getLength());
					TemplateContextType contextType= fProcessor.getContextType();
					if (contextType != null) {
						Iterator<?> iter= contextType.resolvers();
						while (iter.hasNext()) {
							TemplateVariableResolver var= (TemplateVariableResolver) iter.next();
							if (varName.equals(var.getType())) {
								return var.getDescription();
							}
						}
					}
				}				
			} catch (BadLocationException e) {
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.text.ITextHover#getHoverRegion(org.eclipse.jface.text.ITextViewer, int)
		 */
		@Override
		public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
			if (textViewer != null) {
				return CWordFinder.findWord(textViewer.getDocument(), offset);
			}
			return null;	
		}
		
	} 
	
	private final TemplateVariableProcessor fProcessor;

	public CodeTemplateSourceViewerConfiguration(IColorManager colorManager, IPreferenceStore store, ITextEditor editor, TemplateVariableProcessor processor) {
		super(colorManager, store, editor, ICPartitions.C_PARTITIONING, false);
		fProcessor= processor;
	}
	
	/*
	 * @see SourceViewerConfiguration#getContentAssistant(ISourceViewer)
	 */
	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {

		IPreferenceStore store= CUIPlugin.getDefault().getPreferenceStore();
		

		ContentAssistant assistant= new ContentAssistant();
		assistant.setContentAssistProcessor(fProcessor, IDocument.DEFAULT_CONTENT_TYPE);
			// Register the same processor for strings and single line comments to get code completion at the start of those partitions.
		assistant.setContentAssistProcessor(fProcessor, ICPartitions.C_STRING);
		assistant.setContentAssistProcessor(fProcessor, ICPartitions.C_CHARACTER);
		assistant.setContentAssistProcessor(fProcessor, ICPartitions.C_SINGLE_LINE_COMMENT);
		assistant.setContentAssistProcessor(fProcessor, ICPartitions.C_MULTI_LINE_COMMENT);
		assistant.setContentAssistProcessor(fProcessor, ICPartitions.C_PREPROCESSOR);

		ContentAssistPreference.configure(assistant, store);

		assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		assistant.setInformationControlCreator(new IInformationControlCreator() {
			@Override
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent, false);
			}
		});

		return assistant;
	}	

	/*
	 * @see SourceViewerConfiguration#getTextHover(ISourceViewer, String, int)
	 */
	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType, int stateMask) {
		return new TemplateVariableTextHover(fProcessor);
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.CSourceViewerConfiguration#getPresentationReconciler(org.eclipse.jface.text.source.ISourceViewer)
	 */
	@Override
	public IPresentationReconciler getPresentationReconciler( ISourceViewer sourceViewer) {
		if (fProcessor.getContextType() instanceof CodeTemplateContextType) {
			return super.getPresentationReconciler(sourceViewer);
		}
		return null;
	}
}
