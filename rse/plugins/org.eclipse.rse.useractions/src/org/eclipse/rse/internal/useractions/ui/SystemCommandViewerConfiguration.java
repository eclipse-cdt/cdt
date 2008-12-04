package org.eclipse.rse.internal.useractions.ui;

/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

/**
 * @author coulthar
 *
 * This is the class which enables the popup window shown when 
 *  Insert Variable is pressed in the UDA dialog.
 */
public class SystemCommandViewerConfiguration extends SourceViewerConfiguration {
	private SystemCommandContentAssistProcessor contentAssistantProcessor;
	private ContentAssistant contentAssistant;

	/**
	 * Constructor 
	 * You must call setSubstVarList.
	 */
	public SystemCommandViewerConfiguration() {
		super();
		contentAssistantProcessor = new SystemCommandContentAssistProcessor(this);
	}

	/**
	 * Reset the variable list
	 */
	public void setSubstVarList(SystemCmdSubstVarList variableList) {
		contentAssistantProcessor.setSubstVarList(variableList);
	}

	/**
	 * Return the current substitution variable list
	 */
	public SystemCmdSubstVarList getSubstVarList() {
		return contentAssistantProcessor.getSubstVarList();
	}

	/**
	 * Parent override.
	 * Returns the content assistant ready to be used with the given source viewer.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @return a content assistant or <code>null</code> if content assist should not be supported
	 */
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		if (contentAssistant == null) {
			contentAssistant = new ContentAssistant();
			contentAssistant.setContentAssistProcessor(contentAssistantProcessor, IDocument.DEFAULT_CONTENT_TYPE);
			contentAssistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
			contentAssistant.enableAutoActivation(true);
		}
		return contentAssistant;
	}

	/*
	 * Similar to org.eclipse.jdt.internal.ui.text.template.TemplateVariableProcessor#getStart(String,int).
	 */
	/**
	 * Guesses the start position of the completion.
	 * <p>
	 * Overridable by subclasses for cases when not using ${...} substitution variable patterns
	 */
	protected int getStart(String string, int end) {
		int start = end;
		if (start >= 1 && string.charAt(start - 1) == '$') return start - 1;
		while ((start != 0) && Character.isUnicodeIdentifierPart(string.charAt(start - 1)))
			start--;
		if (start >= 2 && string.charAt(start - 1) == '{' && string.charAt(start - 2) == '$') return start - 2;
		return end;
	}

	/**
	 * Return the characters which trigger the auto-display of the list
	 * substitution variables. We return '$' by default, but this can be
	 * overridden.
	 */
	protected char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] { '$' };
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getHyperlinkDetectors(org.eclipse.jface.text.source.ISourceViewer)
	 */
	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
		return null;
	}

	/**
	 * Internal class that implements the content assist processor interface
	 */
	private class SystemCommandContentAssistProcessor implements IContentAssistProcessor {
		private SystemCmdSubstVarList variableList;
		private SystemCommandViewerConfiguration configurator;

		/**
		 * Constructor
		 */
		public SystemCommandContentAssistProcessor(SystemCommandViewerConfiguration configurator) {
			this.configurator = configurator;
		}

		/**
		 * Reset the variable list
		 */
		public void setSubstVarList(SystemCmdSubstVarList variableList) {
			this.variableList = variableList;
		}

		/**
		 * Return the variable list
		 */
		public SystemCmdSubstVarList getSubstVarList() {
			return variableList;
		}

		/**
		 * @see IContentAssistProcessor#computeCompletionProposals(ITextViewer, int)
		 */
		public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
			if (variableList == null) {
				System.out.println("Inside UDAContentAssistProcessor in SystemUDASourceViewerConfiguration. variableList is null!"); //$NON-NLS-1$
				return null;
			}
			SystemCmdSubstVar[] subVars = variableList.getListAsArray();
			ICompletionProposal[] proposalList = new ICompletionProposal[subVars.length];
			int replacementOffset = documentOffset;
			int replacementLength = 0;
			// this little algo comes from the Java template support example.
			// I am not sure I like it... it seems to be designed to replace the
			// contents of the text from the previous substitution-variable-start character
			// (eg '&' or '$') to the current cursor position.    	 	  
			String text = viewer.getDocument().get();
			//System.out.println("docOffset = " + documentOffset + ", text = '" + text + "'");
			replacementOffset = configurator.getStart(text, documentOffset);
			replacementLength = documentOffset - replacementOffset;
			for (int idx = 0; idx < proposalList.length; idx++) {
				SystemCmdSubstVar currVar = subVars[idx];
				// @param replacementString the actual string to be inserted into the document
				// @param replacementOffset the offset of the text to be replaced
				// @param replacementLength the length of the text to be replaced
				// @param cursorPosition the position of the cursor following the insert relative to replacementOffset
				// @param image the image to display for this proposal
				// @param displayString the string to be displayed for the proposal
				// @param contentInformation the context information associated with this proposal
				// @param additionalProposalInfo the additional information associated with this proposal
				proposalList[idx] = new CompletionProposal(currVar.getVariable(), replacementOffset, replacementLength, documentOffset + currVar.getVariable().length(), null, currVar
						.getDisplayString(), null, null);
			}
			return proposalList;
		}

		/*
		 * @see IContentAssistProcessor#computeContextInformation(ITextViewer, int)
		 */
		public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) {
			return null;
		}

		/*
		 * @see IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
		 */
		public char[] getCompletionProposalAutoActivationCharacters() {
			return configurator.getCompletionProposalAutoActivationCharacters();
		}

		/*
		 * @see IContentAssistProcessor#getContextInformationAutoActivationCharacters()
		 */
		public char[] getContextInformationAutoActivationCharacters() {
			return null;
		}

		/*
		 * @see IContentAssistProcessor#getErrorMessage()
		 */
		public String getErrorMessage() {
			return null;
		}

		/*
		 * @see IContentAssistProcessor#getContextInformationValidator()
		 */
		public IContextInformationValidator getContextInformationValidator() {
			return null;
		}
	} // end inner class	
}
