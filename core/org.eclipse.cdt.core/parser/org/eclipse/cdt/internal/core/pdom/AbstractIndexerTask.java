/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     IBM Corporation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit.IDependencyTree;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit.IDependencyTree.IASTInclusionNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.AbstractLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ISignificantMacros;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.internal.core.dom.IIncludeFileResolutionHeuristics;
import org.eclipse.cdt.internal.core.index.FileContentKey;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.index.IndexBasedFileContentProvider;
import org.eclipse.cdt.internal.core.parser.IMacroDictionary;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContentProvider;
import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

/**
 * Task for the actual indexing. Various indexers need to implement the abstract methods.
 * @since 5.0
 */
public abstract class AbstractIndexerTask extends PDOMWriter {
	protected static enum UnusedHeaderStrategy {
		skip, useDefaultLanguage, useAlternateLanguage, useBoth
	}
	private static final int MAX_ERRORS = 500;

	private static enum UpdateKind {REQUIRED_SOURCE, REQUIRED_HEADER, OTHER_HEADER}
	private static class LinkageTask {
		final int fLinkageID;
		private final Map<IIndexFileLocation, LocationTask> fLocationTasks;
		
		LinkageTask(int linkageID) {
			fLinkageID= linkageID;
			fLocationTasks= new HashMap<IIndexFileLocation, LocationTask>();
		}
		
		boolean requestUpdate(IIndexFileLocation ifl, IIndexFragmentFile ifile, Object tu,
				UpdateKind kind) {
			LocationTask locTask= fLocationTasks.get(ifl);
			if (locTask == null) {
				locTask= new LocationTask();
				fLocationTasks.put(ifl, locTask);
			}
			return locTask.requestUpdate(ifile, tu, kind);
		}

		LocationTask find(IIndexFileLocation ifl) {
			return fLocationTasks.get(ifl);
		}
	}

	private static class LocationTask {
		private boolean fAddRequested;
		Object fTu;
		UpdateKind fKind= UpdateKind.OTHER_HEADER;
		private List<FileVersionTask> fVersionTasks= Collections.emptyList();

		/**
		 * Requests the update of a file, returns whether the total count needs to be updated.
		 */
		boolean requestUpdate(IIndexFragmentFile ifile, Object tu, UpdateKind kind) {
			if (tu != null)
				fTu= tu;
			if (kind != null)
				fKind= kind;
			
			if (ifile == null) {
				assert fVersionTasks.isEmpty();
				final boolean count= !fAddRequested;
				fAddRequested= true;
				return count;
			}

			return addVersionTask(ifile).requestUpdate();
		}

		private FileVersionTask addVersionTask(IIndexFragmentFile ifile) {
			FileVersionTask fc= findVersion(ifile);
			if (fc != null) 
				return fc;

			fc= new FileVersionTask(ifile, fAddRequested);
			fAddRequested= false;

			switch(fVersionTasks.size()) {
			case 0:
				fVersionTasks= Collections.singletonList(fc);
				break;
			case 1:
				List<FileVersionTask> newList= new ArrayList<FileVersionTask>(2);
				newList.add(fVersionTasks.get(0));
				newList.add(fc);
				fVersionTasks= newList;
				break;
			default:
				fVersionTasks.add(fc);
				break;
			}
			return fc;
		}

		private FileVersionTask findVersion(IIndexFile ifile) {
			for (FileVersionTask fc : fVersionTasks) {
				if (fc.fIndexFile.equals(ifile))
					return fc;
			}
			return null;
		}

		FileVersionTask findVersion(ISignificantMacros sigMacros) throws CoreException {
			for (FileVersionTask fc : fVersionTasks) {
				if (sigMacros.equals(fc.fIndexFile.getSignificantMacros()))
					return fc;
			}
			return null;
		}

		boolean isCompleted() {
			if (fVersionTasks.isEmpty())
				return !fAddRequested;
			
			for (FileVersionTask fc : fVersionTasks) {
				if (fc.fUpdateRequested)
					return false;
			}
			return true;
		}

		public boolean needsVersion() {
			if (fKind == UpdateKind.OTHER_HEADER)
				return false;
			for (FileVersionTask fc : fVersionTasks) {
				if (fc.fUpdateRequested)
					return true;
			}
			return fVersionTasks.isEmpty() && fAddRequested;
		}
	}

	public static class FileVersionTask {
		private final IIndexFragmentFile fIndexFile;
		private boolean fUpdateRequested;

		FileVersionTask(IIndexFragmentFile file, boolean requestUpdate) {
			fIndexFile= file;
			fUpdateRequested= requestUpdate;
		}
		
		boolean requestUpdate() {
			boolean result= !fUpdateRequested;
			fUpdateRequested= true;
			return result;
		}

		void setUpdated() {
			fUpdateRequested= false;
		}
	}
		
	public static class IndexFileContent {
		private Object[] fPreprocessingDirectives;
		private ICPPUsingDirective[] fDirectives;

		public IndexFileContent(IIndexFile ifile) throws CoreException {
			setPreprocessorDirectives(ifile.getIncludes(), ifile.getMacros());
			setUsingDirectives(ifile.getUsingDirectives());
		}

		public Object[] getPreprocessingDirectives() throws CoreException {
			return fPreprocessingDirectives;
		}
		
		public ICPPUsingDirective[] getUsingDirectives() throws CoreException {
			return fDirectives;
		}

