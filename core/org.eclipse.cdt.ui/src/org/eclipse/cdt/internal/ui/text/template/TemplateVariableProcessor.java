/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.template;



import org.eclipse.cdt.internal.corext.template.ContextType;
import org.eclipse.cdt.internal.corext.template.TemplateVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;


public class TemplateVariableProcessor implements IContentAssistProcessor {	

	private static Comparator fgTemplateVariableProposalComparator= new Comparator() {
		public int compare(Object arg0, Object arg1) {
			TemplateVariableProposal proposal0= (TemplateVariableProposal) arg0;
			TemplateVariableProposal proposal1= (TemplateVariableProposal) arg1;
			
			return proposal0.getDisplayString().compareTo(proposal1.getDisplayString());
		}

		public boolean equals(Object arg0) {
			return false;
		}
	};

	
	/** the context type */
	private ContextType fContextType;
	
	/**
	 * Sets the context type.
	 */
	public void setContextType(ContextType contextType) {
		fContextType= contextType;	
	}
	
	/*
	 * @see IContentAssistProcessor#computeCompletionProposals(ITextViewer, int)
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,	int documentOffset) {

		if (fContextType == null)
			return null;

		List proposals= new ArrayList();		
		
		String text= viewer.getDocument().get();
		int start= getStart(text, documentOffset);
		int end= documentOffset;

		String string= text.substring(start, end);
		String prefix= (string.length() >= 2)
			? string.substring(2)
			: null;

		int offset= start;
		int length= end - start;

		for (Iterator iterator= fContextType.variableIterator(); iterator.hasNext(); ) {
			TemplateVariable variable= (TemplateVariable) iterator.next();

			if (prefix == null || variable.getName().startsWith(prefix))
				proposals.add(new TemplateVariableProposal(variable, offset, length, viewer));
		}

		Collections.sort(proposals, fgTemplateVariableProposalComparator);
		return (ICompletionProposal[]) proposals.toArray(new ICompletionProposal[proposals.size()]);
	}

	/* Guesses the start position of the completion */
	private int getStart(String string, int end) {
		int start= end;

		if (start >= 1 && string.charAt(start - 1) == '$')
			return start - 1;
				
		while ((start != 0) && Character.isUnicodeIdentifierPart(string.charAt(start - 1)))
			start--;

		if (start >= 2 && string.charAt(start - 1) == '{' && string.charAt(start - 2) == '$')
			return start - 2;
			
		return end;
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
		return new char[] {'$'};
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

}

