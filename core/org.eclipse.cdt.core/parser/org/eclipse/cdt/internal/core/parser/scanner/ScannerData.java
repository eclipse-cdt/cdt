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

import java.io.Reader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.internal.core.parser.ast.EmptyIterator;
import org.eclipse.cdt.internal.core.parser.problem.IProblemFactory;


public class ScannerData implements IScannerData
{
	private final List workingCopies;
	private final ContextStack contextStack;
	private IASTFactory astFactory = null;
	private final ISourceElementRequestor requestor;
	private final ParserMode parserMode;
	private final String filename;
	private final Reader reader;
	private final ParserLanguage language;
	private final IParserLogService log;
	private final IProblemFactory problemFactory = new ScannerProblemFactory();
	private Map definitions = new Hashtable();
	private BranchTracker branches = new BranchTracker();
	private final IScanner scanner;
	private final IScannerInfo originalConfig;
	private List includePathNames = new ArrayList();
	private static final Iterator EMPTY_ITERATOR = new EmptyIterator();
	private final Map privateDefinitions;
	/**
	 * @return Returns the contextStack.
	 */
	public ContextStack getContextStack() {
		return contextStack;
	}

	/**
	 * @param includePathNames The includePathNames to set.
	 */
	public void setIncludePathNames(List includePathNames) {
		this.includePathNames = includePathNames;
	}

	/**
	 * @return Returns the includePathNames.
	 */
	public List getIncludePathNames() {
		return includePathNames;
	}

	/**
	 * @return Returns the originalConfig.
	 */
	public IScannerInfo getOriginalConfig() {
		return originalConfig;
	}

	/**
	 * @return Returns the scanner.
	 */
	public IScanner getScanner() {
		return scanner;
	}

	public IASTFactory getASTFactory()
	{
		return astFactory;
	}
	
	public void setASTFactory( IASTFactory factory )
	{
		astFactory = factory;
	}
	
	public BranchTracker getBranchTracker()
	{
		return branches;
	}
	
	public Map getPublicDefinitions()
	{
		return definitions;
	}
	
	/**
	 * @return Returns the problemFactory.
	 */
	public IProblemFactory getProblemFactory() {
		return problemFactory;
	}

	/**
	 * @return Returns the filename.
	 */
	public String getInitialFilename() {
		return filename;
	}

	/**
	 * @return Returns the language.
	 */
	public ParserLanguage getLanguage() {
		return language;
	}

	/**
	 * @return Returns the parserMode.
	 */
	public ParserMode getParserMode() {
		return parserMode;
	}

	/**
	 * @return Returns the reader.
	 */
	public Reader getInitialReader() {
		return reader;
	}

	/**
	 * @return Returns the requestor.
	 */
	public ISourceElementRequestor getClientRequestor() {
		return requestor;
	}

	public ScannerData( IScanner scanner, IParserLogService log, 
			ISourceElementRequestor requestor, 
			ParserMode parserMode, 
			String filename, 
			Reader reader, 
			ParserLanguage language, IScannerInfo info, ContextStack stack, List workingCopies )
	{
		this.scanner = scanner;
		this.log = log;
		this.requestor = requestor;
		this.parserMode = parserMode;
		this.filename = filename;
		this.reader = reader;
		this.language = language;
		this.originalConfig = info;
		this.contextStack = stack;
		this.workingCopies = workingCopies;
		privateDefinitions = new Hashtable();
	}

	
	public IParserLogService getLogService()
	{
		return log;
	}

	/**
	 * @param empty_map
	 */
	public void setDefinitions(Map map) {
		definitions = map;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getWorkingCopies()
	 */
	public Iterator getWorkingCopies() {
		if( workingCopies != null )
			return workingCopies.iterator();
		return EMPTY_ITERATOR;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.IScannerData#getPrivateDefinitions()
	 */
	public Map getPrivateDefinitions() {
		return privateDefinitions;
	}
}