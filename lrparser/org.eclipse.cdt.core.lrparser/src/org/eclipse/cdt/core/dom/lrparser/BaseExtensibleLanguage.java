/*******************************************************************************
 *  Copyright (c) 2006, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lpg.lpgjavaruntime.IToken;
import lpg.lpgjavaruntime.PrsStream;
import lpg.lpgjavaruntime.Token;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.dom.lrparser.action.ITokenStream;
import org.eclipse.cdt.core.dom.parser.CLanguageKeywords;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.AbstractLanguage;
import org.eclipse.cdt.core.model.ICLanguageKeywords;
import org.eclipse.cdt.core.model.IContributedModelBuilder;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.util.ASTPrinter;
import org.eclipse.cdt.core.parser.util.DebugUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.parser.scanner.CPreprocessor;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.cpp.PDOMCPPLinkageFactory;
import org.eclipse.core.runtime.CoreException;


/**
 * Implementation of the ILanguage extension point, 
 * provides the ability to add LPG based languages to CDT.
 */
@SuppressWarnings({ "restriction", "nls" })
public abstract class BaseExtensibleLanguage extends AbstractLanguage {
			
	
	private static final boolean DEBUG_PRINT_GCC_AST = false;
	private static final boolean DEBUG_PRINT_AST     = false;
	// default 0.5 min
	private long parser_timeout_limit_lowerBoundary = 1 * 30 * 1000;
	// default 2 mins
	private long parser_timeout_limit_uppperBoundary = 1 * 60 * 1000;
	//time limit for each token, 1ms
	public static long UNIT_PARSER_TIMEOUT_LIMIT = 10;
	
	private static long LONGEST_CORE_RUNTIME;
	private static long LONGEST_LPR_RUNTIME;
	
	
	public static boolean CATCH_TEMPLATEID_ERROR = false;

	private ICLanguageKeywords keywords = null;
	
	/**
	 * Retrieve the parser (runs after the preprocessor runs).
	 * 
	 * Can be overridden in subclasses to provide a different parser
	 * for a language extension.
	 */
	protected abstract IParser<IASTTranslationUnit> getParser(IScanner scanner, IIndex index, Map<String,String> properties);
	
	protected IParser<IASTTranslationUnit> getCompleteParser(IScanner scanner, IIndex index, Map<String,String> properties){
		return getParser(scanner, index, properties);
	}
	protected ISecondaryParser<IASTTranslationUnit> getCompleteParser(ITokenStream stream, IScanner scanner, IIndex index, Map<String,String> properties){
		return (ISecondaryParser)getParser(scanner, index, properties);
	}
	
	/**
	 * Returns the ParserLanguage value that is to be used when creating
	 * an instance of CPreprocessor.
	 * 
	 */
	protected abstract ParserLanguage getParserLanguage();
	
	
	/**
	 * Returns the scanner extension configuration for this language, may not return null
	 */
	protected abstract IScannerExtensionConfiguration getScannerExtensionConfiguration();
	
	private class ParseThread<AST_TYPE> extends Thread{
		ParseThread(){
			super();
			super.setName("ParserThread");
		}
		AST_TYPE astUnit = null;
		AST_TYPE getASTUnit(){
			return astUnit;
		}
	}
	
	private <AST_TYPE> AST_TYPE runThreadByLimitedTime(long limitTime, ParseThread<AST_TYPE> parseThread) throws InterruptedException{
		parseThread.start();
		
		parseThread.join(limitTime);
	
		return parseThread.getASTUnit();
	}
	
	
	@Override @Deprecated
	public IASTTranslationUnit getASTTranslationUnit(org.eclipse.cdt.core.parser.CodeReader reader,
			IScannerInfo scanInfo, ICodeReaderFactory codeReaderFactory, IIndex index, int options,
			IParserLogService log) throws CoreException {
		return getASTTranslationUnit(FileContent.adapt(reader), scanInfo, IncludeFileContentProvider
				.adapt(codeReaderFactory), index, options, log);
	}
		
	public void setParser_timeout_limit_lowerBoundary(
			long parser_timeout_limit_lowerBoundary) {
		this.parser_timeout_limit_lowerBoundary = parser_timeout_limit_lowerBoundary;
	}

	public void setParser_timeout_limit_uppperBoundary(
			long parser_timeout_limit_uppperBoundary) {
		this.parser_timeout_limit_uppperBoundary = parser_timeout_limit_uppperBoundary;
	}

