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

/**
 * @author jcamelon
 */
public class DOMScanner extends Scanner2 {

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
        super(reader, info, null, parserMode, language, log, workingCopies,
                configuration);
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

}
