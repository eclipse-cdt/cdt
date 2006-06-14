/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.internal.ui.util.ImageDescriptorRegistry;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.FunctionPrototypeSummary;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.graphics.Image;

/**
 *
 * The Result Collector class receives information from the completion engine
 * as a completion requestor. It might also receive information from others 
 * such as the results of a search operation to generate completion proposals.
 * 
 */
public class ResultCollector extends CompletionRequestorAdaptor {
	
	private final static char[] METHOD_WITH_ARGUMENTS_TRIGGERS= new char[] { '(', '-', ' ' };
	private final static char[] METHOD_TRIGGERS= new char[] { ';', ',', '.', '\t', '[', ' ' };
	private final static char[] TYPE_TRIGGERS= new char[] { '.', '\t', '[', '(', ' ' };
	private final static char[] VAR_TRIGGERS= new char[] { '\t', ' ', '[', '(', '=', '-', ';', ',', '.' };

	private final String DEFINE ="#define ";  //$NON-NLS-1$
	
	private Set completions = new HashSet();
	private ImageDescriptorRegistry registry = CUIPlugin.getImageDescriptorRegistry();
	private IProblem fLastProblem;	
	private ITextViewer fTextViewer;
	
	public ResultCollector(){
		completions.clear();
		fTextViewer = null;
	}
	
