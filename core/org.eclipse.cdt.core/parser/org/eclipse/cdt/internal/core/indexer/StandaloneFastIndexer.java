/*******************************************************************************
 * Copyright (c) 2006, 2010 QNX Software Systems and others.
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

import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.WritableCIndex;
import org.eclipse.cdt.internal.core.pdom.WritablePDOM;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
import org.eclipse.core.runtime.CoreException;

/**
 * A standalone tool for populating an index.  This indexer optimizes for 
 * speed at the expense of accuracy.
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
public class StandaloneFastIndexer extends StandaloneIndexer {	
	
	/**
	 * Construct a fast standalone indexer.
	 * @param writableIndexFile - the file where the PDOM index is stored
	 * @param converter - a converter used to convert between String locations and IIndexLocations
	 * @param linkageFactoryMappings - all of the available IPDOMLinkageFactories the index can use during indexing
	 * @param scanner - provides include paths and defined symbols
	 * @param mapper - a mapper used to determine ICLanguage for a particular file
	 * @param log - logger
	 * @throws CoreException
	 * @deprecated
	 */
	@Deprecated
	public StandaloneFastIndexer(File writableIndexFile, IIndexLocationConverter converter, Map<String, IPDOMLinkageFactory> linkageFactoryMappings,
			IScannerInfo scanner, FileEncodingRegistry fileEncodingRegistry, ILanguageMapper mapper, IParserLogService log) throws CoreException {
		super(new WritableCIndex(new WritablePDOM(writableIndexFile, converter, linkageFactoryMappings),new IIndexFragment[0]), 
				false, mapper, log, scanner, fileEncodingRegistry);
	
	}
	
	/**
	 * Construct a fast standalone indexer.
	 * @param writableIndexFile - the file where the PDOM index is stored
	 * @param converter - a converter used to convert between String locations and IIndexLocations
	 * @param linkageFactoryMappings - all of the available IPDOMLinkageFactories the index can use during indexing
	 * @param scannerProvider - provides include paths and defined symbols
	 * @param mapper - a mapper used to determine ICLanguage for a particular file
	 * @param log - logger
	 * @throws CoreException
	 */
	public StandaloneFastIndexer(File writableIndexFile, IIndexLocationConverter converter, Map<String, IPDOMLinkageFactory> linkageFactoryMappings,
			IStandaloneScannerInfoProvider scannerProvider, FileEncodingRegistry fileEncodingRegistry, ILanguageMapper mapper, IParserLogService log) throws CoreException {
		super(new WritableCIndex(new WritablePDOM(writableIndexFile, converter, linkageFactoryMappings),new IIndexFragment[0]), 
				false, mapper, log, scannerProvider, fileEncodingRegistry);
	
	}
	
	/**
	 * Construct a fast standalone indexer.
	 * @param writableIndexFile - the file where the PDOM index is stored
	 * @param converter - a converter used to convert between String locations and IIndexLocations
	 * @param linkageFactoryMappings - all of the available IPDOMLinkageFactories the index can use during indexing
	 * @param mapper - a mapper used to determine ICLanguage for a particular file
	 * @param log - logger
	 * @throws CoreException
	 */
	public StandaloneFastIndexer(File writableIndexFile, IIndexLocationConverter converter, Map<String, IPDOMLinkageFactory> linkageFactoryMappings,
			FileEncodingRegistry fileEncodingRegistry, ILanguageMapper mapper, IParserLogService log) throws CoreException {
		super(new WritableCIndex(new WritablePDOM(writableIndexFile, converter, linkageFactoryMappings),new IIndexFragment[0]), 
				false, mapper, log, (IStandaloneScannerInfoProvider)null, fileEncodingRegistry);
	
	}
	
	
	/**
	 * Create a delegate standalone indexing task
	 */
	@Override
	protected StandaloneIndexerTask createTask(List<String> added, List<String> changed, List<String> removed) {
		StandaloneIndexerTask task = new StandaloneFastIndexerTask(this, added, changed, removed);
		task.setLogService(getParserLog());
		return task;
	}


}
