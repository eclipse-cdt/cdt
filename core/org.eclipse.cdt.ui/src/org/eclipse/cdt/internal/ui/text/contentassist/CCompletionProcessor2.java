/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import java.util.Arrays;

import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.IASTServiceProvider.UnsupportedDialectException;
import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;

/**
 * @author Doug Schaefer
 */
public class CCompletionProcessor2 implements IContentAssistProcessor {

	private IEditorPart editor;
	private String errorMessage;
	
	public CCompletionProcessor2(IEditorPart editor) {
		this.editor = editor;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
	 */
	public ICompletionProposal[] computeCompletionProposals(final ITextViewer viewer,
			int offset) {
		errorMessage = null;
		try {
			IWorkingCopy workingCopy = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editor.getEditorInput());
			ASTCompletionNode completionNode = CDOM.getInstance().getCompletionNode(
				(IFile)workingCopy.getResource(),
				offset,
				new ICodeReaderFactory() {
					public CodeReader createCodeReaderForTranslationUnit(String path) {
						return new CodeReader(viewer.getDocument().get().toCharArray());
					}
					public CodeReader createCodeReaderForInclusion(String path) {
						return ParserUtil.createReader(path,
							Arrays.asList(CUIPlugin.getSharedWorkingCopies()).iterator());
					}
					public int getUniqueIdentifier() {
						return 99;
					}
				}	
			);
			IASTName[] names = completionNode.getNames();
			IASTTranslationUnit tu = names[0].getTranslationUnit();
			IBinding binding = names[0].resolveBinding();
			int repLength = completionNode.getLength();
			int repOffset = offset - repLength;
			
			ICompletionProposal prop = createBindingCompletionProposal(binding, repOffset, repLength);
			ICompletionProposal prop2 = createBindingCompletionProposal(binding, repOffset, repLength);
			
			return new ICompletionProposal[] { prop, prop2 };
		} catch (UnsupportedDialectException e) {
			errorMessage = "Unsupported Dialect Exception";
		} catch (Throwable e) {
			errorMessage = e.toString();
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer, int)
	 */
	public IContextInformation[] computeContextInformation(ITextViewer viewer,
			int offset) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
	 */
	public char[] getCompletionProposalAutoActivationCharacters() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationAutoActivationCharacters()
	 */
	public char[] getContextInformationAutoActivationCharacters() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getErrorMessage()
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationValidator()
	 */
	public IContextInformationValidator getContextInformationValidator() {
		// TODO Auto-generated method stub
		return null;
	}

	private ICompletionProposal createBindingCompletionProposal(IBinding binding, int offset, int length) {
		ImageDescriptor imageDescriptor = null;
		if (binding instanceof ITypedef)
			imageDescriptor = CElementImageProvider.getTypedefImageDescriptor();
		
		Image image = imageDescriptor != null
			? CUIPlugin.getImageDescriptorRegistry().get( imageDescriptor )
			: null;

		return new CCompletionProposal(binding.getName(), offset, length, image, binding.getName(), 1);
	}
}
