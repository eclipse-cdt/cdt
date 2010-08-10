/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *	  IBM Corporation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.indexer;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.Iterator;

import com.ibm.icu.text.MessageFormat;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.model.AbstractLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.pdom.AbstractIndexerTask;
import org.eclipse.cdt.internal.core.pdom.IndexerProgress;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * A task for index updates.
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
public abstract class StandaloneIndexerTask extends AbstractIndexerTask {
	
	protected StandaloneIndexer fIndexer;
	protected IParserLogService fLogger;

	public static final int[] IDS_FOR_LINKAGES_TO_INDEX = {
		ILinkage.CPP_LINKAGE_ID, ILinkage.C_LINKAGE_ID, ILinkage.FORTRAN_LINKAGE_ID
	};
	
	protected StandaloneIndexerTask(StandaloneIndexer indexer, Collection<String> added, Collection<String> changed, Collection<String> removed, boolean isFast) {
		super(concat(added, changed), removed.toArray(), new StandaloneIndexerInputAdapter(indexer), isFast);
		fIndexer= indexer;
		setShowActivity(fIndexer.getShowActivity());
		setShowProblems(fIndexer.getShowProblems());
		setSkipReferences(fIndexer.getSkipReferences());
		
		if (getIndexAllFiles()) {
			setIndexFilesWithoutBuildConfiguration(true);
			setIndexHeadersWithoutContext(UnusedHeaderStrategy.useDefaultLanguage);
		}
		else {
			setIndexFilesWithoutBuildConfiguration(false);
			setIndexHeadersWithoutContext(UnusedHeaderStrategy.skip);
		}
	}
	
	private static Object[] concat(Collection<?> added, Collection<?> changed) {
		Object[] result= new Object[added.size() + changed.size()];
		int i=0;
		for (Iterator<?> iterator = added.iterator(); iterator.hasNext();) {
			result[i++]= iterator.next();
		}
		for (Iterator<?> iterator = changed.iterator(); iterator.hasNext();) {
			result[i++]= iterator.next();
		}
		return result;
	}

	/**
	 * Return the indexer.
	 */
	final public StandaloneIndexer getIndexer() {
		return fIndexer;
	}
	
	/**
	 * Return indexer's progress information.
	 */
	@Override
	final public IndexerProgress getProgressInformation() {
		return super.getProgressInformation();
	}
		
	
	/**
	 * Tells the parser which files to parse first
	 */
	final public void setParseUpFront() {
		setParseUpFront(fIndexer.getFilesToParseUpFront());
	}
	
	/**
	 * Figures out whether all files (sources without config, headers not included)
	 * should be parsed.
	 * @since 4.0
	 */
	final protected boolean getIndexAllFiles() {
		return getIndexer().getIndexAllFiles();
	}

	@Override
	final protected AbstractLanguage[] getLanguages(String filename) {
		ILanguage l = fIndexer.getLanguageMapper().getLanguage(filename);
		if (l instanceof AbstractLanguage) {
			return new AbstractLanguage[] {(AbstractLanguage) l};
		}
		return new AbstractLanguage[0];
	}
	
	@Override
	protected final IWritableIndex createIndex() {
		return fIndexer.getIndex();
	}

	
	public final void run(IProgressMonitor monitor) throws InterruptedException {
		long start = System.currentTimeMillis();
		runTask(monitor);
		traceEnd(start);
	}

