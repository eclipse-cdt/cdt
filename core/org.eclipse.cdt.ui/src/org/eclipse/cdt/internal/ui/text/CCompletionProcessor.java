package org.eclipse.cdt.internal.ui.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.index.ITagEntry;
import org.eclipse.cdt.core.index.IndexModel;
import org.eclipse.cdt.core.index.TagFlags;
import org.eclipse.cdt.internal.corext.template.ContextType;
import org.eclipse.cdt.internal.corext.template.ContextTypeRegistry;
import org.eclipse.cdt.internal.ui.CCompletionContributorManager;
import org.eclipse.cdt.internal.ui.CElementLabelProvider;
import org.eclipse.cdt.internal.ui.CPlugin;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.text.template.TemplateEngine;
import org.eclipse.cdt.ui.IFunctionSummary;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;

/**
 * C completion processor.
 */
public class CCompletionProcessor implements IContentAssistProcessor {
	
	private CEditor fEditor;
	private char[] fProposalAutoActivationSet;
	private CCompletionProposalComparator fComparator;
	private TemplateEngine fTemplateEngine;
	
	private boolean fRestrictToMatchingCase;
	private boolean fAllowAddIncludes;
	
	private CElementLabelProvider fElementLabelProvider;
	//private ImageRegistry fImageRegistry;
	
	
	public CCompletionProcessor(IEditorPart editor) {
		fEditor= (CEditor)editor;
		ContextType contextType= ContextTypeRegistry.getInstance().getContextType("C"); //$NON-NLS-1$
		if (contextType != null)
			fTemplateEngine= new TemplateEngine(contextType);
		fRestrictToMatchingCase= false;
		fAllowAddIncludes= true;
		
		fComparator= new CCompletionProposalComparator();
		
		fElementLabelProvider = new CElementLabelProvider();
		//fImageRegistry= CPlugin.getDefault().getImageRegistry();
	}
	
	/**
	 * Tells this processor to order the proposals alphabetically.
	 * 
	 * @param order <code>true</code> if proposals should be ordered.
	 */
	public void orderProposalsAlphabetically(boolean order) {
		fComparator.setOrderAlphabetically(order);
	}
	
	/**
	 * @see IContentAssistProcessor#getErrorMessage()
	 */
	public String getErrorMessage() {
		return null;
	}

	/**
	 * @see IContentAssistProcessor#getContextInformationValidator()
	 */
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	/**
	 * @see IContentAssistProcessor#getContextInformationAutoActivationCharacters()
	 */
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	/**
	 * @see IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
	 */
	public char[] getCompletionProposalAutoActivationCharacters() {
		return fProposalAutoActivationSet;
	}
	
	/**
	 * Sets this processor's set of characters triggering the activation of the
	 * completion proposal computation.
	 * 
	 * @param activationSet the activation set
	 */
	public void setCompletionProposalAutoActivationCharacters(char[] activationSet) {
		fProposalAutoActivationSet= activationSet;
	}