		public void setPreprocessorDirectives(IIndexInclude[] includes, IIndexMacro[] macros) throws CoreException {
			fPreprocessingDirectives= merge(includes, macros);
		}

		public void setUsingDirectives(ICPPUsingDirective[] usingDirectives) {
			fDirectives= usingDirectives;
		}

		public static Object[] merge(IIndexInclude[] includes, IIndexMacro[] macros) throws CoreException {
			Object[] merged= new Object[includes.length + macros.length];
			int i= 0;
			int m= 0;
			int ioffset= getOffset(includes, i);
			int moffset= getOffset(macros, m);
			for (int k = 0; k < merged.length; k++) {
				if (ioffset <= moffset) {
					merged[k]= includes[i];
					ioffset= getOffset(includes, ++i);
				} else {
					merged[k]= macros[m];
					moffset= getOffset(macros, ++m);
				}
			}
			return merged;
		}

		private static int getOffset(IIndexMacro[] macros, int m) throws CoreException {
			if (m < macros.length) {
				return macros[m].getFileLocation().getNodeOffset();
			}
			return Integer.MAX_VALUE;
		}

		private static int getOffset(IIndexInclude[] includes, int i) throws CoreException {
			if (i < includes.length) {
				return includes[i].getNameOffset();
			}
			return Integer.MAX_VALUE;
		}
	}

	protected enum MessageKind { parsingFileTask, errorWhileParsing, tooManyIndexProblems }
	
	private int fUpdateFlags= IIndexManager.UPDATE_ALL;
	private UnusedHeaderStrategy fIndexHeadersWithoutContext= UnusedHeaderStrategy.useDefaultLanguage;
	private boolean fIndexFilesWithoutConfiguration= true;
	private List<LinkageTask> fRequestsPerLinkage= new ArrayList<LinkageTask>();
	private Map<IIndexFile, IndexFileContent> fIndexContentCache= new HashMap<IIndexFile, IndexFileContent>();
	private Map<IIndexFileLocation, IIndexFile[]> fIndexFilesCache= new HashMap<IIndexFileLocation, IIndexFile[]>();
	
	private Object[] fFilesToUpdate;
	private List<Object> fFilesToRemove = new ArrayList<Object>();
	private int fASTOptions;
	private int fForceNumberFiles= 0;
	
	protected IWritableIndex fIndex;
	private ITodoTaskUpdater fTodoTaskUpdater;
	private final boolean fIsFastIndexer;
	private long fFileSizeLimit= 0;
	private InternalFileContentProvider fCodeReaderFactory;
	private int fSwallowOutOfMemoryError= 5;
	/**
	 * A queue of urgent indexing tasks that contribute additional files to this task.
	 * The files from the urgent tasks are indexed before all not yet processed files. 
	 */
	private final LinkedList<AbstractIndexerTask> fUrgentTasks;
	boolean fTaskCompleted;
	private IndexerProgress fInfo= new IndexerProgress();
	public AbstractIndexerTask(Object[] filesToUpdate, Object[] filesToRemove,
			IndexerInputAdapter resolver, boolean fastIndexer) {
		super(resolver);
		fIsFastIndexer= fastIndexer;
		fFilesToUpdate= filesToUpdate;
		Collections.addAll(fFilesToRemove, filesToRemove);
		incrementRequestedFilesCount(fFilesToUpdate.length + fFilesToRemove.size());
		fUrgentTasks = new LinkedList<AbstractIndexerTask>();
	}
	
	public final void setIndexHeadersWithoutContext(UnusedHeaderStrategy mode) {
		fIndexHeadersWithoutContext= mode;
	}

	public final void setIndexFilesWithoutBuildConfiguration(boolean val) {
		fIndexFilesWithoutConfiguration= val;
	}

	public UnusedHeaderStrategy getIndexHeadersWithoutContext() {
		return fIndexHeadersWithoutContext;
	}

	public boolean indexFilesWithoutConfiguration() {
		return fIndexFilesWithoutConfiguration;
	}

	public final void setUpdateFlags(int flags) {
		fUpdateFlags= flags;
	}

	// TODO(197989) remove
	public final void setParseUpFront(String[] astFilePaths) {
	}

	public final void setForceFirstFiles(int number) {
		fForceNumberFiles= number;
	}

	public final void setFileSizeLimit(long limit) {
		fFileSizeLimit= limit;
	}

	/**
	 * @see IPDOMIndexerTask#acceptUrgentTask(IPDOMIndexerTask)
	 */
	public synchronized boolean acceptUrgentTask(IPDOMIndexerTask urgentTask) {
		if (!(urgentTask instanceof AbstractIndexerTask)) {
			return false;
		}
		AbstractIndexerTask task = (AbstractIndexerTask) urgentTask; 
		if (task.fIsFastIndexer != fIsFastIndexer ||
				task.fIndexFilesWithoutConfiguration != fIndexFilesWithoutConfiguration ||
				(fIndexFilesWithoutConfiguration && task.fIndexHeadersWithoutContext != fIndexHeadersWithoutContext) ||
				fTaskCompleted) {
			// Reject the urgent work since this task is not capable of doing it, or it's too late.
			return false;
		}
		if (task.fFilesToUpdate.length >
				(fFilesToUpdate != null ? fFilesToUpdate.length : getProgressInformation().fRequestedFilesCount)) {
			// Reject the urgent work since it's too heavy for this task.
			return false;
		}
		fUrgentTasks.add(task);
		return true;
	}
	
