/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
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

import java.text.NumberFormat;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.model.AbstractLanguage;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.index.IWritableIndexManager;
import org.eclipse.cdt.internal.core.pdom.AbstractIndexerTask;
import org.eclipse.cdt.internal.core.pdom.ITodoTaskUpdater;
import org.eclipse.cdt.internal.core.pdom.IndexerProgress;
import org.eclipse.cdt.internal.core.pdom.db.ChunkCache;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;

/**
 * Configures the abstract indexer task suitable for indexing projects.
 */
public abstract class PDOMIndexerTask extends AbstractIndexerTask implements IPDOMIndexerTask {
	private static final String TRUE = "true"; //$NON-NLS-1$
	
	private AbstractPDOMIndexer fIndexer;
	
	protected PDOMIndexerTask(ITranslationUnit[] addFiles, ITranslationUnit[] updateFiles, ITranslationUnit[] removeFiles, 
			AbstractPDOMIndexer indexer, boolean isFastIndexer) {
		super(concat(addFiles, updateFiles), removeFiles, new ProjectIndexerInputAdapter(indexer.getProject()), isFastIndexer);
		fIndexer= indexer;
		setShowActivity(checkDebugOption(TRACE_ACTIVITY, TRUE));
		setShowProblems(checkDebugOption(TRACE_PROBLEMS, TRUE));
		if (checkProperty(IndexerPreferences.KEY_SKIP_ALL_REFERENCES)) {
			setSkipReferences(SKIP_ALL_REFERENCES);
		}
		else if (checkProperty(IndexerPreferences.KEY_SKIP_TYPE_REFERENCES)) {
			setSkipReferences(SKIP_TYPE_REFERENCES);
		}
		if (getIndexAllFiles()) {
			setIndexFilesWithoutBuildConfiguration(true);
			setIndexHeadersWithoutContext(true);
		}
		else {
			setIndexFilesWithoutBuildConfiguration(false);
			setIndexHeadersWithoutContext(false);
		}
	}
	
	private static ITranslationUnit[] concat(ITranslationUnit[] added, ITranslationUnit[] changed) {
		ITranslationUnit[] result= new ITranslationUnit[added.length+changed.length];
		System.arraycopy(added, 0, result, 0, added.length);
		System.arraycopy(changed, 0, result, added.length, changed.length);
		return result;
	}
	
	public final void setParseUpFront() {
		setParseUpFront(fIndexer.getFilesToParseUpFront());
	}


	public final IPDOMIndexer getIndexer() {
		return fIndexer;
	}
	
