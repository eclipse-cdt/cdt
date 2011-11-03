/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 * 	  IBM Corporation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.indexer;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.index.IWritableIndexFragment;
import org.eclipse.cdt.internal.core.pdom.IndexerProgress;
import org.eclipse.cdt.internal.core.pdom.PDOMWriter;
import org.eclipse.cdt.internal.core.pdom.WritablePDOM;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * The base class for stand-alone index population tools.
 * 
 * <p>
 * This class is not thread safe.
 * </p>
 */
public abstract class StandaloneIndexer {
	
	/**
	 * Parser should not skip any references.
	 */
	public static final int SKIP_NO_REFERENCES= PDOMWriter.SKIP_NO_REFERENCES;
	
	/**
	 * Parser to skip all references.
	 */
	public static final int SKIP_ALL_REFERENCES= PDOMWriter.SKIP_ALL_REFERENCES; 
	
	/**
	 * Parser to skip implicit references.
	 */
	public static final int SKIP_IMPLICIT_REFERENCES= PDOMWriter.SKIP_IMPLICIT_REFERENCES;

	/**
	 * Parser to skip type references.
	 */
	public static final int SKIP_TYPE_REFERENCES= PDOMWriter.SKIP_TYPE_REFERENCES;
	
	/**
	 * Parser to skip type references.
	 */
	public static final int SKIP_MACRO_REFERENCES= PDOMWriter.SKIP_MACRO_REFERENCES;
	
	/**
	 * Constant for indicating to update all translation units.
	 */
	public final static int UPDATE_ALL= 0x1;

	/**
	 * Constant for indicating to update translation units if their timestamp
	 * has changed.
	 */
	public final static int UPDATE_CHECK_TIMESTAMPS= 0x2;
		
	/**
	 * Empty list.
	 */
	protected static final List<String> NO_TUS = Collections.emptyList();
	
	
	/**
	 * The IWritableIndex that stores all bindings and names.
	 */
	protected IWritableIndex fIndex;
	
	/**
	 * A flag that indicates if all files (sources without config, headers not included)
	 * should be parsed.
	 */
	protected boolean fIndexAllFiles;
	
	/**
	 * Collection of valid file extensions for C/C++ source.
	 */
	protected Set<String> fValidSourceUnitNames;
	
	/**
	 * The IScannerInfo that provides include paths and defined symbols.
	 * Either a single scanner info or a IStandaloneScannerInfoProvider must
	 * be provided, but not both. If a single IScannerInfo object is provided
	 * it will always be used. Otherwise the provider will be used.
	 */
	@Deprecated
	protected IScannerInfo fScanner;
	
	/**
	 * Creates IScannerInfo objects from file paths, allows there
	 * to be separate scanner infos for specific files and folders.
	 */
	protected IStandaloneScannerInfoProvider fScannerInfoProvider;
	
	/**
	 * The ILanguageMapper that determines the ILanguage for a file.
	 */
	protected ILanguageMapper fMapper;
	
	/**
	 * The logger during parsing.
	 */
	protected IParserLogService fLog;
	
	/**
	 * A flag that indicates if all activities during indexing should be shown.
	 */
	protected boolean fShowActivity;
	
	/**
	 * A flag that indicates if any problems encountered during indexing.
	 * should be shown.
	 */
	protected boolean fShowProblems;
	
	/**
	 * A flag that indicates if statistics should be gathered during indexing.
	 */
	protected boolean fTraceStatistics;
	
	/**
	 * The type of references the parser should skip.
	 */
	protected int fSkipReferences = SKIP_NO_REFERENCES;
	
	/**
	 * The exclusion filter that skips over files that should not be indexed.
	 */
	protected FilenameFilter fExclusionFilter;
	
	protected int fUpdateOptions = UPDATE_ALL;
	
	private IndexerProgress fProgress = null;
	private volatile StandaloneIndexerTask fDelegate;
	
