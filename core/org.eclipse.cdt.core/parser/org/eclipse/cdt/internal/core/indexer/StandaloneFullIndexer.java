/*******************************************************************************
 * Copyright (c) 2006, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * IBM Corporation
 *******************************************************************************/
 
package org.eclipse.cdt.internal.core.indexer;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IWritableIndexFragment;
import org.eclipse.cdt.internal.core.index.WritableCIndex;
import org.eclipse.cdt.internal.core.pdom.WritablePDOM;
import org.eclipse.core.runtime.CoreException;

/**
 * A standalone tool for populating an index.  This indexer optimizes for 
 * accuracy so it may be slower than the StandaloneFastIndexer.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * </p>
 * 
 * @since 4.0
 */
public class StandaloneFullIndexer extends StandaloneIndexer{
	
	private ICodeReaderFactory fCodeReaderFactory;
	
	/**
	 * Create a full indexer.
	 * @param writableIndexFile - the file where the PDOM index is stored
	 * @param converter - a converter used to convert between String locations and IIndexLocations
	 * @param linkageFactoryMappings - all of the available IPDOMLinkageFactories the index can use during indexing
	 * @param scanner - provides include paths and defined symbols
	 * @param mapper - a mapper used to determine ICLanguage for a particular file
	 * @param log - logger
	 * @param codeReaderFactory - factory that provides CodeReaders for files included
	 *                    		  by the source code being parsed.
	 * @throws CoreException
	 */
	public StandaloneFullIndexer(File writableIndexFile, IIndexLocationConverter converter, Map linkageFactoryMappings,
			IScannerInfo scanner, ILanguageMapper mapper, IParserLogService log, ICodeReaderFactory codeReaderFactory) throws CoreException {
		WritablePDOM pdom = new WritablePDOM(writableIndexFile, converter, linkageFactoryMappings);
		fIndex = new WritableCIndex(
				new IWritableIndexFragment[] { pdom },
				new IIndexFragment[0]);		
		fIndexAllFiles = false;
		fScanner = scanner;
		fMapper = mapper;
		fCodeReaderFactory = codeReaderFactory;
		fLog = log;
	}
	
	/**
	 * Returns the factory that provides CodeReaders for files included
	 * by the source code being parsed.
	 * @return
	 */
	public ICodeReaderFactory getCodeReaderFactory() {
		return fCodeReaderFactory;
	}
	
	/**
	 * Creates a delegate standalone indexing task
	 */
	protected StandaloneIndexerTask createTask(List added, List changed, List removed) {
		return new StandaloneFullIndexerTask(this, added, changed, removed);
	}

}
