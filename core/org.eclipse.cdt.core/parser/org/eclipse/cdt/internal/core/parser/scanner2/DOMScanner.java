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

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IMacro;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.internal.core.parser.token2.ImagedToken2;
import org.eclipse.cdt.internal.core.parser.token2.SimpleToken2;

/**
 * @author jcamelon
 */
public class DOMScanner extends BaseScanner {

    protected final ICodeReaderFactory codeReaderFactory;

    protected int[] bufferDelta = new int[bufferInitialSize];

    private static class DOMInclusion {
        public final char[] pt;

        public final int o;

        /**
         *  
         */
        public DOMInclusion(char[] path, int offset) {
            this.pt = path;
            this.o = offset;
        }
    }

    /**
     * @param reader
     * @param info
     * @param parserMode
     * @param language
     * @param log
     * @param readerFactory
     *            TODO
     * @param requestor
     */
    public DOMScanner(CodeReader reader, IScannerInfo info,
            ParserMode parserMode, ParserLanguage language,
            IParserLogService log,
            IScannerExtensionConfiguration configuration,
            ICodeReaderFactory readerFactory) {
        super(reader, info, parserMode, language, log, configuration);
        this.expressionEvaluator = new ExpressionEvaluator(null, null);
        this.codeReaderFactory = readerFactory;
        postConstructorSetup(reader, info);
    }

