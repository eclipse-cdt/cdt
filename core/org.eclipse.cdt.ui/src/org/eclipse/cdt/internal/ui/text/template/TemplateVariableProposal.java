package org.eclipse.cdt.internal.ui.text.template;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.cdt.internal.corext.template.TemplateMessages;
import org.eclipse.cdt.internal.corext.template.TemplateVariable;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;


/**
 * A proposal for insertion of template variables.
 */
public class TemplateVariableProposal implements ICompletionProposal {

	private TemplateVariable fVariable;
	private int fOffset;
	private int fLength;	
	private ITextViewer fViewer;
	
	private Point fSelection;

	/**
	 * Creates a template variable proposal.
	 * 
	 * @param variable the template variable
	 * @param offset the offset to replace
	 * @param length the length to replace
	 * @param viewer the viewer
	 */
	public TemplateVariableProposal(TemplateVariable variable, int offset, int length, ITextViewer viewer) {
		fVariable= variable;
		fOffset= offset;
		fLength= length;
		fViewer= viewer;
	}
	
	/*
	 * @see ICompletionProposal#apply(IDocument)
	 */
	public void apply(IDocument document) {

		try {
			String variable= fVariable.getName().equals("dollar") ? "$$" : "${" + fVariable.getName() + '}'; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			document.replace(fOffset, fLength, variable);
			fSelection= new Point(fOffset + variable.length(), 0);

		} catch (BadLocationException e) {
			CUIPlugin.getDefault().log(e);

			Shell shell= fViewer.getTextWidget().getShell();
			MessageDialog.openError(shell, TemplateMessages.getString("TemplateVariableProposal.error.title"), e.getMessage()); //$NON-NLS-1$
		}
	}

	/*
	 * @see ICompletionProposal#getSelection(IDocument)
	 */
	public Point getSelection(IDocument document) {
		return fSelection;
	}

	/*
	 * @see ICompletionProposal#getAdditionalProposalInfo()
	 */
	public String getAdditionalProposalInfo() {
		return null;
	}

	/*
	 * @see ICompletionProposal#getDisplayString()
	 */
	public String getDisplayString() {
		return fVariable.getName() + " - " + fVariable.getDescription(); //$NON-NLS-1$
	}

	/*
	 * @see ICompletionProposal#getImage()
	 */
	public Image getImage() {
		return null;
	}

	/*
	 * @see ICompletionProposal#getContextInformation()
	 */
	public IContextInformation getContextInformation() {
		return null;
	}
}