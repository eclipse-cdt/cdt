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
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMember;
import org.eclipse.cdt.core.model.IMethod;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.search.BasicSearchMatch;
import org.eclipse.cdt.core.search.BasicSearchResultCollector;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.core.model.CElement;
import org.eclipse.cdt.internal.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.internal.core.search.matching.OrPattern;
import org.eclipse.cdt.internal.core.sourcedependency.DependencyQueryJob;
import org.eclipse.cdt.internal.corext.template.ContextType;
import org.eclipse.cdt.internal.corext.template.ContextTypeRegistry;
import org.eclipse.cdt.internal.ui.CCompletionContributorManager;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.text.CParameterListValidator;
import org.eclipse.cdt.internal.ui.text.CWordFinder;
import org.eclipse.cdt.internal.ui.text.template.TemplateEngine;
import org.eclipse.cdt.ui.CSearchResultLabelProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.FunctionPrototypeSummary;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.cdt.ui.text.ICCompletionProposal;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Point;
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
		
		currentOffset = documentOffset;		
		currentSourceUnit = unit;
		
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

				results = evalProposals(document, offset, length, unit);
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

	private ICElement getCurrentScope(ITranslationUnit unit, int documentOffset){
		// quick parse the unit
		Map elements = unit.parse();
		// figure out what element is the enclosing the current offset
		ICElement currentScope = unit;
		Iterator i = elements.keySet().iterator();
		while (i.hasNext()){
			CElement element = (CElement) i.next();
			
			if ((element.getStartPos() < documentOffset ) 
				&& ( element.getStartPos() + element.getLength() > documentOffset)
				)
			{
				if(currentScope instanceof ITranslationUnit){
					currentScope = element;
				}else
				if (currentScope instanceof CElement){
					CElement currentScopeElement = (CElement) currentScope;
					if(
					 (currentScopeElement.getStartPos() < element.getStartPos())
					 && (
					 (currentScopeElement.getStartPos() + currentScopeElement.getLength() )
					  > (element.getStartPos() + element.getLength()) )
					)
					currentScope = element;  
				}
			}
		}
		return currentScope;
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
	public ICCompletionProposal[] evalProposals(IDocument document, int pos, int length, ITranslationUnit unit) {
		return order (evalProposals(document, pos, length, getCurrentScope (unit, pos)));
	}

	private ICCompletionProposal[] evalProposals(IDocument document, int startPos, int length, ICElement currentScope) {
		boolean isDereference = false;
		IRegion region; 
		String frag = "";
		int pos = startPos;

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
			//frag = document.get(region.getOffset(), region.getLength());
			frag = document.get(region.getOffset(), startPos - region.getOffset());
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
		addProposalsFromModel(region, frag,currentScope,  completions);
		
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
	

	private void addProposalsFromModel (IRegion region, String frag, ICElement currentScope, ArrayList completions) {
		List elementsFound = new LinkedList();
		String prefix = frag + "*";
		
		//  TODO: change that to resource scope later
		if (currentScope == null)
		   return;
		
		// clear the completion list at the result collector
		resultCollector.clearCompletions();
		
		//invoke the completion engine
		//IASTCompletionNode completionNode = completionEngine.complete(currentSourceUnit, currentOffset, completions);
		
		// figure out the search scope
		IPreferenceStore store = CUIPlugin.getDefault().getPreferenceStore();
		boolean fileScope = store.getBoolean(ContentAssistPreference.CURRENT_FILE_SEARCH_SCOPE);
		boolean projectScope = store.getBoolean(ContentAssistPreference.PROJECT_SEARCH_SCOPE);
		boolean projectScopeAndDependency = store.getBoolean(ContentAssistPreference.PROJECT_AND_DEPENDENCY_SEARCH_SCOPE);
		ICSearchScope scope = null;
	
		if ((projectScope) || (projectScopeAndDependency)){
			ICElement[] projectScopeElement = new ICElement[1];
			projectScopeElement[0] = (ICElement)currentScope.getCProject();
			scope = SearchEngine.createCSearchScope(projectScopeElement, projectScopeAndDependency);
		}
		else{
			//Try to get the file
			IResource actualFile = currentScope.getUnderlyingResource();
			IProject project = currentScope.getCProject().getProject();
			ArrayList dependencies = new ArrayList();
			if (actualFile != null){
				//Get file's dependencies
				try {
					IndexManager indexMan = CCorePlugin.getDefault().getCoreModel().getIndexManager();
					indexMan.performConcurrentJob(new DependencyQueryJob(project, (IFile)actualFile, indexMan, dependencies), ICSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null, null);
				} catch (Exception e) {
				}
			}
			//Create CFileSearchScope
			scope = SearchEngine.createCFileSearchScope((IFile) actualFile, dependencies);
		}
		
		OrPattern orPattern = new OrPattern();
		// search for global variables, functions, classes, structs, unions, enums and macros
		orPattern.addPattern(SearchEngine.createSearchPattern( prefix, ICSearchConstants.VAR, ICSearchConstants.DECLARATIONS, false ));
		orPattern.addPattern(SearchEngine.createSearchPattern( prefix, ICSearchConstants.FUNCTION, ICSearchConstants.DECLARATIONS, false ));
		orPattern.addPattern(SearchEngine.createSearchPattern( prefix, ICSearchConstants.FUNCTION, ICSearchConstants.DEFINITIONS, false ));
		orPattern.addPattern(SearchEngine.createSearchPattern( prefix, ICSearchConstants.TYPE, ICSearchConstants.DECLARATIONS, false ));
		orPattern.addPattern(SearchEngine.createSearchPattern( prefix, ICSearchConstants.ENUM, ICSearchConstants.DECLARATIONS, false ));
		orPattern.addPattern(SearchEngine.createSearchPattern( prefix, ICSearchConstants.MACRO, ICSearchConstants.DECLARATIONS, false ));
		searchEngine.search(CUIPlugin.getWorkspace(), orPattern, scope, searchResultCollector, true);
		elementsFound.addAll(searchResultCollector.getSearchResults());

		if((currentScope instanceof IMethod) || (currentScope instanceof IMethodDeclaration) ){
			// add the methods and fields of the parent class
			// Work around until CElement scope is implemented
			IStructure parentClass = (IStructure) currentScope.getParent();
			ArrayList children = new ArrayList();
			children.addAll(parentClass.getChildrenOfType(ICElement.C_METHOD));
			children.addAll(parentClass.getChildrenOfType(ICElement.C_METHOD_DECLARATION));
			children.addAll(parentClass.getChildrenOfType(ICElement.C_FIELD));
			Iterator c = children.iterator();
			while (c.hasNext()){
				IMember child = (IMember)c.next();
				if (child.getElementName().startsWith(frag))
				{
					BasicSearchMatch childMatch = new BasicSearchMatch();
					childMatch.setType(child.getElementType());
					childMatch.setParentName(parentClass.getElementName());
					if(child.getVisibility() == ASTAccessVisibility.PUBLIC )
						childMatch.setVisibility(ICElement.CPP_PUBLIC);
					else if(child.getVisibility() == ASTAccessVisibility.PROTECTED )
						childMatch.setVisibility(ICElement.CPP_PROTECTED);
					else if(child.getVisibility() == ASTAccessVisibility.PRIVATE )
						childMatch.setVisibility(ICElement.CPP_PRIVATE);
					childMatch.setConst(child.isConst());
					childMatch.setVolatile(child.isVolatile());
					childMatch.setStatic(child.isStatic());
					if(child instanceof IMethodDeclaration){
						childMatch.setName(((IMethodDeclaration)child).getSignature());
						childMatch.setReturnType( ((IMethodDeclaration)child).getReturnType() );
					}
					else {
						childMatch.setName(child.getElementName());
					}
				 
					elementsFound.add(childMatch);		
				}
			}
						
		}

		sendResultsToCollector(elementsFound.iterator(), region.getOffset(), region.getLength(), frag ); 
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
