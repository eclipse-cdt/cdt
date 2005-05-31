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

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IMacro;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

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
                locationMap.startInclusion(((InclusionData) data).reader, inc.o, resolveOffset(getCurrentOffset()));
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
        Object result = super.popContext();
        if (result instanceof CodeReader) {
            if( isInitialized )
                locationMap.endTranslationUnit(bufferDelta[0]
                        + ((CodeReader) result).buffer.length);
            else
                bufferDelta[0] += bufferDelta[bufferStackPos + 1] + ((CodeReader) result).buffer.length;

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
        return new _BasicToken(signal,
                resolveOffset(bufferPos[bufferStackPos] + 1));
    }

    protected IToken newToken(int signal, char[] buffer) {
        IToken i = new _ImagedToken(signal, buffer,
                resolveOffset(bufferPos[bufferStackPos] + 1));
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

    private static class _BasicToken implements IToken, ITokenDuple {

        public _BasicToken( int type, int endOffset )
        {
            setType( type );
            setOffsetByLength( endOffset );
        }
        
        public String toString() {
            return getImage();
        }
            
        public int getType() { return type; }

        public void setType(int i) {
            type = i;
        }

        public int getLineNumber() {
            return 0;
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.IToken#getFilename()
         */
        public char[] getFilename() {
            return EMPTY_CHAR_ARRAY;
        }
        
        public int getEndOffset() { return getOffset() + getLength(); }

        private int type;
        private IToken next = null;
        private int offset;

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object other) {
            if( other == null ) return false;
            if( !( other instanceof IToken ) ) 
                return false;
            if( ((IToken)other).getType() != getType() ) 
                return false;
            if( !CharArrayUtils.equals( ((IToken)other).getCharImage(), getCharImage() ) ) 
                return false;
            if( getOffset() != ((IToken)other).getOffset() )
                return false;
            if( getEndOffset() != ((IToken)other).getEndOffset() )
                return false;
            return true;
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.IToken#isKeyword()
         */
        public boolean canBeAPrefix() {
            switch( getType() )
            {
                case tIDENTIFIER:
                case tCOMPL:
                    return true;
                default:
                    if( getType() >= t_and && getType() <= t_xor_eq ) return true;
                    if( getType() >= t__Bool && getType() <= t_restrict ) return true;
            }
            return false;
        }

        public boolean looksLikeExpression()
        {
            switch( getType() )
            {
                case IToken.tINTEGER:
                case IToken.t_false:
                case IToken.t_true:
                case IToken.tSTRING:
                case IToken.tLSTRING:
                case IToken.tFLOATINGPT:
                case IToken.tCHAR:
                case IToken.tAMPER:
                case IToken.tDOT:
                case IToken.tLPAREN:
                case IToken.tMINUS:
                case IToken.tSTAR: 
                case IToken.tPLUS: 
                case IToken.tNOT:
                case IToken.tCOMPL:
                    return true;
                default:
                    break;
            }   
            return false;
        }
        
        public boolean isOperator()
        {
            switch( getType() )
            {
                case IToken.t_new:
                case IToken.t_delete:
                case IToken.tPLUS:
                case IToken.tMINUS:
                case IToken.tSTAR:
                case IToken.tDIV:
                case IToken.tXOR:
                case IToken.tMOD:
                case IToken.tAMPER:
                case IToken.tBITOR:
                case IToken.tCOMPL:
                case IToken.tNOT:
                case IToken.tASSIGN:
                case IToken.tLT:
                case IToken.tGT:
                case IToken.tPLUSASSIGN:
                case IToken.tMINUSASSIGN:
                case IToken.tSTARASSIGN:
                case IToken.tDIVASSIGN:
                case IToken.tMODASSIGN:
                case IToken.tBITORASSIGN:
                case IToken.tAMPERASSIGN:
                case IToken.tXORASSIGN:
                case IToken.tSHIFTL:
                case IToken.tSHIFTR:
                case IToken.tSHIFTLASSIGN:
                case IToken.tSHIFTRASSIGN:
                case IToken.tEQUAL:
                case IToken.tNOTEQUAL:
                case IToken.tLTEQUAL:
                case IToken.tGTEQUAL:
                case IToken.tAND:
                case IToken.tOR:
                case IToken.tINCR:
                case IToken.tDECR:
                case IToken.tCOMMA:
                case IToken.tARROW:
                case IToken.tARROWSTAR:
                    return true;
                default:
                    return false;
            }
        }
        
        public boolean isPointer()
        {
            return (getType() == IToken.tAMPER || getType() == IToken.tSTAR);
        }



        public final IToken getNext() { return next; }
        public void setNext(IToken t) { next = t; }
        
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ITokenDuple#contains(org.eclipse.cdt.core.parser.ITokenDuple)
         */
        public boolean contains(ITokenDuple duple) {
            return ( duple.getFirstToken() == duple.getLastToken() ) && ( duple.getFirstToken() == this );
        }
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ITokenDuple#extractNameFromTemplateId()
         */
        public char[] extractNameFromTemplateId(){
            return getCharImage();
        }
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ITokenDuple#findLastTokenType(int)
         */
        public int findLastTokenType(int t) {
            if( getType() == t ) return 0;
            return -1;
        }
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ITokenDuple#getFirstToken()
         */
        public IToken getFirstToken() {
            return this;
        }
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ITokenDuple#getLastSegment()
         */
        public ITokenDuple getLastSegment() {
            return this;
        }
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ITokenDuple#getLastToken()
         */
        public IToken getLastToken() {
            return this;
        }
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ITokenDuple#getLeadingSegments()
         */
        public ITokenDuple getLeadingSegments() {
            return null;
        }
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ITokenDuple#getSegmentCount()
         */
        public int getSegmentCount() {
            return 1;
        }
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ITokenDuple#getStartOffset()
         */
        public int getStartOffset() {
            return getOffset();
        }
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ITokenDuple#getSubrange(int, int)
         */
        public ITokenDuple getSubrange(int startIndex, int endIndex) {
            if( startIndex == 0 && endIndex == 0 ) return this;
            return null;
        }
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ITokenDuple#getTemplateIdArgLists()
         */
        public List[] getTemplateIdArgLists() {
            // TODO Auto-generated method stub
            return null;
        }
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ITokenDuple#getToken(int)
         */
        public IToken getToken(int index) {
            if( index == 0 ) return this;
            return null;
        }
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ITokenDuple#isIdentifier()
         */
        public boolean isIdentifier() {
            return ( getType() == IToken.tIDENTIFIER );
        }
        
        
        private class SingleIterator implements Iterator
        {
            boolean hasNext = true;
            /* (non-Javadoc)
             * @see java.util.Iterator#remove()
             */
            public void remove() {
                throw new UnsupportedOperationException();
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#hasNext()
             */
            public boolean hasNext() {
                return hasNext;
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#next()
             */
            public Object next() {
                hasNext = false;
                return _BasicToken.this;
            }
            
        }
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ITokenDuple#iterator()
         */
        public Iterator iterator() {
            return new SingleIterator();
        }
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ITokenDuple#length()
         */
        public int length() {
            return 1;
        }
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ITokenDuple#syntaxOfName()
         */
        public boolean syntaxOfName() {
            return ( getType() == IToken.tIDENTIFIER );
        }
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ITokenDuple#toQualifiedName()
         */
        public String[] toQualifiedName() {
            String [] qualifiedName = new String[1];
            qualifiedName[0] = getImage();
            return qualifiedName;
        }
        
        
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ITokenDuple#freeReferences(org.eclipse.cdt.core.parser.ast.IReferenceManager)
         */
        public void freeReferences() {
        }
        
        
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ITokenDuple#acceptElement(org.eclipse.cdt.core.parser.ast.IReferenceManager)
         */
        public void acceptElement(ISourceElementRequestor requestor) {
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ITokenDuple#getSegmentIterator()
         */
        public ITokenDuple[] getSegments() {
            ITokenDuple [] r = new ITokenDuple[0];
            r[0] = this;
            return r;
        }
        

        // All the tokens generated by the macro expansion 
        // will have dimensions (offset and length) equal to the expanding symbol.
        public int getOffset() { 
            return offset; 
        }
        
        public int getLength() {
            return getCharImage().length;
        }
        
        protected void setOffsetByLength( int endOffset )
        {
            this.offset = endOffset - getLength();
        }
        
        public String getImage() { 
            switch ( getType() ) {
                    case IToken.tCOLONCOLON :
                        return "::" ; //$NON-NLS-1$
                    case IToken.tCOLON :
                        return ":" ; //$NON-NLS-1$
                    case IToken.tSEMI :
                        return ";" ; //$NON-NLS-1$
                    case IToken.tCOMMA :
                        return "," ; //$NON-NLS-1$
                    case IToken.tQUESTION :
                        return "?" ; //$NON-NLS-1$
                    case IToken.tLPAREN  :
                        return "(" ; //$NON-NLS-1$
                    case IToken.tRPAREN  :
                        return ")" ; //$NON-NLS-1$
                    case IToken.tLBRACKET :
                        return "[" ; //$NON-NLS-1$
                    case IToken.tRBRACKET :
                        return "]" ; //$NON-NLS-1$
                    case IToken.tLBRACE :
                        return "{" ; //$NON-NLS-1$
                    case IToken.tRBRACE :
                        return "}"; //$NON-NLS-1$
                    case IToken.tPLUSASSIGN :
                        return "+="; //$NON-NLS-1$
                    case IToken.tINCR :
                        return "++" ; //$NON-NLS-1$
                    case IToken.tPLUS :
                        return "+"; //$NON-NLS-1$
                    case IToken.tMINUSASSIGN :
                        return "-=" ; //$NON-NLS-1$
                    case IToken.tDECR :
                        return "--" ; //$NON-NLS-1$
                    case IToken.tARROWSTAR :
                        return "->*" ; //$NON-NLS-1$
                    case IToken.tARROW :
                        return "->" ; //$NON-NLS-1$
                    case IToken.tMINUS :
                        return "-" ; //$NON-NLS-1$
                    case IToken.tSTARASSIGN :
                        return "*=" ; //$NON-NLS-1$
                    case IToken.tSTAR :
                        return "*" ; //$NON-NLS-1$
                    case IToken.tMODASSIGN :
                        return "%=" ; //$NON-NLS-1$
                    case IToken.tMOD :
                        return "%" ; //$NON-NLS-1$
                    case IToken.tXORASSIGN :
                        return "^=" ; //$NON-NLS-1$
                    case IToken.tXOR :
                        return "^" ; //$NON-NLS-1$
                    case IToken.tAMPERASSIGN :
                        return "&=" ; //$NON-NLS-1$
                    case IToken.tAND :
                        return "&&" ; //$NON-NLS-1$
                    case IToken.tAMPER :
                        return "&" ; //$NON-NLS-1$
                    case IToken.tBITORASSIGN :
                        return "|=" ; //$NON-NLS-1$
                    case IToken.tOR :
                        return "||" ; //$NON-NLS-1$
                    case IToken.tBITOR :
                        return "|" ; //$NON-NLS-1$
                    case IToken.tCOMPL :
                        return "~" ; //$NON-NLS-1$
                    case IToken.tNOTEQUAL :
                        return "!=" ; //$NON-NLS-1$
                    case IToken.tNOT :
                        return "!" ; //$NON-NLS-1$
                    case IToken.tEQUAL :
                        return "==" ; //$NON-NLS-1$
                    case IToken.tASSIGN :
                        return "=" ; //$NON-NLS-1$
                    case IToken.tSHIFTL :
                        return "<<" ; //$NON-NLS-1$
                    case IToken.tLTEQUAL :
                        return "<=" ; //$NON-NLS-1$
                    case IToken.tLT :
                        return "<"; //$NON-NLS-1$
                    case IToken.tSHIFTRASSIGN :
                        return ">>=" ; //$NON-NLS-1$
                    case IToken.tSHIFTR :
                        return ">>" ; //$NON-NLS-1$
                    case IToken.tGTEQUAL :
                        return ">=" ; //$NON-NLS-1$
                    case IToken.tGT :
                        return ">" ; //$NON-NLS-1$
                    case IToken.tSHIFTLASSIGN :
                        return "<<=" ; //$NON-NLS-1$
                    case IToken.tELLIPSIS :
                        return "..." ; //$NON-NLS-1$
                    case IToken.tDOTSTAR :
                        return ".*" ; //$NON-NLS-1$
                    case IToken.tDOT :
                        return "." ; //$NON-NLS-1$
                    case IToken.tDIVASSIGN :
                        return "/=" ; //$NON-NLS-1$
                    case IToken.tDIV :
                        return "/" ; //$NON-NLS-1$
                    case IToken.t_and :
                        return Keywords.AND;
                    case IToken.t_and_eq :
                        return Keywords.AND_EQ ;
                    case IToken.t_asm :
                        return Keywords.ASM ;
                    case IToken.t_auto :
                        return Keywords.AUTO ;
                    case IToken.t_bitand :
                        return Keywords.BITAND ;
                    case IToken.t_bitor :
                        return Keywords.BITOR ;
                    case IToken.t_bool :
                        return Keywords.BOOL ;
                    case IToken.t_break :
                        return Keywords.BREAK ;
                    case IToken.t_case :
                        return Keywords.CASE ;
                    case IToken.t_catch :
                        return Keywords.CATCH ;
                    case IToken.t_char :
                        return Keywords.CHAR ;
                    case IToken.t_class :
                        return Keywords.CLASS ;
                    case IToken.t_compl :
                        return Keywords.COMPL ;
                    case IToken.t_const :
                        return Keywords.CONST ;
                    case IToken.t_const_cast :
                        return Keywords.CONST_CAST ;
                    case IToken.t_continue :
                        return Keywords.CONTINUE ;
                    case IToken.t_default :
                        return Keywords.DEFAULT ;
                    case IToken.t_delete :
                        return Keywords.DELETE ;
                    case IToken.t_do :
                        return Keywords.DO;
                    case IToken.t_double :
                        return Keywords.DOUBLE ;
                    case IToken.t_dynamic_cast :
                        return Keywords.DYNAMIC_CAST ;
                    case IToken.t_else :
                        return Keywords.ELSE;
                    case IToken.t_enum :
                        return Keywords.ENUM ;
                    case IToken.t_explicit :
                        return Keywords.EXPLICIT ;
                    case IToken.t_export :
                        return Keywords.EXPORT ;
                    case IToken.t_extern :
                        return Keywords.EXTERN;
                    case IToken.t_false :
                        return Keywords.FALSE;
                    case IToken.t_float :
                        return Keywords.FLOAT;
                    case IToken.t_for :
                        return Keywords.FOR;
                    case IToken.t_friend :
                        return Keywords.FRIEND;
                    case IToken.t_goto :
                        return Keywords.GOTO;
                    case IToken.t_if :
                        return Keywords.IF ;
                    case IToken.t_inline :
                        return Keywords.INLINE ;
                    case IToken.t_int :
                        return Keywords.INT ;
                    case IToken.t_long :
                        return Keywords.LONG ;
                    case IToken.t_mutable :
                        return Keywords.MUTABLE ;
                    case IToken.t_namespace :
                        return Keywords.NAMESPACE ;
                    case IToken.t_new :
                        return Keywords.NEW ;
                    case IToken.t_not :
                        return Keywords.NOT ;
                    case IToken.t_not_eq :
                        return Keywords.NOT_EQ; 
                    case IToken.t_operator :
                        return Keywords.OPERATOR ;
                    case IToken.t_or :
                        return Keywords.OR ;
                    case IToken.t_or_eq :
                        return Keywords.OR_EQ;
                    case IToken.t_private :
                        return Keywords.PRIVATE ;
                    case IToken.t_protected :
                        return Keywords.PROTECTED ;
                    case IToken.t_public :
                        return Keywords.PUBLIC ;
                    case IToken.t_register :
                        return Keywords.REGISTER ;
                    case IToken.t_reinterpret_cast :
                        return Keywords.REINTERPRET_CAST ;
                    case IToken.t_return :
                        return Keywords.RETURN ;
                    case IToken.t_short :
                        return Keywords.SHORT ;
                    case IToken.t_sizeof :
                        return Keywords.SIZEOF ;
                    case IToken.t_static :
                        return Keywords.STATIC ;
                    case IToken.t_static_cast :
                        return Keywords.STATIC_CAST ;
                    case IToken.t_signed :
                        return Keywords.SIGNED ;
                    case IToken.t_struct :
                        return Keywords.STRUCT ;
                    case IToken.t_switch :
                        return Keywords.SWITCH ;
                    case IToken.t_template :
                        return Keywords.TEMPLATE ;
                    case IToken.t_this :
                        return Keywords.THIS ;
                    case IToken.t_throw :
                        return Keywords.THROW ;
                    case IToken.t_true :
                        return Keywords.TRUE ;
                    case IToken.t_try :
                        return Keywords.TRY ;
                    case IToken.t_typedef :
                        return Keywords.TYPEDEF ;
                    case IToken.t_typeid :
                        return Keywords.TYPEID ;
                    case IToken.t_typename :
                        return Keywords.TYPENAME ;
                    case IToken.t_union :
                        return Keywords.UNION ;
                    case IToken.t_unsigned :
                        return Keywords.UNSIGNED ;
                    case IToken.t_using :
                        return Keywords.USING ;
                    case IToken.t_virtual :
                        return Keywords.VIRTUAL ;
                    case IToken.t_void :
                        return Keywords.VOID ;
                    case IToken.t_volatile :
                        return Keywords.VOLATILE;
                    case IToken.t_wchar_t :
                        return Keywords.WCHAR_T ;
                    case IToken.t_while :
                        return Keywords.WHILE ;
                    case IToken.t_xor :
                        return Keywords.XOR ;
                    case IToken.t_xor_eq :
                        return Keywords.XOR_EQ ;
                    case IToken.t__Bool :
                        return Keywords._BOOL ;
                    case IToken.t__Complex :
                        return Keywords._COMPLEX ;
                    case IToken.t__Imaginary :
                        return Keywords._IMAGINARY ;
                    case IToken.t_restrict :
                        return Keywords.RESTRICT ;
                    case IScanner.tPOUND:
                        return "#"; //$NON-NLS-1$
                    case IScanner.tPOUNDPOUND:
                        return "##"; //$NON-NLS-1$
                    case IToken.tEOC:
                        return "EOC"; //$NON-NLS-1$
                    default :
                        // we should never get here!
                        // assert false : getType();
                        return ""; //$NON-NLS-1$ 
            }           
        }
       
        
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.IToken#setImage()
         */
        public void setImage( String i ) {
            // do nothing
        }

        public char[] getCharImage() {
            return DOMScanner.getCharImage( getType() );
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.IToken#setImage(char[])
         */
        public void setImage(char[] i) {
            // do nothing
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ITokenDuple#toCharArray()
         */
        public char[] toCharArray() {
            return getCharImage();
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.ITokenDuple#isConversion()
         */
        public boolean isConversion() {
            return false;
        }

        
    }

    private static class _ImagedToken extends _BasicToken {

        private char [] image = null;
        
        public _ImagedToken( int t, char[] i, int endOffset ) {
            super( t, 0 );
            setImage(i);
            setOffsetByLength( endOffset );
        }
        
        
        /* (non-Javadoc)
         * @see org.eclipse.cdt.internal.core.parser.token.AbstractToken#getImage()
         */
        public final String getImage() {
            if( image == null ) return null;
            return new String( image );
        }
        
        public final char[] getCharImage() {
            return image;
        }
        /* (non-Javadoc)
         * @see org.eclipse.cdt.core.parser.IToken#setImage(java.lang.String)
         */
        public void setImage(String i) {
            image = i.toCharArray();
        }
        
        public void setImage( char [] image )
        {
            this.image = image;
        }
        
            
        public int getLength() {
            if( getCharImage() == null )
                return 0;
            int s_length = getCharImage().length;
             switch( getType() )
             {
             case IToken.tSTRING:
                return s_length + 2;
             case IToken.tLSTRING:
                return s_length + 3;
             default:
                return s_length;
             }
        }
    }

    
    static char[] getCharImage( int type ){
        switch ( type ) {
        case IToken.tCOLONCOLON :   return Keywords.cpCOLONCOLON; 
        case IToken.tCOLON :        return Keywords.cpCOLON;
        case IToken.tSEMI :         return Keywords.cpSEMI;
        case IToken.tCOMMA :        return Keywords.cpCOMMA;
        case IToken.tQUESTION :     return Keywords.cpQUESTION;
        case IToken.tLPAREN  :      return Keywords.cpLPAREN;
        case IToken.tRPAREN  :      return Keywords.cpRPAREN;
        case IToken.tLBRACKET :     return Keywords.cpLBRACKET;
        case IToken.tRBRACKET :     return Keywords.cpRBRACKET;
        case IToken.tLBRACE :       return Keywords.cpLBRACE;
        case IToken.tRBRACE :       return Keywords.cpRBRACE;
        case IToken.tPLUSASSIGN :   return Keywords.cpPLUSASSIGN;
        case IToken.tINCR :         return Keywords.cpINCR;
        case IToken.tPLUS :         return Keywords.cpPLUS;
        case IToken.tMINUSASSIGN :  return Keywords.cpMINUSASSIGN;
        case IToken.tDECR :         return Keywords.cpDECR;
        case IToken.tARROWSTAR :    return Keywords.cpARROWSTAR;
        case IToken.tARROW :        return Keywords.cpARROW;
        case IToken.tMINUS :        return Keywords.cpMINUS;
        case IToken.tSTARASSIGN :   return Keywords.cpSTARASSIGN;
        case IToken.tSTAR :         return Keywords.cpSTAR;
        case IToken.tMODASSIGN :    return Keywords.cpMODASSIGN;
        case IToken.tMOD :          return Keywords.cpMOD;
        case IToken.tXORASSIGN :    return Keywords.cpXORASSIGN;
        case IToken.tXOR :          return Keywords.cpXOR;
        case IToken.tAMPERASSIGN :  return Keywords.cpAMPERASSIGN;
        case IToken.tAND :          return Keywords.cpAND;
        case IToken.tAMPER :        return Keywords.cpAMPER;
        case IToken.tBITORASSIGN :  return Keywords.cpBITORASSIGN;
        case IToken.tOR :           return Keywords.cpOR;
        case IToken.tBITOR :        return Keywords.cpBITOR;
        case IToken.tCOMPL :        return Keywords.cpCOMPL;
        case IToken.tNOTEQUAL :     return Keywords.cpNOTEQUAL;
        case IToken.tNOT :          return Keywords.cpNOT;
        case IToken.tEQUAL :        return Keywords.cpEQUAL;
        case IToken.tASSIGN :       return Keywords.cpASSIGN;
        case IToken.tSHIFTL :       return Keywords.cpSHIFTL;
        case IToken.tLTEQUAL :      return Keywords.cpLTEQUAL;
        case IToken.tLT :           return Keywords.cpLT;
        case IToken.tSHIFTRASSIGN : return Keywords.cpSHIFTRASSIGN;
        case IToken.tSHIFTR :       return Keywords.cpSHIFTR;
        case IToken.tGTEQUAL :      return Keywords.cpGTEQUAL;
        case IToken.tGT :           return Keywords.cpGT;
        case IToken.tSHIFTLASSIGN : return Keywords.cpSHIFTLASSIGN;
        case IToken.tELLIPSIS :     return Keywords.cpELLIPSIS;
        case IToken.tDOTSTAR :      return Keywords.cpDOTSTAR;
        case IToken.tDOT :          return Keywords.cpDOT;
        case IToken.tDIVASSIGN :    return Keywords.cpDIVASSIGN;
        case IToken.tDIV :          return Keywords.cpDIV;
        case IToken.t_and :         return Keywords.cAND;
        case IToken.t_and_eq :      return Keywords.cAND_EQ ;
        case IToken.t_asm :         return Keywords.cASM ;
        case IToken.t_auto :        return Keywords.cAUTO ;
        case IToken.t_bitand :      return Keywords.cBITAND ;
        case IToken.t_bitor :       return Keywords.cBITOR ;
        case IToken.t_bool :        return Keywords.cBOOL ;
        case IToken.t_break :       return Keywords.cBREAK ;
        case IToken.t_case :        return Keywords.cCASE ;
        case IToken.t_catch :       return Keywords.cCATCH ;
        case IToken.t_char :        return Keywords.cCHAR ;
        case IToken.t_class :       return Keywords.cCLASS ;
        case IToken.t_compl :       return Keywords.cCOMPL ;
        case IToken.t_const :       return Keywords.cCONST ;
        case IToken.t_const_cast :  return Keywords.cCONST_CAST ;
        case IToken.t_continue :    return Keywords.cCONTINUE ;
        case IToken.t_default :     return Keywords.cDEFAULT ;
        case IToken.t_delete :      return Keywords.cDELETE ;
        case IToken.t_do :          return Keywords.cDO;
        case IToken.t_double :      return Keywords.cDOUBLE ;
        case IToken.t_dynamic_cast: return Keywords.cDYNAMIC_CAST ;
        case IToken.t_else :        return Keywords.cELSE;
        case IToken.t_enum :        return Keywords.cENUM ;
        case IToken.t_explicit :    return Keywords.cEXPLICIT ;
        case IToken.t_export :      return Keywords.cEXPORT ;
        case IToken.t_extern :      return Keywords.cEXTERN;
        case IToken.t_false :       return Keywords.cFALSE;
        case IToken.t_float :       return Keywords.cFLOAT;
        case IToken.t_for :         return Keywords.cFOR;
        case IToken.t_friend :      return Keywords.cFRIEND;
        case IToken.t_goto :        return Keywords.cGOTO;
        case IToken.t_if :          return Keywords.cIF ;
        case IToken.t_inline :      return Keywords.cINLINE ;
        case IToken.t_int :         return Keywords.cINT ;
        case IToken.t_long :        return Keywords.cLONG ;
        case IToken.t_mutable :     return Keywords.cMUTABLE ;
        case IToken.t_namespace :   return Keywords.cNAMESPACE ;
        case IToken.t_new :         return Keywords.cNEW ;
        case IToken.t_not :         return Keywords.cNOT ;
        case IToken.t_not_eq :      return Keywords.cNOT_EQ; 
        case IToken.t_operator :    return Keywords.cOPERATOR ;
        case IToken.t_or :          return Keywords.cOR ;
        case IToken.t_or_eq :       return Keywords.cOR_EQ;
        case IToken.t_private :     return Keywords.cPRIVATE ;
        case IToken.t_protected :   return Keywords.cPROTECTED ;
        case IToken.t_public :      return Keywords.cPUBLIC ;
        case IToken.t_register :    return Keywords.cREGISTER ;
        case IToken.t_reinterpret_cast :    return Keywords.cREINTERPRET_CAST ;
        case IToken.t_return :      return Keywords.cRETURN ;
        case IToken.t_short :       return Keywords.cSHORT ;
        case IToken.t_sizeof :      return Keywords.cSIZEOF ;
        case IToken.t_static :      return Keywords.cSTATIC ;
        case IToken.t_static_cast : return Keywords.cSTATIC_CAST ;
        case IToken.t_signed :      return Keywords.cSIGNED ;
        case IToken.t_struct :      return Keywords.cSTRUCT ;
        case IToken.t_switch :      return Keywords.cSWITCH ;
        case IToken.t_template :    return Keywords.cTEMPLATE ;
        case IToken.t_this :        return Keywords.cTHIS ;
        case IToken.t_throw :       return Keywords.cTHROW ;
        case IToken.t_true :        return Keywords.cTRUE ;
        case IToken.t_try :         return Keywords.cTRY ;
        case IToken.t_typedef :     return Keywords.cTYPEDEF ;
        case IToken.t_typeid :      return Keywords.cTYPEID ;
        case IToken.t_typename :    return Keywords.cTYPENAME ;
        case IToken.t_union :       return Keywords.cUNION ;
        case IToken.t_unsigned :    return Keywords.cUNSIGNED ;
        case IToken.t_using :       return Keywords.cUSING ;
        case IToken.t_virtual :     return Keywords.cVIRTUAL ;
        case IToken.t_void :        return Keywords.cVOID ;
        case IToken.t_volatile :    return Keywords.cVOLATILE;
        case IToken.t_wchar_t :     return Keywords.cWCHAR_T ;
        case IToken.t_while :       return Keywords.cWHILE ;
        case IToken.t_xor :         return Keywords.cXOR ;
        case IToken.t_xor_eq :      return Keywords.cXOR_EQ ;
        case IToken.t__Bool :       return Keywords.c_BOOL ;
        case IToken.t__Complex :    return Keywords.c_COMPLEX ;
        case IToken.t__Imaginary :  return Keywords.c_IMAGINARY ;
        case IToken.t_restrict :    return Keywords.cRESTRICT ;
        case IScanner.tPOUND:       return Keywords.cpPOUND; 
        case IScanner.tPOUNDPOUND:  return Keywords.cpPOUNDPOUND;
        
        default :
            // we should never get here!
            // assert false : getType();
            return "".toCharArray(); //$NON-NLS-1$ 
        }
        //return getImage().toCharArray(); //TODO - fix me!
    }

}