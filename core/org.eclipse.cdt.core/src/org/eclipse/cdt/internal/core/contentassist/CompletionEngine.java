/*
 * Created on Nov 19, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.cdt.internal.core.contentassist;

import java.io.CharArrayReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
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
import org.eclipse.cdt.internal.core.model.IWorkingCopy;
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
	
protected IASTCompletionNode parse(IWorkingCopy sourceUnit, int completionOffset){
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

public List complete(IWorkingCopy sourceUnit, int completionOffset) {
	
	// 1- Parse the translation unit
	IASTCompletionNode completionNode = parse(sourceUnit, completionOffset);
	
	if (completionNode == null)
		return null;

	List completions = new ArrayList();	
	// 2- Check the return value 
	if(completionNode.getCompletionKind() == IASTCompletionNode.CompletionKind.DOT_MEMBER){
		// CompletionOnDotMember
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

	return completions;
	
}

}