/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import java.io.CharArrayReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICompletionRequestor;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserFactoryException;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ParserNotImplementedException;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTMacro;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.IASTNode.LookupResult;
import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.cdt.internal.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.parser.util.ASTUtil;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author hamer
 *
 * This class is the entry point for code completions.
 * It contains a public API used to call ContentAssist on a given working copy 
 * and a given completion offset.
 * 
 */
public class CompletionEngine implements RelevanceConstants{
	List completions = new ArrayList();
	ICompletionRequestor requestor;
	int completionStart = 0;
	int completionLength = 0;
	IPreferenceStore store = CUIPlugin.getDefault().getPreferenceStore();
	// keywords types
	public static final int BASIC_TYPES_KEYWORDS = 0;
	private static final String basicTypesKeywords[] = {
												"namespace",
												"class",
												"struct",
												"union",
												"enum",
												"bool",
												"char",
												"wchar_t",
												"int",
												"float",
												"double",
												"void",
												"template",
											 }; 
	private static final String exceptionKeyword = "...";
	
	public CompletionEngine(ICompletionRequestor completionRequestor){
			completions.clear();
			requestor = completionRequestor;
	}
	
	private int computeCaseMatchingRelevance(String prefix, String proposalName){
		if (CharOperation.prefixEquals(prefix.toCharArray(), proposalName.toCharArray(), true /* do not ignore case */)) {
			if(CharOperation.equals(prefix.toCharArray(), proposalName.toCharArray(), true /* do not ignore case */)) {
				return CASE_MATCH_RELEVANCE + EXACT_NAME_MATCH_RELEVANCE;
			} else {
				return CASE_MATCH_RELEVANCE;
			}
		} 
		else {
				return 0;
		}	
	}
	private int computeTypeRelevance(int type){
		switch (type){
			case ICElement.C_FIELD:
				return FIELD_TYPE_RELEVANCE;
			case ICElement.C_VARIABLE:
			case ICElement.C_VARIABLE_DECLARATION:
				return VARIABLE_TYPE_RELEVANCE;
			case ICElement.C_METHOD:
			case ICElement.C_METHOD_DECLARATION:
				return METHOD_TYPE_RELEVANCE;
			case ICElement.C_FUNCTION:
			case ICElement.C_FUNCTION_DECLARATION:
				return FUNCTION_TYPE_RELEVANCE;
			case ICElement.C_CLASS:
				return CLASS_TYPE_RELEVANCE;
			case ICElement.C_STRUCT:
				return STRUCT_TYPE_RELEVANCE;
			case ICElement.C_UNION:
				return UNION_TYPE_RELEVANCE;
			case ICElement.C_NAMESPACE:
				return NAMESPACE_TYPE_RELEVANCE;
			case ICElement.C_MACRO:
				return MACRO_TYPE_RELEVANCE;			
			case ICElement.C_ENUMERATION:
				return ENUMERATION_TYPE_RELEVANCE;
			case ICElement.C_ENUMERATOR:
				return ENUMERATOR_TYPE_RELEVANCE;
			default :
				return DEFAULT_TYPE_RELEVANCE;
		}		
	}
	public int computeRelevance(int elementType, String prefix, String proposalName){
		// compute the relevance according to the elemnent type
		int relevance = computeTypeRelevance(elementType);
		// compute the relevance according to the case sensitivity
		relevance += computeCaseMatchingRelevance(prefix, proposalName);
		return relevance;
	}
	private IASTCompletionNode parse(IWorkingCopy sourceUnit, int completionOffset){
		ContentAssistElementRequestor requestor = new ContentAssistElementRequestor();
		// Get resource info
		IResource currentResource = sourceUnit.getResource();
		IPath realPath = currentResource.getLocation(); 
		IProject project = currentResource.getProject();
		Reader reader = new CharArrayReader( sourceUnit.getContents() );		
		
		//Get the scanner info
		IScannerInfo scanInfo = new ScannerInfo();
		IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(project);
		if (provider != null){
			IScannerInfo buildScanInfo = provider.getScannerInformation(project);
			if( buildScanInfo != null )
				scanInfo = new ScannerInfo(buildScanInfo.getDefinedSymbols(), buildScanInfo.getIncludePaths());
		}
	
		//C or CPP?
		ParserLanguage language = CoreModel.getDefault().hasCCNature(project) ? ParserLanguage.CPP : ParserLanguage.C;
	
		IParser parser = null;
		try
		{
			IScanner scanner = ParserFactory.createScanner( reader, realPath.toOSString(), scanInfo, ParserMode.COMPLETE_PARSE, language, requestor, ParserUtil.getParserLogService() );
			parser  = ParserFactory.createParser( scanner, requestor, ParserMode.COMPLETE_PARSE, language, ParserUtil.getParserLogService() );
		}
		catch( ParserFactoryException pfe )
		{
					
		}
		if(parser != null){
			IASTCompletionNode result = null;
			try {
				result = parser.parse(completionOffset);
			} catch (ParserNotImplementedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return result;
		} else {
			return null;
		}	 	
	}
	
	private void addNodeToCompletions(IASTNode node, String prefix){
		if(node instanceof IASTField){
			IASTField field = (IASTField)node;
			int relevance = computeRelevance(ICElement.C_FIELD, prefix, field.getName());
			requestor.acceptField(field.getName(), 
					ASTUtil.getType(field.getAbstractDeclaration()),
					field.getVisiblity(), completionStart, completionLength, relevance);
		}
		else if(node instanceof IASTVariable){
			IASTVariable variable = (IASTVariable)node;
			int relevance = computeRelevance(ICElement.C_VARIABLE, prefix, variable.getName());
			requestor.acceptVariable(variable.getName(), 
				ASTUtil.getType(variable.getAbstractDeclaration()),
				completionStart, completionLength, relevance);
		}
		else if(node instanceof IASTMethod) {
			IASTMethod method = (IASTMethod)node;
			int relevance = computeRelevance(ICElement.C_METHOD, prefix, method.getName());
			String parameterString = ASTUtil.getParametersString(ASTUtil.getFunctionParameterTypes(method));
			requestor.acceptMethod(method.getName(), 
				ASTUtil.getType(method.getReturnType()), parameterString,
				method.getVisiblity(), completionStart, completionLength, relevance);
		}
		else if(node instanceof IASTFunction){
			IASTFunction function = (IASTFunction)node;
			int relevance = computeRelevance(ICElement.C_FUNCTION, prefix, function.getName());
			String parameterString = ASTUtil.getParametersString(ASTUtil.getFunctionParameterTypes(function));
			requestor.acceptFunction(function.getName(), 
				ASTUtil.getType(function.getReturnType()), parameterString,
				completionStart, completionLength, relevance);
		}
		else if(node instanceof IASTClassSpecifier){
			IASTClassSpecifier classSpecifier = (IASTClassSpecifier)node;
			ASTClassKind classkind = classSpecifier.getClassKind();
			if(classkind == ASTClassKind.CLASS){
				int relevance = computeRelevance(ICElement.C_CLASS, prefix, classSpecifier.getName());
				requestor.acceptClass(classSpecifier.getName(), 
					completionStart, completionLength, relevance);
			}
			if(classkind == ASTClassKind.STRUCT){
				int relevance = computeRelevance(ICElement.C_STRUCT, prefix, classSpecifier.getName());
				requestor.acceptStruct(classSpecifier.getName(), 
					completionStart, completionLength, relevance);
			}
			if(classkind == ASTClassKind.UNION){
				int relevance = computeRelevance(ICElement.C_UNION, prefix, classSpecifier.getName());
				requestor.acceptUnion(classSpecifier.getName(), 
					completionStart, completionLength, relevance);
			}				
		}
		else if(node instanceof IASTMacro){
			IASTMacro macro = (IASTMacro)node;
			int relevance = computeRelevance(ICElement.C_MACRO, prefix, macro.getName());
			requestor.acceptMacro(macro.getName(), completionStart, completionLength, relevance);
		}
		else if(node instanceof IASTNamespaceDefinition){
			IASTNamespaceDefinition namespace = (IASTNamespaceDefinition)node;
			int relevance = computeRelevance(ICElement.C_NAMESPACE, prefix, namespace.getName());
			requestor.acceptNamespace(namespace.getName(), completionStart, completionLength, relevance);
		}
		else if(node instanceof IASTEnumerationSpecifier){
			IASTEnumerationSpecifier enumeration = (IASTEnumerationSpecifier)node;
			int relevance = computeRelevance(ICElement.C_ENUMERATION, prefix, enumeration.getName());
			requestor.acceptEnumeration(enumeration.getName(), completionStart, completionLength, relevance);
		}
		else if(node instanceof IASTEnumerator){
			IASTEnumerator enumerator = (IASTEnumerator)node;
			int relevance = computeRelevance(ICElement.C_ENUMERATOR, prefix, enumerator.getName());
			requestor.acceptEnumerator(enumerator.getName(), completionStart, completionLength, relevance);
		}
	}
	
	private void addKeywordToCompletions (String keyword){
		int relevance = KEYWORD_TYPE_RELEVANCE;
		requestor.acceptKeyword(keyword, completionStart, completionLength, relevance);		
	}
	
	private void addKeywordsToCompletions(Iterator keywords){
		while (keywords.hasNext()){
			String keyword = (String) keywords.next();
			addKeywordToCompletions(keyword);
		}
	}
	
	private void addToCompletions (LookupResult result){
		Iterator nodes = result.getNodes();
		while (nodes.hasNext()){
			IASTNode node = (IASTNode) nodes.next();
			addNodeToCompletions(node, result.getPrefix());	
		}
		Iterator keywords = result.getKeywords();
		addKeywordsToCompletions(keywords);
		return ;
	}

	private List lookupKeyword(String prefix, int lookupType){
		List result = new ArrayList();
		switch (lookupType){
			case BASIC_TYPES_KEYWORDS:
				for(int i = 0; i <basicTypesKeywords.length; i++){
					String kw =basicTypesKeywords[i]; 
					if(kw.startsWith(prefix))
						result.add(kw);
				}
			break;
		}
		return result;
	}
	private void completionOnMemberReference(IASTCompletionNode completionNode){
		// Completing after a dot
		// 1. Get the search scope node
		IASTNode searchNode = completionNode.getCompletionScope();
		// 2. lookup fields & add to completion proposals
		LookupResult result = searchNode.lookup (completionNode.getCompletionPrefix(), IASTNode.LookupKind.FIELDS);
		addToCompletions (result);
		// 3. looup methods & add to completion proposals
		result = searchNode.lookup (completionNode.getCompletionPrefix(), IASTNode.LookupKind.METHODS);
		addToCompletions (result);
		// 4. lookup nested structures & add to completion proposals
		result = searchNode.lookup (completionNode.getCompletionPrefix(), IASTNode.LookupKind.STRUCTURES);
		addToCompletions (result);				
	}
	private void completionOnTypeReference(IASTCompletionNode completionNode){
		// completing on a type
		// 1. Get the search scope node
		IASTNode searchNode = completionNode.getCompletionScope();
		// if the prefix is not empty
		if(completionNode.getCompletionPrefix().length() > 0 ) {
			// 2. Lookup all types that could be used here
			LookupResult result = searchNode.lookup(completionNode.getCompletionPrefix(), IASTNode.LookupKind.STRUCTURES);
			addToCompletions(result);
			// 3. Lookup keywords
			// basic types should be in the keyword list 
			List keywords = lookupKeyword(completionNode.getCompletionPrefix(), BASIC_TYPES_KEYWORDS);
			addKeywordsToCompletions(keywords.iterator());
		} else // prefix is empty, we can not look for everything 
		{
			
		}
	}
	
	private void completionOnFieldType(IASTCompletionNode completionNode){
		// 1. basic completion on all types
		completionOnTypeReference(completionNode);
		// 2. Get the search scope node
		IASTNode searchNode = completionNode.getCompletionScope();
		// 3. lookup methods
		// we are at a field declaration place, the user could be trying to override a function.
		// We have to lookup functions that could be overridden here.
		LookupResult result = searchNode.lookup(completionNode.getCompletionPrefix(), IASTNode.LookupKind.METHODS);
		addToCompletions(result);
	}
	private void completionOnVariableType(IASTCompletionNode completionNode){
		// 1. basic completion on all types
		completionOnTypeReference(completionNode);
	}
	private void completionOnSingleNameReference(IASTCompletionNode completionNode){
		// 1. Get the search scope node
		// the search node is the code scope inwhich completion is requested
		IASTNode searchNode = completionNode.getCompletionScope();
		// if prefix is not empty
		if (completionNode.getCompletionPrefix().length() > 0){
			// here we have to look for anything that could be referenced within this scope
			// 1. lookup local variables, global variables, functions, methods, structures, enums, macros, and namespaces
			LookupResult result = searchNode.lookup(completionNode.getCompletionPrefix(), IASTNode.LookupKind.ALL);
			addToCompletions(result);
		} else // prefix is empty
		{
			// 1. look only for local variables 
			LookupResult result = searchNode.lookup(completionNode.getCompletionPrefix(), IASTNode.LookupKind.LOCAL_VARIABLES);
			addToCompletions(result);
			// 2. and what can be accessed through the "this" pointer
			// TODO : complete the lookup call
		}
	}
	private void completionOnScopedReference(IASTCompletionNode completionNode){
		// 1. Get the search scope node
		// the search node is the name before the qualification 
		IASTNode searchNode = completionNode.getCompletionScope();
		// here we have to look for anything that could be referenced within this scope
		// 1. lookup local variables, global variables, functions, methods, structures, enums, macros, and namespaces
		LookupResult result = searchNode.lookup(completionNode.getCompletionPrefix(), IASTNode.LookupKind.ALL);
		addToCompletions(result);
	}

	private void completionOnClassReference(IASTCompletionNode completionNode){
		// 1. Get the search scope node 
		IASTNode searchNode = completionNode.getCompletionScope();
		// only look for classes
		LookupResult result = searchNode.lookup(completionNode.getCompletionPrefix(), IASTNode.LookupKind.CLASSES);
		addToCompletions(result);
	}
	private void completionOnNamespaceReference(IASTCompletionNode completionNode){
		// 1. Get the search scope node 
		IASTNode searchNode = completionNode.getCompletionScope();
		// only look for classes
		LookupResult result = searchNode.lookup(completionNode.getCompletionPrefix(), IASTNode.LookupKind.NAMESPACES);
		addToCompletions(result);
	}
	private void completionOnExceptionReference(IASTCompletionNode completionNode){
		// here we have to look for all types
		completionOnTypeReference(completionNode);
		// plus if the prefix is empty, add "..." to the proposals
		if(completionNode.getCompletionPrefix().length() == 0){
			addKeywordToCompletions(exceptionKeyword);
		}
	}
	private void completionOnMacroReference(IASTCompletionNode completionNode){
		// 1. Get the search scope node 
		IASTNode searchNode = completionNode.getCompletionScope();
		// only look for macros
		LookupResult result = searchNode.lookup(completionNode.getCompletionPrefix(), IASTNode.LookupKind.MACROS);
		addToCompletions(result);
	}
	private void completionOnFunctionReference(IASTCompletionNode completionNode){
		// TODO: complete the lookups
	}
	private void completionOnConstructorReference(IASTCompletionNode completionNode){
		// 1. Get the search scope node 
		IASTNode searchNode = completionNode.getCompletionScope();
		// only lookup constructors
		LookupResult result = searchNode.lookup(completionNode.getCompletionPrefix(), IASTNode.LookupKind.CONSTRUCTORS);
	}
	private void completionOnKeyword(IASTCompletionNode completionNode){
		// lookup every type of keywords
		// 1. basic types keword list
		List result = lookupKeyword(completionNode.getCompletionPrefix(), BASIC_TYPES_KEYWORDS);
		addKeywordsToCompletions(result.iterator());
	}
	
	public IASTCompletionNode complete(IWorkingCopy sourceUnit, int completionOffset, List completionList) {
		
		// 1- Parse the translation unit
		IASTCompletionNode completionNode = parse(sourceUnit, completionOffset);
		
		if (completionNode == null)
			return null;
		
		// set the completionStart and the completionLength
		completionStart = completionOffset;
		completionLength = completionNode.getCompletionPrefix().length();
		
		// 2- Check the return value 
		if(completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.MEMBER_REFERENCE){
			// completionOnMemberReference
			completionOnMemberReference(completionNode);
		}
		else if(completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.SCOPED_REFERENCE){
			// completionOnMemberReference
			completionOnMemberReference(completionNode);
		}
		else if(completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.FIELD_TYPE){
			// CompletionOnFieldType
			completionOnFieldType(completionNode);
		}
		else if(completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.VARIABLE_TYPE){
			// CompletionOnVariableType
			completionOnTypeReference(completionNode);
		}
		else if(completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.ARGUMENT_TYPE){
			// CompletionOnArgumentType
			completionOnVariableType(completionNode);
		}
		else if(completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.SINGLE_NAME_REFERENCE){
			// CompletionOnSingleNameReference
			completionOnSingleNameReference(completionNode);
		}
		else if(completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.TYPE_REFERENCE){
			// CompletionOnStructureReference
			completionOnTypeReference(completionNode);
		}
		else if(completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.CLASS_REFERENCE){
			// CompletionOnClassReference
			completionOnClassReference(completionNode);
		}
		else if(completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.NAMESPACE_REFERENCE){
			// completionOnNamespaceReference
			completionOnNamespaceReference(completionNode);
		}
		else if(completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.EXCEPTION_REFERENCE){
			// CompletionOnExceptionReference
			completionOnExceptionReference(completionNode);
		}
		else if(completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.MACRO_REFERENCE){
			// CompletionOnMacroReference
			completionOnMacroReference(completionNode);
		}
		else if(completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.FUNCTION_REFERENCE){
			// completionOnFunctionReference
			completionOnFunctionReference(completionNode);
		}
		else if(completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.CONSTRUCTOR_REFERENCE){
			// completionOnConstructorReference
			completionOnConstructorReference(completionNode);
		}
		else if(completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.KEYWORD){
			// CompletionOnKeyword
			completionOnKeyword(completionNode);
		}
	
		completionList.addAll(completions);
		return completionNode;
			
	}
	
}