	private synchronized boolean hasUrgentTasks() {
		return !fUrgentTasks.isEmpty();
	}

	/**
	 * Retrieves the first urgent task from the queue of urgent tasks.
	 * @return An urgent task, or {@code null} if there are no urgent tasks.
	 */
	private synchronized AbstractIndexerTask getUrgentTask() {
		return fUrgentTasks.poll();
	}

	protected abstract IWritableIndex createIndex();
	protected abstract IIncludeFileResolutionHeuristics createIncludeHeuristics();
	protected abstract IncludeFileContentProvider createReaderFactory();
	protected ITodoTaskUpdater createTodoTaskUpdater() {
		return null;
	}
		
	/**
	 * @return array of linkage IDs that shall be parsed
	 */
	protected int[] getLinkagesToParse() {
		return PDOMManager.IDS_FOR_LINKAGES_TO_INDEX;
	}

	protected IParserLogService getLogService() {
		return ParserUtil.getParserLogService();
	}

	protected void logError(IStatus s) {
		CCorePlugin.log(s);
	}

	protected void logException(Throwable e) {
		CCorePlugin.log(e);
	}

	protected String getMessage(MessageKind kind, Object... arguments) {
		switch (kind) {
		case parsingFileTask:
			return NLS.bind(Messages.AbstractIndexerTask_parsingFileTask, arguments);
		case errorWhileParsing:
			return NLS.bind(Messages.AbstractIndexerTask_errorWhileParsing, arguments);
		case tooManyIndexProblems:
			return Messages.AbstractIndexerTask_tooManyIndexProblems;
		}
		return null;
	}

	/**
	 * Makes a copy of the current progress information and returns it.
	 * @since 4.0
	 */
	public IndexerProgress getProgressInformation() {
		synchronized (fInfo) {
			return new IndexerProgress(fInfo);
		}
	}

	/**
	 * Updates current progress information with the provided delta.
	 */
	private final void updateFileCount(int sources, int primaryHeader, int header) {
		synchronized (fInfo) {
			fInfo.fCompletedSources += sources;
			fInfo.fPrimaryHeaderCount += primaryHeader;
			fInfo.fCompletedHeaders += header;
		}
	}
	
	private final void reportFile(boolean requested, UpdateKind kind) {
		if (requested) {
			if (kind == UpdateKind.REQUIRED_SOURCE) {
				updateFileCount(1, 0, 0);
			} else {
				updateFileCount(0, 1, 1);
			}
		} else {
			updateFileCount(0, 0, 1);
		}
	}

	/**
	 * Updates current progress information with the provided delta.
	 */
	private final void incrementRequestedFilesCount(int delta) {
		synchronized (fInfo) {
			fInfo.fRequestedFilesCount += delta;
		}
	}

	public final void runTask(IProgressMonitor monitor) throws InterruptedException {
		try {
			if (!fIndexFilesWithoutConfiguration) {
				fIndexHeadersWithoutContext= UnusedHeaderStrategy.skip;
			}
			
			fIndex= createIndex();
			if (fIndex == null) {
				return;
			}
			fTodoTaskUpdater= createTodoTaskUpdater();
			
			fASTOptions= ILanguage.OPTION_NO_IMAGE_LOCATIONS
					| ILanguage.OPTION_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS;
			if (getSkipReferences() == SKIP_ALL_REFERENCES) {
				fASTOptions |= ILanguage.OPTION_SKIP_FUNCTION_BODIES;
			}

			fIndex.resetCacheCounters();
			fIndex.acquireReadLock();
	
			try {
				try {
					// Split into sources and headers, remove excluded sources.
					HashMap<Integer, List<IIndexFileLocation>> files= new HashMap<Integer, List<IIndexFileLocation>>();
					final ArrayList<IIndexFragmentFile> indexFilesToRemove= new ArrayList<IIndexFragmentFile>();
					extractFiles(files, indexFilesToRemove, monitor);
	
					setResume(true); 
	
					// Remove files from index
					removeFilesInIndex(fFilesToRemove, indexFilesToRemove, monitor);

					HashMap<Integer, List<IIndexFileLocation>> moreFiles= null;
					while (true) {
						for (int linkageID : getLinkagesToParse()) {
							final List<IIndexFileLocation> filesForLinkage = files.get(linkageID);
							if (filesForLinkage != null) {
								parseLinkage(linkageID, filesForLinkage, monitor);
								fIndexContentCache.clear();
								fIndexFilesCache.clear();
							}
							if (hasUrgentTasks())
								break;
						}
						synchronized (this) {
							if (fUrgentTasks.isEmpty()) {
								if (moreFiles == null) {
									// No urgent tasks and no more files to parse. We are done.
									fTaskCompleted = true;
									break;
								} else {
									files = moreFiles;
									moreFiles = null;
								}
							}
						}
						AbstractIndexerTask urgentTask;
						while ((urgentTask = getUrgentTask()) != null) {
							// Move the lists of not yet parsed files from 'files' to 'moreFiles'.
							if (moreFiles == null) {
								moreFiles = files;
							} else {
								for (Map.Entry<Integer, List<IIndexFileLocation>> entry : files.entrySet()) {
									List<IIndexFileLocation> list= moreFiles.get(entry.getKey());
									if (list == null) {
										moreFiles.put(entry.getKey(), entry.getValue());
									} else {
										list.addAll(0, entry.getValue());
									}
								}							
							}
							// Extract files from the urgent task.
							files = new HashMap<Integer, List<IIndexFileLocation>>();
							fFilesToUpdate = urgentTask.fFilesToUpdate;
							fForceNumberFiles = urgentTask.fForceNumberFiles;
							fFilesToRemove = urgentTask.fFilesToRemove;
							incrementRequestedFilesCount(fFilesToUpdate.length + fFilesToRemove.size());
							extractFiles(files, indexFilesToRemove, monitor);
							removeFilesInIndex(fFilesToRemove, indexFilesToRemove, monitor);
						}
					}
					if (!monitor.isCanceled()) {
						setResume(false);
					}
				} finally {
					fIndex.flush();
				}
			} catch (CoreException e) {
				logException(e);
			} finally {
				fIndex.releaseReadLock();
			}
		} finally {
			synchronized (this) {
				fTaskCompleted = true;
			}
		}
	}