	/**
	 * @see IContentAssistProcessor#computeContextInformation(ITextViewer, int)
	 */
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return null;
	}
	
	/**
	 * Tells this processor to restrict is proposals to those
	 * starting with matching cases.
	 * 
	 * @param restrict <code>true</code> if proposals should be restricted
	 */
	public void restrictProposalsToMatchingCases(boolean restrict) {
		// not yet supported
	}
	
	/**
	 * Tells this processor to add include statement for proposals that have
	 * a fully qualified type name
	 * 
	 * @param restrict <code>true</code> if import can be added
	 */
	public void allowAddingIncludes(boolean allowAddingIncludes) {
		fAllowAddIncludes= allowAddingIncludes;
	}

	/**
	 * @see IContentAssistProcessor#computeCompletionProposals(ITextViewer, int)
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		//IDocument unit= fManager.getWorkingCopy(fEditor.getEditorInput());
		IDocument document= viewer.getDocument();

		ICCompletionProposal[] results = null;

		try {
			if (document != null) {
				
				int offset= documentOffset;
				int length= 0;
				
				Point selection= viewer.getSelectedRange();
				if (selection.y > 0) {
					offset= selection.x;
					length= selection.y;
				}
				
				//CCompletionEvaluator evaluator= new CCompletionEvaluator(document, offset, length);
				//evaluator.restrictProposalsToMatchingCases(fRestrictToMatchingCase);
				results= evalProposals(document, offset, length);
			}
		} catch (Exception e) {
			CPlugin.log(e);
		}
		
		if(results == null) 
			results = new ICCompletionProposal[0];

		if (fTemplateEngine != null) {
			try {
				fTemplateEngine.reset();
				fTemplateEngine.complete(viewer, documentOffset, null);
			} catch (Exception x) {
				System.out.println("Template Exception");
				CPlugin.log(x);
			}				
			
			ICCompletionProposal[] templateResults= fTemplateEngine.getResults();
			if (results.length == 0) {
				results= templateResults;
			} else {
				// concatenate arrays
				ICCompletionProposal[] total= new ICCompletionProposal[results.length + templateResults.length];
				System.arraycopy(templateResults, 0, total, 0, templateResults.length);
				System.arraycopy(results, 0, total, templateResults.length, results.length);
				results= total;
			}
		}

		/*
		 * Order here and not in result collector to make sure that the order
		 * applies to all proposals and not just those of the compilation unit. 
		 */
		order(results);
		if((results.length == 1) && (CPlugin.getDefault().getPreferenceStore().getBoolean(ContentAssistPreference.AUTOINSERT))) {
			results[0].apply(document);
			// Trick the content assistant into thinking we have no proposals
			return new ICCompletionProposal[0];
		} else {
			return results;
		}
	}
	
	/**
	 * Order the given proposals.
	 */
	private ICCompletionProposal[] order(ICCompletionProposal[] proposals) {
		Arrays.sort(proposals, fComparator);
		return proposals;	
	}
	
	/**
	 * Evaluate the actual proposals for C
	 */
	private ICCompletionProposal[] evalProposals(IDocument document, int pos, int length) {
		IRegion region;
		String frag = "";

		// First, check if we're on a space or trying to open a struct/union
		if (pos > 2) {
			try {
				// If we're on a space and the previous character is valid text,
				// parse the previous word.
				if (!Character.isJavaIdentifierPart(document.getChar(pos))) {
					pos--;
					if (!Character.isJavaIdentifierPart(document.getChar(pos))) {
						// Comment out the dereference code, only useful once we can
						// know variable types to go fish back structure members
						//if (document.getChar(offset) == '.') {
						//	isDereference = true;
						//	offset--;
						//} else if ((document.getChar(offset) == '>') && (document.getChar(offset - 1) == '-')) {
						//	isDereference = true;
						//	offset -= 2;
						//}
					}
				}
			} catch (Exception e) {
				return null;
			}
		}

		// Get the current "word"
		region = CWordFinder.findWord(document, pos);

		// If we're currently
		try {
			frag = document.get(region.getOffset(), region.getLength());
			frag = frag.trim();
			if (frag.length() == 0)
				// No word is selected...
				return null;
		} catch (BadLocationException x) {
			// ignore
			return null;
		}
		// Based on the frag name, build a list of completion proposals
		// We look in two places: the content outline and the libs


		ArrayList completions = new ArrayList();

		// Look in index manager
		IndexModel model = IndexModel.getDefault();
		IProject project = null;
		IEditorInput input = fEditor.getEditorInput();
		if(input instanceof IFileEditorInput) {
			project = ((IFileEditorInput)input).getFile().getProject();

			// Bail out quickly, if the project was deleted.
			if (!project.exists()) {
				project = null;
			}
		}
		if(project != null) {
			ITagEntry[] tags= model.query(project, frag+"*", false, false);
			if(tags != null && tags.length > 0) {
				// We have some matches!
				for(int i = 0; i < tags.length; i++) {

					String fname = tags[i].getTagName();

					int kind = tags[i].getKind();

					if(kind == TagFlags.T_FUNCTION || kind == TagFlags.T_PROTOTYPE) {
						fname = fname + "()";
					}
					String proto = fname + " - " + tags[i].getPattern();
					//System.out.println("tagmatch " + fname + " proto " + proto + " type" + tags[i].getKind());
					if(tags[i].getKind() != TagFlags.T_MEMBER) {
						completions.add(
							new CCompletionProposal(
							fname,
							region.getOffset(),
							region.getLength(),
							//fname.length() + 1,
							getTagImage(kind),
							proto.equals("") ? (fname + "()") : proto,
							//null,
							//null));
							3));
					}
				}
			}
		}
				
		
		IFunctionSummary[] summary;
			
		//UserHelpFunctionInfo inf = plugin.getFunctionInfo();
		summary = CCompletionContributorManager.getDefault().getMatchingFunctions(frag);
		if(summary != null) {
			for(int i = 0; i < summary.length; i++) {
				String fname = summary[i].getName();
				String proto = summary[i].getPrototype();
				completions.add(
					new CCompletionProposal(
						fname + "()",
						region.getOffset(),
						region.getLength(),
						//fname.length() + 1,
						CPluginImages.get(CPluginImages.IMG_OBJS_FUNCTION),
						proto.equals("") ? (fname + "()") : proto,
						//null,
						//null));
						2));
			}
		}
		return (ICCompletionProposal[]) completions.toArray(new ICCompletionProposal[0]);
	}
	
	private Image getTagImage(int kind) {
		switch (kind) {
			case TagFlags.T_PROTOTYPE:
				return CPluginImages.get(CPluginImages.IMG_OBJS_DECLARATION);
			case TagFlags.T_CLASS:
				return CPluginImages.get(CPluginImages.IMG_OBJS_CLASS);
			case TagFlags.T_ENUM:
			case TagFlags.T_VARIABLE:
			case TagFlags.T_MEMBER:
				return CPluginImages.get(CPluginImages.IMG_OBJS_FIELD);
			case TagFlags.T_FUNCTION:
				return CPluginImages.get(CPluginImages.IMG_OBJS_FUNCTION);
			case TagFlags.T_STRUCT:
				return CPluginImages.get(CPluginImages.IMG_OBJS_STRUCT);
			case TagFlags.T_UNION:
				return CPluginImages.get(CPluginImages.IMG_OBJS_UNION);
			case TagFlags.T_MACRO:
				return CPluginImages.get(CPluginImages.IMG_OBJS_MACRO);
		}
		return CPluginImages.get(CPluginImages.IMG_OBJS_FUNCTION);
	}
	
}

