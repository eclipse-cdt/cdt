/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner2;

import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.internal.core.parser.ast.EmptyIterator;

/**
 * @author jcamelon
 */
public class Scanner2 extends BaseScanner {

    /**
     * @param reader
     * @param info
     * @param requestor
     * @param parserMode
     * @param language
     * @param log
     * @param workingCopies
     * @param configuration
     */
    public Scanner2(CodeReader reader, IScannerInfo info,
            ISourceElementRequestor requestor, ParserMode parserMode,
            ParserLanguage language, IParserLogService log, List workingCopies,
            IScannerConfiguration configuration) {
        super(reader, info, parserMode, language, log, configuration);
        this.requestor = requestor;
        this.callbackManager = new ScannerCallbackManager( requestor );
        this.expressionEvaluator = new ExpressionEvaluator(callbackManager, spf);
        this.workingCopies = workingCopies;
        postConstructorSetup(reader, info);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getASTFactory()
     */
    protected IASTFactory getASTFactory() {
        if (astFactory == null)
            astFactory = ParserFactory.createASTFactory(parserMode, language);
        return astFactory;
    }

    protected IASTFactory astFactory;
	//callbacks
	protected ScannerCallbackManager callbackManager;
	protected ISourceElementRequestor requestor;
	protected List workingCopies;
	
    public final void setASTFactory(IASTFactory f) {
        astFactory = f;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#createInclusionConstruct(char[],
     *      char[], boolean, int, int, int, int, int, int, int, boolean)
     */
    protected Object createInclusionConstruct(char[] fileNameArray,
            char[] filename, boolean local, int startOffset,
            int startingLineNumber, int nameOffset, int nameEndOffset,
            int nameLine, int endOffset, int endLine, boolean isForced) {
        return getASTFactory().createInclusion(fileNameArray, filename, local,
                startOffset, startingLineNumber, nameOffset, nameEndOffset,
                nameLine, endOffset, endLine, getCurrentFilename(), isForced);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#processMacro(char[],
     *      int, int, int, int, int, int, int)
     */
    protected void processMacro(char[] name, int startingOffset,
            int startingLineNumber, int idstart, int idend, int nameLine,
            int textEnd, int endingLine) {
        callbackManager.pushCallback(getASTFactory().createMacro(name,
                startingOffset, startingLineNumber, idstart, idend, nameLine,
                textEnd, endingLine, getCurrentFilename(), !isInitialized));
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#pushInclusion(java.lang.Object)
     */
    protected void pushInclusion(Object data) {
        callbackManager.pushCallback( data );
        super.pushInclusion(data);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#popInclusion()
     */
    protected void popInclusion() {
        super.popInclusion();
        callbackManager.pushCallback( ((InclusionData) bufferData[bufferStackPos]).inclusion );
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#beforeSecondFetchToken()
     */
    protected void beforeSecondFetchToken() {
        if( callbackManager.hasCallbacks() )
		    callbackManager.popCallbacks();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#pushProblem(org.eclipse.cdt.core.parser.IProblem)
     */
    protected void pushProblem(IProblem p) {
        callbackManager.pushCallback( p );
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#quickParsePushPopInclusion(java.lang.Object)
     */
    protected void quickParsePushPopInclusion(Object inclusion) {
        callbackManager.pushCallback( new InclusionData( null, inclusion ) );
        callbackManager.pushCallback( inclusion );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#createReaderDuple(java.lang.String)
     */
    protected CodeReader createReaderDuple(String finalPath) {
        return ScannerUtility.createReaderDuple( finalPath, requestor, getWorkingCopies() );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.IScanner#getLocationResolver()
     */
    public ILocationResolver getLocationResolver() {
        return null;
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getWorkingCopies()
	 */
	protected Iterator getWorkingCopies() {
		if( workingCopies == null ) return EmptyIterator.EMPTY_ITERATOR;
		return workingCopies.iterator();
	}

}