	@Override
	public IASTTranslationUnit getASTTranslationUnit(final FileContent reader, final IScannerInfo scanInfo,
			final IncludeFileContentProvider fileCreator, final IIndex index, int options, final IParserLogService log)
			throws CoreException {
		CATCH_TEMPLATEID_ERROR = false;
		long startTime=0;
		java.util.Date today=null;
		if(log.isTracing()){
			today = new java.util.Date();
			startTime = today.getTime();
			log.traceLog("^^^^^^ Start parsing " + reader.getFileLocation() + " at " + new java.sql.Timestamp(startTime));
		}
		IASTTranslationUnit gtu = null;
		if(DEBUG_PRINT_GCC_AST) {
			System.out.println("\n********************************************************\nParsing\nOptions: " + options);
			
			ILanguage gppLanguage = getParserLanguage() == ParserLanguage.CPP ? GPPLanguage.getDefault() : GCCLanguage.getDefault();
			gtu = gppLanguage.getASTTranslationUnit(reader, scanInfo, fileCreator, index, options, log);
			
			System.out.println(gppLanguage.getName() + " AST:");
			ASTPrinter.print(gtu);
			System.out.println();
		}

		final IScannerExtensionConfiguration config = getScannerExtensionConfiguration();
		
		final ParserLanguage pl = getParserLanguage();
		final IScanner preprocessor = new CPreprocessor(reader, scanInfo, pl, log, config, fileCreator);
		preprocessor.setComputeImageLocations((options & ILanguage.OPTION_NO_IMAGE_LOCATIONS) == 0);
		
		final Map<String,String> parserProperties = new HashMap<String,String>();
		parserProperties.put(LRParserProperties.TRANSLATION_UNIT_PATH, reader.getFileLocation());
		if((options & OPTION_SKIP_FUNCTION_BODIES) != 0)
			parserProperties.put(LRParserProperties.SKIP_FUNCTION_BODIES, "true");
		if((options & OPTION_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS) != 0)
			parserProperties.put(LRParserProperties.SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS, "true");
		
		final IParser<IASTTranslationUnit> parser = getParser(preprocessor, index, parserProperties);
		long parser_timeout_limit = parser_timeout_limit_uppperBoundary;
		if(parser instanceof PrsStream){
			int token_size = ((PrsStream)parser).getSize();
			parser_timeout_limit = token_size * UNIT_PARSER_TIMEOUT_LIMIT;
			if(parser_timeout_limit < parser_timeout_limit_lowerBoundary)
				parser_timeout_limit = parser_timeout_limit_lowerBoundary;
			if(parser_timeout_limit > parser_timeout_limit_uppperBoundary)
				parser_timeout_limit = parser_timeout_limit_uppperBoundary;
			log.traceLog("^^^^^^ adjusted time out limit with token size: " + token_size + " and the time out limit: " + parser_timeout_limit);
		}
		IASTTranslationUnit tu = null;
		//real token size, substract a dummy token and a eof token
		final int orgTokenSize = ((PrsStream)parser).getTokens().size();
		//final List orginalTokens = copyList(((PrsStream)parser).getTokens());
		ParseThread<IASTTranslationUnit> parseThread = new ParseThread<IASTTranslationUnit>() {
			
			@Override
			public void run() {
				try{
				astUnit = parser.parse();
				}			
				
				catch (Exception e) {
					
					/*
					if(e instanceof TemplateIDErrorException){
						//IScanner completePreprocessor = new CPreprocessor(reader, scanInfo, pl, log, config, fileCreator);
						//IParser<IASTTranslationUnit> completeParser = getCompleteParser(preprocessor, index, parserProperties);
						
						ISecondaryParser<IASTTranslationUnit> completeParser = getCompleteParser((ITokenStream)parser, preprocessor, index, parserProperties);
						//completeParser.setAction(parser.getAction());
						//((ISecondaryParser)completeParser).setTokenMap((ITokenStream)parser);
						//copyTokensToParser((PrsStream)completeParser, ((PrsStream)parser).getTokens().subList(0, orgTokenSize));
						((ISecondaryParser)completeParser).setTokens(((PrsStream)parser).getTokens().subList(1, orgTokenSize-1));
						astUnit = completeParser.parse();
					}else{
					*/
					if(log.isTracing()){
						StringWriter stringW = new StringWriter();
					    PrintWriter printW = new PrintWriter(stringW);
					    e.printStackTrace(printW);
	 
					    log.traceLog("^^^^^^ PARSER_ERR_STACK" + stringW.toString());
					}
					//}
				
				}
				
			}
			
		};
		
		try {
			tu = runThreadByLimitedTime(parser_timeout_limit, parseThread);
		} catch (InterruptedException e) {
			
			StringWriter stringW = new StringWriter();
		    PrintWriter printW = new PrintWriter(stringW);
		    e.printStackTrace(printW);

			
			log.traceLog("^^^^^^_ERR_STACK" + stringW.toString());
			//e.printStackTrace();
		}
		parseThread.stop();
		long lprFinishTime=0;
		long coreFinishTime=0;
		if(log.isTracing()){
			today = new java.util.Date();
			lprFinishTime = today.getTime();
		}
		
		if(tu==null){
			long lpr_fail_time=0;
			if(log.isTracing()){
				lpr_fail_time = lprFinishTime;
				log.traceLog("^^^^^^ LR parser fails in parsing " + reader.getFileLocation() + " after running " + (lpr_fail_time-startTime)/1000 + " seconds");
			}
			
			ILanguage gppLanguage = getParserLanguage() == ParserLanguage.CPP ? GPPLanguage.getDefault() : GCCLanguage.getDefault();
			tu = gppLanguage.getASTTranslationUnit(reader, scanInfo, fileCreator, index, options, log);
			if(log.isTracing()){
				today = new java.util.Date();
				coreFinishTime = today.getTime();
			
				log.traceLog("^^^^^^ core parser parses " + reader.getFileLocation() + " in " + (coreFinishTime - lpr_fail_time)/1000 + " seconds");
			}
		}
		if(tu!=null){
			tu.setIsHeaderUnit((options & OPTION_IS_SOURCE_UNIT) == 0); // the TU is marked as either a source file or a header file
		}
		
		if(DEBUG_PRINT_AST) {
			System.out.println("Base Extensible Language AST:");
			ASTPrinter.print(tu);
		}
		long finishTime ;
		if(log.isTracing()){
			if(coreFinishTime>0){
				//parsed by core parser.
				finishTime = coreFinishTime;
				long core_runtime = finishTime - startTime;
				log.traceLog("^^^^^^ Finish parsing with cdt core parser " + reader.getFileLocation() + " at " + new java.sql.Timestamp(finishTime) + " runtime: " + core_runtime);
				if(core_runtime > LONGEST_CORE_RUNTIME){
					LONGEST_CORE_RUNTIME = core_runtime;
					log.traceLog("^^^^^^ CLCLCLCL so far the longest runtime with core parser is: " + core_runtime/1000);
				}
			}else{
				finishTime = lprFinishTime;
				long lpr_runtime = finishTime - startTime;
				log.traceLog("^^^^^^ Finish parsing " + reader.getFileLocation() + " at " + new java.sql.Timestamp(finishTime) + " runtime: " + lpr_runtime);
				if(lpr_runtime > LONGEST_LPR_RUNTIME){
					LONGEST_LPR_RUNTIME = lpr_runtime;
					log.traceLog("^^^^^^ LLLLLLLL so far the longest runtime by LPR Parser is: " + lpr_runtime/1000);
				}
			}
		}
		
		return tu;
	}
	
