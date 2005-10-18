/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.PDOM;
import org.eclipse.cdt.core.dom.IASTServiceProvider.UnsupportedDialectException;
import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.cdt.internal.ui.text.CParameterListValidator;
import org.eclipse.cdt.internal.ui.util.ExternalEditorInput;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICCompletionProposal;
import org.eclipse.cdt.ui.text.contentassist.ICompletionContributor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.ui.IEditorPart;

/**
 * @author Doug Schaefer
 */
public class CCompletionProcessor2 implements IContentAssistProcessor {

	private IEditorPart editor;
	private String errorMessage;
    
    private char[] autoActivationChars;
	
	// Property names
	private String assistPrefix = "CEditor.contentassist"; //$NON-NLS-1$
	private String noCompletions = assistPrefix + ".noCompletions"; //$NON-NLS-1$
	private String parseError = assistPrefix + ".parseError"; //$NON-NLS-1$
	private String dialectError = assistPrefix + ".badDialect"; //$NON-NLS-1$
	
	public CCompletionProcessor2(IEditorPart editor) {
		this.editor = editor;
	}
	
	public ICompletionProposal[] computeCompletionProposals(final ITextViewer viewer, int offset) {
		try {
			IWorkingCopy workingCopy = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editor.getEditorInput());
			ASTCompletionNode completionNode = null;
            String prefix = null;
            
            IPreferenceStore store = CUIPlugin.getDefault().getPreferenceStore();
            boolean fileScope = store.getBoolean(ContentAssistPreference.CURRENT_FILE_SEARCH_SCOPE);
            boolean projectScope = store.getBoolean(ContentAssistPreference.PROJECT_SEARCH_SCOPE);

            if (fileScope && workingCopy != null) { // do a full parse

                IFile file = (IFile)workingCopy.getResource();
                if (file != null) {
                	IProject project = file.getProject();
                	IPDOM pdom = PDOM.getPDOM(project);
                	ICodeReaderFactory readerFactory;
                	if (pdom != null)
                		readerFactory = pdom.getCodeReaderFactory(workingCopy);
                	else
                		readerFactory = CDOM.getInstance().getCodeReaderFactory(CDOM.PARSE_WORKING_COPY_WHENEVER_POSSIBLE); 
                    completionNode = CDOM.getInstance().getCompletionNode(file, offset, readerFactory);
                } else if (editor.getEditorInput() instanceof ExternalEditorInput) {
                    IStorage storage = ((ExternalEditorInput)(editor.getEditorInput())).getStorage();
                    IProject project = workingCopy.getCProject().getProject();
                	IPDOM pdom = PDOM.getPDOM(project);
                	ICodeReaderFactory readerFactory;
                	if (pdom != null)
                		readerFactory = pdom.getCodeReaderFactory(workingCopy);
                	else
                		readerFactory = CDOM.getInstance().getCodeReaderFactory(CDOM.PARSE_WORKING_COPY_WHENEVER_POSSIBLE); 
                    completionNode = CDOM.getInstance().getCompletionNode(storage, project, offset, readerFactory);
                }
                
                if (completionNode != null)
                    prefix = completionNode.getPrefix();

            }
            
            if (prefix == null)
                prefix = scanPrefix(viewer.getDocument(), offset);
            
			errorMessage = CUIMessages.getString(noCompletions);
			
			List proposals = new ArrayList();
			
			IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(CUIPlugin.PLUGIN_ID, "completionContributors"); //$NON-NLS-1$
			if (point == null)
				return null;
			IExtension[] extensions = point.getExtensions();
			for (int i = 0; i < extensions.length; ++i) {
				IConfigurationElement[] elements = extensions[i].getConfigurationElements();
				for (int j = 0; j < elements.length; ++j) {
					IConfigurationElement element = elements[j];
					if (!"contributor".equals(element.getName())) //$NON-NLS-1$
						continue;
					Object contribObject = element.createExecutableExtension("class"); //$NON-NLS-1$
					if (!(contribObject instanceof ICompletionContributor))
						continue;
					ICompletionContributor contributor = (ICompletionContributor)contribObject;
					contributor.contributeCompletionProposals(viewer, offset, workingCopy, completionNode, prefix, proposals);
				}
			}

			ICCompletionProposal[] propsArray = null;
			
			if (!proposals.isEmpty()) {
				errorMessage = null;
				propsArray = (ICCompletionProposal[])proposals.toArray(new ICCompletionProposal[proposals.size()]);
				CCompletionProposalComparator propsComp = new CCompletionProposalComparator();
				propsComp.setOrderAlphabetically(true);
				Arrays.sort(propsArray, propsComp); 
				
				// remove duplicates but leave the ones with return types
                
				int last = 0;
				int removed = 0;
				for (int i = 1; i < propsArray.length; ++i) {
					if (propsComp.compare(propsArray[last], propsArray[i]) == 0) {
                        // We want to leave the one that has the return string if any
                        boolean lastReturn = propsArray[last].getIdString() != propsArray[last].getDisplayString();
                        boolean iReturn = propsArray[i].getIdString() != propsArray[i].getDisplayString();

                        if (!lastReturn && iReturn)
                            // flip i down to last
                            propsArray[last] = propsArray[i];

                        // Remove the duplicate
						propsArray[i] = null;
                        ++removed;
					} else
						// update last
						last = i;
				}
				if (removed > 0) {
					// Strip out the null entries
					ICCompletionProposal[] newArray = new ICCompletionProposal[propsArray.length - removed];
					int j = 0;
					for (int i = 0; i < propsArray.length; ++i)
						if (propsArray[i] != null)
							newArray[j++] = propsArray[i];
					propsArray = newArray;
				}
			}
			
			return propsArray;
			
		} catch (UnsupportedDialectException e) {
			errorMessage = CUIMessages.getString(dialectError);
		} catch (Throwable e) {
			errorMessage = e.toString();
		}
		
		return null;
	}

    private String scanPrefix(IDocument document, int end) {
        try {
            int start = end;
            while ((start != 0) && Character.isUnicodeIdentifierPart(document.getChar(start - 1)))
                start--;
            
            if ((start != 0) && Character.isUnicodeIdentifierStart(document.getChar(start - 1)))
                start--;

            return document.get(start, end - start);
        } catch (BadLocationException e) {
            return null;
        }
    }
    
	public IContextInformation[] computeContextInformation(ITextViewer viewer,
			int offset) {
		// TODO Auto-generated method stub
		return null;
	}

	public char[] getCompletionProposalAutoActivationCharacters() {
		return autoActivationChars;
	}

    public void setCompletionProposalAutoActivationCharacters(char[] autoActivationChars) {
        this.autoActivationChars = autoActivationChars;
    }

	public char[] getContextInformationAutoActivationCharacters() {
		return null; // none
	}

    public String getErrorMessage() {
		return errorMessage;
	}

	public IContextInformationValidator getContextInformationValidator() {
		return new CParameterListValidator();
	}

    public void orderProposalsAlphabetically(boolean order) {
    }

    public void allowAddingIncludes(boolean allowAddingIncludes) {
    }
    
}    