	public final void run(IProgressMonitor monitor) throws InterruptedException {
		long start = System.currentTimeMillis();
		runTask(monitor);
		traceEnd(start, fIndex);
	}
	
	
	public IndexerProgress getProgressInformation() {
		return super.getProgressInformation();
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

	private boolean getIndexAllFiles() {
		return checkProperty(IndexerPreferences.KEY_INDEX_ALL_FILES);
	}

	private boolean checkProperty(String key) {
		return TRUE.equals(getIndexer().getProperty(key));
	}

	
	protected String getASTPathForParsingUpFront() {
		final IProject project = getProject().getProject();
		final IPath prjLocation= project.getLocation();
		if (prjLocation == null) {
			return null;
		}
		return prjLocation.append(super.getASTPathForParsingUpFront()).toString(); 
	}

	protected AbstractLanguage[] getLanguages(String filename) {
		IContentType ct= CCorePlugin.getContentType(filename);
		if (ct != null) {
			ILanguage l = LanguageManager.getInstance().getLanguage(ct);
			if (l instanceof AbstractLanguage) {
				if (ct.getId().equals(CCorePlugin.CONTENT_TYPE_CXXHEADER) && l.getLinkageID() == ILinkage.CPP_LINKAGE_ID) {
					ILanguage l2= LanguageManager.getInstance().getLanguageForContentTypeID(CCorePlugin.CONTENT_TYPE_CHEADER);
					if (l2 instanceof AbstractLanguage) {
						return new AbstractLanguage[] {(AbstractLanguage) l, (AbstractLanguage) l2};
					}
				}
				return new AbstractLanguage[] {(AbstractLanguage) l};
			}
		}
		return new AbstractLanguage[0];
	}

	protected IScannerInfo createDefaultScannerConfig(int linkageID) {
		IProject project= getProject().getProject();
		IScannerInfoProvider provider= CCorePlugin.getDefault().getScannerInfoProvider(project);
		IScannerInfo scanInfo;
		if (provider != null) { 
			scanInfo= provider.getScannerInformation(project);
		}
		else {
			scanInfo= new ScannerInfo();
		}
		return scanInfo;
	}

	private ICProject getProject() {
		return getIndexer().getProject();
	}

	protected final IWritableIndex createIndex() {
		try {
			return ((IWritableIndexManager) CCorePlugin.getIndexManager()).getWritableIndex(getProject());
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	protected final ITodoTaskUpdater createTodoTaskUpdater() {
		return new TodoTaskUpdater();
	}
	
	protected void traceEnd(long start, IWritableIndex index) {
		if (checkDebugOption(IPDOMIndexerTask.TRACE_STATISTICS, TRUE)) {
			IndexerProgress info= getProgressInformation();
			String name= getClass().getName();
			name= name.substring(name.lastIndexOf('.')+1);

			System.out.println(name + " " + getProject().getElementName()  //$NON-NLS-1$
					+ " (" + info.fCompletedSources + " sources, "  //$NON-NLS-1$ //$NON-NLS-2$
					+ info.fCompletedHeaders + " headers)"); //$NON-NLS-1$
			boolean allFiles= getIndexAllFiles();
			boolean skipRefs= checkProperty(IndexerPreferences.KEY_SKIP_ALL_REFERENCES);
			boolean skipTypeRefs= skipRefs || checkProperty(IndexerPreferences.KEY_SKIP_TYPE_REFERENCES);
			System.out.println(name + " Options: "  //$NON-NLS-1$
					+ "parseAllFiles=" + allFiles //$NON-NLS-1$
					+ ",skipReferences=" + skipRefs //$NON-NLS-1$
					+ ", skipTypeReferences=" + skipTypeRefs //$NON-NLS-1$
					+ "."); //$NON-NLS-1$
			System.out.println(name + " Timings: "  //$NON-NLS-1$
					+ (System.currentTimeMillis() - start) + " total, " //$NON-NLS-1$
					+ fStatistics.fParsingTime + " parser, " //$NON-NLS-1$
					+ fStatistics.fResolutionTime + " resolution, " //$NON-NLS-1$
					+ fStatistics.fAddToIndexTime + " index update."); //$NON-NLS-1$
			System.out.println(name + " Errors: " //$NON-NLS-1$
					+ fStatistics.fUnresolvedIncludes + " unresolved includes, " //$NON-NLS-1$
					+ fStatistics.fErrorCount + " unexpected errors."); //$NON-NLS-1$

			int sum= fStatistics.fDeclarationCount+fStatistics.fReferenceCount+fStatistics.fProblemBindingCount;
			double problemPct= sum==0 ? 0.0 : (double) fStatistics.fProblemBindingCount / (double) sum;
			NumberFormat nf= NumberFormat.getPercentInstance();
			nf.setMaximumFractionDigits(2);
			nf.setMinimumFractionDigits(2);
			System.out.println(name + " Result: " //$NON-NLS-1$
					+ fStatistics.fDeclarationCount + " declarations, " //$NON-NLS-1$
					+ fStatistics.fReferenceCount + " references, " //$NON-NLS-1$
					+ fStatistics.fProblemBindingCount + "(" + nf.format(problemPct) + ") problems.");  //$NON-NLS-1$ //$NON-NLS-2$
			
			if (index != null) {
				long misses= index.getCacheMisses();
				long hits= index.getCacheHits();
				long tries= misses+hits;
				double missPct= tries==0 ? 0.0 : (double) misses / (double) tries;
				nf.setMinimumFractionDigits(4);
				nf.setMaximumFractionDigits(4);
				System.out.println(name + " Cache[" //$NON-NLS-1$
					+ ChunkCache.getSharedInstance().getMaxSize() / 1024 / 1024 + "mb]: " + //$NON-NLS-1$
					+ hits + " hits, "   //$NON-NLS-1$
					+ misses + "(" + nf.format(missPct)+ ") misses.");   //$NON-NLS-1$//$NON-NLS-2$
			}
		}
	}

	protected ICProject getCProject() {
		return fIndexer.project;
	}
}