	private void setResume(boolean value) throws InterruptedException, CoreException {
		fIndex.acquireWriteLock();
		try {
			fIndex.getWritableFragment().setProperty(IIndexFragment.PROPERTY_RESUME_INDEXER, String.valueOf(value)); 
		} finally {
			fIndex.releaseWriteLock();
		}
	}

	private void extractFiles(HashMap<Integer, List<IIndexFileLocation>> files, List<IIndexFragmentFile> iFilesToRemove,
			IProgressMonitor monitor) throws CoreException {
		final boolean forceAll= (fUpdateFlags & IIndexManager.UPDATE_ALL) != 0;
		final boolean checkTimestamps= (fUpdateFlags & IIndexManager.UPDATE_CHECK_TIMESTAMPS) != 0;
		final boolean checkFileContentsHash = (fUpdateFlags & IIndexManager.UPDATE_CHECK_CONTENTS_HASH) != 0;

		int count= 0;
		int forceFirst= fForceNumberFiles;
		BitSet linkages= new BitSet();
		for (final Object tu : fFilesToUpdate) {
			if (monitor.isCanceled())
				return;

			final boolean force= forceAll || --forceFirst >= 0;
			final IIndexFileLocation ifl= fResolver.resolveFile(tu);
			if (ifl == null)
				continue;
			
			final IIndexFragmentFile[] indexFiles= fIndex.getWritableFiles(ifl);
			final boolean isSourceUnit= fResolver.isSourceUnit(tu);
			linkages.clear();
			if (isRequiredInIndex(tu, ifl, isSourceUnit)) {
				// Headers or sources required with a specific linkage
				final UpdateKind updateKind = isSourceUnit ? UpdateKind.REQUIRED_SOURCE : UpdateKind.REQUIRED_HEADER;
				AbstractLanguage[] langs= fResolver.getLanguages(tu, fIndexHeadersWithoutContext == UnusedHeaderStrategy.useBoth);
				for (AbstractLanguage lang : langs) {
					int linkageID = lang.getLinkageID();
					boolean foundInLinkage = false;
					for (int i = 0; i < indexFiles.length; i++) {
						IIndexFragmentFile ifile = indexFiles[i];
						if (ifile != null && ifile.getLinkageID() == linkageID && ifile.hasContent()) {
							foundInLinkage = true;
							indexFiles[i]= null;  // Take the file.
							boolean update= force || isModified(checkTimestamps, checkFileContentsHash, ifl, tu, ifile);
							if (update && requestUpdate(linkageID, ifl, ifile, tu, updateKind)) {
								count++;
								linkages.set(linkageID);
							}
						}
					}
					if (!foundInLinkage && requestUpdate(linkageID, ifl, null, tu, updateKind)) {
						linkages.set(linkageID);
						count++;
					}
				}
			}
			
			// Handle other files present in index.
			for (IIndexFragmentFile ifile : indexFiles) {
				if (ifile != null) {
					IIndexInclude ctx= ifile.getParsedInContext();
					if (ctx == null && !fResolver.isIndexedUnconditionally(ifile.getLocation())) {
						iFilesToRemove.add(ifile);
						count++;
					} else {
						boolean update= force || isModified(checkTimestamps, checkFileContentsHash, ifl, tu, ifile);
						final int linkageID = ifile.getLinkageID();
						if (update && requestUpdate(linkageID, ifl, ifile, tu, UpdateKind.OTHER_HEADER)) {
							count++;
							linkages.set(linkageID);
						}
					}
				}
			}
			for (int lid = linkages.nextSetBit(0); lid >= 0; lid= linkages.nextSetBit(lid+1)) {
				addPerLinkage(lid, ifl, files);
			}
		}
		synchronized (this) {
			incrementRequestedFilesCount(count - fFilesToUpdate.length);
			fFilesToUpdate= null;
		}
	}

	private void addPerLinkage(int linkageID, IIndexFileLocation ifl, HashMap<Integer, List<IIndexFileLocation>> files) {
		List<IIndexFileLocation> list= files.get(linkageID);
		if (list == null) {
			list= new LinkedList<IIndexFileLocation>();
			files.put(linkageID, list);
		}
		list.add(ifl);
	}

