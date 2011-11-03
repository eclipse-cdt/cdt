/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.indexer;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashSet;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.index.IWritableIndexManager;
import org.eclipse.cdt.internal.core.pdom.AbstractIndexerTask;
import org.eclipse.cdt.internal.core.pdom.ITodoTaskUpdater;
import org.eclipse.cdt.internal.core.pdom.IndexerProgress;
import org.eclipse.cdt.internal.core.pdom.db.ChunkCache;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import com.ibm.icu.text.NumberFormat;

/**
 * Configures the abstract indexer task suitable for indexing projects.
 */
public abstract class PDOMIndexerTask extends AbstractIndexerTask implements IPDOMIndexerTask {
	private static final String TRUE = "true"; //$NON-NLS-1$
	
	private AbstractPDOMIndexer fIndexer;
	private boolean fWriteInfoToLog;
	
	protected PDOMIndexerTask(ITranslationUnit[] forceFiles, ITranslationUnit[] updateFiles, ITranslationUnit[] removeFiles,
			AbstractPDOMIndexer indexer, boolean isFastIndexer) {
		super(concat(forceFiles, updateFiles), removeFiles, new ProjectIndexerInputAdapter(indexer.getProject()), isFastIndexer);
		fIndexer= indexer;
		setShowActivity(checkDebugOption(TRACE_ACTIVITY, TRUE));
		setShowInclusionProblems(checkDebugOption(TRACE_INCLUSION_PROBLEMS, TRUE));
		setShowScannerProblems(checkDebugOption(TRACE_SCANNER_PROBLEMS, TRUE));
		setShowSyntaxProblems(checkDebugOption(TRACE_SYNTAX_PROBLEMS, TRUE));
		setShowProblems(checkDebugOption(TRACE_PROBLEMS, TRUE));
		final long limit = getIntProperty(IndexerPreferences.KEY_SKIP_FILES_LARGER_THAN_MB, 0);
		setFileSizeLimit(limit * 1024 * 1024);
		if (checkProperty(IndexerPreferences.KEY_SKIP_ALL_REFERENCES)) {
			setSkipReferences(SKIP_ALL_REFERENCES);
		} else {
			int skipRefs= 0;
			if (checkProperty(IndexerPreferences.KEY_SKIP_IMPLICIT_REFERENCES)) {
				skipRefs |= SKIP_IMPLICIT_REFERENCES;
			}
			if (checkProperty(IndexerPreferences.KEY_SKIP_TYPE_REFERENCES)) {
				skipRefs |= SKIP_TYPE_REFERENCES;
			}
			if (checkProperty(IndexerPreferences.KEY_SKIP_MACRO_REFERENCES)) {
				skipRefs |= SKIP_MACRO_REFERENCES;
			}
			if (skipRefs != 0) {
				setSkipReferences(skipRefs);
			}
		}
		if (checkProperty(IndexerPreferences.KEY_INDEX_ALL_FILES)) {
			setIndexFilesWithoutBuildConfiguration(true);
			boolean i1= checkProperty(IndexerPreferences.KEY_INDEX_UNUSED_HEADERS_WITH_DEFAULT_LANG);
			boolean i2= checkProperty(IndexerPreferences.KEY_INDEX_UNUSED_HEADERS_WITH_ALTERNATE_LANG);
			UnusedHeaderStrategy strategy;
			if (i1) {
				strategy= i2 ? UnusedHeaderStrategy.useBoth : UnusedHeaderStrategy.useDefaultLanguage;
			} else {
				strategy= i2 ? UnusedHeaderStrategy.useAlternateLanguage: UnusedHeaderStrategy.skip;
			}
			setIndexHeadersWithoutContext(strategy);
		} else {
			setIndexFilesWithoutBuildConfiguration(false);
			setIndexHeadersWithoutContext(UnusedHeaderStrategy.skip);
		}
		setUpdateFlags(IIndexManager.UPDATE_CHECK_TIMESTAMPS | IIndexManager.UPDATE_CHECK_CONTENTS_HASH);
		setForceFirstFiles(forceFiles.length);
	}
	
