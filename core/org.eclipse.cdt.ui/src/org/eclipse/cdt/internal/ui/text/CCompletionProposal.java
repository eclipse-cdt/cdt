package org.eclipse.cdt.internal.ui.text;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;


public class CCompletionProposal implements ICCompletionProposal {
	private String fDisplayString;
	private String fReplacementString;
	private int fReplacementOffset;
	private int fReplacementLength;
	private int fCursorPosition;
	private Image fImage;
	private IContextInformation fContextInformation;
	private int fContextInformationPosition;
	//private ProposalInfo fProposalInfo;
	//private IImportDeclaration fImportDeclaration;
	private char[] fTriggerCharacters;
	
	private int fRelevance;

	/**
	 * Creates a new completion proposal. All fields are initialized based on the provided information.
	 *
	 * @param replacementString the actual string to be inserted into the document
	 * @param replacementOffset the offset of the text to be replaced
	 * @param replacementLength the length of the text to be replaced
	 * @param image the image to display for this proposal
	 * @param displayString the string to be displayed for the proposal
	 * If set to <code>null</code>, the replacement string will be taken as display string.
	 */
	public CCompletionProposal(String replacementString, int replacementOffset, int replacementLength, Image image, String displayString, int relevance) {
		Assert.isNotNull(replacementString);
		Assert.isTrue(replacementOffset >= 0);
		Assert.isTrue(replacementLength >= 0);
		
		fReplacementString= replacementString;
		fReplacementOffset= replacementOffset;
		fReplacementLength= replacementLength;
		fImage= image;
		fDisplayString= displayString != null ? displayString : replacementString;
		fRelevance= relevance;

		fCursorPosition= replacementString.length();
	
		fContextInformation= null;
		fContextInformationPosition= -1;
		//fIncludeDeclaration= null;
		fTriggerCharacters= null;
		//fProposalInfo= null;
	}
	
	/**
	 * Sets the context information.
	 * @param contentInformation The context information associated with this proposal
	 */
	public void setContextInformation(IContextInformation contextInformation) {
		fContextInformation= contextInformation;
		fContextInformationPosition= (fContextInformation != null ? fCursorPosition : -1);
	}
	
	/**
	 * Sets the import declaration to import when applied.
	 * @param importDeclaration Optional import declaration to be added. Can be <code>null</code>. The underlying compilation unit
	 * is assumed to be compatible with the document passed in <code>apply</code>.
	 *
	public void setIncludeDeclaration(IImportDeclaration importDeclaration) {
		fIncludeDeclaration= importDeclaration;
	} */
	
	/**
	 * Sets the trigger characters.
	 * @param triggerCharacters The set of characters which can trigger the application of this completion proposal
	 */
	public void setTriggerCharacters(char[] triggerCharacters) {
		fTriggerCharacters= triggerCharacters;
	}
	
	/**
	 * Sets the proposal info.
	 * @param additionalProposalInfo The additional information associated with this proposal or <code>null</code>
	 *
	public void setProposalInfo(ProposalInfo proposalInfo) {
		fProposalInfo= proposalInfo;
	}*/
	
	/**
	 * Sets the cursor position relative to the insertion offset. By default this is the length of the completion string
	 * (Cursor positioned after the completion)
	 * @param cursorPosition The cursorPosition to set
	 */
	public void setCursorPosition(int cursorPosition) {
		Assert.isTrue(cursorPosition >= 0);
		fCursorPosition= cursorPosition;
		fContextInformationPosition= (fContextInformation != null ? fCursorPosition : -1);
	}	
	
/*	protected void addInclude(IRequiredInclude[] inc, CFileElementWorkingCopy tu) {
		AddIncludeOperation op= new AddIncludeOperation(fEditor, tu, inc, false);
		try {
			ProgressMonitorDialog dialog= new ProgressMonitorDialog(getShell());
			dialog.run(false, true, op);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			MessageDialog.openError(getShell(), CEditorMessages.getString("AddIncludeOnSelection.error.message1"), e.getTargetException().getMessage()); //$NON-NLS-1$
		} catch (InterruptedException e) {
			// Do nothing. Operation has been canceled.
		}
	} */
	
	protected void applyIncludes(IDocument document) {
		//AddIncludeOperation(ITextEditor ed, CFileElementWorkingCopy tu, IRequiredInclude[] includes, boolean save) {
			
		//if (fIncludeDeclaration == null) {
			return;
		//}
		
		/* ICompilationUnit cu= (ICompilationUnit) JavaModelUtil.findElementOfKind(fImportDeclaration, IJavaElement.COMPILATION_UNIT);
		if (cu != null) {
			try {
				IType[] types= cu.getTypes();
				if (types.length == 0 || types[0].getSourceRange().getOffset() > fReplacementOffset) {
					// do not add import for code assist on import statements
					return;
				}

				String[] prefOrder= ImportOrganizePreferencePage.getImportOrderPreference();
				int threshold= ImportOrganizePreferencePage.getImportNumberThreshold();					
				ImportsStructure impStructure= new ImportsStructure(cu, prefOrder, threshold, true);

				impStructure.addImport(fImportDeclaration.getElementName());
				// will modify the document as the CU works on the document
				impStructure.create(false, null);

			} catch (CoreException e) {
				JavaPlugin.log(e);
			}
		} */
	}
	