	private boolean isRequiredInIndex(Object tu, IIndexFileLocation ifl, boolean isSourceUnit) {
		// External files are never required
		if (fResolver.isIndexedOnlyIfIncluded(tu)) 
			return false;
		
		// User preference to require all
		if (fIndexHeadersWithoutContext != UnusedHeaderStrategy.skip)
			return true;
		
		// File required because it is open in the editor.
		if (fResolver.isIndexedUnconditionally(ifl))
			return true;
		
		// Source file
		if (isSourceUnit) {
			if (fIndexFilesWithoutConfiguration || fResolver.isFileBuildConfigured(tu))
				return true;
		}
		return false;
	}

	private boolean isModified(boolean checkTimestamps, boolean checkFileContentsHash, IIndexFileLocation ifl,
			Object tu, IIndexFragmentFile file)	throws CoreException {
		if (checkTimestamps) {
			if (fResolver.getLastModified(ifl) != file.getTimestamp() || 
					fResolver.getEncoding(ifl).hashCode() != file.getEncodingHashcode()) {
				if (checkFileContentsHash && computeFileContentsHash(tu) == file.getContentsHash()) {
					return false;
				}
				return true;
			}
		}
		return false;
	}

	private long computeFileContentsHash(Object tu) {
		FileContent codeReader= fResolver.getCodeReader(tu);
		return codeReader != null ? codeReader.getContentsHash() : 0;
	}

	private boolean requestUpdate(int linkageID, IIndexFileLocation ifl, IIndexFragmentFile ifile, Object tu, UpdateKind kind) {
		LinkageTask fileMap= createRequestMap(linkageID);
		return fileMap.requestUpdate(ifl, ifile, tu, kind);
	}
	
	private LinkageTask createRequestMap(int linkageID) {
		LinkageTask map= findRequestMap(linkageID);
		if (map == null) {
			map= new LinkageTask(linkageID);
			fRequestsPerLinkage.add(map);
		}
		return map;
	}

	private LinkageTask findRequestMap(int linkageID) {
		for (LinkageTask map : fRequestsPerLinkage) {
			if (map.fLinkageID == linkageID)
				return map;
		}
		return null;
	}
	
	@Override
	protected void reportFileWrittenToIndex(FileInAST file, IIndexFragmentFile ifile,
			IIndexFragmentFile replaces) throws CoreException {
		final FileContentKey fck = file.fFileContentKey;
		boolean wasRequested= false;
		UpdateKind kind= UpdateKind.OTHER_HEADER;
		LinkageTask map = findRequestMap(fck.getLinkageID());
		if (map != null) {
			LocationTask locTask = map.find(fck.getLocation());
			if (locTask != null) {
				kind= locTask.fKind;
				FileVersionTask v = locTask.addVersionTask(ifile);
				wasRequested= v.fUpdateRequested;
				v.setUpdated();
			}
		}
		fIndexContentCache.remove(ifile);
		fIndexFilesCache.remove(file.fFileContentKey.getLocation());
		reportFile(wasRequested, kind);
			
		if (replaces != null) {
			if (map != null) {
				LocationTask locTask = map.find(fck.getLocation());
				if (locTask != null) {
					kind= locTask.fKind;
					FileVersionTask v = locTask.findVersion(ifile);
					if (v != null) {
						if (v.fUpdateRequested) {
							reportFile(true, locTask.fKind);
							v.setUpdated();
						}
					}
				}
			}
			fIndexContentCache.remove(replaces);
		}
	}

	private void removeFilesInIndex(List<Object> filesToRemove, List<IIndexFragmentFile> indexFilesToRemove,
			IProgressMonitor monitor) throws InterruptedException, CoreException {
		if (!filesToRemove.isEmpty() || !indexFilesToRemove.isEmpty()) {
			fIndex.acquireWriteLock();
			try {
				for (Object tu : filesToRemove) {
					if (monitor.isCanceled()) {
						return;
					}
					IIndexFileLocation ifl= fResolver.resolveFile(tu);
					if (ifl == null)
						continue;
					IIndexFragmentFile[] ifiles= fIndex.getWritableFiles(ifl);
					for (IIndexFragmentFile ifile : ifiles) {
						fIndex.clearFile(ifile);
					}
					incrementRequestedFilesCount(-1);
				}
				for (IIndexFragmentFile ifile : indexFilesToRemove) {
					if (monitor.isCanceled()) {
						return;
					}
					fIndex.clearFile(ifile);
					incrementRequestedFilesCount(-1);
				}
			} finally {
				fIndex.releaseWriteLock();
			}
		}
		filesToRemove.clear();
	}
	
