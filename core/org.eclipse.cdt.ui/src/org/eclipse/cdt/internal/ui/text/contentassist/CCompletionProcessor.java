package org.eclipse.cdt.internal.ui.text.contentassist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.search.BasicSearchMatch;
import org.eclipse.cdt.core.search.BasicSearchResultCollector;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.OrPattern;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.corext.template.c.CContextType;
import org.eclipse.cdt.internal.ui.CHelpProviderManager;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.text.CParameterListValidator;
import org.eclipse.cdt.internal.ui.text.template.TemplateEngine;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;
import org.eclipse.cdt.ui.text.ICCompletionProposal;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationExtension;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;

/**
 * C completion processor.
 */
public class CCompletionProcessor implements IContentAssistProcessor {

	private static class ContextInformationWrapper implements IContextInformation, IContextInformationExtension {
		
		private final IContextInformation fContextInformation;
		private int fPosition;
		
		public ContextInformationWrapper(IContextInformation contextInformation) {
			fContextInformation= contextInformation;
		}
		
		/*
		 * @see IContextInformation#getContextDisplayString()
		 */
		public String getContextDisplayString() {
			return fContextInformation.getContextDisplayString();
		}

		/*
		 * @see IContextInformation#getImage()
		 */
		public Image getImage() {
			return fContextInformation.getImage();
		}

		/*
		 * @see IContextInformation#getInformationDisplayString()
		 */
		public String getInformationDisplayString() {
			return fContextInformation.getInformationDisplayString();
		}

		/*
		 * @see IContextInformationExtension#getContextInformationPosition()
		 */
		public int getContextInformationPosition() {
			return fPosition;
		}
		
		public void setContextInformationPosition(int position) {
			fPosition= position;	
		}
	}
	
	
	private CEditor fEditor;
	private char[] fProposalAutoActivationSet;
	private CCompletionProposalComparator fComparator;
	private IContextInformationValidator fValidator;

	private TemplateEngine fTemplateEngine;
	
	//private boolean fRestrictToMatchingCase;
	private boolean fAllowAddIncludes;

	private BasicSearchResultCollector  searchResultCollector = null;
	private ResultCollector resultCollector = null;
	private CompletionEngine completionEngine = null;
	
	private SearchEngine searchEngine = null;
	
	IWorkingCopy fCurrentSourceUnit = null;

	private int fCurrentOffset = 0;
	private IASTCompletionNode fCurrentCompletionNode = null;
	private int fNumberOfComputedResults= 0;
	private ITextViewer fTextViewer;
	
	public CCompletionProcessor(IEditorPart editor) {
		fEditor = (CEditor) editor;
	
		// Needed for search
		searchResultCollector = new BasicSearchResultCollector ();
		resultCollector = new ResultCollector();
		completionEngine = new CompletionEngine(resultCollector);
		searchEngine = new SearchEngine();
		searchEngine.setWaitingPolicy( ICSearchConstants.FORCE_IMMEDIATE_SEARCH );
		setupTemplateEngine();
		
		//fRestrictToMatchingCase = false;
		fAllowAddIncludes = true;

		fComparator = new CCompletionProposalComparator();
	}
	
	private void setupTemplateEngine() {
		TemplateContextType contextType = CUIPlugin.getDefault().getTemplateContextRegistry().getContextType(CContextType.CCONTEXT_TYPE);			
		if (contextType == null) {
			contextType= new CContextType();
			CUIPlugin.getDefault().getTemplateContextRegistry().addContextType(contextType);
		}
		if (contextType != null) {
			fTemplateEngine = new TemplateEngine(contextType);
		}

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
		if (fNumberOfComputedResults == 0) {
			String errorMsg= resultCollector.getErrorMessage();
			if (errorMsg == null || errorMsg.length() == 0)
				errorMsg= CUIMessages.getString("CEditor.contentassist.noCompletions"); //$NON-NLS-1$
			return errorMsg;
		}
				
		return resultCollector.getErrorMessage();
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
		List result= addContextInformations(viewer, offset);
		return (IContextInformation[]) result.toArray(new IContextInformation[result.size()]);
	}
	
