/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser;

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserFactoryError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.extension.ExtensionDialect;
import org.eclipse.cdt.core.parser.extension.IParserExtensionFactory;

/**
 * @author jcamelon
 */
public class InternalParserUtil extends ParserFactory {

	private static IParserExtensionFactory extensionFactory = new ParserExtensionFactory( ExtensionDialect.GCC );
	
	public static IExpressionParser createExpressionParser( IScanner scanner, ParserLanguage language, IParserLogService log ) throws ParserFactoryError
	{
		if( scanner == null ) throw new ParserFactoryError( ParserFactoryError.Kind.NULL_SCANNER );
		if( language == null ) throw new ParserFactoryError( ParserFactoryError.Kind.NULL_LANGUAGE );
		IParserLogService logService = ( log == null ) ? createDefaultLogService() : log;
		return new ExpressionParser( scanner, language, logService, extensionFactory.createParserExtension() );
	}

	/**
	 * @param finalPath
	 * @return
	 */
	public static CodeReader createFileReader(String finalPath) {
		File includeFile = new File(finalPath);
		if (includeFile.exists() && includeFile.isFile()) 
		{
			try {
				return new CodeReader(finalPath);
			} catch (IOException e) {
			}
		}
		return null;
	}
}
