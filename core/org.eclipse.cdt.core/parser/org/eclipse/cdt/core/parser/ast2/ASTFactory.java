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
package org.eclipse.cdt.core.parser.ast2;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.internal.core.parser.ast2.AST2SourceElementRequestor;

/**
 * This is a factory class that will create a IASTTranslationUnit for a given
 * source file or string.
 * 
 * @author Doug Schaefer
 */
public class ASTFactory {

    public static IASTTranslationUnit parseString(String code, IScannerInfo scannerInfo) {
        AST2SourceElementRequestor callback = new AST2SourceElementRequestor();
        IScanner scanner = ParserFactory.createScanner(
                new CodeReader(code.toCharArray()),
                scannerInfo,
                ParserMode.COMPLETE_PARSE,
                ParserLanguage.CPP,
                callback,
                callback,
                null);
        IParser parser = ParserFactory.createParser(
                scanner,
                callback,
                ParserMode.COMPLETE_PARSE,
                ParserLanguage.CPP,
                callback);
        parser.parse();
        
        return callback.getTranslationUnit();
    }

}
