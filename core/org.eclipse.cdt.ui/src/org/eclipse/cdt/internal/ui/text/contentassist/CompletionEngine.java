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
import org.eclipse.cdt.core.ICLogConstants;
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
import org.eclipse.cdt.core.parser.ast.IASTCodeScope;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTMacro;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode.CompletionKind;
import org.eclipse.cdt.core.parser.ast.IASTNode.LookupKind;
import org.eclipse.cdt.core.parser.ast.IASTNode.LookupResult;
import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.cdt.internal.core.model.IDebugLogConstants;
import org.eclipse.cdt.internal.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.model.Util;
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
	// scope relevance element counters
	private int numFields = 0;
	private int numVariables = 0;
	private int numLocalVariables = 0;
	private int numMethods = 0;
	private int numFunctions = 0;
	private int numClasses = 0;
	private int numStructs = 0;
	private int numUnions = 0;
	private int numEnumerations = 0;
	private int numEnumerators = 0;
	private int numNamespaces = 0;
	private int numMacros = 0;
	
	public CompletionEngine(ICompletionRequestor completionRequestor){
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
			case ICElement.C_VARIABLE_LOCAL:
				return LOCAL_VARIABLE_TYPE_RELEVANCE;
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
			IScanner scanner = ParserFactory.createScanner( reader, realPath.toOSString(), scanInfo, ParserMode.CONTEXTUAL_PARSE, language, requestor, ParserUtil.getParserLogService() );
			parser  = ParserFactory.createParser( scanner, requestor, ParserMode.CONTEXTUAL_PARSE, language, ParserUtil.getParserLogService() );
		}
		catch( ParserFactoryException pfe )
		{
					
		}
		if(parser != null){
			IASTCompletionNode result = null;
			try {
				result = parser.parse(completionOffset);
			} catch (ParserNotImplementedException e) {
			}
			return result;
		} else {
			return null;
		}	 	
	}
	
	private void addNodeToCompletions(IASTNode node, String prefix, int totalNumberOfResults){
		if(node instanceof IASTField){
			numFields++;
			IASTField field = (IASTField)node;
			int relevance = computeRelevance(ICElement.C_FIELD, prefix, field.getName());
			relevance += totalNumberOfResults - numFields;
			
			requestor.acceptField(field.getName(), 
					ASTUtil.getType(field.getAbstractDeclaration()),
					field.getVisiblity(), completionStart, completionLength, relevance);
		}
		else if (node instanceof IASTParameterDeclaration){
			numLocalVariables++;
			IASTParameterDeclaration param = (IASTParameterDeclaration) node;
			int relevance = computeRelevance(ICElement.C_VARIABLE_LOCAL, prefix, param.getName());
			relevance += totalNumberOfResults - numLocalVariables;
			
			requestor.acceptLocalVariable(param.getName(), 
					ASTUtil.getType(param),
					completionStart, completionLength, relevance);
		}
		else if(node instanceof IASTVariable){
			IASTVariable variable = (IASTVariable)node;
			// get the container to check if it is a local variable
			IASTNode container = variable.getOwnerScope();
			if(container instanceof IASTCodeScope){
				numLocalVariables++;
				int relevance = computeRelevance(ICElement.C_VARIABLE_LOCAL, prefix, variable.getName());
				relevance += totalNumberOfResults - numLocalVariables;
				
				requestor.acceptLocalVariable(variable.getName(), 
						ASTUtil.getType(variable.getAbstractDeclaration()),
						completionStart, completionLength, relevance);
			}else {
			numVariables++;
			int relevance = computeRelevance(ICElement.C_VARIABLE, prefix, variable.getName());
			relevance += totalNumberOfResults - numVariables;
			
			requestor.acceptVariable(variable.getName(), 
				ASTUtil.getType(variable.getAbstractDeclaration()),
				completionStart, completionLength, relevance);
			}
		}
		else if(node instanceof IASTMethod) {
			numMethods++;
			IASTMethod method = (IASTMethod)node;
			int relevance = computeRelevance(ICElement.C_METHOD, prefix, method.getName());
			relevance += totalNumberOfResults - numMethods;
			
			String parameterString = ASTUtil.getParametersString(ASTUtil.getFunctionParameterTypes(method));
			requestor.acceptMethod(method.getName(), 
				parameterString,
				ASTUtil.getType(method.getReturnType()), 
				method.getVisiblity(), completionStart, completionLength, relevance);
		}
		else if(node instanceof IASTFunction){
			numFunctions++;
			IASTFunction function = (IASTFunction)node;
			int relevance = computeRelevance(ICElement.C_FUNCTION, prefix, function.getName());
			relevance += totalNumberOfResults - numFunctions;
			
			String parameterString = ASTUtil.getParametersString(ASTUtil.getFunctionParameterTypes(function));
			requestor.acceptFunction(function.getName(), 
				parameterString,					
				ASTUtil.getType(function.getReturnType()), 
				completionStart, completionLength, relevance);
		}
		else if(node instanceof IASTClassSpecifier){
			IASTClassSpecifier classSpecifier = (IASTClassSpecifier)node;
			ASTClassKind classkind = classSpecifier.getClassKind();
			if(classkind == ASTClassKind.CLASS){
				numClasses++;
				int relevance = computeRelevance(ICElement.C_CLASS, prefix, classSpecifier.getName());
				relevance += totalNumberOfResults - numClasses;
				
				requestor.acceptClass(classSpecifier.getName(), 
					completionStart, completionLength, relevance);
			}
			if(classkind == ASTClassKind.STRUCT){
				numStructs++;
				int relevance = computeRelevance(ICElement.C_STRUCT, prefix, classSpecifier.getName());
				relevance += totalNumberOfResults - numStructs;
				
				requestor.acceptStruct(classSpecifier.getName(), 
					completionStart, completionLength, relevance);
			}
			if(classkind == ASTClassKind.UNION){
				numUnions++;
				int relevance = computeRelevance(ICElement.C_UNION, prefix, classSpecifier.getName());
				relevance += totalNumberOfResults - numUnions;
				
				requestor.acceptUnion(classSpecifier.getName(), 
					completionStart, completionLength, relevance);
			}				
		}
		else if(node instanceof IASTMacro){
			numMacros++;
			IASTMacro macro = (IASTMacro)node;
			int relevance = computeRelevance(ICElement.C_MACRO, prefix, macro.getName());
			relevance += totalNumberOfResults - numMacros;
			
			requestor.acceptMacro(macro.getName(), completionStart, completionLength, relevance);
		}
		else if(node instanceof IASTNamespaceDefinition){
			numNamespaces++;
			IASTNamespaceDefinition namespace = (IASTNamespaceDefinition)node;
			int relevance = computeRelevance(ICElement.C_NAMESPACE, prefix, namespace.getName());
			relevance += totalNumberOfResults - numNamespaces;
			
			requestor.acceptNamespace(namespace.getName(), completionStart, completionLength, relevance);
		}
		else if(node instanceof IASTEnumerationSpecifier){
			numEnumerations++;
			IASTEnumerationSpecifier enumeration = (IASTEnumerationSpecifier)node;
			int relevance = computeRelevance(ICElement.C_ENUMERATION, prefix, enumeration.getName());
			relevance += totalNumberOfResults - numEnumerations;
			
			requestor.acceptEnumeration(enumeration.getName(), completionStart, completionLength, relevance);
		}
		else if(node instanceof IASTEnumerator){
			numEnumerators++;
			IASTEnumerator enumerator = (IASTEnumerator)node;
			int relevance = computeRelevance(ICElement.C_ENUMERATOR, prefix, enumerator.getName());
			relevance += totalNumberOfResults - numEnumerators;
			
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
	
	private void resetElementNumbers(){
		numFields = 0;
		numVariables = 0;
		numLocalVariables = 0;
		numMethods = 0;
		numFunctions = 0;
		numClasses = 0;
		numStructs = 0;
		numUnions = 0;
		numEnumerations = 0;
		numEnumerators = 0;
		numNamespaces = 0;
		numMacros = 0;
	}
	private void addToCompletions (LookupResult result){
		if(result == null)
			return;
		Iterator nodes = result.getNodes();
		int numberOfElements = result.getResultsSize();
		resetElementNumbers();
		while (nodes.hasNext()){
			IASTNode node = (IASTNode) nodes.next();
			addNodeToCompletions(node, result.getPrefix(), numberOfElements);	
		}
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
	
	private LookupResult lookup(IASTScope searchNode, String prefix, LookupKind[] kinds, IASTNode context){
		try {
			logLookups (kinds);
			LookupResult result = searchNode.lookup (prefix, kinds, context);
			return result ;
		} catch (IASTNode.LookupException ilk ){
			// do we want to do something here?
			ilk.printStackTrace();
			return null;
		}
	}
	private void completionOnMemberReference(IASTCompletionNode completionNode){
		// Completing after a dot
		// 1. Get the search scope node
		IASTScope searchNode = completionNode.getCompletionScope();
		
		LookupResult result = null;
		// lookup fields and methods with the right visibility
		IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[2];
		kinds[0] = IASTNode.LookupKind.FIELDS; 
		kinds[1] = IASTNode.LookupKind.METHODS; 
		result = lookup (searchNode, completionNode.getCompletionPrefix(), kinds, completionNode.getCompletionContext());
		addToCompletions (result);
				
	}
	private void completionOnScopedReference(IASTCompletionNode completionNode){
		// 1. Get the search scope node
		// the search node is the name before the qualification 
		IASTScope searchNode = completionNode.getCompletionScope();
		// here we have to look for anything that could be referenced within this scope
		// 1. lookup local variables, global variables, functions, methods, structures, enums, macros, and namespaces
		IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[4];
		kinds[0] = IASTNode.LookupKind.VARIABLES; 
		kinds[1] = IASTNode.LookupKind.STRUCTURES; 
		kinds[2] = IASTNode.LookupKind.ENUMERATIONS; 
		kinds[3] = IASTNode.LookupKind.NAMESPACES; 
		LookupResult result = lookup(searchNode, completionNode.getCompletionPrefix(), kinds, completionNode.getCompletionContext());
		addToCompletions(result);
		// TODO
		// lookup static members (field / methods) in type
}
	private void completionOnTypeReference(IASTCompletionNode completionNode){
		// completing on a type
		// 1. Get the search scope node
		IASTScope searchNode = completionNode.getCompletionScope();
		// if the prefix is not empty
		if(completionNode.getCompletionPrefix().length() > 0 ) {
			// 2. Lookup all types that could be used here
			LookupResult result;
			IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[2];
			kinds[0] = IASTNode.LookupKind.STRUCTURES; 				
			kinds[1] = IASTNode.LookupKind.ENUMERATIONS; 				
			result = lookup(searchNode, completionNode.getCompletionPrefix(), kinds, completionNode.getCompletionContext());
			addToCompletions(result);
		} else // prefix is empty, we can not look for everything 
		{
			
		}
	}
	
	private void completionOnFieldType(IASTCompletionNode completionNode){
		// 1. basic completion on all types
		completionOnTypeReference(completionNode);
		// 2. Get the search scope node
		IASTScope searchNode = completionNode.getCompletionScope();
		// TODO
		// 3. provide a template for constructor/ destructor
		// 4. lookup methods
		// we are at a field declaration place, the user could be trying to override a function.
		// We have to lookup functions that could be overridden here.
//		LookupResult result;
//		IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[1];
//		kinds[0] = IASTNode.LookupKind.METHODS; 
//		result = lookup(searchNode, completionNode.getCompletionPrefix(), kinds, completionNode.getCompletionContext());
//		addToCompletions(result);
		
	}
	private void completionOnVariableType(IASTCompletionNode completionNode){
		// 1. basic completion on all types
		completionOnTypeReference(completionNode);
	}
	private void completionOnSingleNameReference(IASTCompletionNode completionNode){
		// 1. Get the search scope node
		// the search node is the code scope inwhich completion is requested
		IASTScope searchNode = completionNode.getCompletionScope();
		// if prefix is not empty
		if (completionNode.getCompletionPrefix().length() > 0){
			// here we have to look for anything that could be referenced within this scope
			// 1. lookup local variables, global variables, functions, methods, structures, enums, macros, and namespaces
			IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[7];
			kinds[0] = IASTNode.LookupKind.LOCAL_VARIABLES; 
			kinds[1] = IASTNode.LookupKind.FIELDS; 
			kinds[2] = IASTNode.LookupKind.VARIABLES; 
			kinds[3] = IASTNode.LookupKind.STRUCTURES; 
			kinds[4] = IASTNode.LookupKind.ENUMERATIONS; 
			kinds[5] = IASTNode.LookupKind.METHODS; 
			kinds[6] = IASTNode.LookupKind.FUNCTIONS; 
			LookupResult result = lookup(searchNode, completionNode.getCompletionPrefix(), kinds, completionNode.getCompletionContext());
			addToCompletions(result);
		} else // prefix is empty
		{
			// instead of only fields and methods
			// kinds[0] = IASTNode.LookupKind.THIS 
			IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[3];
			kinds[0] = IASTNode.LookupKind.LOCAL_VARIABLES; 
			kinds[1] = IASTNode.LookupKind.FIELDS; 
			kinds[2] = IASTNode.LookupKind.METHODS; 
			LookupResult result = lookup(searchNode, completionNode.getCompletionPrefix(), kinds, completionNode.getCompletionContext());
			addToCompletions(result);
		}
	}

	private void completionOnClassReference(IASTCompletionNode completionNode){
		// 1. Get the search scope node 
		IASTScope searchNode = completionNode.getCompletionScope();
		// only look for classes
		IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[1];
		kinds[0] = IASTNode.LookupKind.CLASSES; 
		LookupResult result = lookup(searchNode, completionNode.getCompletionPrefix(), kinds, completionNode.getCompletionContext());
		addToCompletions(result);
	}
	private void completionOnNamespaceReference(IASTCompletionNode completionNode){
		// 1. Get the search scope node 
		IASTScope searchNode = completionNode.getCompletionScope();
		// only look for namespaces
		IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[1];
		kinds[0] = IASTNode.LookupKind.NAMESPACES; 
		LookupResult result = lookup(searchNode, completionNode.getCompletionPrefix(), kinds, completionNode.getCompletionContext());
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
		IASTScope searchNode = completionNode.getCompletionScope();
		// only look for macros
		IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[1];
		kinds[0] = IASTNode.LookupKind.MACROS; 
		LookupResult result = lookup(searchNode, completionNode.getCompletionPrefix(), kinds, completionNode.getCompletionContext());
		addToCompletions(result);
	}
	private void completionOnFunctionReference(IASTCompletionNode completionNode){
		// TODO: complete the lookups
	}
	private void completionOnConstructorReference(IASTCompletionNode completionNode){
		// 1. Get the search scope node 
		IASTScope searchNode = completionNode.getCompletionScope();
		// only lookup constructors
		IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[1];
		kinds[0] = IASTNode.LookupKind.CONSTRUCTORS; 
		LookupResult result = lookup(searchNode, completionNode.getCompletionPrefix(), kinds, completionNode.getCompletionContext());
		addToCompletions(result);
	}
	private void completionOnKeyword(IASTCompletionNode completionNode){
		// lookup every type of keywords
		// 1. basic types keword list
		List result = lookupKeyword(completionNode.getCompletionPrefix(), BASIC_TYPES_KEYWORDS);
		addKeywordsToCompletions(result.iterator());
	}
	
	public IASTCompletionNode complete(IWorkingCopy sourceUnit, int completionOffset) {
		
		// 1- Parse the translation unit
		IASTCompletionNode completionNode = parse(sourceUnit, completionOffset);
		
		log("");
		
		if (completionNode == null){
			log("Null Completion Node Error");
			return null;
		}
		
		logNode("Scope   = " , completionNode.getCompletionScope());
		logNode("Context = " , completionNode.getCompletionContext());
		logKind("Kind    = ", completionNode.getCompletionKind().getEnumValue());		
		
		// set the completionStart and the completionLength
		completionStart = completionOffset - completionNode.getCompletionPrefix().length();
		completionLength = completionNode.getCompletionPrefix().length();
		CompletionKind kind = completionNode.getCompletionKind();
		
		// 2- Check the return value 
		if(kind == IASTCompletionNode.CompletionKind.MEMBER_REFERENCE){
			// completionOnMemberReference
			completionOnMemberReference(completionNode);
		}
		else if(kind == IASTCompletionNode.CompletionKind.SCOPED_REFERENCE){
			// completionOnMemberReference
			completionOnScopedReference(completionNode);
		}
		else if(kind == IASTCompletionNode.CompletionKind.FIELD_TYPE){
			// CompletionOnFieldType
			completionOnFieldType(completionNode);
		}
		else if(kind == IASTCompletionNode.CompletionKind.VARIABLE_TYPE) {
			// CompletionOnVariableType
			completionOnVariableType(completionNode);
		}
		else if(kind == IASTCompletionNode.CompletionKind.ARGUMENT_TYPE){
			// CompletionOnArgumentType
			completionOnTypeReference(completionNode);
		}
		else if(kind == IASTCompletionNode.CompletionKind.SINGLE_NAME_REFERENCE){
			// CompletionOnSingleNameReference
			completionOnSingleNameReference(completionNode);
		}
		else if(kind == IASTCompletionNode.CompletionKind.TYPE_REFERENCE){
			// CompletionOnStructureReference
			completionOnTypeReference(completionNode);
		}
		else if(kind == IASTCompletionNode.CompletionKind.CLASS_REFERENCE){
			// CompletionOnClassReference
			completionOnClassReference(completionNode);
		}
		else if(kind == IASTCompletionNode.CompletionKind.NAMESPACE_REFERENCE){
			// completionOnNamespaceReference
			completionOnNamespaceReference(completionNode);
		}
		else if(kind == IASTCompletionNode.CompletionKind.EXCEPTION_REFERENCE){
			// CompletionOnExceptionReference
			completionOnExceptionReference(completionNode);
		}
		else if(kind == IASTCompletionNode.CompletionKind.MACRO_REFERENCE){
			// CompletionOnMacroReference
			completionOnMacroReference(completionNode);
		}
		else if(kind == IASTCompletionNode.CompletionKind.FUNCTION_REFERENCE){
			// completionOnFunctionReference
			completionOnFunctionReference(completionNode);
		}
		else if(kind == IASTCompletionNode.CompletionKind.CONSTRUCTOR_REFERENCE){
			// completionOnConstructorReference
			completionOnConstructorReference(completionNode);
		}
		else if(kind == IASTCompletionNode.CompletionKind.KEYWORD){
			// CompletionOnKeyword
			completionOnKeyword(completionNode);
		}
	
		addKeywordsToCompletions( completionNode.getKeywords());
		return completionNode;
			
	}
	private void logKind(String message, int kindEnum){
		if (! CCorePlugin.getDefault().isDebugging() && Util.isActive(IDebugLogConstants.CONTENTASSIST) )
			return;
		
		String kindStr = "";
		switch (kindEnum){
		case 0:
			kindStr = "MEMBER_REFERENCE";
			break;
	
		case 1:
			kindStr = "SCOPED_REFERENCE";
			break;
			
		case 2:
			kindStr = "FIELD_TYPE";
			break;
			
		case 3:
			kindStr = "VARIABLE_TYPE";
			break;
			
		case 4:
			kindStr = "ARGUMENT_TYPE";
			break;
			
		case 5:
			kindStr = "SINGLE_NAME_REFERENCE";
			break;
			
		case 6:
			kindStr = "TYPE_REFERENCE";
			break;
			
		case 7:
			kindStr = "CLASS_REFERENCE";
			break;
			
		case 8:
			kindStr = "NAMESPACE_REFERENCE";
			break;
			
		case 9:
			kindStr = "EXCEPTION_REFERENCE";
			break;
			
		case 10:
			kindStr = "MACRO_REFERENCE";
			break;
			
		case 11:
			kindStr = "FUNCTION_REFERENCE";
			break;
			
		case 12:
			kindStr = "CONSTRUCTOR_REFERENCE";
			break;
			
		case 13:
			kindStr = "KEYWORD";
			break;
			
		case 14:
			kindStr = "PREPROCESSOR_DIRECTIVE";
			break;
			
		case 15:
			kindStr = "USER_SPECIFIED_NAME";
			break;
			
		case 200:
			kindStr = "NO_SUCH_KIND";
			break;
		}
		log (message + kindStr);
	}
	private void logNode(String message, IASTNode node){
		if (! CCorePlugin.getDefault().isDebugging() && Util.isActive(IDebugLogConstants.CONTENTASSIST))
			return;
		
		if(node == null){
			log(message + "null");
			return;
		}
		if(node instanceof IASTMethod){
			String name = "Method: ";
			name += ((IASTMethod)node).getName();
			log(message + name);
			return;
		}
		if(node instanceof IASTFunction){
			String name = "Function: ";
			name += ((IASTFunction)node).getName();
			log(message + name);
			return;
		}
		if(node instanceof IASTClassSpecifier){
			String name = "Class: ";
			name += ((IASTClassSpecifier)node).getName();
			log(message + name);
			return;
		}
		if(node instanceof IASTCompilationUnit){
			String name = "Global";
			log(message + name);
			return;
		}
		
		log(message + node.toString());
		return;
		
	}
	private void logLookups(LookupKind[] kinds){
		if (! CCorePlugin.getDefault().isDebugging() && Util.isActive(IDebugLogConstants.CONTENTASSIST))
			return;
		
		StringBuffer kindName = new StringBuffer("Looking For ");
		for(int i = 0; i<kinds.length; i++){
			LookupKind kind = (LookupKind) kinds[i];
			switch (kind.getEnumValue()){
			case 0:
				kindName.append("ALL");
				break;
			case 1:
				kindName.append("STRUCTURES");
				break;
			case 2:
				kindName.append("STRUCS");
				break;
			case 3:
				kindName.append("UNIONS");
				break;
			case 4:
				kindName.append("CLASSES");
				break;
			case 5:
				kindName.append("FUNCTIONS");
				break;
			case 6:
				kindName.append("VARIABLES");
				break;
			case 7:
				kindName.append("LOCAL_VARIABLES");
				break;
			case 8:
				kindName.append("MEMBERS");
				break;
			case 9:
				kindName.append("METHODS");
				break;
			case 10:
				kindName.append("FIELDS");
				break;
			case 11:
				kindName.append("CONSTRUCTORS");
				break;
			case 12:
				kindName.append("NAMESPACES"); 
				break;
			case 13:
				kindName.append("MACROS"); 
				break;
			case 14:
				kindName.append("ENUMERATIONS"); 
				break;
			case 15:
				kindName.append("ENUMERATORS");
				break;
			case 16:
				kindName.append("THIS");
				break;		
			}
			kindName.append(", ");
		}
		log (kindName.toString());
	}
	private void log(String message){
		if (! CCorePlugin.getDefault().isDebugging() && Util.isActive(IDebugLogConstants.CONTENTASSIST))
			return;
		Util.debugLog(message, IDebugLogConstants.CONTENTASSIST, false);
	}
}