	/**
	 * @return the completion list
	 */
	public Set getCompletions() {
		return completions;
	}
	public void reset(ITextViewer viewer) {
		completions.clear();
		fTextViewer = viewer;
		fLastProblem = null;
	}
	/*
	 * Create a proposal
	 */
	public CCompletionProposal createProposal(String replaceString, String displayString, String infoString, String arguments, int contextInfoOffset, Image image, int offset, int length, int relevance){
		CCompletionProposal proposal;
			
		proposal = new CCompletionProposal(
											replaceString, // Replacement string
											offset,
											length,							
											image,
											displayString, // Display string
											relevance,
										    fTextViewer);
		
		if(arguments != null && arguments.length() > 0) {
			CProposalContextInformation info = new CProposalContextInformation(replaceString, arguments);
			info.setContextInformationPosition(contextInfoOffset - 1);
			proposal.setContextInformation( info );
		}
			
		// The info string could be populated with documentation info.
		// For now, it has the name and the parent's name if available.
		if((infoString != null)&& (!displayString.equals(infoString)))
			proposal.setAdditionalProposalInfo(infoString);
		
		return proposal;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICompletionRequestor#acceptField(java.lang.String, java.lang.String, org.eclipse.cdt.core.parser.ast.ASTAccessVisibility, int, int, int)
	 */
	public void acceptField(String name, String returnType, ASTAccessVisibility visibility, 
	int completionStart, int completionLength, int relevance) {
		String replaceString = ""; //$NON-NLS-1$
		String displayString = ""; //$NON-NLS-1$
		Image image = null;
		StringBuffer infoString = new StringBuffer();
		
		// fill the replace, display and info strings
		replaceString = name;
		displayString = name;
		if(returnType != null)
			displayString+= " : " + returnType; //$NON-NLS-1$
	
		// get the image 	
		ImageDescriptor imageDescriptor = CElementImageProvider.getFieldImageDescriptor(visibility);
		image = registry.get( imageDescriptor );

		// create proposal and add it to completions list
		CCompletionProposal proposal = createProposal(replaceString, displayString, infoString.toString(), 
			null, 0, image, completionStart, completionLength, relevance);
		proposal.setTriggerCharacters(VAR_TRIGGERS);
		completions.add(proposal);		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICompletionRequestor#acceptClass(java.lang.String, int, int, int)
	 */
	public void acceptClass(
		String name,
		int completionStart,
		int completionLength,
		int relevance) {		
			String replaceString = ""; //$NON-NLS-1$
			String displayString = ""; //$NON-NLS-1$
			Image image = null;
			StringBuffer infoString = new StringBuffer();
		
			// fill the replace, display and info strings
			replaceString = name;
			displayString = name;
	
			// get the image 	
			ImageDescriptor imageDescriptor = CElementImageProvider.getClassImageDescriptor();
			image = registry.get( imageDescriptor );

			// create proposal and add it to completions list
			CCompletionProposal proposal = createProposal(replaceString, displayString, infoString.toString(), 
				null, 0, image, completionStart, completionLength, relevance);
			proposal.setTriggerCharacters(TYPE_TRIGGERS);
			completions.add(proposal);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICompletionRequestor#acceptFunction(java.lang.String, java.lang.String, java.lang.String, int, int, int)
	 */
	public void acceptFunction(
		String name,
		String parameterString,
		String returnType,
		int completionStart,
		int completionLength,
		int relevance, boolean insertFunctionName, int contextInfoOffset ) {
			String replaceString = ""; //$NON-NLS-1$
			String displayString = ""; //$NON-NLS-1$
			String arguments = ""; //$NON-NLS-1$
			
			Image image = null;
			StringBuffer infoString = new StringBuffer();
		
			// fill the replace, display and info strings
			replaceString = name;
			displayString = name;
			String functionPrototype = returnType + " " + name; //$NON-NLS-1$
			if(parameterString != null){
				if ((parameterString.indexOf("(") == -1) && (parameterString.indexOf(")") == -1))  //$NON-NLS-1$ //$NON-NLS-2$
				{	
					functionPrototype += "(" + parameterString + ")"; //$NON-NLS-1$ //$NON-NLS-2$
				}
				else {
					functionPrototype += parameterString;
				}
			}

			FunctionPrototypeSummary fproto = new FunctionPrototypeSummary(functionPrototype);
			if(fproto != null) {						
				replaceString = fproto.getName() + "()"; //$NON-NLS-1$
				displayString = fproto.getPrototypeString(true);
				infoString.append(displayString);
				arguments = fproto.getArguments();
			}
	
			// get the image 	
			ImageDescriptor imageDescriptor = CElementImageProvider.getFunctionImageDescriptor();
			image = registry.get( imageDescriptor );

			if( !insertFunctionName ){
				replaceString = "";  //$NON-NLS-1$
			}
			// create proposal and add it to completions list
			CCompletionProposal proposal = createProposal(replaceString, displayString, infoString.toString(), 
				arguments, contextInfoOffset, image, completionStart, completionLength, relevance);
			
			boolean userMustCompleteParameters= (arguments != null && arguments.length() > 0) && insertFunctionName;

			char[] triggers= userMustCompleteParameters ? METHOD_WITH_ARGUMENTS_TRIGGERS : METHOD_TRIGGERS;
			proposal.setTriggerCharacters(triggers);

			if (userMustCompleteParameters) {
				// set the cursor before the closing bracket
				proposal.setCursorPosition(replaceString.length() - 1);
			}			

			completions.add(proposal);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICompletionRequestor#acceptLocalVariable(java.lang.String, java.lang.String, int, int, int)
	 */
	public void acceptLocalVariable(
		String name,
		String returnType,
		int completionStart,
		int completionLength,
		int relevance) {
		
		String replaceString = ""; //$NON-NLS-1$
		String displayString = ""; //$NON-NLS-1$
		Image image = null;
		StringBuffer infoString = new StringBuffer();
		
		// fill the replace, display and info strings
		replaceString = name;
		displayString = name;
		if(returnType != null)
			displayString+= " : " + returnType; //$NON-NLS-1$
		
		// get the image 	
		ImageDescriptor imageDescriptor = CElementImageProvider.getLocalVariableImageDescriptor();
		image = registry.get( imageDescriptor );

		// create proposal and add it to completions list
		CCompletionProposal proposal = createProposal(replaceString, displayString, infoString.toString(), null, 0, image, completionStart, completionLength, relevance);
		proposal.setTriggerCharacters(VAR_TRIGGERS);
		completions.add(proposal);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICompletionRequestor#acceptMacro(java.lang.String, int, int, int)
	 */
	public void acceptMacro(
		String name,
		int completionStart,
		int completionLength,
		int relevance, int contextInfoOffset) {
		
			String replaceString = ""; //$NON-NLS-1$
			String displayString = ""; //$NON-NLS-1$
			String arguments = ""; //$NON-NLS-1$
			Image image = null;
			StringBuffer infoString = new StringBuffer();
			String prototype = ""; //$NON-NLS-1$
			
			// fill the replace, display and info strings
			int bracket = name.indexOf('{');
			if(name.startsWith(DEFINE)){
				prototype = name.substring(DEFINE.length(), bracket == -1? name.length() : bracket);				
			}else {
				prototype = name;
			}
			int leftbracket = prototype.indexOf('(');
			int rightbracket = prototype.lastIndexOf(')');
			if(( leftbracket == -1 ) && (rightbracket == -1)) {
				replaceString = prototype;
				displayString = prototype;
			}else {
				FunctionPrototypeSummary fproto = new FunctionPrototypeSummary(prototype);
				if(fproto != null) {						
					replaceString = fproto.getName() + "()"; //$NON-NLS-1$
					displayString = fproto.getPrototypeString(true, false);
					infoString.append(displayString);
					arguments = fproto.getArguments();
				} else {
					replaceString = prototype;
					displayString = prototype;
				}
			}
			// get the image 	
			ImageDescriptor imageDescriptor = CElementImageProvider.getMacroImageDescriptor();
			image = registry.get( imageDescriptor );

			// create proposal and add it to completions list
			CCompletionProposal proposal = createProposal(replaceString, displayString, infoString.toString(), 
				arguments, contextInfoOffset, image, completionStart, completionLength, relevance);

			proposal.setTriggerCharacters(VAR_TRIGGERS);

			boolean userMustCompleteParameters= (arguments != null && arguments.length() > 0);
			if (userMustCompleteParameters) {
				// set the cursor before the closing bracket
				proposal.setCursorPosition(replaceString.length() - 1);
			}			
			
			completions.add(proposal);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICompletionRequestor#acceptMethod(java.lang.String, java.lang.String, java.lang.String, org.eclipse.cdt.core.parser.ast.ASTAccessVisibility, int, int, int)
	 */
	public void acceptMethod(
		String name,
		String parameterString,
		String returnType,
		ASTAccessVisibility visibility,
		int completionStart,
		int completionLength,
		int relevance, boolean insertFunctionName, int contextInfoOffset) {
		
			String replaceString = ""; //$NON-NLS-1$
			String displayString = ""; //$NON-NLS-1$
			String arguments = ""; //$NON-NLS-1$
			Image image = null;
			StringBuffer infoString = new StringBuffer();
		
			// fill the replace, display and info strings
			replaceString = name;
			displayString = name;
			String functionPrototype = returnType + " " + name; //$NON-NLS-1$
			if(parameterString != null){
				if ((parameterString.indexOf("(") == -1) && (parameterString.indexOf(")") == -1)) //$NON-NLS-1$ //$NON-NLS-2$
				{	
					functionPrototype += "(" + parameterString + ")"; //$NON-NLS-1$ //$NON-NLS-2$
				}
				else {
					functionPrototype += parameterString;
				}
			}
				
			FunctionPrototypeSummary fproto = new FunctionPrototypeSummary(functionPrototype);
			if(fproto != null) {						
				replaceString = fproto.getName() + "()"; //$NON-NLS-1$
				displayString = fproto.getPrototypeString(true);
				infoString.append(displayString);
				arguments = fproto.getArguments();
			}
	
			// get the image 	
			ImageDescriptor imageDescriptor = CElementImageProvider.getMethodImageDescriptor(visibility);
			image = registry.get( imageDescriptor );

			if( !insertFunctionName ){
				//completion only to display the infoString, don't actually insert text
				replaceString = ""; //$NON-NLS-1$
			}
			// create proposal and add it to completions list
			CCompletionProposal proposal = createProposal(replaceString, displayString, infoString.toString(), 
				arguments, contextInfoOffset, image, completionStart, completionLength, relevance );
			
			boolean userMustCompleteParameters= (arguments != null && arguments.length() > 0) && insertFunctionName;
			
			char[] triggers= userMustCompleteParameters ? METHOD_WITH_ARGUMENTS_TRIGGERS : METHOD_TRIGGERS;
			proposal.setTriggerCharacters(triggers);

			if (userMustCompleteParameters) {
				// set the cursor before the closing bracket
				proposal.setCursorPosition(replaceString.length() - 1);
			}
		
			completions.add(proposal);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICompletionRequestor#acceptNamespace(java.lang.String, int, int, int)
	 */
	public void acceptNamespace(
		String name,
		int completionStart,
		int completionLength,
		int relevance) {
			String replaceString = ""; //$NON-NLS-1$
			String displayString = ""; //$NON-NLS-1$
			Image image = null;
			StringBuffer infoString = new StringBuffer();
		
			// fill the replace, display and info strings
			replaceString = name;
			displayString = name;
	
			// get the image 	
			ImageDescriptor imageDescriptor = CElementImageProvider.getNamespaceImageDescriptor();
			image = registry.get( imageDescriptor );

			// create proposal and add it to completions list
			CCompletionProposal proposal = createProposal(replaceString, displayString, infoString.toString(), 
				null, 0, image, completionStart, completionLength, relevance);
			proposal.setTriggerCharacters(TYPE_TRIGGERS);
			completions.add(proposal);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICompletionRequestor#acceptStruct(java.lang.String, int, int, int)
	 */
	public void acceptStruct(
		String name,
		int completionStart,
		int completionLength,
		int relevance) {
			String replaceString = ""; //$NON-NLS-1$
			String displayString = ""; //$NON-NLS-1$
			Image image = null;
			StringBuffer infoString = new StringBuffer();
		
			// fill the replace, display and info strings
			replaceString = name;
			displayString = name;
	
			// get the image 	
			ImageDescriptor imageDescriptor = CElementImageProvider.getStructImageDescriptor();
			image = registry.get( imageDescriptor );

			// create proposal and add it to completions list
			CCompletionProposal proposal = createProposal(replaceString, displayString, infoString.toString(), null, 0, image, completionStart, completionLength, relevance);
			proposal.setTriggerCharacters(TYPE_TRIGGERS);
			completions.add(proposal);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICompletionRequestor#acceptUnion(java.lang.String, int, int, int)
	 */
	public void acceptUnion(
		String name,
		int completionStart,
		int completionLength,
		int relevance) {
			String replaceString = ""; //$NON-NLS-1$
			String displayString = ""; //$NON-NLS-1$
			Image image = null;
			StringBuffer infoString = new StringBuffer();
		
			// fill the replace, display and info strings
			replaceString = name;
			displayString = name;
	
			// get the image 	
			ImageDescriptor imageDescriptor = CElementImageProvider.getUnionImageDescriptor();
			image = registry.get( imageDescriptor );

			// create proposal and add it to completions list
			CCompletionProposal proposal = createProposal(replaceString, displayString, infoString.toString(), null, 0, image, completionStart, completionLength, relevance);
			proposal.setTriggerCharacters(TYPE_TRIGGERS);
			completions.add(proposal);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICompletionRequestor#acceptVariable(java.lang.String, java.lang.String, int, int, int)
	 */
	public void acceptVariable(
		String name,
		String returnType,
		int completionStart,
		int completionLength,
		int relevance) {
			String replaceString = ""; //$NON-NLS-1$
			String displayString = ""; //$NON-NLS-1$
			Image image = null;
			StringBuffer infoString = new StringBuffer();
		
			// fill the replace, display and info strings
			replaceString = name;
			displayString = name;
			if(returnType != null)
				displayString+= " : " + returnType; //$NON-NLS-1$
	
			// get the image 	
			ImageDescriptor imageDescriptor = CElementImageProvider.getVariableImageDescriptor();
			image = registry.get( imageDescriptor );

			// create proposal and add it to completions list
			CCompletionProposal proposal = createProposal(replaceString, displayString, infoString.toString(), null, 0, image, completionStart, completionLength, relevance);
			proposal.setTriggerCharacters(VAR_TRIGGERS);
			completions.add(proposal);		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICompletionRequestor#acceptEnumeration(java.lang.String, int, int, int)
	 */
	public void acceptEnumeration(
		String name,
		int completionStart,
		int completionLength,
		int relevance) {
			String replaceString = ""; //$NON-NLS-1$
			String displayString = ""; //$NON-NLS-1$
			Image image = null;
			StringBuffer infoString = new StringBuffer();
		
			// fill the replace, display and info strings
			replaceString = name;
			displayString = name;
	
			// get the image 	
			ImageDescriptor imageDescriptor = CElementImageProvider.getEnumerationImageDescriptor();
			image = registry.get( imageDescriptor );

			// create proposal and add it to completions list
			CCompletionProposal proposal = createProposal(replaceString, displayString, infoString.toString(), 
				null, 0, image, completionStart, completionLength, relevance);
			proposal.setTriggerCharacters(TYPE_TRIGGERS);
			completions.add(proposal);		
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICompletionRequestor#acceptEnumerator(java.lang.String, int, int, int)
	 */
	public void acceptEnumerator(
		String name,
		int completionStart,
		int completionLength,
		int relevance) {
			String replaceString = ""; //$NON-NLS-1$
			String displayString = ""; //$NON-NLS-1$
			Image image = null;
			StringBuffer infoString = new StringBuffer();
		
			// fill the replace, display and info strings
			replaceString = name;
			displayString = name;
	
			// get the image 	
			ImageDescriptor imageDescriptor = CElementImageProvider.getEnumeratorImageDescriptor();
			image = registry.get( imageDescriptor );

			// create proposal and add it to completions list
			CCompletionProposal proposal = createProposal(replaceString, displayString, infoString.toString(), 
				null, 0, image, completionStart, completionLength, relevance);
			proposal.setTriggerCharacters(VAR_TRIGGERS);
			completions.add(proposal);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.text.contentassist.ICompletionRequestor#acceptTypedef(java.lang.String, int, int, int)
	 */
	public void acceptTypedef(String name, 
			int completionStart,
			int completionLength, 
			int relevance) {
		String replaceString = ""; //$NON-NLS-1$
		String displayString = ""; //$NON-NLS-1$
		Image image = null;
		StringBuffer infoString = new StringBuffer();
	
		// fill the replace, display and info strings
		replaceString = name;
		displayString = name;

		// get the image 	
		ImageDescriptor imageDescriptor = CElementImageProvider.getTypedefImageDescriptor();
		image = registry.get( imageDescriptor );

		// create proposal and add it to completions list
		CCompletionProposal proposal = createProposal(replaceString, displayString, infoString.toString(), 
			null, 0, image, completionStart, completionLength, relevance);
		proposal.setTriggerCharacters(TYPE_TRIGGERS);
		completions.add(proposal);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICompletionRequestor#acceptKeyword(java.lang.String, int, int, int)
	 */
	public void acceptKeyword(
		String name,
		int completionStart,
		int completionLength,
		int relevance) {
		String replaceString = ""; //$NON-NLS-1$
		String displayString = ""; //$NON-NLS-1$
		Image image = null;
		StringBuffer infoString = new StringBuffer();
		
		// fill the replace, display and info strings
		replaceString = name;
		displayString = name;

		ImageDescriptor imageDescriptor = CElementImageProvider.getKeywordImageDescriptor();
		image = registry.get( imageDescriptor );

		// no image for keywords 	
		// create proposal and add it to completions list
		CCompletionProposal proposal = createProposal(replaceString, displayString, infoString.toString(), 
				null, 0, image, completionStart, completionLength, relevance);
		proposal.setTriggerCharacters(VAR_TRIGGERS);
		completions.add(proposal);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.ICompletionRequestor#acceptError(org.eclipse.cdt.core.parser.IProblem)
	 */
	public void acceptError(IProblem error) {
		fLastProblem = error;
	}

	public String getErrorMessage() {
		if (fLastProblem != null)
			return fLastProblem.getMessage();
		return ""; //$NON-NLS-1$
	}
	
}
