/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserFactoryError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.ASTUtil;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCodeScope;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode.CompletionKind;
import org.eclipse.cdt.core.parser.ast.IASTNode.ILookupResult;
import org.eclipse.cdt.core.parser.ast.IASTNode.LookupKind;
import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.cdt.internal.core.parser.scanner2.FunctionStyleMacro;
import org.eclipse.cdt.internal.core.parser.scanner2.ObjectStyleMacro;
import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.cdt.internal.ui.util.IDebugLogConstants;
import org.eclipse.cdt.internal.ui.util.Util;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 *
 * This class is the entry point for code completions.
 * It contains a public API used to call ContentAssist on a given working copy 
 * and a given completion offset.
 * 
 */
public class CompletionEngine implements RelevanceConstants {
	ICompletionRequestor requestor;
	int completionStart = 0;
	int completionLength = 0;
	int completionOrigin = 0;
	IPreferenceStore store = CUIPlugin.getDefault().getPreferenceStore();
	private Map macroMap = null;
	private ContentAssistElementRequestor elementRequestor = null;
	
	private static final String exceptionKeyword = "..."; //$NON-NLS-1$

	public CompletionEngine(ICompletionRequestor completionRequestor){
			requestor = completionRequestor;
			elementRequestor = new ContentAssistElementRequestor();
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
			case ICElement.C_TYPEDEF:
				return TYPEDEF_TYPE_RELEVANCE;
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
		// Get resource info
		IResource currentResource = sourceUnit.getResource();
		IPath realPath = currentResource.getLocation(); 
		IProject project = currentResource.getProject();
		CodeReader reader = new CodeReader(realPath.toOSString(), sourceUnit.getContents()); 
		
		//Get the scanner info
		IScannerInfo scanInfo = new ScannerInfo();
		IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(project);
		if (provider != null){
			IScannerInfo buildScanInfo = provider.getScannerInformation(currentResource);
			if( buildScanInfo != null )
				scanInfo = new ScannerInfo(buildScanInfo.getDefinedSymbols(), buildScanInfo.getIncludePaths());
		} 			
	
		//C or CPP?
		ParserLanguage language = CoreModel.hasCCNature(project) ? ParserLanguage.CPP : ParserLanguage.C;
	
		IParser parser = null;
		IScanner scanner = null;
		try
		{
			scanner = ParserFactory.createScanner( reader, scanInfo, ParserMode.COMPLETION_PARSE, language, elementRequestor, ParserUtil.getScannerLogService(), Arrays.asList(CUIPlugin.getSharedWorkingCopies()) );
			parser  = ParserFactory.createParser( scanner, elementRequestor, ParserMode.COMPLETION_PARSE, language, ParserUtil.getParserLogService() );
			elementRequestor.setParser(parser);
		}
		catch( ParserFactoryError pfe )
		{
					
		}
		if(parser != null){
			IASTCompletionNode result = null;
			try {
				// set timeout
				IPreferenceStore prefStore = CUIPlugin.getDefault().getPreferenceStore();
				int timeout = prefStore.getInt(ContentAssistPreference.TIMEOUT_DELAY);
				if( timeout > 0 )
					elementRequestor.setTimeout(timeout);

				// start timer
				elementRequestor.startTimer();
				long parserTime = System.currentTimeMillis();
				macroMap = null;
				result = parser.parse(completionOffset);
				log("Time spent in Parser = "+ ( System.currentTimeMillis() - parserTime ) + " ms");		 //$NON-NLS-1$ //$NON-NLS-2$
				
				macroMap = scanner.getDefinitions();
			} catch (ParseError e ) {
				if(e.getErrorKind() == ParseError.ParseErrorKind.TIMEOUT_OR_CANCELLED){
					log("Timeout received !!!!!! "); //$NON-NLS-1$;
					requestor.acceptError(new Problem(CUIMessages.getString("CEditor.contentassist.timeout"))); //$NON-NLS-1$;
				}
			} finally {
				// stop timer
				elementRequestor.stopTimer();
			}
			return result;
		} else {
			return null;
		}	 	
	}
	
