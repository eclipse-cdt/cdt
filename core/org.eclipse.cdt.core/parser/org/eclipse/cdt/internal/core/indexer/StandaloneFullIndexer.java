/*******************************************************************************
 * Copyright (c) 2006, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.cdt.internal.core.dom.AbstractCodeReaderFactory;
import org.eclipse.cdt.internal.core.index.WritableCIndex;
import org.eclipse.cdt.internal.core.pdom.WritablePDOM;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
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
@SuppressWarnings("deprecation")
public class StandaloneFullIndexer extends StandaloneIndexer {

	private AbstractCodeReaderFactory fCodeReaderFactory;

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
	 * @deprecated
	 */
	@Deprecated
	public StandaloneFullIndexer(File writableIndexFile, IIndexLocationConverter converter,
			Map<String, IPDOMLinkageFactory> linkageFactoryMappings, IScannerInfo scanner,
			FileEncodingRegistry fileEncodingRegistry, ILanguageMapper mapper, IParserLogService log,
			AbstractCodeReaderFactory codeReaderFactory) throws CoreException {
		super(new WritableCIndex(new WritablePDOM(writableIndexFile, converter, linkageFactoryMappings)), false, mapper,
				log, scanner, fileEncodingRegistry);
		fCodeReaderFactory = codeReaderFactory;
	}

	/**
	 * Create a full indexer.
	 * @param writableIndexFile - the file where the PDOM index is stored
	 * @param converter - a converter used to convert between String locations and IIndexLocations
	 * @param linkageFactoryMappings - all of the available IPDOMLinkageFactories the index can use during indexing
	 * @param scannerProvider - provides include paths and defined symbols
	 * @param mapper - a mapper used to determine ICLanguage for a particular file
	 * @param log - logger
	 * @param codeReaderFactory - factory that provides CodeReaders for files included
	 *                    		  by the source code being parsed.
	 * @throws CoreException
	 */
	public StandaloneFullIndexer(File writableIndexFile, IIndexLocationConverter converter,
			Map<String, IPDOMLinkageFactory> linkageFactoryMappings, IStandaloneScannerInfoProvider scannerProvider,
			FileEncodingRegistry fileEncodingRegistry, ILanguageMapper mapper, IParserLogService log,
			AbstractCodeReaderFactory codeReaderFactory) throws CoreException {
		super(new WritableCIndex(new WritablePDOM(writableIndexFile, converter, linkageFactoryMappings)), false, mapper,
				log, scannerProvider, fileEncodingRegistry);
		fCodeReaderFactory = codeReaderFactory;
	}

	/**
	 * Returns the factory that provides CodeReaders for files included
	 * by the source code being parsed.
	 */
	public AbstractCodeReaderFactory getCodeReaderFactory() {
		return fCodeReaderFactory;
	}

	/**
	 * Creates a delegate standalone indexing task
	 */
	@Override
	protected StandaloneIndexerTask createTask(List<String> added, List<String> changed, List<String> removed) {
		StandaloneIndexerTask task = new StandaloneFullIndexerTask(this, added, changed, removed);
		task.setLogService(getParserLog());
		return task;
	}

}
