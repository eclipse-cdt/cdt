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
package org.eclipse.cdt.internal.core.parser.scanner;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.internal.core.parser.problem.IProblemFactory;
import org.eclipse.cdt.internal.core.parser.scanner.ScannerUtility.InclusionDirective;
import org.eclipse.cdt.internal.core.parser.scanner.ScannerUtility.InclusionParseException;

/**
 * @author jcamelon
 */
public interface IScannerData {
	public Map getFileCache();
	/**
	 * @return Returns the contextStack.
	 */
	public abstract ContextStack getContextStack();
	/**
	 * @param includePathNames The includePathNames to set.
	 */
	public abstract void setIncludePathNames(List includePathNames);
	/**
	 * @return Returns the includePathNames.
	 */
	public abstract List getIncludePathNames();
	/**
	 * @return Returns the originalConfig.
	 */
	public abstract IScannerInfo getOriginalConfig();
	/**
	 * @return Returns the scanner.
	 */
	public abstract IScanner getScanner();
	public abstract IASTFactory getASTFactory();
	public abstract void setASTFactory(IASTFactory factory);
	public abstract BranchTracker getBranchTracker();
	public abstract Map getPublicDefinitions();
	public Map getPrivateDefinitions();
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
	 * @return Returns the reader.
	 */
	public abstract CodeReader getInitialReader();
	/**
	 * @return Returns the requestor.
	 */
	public abstract ISourceElementRequestor getClientRequestor();
	public abstract IParserLogService getLogService();
	/**
	 * @param empty_map
	 */
	public abstract void setDefinitions(Map map);
	
	public Iterator getWorkingCopies();
	/**
	 * @param restOfLine
	 * @param offset
	 * @return
	 */
	public abstract InclusionDirective parseInclusionDirective(String restOfLine, int offset) throws InclusionParseException;
}