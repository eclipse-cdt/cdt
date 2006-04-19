/*******************************************************************************
 * Copyright (c) 2000 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import java.util.Vector;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.CElementHyperlinkDetector;
import org.eclipse.cdt.internal.ui.editor.CSourceViewer;
import org.eclipse.cdt.internal.ui.text.c.hover.CEditorTextHoverDescriptor;
import org.eclipse.cdt.internal.ui.text.c.hover.CEditorTextHoverProxy;
import org.eclipse.cdt.internal.ui.text.contentassist.CCompletionProcessor2;
import org.eclipse.cdt.internal.ui.text.contentassist.ContentAssistPreference;
import org.eclipse.cdt.ui.CElementContentProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.formatter.MultiPassContentFormatter;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.ITextEditor;



/**
 * Configuration for an <code>SourceViewer</code> which shows C code.
 */
public class CSourceViewerConfiguration extends TextSourceViewerConfiguration {
	
    private CTextTools fTextTools;
	private CEditor fEditor;
	
	/**
	 * Creates a new C source viewer configuration for viewers in the given editor using
	 * the given C tools collection.
	 *
	 * @param tools the C text tools collection to be used
	 * @param editor the editor in which the configured viewer will reside
	 */
	public CSourceViewerConfiguration(CTextTools tools, CEditor editor) {
		super(CUIPlugin.getDefault().getCombinedPreferenceStore());
		fTextTools= tools;
		fEditor= editor;
	}

	/**
	 * Returns the C multiline comment scanner for this configuration.
	 *
	 * @return the C multiline comment scanner
	 */
	protected RuleBasedScanner getMultilineCommentScanner() {
		return fTextTools.getMultilineCommentScanner();
	}
	
	/**
	 * Returns the C singleline comment scanner for this configuration.
	 *
	 * @return the C singleline comment scanner
	 */
	protected RuleBasedScanner getSinglelineCommentScanner() {
		return fTextTools.getSinglelineCommentScanner();
	}
	
	/**
	 * Returns the C string scanner for this configuration.
	 *
	 * @return the C string scanner
	 */
	protected RuleBasedScanner getStringScanner() {
		return fTextTools.getStringScanner();
	}	
	
	/**
	 * Returns the color manager for this configuration.
	 *
	 * @return the color manager
	 */
	protected IColorManager getColorManager() {
		return fTextTools.getColorManager();
	}
	
	/**
	 * Returns the editor in which the configured viewer(s) will reside.
	 *
	 * @return the enclosing editor
	 */
	protected ITextEditor getEditor() {
		return fEditor;
	}

	
    /**
     * Creates outline presenter. 
     * @param editor Editor.
     * @return Presenter with outline view.
     */
    public IInformationPresenter getOutlinePresenter(CEditor editor)
    {
        final IInformationControlCreator outlineControlCreator = getOutlineContolCreator(editor);
        final InformationPresenter presenter = new InformationPresenter(outlineControlCreator);
        final IInformationProvider provider = new CElementContentProvider(getEditor());
        presenter.setInformationProvider(provider, IDocument.DEFAULT_CONTENT_TYPE);
        presenter.setInformationProvider(provider, ICPartitions.C_MULTILINE_COMMENT);
        presenter.setInformationProvider(provider, ICPartitions.C_SINGLE_LINE_COMMENT);
        presenter.setInformationProvider(provider, ICPartitions.C_STRING);
        presenter.setSizeConstraints(20, 20, true, false);
        presenter.setRestoreInformationControlBounds(getSettings("outline_presenter_bounds"), true, true); //$NON-NLS-1$        
        return presenter;
    }

    /**
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getPresentationReconciler(org.eclipse.jface.text.source.ISourceViewer)
	 */
    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {

		PresentationReconciler reconciler= new PresentationReconciler();
		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

		RuleBasedScanner scanner;

		if(sourceViewer instanceof CSourceViewer) {
			String language = ((CSourceViewer)sourceViewer).getDisplayLanguage();
			if(language != null && language.equals(CEditor.LANGUAGE_CPP)) {
				scanner= fTextTools.getCppCodeScanner();
			} else {
				scanner= fTextTools.getCCodeScanner();
			}
		} else {
			scanner= fTextTools.getCCodeScanner();
		}

		DefaultDamagerRepairer dr= new DefaultDamagerRepairer(scanner);

		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		//TextAttribute attr = new TextAttribute(manager.getColor(ICColorConstants.C_DEFAULT));
		
		dr= new DefaultDamagerRepairer(getSinglelineCommentScanner());		
		reconciler.setDamager(dr, ICPartitions.C_SINGLE_LINE_COMMENT);
		reconciler.setRepairer(dr, ICPartitions.C_SINGLE_LINE_COMMENT);
		
		dr= new DefaultDamagerRepairer(getMultilineCommentScanner());		
		reconciler.setDamager(dr, ICPartitions.C_MULTILINE_COMMENT);
		reconciler.setRepairer(dr, ICPartitions.C_MULTILINE_COMMENT);

		dr= new DefaultDamagerRepairer(getStringScanner());
		reconciler.setDamager(dr, ICPartitions.C_STRING);
		reconciler.setRepairer(dr, ICPartitions.C_STRING);
		
		dr= new DefaultDamagerRepairer(getStringScanner());
		reconciler.setDamager(dr, ICPartitions.C_CHARACTER);
		reconciler.setRepairer(dr, ICPartitions.C_CHARACTER);
		
		return reconciler;
	}