	private static ITranslationUnit[] concat(ITranslationUnit[] added, ITranslationUnit[] changed) {
		HashSet<ITranslationUnit> union = new HashSet<ITranslationUnit>(added.length + changed.length);
		union.addAll(Arrays.asList(added));
		union.addAll(Arrays.asList(changed));
		final ITranslationUnit[] result = union.toArray(new ITranslationUnit[union.size()]);
		Arrays.sort(result, new Comparator<ITranslationUnit>() {
			@Override
			public int compare(ITranslationUnit o1, ITranslationUnit o2) {
				IResource res1= o1.getResource();
				IResource res2= o2.getResource();
				if (res1 != null && res2 != null) {
					return compare(res1.getFullPath().segments(), res2.getFullPath().segments());
				}
				return res1 != null ? -1 : res2 != null ? 1 : 0;
			}

			private int compare(String[] s1, String[] s2) {
				int max= Math.min(s1.length, s2.length) - 1;
				for (int i = 0; i < max; i++) {
					int cmp= s1[i].compareTo(s2[i]);
					if (cmp != 0)
						return cmp;
				}
				int cmp = s1.length-s2.length;
				if (cmp != 0)
					return cmp;
				return s1[max].compareTo(s2[max]);
			}
		});
		return result;
	}
	
	@Override
	public final IPDOMIndexer getIndexer() {
		return fIndexer;
	}
	
	@Override
	public final void run(IProgressMonitor monitor) throws InterruptedException {
		long start = System.currentTimeMillis();
		runTask(monitor);
		traceEnd(start, fIndex, monitor.isCanceled());
	}
	
	/**
	 * Checks whether a given debug option is enabled. See {@link IPDOMIndexerTask}
	 * for valid values.
	 * @since 4.0
	 */
	public static boolean checkDebugOption(String option, String value) {
		String trace= Platform.getDebugOption(option);
		boolean internallyActivated= Boolean.getBoolean(option);
		return internallyActivated || (trace != null && trace.equalsIgnoreCase(value));
	}

	private boolean checkProperty(String key) {
		return TRUE.equals(getIndexer().getProperty(key));
	}

