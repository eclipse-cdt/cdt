package org.eclipse.cdt.internal.ui.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.core.index.ITagEntry;
import org.eclipse.cdt.core.index.IndexModel;
import org.eclipse.cdt.core.index.TagFlags;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.internal.corext.template.ContextType;
import org.eclipse.cdt.internal.corext.template.ContextTypeRegistry;
import org.eclipse.cdt.internal.ui.CCompletionContributorManager;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.text.template.TemplateEngine;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.FunctionPrototypeSummary;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
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
	private IContextInformationValidator fValidator;

	private TemplateEngine[] fTemplateEngine;
	
	private boolean fRestrictToMatchingCase;
	private boolean fAllowAddIncludes;

	private CElementLabelProvider fElementLabelProvider;
	//private ImageRegistry fImageRegistry;

	public CCompletionProcessor(IEditorPart editor) {
		fEditor = (CEditor) editor;
		
		//Determine if this is a C or a C++ file for the context completion       +        //This is _totally_ ugly and likely belongs in the main editor class.
		String contextNames[] = new String[2];
		ArrayList templateList = new ArrayList(2);
		String filename = null;
		if (fEditor != null && fEditor.getEditorInput() != null) {
			filename = fEditor.getEditorInput().getName();
		}
		if (filename == null) {
			contextNames[0] = "C"; //$NON-NLS-1$
			contextNames[1] = "C++"; //$NON-NLS-1$
		} else if (filename.endsWith(".c")) { //Straight C files are always C
			contextNames[0] = "C"; //$NON-NLS-1$
		} else if (
			filename.endsWith(".cpp")
				|| filename.endsWith(".cc")
				|| filename.endsWith(".cxx")
				|| filename.endsWith(".C")
				|| filename.endsWith(".hxx")) {
			contextNames[0] = "C++"; //$NON-NLS-1$
			contextNames[1] = "C"; //$NON-NLS-1$
		} else { //Defer to the nature of the project
			IFile file = fEditor.getInputFile();
			if (file != null && CoreModel.getDefault().hasCCNature(file.getProject())) {
				contextNames[0] = "C++"; //$NON-NLS-1$
				contextNames[1] = "C"; //$NON-NLS-1$
			} else {
				contextNames[0] = "C"; //$NON-NLS-1$
			}
		}
		ContextType contextType;
		for (int i = 0; i < contextNames.length; i++) {
			contextType = ContextTypeRegistry.getInstance().getContextType(contextNames[i]);
			if (contextType != null) {
				templateList.add(new TemplateEngine(contextType));
			}
		}
		fTemplateEngine = (TemplateEngine[]) templateList.toArray(new TemplateEngine[templateList.size()]);
		fRestrictToMatchingCase = false;
		fAllowAddIncludes = true;

		fComparator = new CCompletionProposalComparator();

		fElementLabelProvider = new CElementLabelProvider();
		//fImageRegistry= CUIPlugin.getDefault().getImageRegistry();
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
		if(fValidator == null) {
			fValidator = new CParameterListValidator();
		}
		return fValidator;
	}

	/**
	 * @see IContentAssistProcessor#getContextInformationAutoActivationCharacters()
	 */
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	/**
	 * @see IContentAssistProcessor#computeContextInformation(ITextViewer, int)
	 */
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
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
		fProposalAutoActivationSet = activationSet;
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
		fAllowAddIncludes = allowAddingIncludes;
	}

	/**
	 * @see IContentAssistProcessor#computeCompletionProposals(ITextViewer, int)
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		//IDocument unit= fManager.getWorkingCopy(fEditor.getEditorInput());
		IDocument document = viewer.getDocument();

		ICCompletionProposal[] results = null;

		try {
			if (document != null) {

				int offset = documentOffset;
				int length = 0;

				Point selection = viewer.getSelectedRange();
				if (selection.y > 0) {
					offset = selection.x;
					length = selection.y;
				}

				//CCompletionEvaluator evaluator= new CCompletionEvaluator(document, offset, length);
				//evaluator.restrictProposalsToMatchingCases(fRestrictToMatchingCase);
				results = evalProposals(document, offset, length);
			}
		} catch (Exception e) {
			CUIPlugin.getDefault().log(e);
		}

		if (results == null)
			results = new ICCompletionProposal[0];

		for (int i = 0; i < fTemplateEngine.length; i++) {
			if (fTemplateEngine[i] == null) {
				continue;
			}
			try {
				fTemplateEngine[i].reset();
				fTemplateEngine[i].complete(viewer, documentOffset, null);
			} catch (Exception x) {
				System.out.println("Template Exception");
				CUIPlugin.getDefault().log(x);
			}

			ICCompletionProposal[] templateResults = fTemplateEngine[i].getResults();
			if (results.length == 0) {
				results = templateResults;
			} else {
				// concatenate arrays
				ICCompletionProposal[] total = new ICCompletionProposal[results.length + templateResults.length];
				System.arraycopy(templateResults, 0, total, 0, templateResults.length);
				System.arraycopy(results, 0, total, templateResults.length, results.length);
				results = total;
			}
		}

		/*
		 * Order here and not in result collector to make sure that the order
		 * applies to all proposals and not just those of the compilation unit. 
		 */
		order(results);
		return results;
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
		boolean isDereference = false;
		IRegion region; 
		String frag = "";
		
		// Move back the pos by one the position is 0-based
		if (pos > 0) {
			pos--;
		}

		// TODO: Check to see if we are trying to open for a structure/class, then
		// provide that structure's completion instead of the function/variable
		// completions. This needs to be properly dealt with so that we can
		// offer completion proposals.
		if (pos > 1) {
			int struct_pos = pos;
			
			try {
				//While we aren't on a space, then go back and look for
				// . or a -> then determine the structure variable type.
				while(document.getChar(struct_pos) == ' ') {	
					struct_pos--;
				}
				
				if (document.getChar(struct_pos) == '.') {
					isDereference = true;
					pos -= struct_pos - 1;
				} else if ((document.getChar(struct_pos) == '>') && (document.getChar(struct_pos - 1) == '-')) {
					isDereference = true;
					pos -= struct_pos - 2;
				} else {
					isDereference = false;
				}
			} catch (BadLocationException ex) {
				return null;
			}
		}

		// Get the current "word", it might be a variable or another starter
		region = CWordFinder.findWord(document, pos);
		if(region == null) {
			return null;	//Bail out on error
		}
		
		//@@@ TODO: Implement the structure member completion
		if(isDereference) {
			return null;
		}
				
		try {
			frag = document.get(region.getOffset(), region.getLength());
			frag = frag.trim();
		} catch (BadLocationException ex) {
			return null;		//Bail out on error
		}
		
		//If there is no fragment, then see if we are in a function
		if(frag.length() == 0) { 
			IRegion funcregion;
			String  funcfrag = "";

			funcregion = CWordFinder.findFunction(document, pos + 1);
			if(funcregion != null) {			
				try {
					funcfrag = document.get(funcregion.getOffset(), funcregion.getLength());
					funcfrag = funcfrag.trim();
				} catch(Exception ex) {
					funcfrag = "";
				}
				if(funcfrag.length() == 0) {
					return null;			
				} else {
					//@@@ Add some marker here to indicate different path!
					region = funcregion;
					frag = funcfrag;
				}
			}
		}
		
		// Based on the frag name, build a list of completion proposals
		ArrayList completions = new ArrayList();

		// Look in index manager
		addProposalsFromModel(region, frag, completions);
		
		// Loot in the contributed completions
		addProposalsFromCompletionContributors(region, frag, completions);
		
		return (ICCompletionProposal[]) completions.toArray(new ICCompletionProposal[0]);
	}

	private void addProposalsFromCompletionContributors(IRegion region, String frag, ArrayList completions) {
		IFunctionSummary[] summary;

		summary = CCompletionContributorManager.getDefault().getMatchingFunctions(frag);
		if(summary == null) {
			return;
		}
		
		for (int i = 0; i < summary.length; i++) {
			String fname = summary[i].getName() + "()";
			String fdesc = summary[i].getDescription();
			IFunctionSummary.IFunctionPrototypeSummary fproto = summary[i].getPrototype();
			String fargs = fproto.getArguments();
			
			CCompletionProposal proposal;
			proposal = new CCompletionProposal(fname, 
											   region.getOffset(), 
											   region.getLength(),
											   getTagImage(TagFlags.T_FUNCTION), 
											   fproto.getPrototypeString(true),
											   2);

			if(fdesc != null) {
				proposal.setAdditionalProposalInfo(fdesc);
			}
			
			if(fargs != null && fargs.length() > 0) {
				proposal.setContextInformation(new ContextInformation(fname, fargs));
			}

			completions.add(proposal);
		}
	}
	
	private void addProposalsFromModel(IRegion region, String frag, ArrayList completions) {
		IProject project = null;
		IEditorInput input = fEditor.getEditorInput();
		if (input instanceof IFileEditorInput) {
			project = ((IFileEditorInput) input).getFile().getProject();
	
			// Bail out quickly, if the project was deleted.
			if (!project.exists()) {
				project = null;
			}
		}
		if (project != null) {
			addProjectCompletions(project, region, frag, completions);
			// Now query referenced projects
			IProject referenced[];
			try {
				referenced = project.getReferencedProjects();
				if (referenced.length > 0) {
					for (int i = 0; i < referenced.length; i++) {
						addProjectCompletions(referenced[i], region, frag, completions);
					}
				}
			} catch (CoreException e) {
			}
		}
	}

	private void addProjectCompletions(IProject project, IRegion region, String frag, ArrayList completions) {
		IndexModel model = IndexModel.getDefault();

		ITagEntry[] tags = model.query(project, frag + "*", false, false);
		if (tags != null && tags.length > 0) {
			for (int i = 0; i < tags.length; i++) {
				String fname = tags[i].getTagName();
				FunctionPrototypeSummary fproto = null;
				int kind = tags[i].getKind();

				if (kind == TagFlags.T_FUNCTION || kind == TagFlags.T_PROTOTYPE) {
					fname = fname + "()";
				}
				
				if(tags[i].getPattern() != null) {
					try {
						fproto = new FunctionPrototypeSummary(tags[i].getPattern());
					} catch(Exception ex) {
						fproto = null;
					}
				} 				
				if(fproto == null) {
					fproto = new FunctionPrototypeSummary(fname);
				}

				//System.out.println("tagmatch " + fname + " proto " + proto + " type" + tags[i].getKind());
				if (kind != TagFlags.T_MEMBER) {
					CCompletionProposal proposal;
					proposal = new CCompletionProposal(fname, 
													   region.getOffset(), 
													   region.getLength(),
													   getTagImage(kind), 
													   fproto.getPrototypeString(true),
													   3);
					completions.add(proposal);

					//No summary information available yet

					String fargs = fproto.getArguments();
					if(fargs != null && fargs.length() > 0) {
						proposal.setContextInformation(new ContextInformation(fname, fargs));
					}
				}
			}
		}
	}

	private Image getTagImage(int kind) {
		switch (kind) {
			case TagFlags.T_PROTOTYPE :
				return CPluginImages.get(CPluginImages.IMG_OBJS_DECLARATION);
			case TagFlags.T_CLASS :
				return CPluginImages.get(CPluginImages.IMG_OBJS_CLASS);
			case TagFlags.T_ENUM :
			case TagFlags.T_VARIABLE :
			case TagFlags.T_MEMBER :
				return CPluginImages.get(CPluginImages.IMG_OBJS_FIELD);
			case TagFlags.T_FUNCTION :
				return CPluginImages.get(CPluginImages.IMG_OBJS_FUNCTION);
			case TagFlags.T_STRUCT :
				return CPluginImages.get(CPluginImages.IMG_OBJS_STRUCT);
			case TagFlags.T_UNION :
				return CPluginImages.get(CPluginImages.IMG_OBJS_UNION);
			case TagFlags.T_MACRO :
				return CPluginImages.get(CPluginImages.IMG_OBJS_MACRO);
		}
		return CPluginImages.get(CPluginImages.IMG_OBJS_FUNCTION);
	}

}