	protected void traceEnd(long start) {
		if (fIndexer.getTraceStatistics()) {
			IndexerProgress info= getProgressInformation();
			String name= getClass().getName();
			name= name.substring(name.lastIndexOf('.')+1);

			trace(name + " "  //$NON-NLS-1$
					+ " (" + info.fCompletedSources + " sources, "  //$NON-NLS-1$ //$NON-NLS-2$
					+ info.fCompletedHeaders + " headers)"); //$NON-NLS-1$
			
			boolean allFiles= getIndexAllFiles();
			boolean skipRefs= fIndexer.getSkipReferences() == StandaloneIndexer.SKIP_ALL_REFERENCES;
			boolean skipTypeRefs= skipRefs || fIndexer.getSkipReferences() == StandaloneIndexer.SKIP_TYPE_REFERENCES;
			trace(name + " Options: "  //$NON-NLS-1$
					+ "parseAllFiles=" + allFiles //$NON-NLS-1$
					+ ",skipReferences=" + skipRefs //$NON-NLS-1$
					+ ", skipTypeReferences=" + skipTypeRefs //$NON-NLS-1$
					+ "."); //$NON-NLS-1$
			
			trace(name + " Timings: "  //$NON-NLS-1$
					+ (System.currentTimeMillis() - start) + " total, " //$NON-NLS-1$
					+ fStatistics.fParsingTime + " parser, " //$NON-NLS-1$
					+ fStatistics.fResolutionTime + " resolution, " //$NON-NLS-1$
					+ fStatistics.fAddToIndexTime + " index update."); //$NON-NLS-1$
			int sum= fStatistics.fDeclarationCount+fStatistics.fReferenceCount+fStatistics.fProblemBindingCount;
			double problemPct= sum==0 ? 0.0 : (double) fStatistics.fProblemBindingCount / (double) sum;
			NumberFormat nf= NumberFormat.getPercentInstance();
			nf.setMaximumFractionDigits(2);
			nf.setMinimumFractionDigits(2);
			trace(name + " Result: " //$NON-NLS-1$
					+ fStatistics.fDeclarationCount + " declarations, " //$NON-NLS-1$
					+ fStatistics.fReferenceCount + " references, " //$NON-NLS-1$
					+ fStatistics.fErrorCount + " errors, " //$NON-NLS-1$
					+ fStatistics.fProblemBindingCount + "(" + nf.format(problemPct) + ") problems.");  //$NON-NLS-1$ //$NON-NLS-2$
						
			IWritableIndex index = fIndexer.getIndex();
			if (index != null) {
				long misses= index.getCacheMisses();
				long hits= index.getCacheHits();
				long tries= misses+hits;
				double missPct= tries==0 ? 0.0 : (double) misses / (double) tries;
				trace(name + " Cache: " //$NON-NLS-1$
					+ hits + " hits, "  //$NON-NLS-1$
					+ misses + "(" + nf.format(missPct)+ ") misses."); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.pdom.AbstractIndexerTask#createStatus(java.lang.String)
	 */
	@Override
	protected IStatus createStatus(String msg) {
		return new Status(IStatus.ERROR, "org.eclipse.cdt.core", msg, null); //$NON-NLS-1$
	}
	
	

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.pdom.PDOMWriter#createStatus(java.lang.String, java.lang.Throwable)
	 */
	@Override
	protected IStatus createStatus(String msg, Throwable e) {
		return new Status(IStatus.ERROR, "org.eclipse.cdt.core", msg, e); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.pdom.AbstractIndexerTask#getMessage(org.eclipse.cdt.internal.core.pdom.AbstractIndexerTask.MessageKind, java.lang.Object[])
	 */
	@Override
	protected String getMessage(MessageKind kind, Object... arguments) {
		// unfortunately we don't have OSGi on the remote system so for now we'll just settle for
		// English strings
		// TODO: find a way to do non-OSGi NLS
		switch(kind) {
		case parsingFileTask:
			return MessageFormat.format("parsing {0} ({1})", arguments); //$NON-NLS-1$
			
		case errorWhileParsing:
			return MessageFormat.format("Error while parsing {0}.", arguments); //$NON-NLS-1$
			
		case tooManyIndexProblems:
			return "Too many errors while indexing, stopping indexer."; //$NON-NLS-1$
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.pdom.AbstractIndexerTask#getLogService()
	 */
	@Override
	protected IParserLogService getLogService() {
		if (fLogger != null)
			return fLogger;
		return new StdoutLogService();
	}
	
	protected void setLogService(IParserLogService logService){
		fLogger = logService;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.pdom.AbstractIndexerTask#logError(org.eclipse.core.runtime.IStatus)
	 */
	@Override
	protected void logError(IStatus s) {
		trace(s.getMessage());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.pdom.AbstractIndexerTask#logException(java.lang.Throwable)
	 */
	@Override
	protected void logException(Throwable e) {
		trace(e.getMessage());
	}

	@SuppressWarnings("deprecation")
	@Override
	protected IScannerInfo createDefaultScannerConfig(int linkageID) {
		IStandaloneScannerInfoProvider provider = fIndexer.getScannerInfoProvider();
		if(provider != null)
			return provider.getDefaultScannerInformation(linkageID);
		
		IScannerInfo scannerInfo = fIndexer.getScannerInfo();
		if(scannerInfo != null)
			return scannerInfo;
		
		return super.createDefaultScannerConfig(linkageID);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.pdom.AbstractIndexerTask#getLinkagesToParse()
	 */
	@Override
	protected int[] getLinkagesToParse() {
		return IDS_FOR_LINKAGES_TO_INDEX;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.pdom.PDOMWriter#trace(java.lang.String)
	 */
	@Override
	protected void trace(String message) {		
		getLogService().traceLog(message);
	}

}