	public void copyTokensToParser(PrsStream parser, List<IToken> tokens) {
		parser.resetTokenStream();
		
		for(IToken token : tokens) {
			
			parser.addToken(token);
		}
		
	}
	
	public List copyList(List orgList){
		List returnList = new ArrayList(orgList.size());
		for(int i=0; i<orgList.size(); i++){
			returnList.add(orgList.get(i));
		}
		return returnList;
		
	}
	
	@Deprecated
	public IASTTranslationUnit getASTTranslationUnit(org.eclipse.cdt.core.parser.CodeReader reader,
			IScannerInfo scanInfo, ICodeReaderFactory fileCreator, IIndex index, IParserLogService log)
			throws CoreException {

		return getASTTranslationUnit(reader, scanInfo, fileCreator, index, 0, log);
	}
	
	@Deprecated
	public IASTCompletionNode getCompletionNode(org.eclipse.cdt.core.parser.CodeReader reader,
			IScannerInfo scanInfo, ICodeReaderFactory fileCreator, IIndex index, IParserLogService log,
			int offset) throws CoreException {
		return getCompletionNode(FileContent.adapt(reader), scanInfo, IncludeFileContentProvider
				.adapt(fileCreator), index, log, offset);
	}
		
	@Override
	public IASTCompletionNode getCompletionNode(FileContent reader, IScannerInfo scanInfo,
			IncludeFileContentProvider fileCreator, IIndex index, IParserLogService log, int offset)
			throws CoreException {		
		
		IASTCompletionNode cn;
		if(DEBUG_PRINT_GCC_AST) {
			ILanguage gppLanguage = GCCLanguage.getDefault();
			cn = gppLanguage.getCompletionNode(reader, scanInfo, fileCreator, index, log, offset);
			
			System.out.println();
			System.out.println("********************************************************");
			System.out.println("GPP AST:");
			printCompletionNode(cn);
		}
		
		IScannerExtensionConfiguration config = getScannerExtensionConfiguration();
		
		ParserLanguage pl = getParserLanguage();
		IScanner preprocessor = new CPreprocessor(reader, scanInfo, pl, log, config, fileCreator);
		preprocessor.setContentAssistMode(offset);
		
		
		Map<String,String> parserProperties = new HashMap<String,String>();
		parserProperties.put(LRParserProperties.TRANSLATION_UNIT_PATH, reader.getFileLocation());
		parserProperties.put(LRParserProperties.SKIP_FUNCTION_BODIES, "true");
		parserProperties.put(LRParserProperties.SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS, "true");
		
		final IParser<IASTTranslationUnit> parser = getParser(preprocessor, index, parserProperties);
		
		long parser_timeout_limit = parser_timeout_limit_uppperBoundary;
		if(parser instanceof PrsStream){
			int token_size = ((PrsStream)parser).getSize();
			parser_timeout_limit = token_size * UNIT_PARSER_TIMEOUT_LIMIT;
			if(parser_timeout_limit < parser_timeout_limit_lowerBoundary)
				parser_timeout_limit = parser_timeout_limit_lowerBoundary;
			if(parser_timeout_limit > parser_timeout_limit_uppperBoundary)
				parser_timeout_limit = parser_timeout_limit_uppperBoundary;
			if(log.isTracing()){
				log.traceLog("^^^^^^ adjusted time out limit with token size: " + token_size + " and the time out limit: " + parser_timeout_limit);
			}
		}
		ParseThread<IASTCompletionNode> parseThread = new ParseThread<IASTCompletionNode>() {
			
			@Override
			public void run() {
				parser.parse();
				astUnit = parser.getCompletionNode();
			}
			
		};
		
		IASTCompletionNode completionNode=null;
		try {
			completionNode = runThreadByLimitedTime(parser_timeout_limit*100, parseThread);
		} catch (InterruptedException e) {
			if(log.isTracing()){
				StringWriter stringW = new StringWriter();
			    PrintWriter printW = new PrintWriter(stringW);
			    e.printStackTrace(printW);
	
				
				log.traceLog("^^^^^^_ERR_STACK" + stringW.toString());
			}
			//e.printStackTrace();
		}
		parseThread.stop();
		if(completionNode==null){
			log.traceLog("LR parser fails in parsing " + reader.getFileLocation());
			if(log.isTracing()){
				log.traceLog("LR parser fails in parsing " + reader.getFileLocation());
			}
			ILanguage gppLanguage = getParserLanguage() == ParserLanguage.CPP ? GPPLanguage.getDefault() : GCCLanguage.getDefault();
			completionNode=gppLanguage.getCompletionNode(reader, scanInfo, fileCreator, index, log, offset);
		}
		
		if(DEBUG_PRINT_AST) {
			System.out.println("Base Extensible Language AST:");
			printCompletionNode(completionNode);
		}
		
		return completionNode;
	}
	
	
	/*
	 * For debugging.
	 */
	private static void printCompletionNode(IASTCompletionNode cn) {
		if(cn == null) {
			System.out.println("Completion node is null");
			return;
		}
			
		ASTPrinter.print(cn.getTranslationUnit());
		for(IASTName name : cn.getNames()) {
			ASTNode context = (ASTNode)name.getCompletionContext();
			System.out.printf("Name: %s, Context: %s, At: %d", 
					name, DebugUtil.safeClassName(context), context == null ? null : context.getOffset());
			if(name.getTranslationUnit() == null) // some name nodes are not hooked up to the AST
				System.out.print(", not hooked up");
			System.out.println();
		}
		System.out.println();
	}
	
	@Deprecated
	public IASTName[] getSelectedNames(IASTTranslationUnit ast, int start, int length) {
		return GCCLanguage.getDefault().getSelectedNames(ast, start, length);
	}
	
	private ICLanguageKeywords cLanguageKeywords = new CLanguageKeywords(getParserLanguage(), getScannerExtensionConfiguration());
	
	
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		if(ICLanguageKeywords.class.equals(adapter))
			return cLanguageKeywords;
		if(IPDOMLinkageFactory.class.equals(adapter)) {
			if(getParserLanguage().isCPP())
				return new PDOMCPPLinkageFactory();
			return new PDOMCLinkageFactory();
		}
		
		return super.getAdapter(adapter);
	}
	
	public IContributedModelBuilder createModelBuilder(@SuppressWarnings("unused") ITranslationUnit tu) {
		return null;
	}
	
}