	private List addContextInformations(ITextViewer viewer, int offset) {
		ICompletionProposal[] proposals= internalComputeCompletionProposals(viewer, offset);

		List result= new ArrayList();
		for (int i= 0; i < proposals.length; i++) {
			IContextInformation contextInformation= proposals[i].getContextInformation();
			if (contextInformation != null) {
				ContextInformationWrapper wrapper= new ContextInformationWrapper(contextInformation);
				wrapper.setContextInformationPosition(offset);
				result.add(wrapper);				
			}
		}
		return result;
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
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		return internalComputeCompletionProposals(viewer, offset);
	}
	
	private ICompletionProposal[] internalComputeCompletionProposals(ITextViewer viewer, int offset) {
		IWorkingCopyManager fManager = CUIPlugin.getDefault().getWorkingCopyManager();
		IWorkingCopy unit = fManager.getWorkingCopy(fEditor.getEditorInput());
		
		IDocument document = viewer.getDocument();
		
		// check for :: and ->
		int pos = offset -1;
		if(pos >= 0){
			try{
				if ((document.getChar(pos) == ':') && (document.getChar(pos -1) != ':')) {
					// ignore this request
					return null;
				} else if ((document.getChar(pos) == '>') && (document.getChar(pos - 1) != '-')) {
					// ignore this request
					return null;
				}
			} catch ( BadLocationException ex ){
				// ignore this request
				return null;
			}
		}

		ICCompletionProposal[] results = null;
		try {
			results = evalProposals(document, offset, unit, viewer);
		} catch (Exception e) {
			CUIPlugin.getDefault().log(e);
		}

		fNumberOfComputedResults= (results == null ? 0 : results.length);
		
		if (results == null)
			results = new ICCompletionProposal[0];

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
		if(proposals != null)
			Arrays.sort(proposals, fComparator);
		return proposals;
	}

	/**
	 * Evaluate the actual proposals for C
	 */
	public ICCompletionProposal[] evalProposals(IDocument document, int documentOffset, IWorkingCopy unit, ITextViewer viewer) {
		// setup the global variables
		fCurrentOffset = documentOffset;		
		fCurrentSourceUnit = unit;
		fTextViewer = viewer;
		
		ArrayList completions = new ArrayList();
		
		if (fCurrentSourceUnit == null)
			return null;
		
		// clear the completion list at the result collector
		resultCollector.reset(viewer);
		
		fCurrentCompletionNode = addProposalsFromModel(completions);
		if (fCurrentCompletionNode != null) {
			addProposalsFromSearch(fCurrentCompletionNode, completions);
			addProposalsFromCompletionContributors(fCurrentCompletionNode, completions);
			addProposalsFromTemplates(viewer, fCurrentCompletionNode, completions);		
			return order( (ICCompletionProposal[]) completions.toArray(new ICCompletionProposal[0]));
		}
		return null;			
	}
	
	private void addProposalsFromTemplates(ITextViewer viewer, IASTCompletionNode completionNode, List completions){
		if (completionNode == null)
			return;

		if (viewer == null)
			return;
		
		IASTCompletionNode.CompletionKind kind = completionNode.getCompletionKind();

		
//		if ((kind == IASTCompletionNode.CompletionKind.VARIABLE_TYPE) ||
//				(kind == IASTCompletionNode.CompletionKind.CLASS_REFERENCE)
//			|| (kind == IASTCompletionNode.CompletionKind.SINGLE_NAME_REFERENCE)) {
			addProposalsFromTemplateEngine(viewer, fTemplateEngine, completions);
//		}
	}

	private void addProposalsFromTemplateEngine(ITextViewer viewer, TemplateEngine fTemplateEngine, List completions){
		if (fTemplateEngine != null) {
			try {
				fTemplateEngine.reset();
				fTemplateEngine.complete(viewer, fCurrentOffset, null);
			} catch (Exception x) {
				CUIPlugin.getDefault().log(x);
			}
			completions.addAll(fTemplateEngine.getResults());
		}				
	}