	/**
	 * @see SourceViewerConfiguration#getContentAssistant(ISourceViewer)
	 */
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		if (getEditor() == null) {
			return null;
		}

		ContentAssistant assistant = new ContentAssistant();
		
		IContentAssistProcessor processor = new CCompletionProcessor2(getEditor());
		assistant.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);

		//Will this work as a replacement for the configuration lines below?
		ContentAssistPreference.configure(assistant, getPreferenceStore());
		
		assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);		
		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));

		return assistant;
	}
	
	
	/**
	 * @see SourceViewerConfiguration#getReconciler(ISourceViewer)
	 */
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		if (fEditor != null && fEditor.isEditable()) {
			//Delay changed and non-incremental reconciler used due to 
			//PR 130089
			MonoReconciler reconciler= new MonoReconciler(new CReconcilingStrategy(fEditor), false);
			reconciler.setDelay(500);
			return reconciler;
		}
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getAutoEditStrategies(org.eclipse.jface.text.source.ISourceViewer, java.lang.String)
	 */
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
		if(ICPartitions.C_MULTILINE_COMMENT.equals(contentType)) {
			return new IAutoEditStrategy[] {new CCommentAutoIndentStrategy()};
		}
		return new IAutoEditStrategy[] {new CAutoIndentStrategy()};
	}

	/**
	 * @see SourceViewerConfiguration#getDoubleClickStrategy(ISourceViewer, String)
	 */
	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {
		return new CDoubleClickSelector();
	}

	/**
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getDefaultPrefixes(ISourceViewer, String)
	 */
	public String[] getDefaultPrefixes(ISourceViewer sourceViewer, String contentType) {
		return new String[] { "//", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see SourceViewerConfiguration#getDefaultPrefix(ISourceViewer, String)
	 */
	public String getDefaultPrefix(ISourceViewer sourceViewer, String contentType) {
		if(IDocument.DEFAULT_CONTENT_TYPE.equals(contentType))
			return "//"; //$NON-NLS-1$
		if(ICPartitions.C_SINGLE_LINE_COMMENT.equals(contentType)) {
			return "//"; //$NON-NLS-1$
		}
		if(ICPartitions.C_MULTILINE_COMMENT.equals(contentType)) {
			return "//"; //$NON-NLS-1$
		}
		return null;
	}


	/*
	 * @see SourceViewerConfiguration#getIndentPrefixes(ISourceViewer, String)
	 */
	public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType) {

		Vector vector= new Vector();

		// prefix[0] is either '\t' or ' ' x tabWidth, depending on useSpaces
		int tabWidth= getTabWidth(sourceViewer);
		boolean useSpaces= getPreferenceStore().getBoolean(CEditor.SPACES_FOR_TABS); //$NON-NLS-1$

		for (int i= 0; i <= tabWidth; i++) {
		    StringBuffer prefix= new StringBuffer();

			if (useSpaces) {
			    for (int j= 0; j + i < tabWidth; j++)
			    	prefix.append(' ');
		    	
				if (i != 0)
		    		prefix.append('\t');				
			} else {    
			    for (int j= 0; j < i; j++)
			    	prefix.append(' ');
		    	
				if (i != tabWidth)
		    		prefix.append('\t');
			}
			
			vector.add(prefix.toString());
		}

		vector.add(""); //$NON-NLS-1$
		
		return (String[]) vector.toArray(new String[vector.size()]);
	}

	/**
	 * @see SourceViewerConfiguration#getAnnotationHover(ISourceViewer)
	 */
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return new CAnnotationHover();
	}


	
	/*
	 * @see SourceViewerConfiguration#getConfiguredTextHoverStateMasks(ISourceViewer, String)
	 * @since 2.1
	 */
	public int[] getConfiguredTextHoverStateMasks(ISourceViewer sourceViewer, String contentType) {
		CEditorTextHoverDescriptor[] hoverDescs= CUIPlugin.getDefault().getCEditorTextHoverDescriptors();
		int stateMasks[]= new int[hoverDescs.length];
		int stateMasksLength= 0;		
		for (int i= 0; i < hoverDescs.length; i++) {
			if (hoverDescs[i].isEnabled()) {
				int j= 0;
				int stateMask= hoverDescs[i].getStateMask();
				while (j < stateMasksLength) {
					if (stateMasks[j] == stateMask)
						break;
					j++;
				}
				if (j == stateMasksLength)
					stateMasks[stateMasksLength++]= stateMask;
			}
		}
		if (stateMasksLength == hoverDescs.length)
			return stateMasks;
		
		int[] shortenedStateMasks= new int[stateMasksLength];
		System.arraycopy(stateMasks, 0, shortenedStateMasks, 0, stateMasksLength);
		return shortenedStateMasks;
	}
	
	/*
	 * @see SourceViewerConfiguration#getTextHover(ISourceViewer, String, int)
	 * @since 2.1
	 */
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType, int stateMask) {
		CEditorTextHoverDescriptor[] hoverDescs= CUIPlugin.getDefault().getCEditorTextHoverDescriptors();
		int i= 0;
		while (i < hoverDescs.length) {
			if (hoverDescs[i].isEnabled() &&  hoverDescs[i].getStateMask() == stateMask)
				return new CEditorTextHoverProxy(hoverDescs[i], getEditor());
			i++;
		}

		return null;
	}

	/*
	 * @see SourceViewerConfiguration#getTextHover(ISourceViewer, String)
	 */
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		return getTextHover(sourceViewer, contentType, ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK);
	}

	/**
	 * @see SourceViewerConfiguration#getConfiguredContentTypes(ISourceViewer)
	 */
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] { 	
				IDocument.DEFAULT_CONTENT_TYPE, 
				ICPartitions.C_MULTILINE_COMMENT,
				ICPartitions.C_SINGLE_LINE_COMMENT,
				ICPartitions.C_STRING,
				ICPartitions.C_CHARACTER};
	}
	
	/**
	 * @see SourceViewerConfiguration#getContentFormatter(ISourceViewer)
	 */
	public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
		
		final MultiPassContentFormatter formatter = 
			new MultiPassContentFormatter(getConfiguredDocumentPartitioning(sourceViewer), 
				IDocument.DEFAULT_CONTENT_TYPE);
		
		formatter.setMasterStrategy(new CFormattingStrategy());
		return formatter;
		
		
	}
	
	public boolean affectsBehavior(PropertyChangeEvent event) {
		return fTextTools.affectsBehavior(event);
	}

	/**
	 * Adapts the behavior of the contained components to the change
	 * encoded in the given event.
	 * 
	 * @param event the event to whch to adapt
	 */
	public void adaptToPreferenceChange(PropertyChangeEvent event) {
		fTextTools.adaptToPreferenceChange(event);
	}

	protected IPreferenceStore getPreferenceStore() {
		return CUIPlugin.getDefault().getPreferenceStore();
	}
	
	/*
	 * @see SourceViewerConfiguration#getHoverControlCreator(ISourceViewer)
	 * @since 2.0
	 */
	public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer) {
		return getInformationControlCreator(sourceViewer, true);
	}
	

	public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer, final boolean cutDown) {
			return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				int style = cutDown ? SWT.NONE : (SWT.V_SCROLL | SWT.H_SCROLL);
				return new DefaultInformationControl(parent, style, new HTMLTextPresenter(cutDown));
				// return new HoverBrowserControl(parent);
			}
		};
	}

	/*
	 * @see SourceViewerConfiguration#getInformationPresenter(ISourceViewer)
	 * @since 2.0
	 */
	public IInformationPresenter getInformationPresenter(ISourceViewer sourceViewer) {
		return super.getInformationPresenter(sourceViewer);
	}
    
	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getHyperlinkDetectors(org.eclipse.jface.text.source.ISourceViewer)
	 * @since 3.1
	 */
	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
		if (!fPreferenceStore.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINKS_ENABLED))
			return null;
		
		IHyperlinkDetector[] inheritedDetectors= super.getHyperlinkDetectors(sourceViewer);
		
		if (fEditor == null)
			return inheritedDetectors;
		
		int inheritedDetectorsLength= inheritedDetectors != null ? inheritedDetectors.length : 0;
		IHyperlinkDetector[] detectors= new IHyperlinkDetector[inheritedDetectorsLength + 1];
		detectors[0]= new CElementHyperlinkDetector(fEditor); 
		for (int i= 0; i < inheritedDetectorsLength; i++) {
			detectors[i+1]= inheritedDetectors[i];
		}
		
		return detectors;
	}

	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredDocumentPartitioning(org.eclipse.jface.text.source.ISourceViewer)
	 */
    public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
		return fTextTools.getDocumentPartitioning();
	}

	/**
     * Creates control for outline presentation in editor.
     * @param editor Editor.
     * @return Control.
     */
    private IInformationControlCreator getOutlineContolCreator(final CEditor editor)
    {
        final IInformationControlCreator conrolCreator = new IInformationControlCreator()
        {
            /**
             * @see org.eclipse.jface.text.IInformationControlCreator#createInformationControl(org.eclipse.swt.widgets.Shell)
             */
            public IInformationControl createInformationControl(Shell parent)
            {
                int shellStyle= SWT.RESIZE;
                int treeStyle= SWT.V_SCROLL | SWT.H_SCROLL;
                return new COutlineInformationControl(editor, parent, shellStyle, treeStyle);   
            }
        };
        return conrolCreator;
    }

    /**
     * Returns the settings for the given section.
     *
     * @param sectionName the section name
     * @return the settings
     */
    private IDialogSettings getSettings(String sectionName) {
        IDialogSettings settings= CUIPlugin.getDefault().getDialogSettings().getSection(sectionName);
        if (settings == null) {
            settings= CUIPlugin.getDefault().getDialogSettings().addNewSection(sectionName);
        }
        
        return settings;
    }
    
}