    private void registerMacros() {
        for( int i = 0; i < definitions.size(); ++i )
        {
            IMacro m = (IMacro) definitions.get( definitions.keyAt(i) );
            
            if( m instanceof DynamicStyleMacro )
            {
                DynamicStyleMacro macro = (DynamicStyleMacro) m;
                macro.attachment = locationMap.registerBuiltinDynamicStyleMacro( macro );
            }
            else if( m instanceof DynamicFunctionStyleMacro )
            {
                DynamicFunctionStyleMacro macro = (DynamicFunctionStyleMacro) m;
                macro.attachment = locationMap.registerBuiltinDynamicFunctionStyleMacro( macro );
            }
            else if( m instanceof FunctionStyleMacro )
            {
                FunctionStyleMacro macro = (FunctionStyleMacro) m;
                macro.attachment = locationMap.registerBuiltinFunctionStyleMacro( macro );
            }
            else if( m instanceof ObjectStyleMacro )
            {
                ObjectStyleMacro macro = (ObjectStyleMacro) m;
                macro.attachment = locationMap.registerBuiltinObjectStyleMacro( macro );
            }
            
        }
        
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.parser.IScanner#getLocationResolver()
     */
    public ILocationResolver getLocationResolver() {
        if (locationMap instanceof ILocationResolver)
            return (ILocationResolver) locationMap;
        return null;
    }

    final IScannerPreprocessorLog locationMap = new LocationMap();

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.parser.IScanner#setASTFactory(org.eclipse.cdt.core.parser.ast.IASTFactory)
     */
    public void setASTFactory(IASTFactory f) {
        // do nothing
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
        return new DOMInclusion(filenamePath, resolveOffset(startOffset));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#processMacro(char[],
     *      int, int, int, int, int, int, int)
     */
    protected void processMacro(char[] name, int startingOffset,
            int startingLineNumber, int idstart, int idend, int nameLine,
            int textEnd, int endingLine, IMacro macro) {
        IScannerPreprocessorLog.IMacroDefinition m = null;
        if (macro instanceof FunctionStyleMacro)
            m = locationMap.defineFunctionStyleMacro(
                    (FunctionStyleMacro) macro, resolveOffset(startingOffset),
                    resolveOffset(idstart), resolveOffset(idend),
                    resolveOffset(textEnd));
        else if (macro instanceof ObjectStyleMacro)
            m = locationMap.defineObjectStyleMacro((ObjectStyleMacro) macro,
                    resolveOffset(startingOffset), resolveOffset(idstart),
                    resolveOffset(idend), resolveOffset(textEnd));
        if (m != null && macro instanceof ObjectStyleMacro)
            ((ObjectStyleMacro) macro).attachment = m;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#createReaderDuple(java.lang.String)
     */
    protected CodeReader createReaderDuple(String finalPath) {
        return codeReaderFactory.createCodeReaderForInclusion(finalPath);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#pushContext(char[],
     *      java.lang.Object)
     */
    protected void pushContext(char[] buffer, Object data) {
        if (bufferStackPos + 1 == bufferDelta.length) {
            int size = bufferDelta.length * 2;
            int[] oldBufferDelta = bufferDelta;
            bufferDelta = new int[size];
            System.arraycopy(oldBufferDelta, 0, bufferDelta, 0,
                    oldBufferDelta.length);
        }

        if (data instanceof InclusionData) {

            if (log.isTracing()) {
                StringBuffer b = new StringBuffer("Entering inclusion "); //$NON-NLS-1$
                b.append(((InclusionData) data).reader.filename);
                log.traceLog(b.toString());
            }
            if( ! isCircularInclusion( (InclusionData) data ))
            {
                DOMInclusion inc = ((DOMInclusion) ((InclusionData) data).inclusion);
                locationMap.startInclusion(((InclusionData) data).reader, inc.o,
                        resolveOffset(getCurrentOffset()));
                bufferDelta[bufferStackPos + 1] = 0;
            }
        }

        else if (data instanceof MacroData) {
            MacroData d = (MacroData) data;
            if (d.macro instanceof FunctionStyleMacro && fsmCount == 0) {
                FunctionStyleMacro fsm = (FunctionStyleMacro) d.macro;
                locationMap.startFunctionStyleExpansion(fsm.attachment,
                        fsm.arglist, resolveOffset(d.startOffset),
                        resolveOffset(d.endOffset));
                bufferDelta[bufferStackPos + 1] = 0;
            } else if (d.macro instanceof ObjectStyleMacro && fsmCount == 0) {
                ObjectStyleMacro osm = (ObjectStyleMacro) d.macro;
                locationMap.startObjectStyleMacroExpansion(osm.attachment,
                        resolveOffset(d.startOffset),
                        resolveOffset(d.endOffset));
                bufferDelta[bufferStackPos + 1] = 0;
            }
        }

        super.pushContext(buffer, data);
    }

    protected int fsmCount = 0;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#popContext()
     */
    protected Object popContext() {
        // TODO calibrate offsets
        Object result = super.popContext();
        if (result instanceof CodeReader) {
            locationMap.endTranslationUnit(bufferDelta[0]
                    + ((CodeReader) result).buffer.length);
        } else if (result instanceof InclusionData) {
            CodeReader codeReader = ((InclusionData) result).reader;
            if (log.isTracing()) {
                StringBuffer buffer = new StringBuffer("Exiting inclusion "); //$NON-NLS-1$
                buffer.append(codeReader.filename);
                log.traceLog(buffer.toString());
            }

            locationMap.endInclusion(getGlobalCounter(bufferStackPos + 1)
                    + bufferPos[bufferStackPos + 1]);
            bufferDelta[bufferStackPos] += bufferDelta[bufferStackPos + 1]
                    + codeReader.buffer.length;
        } else if (result instanceof MacroData) {
            MacroData data = (MacroData) result;
            if (data.macro instanceof FunctionStyleMacro && fsmCount == 0) {

                locationMap
                        .endFunctionStyleExpansion(getGlobalCounter(bufferStackPos + 1)
                                + bufferPos[bufferStackPos + 1] + 1); // functionstyle
                // macro)
                // ;
                bufferDelta[bufferStackPos] += bufferDelta[bufferStackPos + 1]
                        + bufferPos[bufferStackPos + 1] + 1;
            } else if (data.macro instanceof ObjectStyleMacro && fsmCount == 0) {
                locationMap
                        .endObjectStyleMacroExpansion(getGlobalCounter(bufferStackPos + 1)
                                + bufferPos[bufferStackPos + 1]);
                bufferDelta[bufferStackPos] += bufferDelta[bufferStackPos + 1]
                        + bufferPos[bufferStackPos + 1];

            }
        }
        return result;
    }

    protected int getGlobalCounter(int value) {
        if (value < 0)
            return 0;
        int result = bufferDelta[value];
        for (int i = value - 1; i >= 0; --i)
            result += bufferPos[i] + bufferDelta[i];

        return result;

    }

    protected int getGlobalCounter() {
        return getGlobalCounter(bufferStackPos);
    }

    /**
     * @return
     */
    protected IToken newToken(int signal) {
        return new SimpleToken2(signal,
                resolveOffset(bufferPos[bufferStackPos] + 1),
                getLineNumber(bufferPos[bufferStackPos] + 1));
    }

    protected IToken newToken(int signal, char[] buffer) {
        IToken i = new ImagedToken2(signal, buffer,
                resolveOffset(bufferPos[bufferStackPos] + 1), 
                getLineNumber(bufferPos[bufferStackPos] + 1));
        if (buffer != null && buffer.length == 0 && signal != IToken.tSTRING
                && signal != IToken.tLSTRING)
            bufferPos[bufferStackPos] += 1; // TODO - remove this hack at some
        // point

        return i;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#quickParsePushPopInclusion(java.lang.Object)
     */
    protected void quickParsePushPopInclusion(Object inclusion) {
        // do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#handleProblem(int,
     *      int, char[])
     */
    protected void handleProblem(int id, int offset, char[] arg) {
        IASTProblem problem = new ScannerASTProblem(id, arg, true, false);
        int o = resolveOffset(offset);
        ((ScannerASTProblem) problem).setOffsetAndLength(o,
                resolveOffset(getCurrentOffset() + 1) - o);
        locationMap.encounterProblem(problem);
    }

    /**
     * @param offset
     * @return
     */
    private int resolveOffset(int offset) {
        return getGlobalCounter() + offset;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#postConstructorSetup(org.eclipse.cdt.core.parser.CodeReader,
     *      org.eclipse.cdt.core.parser.IScannerInfo)
     */
    protected void postConstructorSetup(CodeReader reader, IScannerInfo info) {
        super.postConstructorSetup(reader, info);
        locationMap.startTranslationUnit(getMainReader());
        registerMacros();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#processIfdef(int,
     *      int, boolean, boolean)
     */
    protected void processIfdef(int startPos, int endPos, boolean positive,
            boolean taken) {
        if (positive)
            locationMap.encounterPoundIfdef(resolveOffset(startPos),
                    resolveOffset(endPos), taken);
        else
            locationMap.encounterPoundIfndef(resolveOffset(startPos),
                    resolveOffset(endPos), taken);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#processIf(int,
     *      int, boolean)
     */
    protected void processIf(int startPos, int endPos, boolean taken) {
        locationMap.encounterPoundIf(resolveOffset(startPos),
                resolveOffset(endPos), taken);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#processElsif(int,
     *      int, boolean)
     */
    protected void processElsif(int startPos, int endPos, boolean taken) {
        locationMap.encounterPoundElif(resolveOffset(startPos),
                resolveOffset(endPos), taken);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#processElse(int,
     *      int, boolean)
     */
    protected void processElse(int startPos, int endPos, boolean taken) {
        locationMap.encounterPoundElse(resolveOffset(startPos),
                resolveOffset(endPos), taken);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#processUndef(int,
     *      int)
     */
    protected void processUndef(int pos, int endPos, char[] symbol,
            int namePos, Object definition) {
        final IScannerPreprocessorLog.IMacroDefinition macroDefinition = (definition instanceof ObjectStyleMacro) ? ((ObjectStyleMacro) definition).attachment
                : null;
        locationMap.encounterPoundUndef(resolveOffset(pos),
                resolveOffset(endPos), symbol, namePos, macroDefinition);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#processError(int,
     *      int)
     */
    protected void processError(int startPos, int endPos) {
        locationMap.encounterPoundError(resolveOffset(startPos),
                resolveOffset(endPos));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#processEndif(int,
     *      int)
     */
    protected void processEndif(int startPos, int endPos) {
        locationMap.encounterPoundEndIf(resolveOffset(startPos),
                resolveOffset(endPos));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#processPragma(int,
     *      int)
     */
    protected void processPragma(int startPos, int endPos) {
        locationMap.encounterPoundPragma(resolveOffset(startPos),
                resolveOffset(endPos));
    }

    protected void beforeReplaceAllMacros() {
        ++fsmCount;
    }
    
    protected void afterReplaceAllMacros() {
        --fsmCount;
    }
    
    protected CodeReader createReader(String path, String fileName){
        String finalPath = ScannerUtility.createReconciledPath(path, fileName);
        CodeReader reader = createReaderDuple(finalPath);
        return reader;
    }

}