	private void addProposalsFromCompletionContributors(IASTCompletionNode completionNode, List completions) {
		if(completionNode == null)
			return;
		String prefix = completionNode.getCompletionPrefix();
		int offset = fCurrentOffset - prefix.length();
		int length = prefix.length();
		
		// calling functions should happen only within the context of a code body
		if (completionNode.getCompletionContext() != null)
			return;
		
		IFunctionSummary[] summary;

		ICHelpInvocationContext context = new ICHelpInvocationContext() {

			public IProject getProject() {
				return fCurrentSourceUnit.getCProject().getProject();
			}

			public ITranslationUnit getTranslationUnit() {
				return fCurrentSourceUnit;
			}	
		};
		summary = CHelpProviderManager.getDefault().getMatchingFunctions(context, prefix);
		if(summary == null) {
			return;
		}
		
		for (int i = 0; i < summary.length; i++) {
			String fname = summary[i].getName() + "()"; //$NON-NLS-1$
			String fdesc = summary[i].getDescription();
			IFunctionSummary.IFunctionPrototypeSummary fproto = summary[i].getPrototype();
			String fargs = fproto.getArguments();
			
			CCompletionProposal proposal;
			proposal = new CCompletionProposal(fname, 
											   offset, 
											   length,
											   CPluginImages.get(CPluginImages.IMG_OBJS_FUNCTION), 
											   fproto.getPrototypeString(true),
											   2,
											   fTextViewer);

			if(fdesc != null) {
				proposal.setAdditionalProposalInfo(fdesc);
			}
			
			if(fargs != null && fargs.length() > 0) {
				proposal.setContextInformation(new ContextInformation(fname, fargs));
				// set the cursor before the closing bracket
				proposal.setCursorPosition(fname.length() - 1);
			}

			completions.add(proposal);
		}
	}
	
//	private FunctionPrototypeSummary getPrototype (BasicSearchMatch match) {
//		switch(match.getElementType()){
//			case ICElement.C_FUNCTION:
//			case ICElement.C_FUNCTION_DECLARATION:
//			case ICElement.C_METHOD:
//			case ICElement.C_METHOD_DECLARATION:
//			{
//				return (new FunctionPrototypeSummary ( match.getReturnType() + " " + match.getName() )); //$NON-NLS-1$
//			}
//		default:
//			return null;						
//		}
//	}
	

	private IASTCompletionNode addProposalsFromModel(List completions){
		//invoke the completion engine
		IASTCompletionNode completionNode = completionEngine.complete(fCurrentSourceUnit, fCurrentOffset);
		return completionNode;
	}
	
	private void addProposalsFromSearch (IASTCompletionNode completionNode, List completions) {
		if(completionNode == null)
			return;
		String prefix = completionNode.getCompletionPrefix();
		int offset = fCurrentOffset - prefix.length();
		int length = prefix.length();
		
		String searchPrefix = prefix + "*"; //$NON-NLS-1$
		
		// figure out the search scope
		IPreferenceStore store = CUIPlugin.getDefault().getPreferenceStore();
		//boolean fileScope = store.getBoolean(ContentAssistPreference.CURRENT_FILE_SEARCH_SCOPE);
		boolean projectScope = store.getBoolean(ContentAssistPreference.PROJECT_SEARCH_SCOPE);
		ICSearchScope scope = null;
	
		if ( (projectScope)
				&& (   (completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.SINGLE_NAME_REFERENCE)
					|| (completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.SINGLE_NAME_REFERENCE)
					|| (completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.VARIABLE_TYPE)
					|| (completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.FIELD_TYPE) )
				&& (prefix.length() > 0)){
			List elementsFound = new LinkedList();
			
			ICElement[] projectScopeElement = new ICElement[1];
			projectScopeElement[0] = fCurrentSourceUnit.getCProject();
			scope = SearchEngine.createCSearchScope(projectScopeElement, true);
			
			// search for global variables, functions, classes, structs, unions, enums, macros, and namespaces
			OrPattern orPattern = new OrPattern();
			orPattern.addPattern(SearchEngine.createSearchPattern( 
					searchPrefix, ICSearchConstants.TYPE, ICSearchConstants.DECLARATIONS, false ));
			orPattern.addPattern(SearchEngine.createSearchPattern( 
					searchPrefix, ICSearchConstants.ENUM, ICSearchConstants.DECLARATIONS, false ));
			orPattern.addPattern(SearchEngine.createSearchPattern( 
					searchPrefix, ICSearchConstants.MACRO, ICSearchConstants.DECLARATIONS, false ));
			orPattern.addPattern(SearchEngine.createSearchPattern( 
					searchPrefix, ICSearchConstants.NAMESPACE, ICSearchConstants.DEFINITIONS, false ));
			
			if( (completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.SINGLE_NAME_REFERENCE)
			|| (completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.SINGLE_NAME_REFERENCE)){
				orPattern.addPattern(SearchEngine.createSearchPattern( 
						searchPrefix, ICSearchConstants.VAR, ICSearchConstants.DECLARATIONS, false ));
				orPattern.addPattern(SearchEngine.createSearchPattern( 
						searchPrefix, ICSearchConstants.FUNCTION, ICSearchConstants.DEFINITIONS, false ));
				orPattern.addPattern(SearchEngine.createSearchPattern( 
						searchPrefix, ICSearchConstants.FUNCTION, ICSearchConstants.DECLARATIONS, false ));
			}
			try {
				searchEngine.search(CUIPlugin.getWorkspace(), orPattern, scope, searchResultCollector, true);
			} catch (InterruptedException e) {
			}
			elementsFound.addAll(searchResultCollector.getSearchResults());
			
			sendResultsToCollector(elementsFound.iterator(), offset, length, prefix ); 
		}
		
		completions.addAll(resultCollector.getCompletions());		
		
	}
	
