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

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.search.BasicSearchMatch;
import org.eclipse.cdt.core.search.BasicSearchResultCollector;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.search.matching.OrPattern;
import org.eclipse.cdt.internal.corext.template.ContextType;
import org.eclipse.cdt.internal.corext.template.ContextTypeRegistry;
import org.eclipse.cdt.internal.ui.CCompletionContributorManager;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.text.CParameterListValidator;
import org.eclipse.cdt.internal.ui.text.template.TemplateEngine;
import org.eclipse.cdt.ui.CSearchResultLabelProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.FunctionPrototypeSummary;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.cdt.ui.text.ICCompletionProposal;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.ui.IEditorPart;

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

	BasicSearchResultCollector  searchResultCollector = null;
	ResultCollector resultCollector = null;
	CompletionEngine completionEngine = null;
	
	SearchEngine searchEngine = null;
	CSearchResultLabelProvider labelProvider = null;
	
	int currentOffset = 0;
	IWorkingCopy currentSourceUnit = null;
	
	public CCompletionProcessor(IEditorPart editor) {
		fEditor = (CEditor) editor;
	
		// Needed for search
		labelProvider = new CSearchResultLabelProvider();
		searchResultCollector = new BasicSearchResultCollector ();
		resultCollector = new ResultCollector();
		completionEngine = new CompletionEngine(resultCollector);
		searchEngine = new SearchEngine();
		
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
				
		IWorkingCopyManager fManager = CUIPlugin.getDefault().getWorkingCopyManager();
		IWorkingCopy unit = fManager.getWorkingCopy(fEditor.getEditorInput());
							
		IDocument document = viewer.getDocument();
				
		ICCompletionProposal[] results = null;

		try {
//			if (document != null) {
//
//				int offset = documentOffset;
//				int length = 0;
//
//				Point selection = viewer.getSelectedRange();
//				if (selection.y > 0) {
//					offset = selection.x;
//					length = selection.y;
//				}

				results = evalProposals(document, documentOffset, unit);
//			}
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
		if(proposals != null)
			Arrays.sort(proposals, fComparator);
		return proposals;
	}

	/**
	 * Evaluate the actual proposals for C
	 */
		// TODO: Check to see if we are trying to open for a structure/class, then
		//@@@ TODO: Implement the structure member completion
	public ICCompletionProposal[] evalProposals(IDocument document, int documentOffset, IWorkingCopy unit) {
		
		currentOffset = documentOffset;		
		currentSourceUnit = unit;
		ArrayList completions = new ArrayList();
		
		addProposalsFromModel(completions);
		
		return order ( (ICCompletionProposal[]) completions.toArray(new ICCompletionProposal[0]) );		
	}

	private void addProposalsFromCompletionContributors(String prefix, int offset, int length, List completions) {
		IFunctionSummary[] summary;

		summary = CCompletionContributorManager.getDefault().getMatchingFunctions(prefix);
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
											   offset, 
											   length,
											   CPluginImages.get(CPluginImages.IMG_OBJS_FUNCTION), 
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
	
	private FunctionPrototypeSummary getPrototype (BasicSearchMatch match) {
		switch(match.getElementType()){
			case ICElement.C_FUNCTION:
			case ICElement.C_FUNCTION_DECLARATION:
			case ICElement.C_METHOD:
			case ICElement.C_METHOD_DECLARATION:
			{
				return (new FunctionPrototypeSummary ( match.getReturnType() + " " + match.getName() ));
			}
		default:
			return null;						
		}
	}
	

	private void addProposalsFromModel (List completions) {
		
		if (currentSourceUnit == null)
		   return;
		
		// clear the completion list at the result collector
		resultCollector.clearCompletions();
		
		//invoke the completion engine
		IASTCompletionNode completionNode = completionEngine.complete(currentSourceUnit, currentOffset);
		if(completionNode == null)
			return;
		String prefix = completionNode.getCompletionPrefix();
		int offset = currentOffset - prefix.length();
		int length = prefix.length();
		
		String searchPrefix = prefix + "*";
		
		// figure out the search scope
		IPreferenceStore store = CUIPlugin.getDefault().getPreferenceStore();
		boolean fileScope = store.getBoolean(ContentAssistPreference.CURRENT_FILE_SEARCH_SCOPE);
		boolean projectScope = store.getBoolean(ContentAssistPreference.PROJECT_SEARCH_SCOPE);
		boolean projectScopeAndDependency = store.getBoolean(ContentAssistPreference.PROJECT_AND_DEPENDENCY_SEARCH_SCOPE);
		ICSearchScope scope = null;
	
		if ((projectScope) || (projectScopeAndDependency)){
			List elementsFound = new LinkedList();
			
			ICElement[] projectScopeElement = new ICElement[1];
			projectScopeElement[0] = (ICElement)currentSourceUnit.getCProject();
			scope = SearchEngine.createCSearchScope(projectScopeElement, projectScopeAndDependency);

			OrPattern orPattern = new OrPattern();
			// search for global variables, functions, classes, structs, unions, enums, macros, and namespaces
			orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix, ICSearchConstants.VAR, ICSearchConstants.DECLARATIONS, false ));
			orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix, ICSearchConstants.FUNCTION, ICSearchConstants.DEFINITIONS, false ));
			orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix, ICSearchConstants.FUNCTION, ICSearchConstants.DECLARATIONS, false ));
			orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix, ICSearchConstants.TYPE, ICSearchConstants.DECLARATIONS, false ));
			orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix, ICSearchConstants.ENUM, ICSearchConstants.DECLARATIONS, false ));
			orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix, ICSearchConstants.MACRO, ICSearchConstants.DECLARATIONS, false ));
			orPattern.addPattern(SearchEngine.createSearchPattern( searchPrefix, ICSearchConstants.NAMESPACE, ICSearchConstants.DEFINITIONS, false ));
			searchEngine.search(CUIPlugin.getWorkspace(), orPattern, scope, searchResultCollector, true);
			elementsFound.addAll(searchResultCollector.getSearchResults());
			
			sendResultsToCollector(elementsFound.iterator(), offset, length, prefix ); 
		}
		
		completions.addAll(resultCollector.getCompletions());		
		
		// Loot in the contributed completions
		addProposalsFromCompletionContributors(prefix, offset, length, completions);
		
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
					};
					resultCollector.acceptField(
							match.getName(), 
							null, 
							visibility, 
							completionStart, 
							completionLength, 
							relevance);
					break;
					
				case ICElement.C_VARIABLE:
				case ICElement.C_VARIABLE_DECLARATION:
					resultCollector.acceptVariable(
							match.getName(), 
							null, 
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
					};
					resultCollector.acceptMethod(
							match.getName(), 
							null,
							match.getReturnType(), 
							visibility, 
							completionStart, 
							completionLength, 
							relevance);
					break;				
				case ICElement.C_FUNCTION:
				case ICElement.C_FUNCTION_DECLARATION:
					resultCollector.acceptFunction(
							match.getName(), 
							null,
							match.getReturnType(), 
							completionStart, 
							completionLength, 
							relevance);
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
							relevance);
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
}