	private void parseLinkage(int linkageID, List<IIndexFileLocation> files, IProgressMonitor monitor)
			throws CoreException, InterruptedException {
		LinkageTask map = findRequestMap(linkageID);
		if (map == null || files == null || files.isEmpty())
			return;
		
		// First parse the required sources
		for (Iterator<IIndexFileLocation> it= files.iterator(); it.hasNext();) {
			IIndexFileLocation ifl= it.next();
			LocationTask locTask = map.find(ifl);
			if (locTask == null || locTask.isCompleted()) {
				it.remove();
			} else if (locTask.fKind == UpdateKind.REQUIRED_SOURCE) {
				if (monitor.isCanceled() || hasUrgentTasks())
					return;
				final Object tu = locTask.fTu;
				final IScannerInfo scannerInfo= fResolver.getBuildConfiguration(linkageID, tu);
				parseFile(tu, linkageID, ifl, scannerInfo, null, monitor);
			}
		}
		
		// Files with context
		for (Iterator<IIndexFileLocation> it= files.iterator(); it.hasNext();) {
			IIndexFileLocation ifl= it.next();
			LocationTask locTask = map.find(ifl);
			if (locTask == null || locTask.isCompleted()) {
				it.remove();
			} else {
				// Additional version tasks may be added while parsing a file,
				// use an integer to iterate over the list.
				final List<FileVersionTask> versionTasks = locTask.fVersionTasks;
				for (int i=0; i<versionTasks.size(); i++) {
					FileVersionTask versionTask = versionTasks.get(i);
					if (versionTask.fUpdateRequested) {
						if (monitor.isCanceled() || hasUrgentTasks())
							return;
						parseVersionInContext(linkageID, map, ifl, versionTask, locTask.fTu,
								new HashSet<IIndexFile>(), monitor);
					}
				}
			}
		}
		
		// Files without context
		for (Iterator<IIndexFileLocation> it= files.iterator(); it.hasNext();) {
			IIndexFileLocation ifl= it.next();
			LocationTask locTask = map.find(ifl);
			if (locTask == null || locTask.isCompleted()) {
				it.remove();
			} else {
				if (locTask.needsVersion()) {
					if (monitor.isCanceled() || hasUrgentTasks())
						return;
					final Object tu = locTask.fTu;
					final IScannerInfo scannerInfo= fResolver.getBuildConfiguration(linkageID, tu);
					parseFile(tu, linkageID, ifl, scannerInfo, null, monitor);
					if (locTask.isCompleted())
						it.remove();
					
				}
			}
		}

		// Delete remaining files.
		fIndex.acquireWriteLock();
		try {
			for (IIndexFileLocation ifl : files) {
				LocationTask locTask = map.find(ifl);
				if (locTask != null && !locTask.isCompleted()) {
					if (!locTask.needsVersion()) {
						if (monitor.isCanceled() || hasUrgentTasks())
							return;
						for (FileVersionTask v : locTask.fVersionTasks) {
							if (v.fUpdateRequested) {
								fIndex.clearFile(v.fIndexFile);
								reportFile(true, locTask.fKind);
							}
						}
					}
				}
			}
		} finally {
			fIndex.releaseWriteLock();
		}
	}

	private void parseVersionInContext(int linkageID, LinkageTask map, IIndexFileLocation ifl,
			final FileVersionTask versionTask, Object tu, Set<IIndexFile> safeGuard,
			IProgressMonitor monitor) throws CoreException, InterruptedException {
		final IIndexFragmentFile headerFile = versionTask.fIndexFile;
		IIndexFragmentFile ctx= headerFile;
		for(;;) {
			IIndexInclude ctxInclude= ctx.getParsedInContext();
			if (ctxInclude == null)
				break;
			
			final IIndexFragmentFile nextCtx= (IIndexFragmentFile) ctxInclude.getIncludedBy();
			if (!fIndex.isWritableFile(nextCtx) || !safeGuard.add(nextCtx)) 
				break;
			
			final IIndexFileLocation ctxIfl = nextCtx.getLocation();
			LocationTask ctxTask= map.find(ctxIfl);
			if (ctxTask != null) {
				FileVersionTask ctxVersionTask = ctxTask.findVersion(nextCtx);
				if (ctxVersionTask != null && ctxVersionTask.fUpdateRequested) {
					// Handle the context first.
					parseVersionInContext(linkageID, map, ctxIfl, ctxVersionTask, ctxTask.fTu,
							safeGuard, monitor);
					if (ctxVersionTask.fUpdateRequested 		// This is unexpected.
							|| !versionTask.fUpdateRequested) 	// Our file was parsed.
						return;
					
					// The file is no longer a context, look for a different one.
					ctxInclude= ctx.getParsedInContext();
					continue;
				}
			}
			ctx= nextCtx;
		}
		
		// See if we found a context and parse the file
		if (ctx != headerFile) {
			Object contextTu= fResolver.getInputFile(ctx.getLocation());
			if (contextTu != null) {
				final IScannerInfo scannerInfo= fResolver.getBuildConfiguration(linkageID, contextTu);
				IIndexFragmentFile[] ctx2header= {ctx, headerFile};
				parseFile(tu, linkageID, ifl, scannerInfo, ctx2header, monitor);
			}
		}	
	}


	private void parseFile(Object tu, int linkageID, IIndexFileLocation ifl, IScannerInfo scanInfo,
			IIndexFragmentFile[] ctx2header, IProgressMonitor pm) throws CoreException, InterruptedException {
		IPath path= getLabel(ifl);
		AbstractLanguage[] langs= fResolver.getLanguages(tu, true);
		AbstractLanguage lang= null;
		for (AbstractLanguage lang2 : langs) {
			if (lang2.getLinkageID() == linkageID) {
				lang= lang2;
				break;
			}
		}
		if (lang == null) {
			return;
		}
		
		Throwable th= null;
		try {
			if (fShowActivity) {
				trace("Indexer: parsing " + path.toOSString()); //$NON-NLS-1$
			}
			pm.subTask(getMessage(MessageKind.parsingFileTask,
					path.lastSegment(), path.removeLastSegments(1).toString()));
			long start= System.currentTimeMillis();
			FileContent codeReader= fResolver.getCodeReader(tu);
			IASTTranslationUnit ast= createAST(tu, lang, codeReader, scanInfo, fASTOptions, ctx2header, pm);
			fStatistics.fParsingTime += System.currentTimeMillis() - start;
			if (ast != null) {
				IIndexFragmentFile rewrite= ctx2header == null ? null : ctx2header[1];
				writeToIndex(linkageID, ast, codeReader.getContentsHash(), rewrite, pm);
			}
		} catch (CoreException e) {
			th= e;
		} catch (RuntimeException e) {
			th= e;
		} catch (StackOverflowError e) {
			th= e;
		} catch (AssertionError e) {
			th= e;
		} catch (OutOfMemoryError e) {
			if (--fSwallowOutOfMemoryError < 0)
				throw e;
			th= e;
		}
		if (th != null) {
			swallowError(path, th);
		}
	}
	