	private void addNodeToCompletions(IASTNode node, String prefix, int totalNumberOfResults, boolean addStaticMethodsOnly, boolean addStaticFieldsOnly, int parameterIndex){
		if(node instanceof IASTField){
			IASTField field = (IASTField)node;
			if(addStaticFieldsOnly && (!field.isStatic()))
				return;
			int relevance = computeRelevance(ICElement.C_FIELD, prefix, field.getName());
			
			requestor.acceptField(field.getName(), 
					ASTUtil.getType(field.getAbstractDeclaration()),
					field.getVisiblity(), completionStart, completionLength, relevance);
		}
		else if (node instanceof IASTParameterDeclaration){
			IASTParameterDeclaration param = (IASTParameterDeclaration) node;
			int relevance = computeRelevance(ICElement.C_VARIABLE_LOCAL, prefix, param.getName());
			
			requestor.acceptLocalVariable(param.getName(), 
					ASTUtil.getType(param),
					completionStart, completionLength, relevance);
		}
		else if(node instanceof IASTVariable){
			IASTVariable variable = (IASTVariable)node;
			// get the container to check if it is a local variable
			IASTNode container = variable.getOwnerScope();
			if(container instanceof IASTCodeScope){
				// local variable
				int relevance = computeRelevance(ICElement.C_VARIABLE_LOCAL, prefix, variable.getName());
				
				requestor.acceptLocalVariable(variable.getName(), 
						ASTUtil.getType(variable.getAbstractDeclaration()),
						completionStart, completionLength, relevance);
			}else {
			// global variable	
			if(addStaticFieldsOnly && (!variable.isStatic()))
				return;

			int relevance = computeRelevance(ICElement.C_VARIABLE, prefix, variable.getName());
			
			requestor.acceptVariable(variable.getName(), 
				ASTUtil.getType(variable.getAbstractDeclaration()),
				completionStart, completionLength, relevance);
			}
		}
		else if(node instanceof IASTMethod) {
			IASTMethod method = (IASTMethod)node;
			if(addStaticMethodsOnly && (!method.isStatic()))
				return;
			
			int relevance = computeRelevance(ICElement.C_METHOD, prefix, method.getName());
			
			String parameterString = ASTUtil.getParametersString(ASTUtil.getFunctionParameterTypes(method));
			
			int contextInfoOffset = completionOrigin;
			if( parameterIndex > -1 && parameterString.length() > 0){
				int idx = 0;
				for( int i = 0; i < parameterIndex; i++ ){
					idx = parameterString.indexOf( ',', idx );
				}
				contextInfoOffset -= idx;
			}
			
			requestor.acceptMethod(method.getName(), 
				parameterString,
				ASTUtil.getType(method.getReturnType()), 
				method.getVisiblity(), completionStart, completionLength, relevance, (parameterIndex == -1 ), contextInfoOffset);
		}
		else if(node instanceof IASTFunction){
			IASTFunction function = (IASTFunction)node;
			if(addStaticMethodsOnly && (!function.isStatic()))
				return;
			
			int relevance = computeRelevance(ICElement.C_FUNCTION, prefix, function.getName());
			
			String parameterString = ASTUtil.getParametersString(ASTUtil.getFunctionParameterTypes(function));
			
			int contextInfoOffset = completionOrigin;
			if( parameterIndex > -1 && parameterString.length() > 0){
				int idx = 0;
				for( int i = 0; i < parameterIndex; i++ ){
					idx = parameterString.indexOf( ',', idx );
				}
				contextInfoOffset -= idx;
			}

			requestor.acceptFunction(function.getName(), 
				parameterString,					
				ASTUtil.getType(function.getReturnType()), 
				completionStart, completionLength, relevance, (parameterIndex == -1 ), contextInfoOffset);
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
				if (classSpecifier.getName().length() > 0){
					int relevance = computeRelevance(ICElement.C_STRUCT, prefix, classSpecifier.getName());
					
					requestor.acceptStruct(classSpecifier.getName(), 
						completionStart, completionLength, relevance);
				}
			}
			if(classkind == ASTClassKind.UNION){
				if(classSpecifier.getName().length() > 0){
					int relevance = computeRelevance(ICElement.C_UNION, prefix, classSpecifier.getName());
					
					requestor.acceptUnion(classSpecifier.getName(), 
						completionStart, completionLength, relevance);
				}
			}				
		}
		else if(node instanceof IASTNamespaceDefinition){
			IASTNamespaceDefinition namespace = (IASTNamespaceDefinition)node;
			int relevance = computeRelevance(ICElement.C_NAMESPACE, prefix, namespace.getName());
			
			requestor.acceptNamespace(namespace.getName(), completionStart, completionLength, relevance);
		}
		else if(node instanceof IASTEnumerationSpecifier){
			IASTEnumerationSpecifier enumeration = (IASTEnumerationSpecifier)node;
			if(enumeration.getName().length() > 0){
				int relevance = computeRelevance(ICElement.C_ENUMERATION, prefix, enumeration.getName());

				requestor.acceptEnumeration(enumeration.getName(), completionStart, completionLength, relevance);
			}
		}
		else if(node instanceof IASTEnumerator){
			IASTEnumerator enumerator = (IASTEnumerator)node;
			int relevance = computeRelevance(ICElement.C_ENUMERATOR, prefix, enumerator.getName());
			
			requestor.acceptEnumerator(enumerator.getName(), completionStart, completionLength, relevance);
		}
		else if(node instanceof IASTTypedefDeclaration){
			IASTTypedefDeclaration typedef = (IASTTypedefDeclaration)node;
			int relevance = computeRelevance(ICElement.C_TYPEDEF, prefix, typedef.getName());
			
			requestor.acceptTypedef(typedef.getName(), completionStart, completionLength, relevance);
		}
		else if(node instanceof IASTElaboratedTypeSpecifier){
			IASTElaboratedTypeSpecifier elaboratedTypeSpecifier = (IASTElaboratedTypeSpecifier)node;
			ASTClassKind classkind = elaboratedTypeSpecifier.getClassKind();
			if(classkind == ASTClassKind.CLASS){
				int relevance = computeRelevance(ICElement.C_CLASS, prefix, elaboratedTypeSpecifier.getName());
				
				requestor.acceptClass(elaboratedTypeSpecifier.getName(), 
					completionStart, completionLength, relevance);
			}
			else if(classkind == ASTClassKind.STRUCT){
				int relevance = computeRelevance(ICElement.C_STRUCT, prefix, elaboratedTypeSpecifier.getName());
				
				requestor.acceptStruct(elaboratedTypeSpecifier.getName(), 
					completionStart, completionLength, relevance);
			}
			else if(classkind == ASTClassKind.UNION){
				int relevance = computeRelevance(ICElement.C_UNION, prefix, elaboratedTypeSpecifier.getName());
				
				requestor.acceptUnion(elaboratedTypeSpecifier.getName(), 
					completionStart, completionLength, relevance);
			}				
			else if(classkind == ASTClassKind.ENUM){
				int relevance = computeRelevance(ICElement.C_ENUMERATION, prefix, elaboratedTypeSpecifier.getName());
				
				requestor.acceptEnumeration(elaboratedTypeSpecifier.getName(), 
					completionStart, completionLength, relevance);
			}				
		}		
	}
	
	private void addKeywordToCompletions (String keyword){
		int relevance = KEYWORD_TYPE_RELEVANCE;
		requestor.acceptKeyword(keyword, completionStart, completionLength, relevance);		
	}
	
	private void addKeywordsToCompletions(Iterator keywords){
		int numOfKeywords = 0;
		while (keywords.hasNext()){
			String keyword = (String) keywords.next();
			addKeywordToCompletions(keyword);
			numOfKeywords++;
		}
		log("No of Keywords       = " + numOfKeywords); //$NON-NLS-1$
	}
	
	private void addMacroToCompletions (String prefix, String macroName){
		int relevance = computeRelevance(ICElement.C_MACRO, prefix, macroName);
		requestor.acceptMacro(macroName, completionStart, completionLength, relevance, completionOrigin);		
	}

	private void addMacrosToCompletions(String prefix, Iterator macros){
		int numOfMacros = 0;
		while (macros.hasNext()){
			String macro = (String) macros.next();
			addMacroToCompletions(prefix, macro);
			numOfMacros++;
		}
		log("No of Macros         = " + numOfMacros); //$NON-NLS-1$
	}

	private void addToCompletions (ILookupResult result){
		addToCompletions(result, false, false, -1);
	}	
	
	private void addToCompletions (ILookupResult result, boolean addStaticMethodsOnly, boolean addStaticFieldsOnly, int paramIndex){
		if(result == null){
			log("Lookup Results       = null ................. !!! No Lookup Results found !!! "); //$NON-NLS-1$
			return;
		}
		Iterator nodes = result.getNodes();
		int numberOfElements = result.getResultsSize();
		
		log("No of Lookup Results = " + numberOfElements); //$NON-NLS-1$
		
		while (nodes.hasNext()){
			IASTNode node = (IASTNode) nodes.next();
			addNodeToCompletions(node, result.getPrefix(), numberOfElements, addStaticMethodsOnly, addStaticFieldsOnly, paramIndex );	
		}
		return ;
	}
	
	private ILookupResult lookup(IASTScope searchNode, String prefix, LookupKind[] kinds, IASTNode context, IASTExpression expression){
		try {
			logLookups (kinds);
			ILookupResult result = searchNode.lookup (prefix, kinds, context, expression);
			return result ;
		} catch (IASTNode.LookupError ilk ){
			// do we want to do something here?
			ilk.printStackTrace();
			return null;
		} catch (ASTNotImplementedException e) {
			// shouldn't happen
			e.printStackTrace();
			return null;
		}
	}
	
	private List lookupMacros(String prefix){	
		Set keySet = new TreeSet(macroMap.keySet());
		Iterator i = keySet.iterator();
		final int length = prefix.length();
		String newPrefix = prefix.toUpperCase();
		List resultSet = new ArrayList();
		String key = null;
		String value = null; //$NON-NLS-1$
		while( i.hasNext() )
		{
			key = (String) i.next();	
			if( key.length() < length )
				continue;
			
			if(key.length() > length) {
				value = key.substring(0, length).toUpperCase();
			}else {
				value = key;
			}
			
			if( value.equals( newPrefix ) ) {
				Object macroD = macroMap.get(key);
				if( macroD instanceof FunctionStyleMacro )
				{
					FunctionStyleMacro f = ((FunctionStyleMacro)macroD);
					StringBuffer buffer = new StringBuffer( String.valueOf( f.name ));
					buffer.append( "("); //$NON-NLS-1$
					if( f.arglist != null )
					{
						for( int j = 0; j < f.arglist.length; ++j )
						{
							if( f.arglist[j] != null )
								buffer.append( f.arglist[j]);
							if( j != f.arglist.length -1 && f.arglist[j+1] != null )
								buffer.append( ","); //$NON-NLS-1$
						}
					}
					buffer.append( ")"); //$NON-NLS-1$
					String result = buffer.toString();
					resultSet.add( result );
				}
				else if (macroD instanceof ObjectStyleMacro  )
				{
					String v = String.valueOf( ((ObjectStyleMacro)macroD).name);
					resultSet.add( v );
				}
			}
			else if( key.compareTo( prefix ) > 0 )					
				break;
		}
		return resultSet;		
	}
	
	private void completionOnMemberReference(IASTCompletionNode completionNode){
		// Completing after a dot
		// 1. Get the search scope node
		IASTScope searchNode = completionNode.getCompletionScope();
		
		ILookupResult result = null;
		// lookup fields and methods with the right visibility
		IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[3];
		kinds[0] = IASTNode.LookupKind.FIELDS; 
		kinds[1] = IASTNode.LookupKind.METHODS; 
		kinds[2] = IASTNode.LookupKind.ENUMERATORS;
		result = lookup (searchNode, completionNode.getCompletionPrefix(), kinds, completionNode.getCompletionContext(), null);
		addToCompletions (result);
	}	

	private void completionOnScopedReference(IASTCompletionNode completionNode){
		// 1. Get the search scope node
		// the search node is the name before the qualification 
		IASTScope searchNode = completionNode.getCompletionScope();
		// here we have to look for anything that could be referenced within this scope
		// 1. lookup local variables, global variables, functions, methods, structures, enums, and namespaces
		IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[1];
		kinds[0] = IASTNode.LookupKind.ALL; 
/*
		kinds[0] = IASTNode.LookupKind.VARIABLES; 
		kinds[1] = IASTNode.LookupKind.STRUCTURES; 
		kinds[2] = IASTNode.LookupKind.ENUMERATIONS; 
		kinds[3] = IASTNode.LookupKind.NAMESPACES; 
		kinds[4] = IASTNode.LookupKind.TYPEDEFS; 
		kinds[5] = IASTNode.LookupKind.FIELDS; 
		kinds[6] = IASTNode.LookupKind.METHODS; 
		kinds[7] = IASTNode.LookupKind.FUNCTIONS; 
		kinds[8] = IASTNode.LookupKind.ENUMERATORS; 
		kinds[9] = IASTNode.LookupKind.CONSTRUCTORS; 
*/		
		ILookupResult result = lookup(searchNode, completionNode.getCompletionPrefix(), kinds, completionNode.getCompletionContext(), null);
		// lookup static members (field / methods) in types
		if( (completionNode.getCompletionContext() != null)
			&& (completionNode.getCompletionContext() instanceof IASTClassSpecifier) 
			&& (((IASTClassSpecifier) completionNode.getCompletionContext()).getClassKind() != ASTClassKind.ENUM) ){
				if (completionNode.getCompletionScope() instanceof IASTCodeScope){
					addToCompletions(result, true, true, -1);
				}
				else {
					addToCompletions(result, false, true, -1);					
				}
		} else {
			addToCompletions(result);
		}
	}

	private void completionOnTypeReference(IASTCompletionNode completionNode){
		// completing on a type
		// 1. Get the search scope node
		IASTScope searchNode = completionNode.getCompletionScope();
		// if the prefix is not empty, or we have a context
		if(completionNode.getCompletionPrefix().length() > 0 ||
		   completionNode.getCompletionContext() != null ) 
		{
			// 2. Lookup all types that could be used here
			ILookupResult result;
			IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[4];
			kinds[0] = IASTNode.LookupKind.STRUCTURES; 				
			kinds[1] = IASTNode.LookupKind.ENUMERATIONS;
			kinds[2] = IASTNode.LookupKind.NAMESPACES;
			kinds[3] = IASTNode.LookupKind.TYPEDEFS;
			result = lookup(searchNode, completionNode.getCompletionPrefix(), kinds, completionNode.getCompletionContext(), null);
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
		// 3. provide a template for constructor/ destructor
		if(completionNode.getCompletionPrefix().length() == 0){
			if(searchNode instanceof IASTClassSpecifier){
				IASTClassSpecifier classSpec = (IASTClassSpecifier)searchNode;
				if (classSpec.getClassKind() == ASTClassKind.CLASS ) {
					ILookupResult result = null;
					IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[1];
					kinds[0] = IASTNode.LookupKind.STRUCTURES; 
					result = lookup(searchNode, classSpec.getName(), kinds, completionNode.getCompletionContext(), null);
					addToCompletions(result);
				}
			}		
		}
	}
	private void completionOnVariableType(IASTCompletionNode completionNode){
		IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[5];
		kinds[0] = IASTNode.LookupKind.STRUCTURES; 				
		kinds[1] = IASTNode.LookupKind.ENUMERATIONS;
		kinds[2] = IASTNode.LookupKind.NAMESPACES;
		kinds[3] = IASTNode.LookupKind.TYPEDEFS;
		kinds[4] = IASTNode.LookupKind.CONSTRUCTORS;
		ILookupResult result = lookup(completionNode.getCompletionScope(), completionNode.getCompletionPrefix(), kinds, completionNode.getCompletionContext(), null);
		addToCompletions(result);
	}
	
	private void completionOnSingleNameReference(IASTCompletionNode completionNode){
		// 1. Get the search scope node
		// the search node is the code scope inwhich completion is requested
		IASTScope searchNode = completionNode.getCompletionScope();
		String prefix = completionNode.getCompletionPrefix();
		// here we have to look for any names that could be referenced within this scope
		// 1. lookup all
		ILookupResult result = null;
		if (completionNode.getCompletionPrefix().length() > 0){
			IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[1];
			kinds[0] = IASTNode.LookupKind.ALL; 
			result = lookup(searchNode, prefix, kinds, completionNode.getCompletionContext(), null);
			addToCompletions(result);
		} 
		else // prefix is empty
		{
			if(searchNode instanceof IASTCodeScope){
				if (((IASTCodeScope)searchNode).getContainingFunction() instanceof IASTMethod){
					// we are inside of a method
					IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[1];
					kinds[0] = IASTNode.LookupKind.THIS;
					result = lookup(searchNode, completionNode.getCompletionPrefix(), kinds, completionNode.getCompletionContext(), null);
					addToCompletions(result);
					
					kinds = new IASTNode.LookupKind[1];
					kinds[0] = IASTNode.LookupKind.LOCAL_VARIABLES; 
					result = lookup(searchNode, completionNode.getCompletionPrefix(), kinds, completionNode.getCompletionContext(), null);
					addToCompletions(result);
				} else {
					// we are inside of a function
					IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[1];
					kinds[0] = IASTNode.LookupKind.LOCAL_VARIABLES; 
					result = lookup(searchNode, completionNode.getCompletionPrefix(), kinds, completionNode.getCompletionContext(), null);
					addToCompletions(result);
				}
			} else {
				IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[1];
				kinds[0] = IASTNode.LookupKind.ALL;
				result = lookup(searchNode, completionNode.getCompletionPrefix(), kinds, completionNode.getCompletionContext(), null);
				addToCompletions(result);				
			}
		}

		List macros = lookupMacros(completionNode.getCompletionPrefix());
		addMacrosToCompletions(prefix, macros.iterator());
		
	}

	private void completionOnClassReference(IASTCompletionNode completionNode){
		// 1. Get the search scope node 
		IASTScope searchNode = completionNode.getCompletionScope();
		// only look for classes
		IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[1];
		kinds[0] = IASTNode.LookupKind.CLASSES; 
		ILookupResult result = lookup(searchNode, completionNode.getCompletionPrefix(), kinds, completionNode.getCompletionContext(), null);
		addToCompletions(result);
	}

	private void completionOnStructReference(IASTCompletionNode completionNode){
		// 1. Get the search scope node 
		IASTScope searchNode = completionNode.getCompletionScope();
		// only look for classes
		IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[1];
		kinds[0] = IASTNode.LookupKind.STRUCTS; 
		ILookupResult result = lookup(searchNode, completionNode.getCompletionPrefix(), kinds, completionNode.getCompletionContext(), null);
		addToCompletions(result);
	}
	private void completionOnUnionReference(IASTCompletionNode completionNode){
		// 1. Get the search scope node 
		IASTScope searchNode = completionNode.getCompletionScope();
		// only look for classes
		IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[1];
		kinds[0] = IASTNode.LookupKind.UNIONS; 
		ILookupResult result = lookup(searchNode, completionNode.getCompletionPrefix(), kinds, completionNode.getCompletionContext(), null);
		addToCompletions(result);
	}
	private void completionOnEnumReference(IASTCompletionNode completionNode){
		// 1. Get the search scope node 
		IASTScope searchNode = completionNode.getCompletionScope();
		// only look for classes
		IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[1];
		kinds[0] = IASTNode.LookupKind.ENUMERATIONS; 
		ILookupResult result = lookup(searchNode, completionNode.getCompletionPrefix(), kinds, completionNode.getCompletionContext(), null);
		addToCompletions(result);
	}
	
	private void completionOnNamespaceReference(IASTCompletionNode completionNode){
		// 1. Get the search scope node 
		IASTScope searchNode = completionNode.getCompletionScope();
		// only look for namespaces
		IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[1];
		kinds[0] = IASTNode.LookupKind.NAMESPACES; 
		ILookupResult result = lookup(searchNode, completionNode.getCompletionPrefix(), kinds, completionNode.getCompletionContext(), null);
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
		//IASTScope searchNode = completionNode.getCompletionScope();
		// only look for macros
		List result = lookupMacros(completionNode.getCompletionPrefix());
		addMacrosToCompletions(completionNode.getCompletionPrefix(), result.iterator());
	}
	private void completionOnNewTypeReference(IASTCompletionNode completionNode){
		// 1. Get the search scope node 
		//IASTScope searchNode = completionNode.getCompletionScope();
		// look for the specific type being newed and the scope
		//IASTNode context = completionNode.getCompletionContext();
		// basic completion on all types
		completionOnTypeReference(completionNode);
	}

	private void completionOnConstructorReference(IASTCompletionNode completionNode){
		// 1. Get the search scope node 
		IASTScope searchNode = completionNode.getCompletionScope();
		if( completionNode.getFunctionName().length() > 0 && 
			completionNode.getCompletionContext() == null )
		{
			IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[]{ IASTNode.LookupKind.STRUCTURES };
			ILookupResult result = lookup( searchNode, completionNode.getFunctionName(), kinds, null, null );
			if( result != null && result.getResultsSize() == 1 ){
				IASTClassSpecifier cls = (IASTClassSpecifier) result.getNodes().next();
				kinds[ 0 ] = IASTNode.LookupKind.CONSTRUCTORS;
				result = lookup( searchNode, completionNode.getCompletionPrefix(), kinds, cls, completionNode.getFunctionParameters() );
				addToCompletions( result, false, false, result.getIndexOfNextParameter() );
			}
		} else {
			// only lookup constructors
			IASTNode.LookupKind[] kinds = new IASTNode.LookupKind[1];
			kinds[0] = IASTNode.LookupKind.CONSTRUCTORS; 
			ILookupResult result = lookup(searchNode, completionNode.getCompletionPrefix(), kinds, completionNode.getCompletionContext(), null);
			addToCompletions(result);
		}
	}
	private void completionOnFunctionReference(IASTCompletionNode completionNode){
		//NOTE:
		// Hoda, I changed this so it makes sense with regards to your JUnit tests
		// and examples.  If my assumptions are not correct as to what deserves to be 
		// looked up for FUNCTION_REFRENCE then please update the documentation in
		// IASTCompletionNode.java.
		String prefix = completionNode.getCompletionPrefix();
		boolean functionsOnly = false;
		IASTNode.LookupKind[] kinds = null;
		if( prefix.length() == 0 )
		{
			kinds = new IASTNode.LookupKind[] { IASTNode.LookupKind.CONSTRUCTORS, IASTNode.LookupKind.FUNCTIONS, IASTNode.LookupKind.METHODS };
			prefix = completionNode.getFunctionName();
			functionsOnly = true;
		}
		else
			kinds = new IASTNode.LookupKind[] { IASTNode.LookupKind.ALL };

		ILookupResult result = lookup(completionNode.getCompletionScope(), prefix, kinds, completionNode.getCompletionContext(), completionNode.getFunctionParameters());
		if( result != null)
			addToCompletions(result, false, false, functionsOnly ? result.getIndexOfNextParameter() : -1 );

		if( !functionsOnly ){
			List macros = lookupMacros(completionNode.getCompletionPrefix());
			addMacrosToCompletions(prefix, macros.iterator());
		}
	}
	
	public IASTCompletionNode complete(IWorkingCopy sourceUnit, int completionOffset) {
		log(""); //$NON-NLS-1$

		long startTime = System.currentTimeMillis();
		
		// 1- Parse the translation unit
		IASTCompletionNode completionNode = parse(sourceUnit, completionOffset);
		
		if (completionNode == null){
			log("Null Completion Node Error"); //$NON-NLS-1$
			return null;
		}
		
		log    ("Offset  = " + completionOffset); //$NON-NLS-1$
		logNode("Scope   = " , completionNode.getCompletionScope()); //$NON-NLS-1$
		logNode("Context = " , completionNode.getCompletionContext()); //$NON-NLS-1$
		logKind("Kind    = " , completionNode.getCompletionKind());		 //$NON-NLS-1$
		log	   ("Prefix  = " + completionNode.getCompletionPrefix()); //$NON-NLS-1$

		if (completionNode.getCompletionScope() == null){
			log("Null Completion Scope Error"); //$NON-NLS-1$
			return null;
		}
		
		if(completionNode.getCompletionKind() == CompletionKind.NO_SUCH_KIND){
			log("Invalid Completion Kind Error"); //$NON-NLS-1$
			return null;
		}
		
		// set the completionStart and the completionLength
		completionOrigin = completionOffset;
		completionStart = completionOffset - completionNode.getCompletionPrefix().length();
		completionLength = completionNode.getCompletionPrefix().length();
		CompletionKind kind = completionNode.getCompletionKind();
		
		// 2- Check the return value 
		if(kind == CompletionKind.MEMBER_REFERENCE){
			// completionOnMemberReference
			completionOnMemberReference(completionNode);
		}
		else if(kind == CompletionKind.FIELD_TYPE){
			if (completionNode.getCompletionContext() == null){
				// CompletionOnFieldType
				completionOnFieldType(completionNode);
			}else {
				completionOnScopedReference(completionNode);
			}				
		}
		else if(kind == CompletionKind.VARIABLE_TYPE) {
			if (completionNode.getCompletionContext() != null){
				// CompletionOnVariableType
				completionOnVariableType(completionNode);
			}else {
				completionOnTypeReference( completionNode );
			}
		}
		else if(kind == CompletionKind.ARGUMENT_TYPE){
			// CompletionOnArgumentType
			completionOnTypeReference(completionNode);
		}
		else if(kind == CompletionKind.SINGLE_NAME_REFERENCE){
			if (completionNode.getCompletionContext() == null){
				// CompletionOnSingleNameReference
				completionOnSingleNameReference(completionNode);
			}else {
				completionOnScopedReference(completionNode);
			}
		}
		else if(kind == CompletionKind.TYPE_REFERENCE){
			// CompletionOnTypeReference
			completionOnTypeReference(completionNode);
		}
		else if(kind == CompletionKind.CLASS_REFERENCE){
			// CompletionOnClassReference
			completionOnClassReference(completionNode);
		}
		else if(kind == CompletionKind.NAMESPACE_REFERENCE){
			// completionOnNamespaceReference
			completionOnNamespaceReference(completionNode);
		}
		else if(kind == CompletionKind.EXCEPTION_REFERENCE){
			// CompletionOnExceptionReference
			completionOnExceptionReference(completionNode);
		}
		else if(kind == CompletionKind.MACRO_REFERENCE){
			// CompletionOnMacroReference
			completionOnMacroReference(completionNode);
		}
		else if(kind == CompletionKind.NEW_TYPE_REFERENCE){
			// completionOnNewTypeReference
			completionOnNewTypeReference(completionNode);
		}
		else if(kind == CompletionKind.FUNCTION_REFERENCE){
			// completionOnFunctionReference
			completionOnFunctionReference(completionNode);
		}
		else if(kind == CompletionKind.CONSTRUCTOR_REFERENCE){
			// completionOnConstructorReference
			completionOnConstructorReference(completionNode);
		}
		else if(kind == CompletionKind.STRUCT_REFERENCE){
			// CompletionOnClassReference
			completionOnStructReference(completionNode);
		}
		else if(kind == CompletionKind.UNION_REFERENCE){
			// CompletionOnClassReference
			completionOnUnionReference(completionNode);
		}
		else if(kind == CompletionKind.ENUM_REFERENCE){
			// CompletionOnClassReference
			completionOnEnumReference(completionNode);
		}
	
		// add keywords in all cases except for member and scoped reference cases. 
		if(kind != CompletionKind.MEMBER_REFERENCE){
			addKeywordsToCompletions( completionNode.getKeywords());
		}
		
		log("Time spent in Completion Engine = "+ ( System.currentTimeMillis() - startTime ) + " ms");		 //$NON-NLS-1$ //$NON-NLS-2$
		return completionNode;
			
	}
	private void logKind(String message, IASTCompletionNode.CompletionKind kind){
		if (! CUIPlugin.getDefault().isDebugging() && Util.isActive(IDebugLogConstants.CONTENTASSIST))
			return;
		
		String kindStr = ""; //$NON-NLS-1$
		if(kind == IASTCompletionNode.CompletionKind.MEMBER_REFERENCE)
			kindStr = "MEMBER_REFERENCE"; //$NON-NLS-1$
		else if(kind == IASTCompletionNode.CompletionKind.FIELD_TYPE)
			kindStr = "FIELD_TYPE Class Scope"; //$NON-NLS-1$
		else if(kind == IASTCompletionNode.CompletionKind.VARIABLE_TYPE)
			kindStr = "VARIABLE_TYPE Global Scope"; //$NON-NLS-1$
		else if(kind == IASTCompletionNode.CompletionKind.ARGUMENT_TYPE)
			kindStr = "ARGUMENT_TYPE"; //$NON-NLS-1$
		else if(kind == IASTCompletionNode.CompletionKind.SINGLE_NAME_REFERENCE)
			kindStr = "SINGLE_NAME_REFERENCE"; //$NON-NLS-1$
		else if(kind == IASTCompletionNode.CompletionKind.TYPE_REFERENCE)
			kindStr = "TYPE_REFERENCE"; //$NON-NLS-1$
		else if(kind == IASTCompletionNode.CompletionKind.CLASS_REFERENCE)
			kindStr = "CLASS_REFERENCE"; //$NON-NLS-1$
		else if(kind == IASTCompletionNode.CompletionKind.NAMESPACE_REFERENCE)
			kindStr = "NAMESPACE_REFERENCE"; //$NON-NLS-1$
		else if(kind == IASTCompletionNode.CompletionKind.EXCEPTION_REFERENCE)
			kindStr = "EXCEPTION_REFERENCE"; //$NON-NLS-1$
		else if(kind == IASTCompletionNode.CompletionKind.MACRO_REFERENCE)
			kindStr = "MACRO_REFERENCE"; //$NON-NLS-1$
		else if(kind == IASTCompletionNode.CompletionKind.CONSTRUCTOR_REFERENCE)
			kindStr = "CONSTRUCTOR_REFERENCE"; //$NON-NLS-1$
		else if(kind == IASTCompletionNode.CompletionKind.NEW_TYPE_REFERENCE)
			kindStr = "NEW_TYPE_REFERENCE"; //$NON-NLS-1$
		else if(kind == IASTCompletionNode.CompletionKind.PREPROCESSOR_DIRECTIVE)
			kindStr = "PREPROCESSOR_DIRECTIVE"; //$NON-NLS-1$
		else if(kind == IASTCompletionNode.CompletionKind.STRUCT_REFERENCE)
			kindStr = "STRUCT_REFERENCE"; //$NON-NLS-1$
		else if(kind == IASTCompletionNode.CompletionKind.UNION_REFERENCE)
			kindStr = "UNION_REFERENCE"; //$NON-NLS-1$
		else if(kind == IASTCompletionNode.CompletionKind.ENUM_REFERENCE)
			kindStr = "ENUM_REFERENCE"; //$NON-NLS-1$
		else if(kind == IASTCompletionNode.CompletionKind.NO_SUCH_KIND)
			kindStr = "NO_SUCH_KIND"; //$NON-NLS-1$

		log (message + kindStr);
	}
	private void logNode(String message, IASTNode node){
		if (! CUIPlugin.getDefault().isDebugging() && Util.isActive(IDebugLogConstants.CONTENTASSIST))
			return;
		
		if(node == null){
			log(message + "null"); //$NON-NLS-1$
			return;
		}
		if(node instanceof IASTMethod){
			String name = "Method: "; //$NON-NLS-1$
			name += ((IASTMethod)node).getName();
			log(message + name);
			return;
		}
		if(node instanceof IASTFunction){
			String name = "Function: "; //$NON-NLS-1$
			name += ((IASTFunction)node).getName();
			log(message + name);
			return;
		}
		if(node instanceof IASTClassSpecifier){
			String name = "Class: "; //$NON-NLS-1$
			name += ((IASTClassSpecifier)node).getName();
			log(message + name);
			return;
		}
		if(node instanceof IASTCompilationUnit){
			String name = "Global"; //$NON-NLS-1$
			log(message + name);
			return;
		}
		if(node instanceof IASTCodeScope){
			String name = "Code Scope"; //$NON-NLS-1$
			log(message + name);
			return;
		}
		if(node instanceof IASTNamespaceDefinition){
			String name = "Namespace "; //$NON-NLS-1$
			log(message + name);
			return;
		}		
		log(message + node.toString());
		return;
		
	}
	private void logLookups(LookupKind[] kinds){
		if (! CUIPlugin.getDefault().isDebugging() && Util.isActive(IDebugLogConstants.CONTENTASSIST))
			return;
		
		StringBuffer kindName = new StringBuffer("Looking For "); //$NON-NLS-1$
		for(int i = 0; i<kinds.length; i++){
			LookupKind kind = kinds[i];
			if(kind == IASTNode.LookupKind.ALL)
				kindName.append("ALL"); //$NON-NLS-1$
			else if(kind == IASTNode.LookupKind.STRUCTURES)				
				kindName.append("STRUCTURES"); //$NON-NLS-1$
			else if(kind == IASTNode.LookupKind.STRUCTS)				
				kindName.append("STRUCTS"); //$NON-NLS-1$
			else if(kind == IASTNode.LookupKind.UNIONS)				
				kindName.append("UNIONS"); //$NON-NLS-1$
			else if(kind == IASTNode.LookupKind.CLASSES)				
				kindName.append("CLASSES"); //$NON-NLS-1$
			else if(kind == IASTNode.LookupKind.FUNCTIONS)				
				kindName.append("FUNCTIONS"); //$NON-NLS-1$
			else if(kind == IASTNode.LookupKind.VARIABLES)				
				kindName.append("VARIABLES"); //$NON-NLS-1$
			else if(kind == IASTNode.LookupKind.LOCAL_VARIABLES)				
				kindName.append("LOCAL_VARIABLES"); //$NON-NLS-1$
			else if(kind == IASTNode.LookupKind.MEMBERS)				
				kindName.append("MEMBERS"); //$NON-NLS-1$
			else if(kind == IASTNode.LookupKind.METHODS)				
				kindName.append("METHODS"); //$NON-NLS-1$
			else if(kind == IASTNode.LookupKind.FIELDS)				
				kindName.append("FIELDS"); //$NON-NLS-1$
			else if(kind == IASTNode.LookupKind.CONSTRUCTORS)				
				kindName.append("CONSTRUCTORS"); //$NON-NLS-1$
			else if(kind == IASTNode.LookupKind.NAMESPACES)				
				kindName.append("NAMESPACES");  //$NON-NLS-1$
			else if(kind == IASTNode.LookupKind.ENUMERATIONS)				
				kindName.append("ENUMERATIONS");  //$NON-NLS-1$
			else if(kind == IASTNode.LookupKind.ENUMERATORS)				
				kindName.append("ENUMERATORS"); //$NON-NLS-1$
			else if(kind == IASTNode.LookupKind.THIS)				
				kindName.append("THIS"); //$NON-NLS-1$
			else if(kind == IASTNode.LookupKind.TYPEDEFS)				
				kindName.append("TYPEDEFS"); //$NON-NLS-1$

			kindName.append(", "); //$NON-NLS-1$
		}
		log (kindName.toString());
	}
	private void log(String message){
		if (! CUIPlugin.getDefault().isDebugging() && Util.isActive(IDebugLogConstants.CONTENTASSIST))
			return;
		Util.debugLog(message, IDebugLogConstants.CONTENTASSIST);
	}
}