	private int getIntProperty(String key, int defaultValue) {
		final String value = getIndexer().getProperty(key);
		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
			}
		}
		return defaultValue;
	}

	private ICProject getProject() {
		return getIndexer().getProject();
	}

	@Override
	protected final IWritableIndex createIndex() {
		try {
			return ((IWritableIndexManager) CCorePlugin.getIndexManager()).getWritableIndex(getProject());
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	@Override
	protected final ITodoTaskUpdater createTodoTaskUpdater() {
		return new TodoTaskUpdater();
	}
	
	protected void traceEnd(long start, IWritableIndex index, boolean wasCancelled) {
		// log entry
		if (fWriteInfoToLog && !wasCancelled && index != null) {
			final long totalTime = System.currentTimeMillis() - start;
			final IndexerProgress info= getProgressInformation();
			final int sum= fStatistics.fDeclarationCount + fStatistics.fReferenceCount + fStatistics.fProblemBindingCount;
			final double problemPct= sum == 0 ? 0.0 : (double) fStatistics.fProblemBindingCount / (double) sum;
			NumberFormat nfGroup= NumberFormat.getNumberInstance();
			nfGroup.setGroupingUsed(true);
			NumberFormat nfPercent= NumberFormat.getPercentInstance();
			nfPercent.setMaximumFractionDigits(2);
			nfPercent.setMinimumFractionDigits(2);
			NumberFormat nfTime= NumberFormat.getNumberInstance();
			nfTime.setMaximumFractionDigits(2);
			nfTime.setMinimumFractionDigits(2);
			nfTime.setGroupingUsed(true);
			final String msg= NLS.bind(Messages.PDOMIndexerTask_indexerInfo,
					new Object[] {
						getCProject().getElementName(),
						nfGroup.format(info.fCompletedSources),
						nfGroup.format(info.fCompletedHeaders),
						nfTime.format((double) totalTime / 1000),
						nfGroup.format(fStatistics.fDeclarationCount),
						nfGroup.format(fStatistics.fReferenceCount),
						nfGroup.format(fStatistics.fUnresolvedIncludesCount),
						nfGroup.format(fStatistics.fPreprocessorProblemCount + fStatistics.fSyntaxProblemsCount),
						nfGroup.format(fStatistics.fProblemBindingCount),
						nfPercent.format(problemPct)
					}
			);
			CCorePlugin.getDefault().getLog().log(new Status(IStatus.INFO, CCorePlugin.PLUGIN_ID, msg));
		}
		
		// tracing
		if (checkDebugOption(IPDOMIndexerTask.TRACE_STATISTICS, TRUE)) {
			String ident= "   ";   //$NON-NLS-1$
			final long totalTime = System.currentTimeMillis() - start;
			final IndexerProgress info= getProgressInformation();
			final int sum= fStatistics.fDeclarationCount + fStatistics.fReferenceCount + fStatistics.fProblemBindingCount;
			final double problemPct= sum == 0 ? 0.0 : (double) fStatistics.fProblemBindingCount / (double) sum;
			String kind= getIndexer().getClass().getName();
			kind= kind.substring(kind.lastIndexOf('.') + 1);
			final long dbSize= index.getDatabaseSizeBytes();
			
			System.out.println("C/C++ Indexer: Project '" + getProject().getElementName()     //$NON-NLS-1$
					+ "' (" + info.fCompletedSources + " sources, "      //$NON-NLS-1$//$NON-NLS-2$
					+ info.fCompletedHeaders + " headers)");    //$NON-NLS-1$
			boolean skipRefs= checkProperty(IndexerPreferences.KEY_SKIP_ALL_REFERENCES);
			boolean skipImplRefs= skipRefs || checkProperty(IndexerPreferences.KEY_SKIP_IMPLICIT_REFERENCES);
			boolean skipTypeRefs= skipRefs || checkProperty(IndexerPreferences.KEY_SKIP_TYPE_REFERENCES);
			boolean skipMacroRefs= skipRefs || checkProperty(IndexerPreferences.KEY_SKIP_MACRO_REFERENCES);
			System.out.println(ident + " Options: "     //$NON-NLS-1$
					+ "indexer='" + kind    //$NON-NLS-1$
					+ "', parseAllFiles=" + indexFilesWithoutConfiguration()    //$NON-NLS-1$
					+ ", unusedHeaders=" + getIndexHeadersWithoutContext()    //$NON-NLS-1$
					+ ", skipReferences=" + skipRefs    //$NON-NLS-1$
					+ ", skipImplicitReferences=" + skipImplRefs    //$NON-NLS-1$
					+ ", skipTypeReferences=" + skipTypeRefs    //$NON-NLS-1$
					+ ", skipMacroReferences=" + skipMacroRefs    //$NON-NLS-1$
					+ ".");    //$NON-NLS-1$
			System.out.println(ident + " Database: " + dbSize + " bytes");   //$NON-NLS-1$ //$NON-NLS-2$
			System.out.println(ident + " Timings: "     //$NON-NLS-1$
					+ totalTime + " total, "    //$NON-NLS-1$
					+ fStatistics.fParsingTime + " parser, "    //$NON-NLS-1$
					+ fStatistics.fResolutionTime + " resolution, "    //$NON-NLS-1$
					+ fStatistics.fAddToIndexTime + " index update.");    //$NON-NLS-1$
			System.out.println(ident + " Errors: "    //$NON-NLS-1$
					+ fStatistics.fErrorCount + " internal, "    //$NON-NLS-1$
					+ fStatistics.fUnresolvedIncludesCount + " include, "     //$NON-NLS-1$
					+ fStatistics.fPreprocessorProblemCount + " scanner, "     //$NON-NLS-1$
					+ fStatistics.fSyntaxProblemsCount + " syntax errors.");    //$NON-NLS-1$

			NumberFormat nfPercent= NumberFormat.getPercentInstance();
			nfPercent.setMaximumFractionDigits(2);
			nfPercent.setMinimumFractionDigits(2);
			System.out.println(ident + " Names: "    //$NON-NLS-1$
					+ fStatistics.fDeclarationCount + " declarations, "    //$NON-NLS-1$
					+ fStatistics.fReferenceCount + " references, "    //$NON-NLS-1$
					+ fStatistics.fProblemBindingCount + "(" + nfPercent.format(problemPct) + ") unresolved.");     //$NON-NLS-1$ //$NON-NLS-2$
			
			long misses= index.getCacheMisses();
			long hits= index.getCacheHits();
			long tries= misses + hits;
			double missPct= tries == 0 ? 0.0 : (double) misses / (double) tries;
			System.out.println(ident + " Cache["    //$NON-NLS-1$
					+ ChunkCache.getSharedInstance().getMaxSize() / 1024 / 1024 + "MB]: " +    //$NON-NLS-1$
					+ hits + " hits, "      //$NON-NLS-1$
					+ misses + "(" + nfPercent.format(missPct) + ") misses.");      //$NON-NLS-1$ //$NON-NLS-2$

			if ("true".equals(System.getProperty("SHOW_COMPRESSED_INDEXER_INFO"))) {    //$NON-NLS-1$ //$NON-NLS-2$
				Calendar cal = Calendar.getInstance();
				NumberFormat twoDigits= NumberFormat.getNumberInstance();
				twoDigits.setMinimumIntegerDigits(2);
				NumberFormat nfGroup= NumberFormat.getNumberInstance();
				nfGroup.setGroupingUsed(true);

				final String sep0 = "|"; //$NON-NLS-1$
				final String sep = "|  "; //$NON-NLS-1$
				final String sec = "s"; //$NON-NLS-1$
				final String mb = "MB"; //$NON-NLS-1$
				final String million = "M"; //$NON-NLS-1$
				System.out.print(sep0);
				System.out.print(cal.get(Calendar.YEAR) + twoDigits.format(cal.get(Calendar.MONTH) + 1) + twoDigits.format(cal.get(Calendar.DAY_OF_MONTH)));
				System.out.print(sep);
				System.out.print(nfGroup.format(info.fCompletedSources));
				System.out.print(sep);
				System.out.print(nfGroup.format(info.fCompletedHeaders));
				System.out.print(sep);
				System.out.print(nfGroup.format((totalTime + 500) / 1000) + sec);
				System.out.print(sep);
				System.out.print(nfGroup.format((fStatistics.fParsingTime + 500) / 1000) + sec);
				System.out.print(sep);
				System.out.print(nfGroup.format((fStatistics.fResolutionTime + 500) / 1000) + sec);
				System.out.print(sep);
				System.out.print(nfGroup.format((fStatistics.fAddToIndexTime + 500) / 1000) + sec);
				System.out.print(sep);
				System.out.print(nfGroup.format((dbSize + 1024 * 512) / 1024 / 1024) + mb);
				System.out.print(sep);
				System.out.print(nfGroup.format((tries + 1000 * 500) / 1000000) + million);
				System.out.print(sep);
				System.out.print(nfGroup.format(fStatistics.fDeclarationCount));
				System.out.print(sep);
				System.out.print(nfGroup.format(fStatistics.fReferenceCount));
				System.out.print(sep);
				System.out.print(nfGroup.format(fStatistics.fProblemBindingCount));
				System.out.print(sep);
				System.out.print(nfPercent.format(problemPct));
				System.out.print(sep);
				System.out.print(nfGroup.format(fStatistics.fErrorCount));
				System.out.print(sep);
				System.out.print(nfGroup.format(fStatistics.fUnresolvedIncludesCount));
				System.out.print(sep);
				System.out.print(nfGroup.format(fStatistics.fPreprocessorProblemCount));
				System.out.print(sep);
				System.out.print(nfGroup.format(fStatistics.fSyntaxProblemsCount));
				System.out.println(sep0);
			}
		}
	}

	protected ICProject getCProject() {
		return fIndexer.project;
	}

	public void setWriteInfoToLog() {
		fWriteInfoToLog= true;
	}

	@Override
	public synchronized boolean acceptUrgentTask(IPDOMIndexerTask urgentTask) {
		final IPDOMIndexer ti = urgentTask.getIndexer();
		if (fIndexer == null || ti == null ||
				fIndexer.getProject().getProject() != ti.getProject().getProject()) {
			return false;
		}
		return super.acceptUrgentTask(urgentTask);
	}
}
