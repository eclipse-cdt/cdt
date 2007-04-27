/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.pdom.IndexerProgress;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * The base class for standalone index population tools.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * 
 * This class is not thread safe.
 * </p>
 * 
 * @since 4.0
 */
public abstract class StandaloneIndexer {
	
	/**
	 * Parser should not skip any references.
	 */
	public static final int SKIP_NO_REFERENCES= 0;
	
	/**
	 * Parser to skip all references.
	 */
	public static final int SKIP_ALL_REFERENCES= 1; 
	
	/**
	 * Parser to skp type references.
	 */
	public static final int SKIP_TYPE_REFERENCES= 2;
	
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
	protected static final List NO_TUS = new ArrayList();
	/**
	 * The IWritableIndex that stores all bindings and names.
	 */
	protected IWritableIndex fIndex;
	
	/**
	 * A flag that indiciates if all files (sources without config, headers not included)
	 * should be parsed.
	 */
	protected boolean fIndexAllFiles;
	
	/**
	 * Collection of valid file extensions for C/C++ source.
	 */
	protected Set fValidSourceUnitNames;
	
	/**
	 * The IScannerInfo that provides include paths and defined symbols.
	 */
	protected IScannerInfo fScanner;
	
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
	
	/**
	 * Files to parse up front.
	 */
	protected String[] fFilesToParseUpFront = new String[0];
	
	protected int fUpdateOptions = UPDATE_ALL;
	
	private IndexerProgress fProgress = null;
	private volatile StandaloneIndexerTask fDelegate;
	
	private static FilenameFilter DEFAULT_FILTER = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return true;
		}
	};
	
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
	 * Returns the collection of valid file extensions for C/C++ source.
	 * @return
	 */
	public Set getValidSourceUnitNames() {
		return fValidSourceUnitNames;
	}
	
	/**
	 * Sets the collection of valid file extensions for C/C++ source.
	 */
	public void setValidSourceUnitNames(Set validSourceUnitNames) {
		fValidSourceUnitNames = validSourceUnitNames;
	}
	
	/**
	 * Returns the IScannerInfo that provides include paths and defined symbols.
	 * @return
	 */
	public IScannerInfo getScannerInfo() {
		return fScanner;
	}
	
	/**
	 * Returns the ILanguageMapper that determines the ILanguage for a file.
	 * @return
	 */
	public ILanguageMapper getLanguageMapper() {
		return fMapper;
	}
	
	/**
	 * Returns the logger.
	 * @return
	 */
	public IParserLogService getParserLog() {
		return fLog;
	}
	
	/**
	 * Returns true if indexing activities should be shown.
	 * Otherwise, this method returns false.
	 * @return
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
	 * @return
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
	 * @return
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
		index.acquireWriteLock(0);
		try {
			index.clear();
		}
		finally {
			index.releaseWriteLock(0);
		}
	}
	
	/**
	 * Returns the progress information.
	 * @return
	 */
	public synchronized IndexerProgress getProgressInformation() {
		return fDelegate != null ? fDelegate.getProgressInformation() : fProgress;
	}
	
	/**
	 * Returns the update options specified.
	 * @return
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
	public void rebuild(List tus, IProgressMonitor monitor) throws IOException {
		fProgress = createProgress();
		
		try {
			clearIndex();
			fDelegate= createTask(getFilesAdded(tus), NO_TUS, NO_TUS);
			fDelegate.setUpdateFlags(fUpdateOptions);
			
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
		}
		
		if (fDelegate != null) {
			fDelegate.run(monitor);
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
	public void handleDelta(List added, List changed, List removed, IProgressMonitor monitor) throws IOException {
		fProgress= new IndexerProgress();
				
		fDelegate= createTask(getFilesAdded(added), changed, removed);
		if (fDelegate instanceof StandaloneIndexerTask) {
			fDelegate.setUpdateFlags(fUpdateOptions);
		}
		
		if (fDelegate != null) {
			fDelegate.run(monitor);
		}
		
	}
	
	/**
	 * Returns files that are being added to the index, skipping over files that 
	 * should not be excluded.
	 * @param tus
	 * @return
	 */
	private List getFilesAdded(List tus) {
		List added = new ArrayList();
		
		FilenameFilter filter = getExclusionFilter();
		if (filter == null) {
			filter = DEFAULT_FILTER;
		}
		
		Iterator iter = tus.iterator();
		while (iter.hasNext()) {
			String path = (String) iter.next();
			File file = new File(path);
			if (file.isDirectory()) {
				String[] files = file.list(filter);
				for (int i = 0; i < files.length; i++) {
					added.add((String)files[i]);
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
	protected abstract StandaloneIndexerTask createTask(List added, List changed, List removed);

	/**
	 * Return the type of references the parser should skip.
	 * @return
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
	 * Returns an array of files that should be parsed up front.
	 * @return
	 */
	public String[] getFilesToParseUpFront() {
		return fFilesToParseUpFront;
	}
	
	/**
	 * Sets an array of files that should be parsed up front.
	 * @param filesToParseUpFront
	 */
	public void setFilesToParseUpFront(String[] filesToParseUpFront) {
		fFilesToParseUpFront = filesToParseUpFront;
	}
	
	/**
	 * Returns the exclusion filter for this indexer.
	 * @return
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
}
