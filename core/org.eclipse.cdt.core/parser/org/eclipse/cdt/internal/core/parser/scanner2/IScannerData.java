/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner2;

import java.util.Iterator;

import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.internal.core.parser.problem.IProblemFactory;

/**
 * @author jcamelon
 */
public interface IScannerData {
	public abstract IASTFactory getASTFactory();
	
	
	/**
	 * @return Returns the problemFactory.
	 */
	public abstract IProblemFactory getProblemFactory();
	/**
	 * @return Returns the language.
	 */
	public abstract ParserLanguage getLanguage();
	/**
	 * @return Returns the parserMode.
	 */
	public abstract ParserMode getParserMode();

	/**
	 * @return Returns the requestor.
	 */
	public abstract ISourceElementRequestor getClientRequestor();
	public abstract IParserLogService getLogService();
	
	public Iterator getWorkingCopies();
	/**
	 * @param restOfLine
	 * @param offset
	 * @return
	 */
//	public abstract InclusionDirective parseInclusionDirective(String restOfLine, int offset) throws InclusionParseException;
}