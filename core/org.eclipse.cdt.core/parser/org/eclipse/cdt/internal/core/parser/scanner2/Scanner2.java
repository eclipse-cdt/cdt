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
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
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
            IScannerExtensionConfiguration configuration) {
        super(reader, info, parserMode, language, log, configuration);
        this.requestor = requestor;
        this.callbackManager = new ScannerCallbackManager(requestor);
        this.expressionEvaluator = new ExpressionEvaluator(callbackManager, spf);
        this.workingCopies = workingCopies;
        postConstructorSetup(reader, info);
        if (reader.filename != null)
            fileCache.put(reader.filename, reader);

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
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#pushContext(char[],
     *      java.lang.Object)
     */
    protected void pushContext(char[] buffer, Object data) {
        super.pushContext(buffer, data);
        if (data instanceof InclusionData) {
            callbackManager.pushCallback(data);
            if (log.isTracing()) {
                StringBuffer b = new StringBuffer("Entering inclusion "); //$NON-NLS-1$
                b.append(((InclusionData) data).reader.filename);
                log.traceLog(b.toString());
            }
        }
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

    protected final CharArrayObjectMap fileCache = new CharArrayObjectMap(100);

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#handleProblem(int,
     *      int, char[])
     */
    protected void handleProblem(int id, int offset, char[] arg) {
        if (parserMode == ParserMode.COMPLETION_PARSE)
            return;
        IProblem p = spf.createProblem(id, offset, bufferPos[bufferStackPos],
                getLineNumber(bufferPos[bufferStackPos]), getCurrentFilename(),
                arg != null ? arg : EMPTY_CHAR_ARRAY, false, true);
        callbackManager.pushCallback(p);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#popContext()
     */
    protected Object popContext() {
        if (bufferData[bufferStackPos] instanceof InclusionData) {
            if (log.isTracing()) {
                StringBuffer buffer = new StringBuffer("Exiting inclusion "); //$NON-NLS-1$
                buffer
                        .append(((InclusionData) bufferData[bufferStackPos]).reader.filename);
                log.traceLog(buffer.toString());
            }
            callbackManager
                    .pushCallback(((InclusionData) bufferData[bufferStackPos]).inclusion);
        }
        return super.popContext();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#processIfdef(int,
     *      int, boolean, boolean)
     */
    protected void processIfdef(int startPos, int endPos, boolean positive,
            boolean taken) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#processIf(int,
     *      int, boolean)
     */
    protected void processIf(int startPos, int endPos, boolean taken) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#processElsif(int,
     *      int, boolean)
     */
    protected void processElsif(int startPos, int endPos, boolean taken) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#processElse(int,
     *      int, boolean)
     */
    protected void processElse(int startPos, int endPos, boolean taken) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#processUndef(int,
     *      int)
     */
    protected void processUndef(int pos, int endPos, char[] symbol, int namePos, Object definition) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#processError(int,
     *      int)
     */
    protected void processError(int startPos, int endPos) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#processEndif(int,
     *      int)
     */
    protected void processEndif(int pos, int i) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#processPragma(int,
     *      int)
     */
    protected void processPragma(int startPos, int endPos) {
    }
    
    protected CodeReader createReader(String path, String fileName){
        String finalPath = ScannerUtility.createReconciledPath(path, fileName);
        char[] finalPathc = finalPath.toCharArray();
        CodeReader reader = (CodeReader) fileCache.get(finalPathc);
        if (reader != null)
            return reader; // found the file in the cache

        // create a new reader on this file (if the file does not exist we will
        // get null)
        reader = createReaderDuple(finalPath);
        if (reader == null)
            return null; // the file was not found

        if (reader.filename != null)
            // put the full requested path in the cache -- it is more likely
            // to match next time than the reader.filename
            fileCache.put(finalPathc, reader);
        return reader;
    }


}