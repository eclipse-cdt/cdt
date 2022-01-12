/*******************************************************************************
 * Copyright (c) 2006, 2015 Red Hat, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.editors;

import java.util.ArrayList;

import org.eclipse.cdt.internal.autotools.ui.text.hover.AutoconfTextHover;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

public class AutoconfMacroContentAssistProcessor implements IContentAssistProcessor {

	protected AutoconfEditor editor;

	/**
	 * @since 2.0
	 */
	public AutoconfMacroContentAssistProcessor(AutoconfEditor editor) {
		this.editor = editor;
	}

	private int computeMacroStart(IDocument document, int offset) {
		try {
			while (Character.isJavaIdentifierPart(document.getChar(offset - 1))) {
				--offset;
			}
		} catch (BadLocationException e) {
			// Do nothing
		}
		return offset;
	}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {

		IDocument document = viewer.getDocument();
		String prefix = ""; //$NON-NLS-1$
		AutoconfMacro[] macros = AutoconfTextHover.getMacroList(editor);
		try {
			int macroStart = computeMacroStart(document, offset);
			if (macroStart < offset) {
				prefix = prefix + document.get(macroStart, offset - macroStart);
			}
		} catch (BadLocationException e) {
			// Do nothing.  Leave prefix empty.
		}
		ICompletionProposal[] result = null;
		if (macros != null) {
			ArrayList<ICompletionProposal> validList = new ArrayList<>();
			for (int i = 0; i < macros.length; ++i) {
				String name = macros[i].getName();
				if (name.length() >= prefix.length()) {
					if (name.startsWith(prefix)) {
						String template = macros[i].getTemplate();
						int cursorPos = template.length();
						int prefixLen = prefix.length();
						if (template.charAt(template.length() - 1) == ')')
							cursorPos -= 1;
						AutoconfMacroProposalContextInformation ci = null;
						if (macros[i].hasParms()) {
							// Provide parameter info as context information that
							// is tied to the completion proposal.
							ci = new AutoconfMacroProposalContextInformation(macros[i].getParms(),
									macros[i].getParms());
							ci.setContextInformationPosition(offset - prefixLen + cursorPos - 1);
						}
						ICompletionProposal cp = new CompletionProposal(template, offset - prefixLen, prefixLen,
								cursorPos, null, name, ci, AutoconfTextHover.getIndexedInfo(name, editor));
						validList.add(cp);
					}
				}
			}
			result = new ICompletionProposal[validList.size()];
			result = validList.toArray(result);
		}
		return result;
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		// TODO Auto-generated method stub
		return new AutoconfMacroParameterListValidator();
	}

	@Override
	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

}