	private static FilenameFilter DEFAULT_FILTER = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return true;
		}
	};
	
	protected FileEncodingRegistry fFileEncodingRegistry;
	
	/**
	 * @deprecated Its better to provide a scanner info provider instead.
	 */
	@Deprecated
	public StandaloneIndexer(IWritableIndex index, boolean indexAllFiles,  
			                 ILanguageMapper mapper, IParserLogService log, IScannerInfo scanner, FileEncodingRegistry fileEncodingRegistry) {
		fIndex = index;
		fIndexAllFiles = indexAllFiles;
		fMapper = mapper;
		fLog = log;	
		fScanner = scanner;
		fScannerInfoProvider = null;
		fFileEncodingRegistry = fileEncodingRegistry;
		setupASTFilePathResolver();
	}
	
	
	public StandaloneIndexer(IWritableIndex index, boolean indexAllFiles,  
            ILanguageMapper mapper, IParserLogService log, IStandaloneScannerInfoProvider scannerProvider, FileEncodingRegistry fileEncodingRegistry) {
		fIndex = index;
		fIndexAllFiles = indexAllFiles;
		fMapper = mapper;
		fLog = log;	
		fScanner = null;
		fScannerInfoProvider = scannerProvider;
		fFileEncodingRegistry = fileEncodingRegistry;
		setupASTFilePathResolver();
	}
	
	
	private void setupASTFilePathResolver() {
		IWritableIndexFragment fragment = getIndex().getWritableFragment();
		if(fragment instanceof WritablePDOM) {
			WritablePDOM pdom = (WritablePDOM)fragment;
			pdom.setASTFilePathResolver(new StandaloneIndexerInputAdapter(this));
		}
	}
	
	public void setScannerInfoProvider(IStandaloneScannerInfoProvider provider) {
		fScannerInfoProvider = provider;
		fScanner = null;
	}
	
	/**
	 * Returns the index.
	 * @return the IWritable index the indexer is writing to
	 */
	public IWritableIndex getIndex() {
		return fIndex;
	}
	
	/**
	 * Returns true if all files (sources without config, headers not included)
	 * should be parsed.  Otherwise, this method returns false.
	 */
	public boolean getIndexAllFiles() {
		return fIndexAllFiles;
	}
	
	/**
	 * If true then all files will be indexed.
	 */
	public void setIndexAllFiles(boolean indexAllFiles) {
		fIndexAllFiles = indexAllFiles;
	}
	
	/**
	 * Returns the collection of valid file extensions for C/C++ source.
	 */
	public Set<String> getValidSourceUnitNames() {
		return fValidSourceUnitNames;
	}
	
	/**
	 * Sets the collection of valid file extensions for C/C++ source.
	 */
	public void setValidSourceUnitNames(Set<String> validSourceUnitNames) {
		fValidSourceUnitNames = validSourceUnitNames;
	}
	
	/**
	 * Returns the IScannerInfo that provides include paths and defined symbols.
	 * @deprecated Should probably be using a IStandaloneScannerInfoProvider instead and
	 * calling getScannerInfo(String).
	 */
	@Deprecated
	public IScannerInfo getScannerInfo() {
		return fScanner;
	}
	
	
	/**
	 * Returns the IScannerInfo for the given path.
	 * If the current instance was created with an IScannerInfo instead of
	 * an IScannerInfoProvider then the path will be ignored and
	 * that IScannerInfo will always be returned.
	 */
	public IScannerInfo getScannerInfo(String path) {
		if(fScanner != null)
			return fScanner;
		
		return fScannerInfoProvider.getScannerInformation(path);
	}


	/**
	 * Returns the IStandaloneScannerInfoProvider or null if one was not provided.
	 */
	public IStandaloneScannerInfoProvider getScannerInfoProvider() {
		return fScannerInfoProvider;
	}
	
	/**
	 * Returns the ILanguageMapper that determines the ILanguage for a file.
	 */
	public ILanguageMapper getLanguageMapper() {
		return fMapper;
	}
	
	
	public void setLanguageMapper(ILanguageMapper mapper) {
		fMapper = mapper;
	}
	
	/**
	 * Returns the logger.
	 */
	public IParserLogService getParserLog() {
		return fLog;
	}
	
	/**
	 * Sets the logger.
	 */
	public void setParserLog(IParserLogService log) {
		fLog = log;
	}
	
	/**
	 * Returns true if indexing activities should be shown.
	 * Otherwise, this method returns false.
	 */
	public boolean getShowActivity() {
		return fShowActivity;
	}
	
	/**
	 * Tells indexer if indexing activities should be shown.
	 */
	public void setShowActivity(boolean showActivity) {
		fShowActivity = showActivity;
	}
	
	/**
	 * Returns true if problems during indexing should be shown.
	 * Otherwise, this method returns false.
	 */
	public boolean getShowProblems() {
		return fShowProblems;
	}
	
	/**
	 * Tells indexer if problems during indexing should be shown.
	 */
	public void setShowProblems(boolean showProblems) {
		fShowProblems = showProblems;
	}
	
	/**
	 * Returns true if statistics should be gathered during indexing.
	 * Otherwise, this method returns false..
	 */
	public boolean getTraceStatistics() {
		return fTraceStatistics;
	}
	
	/**
	 * Tells indexer if statistics should be gathered during indexing.
	 */
	public void setTraceStatistics(boolean traceStatistics) {
		fTraceStatistics = traceStatistics;
	}
	
	private IndexerProgress createProgress() {
		IndexerProgress progress= new IndexerProgress();
		progress.fTimeEstimate= 1000;
		return progress;
	}
	
	private void clearIndex() throws CoreException, InterruptedException {
		IWritableIndex index= getIndex();
		// First clear the pdom
		index.acquireWriteLock();
		try {
			index.clear();
		}
		finally {
			index.releaseWriteLock();
		}
	}
	
	/**
	 * Returns the progress information.
	 */
	public synchronized IndexerProgress getProgressInformation() {
		return fDelegate != null ? fDelegate.getProgressInformation() : fProgress;
	}
	
	/**
	 * Returns the update options specified.
	 */
	public int getUpdateOptions() {
		return fUpdateOptions;
	}
	
	/**
	 * Specifies the update options, whether all translation units should be updated or only the ones
	 * with timestamp changes.
	 * @param options
	 */
	public void setUpdateOptions (int options) {
		fUpdateOptions = options;
	}
		
	/**
	 * Clears the index and rebuild
	 * @param tus - directories/files to be added to index
	 * @param monitor
	 * @throws IOException
	 */
	public void rebuild(List<String> tus, IProgressMonitor monitor) throws IOException {
		fProgress = createProgress();
		
		try {
			clearIndex();
			fDelegate= createTask(getFilesAdded(tus), NO_TUS, NO_TUS);
			fDelegate.setUpdateFlags(fUpdateOptions);
			
			if (fDelegate != null) {
				fDelegate.run(monitor);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
		}
	}
	
	/**
	 * Updates the index with changes.
	 * @param added - directories/files to be added to the index
	 * @param changed - files that have been changed
	 * @param removed - files to be removed from the index
	 * @param monitor
	 * @throws IOException
	 */
	public void handleDelta(List<String> added, List<String> changed, List<String> removed, IProgressMonitor monitor) throws IOException {
		fProgress= new IndexerProgress();
				
		fDelegate= createTask(getFilesAdded(added), changed, removed);
		if (fDelegate != null) {
			try {
				fDelegate.setUpdateFlags(fUpdateOptions);
				fDelegate.run(monitor);
			} catch (InterruptedException e) {
			}
		}
		
	}
	
	/**
	 * Returns files that are being added to the index, skipping over files that 
	 * should not be excluded.
	 * @param tus
	 * @return
	 */
	private List<String> getFilesAdded(List<String> tus) {
		List<String> added = new ArrayList<String>();
		
		FilenameFilter filter = getExclusionFilter();
		if (filter == null) {
			filter = DEFAULT_FILTER;
		}
		
		Iterator<String> iter = tus.iterator();
		while (iter.hasNext()) {
			String path = iter.next();
			File file = new File(path);
			if (file.isDirectory()) {
				String[] files = file.list(filter);
				for (String file2 : files) {
					added.add(file2);
				}
			}
			else {				
				if (filter.accept(file.getParentFile(), file.getName())) {
					added.add(path);
				}
			}
		}
		return added;
	}
	
	/**
	 * Creates a delegate standalone indexing task
	 */
	protected abstract StandaloneIndexerTask createTask(List<String> added, List<String> changed, List<String> removed);

	/**
	 * Return the type of references the parser should skip.
	 */
	public int getSkipReferences() {
		return fSkipReferences;
	}
	
	/**
	 * Sets the type of references the parser should skip.
	 * @param skipReferences
	 */
	public void setSkipReferences(int skipReferences) {
		fSkipReferences = skipReferences;
	}

	/**
	 * Returns the exclusion filter for this indexer.
	 */
	public FilenameFilter getExclusionFilter() {
		return fExclusionFilter;
	}
	
	/**
	 * Sets the exclusion filter that tells the indexer to skip over 
	 * files that should not be indexed.
	 * @param exclusionFilter
	 */
	public void setExclusionFilter(FilenameFilter exclusionFilter) {
		fExclusionFilter = exclusionFilter;
	}


	public FileEncodingRegistry getFileEncodingRegistry() {
		return fFileEncodingRegistry;
	}


	public void setFileEncodingRegistry(FileEncodingRegistry fileEncodingRegistry) {
		fFileEncodingRegistry = fileEncodingRegistry;
	}
	
	
	
	
}
