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
import org.eclipse.cdt.core.parser.CodeReader;
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
//    private int overallOffset = 0;


    /**
     * @param reader
     * @param info
     * @param parserMode
     * @param language
     * @param log
     * @param readerFactory TODO
     * @param requestor
     */
    public DOMScanner(CodeReader reader, IScannerInfo info, ParserMode parserMode, ParserLanguage language, IParserLogService log, IScannerConfiguration configuration, ICodeReaderFactory readerFactory) {
        super(reader, info, parserMode, language, log, configuration);
        this.expressionEvaluator = new ExpressionEvaluator(null, spf);
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
    protected Object createInclusionConstruct(char[] fileNameArray, char[] filename, boolean local, int startOffset, int startingLineNumber, int nameOffset, int nameEndOffset, int nameLine, int endOffset, int endLine, boolean isForced) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#processMacro(char[], int, int, int, int, int, int, int)
     */
    protected void processMacro(char[] name, int startingOffset, int startingLineNumber, int idstart, int idend, int nameLine, int textEnd, int endingLine) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.scanner2.BaseScanner#createReaderDuple(java.lang.String)
     */
    protected CodeReader createReaderDuple(String finalPath) {
        return codeReaderFactory.createCodeReaderForInclusion(finalPath);
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
			return new SimpleExpansionToken( signal, data.startOffset, data.endOffset - data.startOffset + 1, getCurrentFilename(), getLineNumber( bufferPos[mostRelevant] + 1)); 
		}
		return new SimpleToken(signal,  bufferPos[bufferStackPos] + 1 , getCurrentFilename(), getLineNumber( bufferPos[bufferStackPos] + 1)  );
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
			return new ImagedExpansionToken( signal, buffer, data.startOffset, data.endOffset - data.startOffset + 1, getCurrentFilename(), getLineNumber( bufferPos[mostRelevant] + 1));
		}
		IToken i = new ImagedToken(signal, buffer, bufferPos[bufferStackPos] + 1 , getCurrentFilename(), getLineNumber( bufferPos[bufferStackPos] + 1));
		if( buffer != null && buffer.length == 0 && signal != IToken.tSTRING && signal != IToken.tLSTRING )
			bufferPos[bufferStackPos] += 1; //TODO - remove this hack at some point
		
		return i;
	}

}
