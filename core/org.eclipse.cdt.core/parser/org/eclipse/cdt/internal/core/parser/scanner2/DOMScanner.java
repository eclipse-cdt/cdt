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
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IMacro;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.internal.core.parser.token.ImagedExpansionToken;
import org.eclipse.cdt.internal.core.parser.token.ImagedToken;
import org.eclipse.cdt.internal.core.parser.token.SimpleExpansionToken;
import org.eclipse.cdt.internal.core.parser.token.SimpleToken;

/**
 * @author jcamelon
 */
public class DOMScanner extends BaseScanner {

    private final ICodeReaderFactory codeReaderFactory;
    private int globalCounter = 0;
    private int contextDelta = 0;
    
    private static class DOMInclusion
    {
        public final char[] pt;
        public final int o;

        /**
         * 
         */
        public DOMInclusion( char [] path, int offset ) {
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
     * @param readerFactory TODO
     * @param requestor
     */
    public DOMScanner(CodeReader reader, IScannerInfo info, ParserMode parserMode, ParserLanguage language, IParserLogService log, IScannerExtensionConfiguration configuration, ICodeReaderFactory readerFactory) {
        super(reader, info, parserMode, language, log, configuration);
        this.expressionEvaluator = new ExpressionEvaluator(null, null);
        this.codeReaderFactory = readerFactory;
        postConstructorSetup(reader, info);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.IScanner#getLocationResolver()
     */
    public ILocationResolver getLocationResolver() {
        if( locationMap instanceof ILocationResolver )
            return (ILocationResolver) locationMap;
        return null;
    }


    final IScannerPreprocessorLog locationMap = new LocationMap();


    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.IScanner#setASTFactory(org.eclipse.cdt.core.parser.ast.IASTFactory)
     */
    public void setASTFactory(IASTFactory f) {
        // do nothing
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#createInclusionConstruct(char[], char[], boolean, int, int, int, int, int, int, int, boolean)
     */
    protected Object createInclusionConstruct(char[] fileName, char[] filenamePath, boolean local, int startOffset, int startingLineNumber, int nameOffset, int nameEndOffset, int nameLine, int endOffset, int endLine, boolean isForced) {
        return new DOMInclusion( filenamePath, startOffset );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#processMacro(char[], int, int, int, int, int, int, int)
     */
    protected void processMacro(char[] name, int startingOffset, int startingLineNumber, int idstart, int idend, int nameLine, int textEnd, int endingLine, IMacro macro) {
        if( macro instanceof ObjectStyleMacro )
            locationMap.defineObjectStyleMacro( (ObjectStyleMacro) macro, startingLineNumber, idstart, idend, textEnd );
        else if( macro instanceof FunctionStyleMacro )
            locationMap.defineFunctionStyleMacro( (FunctionStyleMacro) macro, startingLineNumber, idstart, idend, textEnd );
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#createReaderDuple(java.lang.String)
     */
    protected CodeReader createReaderDuple(String finalPath) {
        return codeReaderFactory.createCodeReaderForInclusion(finalPath);
    }
    
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#pushContext(char[])
     */
    protected void pushContext(char[] buffer) {
        //TODO calibrate offsets
        super.pushContext(buffer);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#pushContext(char[], java.lang.Object)
     */
    protected void pushContext(char[] buffer, Object data) {
        //TODO calibrate offsets
        super.pushContext(buffer, data);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#popContext()
     */
    protected Object popContext() {
        //TODO calibrate offsets
        Object result = super.popContext();
        if( result instanceof CodeReader )
        {
           globalCounter += (((CodeReader)result).buffer.length - contextDelta);
           locationMap.endTranslationUnit( globalCounter );
        }
        return result;
    }
	/**
	 * @return
	 */
	protected IToken newToken( int signal ) {
	    if( bufferData[bufferStackPos] instanceof MacroData )
		{
			int mostRelevant;
			for( mostRelevant = bufferStackPos; mostRelevant >= 0; --mostRelevant )
				if( bufferData[mostRelevant] instanceof InclusionData || bufferData[mostRelevant] instanceof CodeReader )
					break;
			MacroData data = (MacroData)bufferData[mostRelevant + 1];
			return new SimpleExpansionToken( signal, resolveOffset( data.startOffset ), data.endOffset - data.startOffset + 1, getCurrentFilename(), getLineNumber( bufferPos[mostRelevant] + 1)); 
		}
		return new SimpleToken(signal,  resolveOffset( bufferPos[bufferStackPos] + 1 ) , getCurrentFilename(), getLineNumber( bufferPos[bufferStackPos] + 1)  );
	}

	protected IToken newToken( int signal, char [] buffer )
	{
		if( bufferData[bufferStackPos] instanceof MacroData )
		{
			int mostRelevant;
			for( mostRelevant = bufferStackPos; mostRelevant >= 0; --mostRelevant )
				if( bufferData[mostRelevant] instanceof InclusionData || bufferData[mostRelevant] instanceof CodeReader )
					break;
			MacroData data = (MacroData)bufferData[mostRelevant + 1];
			return new ImagedExpansionToken( signal, buffer, resolveOffset( data.startOffset ), data.endOffset - data.startOffset + 1, getCurrentFilename(), getLineNumber( bufferPos[mostRelevant] + 1));
		}
		IToken i = new ImagedToken(signal, buffer, resolveOffset( bufferPos[bufferStackPos] + 1 ), EMPTY_CHAR_ARRAY, getLineNumber( bufferPos[bufferStackPos] + 1));
		if( buffer != null && buffer.length == 0 && signal != IToken.tSTRING && signal != IToken.tLSTRING )
			bufferPos[bufferStackPos] += 1; //TODO - remove this hack at some point
		
		return i;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#quickParsePushPopInclusion(java.lang.Object)
     */
    protected void quickParsePushPopInclusion(Object inclusion) {
        //do nothing
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#pushInclusion(java.lang.Object)
     */
    protected void pushInclusion(Object data) {
        super.pushInclusion(data);
        if( data instanceof DOMInclusion )
        {
            DOMInclusion d = (DOMInclusion)data;
            locationMap.startInclusion( d.pt, d.o );
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#popInclusion()
     */
    protected void popInclusion(Object data) {
        super.popInclusion(data);
        if( data instanceof DOMInclusion )
        {
            DOMInclusion d = (DOMInclusion)data;
            locationMap.endInclusion( d.pt, d.o );
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#handleProblem(int, int, char[])
     */
    protected void handleProblem(int id, int offset, char[] arg) {
        IASTProblem problem = new ScannerASTProblem(id, arg, true, false );
        int o = resolveOffset( offset );
        ((ScannerASTProblem)problem).setOffsetAndLength( o, resolveOffset( getCurrentOffset() + 1 ) - o );
        locationMap.encounterProblem(problem); 
    }

    /**
     * @param offset
     * @return
     */
    private int resolveOffset(int offset) {
        return globalCounter - contextDelta + offset; 
    }

    
    /* (non-Javadoc)
    * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#postConstructorSetup(org.eclipse.cdt.core.parser.CodeReader, org.eclipse.cdt.core.parser.IScannerInfo)
    */
   protected void postConstructorSetup(CodeReader reader, IScannerInfo info) {
      super.postConstructorSetup(reader, info);
      locationMap.startTranslationUnit( getMainFilename() );
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#throwEOF()
    */
   protected void throwEOF() throws EndOfFileException {
      locationMap.endTranslationUnit( globalCounter );
      super.throwEOF();
   }
   
}