	private IPath getLabel(IIndexFileLocation ifl) {
		String fullPath= ifl.getFullPath();
		if (fullPath != null) {
			return new Path(fullPath);
		}
		IPath path= IndexLocationFactory.getAbsolutePath(ifl);
		if (path != null) {
			return path;
		}
		URI uri= ifl.getURI();
		return new Path(EFSExtensionManager.getDefault().getPathFromURI(uri));
	}

	private void swallowError(IPath file, Throwable e) throws CoreException {
		IStatus s;
		/*
		 * If the thrown CoreException is for a STATUS_PDOM_TOO_LARGE, we don't want to
		 * swallow this one.
		 */
		if (e instanceof CoreException) {
			s=((CoreException) e).getStatus();
			if (s.getCode() == CCorePlugin.STATUS_PDOM_TOO_LARGE) {
				if (CCorePlugin.PLUGIN_ID.equals(s.getPlugin()))
					throw (CoreException) e;
			}
	
			// mask errors in order to avoid dialog from platform
			Throwable exception = s.getException();
			if (exception != null) {
				Throwable masked= getMaskedException(exception);
				if (masked != exception) {
					e= masked;
					exception= null;
				}
			} 
			if (exception == null) {
				s= new Status(s.getSeverity(), s.getPlugin(), s.getCode(), s.getMessage(), e);
			}
		} else {
			e= getMaskedException(e);
			s= createStatus(getMessage(MessageKind.errorWhileParsing, file), e);
		}
		logError(s);
		if (++fStatistics.fErrorCount > MAX_ERRORS) {
			throw new CoreException(createStatus(getMessage(MessageKind.tooManyIndexProblems)));
		}
	}

	private Throwable getMaskedException(Throwable e) {
		if (e instanceof OutOfMemoryError || e instanceof StackOverflowError || e instanceof AssertionError) {
			return new InvocationTargetException(e);
		}
		return e;
	}

	private final IASTTranslationUnit createAST(Object tu, AbstractLanguage language,
			FileContent codeReader, IScannerInfo scanInfo, int options,
			IIndexFile[] ctx2header, IProgressMonitor pm) throws CoreException {
		if (codeReader == null) {
			return null;
		}
		if (fResolver.isSourceUnit(tu)) {
			options |= ILanguage.OPTION_IS_SOURCE_UNIT;
		}
		if (fFileSizeLimit > 0 && fResolver.getFileSize(codeReader.getFileLocation()) > fFileSizeLimit) {
			if (fShowActivity) {
				trace("Indexer: Skipping large file " + codeReader.getFileLocation());  //$NON-NLS-1$ 
			}
			return null;
		}
		if (fCodeReaderFactory == null) {
			InternalFileContentProvider fileContentProvider = createInternalFileContentProvider();
			if (fIsFastIndexer) {
				IndexBasedFileContentProvider ibfcp = new IndexBasedFileContentProvider(fIndex, fResolver,
						language.getLinkageID(), fileContentProvider, this);
				ibfcp.setContextToHeaderGap(ctx2header);
				ibfcp.setFileSizeLimit(fFileSizeLimit);
				fCodeReaderFactory= ibfcp;
			} else {
				fCodeReaderFactory= fileContentProvider;
			}
			fCodeReaderFactory.setIncludeResolutionHeuristics(createIncludeHeuristics());
		} else if (fIsFastIndexer) {
			final IndexBasedFileContentProvider ibfcp = (IndexBasedFileContentProvider) fCodeReaderFactory;
			ibfcp.setContextToHeaderGap(ctx2header);
			ibfcp.setLinkage(language.getLinkageID());
		}
		
		IASTTranslationUnit ast= language.getASTTranslationUnit(codeReader, scanInfo, fCodeReaderFactory,
				fIndex, options, getLogService());
		if (pm.isCanceled()) {
			return null;
		}
		return ast;
	}

	private InternalFileContentProvider createInternalFileContentProvider() {
		final IncludeFileContentProvider fileContentProvider = createReaderFactory();
		if (fileContentProvider instanceof InternalFileContentProvider)
			return (InternalFileContentProvider) fileContentProvider;
		
		throw new IllegalArgumentException("Invalid file content provider"); //$NON-NLS-1$
	}