	/*
	 * @see ICompletionProposalExtension#apply(IDocument, char, int)
	 */
	public void apply(IDocument document, char trigger, int offset) {
		try {
			
			// patch replacement length
			int delta= offset - (fReplacementOffset + fReplacementLength);
			if (delta > 0)
				fReplacementLength += delta;
			
			if (trigger == (char) 0) {
				replace(document, fReplacementOffset, fReplacementLength, fReplacementString);
			} else {
				StringBuffer buffer= new StringBuffer(fReplacementString);

				// fix for PR #5533. Assumes that no eating takes place.
				if ((fCursorPosition > 0 && fCursorPosition <= buffer.length() && buffer.charAt(fCursorPosition - 1) != trigger)) {
					buffer.insert(fCursorPosition, trigger);
					++fCursorPosition;
				}
				
				replace(document, fReplacementOffset, fReplacementLength, buffer.toString());
			}
			
			int oldLen= document.getLength();
			applyIncludes(document);
			fReplacementOffset += document.getLength() - oldLen;
			
		} catch (BadLocationException x) {
			// ignore
		}	
	}
	
	// #6410 - File unchanged but dirtied by code assist
	private void replace(IDocument document, int offset, int length, String string) throws BadLocationException {
		if (!document.get(offset, length).equals(string))
			document.replace(offset, length, string);
	}

	/*
	 * @see ICompletionProposal#apply
	 */
	public void apply(IDocument document) {
		apply(document, (char) 0, fReplacementOffset + fReplacementLength);
	}
	
	/*
	 * @see ICompletionProposal#getSelection
	 */
	public Point getSelection(IDocument document) {
		return new Point(fReplacementOffset + fCursorPosition, 0);
	}

	/*
	 * @see ICompletionProposal#getContextInformation()
	 */
	public IContextInformation getContextInformation() {
		return fContextInformation;
	}

	/*
	 * @see ICompletionProposal#getImage()
	 */
	public Image getImage() {
		return fImage;
	}

	/*
	 * @see ICompletionProposal#getDisplayString()
	 */
	public String getDisplayString() {
		return fDisplayString;
	}

	/*
	 * @see ICompletionProposal#getAdditionalProposalInfo()
	 */
	public String getAdditionalProposalInfo() {
		//if (fProposalInfo != null) {
		//	return fProposalInfo.getInfo();
		//}
		return null;
	}
	
	/*
	 * @see ICompletionProposalExtension#getTriggerCharacters()
	 */
	public char[] getTriggerCharacters() {
		return fTriggerCharacters;
	}

	/*
	 * @see ICompletionProposalExtension#getContextInformationPosition()
	 */
	public int getContextInformationPosition() {
		return fReplacementOffset + fContextInformationPosition;
	}
	
	/**
	 * Gets the replacement offset.
	 * @return Returns a int
	 */
	public int getReplacementOffset() {
		return fReplacementOffset;
	}

	/**
	 * Sets the replacement offset.
	 * @param replacementOffset The replacement offset to set
	 */
	public void setReplacementOffset(int replacementOffset) {
		Assert.isTrue(replacementOffset >= 0);
		fReplacementOffset= replacementOffset;
	}	

	/**
	 * Gets the replacement length.
	 * @return Returns a int
	 */
	public int getReplacementLength() {
		return fReplacementLength;
	}

	/**
	 * Sets the replacement length.
	 * @param replacementLength The replacementLength to set
	 */
	public void setReplacementLength(int replacementLength) {
		Assert.isTrue(replacementLength >= 0);
		fReplacementLength= replacementLength;
	}

	/**
	 * Gets the replacement string.
	 * @return Returns a String
	 */
	public String getReplacementString() {
		return fReplacementString;
	}

	/**
	 * Sets the replacement string.
	 * @param replacementString The replacement string to set
	 */
	public void setReplacementString(String replacementString) {
		fReplacementString= replacementString;
	}

	/**
	 * Sets the image.
	 * @param image The image to set
	 */
	public void setImage(Image image) {
		fImage= image;
	}

	/*
	 * @see ICompletionProposalExtension#isValidFor(IDocument, int)
	 */
	public boolean isValidFor(IDocument document, int offset) {
		
		if (offset < fReplacementOffset)
			return false;
		
		int replacementLength= fReplacementString == null ? 0 : fReplacementString.length();
		if (offset >=  fReplacementOffset + replacementLength)
			return false;
		
		try {
			int length= offset - fReplacementOffset;
			String start= document.get(fReplacementOffset, length);
			return fReplacementString.substring(0, length).equalsIgnoreCase(start);
		} catch (BadLocationException x) {
		}		
		
		return false;
	}
	
	/**
	 * Gets the proposal's relevance.
	 * @return Returns a int
	 */
	public int getRelevance() {
		return fRelevance;
	}

	/**
	 * Sets the proposal's relevance.
	 * @param relevance The relevance to set
	 */
	public void setRelevance(int relevance) {
		fRelevance= relevance;
	}

}

