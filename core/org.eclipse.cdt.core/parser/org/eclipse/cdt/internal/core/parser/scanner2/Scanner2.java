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
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.internal.core.parser.ast.EmptyIterator;
import org.eclipse.cdt.internal.core.parser.token.ImagedExpansionToken;
import org.eclipse.cdt.internal.core.parser.token.ImagedToken;
import org.eclipse.cdt.internal.core.parser.token.SimpleExpansionToken;
import org.eclipse.cdt.internal.core.parser.token.SimpleToken;

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
        this.callbackManager = new ScannerCallbackManager(requestor);
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

    // callbacks
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
    protected Object createInclusionConstruct(char[] fileName,
            char[] filenamePath, boolean local, int startOffset,
            int startingLineNumber, int nameOffset, int nameEndOffset,
            int nameLine, int endOffset, int endLine, boolean isForced) {
        return getASTFactory().createInclusion(fileName, filenamePath, local,
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
            int textEnd, int endingLine,
            org.eclipse.cdt.core.parser.IMacro macro) {
        callbackManager.pushCallback(getASTFactory().createMacro(name,
                startingOffset, startingLineNumber, idstart, idend, nameLine,
                textEnd, endingLine, getCurrentFilename(), !isInitialized));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#pushInclusion(java.lang.Object)
     */
    protected void pushInclusion(Object data) {
        callbackManager.pushCallback(data);
        super.pushInclusion(data);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#popInclusion()
     */
    protected void popInclusion(java.lang.Object data) {
        super.popInclusion(data);
        callbackManager
                .pushCallback(((InclusionData) bufferData[bufferStackPos]).inclusion);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#beforeSecondFetchToken()
     */
    protected void beforeSecondFetchToken() {
        if (callbackManager.hasCallbacks())
            callbackManager.popCallbacks();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#pushProblem(org.eclipse.cdt.core.parser.IProblem)
     */
    protected void pushProblem(IProblem p) {
        callbackManager.pushCallback(p);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#quickParsePushPopInclusion(java.lang.Object)
     */
    protected void quickParsePushPopInclusion(Object inclusion) {
        callbackManager.pushCallback(new InclusionData(null, inclusion));
        callbackManager.pushCallback(inclusion);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#createReaderDuple(java.lang.String)
     */
    protected CodeReader createReaderDuple(String finalPath) {
        return ScannerUtility.createReaderDuple(finalPath, requestor,
                getWorkingCopies());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.parser.IScanner#getLocationResolver()
     */
    public ILocationResolver getLocationResolver() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getWorkingCopies()
     */
    protected Iterator getWorkingCopies() {
        if (workingCopies == null)
            return EmptyIterator.EMPTY_ITERATOR;
        return workingCopies.iterator();
    }

    /**
     * @return
     */
    protected IToken newToken(int signal) {
        if (bufferData[bufferStackPos] instanceof MacroData) {
            int mostRelevant;
            for (mostRelevant = bufferStackPos; mostRelevant >= 0; --mostRelevant)
                if (bufferData[mostRelevant] instanceof InclusionData
                        || bufferData[mostRelevant] instanceof CodeReader)
                    break;
            MacroData data = (MacroData) bufferData[mostRelevant + 1];
            return new SimpleExpansionToken(signal, data.startOffset,
                    data.endOffset - data.startOffset + 1,
                    getCurrentFilename(),
                    getLineNumber(bufferPos[mostRelevant] + 1));
        }
        return new SimpleToken(signal, bufferPos[bufferStackPos] + 1,
                getCurrentFilename(),
                getLineNumber(bufferPos[bufferStackPos] + 1));
    }

    protected IToken newToken(int signal, char[] buffer) {
        if (bufferData[bufferStackPos] instanceof MacroData) {
            int mostRelevant;
            for (mostRelevant = bufferStackPos; mostRelevant >= 0; --mostRelevant)
                if (bufferData[mostRelevant] instanceof InclusionData
                        || bufferData[mostRelevant] instanceof CodeReader)
                    break;
            MacroData data = (MacroData) bufferData[mostRelevant + 1];
            return new ImagedExpansionToken(signal, buffer, data.startOffset,
                    data.endOffset - data.startOffset + 1,
                    getCurrentFilename(),
                    getLineNumber(bufferPos[mostRelevant] + 1));
        }
        IToken i = new ImagedToken(signal, buffer,
                bufferPos[bufferStackPos] + 1, getCurrentFilename(),
                getLineNumber(bufferPos[bufferStackPos] + 1));
        if (buffer != null && buffer.length == 0 && signal != IToken.tSTRING
                && signal != IToken.tLSTRING)
            bufferPos[bufferStackPos] += 1; // TODO - remove this hack at some
                                            // point

        return i;
    }

    protected static final ScannerProblemFactory spf = new ScannerProblemFactory();
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#handleProblem(int,
     *      int, char[])
     */
    protected void handleProblem(int id, int startOffset, char[] arg) {
        if (parserMode == ParserMode.COMPLETION_PARSE)
            return;
        IProblem p = spf.createProblem(id, startOffset,
                bufferPos[bufferStackPos],
                getLineNumber(bufferPos[bufferStackPos]), getCurrentFilename(),
                arg != null ? arg : EMPTY_CHAR_ARRAY, false, true);
        pushProblem(p);
    }

}