	private void sendResultsToCollector(Iterator results , int completionStart, int completionLength, String prefix){
		while (results.hasNext()){
			ASTAccessVisibility visibility;
			
			BasicSearchMatch match = (BasicSearchMatch)results.next();
			int type = match.getElementType();
			int relevance = completionEngine.computeRelevance(type, prefix, match.getName());
			switch (type){
				case ICElement.C_FIELD:
					switch (match.getVisibility()){
						case ICElement.CPP_PUBLIC:
							visibility = ASTAccessVisibility.PUBLIC;
							break;
						case ICElement.CPP_PROTECTED:
							visibility = ASTAccessVisibility.PROTECTED;
							break;
						default:
							visibility = ASTAccessVisibility.PRIVATE;
							break;
					}
					resultCollector.acceptField(
							match.getName(), 
							match.getReturnType(), 
							visibility, 
							completionStart, 
							completionLength, 
							relevance);
					break;
					
				case ICElement.C_VARIABLE:
				case ICElement.C_VARIABLE_DECLARATION:
					resultCollector.acceptVariable(
							match.getName(), 
							match.getReturnType(), 
							completionStart, 
							completionLength, 
							relevance);
					break;
				case ICElement.C_METHOD:
				case ICElement.C_METHOD_DECLARATION:
					switch (match.getVisibility()){
						case ICElement.CPP_PUBLIC:
							visibility = ASTAccessVisibility.PUBLIC;
							break;
						case ICElement.CPP_PROTECTED:
							visibility = ASTAccessVisibility.PROTECTED;
							break;
						default:
							visibility = ASTAccessVisibility.PRIVATE;
							break;
					}
					resultCollector.acceptMethod(
							match.getName(), 
							null,
							match.getReturnType(), 
							visibility, 
							completionStart, 
							completionLength, 
							relevance, true, completionStart);
					break;				
				case ICElement.C_FUNCTION:
				case ICElement.C_FUNCTION_DECLARATION:
					resultCollector.acceptFunction(
							match.getName(), 
							null,
							match.getReturnType(), 
							completionStart, 
							completionLength, 
							relevance, true, completionStart);
					break;
				case ICElement.C_CLASS:
					resultCollector.acceptClass(
							match.getName(), 
							completionStart, 
							completionLength, 
							relevance);
					break;
				case ICElement.C_STRUCT:
					resultCollector.acceptStruct(
							match.getName(), 
							completionStart, 
							completionLength, 
							relevance);
					break;
				case ICElement.C_UNION:
					resultCollector.acceptUnion(
							match.getName(), 
							completionStart, 
							completionLength, 
							relevance);
					break;
				case ICElement.C_NAMESPACE:
					resultCollector.acceptNamespace(
							match.getName(), 
							completionStart, 
							completionLength, 
							relevance);				
					break;
				case ICElement.C_MACRO:
					resultCollector.acceptMacro(
							match.getName(), 
							completionStart, 
							completionLength, 
							relevance, completionStart);
					break;
				case ICElement.C_ENUMERATION:
					resultCollector.acceptEnumeration(
							match.getName(), 
							completionStart, 
							completionLength, 
							relevance);				
					break;
				case ICElement.C_ENUMERATOR:
					resultCollector.acceptEnumerator(
							match.getName(), 
							completionStart, 
							completionLength, 
							relevance);
					break;
				default :
					break;				
			} // end switch
		} // end while		
	}
	/**
	 * @return Returns the fCurrentCompletionNode.
	 * This method is added for JUnit tests.
	 */
	public IASTCompletionNode getCurrentCompletionNode() {
		return fCurrentCompletionNode;
	}

}
