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
package org.eclipse.cdt.internal.core.contentassist;

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
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTNode.LookupResult;
import org.eclipse.cdt.internal.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.parser.util.ASTUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * @author hamer
 *
 * This class is the entry point for code completions.
 * It contains a public API used to call ContentAssist on a given working copy 
 * and a given completion offset.
 * 
 */
public class CompletionEngine {
	List completions = new ArrayList();
	ICompletionRequestor requestor;
	int completionStart = 0;
	int completionLength = 0;
	public static final int LOCAL_VARIABLE_TYPE_RELEVANCE = 12;
	public static final int FIELD_TYPE_RELEVANCE = 11;
	public static final int VARIABLE_TYPE_RELEVANCE = 10;
	public static final int METHOD_TYPE_RELEVANCE = 9;
	public static final int FUNCTION_TYPE_RELEVANCE = 8;
	public static final int CLASS_TYPE_RELEVANCE = 7;
	public static final int STRUCT_TYPE_RELEVANCE = 6;
	public static final int UNION_TYPE_RELEVANCE = 5;
	public static final int NAMESPACE_TYPE_RELEVANCE = 4;
	public static final int MACRO_TYPE_RELEVANCE = 3;
	public static final int ENUMERATION_TYPE_RELEVANCE = 2;
	public static final int ENUMERATOR_TYPE_RELEVANCE = 1;
	public static final int DEFAULT_TYPE_RELEVANCE = 0;
	
	
	public CompletionEngine(ICompletionRequestor completionRequestor){
			completions.clear();
			requestor = completionRequestor;
	}
	public int computeTypeRelevance(int type){
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
			IASTCompletionNode result = parser.parse(completionOffset);
			return result;
		} else {
			return null;
		}	 	
	}

	private void addToCompletions (LookupResult result){
		Iterator nodes = result.getNodes();
		while (nodes.hasNext()){
			IASTNode node = (IASTNode) nodes.next();
			if(node instanceof IASTField){
				int relevance = computeTypeRelevance(ICElement.C_FIELD);
				IASTField field = (IASTField)node;
				requestor.acceptField(field.getName(), 
				ASTUtil.getType(field.getAbstractDeclaration()),
				field.getVisiblity(), completionStart, completionLength, relevance);	
			}
		}
		return ;
	}
	
	private void completionOnDotMember(IASTCompletionNode completionNode){
		// Completing after a dot
		// 1. Get the search scope node
		IASTNode searchNode = completionNode.getCompletionContext();
		// 2. lookup fields & add to completion proposals
		LookupResult result = searchNode.lookup (completionNode.getCompletionPrefix(), IASTNode.LookupKind.FIELDS);
		addToCompletions (result);
		// 3. looup methods & add to completion proposals
		result = searchNode.lookup (completionNode.getCompletionPrefix(), IASTNode.LookupKind.METHODS);
		addToCompletions (result);
		// 4. lookup nested structures & add to completion proposals
		result = searchNode.lookup (completionNode.getCompletionPrefix(), IASTNode.LookupKind.METHODS);
		addToCompletions (result);		
	}
	
	public void complete(IWorkingCopy sourceUnit, int completionOffset, List completionList) {
		
		// 1- Parse the translation unit
		IASTCompletionNode completionNode = parse(sourceUnit, completionOffset);
		
		if (completionNode == null)
			return;
		
		// set the completionStart and the completionLength
		completionStart = completionOffset;
		completionLength = completionNode.getCompletionPrefix().length();
		
		// 2- Check the return value 
		if(completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.DOT_MEMBER){
			completionOnDotMember(completionNode);
		}
		else if(completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.ARROW_MEMBER){
			// CompletionOnArrowMember
		}
		else if(completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.QUALIFIEDNAME_MEMBER){
			// CompletionOnQualifiedNameMember
		}
		else if(completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.FIELD_TYPE){
			// CompletionOnFieldType
		}
		else if(completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.VARIABLE_TYPE){
				// CompletionOnVariableType
		}
		else if(completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.ARGUMENT_TYPE){
				// CompletionOnArgumentType
		}
		else if(completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.METHOD_RETURN_TYPE){
				// CompletionOnMethodReturnType
		}
		else if(completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.FUNCTIOND_RETURN_TYPE){
				// CompletionOnFunctionReturnType
		}
		else if(completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.SINGLE_NAME_REFERENCE){
				// CompletionOnSingleNameReference
		}
		else if(completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.QUALIFIED_NAME_REFERENCE){
				// CompletionOnQualifiedNameReference
		}
		else if(completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.STRUCTURE_REFERENCE){
				// CompletionOnStructureReference
		}
		else if(completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.CLASS_REFERENCE){
				// CompletionOnClassReference
		}
		else if(completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.EXCEPTION_REFERENCE){
				// CompletionOnExceptionReference
		}
		else if(completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.MACRO_REFERENCE){
				// CompletionOnMacroReference
		}
		else if(completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.MESSAGE_SEND){
				// CompletionOnMessageSend
		}
		else if(completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.QUALIFIED_ALLOCATION_EXPRESSION){
				// CompletionOnQualifiedAllocationExpression
		}
		else if(completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.KEYWORD){
				// CompletionOnKeyword
		}
	
		completionList.addAll(completions);
		return;
			
	}
	
}