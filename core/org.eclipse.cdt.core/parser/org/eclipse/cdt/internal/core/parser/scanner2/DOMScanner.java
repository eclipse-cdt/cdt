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

import java.util.List;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.IASTFactory;

/**
 * @author jcamelon
 */
public class DOMScanner extends BaseScanner {

    /**
     * @param reader
     * @param info
     * @param parserMode
     * @param language
     * @param log
     * @param workingCopies
     * @param requestor
     */
    public DOMScanner(CodeReader reader, IScannerInfo info, ParserMode parserMode, ParserLanguage language, IParserLogService log, List workingCopies, IScannerConfiguration configuration) {
        super(reader, info, parserMode, language, log, configuration);
        this.expressionEvaluator = new ExpressionEvaluator(null, spf);
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
        // TODO Auto-generated method stub
        return null;
    }
    

}