	private void writeToIndex(final int linkageID, IASTTranslationUnit ast, long fileContentsHash,
			IIndexFragmentFile replace, IProgressMonitor pm) throws CoreException, InterruptedException {
		HashSet<FileContentKey> enteredFiles= new HashSet<FileContentKey>();
		ArrayList<FileInAST> orderedFileKeys= new ArrayList<FileInAST>();
		
		final IIndexFileLocation topIfl = fResolver.resolveASTPath(ast.getFilePath());
		FileContentKey topKey = new FileContentKey(linkageID, topIfl, ast.getSignificantMacros());
		enteredFiles.add(topKey);
		IDependencyTree tree= ast.getDependencyTree();
		IASTInclusionNode[] inclusions= tree.getInclusions();
		for (IASTInclusionNode inclusion : inclusions) {
			collectOrderedFileKeys(linkageID, inclusion, enteredFiles, orderedFileKeys);
		}
		
		if (replace != null || needToStoreInIndex(linkageID, topIfl, ast.getSignificantMacros())) {
			orderedFileKeys.add(new FileInAST(null, topKey, fileContentsHash));
		}
		
		FileInAST[] fileKeys= orderedFileKeys.toArray(new FileInAST[orderedFileKeys.size()]);
		try {
			addSymbols(ast, fileKeys, fIndex, false, replace, fTodoTaskUpdater, pm);
		} catch (CoreException e) {
			// Avoid parsing files again, that caused an exception to be thrown.
			withdrawRequests(linkageID, fileKeys);
			throw e;
		} catch (RuntimeException e) {
			withdrawRequests(linkageID, fileKeys);
			throw e;
		} catch (Error e) {
			withdrawRequests(linkageID, fileKeys);
			throw e;
		}
	}

	private void collectOrderedFileKeys(final int linkageID, IASTInclusionNode inclusion,
			HashSet<FileContentKey> enteredFiles, ArrayList<FileInAST> orderedFileKeys) throws CoreException {
		final IASTPreprocessorIncludeStatement include= inclusion.getIncludeDirective();
		if (include.createsAST()) {
			final IIndexFileLocation ifl= fResolver.resolveASTPath(include.getPath());
			FileContentKey fileKey = new FileContentKey(linkageID, ifl, include.getSignificantMacros());
			final boolean isFirstEntry= enteredFiles.add(fileKey);
			IASTInclusionNode[] nested= inclusion.getNestedInclusions();
			for (IASTInclusionNode element : nested) {
				collectOrderedFileKeys(linkageID, element, enteredFiles, orderedFileKeys);
			}
			if (isFirstEntry && needToStoreInIndex(linkageID, ifl, include.getSignificantMacros())) {
				orderedFileKeys.add(new FileInAST(include, fileKey, include.getContentsHash()));
			}
		}
	}

	private boolean needToStoreInIndex(int linkageID, IIndexFileLocation ifl, ISignificantMacros sigMacros) throws CoreException {
		LinkageTask map = findRequestMap(linkageID);
		if (map != null) {
			LocationTask locTask= map.find(ifl);
			if (locTask != null) {
				FileVersionTask task = locTask.findVersion(sigMacros);
				if (task != null) {
					return task.fUpdateRequested;
				}
			}
		}
		IIndexFile ifile= null;
		if (fResolver.canBePartOfSDK(ifl)) {
			// Check for a version in potentially another pdom.
			ifile = fIndex.getFile(linkageID, ifl, sigMacros);
		} else {
			// Search the writable PDOM, only.
			IIndexFragmentFile fragFile = fIndex.getWritableFile(linkageID, ifl, sigMacros);
			if (fragFile != null && fragFile.hasContent()) {
				ifile= fragFile;
			} 
		}
		return ifile == null;
	}

	private void withdrawRequests(int linkageID, FileInAST[] fileKeys) {
		LinkageTask map = findRequestMap(linkageID);
		if (map != null) {
			for (FileInAST fileKey : fileKeys) {
				LocationTask locTask = map.find(fileKey.fFileContentKey.getLocation());
				if (locTask != null) {
					if (locTask.fAddRequested) {
						locTask.fAddRequested= false;
						reportFile(true, locTask.fKind);
					} else {
						for (FileVersionTask fc : locTask.fVersionTasks) {
							if (fc.fUpdateRequested) {
								reportFile(true, locTask.fKind);
								fc.setUpdated();
							}
						}
					}
				}
			}
		}
	}

	public final IndexFileContent getFileContent(int linkageID, IIndexFileLocation ifl,
			IIndexFile file) throws CoreException {
		LinkageTask map = findRequestMap(linkageID);
		if (map != null) {
			LocationTask request= map.find(ifl);
			if (request != null) {
				FileVersionTask task= request.findVersion(file);
				if (task != null && task.fUpdateRequested)
					return null;
			}
		}
		IndexFileContent fc= fIndexContentCache.get(file);
		if (fc == null) {
			fc= new IndexFileContent(file);
			fIndexContentCache.put(file, fc);
		}
		return fc;
	}
	
	public IIndexFile selectIndexFile(int linkageID, IIndexFileLocation ifl, IMacroDictionary md) throws CoreException {
		LinkageTask map = findRequestMap(linkageID);
		if (map != null) {
			LocationTask request= map.find(ifl);
			if (request != null) {
				for (FileVersionTask fileVersion : request.fVersionTasks) {
					final IIndexFile indexFile = fileVersion.fIndexFile;
					if (md.satisfies(indexFile.getSignificantMacros())) {
						if (fileVersion.fUpdateRequested)
							return null;
						return indexFile;
					}
				}
			}
		}
		
		IIndexFile[] files= fIndexFilesCache.get(ifl);
		if (files == null) {
			files= fIndex.getFiles(linkageID, ifl);
			fIndexFilesCache.put(ifl, files);
		}
		for (IIndexFile indexFile : files) {
			if (md.satisfies(indexFile.getSignificantMacros())) {
				return indexFile;
			}
		}
		return null;
	}
}
