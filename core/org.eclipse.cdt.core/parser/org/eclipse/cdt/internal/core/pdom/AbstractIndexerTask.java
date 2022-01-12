/*******************************************************************************
 * Copyright (c) 2007, 2014 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     IBM Corporation
 *     Sergey Prigogin (Google)
 *     Thomas Corbat (IFS)
 *     Marc-Andre Laperle (Ericsson)
 *     Karsten Thoms (itemis) - Bug#471103
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
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
import org.eclipse.cdt.core.index.IPDOMASTProcessor;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.AbstractLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IParserSettings;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ISignificantMacros;
import org.eclipse.cdt.core.parser.IncludeExportPatterns;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.internal.core.dom.IIncludeFileResolutionHeuristics;
import org.eclipse.cdt.internal.core.index.FileContentKey;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.index.IndexBasedFileContentProvider;
import org.eclipse.cdt.internal.core.model.DebugLogConstants;
import org.eclipse.cdt.internal.core.parser.IMacroDictionary;
import org.eclipse.cdt.internal.core.parser.ParserLogService;
import org.eclipse.cdt.internal.core.parser.ParserSettings2;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContentProvider;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContentProvider.DependsOnOutdatedFileException;
import org.eclipse.cdt.internal.core.parser.util.LRUCache;
import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;

/**
 * Task for the actual indexing. Various indexers need to implement the abstract methods.
 * @since 5.0
 */
public abstract class AbstractIndexerTask extends PDOMWriter {
	public static enum UnusedHeaderStrategy {
		skip, useC, useCPP, useDefaultLanguage, useBoth
	}

	private static final int MAX_ERRORS = 500;

	// Order of constants is important. Stronger update types have to precede the weaker ones.
	private static enum UpdateKind {
		REQUIRED_SOURCE, REQUIRED_HEADER, ONE_LINKAGE_HEADER, OTHER_HEADER
	}

	private static final Pattern HEADERNAME_PATTERN = Pattern.compile("@headername\\{(?<header>[^\\}]+)\\}"); //$NON-NLS-1$

	private static class LinkageTask {
		final int fLinkageID;
		private final Map<IIndexFileLocation, LocationTask> fLocationTasks;

		LinkageTask(int linkageID) {
			fLinkageID = linkageID;
			fLocationTasks = new HashMap<>();
		}

		boolean requestUpdate(IIndexFileLocation ifl, IIndexFragmentFile ifile, Object tu, UpdateKind kind,
				Map<IIndexFileLocation, LocationTask> oneLinkageTasks) {
			LocationTask locTask = fLocationTasks.get(ifl);
			if (locTask == null) {
				locTask = new LocationTask();
				fLocationTasks.put(ifl, locTask);
			}
			boolean result = locTask.requestUpdate(ifile, tu, kind);

			// Store one-linkage tasks.
			if (kind == UpdateKind.ONE_LINKAGE_HEADER && locTask.fVersionTasks.isEmpty())
				oneLinkageTasks.put(ifl, locTask);

			return result;
		}

		LocationTask find(IIndexFileLocation ifl) {
			return fLocationTasks.get(ifl);
		}
	}

	private static class LocationTask {
		private boolean fCountedUnknownVersion;
		private boolean fStoredAVersion;
		Object fTu;
		UpdateKind fKind = UpdateKind.OTHER_HEADER;
		private List<FileVersionTask> fVersionTasks = Collections.emptyList();

		/**
		 * Requests the update of a file, returns whether the total count needs to be updated.
		 */
		boolean requestUpdate(IIndexFragmentFile ifile, Object tu, UpdateKind kind) {
			if (tu != null)
				fTu = tu;
			// Change fKind only if it becomes stronger as a result.
			if (fKind == null || (kind != null && kind.compareTo(fKind) < 0))
				fKind = kind;

			if (ifile == null) {
				assert fVersionTasks.isEmpty();
				final boolean countRequest = !fCountedUnknownVersion;
				fCountedUnknownVersion = true;
				return countRequest;
			}

			return addVersionTask(ifile);
		}

		/**
		 * Return whether the task needs to be counted.
		 */
		private boolean addVersionTask(IIndexFragmentFile ifile) {
			FileVersionTask fc = findVersion(ifile);
			if (fc != null)
				return false;

			fc = new FileVersionTask(ifile);
			boolean countRequest = true;
			if (fCountedUnknownVersion) {
				fCountedUnknownVersion = false;
				countRequest = false;
			}

			switch (fVersionTasks.size()) {
			case 0:
				fVersionTasks = Collections.singletonList(fc);
				break;
			case 1:
				List<FileVersionTask> newList = new ArrayList<>(2);
				newList.add(fVersionTasks.get(0));
				newList.add(fc);
				fVersionTasks = newList;
				break;
			default:
				fVersionTasks.add(fc);
				break;
			}
			return countRequest;
		}

		void removeVersionTask(Iterator<FileVersionTask> it) {
			if (fVersionTasks.size() == 1) {
				fVersionTasks = Collections.emptyList();
			} else {
				it.remove();
			}
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
			for (FileVersionTask fc : fVersionTasks) {
				if (fc.fOutdated)
					return false;
			}
			if (fKind == UpdateKind.OTHER_HEADER)
				return true;

			return fStoredAVersion;
		}

		public boolean needsVersion() {
			if (fKind == UpdateKind.OTHER_HEADER)
				return false;

			return !fStoredAVersion;
		}
	}

	public static class FileVersionTask {
		private final IIndexFragmentFile fIndexFile;
		private boolean fOutdated;

		FileVersionTask(IIndexFragmentFile file) {
			fIndexFile = file;
			fOutdated = true;
		}

		void setUpdated() {
			fOutdated = false;
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
			fPreprocessingDirectives = merge(includes, macros);
		}

		public void setUsingDirectives(ICPPUsingDirective[] usingDirectives) {
			fDirectives = usingDirectives;
		}

		public static Object[] merge(IIndexInclude[] includes, IIndexMacro[] macros) throws CoreException {
			Object[] merged = new Object[includes.length + macros.length];
			int i = 0;
			int m = 0;
			int ioffset = getOffset(includes, i);
			int moffset = getOffset(macros, m);
			for (int k = 0; k < merged.length; k++) {
				if (ioffset <= moffset && i < includes.length) {
					merged[k] = includes[i];
					ioffset = getOffset(includes, ++i);
				} else if (m < macros.length) {
					merged[k] = macros[m];
					moffset = getOffset(macros, ++m);
				}
			}
			return merged;
		}

		private static int getOffset(IIndexMacro[] macros, int m) throws CoreException {
			if (m < macros.length) {
				IASTFileLocation fileLoc = macros[m].getFileLocation();
				if (fileLoc != null) {
					return fileLoc.getNodeOffset();
				}
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

	protected enum MessageKind {
		parsingFileTask, errorWhileParsing, tooManyIndexProblems
	}

	private int fUpdateFlags = IIndexManager.UPDATE_ALL;
	private UnusedHeaderStrategy fIndexHeadersWithoutContext = UnusedHeaderStrategy.useDefaultLanguage;
	private boolean fIndexFilesWithoutConfiguration = true;
	private boolean fIndexAllHeaderVersions = false;
	private Set<String> fHeadersToIndexAllVersions = Collections.emptySet();
	private Pattern fPragmaPrivatePattern;
	private List<LinkageTask> fRequestsPerLinkage = new ArrayList<>();
	private Map<IIndexFile, IndexFileContent> fIndexContentCache = new LRUCache<>(500);
	private Map<IIndexFileLocation, IIndexFragmentFile[]> fIndexFilesCache = new LRUCache<>(5000);
	private Map<IIndexFileLocation, LocationTask> fOneLinkageTasks = new HashMap<>();

	private Object[] fFilesToUpdate;
	private List<Object> fFilesToRemove = new ArrayList<>();
	private int fASTOptions;
	private int fForceNumberFiles;

	protected IWritableIndex fIndex;
	private ITodoTaskUpdater fTodoTaskUpdater;
	private final boolean fIsFastIndexer;
	private long fTranslationUnitSizeLimit;
	private long fIncludedFileSizeLimit;
	private InternalFileContentProvider fCodeReaderFactory;
	private int fSwallowOutOfMemoryError = 5;
	/**
	 * A queue of urgent indexing tasks that contribute additional files to this task.
	 * The files from the urgent tasks are indexed before all not yet processed files.
	 */
	private final Deque<AbstractIndexerTask> fUrgentTasks;
	boolean fTaskCompleted;
	private IndexerProgress fInfo = new IndexerProgress();

	public AbstractIndexerTask(Object[] filesToUpdate, Object[] filesToRemove, IndexerInputAdapter resolver,
			boolean fastIndexer) {
		super(resolver);
		fIsFastIndexer = fastIndexer;
		fFilesToUpdate = filesToUpdate;
		Collections.addAll(fFilesToRemove, filesToRemove);
		incrementRequestedFilesCount(fFilesToUpdate.length + fFilesToRemove.size());
		fUrgentTasks = new ArrayDeque<>();
	}

	public final void setIndexHeadersWithoutContext(UnusedHeaderStrategy mode) {
		fIndexHeadersWithoutContext = mode;
	}

	public final void setIndexFilesWithoutBuildConfiguration(boolean val) {
		fIndexFilesWithoutConfiguration = val;
	}

	public UnusedHeaderStrategy getIndexHeadersWithoutContext() {
		return fIndexHeadersWithoutContext;
	}

	public boolean indexFilesWithoutConfiguration() {
		return fIndexFilesWithoutConfiguration;
	}

	public final void setUpdateFlags(int flags) {
		fUpdateFlags = flags;
	}

	public final void setForceFirstFiles(int number) {
		fForceNumberFiles = number;
	}

	public final void setFileSizeLimits(long translationUnitSizeLimit, long includedFileSizeLimit) {
		fTranslationUnitSizeLimit = translationUnitSizeLimit;
		fIncludedFileSizeLimit = includedFileSizeLimit;
	}

	public void setIndexAllHeaderVersions(boolean indexAllHeaderVersions) {
		fIndexAllHeaderVersions = indexAllHeaderVersions;
	}

	public void setHeadersToIndexAllVersions(Set<String> headers) {
		fHeadersToIndexAllVersions = headers;
	}

	public void setPragmaPrivatePattern(Pattern pattern) {
		fPragmaPrivatePattern = pattern;
	}

	/**
	 * @see IPDOMIndexerTask#acceptUrgentTask(IPDOMIndexerTask)
	 */
	public synchronized boolean acceptUrgentTask(IPDOMIndexerTask urgentTask) {
		if (!(urgentTask instanceof AbstractIndexerTask)) {
			return false;
		}
		AbstractIndexerTask task = (AbstractIndexerTask) urgentTask;
		if (task.fIsFastIndexer != fIsFastIndexer
				|| task.fIndexFilesWithoutConfiguration != fIndexFilesWithoutConfiguration
				|| (fIndexFilesWithoutConfiguration && task.fIndexHeadersWithoutContext != fIndexHeadersWithoutContext)
				|| fTaskCompleted) {
			// Reject the urgent work since this task is not capable of doing it, or it's too late.
			return false;
		}
		if (task.fFilesToUpdate.length > (fFilesToUpdate != null ? fFilesToUpdate.length
				: getProgressInformation().fRequestedFilesCount)) {
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

	protected IncludeExportPatterns getIncludeExportPatterns() {
		return null;
	}

	protected IParserSettings createParserSettings() {
		return new ParserSettings2();
	}

	/**
	 * @return array of linkage IDs that shall be parsed
	 */
	protected int[] getLinkagesToParse() {
		if (fIndexHeadersWithoutContext == UnusedHeaderStrategy.useCPP)
			return PDOMManager.IDS_FOR_LINKAGES_TO_INDEX_C_FIRST;
		return PDOMManager.IDS_FOR_LINKAGES_TO_INDEX;
	}

	protected IParserLogService getLogService() {
		return new ParserLogService(DebugLogConstants.PARSER, fCancelState);
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

	private final void reportFile(boolean wasCounted, UpdateKind kind) {
		if (wasCounted) {
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
				fIndexHeadersWithoutContext = UnusedHeaderStrategy.skip;
			}

			fIndex = createIndex();
			if (fIndex == null) {
				return;
			}
			fTodoTaskUpdater = createTodoTaskUpdater();

			fASTOptions = ILanguage.OPTION_NO_IMAGE_LOCATIONS
					| ILanguage.OPTION_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS;

			if (getSkipReferences() == SKIP_ALL_REFERENCES) {
				fASTOptions |= ILanguage.OPTION_SKIP_FUNCTION_BODIES;
			}

			fIndex.resetCacheCounters();
			fIndex.acquireReadLock();

			try {
				try {
					SubMonitor progress = SubMonitor.convert(monitor, 20);
					// Split into sources and headers, remove excluded sources.
					HashMap<Integer, List<IIndexFileLocation>> files = new HashMap<>();
					final ArrayList<IIndexFragmentFile> indexFilesToRemove = new ArrayList<>();
					extractFiles(files, indexFilesToRemove, progress.split(1));

					setResume(true, progress.split(1));

					// Remove files from index
					removeFilesInIndex(fFilesToRemove, indexFilesToRemove, progress.split(1));

					HashMap<Integer, List<IIndexFileLocation>> moreFiles = null;
					while (true) {
						int[] linkageIDs = getLinkagesToParse();
						progress.setWorkRemaining((linkageIDs.length + 2) * 2);
						for (int linkageID : linkageIDs) {
							final List<IIndexFileLocation> filesForLinkage = files.get(linkageID);
							if (filesForLinkage != null) {
								parseLinkage(linkageID, filesForLinkage, progress.split(1));
								for (Iterator<LocationTask> it = fOneLinkageTasks.values().iterator(); it.hasNext();) {
									LocationTask task = it.next();
									if (task.isCompleted())
										it.remove();
								}
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
									List<IIndexFileLocation> list = moreFiles.get(entry.getKey());
									if (list == null) {
										moreFiles.put(entry.getKey(), entry.getValue());
									} else {
										list.addAll(0, entry.getValue());
									}
								}
							}
							// Extract files from the urgent task.
							files = new HashMap<>();
							fFilesToUpdate = urgentTask.fFilesToUpdate;
							fForceNumberFiles = urgentTask.fForceNumberFiles;
							fFilesToRemove = urgentTask.fFilesToRemove;
							incrementRequestedFilesCount(fFilesToUpdate.length + fFilesToRemove.size());
							extractFiles(files, indexFilesToRemove, progress.split(1));
							removeFilesInIndex(fFilesToRemove, indexFilesToRemove, progress.split(1));
						}
					}
					setResume(false, progress.split(1));
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

	private void setResume(boolean value, IProgressMonitor monitor) throws InterruptedException, CoreException {
		fIndex.acquireWriteLock(monitor);
		try {
			fIndex.getWritableFragment().setProperty(IIndexFragment.PROPERTY_RESUME_INDEXER, String.valueOf(value));
		} finally {
			fIndex.releaseWriteLock();
		}
	}

	private void extractFiles(HashMap<Integer, List<IIndexFileLocation>> files, List<IIndexFragmentFile> filesToRemove,
			IProgressMonitor monitor) throws CoreException {
		final boolean forceAll = (fUpdateFlags & IIndexManager.UPDATE_ALL) != 0;
		final boolean checkTimestamps = (fUpdateFlags & IIndexManager.UPDATE_CHECK_TIMESTAMPS) != 0;
		final boolean checkFileContentsHash = (fUpdateFlags & IIndexManager.UPDATE_CHECK_CONTENTS_HASH) != 0;
		final boolean forceUnresolvedIncludes = (fUpdateFlags & IIndexManager.UPDATE_UNRESOLVED_INCLUDES) != 0;
		final boolean both = fIndexHeadersWithoutContext == UnusedHeaderStrategy.useBoth;
		int count = 0;
		int forceFirst = fForceNumberFiles;
		BitSet linkages = new BitSet();
		SubMonitor progress = SubMonitor.convert(monitor, fFilesToUpdate.length);
		for (final Object tu : fFilesToUpdate) {
			progress.split(1);
			final boolean force = forceAll || --forceFirst >= 0;
			final IIndexFileLocation ifl = fResolver.resolveFile(tu);
			if (ifl == null)
				continue;

			final IIndexFragmentFile[] indexFiles = fIndex.getWritableFiles(ifl);
			final boolean isSourceUnit = fResolver.isSourceUnit(tu);
			linkages.clear();
			final boolean regularContent = isRequiredInIndex(tu, ifl, isSourceUnit);
			final boolean indexedUnconditionally = fResolver.isIndexedUnconditionally(ifl);
			if (regularContent || indexedUnconditionally) {
				// Headers or sources required with a specific linkage.
				final UpdateKind updateKind = isSourceUnit ? UpdateKind.REQUIRED_SOURCE
						: regularContent && both ? UpdateKind.REQUIRED_HEADER : UpdateKind.ONE_LINKAGE_HEADER;
				if (regularContent || indexFiles.length == 0) {
					AbstractLanguage[] langs = fResolver.getLanguages(tu, fIndexHeadersWithoutContext);
					for (AbstractLanguage lang : langs) {
						int linkageID = lang.getLinkageID();
						boolean foundInLinkage = false;
						for (int i = 0; i < indexFiles.length; i++) {
							IIndexFragmentFile ifile = indexFiles[i];
							if (ifile != null && ifile.getLinkageID() == linkageID && ifile.hasContent()) {
								foundInLinkage = true;
								indexFiles[i] = null; // Take the file.
								boolean update = force || (forceUnresolvedIncludes && ifile.hasUnresolvedInclude())
										|| isModified(checkTimestamps, checkFileContentsHash, ifl, tu, ifile);
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
			}

			// Handle other files present in index.
			for (IIndexFragmentFile ifile : indexFiles) {
				if (ifile != null) {
					IIndexInclude ctx = ifile.getParsedInContext();
					if (ctx == null && !indexedUnconditionally && ifile.hasContent()) {
						filesToRemove.add(ifile);
						count++;
					} else {
						boolean update = force || (forceUnresolvedIncludes && ifile.hasUnresolvedInclude())
								|| isModified(checkTimestamps, checkFileContentsHash, ifl, tu, ifile);
						final int linkageID = ifile.getLinkageID();
						if (update && requestUpdate(linkageID, ifl, ifile, tu, UpdateKind.OTHER_HEADER)) {
							count++;
							linkages.set(linkageID);
						}
					}
				}
			}
			for (int lid = linkages.nextSetBit(0); lid >= 0; lid = linkages.nextSetBit(lid + 1)) {
				addPerLinkage(lid, ifl, files);
			}
		}
		synchronized (this) {
			incrementRequestedFilesCount(count - fFilesToUpdate.length);
			fFilesToUpdate = null;
		}
	}

	private void addPerLinkage(int linkageID, IIndexFileLocation ifl,
			HashMap<Integer, List<IIndexFileLocation>> files) {
		List<IIndexFileLocation> list = files.get(linkageID);
		if (list == null) {
			list = new ArrayList<>();
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

		// Source file
		if (isSourceUnit) {
			if (fIndexFilesWithoutConfiguration || fResolver.isFileBuildConfigured(tu))
				return true;
		}
		return false;
	}

	private boolean isModified(boolean checkTimestamps, boolean checkFileContentsHash, IIndexFileLocation ifl,
			Object tu, IIndexFragmentFile file) throws CoreException {
		if (checkTimestamps) {
			if (fResolver.getLastModified(ifl) != file.getTimestamp()
					|| computeFileSizeAndEncodingHashcode(ifl) != file.getSizeAndEncodingHashcode()) {
				if (checkFileContentsHash && computeFileContentsHash(tu) == file.getContentsHash()) {
					return false;
				}
				return true;
			}
		}
		return false;
	}

	private long computeFileContentsHash(Object tu) {
		FileContent codeReader = fResolver.getCodeReader(tu);
		return codeReader != null ? codeReader.getContentsHash() : 0;
	}

	private boolean requestUpdate(int linkageID, IIndexFileLocation ifl, IIndexFragmentFile ifile, Object tu,
			UpdateKind kind) {
		LinkageTask fileMap = createRequestMap(linkageID);
		return fileMap.requestUpdate(ifl, ifile, tu, kind, fOneLinkageTasks);
	}

	private LinkageTask createRequestMap(int linkageID) {
		LinkageTask map = findRequestMap(linkageID);
		if (map == null) {
			map = new LinkageTask(linkageID);
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
	protected void reportFileWrittenToIndex(FileInAST file, IIndexFragmentFile ifile) throws CoreException {
		final FileContentKey fck = file.fileContentKey;
		final IIndexFileLocation location = fck.getLocation();
		boolean wasCounted = false;
		UpdateKind kind = UpdateKind.OTHER_HEADER;
		LinkageTask map = findRequestMap(fck.getLinkageID());
		LocationTask locTask = null;
		if (map != null) {
			locTask = map.find(location);
			if (locTask != null) {
				kind = locTask.fKind;
				FileVersionTask v = locTask.findVersion(ifile);
				if (v != null) {
					wasCounted = v.fOutdated;
					v.setUpdated();
				} else {
					// We have added a version, the request is fulfilled.
					wasCounted = locTask.fCountedUnknownVersion;
					locTask.fCountedUnknownVersion = false;
				}
				locTask.fStoredAVersion = true;
			}
		}
		fIndexContentCache.remove(ifile);
		fIndexFilesCache.remove(file.fileContentKey.getLocation());

		LocationTask task = fOneLinkageTasks.remove(location);
		if (task != null && task != locTask) {
			if (task.fKind == UpdateKind.ONE_LINKAGE_HEADER && !task.isCompleted()) {
				task.fKind = UpdateKind.OTHER_HEADER;
				if (task.isCompleted()) {
					if (!wasCounted) {
						kind = UpdateKind.ONE_LINKAGE_HEADER;
						wasCounted = true;
					} else {
						reportFile(wasCounted, UpdateKind.ONE_LINKAGE_HEADER);
					}
				}
			}
		}
		reportFile(wasCounted, kind);
	}

	private void removeFilesInIndex(List<Object> filesToRemove, List<IIndexFragmentFile> indexFilesToRemove,
			IProgressMonitor monitor) throws InterruptedException, CoreException {
		if (!filesToRemove.isEmpty() || !indexFilesToRemove.isEmpty()) {
			SubMonitor progress = SubMonitor.convert(monitor, 1 + filesToRemove.size() + indexFilesToRemove.size());
			fIndex.acquireWriteLock(progress.split(1));
			try {
				for (Object tu : filesToRemove) {
					progress.split(1);
					IIndexFileLocation ifl = fResolver.resolveFile(tu);
					if (ifl == null)
						continue;
					IIndexFragmentFile[] ifiles = fIndex.getWritableFiles(ifl);
					for (IIndexFragmentFile ifile : ifiles) {
						fIndex.clearFile(ifile);
					}
					incrementRequestedFilesCount(-1);
				}
				for (IIndexFragmentFile ifile : indexFilesToRemove) {
					progress.split(1);
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

		SubMonitor progress = SubMonitor.convert(monitor, files.size() + 1);
		int maxPriority = Integer.MIN_VALUE;
		int minPriority = Integer.MAX_VALUE;
		Map<Integer, List<IIndexFileLocation>> filesByPriority = new HashMap<>();
		for (IIndexFileLocation file : files) {
			int priority = fResolver.getIndexingPriority(file);
			List<IIndexFileLocation> list = filesByPriority.get(priority);
			if (list == null) {
				list = new LinkedList<>();
				filesByPriority.put(priority, list);
			}
			list.add(file);

			if (maxPriority < priority)
				maxPriority = priority;
			if (minPriority > priority)
				minPriority = priority;
		}

		for (int priority = maxPriority; priority >= minPriority; priority--) {
			List<IIndexFileLocation> filesAtPriority = filesByPriority.get(priority);
			if (filesAtPriority == null)
				continue;

			// First parse the required sources.
			for (Iterator<IIndexFileLocation> it = filesAtPriority.iterator(); it.hasNext();) {
				IIndexFileLocation ifl = it.next();
				LocationTask locTask = map.find(ifl);
				if (locTask == null || locTask.isCompleted()) {
					it.remove();
				} else if (locTask.fKind == UpdateKind.REQUIRED_SOURCE) {
					if (hasUrgentTasks())
						return;
					final Object tu = locTask.fTu;
					final IScannerInfo scannerInfo = getScannerInfo(linkageID, tu);
					parseFile(tu, getLanguage(tu, linkageID), ifl, scannerInfo, null, progress.split(1));
				}
			}

			// Files with context.
			for (Iterator<IIndexFileLocation> it = filesAtPriority.iterator(); it.hasNext();) {
				IIndexFileLocation ifl = it.next();
				LocationTask locTask = map.find(ifl);
				if (locTask == null || locTask.isCompleted()) {
					it.remove();
				} else {
					for (FileVersionTask versionTask : locTask.fVersionTasks) {
						if (versionTask.fOutdated) {
							if (hasUrgentTasks())
								return;
							parseVersionInContext(linkageID, map, ifl, versionTask, locTask.fTu,
									new LinkedHashSet<IIndexFile>(), progress.split(1));
						}
					}
				}
			}

			// Files without context.
			for (Iterator<IIndexFileLocation> it = filesAtPriority.iterator(); it.hasNext();) {
				IIndexFileLocation ifl = it.next();
				LocationTask locTask = map.find(ifl);
				if (locTask == null || locTask.isCompleted()) {
					it.remove();
				} else {
					if (locTask.needsVersion()) {
						if (hasUrgentTasks())
							return;
						final Object tu = locTask.fTu;
						final IScannerInfo scannerInfo = getScannerInfo(linkageID, tu);
						parseFile(tu, getLanguage(tu, linkageID), ifl, scannerInfo, null, progress.split(1));
						if (locTask.isCompleted())
							it.remove();

					}
				}
			}

			// Delete remaining files.
			fIndex.acquireWriteLock(progress.split(1));
			try {
				for (IIndexFileLocation ifl : filesAtPriority) {
					LocationTask locTask = map.find(ifl);
					if (locTask != null && !locTask.isCompleted()) {
						if (!locTask.needsVersion()) {
							progress.split(1);
							if (hasUrgentTasks())
								return;
							Iterator<FileVersionTask> it = locTask.fVersionTasks.iterator();
							while (it.hasNext()) {
								FileVersionTask v = it.next();
								if (v.fOutdated) {
									fIndex.clearFile(v.fIndexFile);
									reportFile(true, locTask.fKind);
									locTask.removeVersionTask(it);
									fIndexContentCache.remove(v.fIndexFile);
									fIndexFilesCache.remove(ifl);
								}
							}
						}
					}
				}
			} finally {
				fIndex.releaseWriteLock();
			}
		}
	}

	private void parseVersionInContext(int linkageID, LinkageTask map, IIndexFileLocation ifl,
			final FileVersionTask versionTask, Object tu, LinkedHashSet<IIndexFile> safeGuard, IProgressMonitor monitor)
			throws CoreException, InterruptedException {
		final IIndexFragmentFile headerFile = versionTask.fIndexFile;

		SubMonitor progress = SubMonitor.convert(monitor, 10);
		final int safeguardSize = safeGuard.size();
		while (true) {
			progress.setWorkRemaining(10);
			// Look for a context and parse the file.
			IIndexFragmentFile ctxFile = findContextFile(linkageID, map, versionTask, safeGuard, progress.split(1));
			if (ctxFile == null || ctxFile == headerFile)
				return;

			Object contextTu = fResolver.getInputFile(ctxFile.getLocation());
			if (contextTu == null)
				return;

			final IScannerInfo scannerInfo = getScannerInfo(linkageID, contextTu);
			final AbstractLanguage language = getLanguage(contextTu, linkageID);
			final FileContext ctx = new FileContext(ctxFile, headerFile);
			Set<IIndexFile> dependencies = null;
			boolean done = false;
			while (!done) {
				progress.setWorkRemaining(9);
				done = true;
				DependsOnOutdatedFileException d = parseFile(tu, language, ifl, scannerInfo, ctx, progress.split(1));
				if (d != null) {
					// File was not parsed, because there is a dependency that needs to be handled before.
					if (dependencies == null)
						dependencies = new HashSet<>();
					if (dependencies.add(d.fIndexFile)) {
						if (parseFile(d.fTu, language, d.fIndexFile.getLocation(), scannerInfo,
								new FileContext(ctxFile, d.fIndexFile), progress.split(1)) == null) {
							done = false;
						}
					}
				}
			}
			if (!ctx.fLostPragmaOnceSemantics)
				return;

			// Try the next context.
			restoreSet(safeGuard, safeguardSize);
		}
	}

	private IScannerInfo getScannerInfo(int linkageID, Object contextTu) {
		final IScannerInfo scannerInfo = fResolver.getBuildConfiguration(linkageID, contextTu);
		if (scannerInfo instanceof ExtendedScannerInfo) {
			ExtendedScannerInfo extendedScannerInfo = (ExtendedScannerInfo) scannerInfo;
			extendedScannerInfo.setIncludeExportPatterns(getIncludeExportPatterns());
			extendedScannerInfo.setParserSettings(createParserSettings());
		}
		return scannerInfo;
	}

	private void restoreSet(LinkedHashSet<?> set, int restoreSize) {
		for (Iterator<?> it = set.iterator(); it.hasNext();) {
			it.next();
			if (restoreSize == 0) {
				it.remove();
			} else {
				restoreSize--;
			}
		}
	}

	private IIndexFragmentFile findContextFile(int linkageID, LinkageTask map, final FileVersionTask versionTask,
			LinkedHashSet<IIndexFile> safeGuard, IProgressMonitor monitor) throws CoreException, InterruptedException {
		IIndexFragmentFile ctxFile = versionTask.fIndexFile;
		while (true) {
			IIndexInclude ctxInclude = ctxFile.getParsedInContext();
			if (ctxInclude == null)
				return ctxFile;

			IIndexFragmentFile nextCtx = (IIndexFragmentFile) ctxInclude.getIncludedBy();
			if (nextCtx == null)
				return nextCtx;

			// Found a recursion.
			if (!safeGuard.add(nextCtx))
				return null;

			final IIndexFileLocation ctxIfl = nextCtx.getLocation();
			LocationTask ctxTask = map.find(ctxIfl);
			if (ctxTask != null) {
				FileVersionTask ctxVersionTask = ctxTask.findVersion(nextCtx);
				if (ctxVersionTask != null && ctxVersionTask.fOutdated) {
					// Handle the context first.
					parseVersionInContext(linkageID, map, ctxIfl, ctxVersionTask, ctxTask.fTu, safeGuard, monitor);
					if (ctxVersionTask.fOutdated // This is unexpected.
							|| !versionTask.fOutdated) { // Our file was parsed.
						return null;
					}

					// The file is no longer a context, look for a different one.
					nextCtx = ctxFile;
				}
			}
			ctxFile = nextCtx;
		}
	}

	private DependsOnOutdatedFileException parseFile(Object tu, AbstractLanguage lang, IIndexFileLocation ifl,
			IScannerInfo scanInfo, FileContext ctx, IProgressMonitor monitor)
			throws CoreException, InterruptedException {
		SubMonitor progress = SubMonitor.convert(monitor, 21);
		boolean resultCacheCleared = false;
		IPath path = getLabel(ifl);
		Throwable th = null;
		try {
			if (fShowActivity) {
				trace("Indexer: parsing " + path.toOSString()); //$NON-NLS-1$
			}
			progress.subTask(
					getMessage(MessageKind.parsingFileTask, path.lastSegment(), path.removeLastSegments(1).toString()));
			FileContent codeReader = fResolver.getCodeReader(tu);

			long start = System.currentTimeMillis();
			IASTTranslationUnit ast = createAST(lang, codeReader, scanInfo, fASTOptions, ctx, progress.split(10));
			fStatistics.fParsingTime += System.currentTimeMillis() - start;
			if (ast == null) {
				++fStatistics.fTooManyTokensCount;
			} else {
				writeToIndex(lang.getLinkageID(), ast, codeReader, ctx, progress.split(10));
				resultCacheCleared = true; // The cache was cleared while writing to the index.
			}
			if (fShowActivity) {
				long time = System.currentTimeMillis() - start;
				trace("Indexer: processed " + path.toOSString() + " [" + time + " ms]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		} catch (OperationCanceledException e) {
		} catch (RuntimeException e) {
			final Throwable cause = e.getCause();
			if (cause instanceof DependsOnOutdatedFileException)
				return (DependsOnOutdatedFileException) cause;
			th = e;
		} catch (StackOverflowError | CoreException | AssertionError e) {
			th = e;
		} catch (OutOfMemoryError e) {
			if (--fSwallowOutOfMemoryError < 0)
				throw e;
			th = e;
		}
		if (th != null) {
			swallowError(path, th);
		}

		if (!resultCacheCleared) {
			// If the result cache has not been cleared, clear it under a write lock to reduce
			// interference with index readers.
			fIndex.acquireWriteLock(progress.split(1));
			try {
				fIndex.clearResultCache();
			} finally {
				fIndex.releaseWriteLock();
			}
		}
		return null;
	}

	private AbstractLanguage getLanguage(Object tu, int linkageID) {
		for (AbstractLanguage language : fResolver.getLanguages(tu, UnusedHeaderStrategy.useBoth)) {
			if (language.getLinkageID() == linkageID) {
				return language;
			}
		}
		return null;
	}

	private IPath getLabel(IIndexFileLocation ifl) {
		String fullPath = ifl.getFullPath();
		if (fullPath != null) {
			return new Path(fullPath);
		}
		IPath path = IndexLocationFactory.getAbsolutePath(ifl);
		if (path != null) {
			return path;
		}
		URI uri = ifl.getURI();
		return new Path(EFSExtensionManager.getDefault().getPathFromURI(uri));
	}

	private void swallowError(IPath file, Throwable e) throws CoreException {
		IStatus s;
		/*
		 * If the thrown CoreException is for a STATUS_PDOM_TOO_LARGE, we don't want to
		 * swallow this one.
		 */
		if (e instanceof CoreException) {
			s = ((CoreException) e).getStatus();
			if (s.getCode() == CCorePlugin.STATUS_PDOM_TOO_LARGE) {
				if (CCorePlugin.PLUGIN_ID.equals(s.getPlugin()))
					throw (CoreException) e;
			}

			// Mask errors in order to avoid dialog from platform
			Throwable exception = s.getException();
			if (exception != null) {
				Throwable masked = getMaskedException(exception);
				if (masked != exception) {
					e = masked;
					exception = null;
				}
			}
			if (exception == null) {
				s = new Status(s.getSeverity(), s.getPlugin(), s.getCode(), s.getMessage(), e);
			}
		} else {
			e = getMaskedException(e);
			s = createStatus(getMessage(MessageKind.errorWhileParsing, file), e);
		}
		logError(s);
		if (fShowProblems) {
			reportException(e);
		}
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

	private final IASTTranslationUnit createAST(AbstractLanguage language, FileContent codeReader,
			IScannerInfo scanInfo, int options, FileContext ctx, IProgressMonitor monitor) throws CoreException {
		if (codeReader == null) {
			return null;
		}
		if (fTranslationUnitSizeLimit > 0
				&& fResolver.getFileSize(codeReader.getFileLocation()) > fTranslationUnitSizeLimit) {
			if (fShowActivity) {
				trace("Indexer: Skipping large file " + codeReader.getFileLocation()); //$NON-NLS-1$
			}
			return null;
		}
		final IIndexFile[] ctx2header = ctx == null ? null : new IIndexFile[] { ctx.fContext, ctx.fOldFile };
		if (fCodeReaderFactory == null) {
			InternalFileContentProvider fileContentProvider = createInternalFileContentProvider();
			if (fIsFastIndexer) {
				IndexBasedFileContentProvider ibfcp = new IndexBasedFileContentProvider(fIndex, fResolver,
						language.getLinkageID(), fileContentProvider, this);
				ibfcp.setContextToHeaderGap(ctx2header);
				ibfcp.setFileSizeLimit(fIncludedFileSizeLimit);
				ibfcp.setHeadersToIndexAllVersions(fHeadersToIndexAllVersions);
				ibfcp.setIndexAllHeaderVersions(fIndexAllHeaderVersions);
				fCodeReaderFactory = ibfcp;
			} else {
				fCodeReaderFactory = fileContentProvider;
			}
			fCodeReaderFactory.setIncludeResolutionHeuristics(createIncludeHeuristics());
		} else if (fIsFastIndexer) {
			final IndexBasedFileContentProvider ibfcp = (IndexBasedFileContentProvider) fCodeReaderFactory;
			ibfcp.setContextToHeaderGap(ctx2header);
			ibfcp.setLinkage(language.getLinkageID());
		}

		IASTTranslationUnit ast = language.getASTTranslationUnit(codeReader, scanInfo, fCodeReaderFactory, fIndex,
				options, getLogService());
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		return ast;
	}

	private InternalFileContentProvider createInternalFileContentProvider() {
		final IncludeFileContentProvider fileContentProvider = createReaderFactory();
		if (fileContentProvider instanceof InternalFileContentProvider)
			return (InternalFileContentProvider) fileContentProvider;

		throw new IllegalArgumentException("Invalid file content provider"); //$NON-NLS-1$
	}

	private void writeToIndex(final int linkageID, IASTTranslationUnit ast, FileContent codeReader, FileContext ctx,
			IProgressMonitor monitor) throws CoreException, InterruptedException {
		SubMonitor progress = SubMonitor.convert(monitor, 3);
		HashSet<FileContentKey> enteredFiles = new HashSet<>();
		ArrayList<FileInAST> orderedFileKeys = new ArrayList<>();

		final IIndexFileLocation topIfl = fResolver.resolveASTPath(ast.getFilePath());
		ISignificantMacros significantMacros = ast.isHeaderUnit() ? ast.getSignificantMacros()
				: ISignificantMacros.NONE;
		FileContentKey topKey = new FileContentKey(linkageID, topIfl, significantMacros);
		enteredFiles.add(topKey);
		IDependencyTree tree = ast.getDependencyTree();
		IASTInclusionNode[] inclusions = tree.getInclusions();
		for (IASTInclusionNode inclusion : inclusions) {
			collectOrderedFileKeys(linkageID, inclusion, enteredFiles, orderedFileKeys);
		}

		IIndexFragmentFile newFile = selectIndexFile(linkageID, topIfl, significantMacros);
		if (ctx != null) {
			orderedFileKeys.add(new FileInAST(topKey, codeReader));
			// File can be reused
			ctx.fNewFile = newFile;
		} else if (newFile == null) {
			orderedFileKeys.add(new FileInAST(topKey, codeReader));
		}

		FileInAST[] fileKeys = orderedFileKeys.toArray(new FileInAST[orderedFileKeys.size()]);
		try {
			// The default processing is handled by the indexer task.
			PDOMWriter.Data data = new PDOMWriter.Data(ast, fileKeys, fIndex);
			int storageLinkageID = process(ast, data);
			if (storageLinkageID != ILinkage.NO_LINKAGE_ID) {
				IASTComment[] comments = ast.getComments();
				data.fReplacementHeaders = extractReplacementHeaders(comments, progress.split(1));

				addSymbols(data, storageLinkageID, ctx, progress.split(1));

				// Update task markers.
				if (fTodoTaskUpdater != null) {
					Set<IIndexFileLocation> locations = new HashSet<>();
					for (FileInAST file : data.fSelectedFiles) {
						locations.add(file.fileContentKey.getLocation());
					}
					fTodoTaskUpdater.updateTasks(comments, locations.toArray(new IIndexFileLocation[locations.size()]));
				}
			}

			// Contributed processors now have an opportunity to examine the AST.
			List<IPDOMASTProcessor> processors = PDOMASTProcessorManager.getProcessors(ast);
			progress.setWorkRemaining(processors.size());
			for (IPDOMASTProcessor processor : processors) {
				data = new PDOMWriter.Data(ast, fileKeys, fIndex);
				storageLinkageID = processor.process(ast, data);
				if (storageLinkageID != ILinkage.NO_LINKAGE_ID)
					addSymbols(data, storageLinkageID, ctx, progress.split(1));
			}
		} catch (CoreException | RuntimeException | Error e) {
			// Avoid parsing files again, that caused an exception to be thrown.
			withdrawRequests(linkageID, fileKeys);
			throw e;
		}
	}

	private void collectOrderedFileKeys(final int linkageID, IASTInclusionNode inclusion,
			Set<FileContentKey> enteredFiles, List<FileInAST> orderedFileKeys) throws CoreException {
		final IASTPreprocessorIncludeStatement include = inclusion.getIncludeDirective();
		if (include.createsAST()) {
			final IIndexFileLocation ifl = fResolver.resolveASTPath(include.getPath());
			FileContentKey fileKey = new FileContentKey(linkageID, ifl, include.getSignificantMacros());
			final boolean isFirstEntry = enteredFiles.add(fileKey);
			IASTInclusionNode[] nested = inclusion.getNestedInclusions();
			for (IASTInclusionNode element : nested) {
				collectOrderedFileKeys(linkageID, element, enteredFiles, orderedFileKeys);
			}
			if (isFirstEntry && selectIndexFile(linkageID, ifl, include.getSignificantMacros()) == null) {
				orderedFileKeys.add(new FileInAST(include, fileKey));
			}
		}
	}

	private void withdrawRequests(int linkageID, FileInAST[] fileKeys) {
		LinkageTask map = findRequestMap(linkageID);
		if (map != null) {
			for (FileInAST fileKey : fileKeys) {
				LocationTask locTask = map.find(fileKey.fileContentKey.getLocation());
				if (locTask != null) {
					if (locTask.fCountedUnknownVersion) {
						locTask.fCountedUnknownVersion = false;
						reportFile(true, locTask.fKind);
					} else {
						for (FileVersionTask fc : locTask.fVersionTasks) {
							if (fc.fOutdated) {
								reportFile(true, locTask.fKind);
								fc.setUpdated();
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Parses comments to extract replacement headers from <code>@headername{header}</code> and
	 * {@code IWYU pragma: private}.
	 *
	 * @return replacement headers keyed by file paths
	 */
	private Map<String, String> extractReplacementHeaders(IASTComment[] comments, IProgressMonitor monitor) {
		Map<String, String> replacementHeaders = new HashMap<>();
		StringBuilder text = new StringBuilder();
		IASTFileLocation carryoverLocation = null;
		for (int i = 0; i < comments.length; i++) {
			IASTComment comment = comments[i];
			IASTFileLocation location = comment.getFileLocation();
			if (location == null)
				continue;
			String fileName = location.getFileName();
			if (replacementHeaders.containsKey(fileName))
				continue;
			char[] commentChars = comment.getComment();
			if (commentChars.length <= 2)
				continue;
			if (carryoverLocation == null || !location.getFileName().equals(carryoverLocation.getFileName())
					|| location.getStartingLineNumber() != carryoverLocation.getEndingLineNumber() + 1) {
				text.delete(0, text.length());
			}
			carryoverLocation = null;
			text.append(commentChars, 2, commentChars.length - 2);
			// Look for @headername{header}.
			Matcher matcher = HEADERNAME_PATTERN.matcher(text);
			if (matcher.find()) {
				String header = matcher.group("header"); //$NON-NLS-1$
				if (header == null) {
					header = ""; //$NON-NLS-1$
				} else {
					// Normalize the header list.
					header = header.replace(" or ", ",").replace(" ", ""); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
				}
				replacementHeaders.put(fileName, header);
				continue;
			}
			if (fPragmaPrivatePattern != null) {
				// Look for IWYU pragma: private
				matcher = fPragmaPrivatePattern.matcher(text);
				if (matcher.find()) {
					if (!isWhitespace(text, 0, matcher.start()))
						continue; // Extraneous text before the pragma.
					if (isWhitespace(text, matcher.end(), text.length())) {
						String header = matcher.group("header"); //$NON-NLS-1$
						if (header == null)
							header = ""; //$NON-NLS-1$
						replacementHeaders.put(fileName, header);
						continue;
					}
					// Handle the case when a IWYU pragma is split between two comment lines as:
					//   IWYU pragma: private,
					//   include "header"
					if (text.charAt(matcher.end()) == ',' && isWhitespace(text, matcher.end() + 1, text.length())) {
						// Defer processing until the next comment, which will be appended to this one.
						carryoverLocation = location;
					}
				}
			}
		}
		return replacementHeaders;
	}

	private boolean isWhitespace(CharSequence text, int start, int end) {
		while (start < end) {
			if (text.charAt(start++) > ' ')
				return false;
		}
		return true;
	}

	public final IndexFileContent getFileContent(int linkageID, IIndexFileLocation ifl, IIndexFile file)
			throws CoreException, DependsOnOutdatedFileException {
		LinkageTask map = findRequestMap(linkageID);
		if (map != null) {
			LocationTask request = map.find(ifl);
			if (request != null) {
				FileVersionTask task = request.findVersion(file);
				if (task != null && task.fOutdated)
					throw new DependsOnOutdatedFileException(request.fTu, task.fIndexFile);
			}
		}
		IndexFileContent fc = fIndexContentCache.get(file);
		if (fc == null) {
			fc = new IndexFileContent(file);
			fIndexContentCache.put(file, fc);
		}
		return fc;
	}

	IIndexFragmentFile selectIndexFile(int linkageID, IIndexFileLocation ifl, ISignificantMacros sigMacros)
			throws CoreException {
		LinkageTask map = findRequestMap(linkageID);
		if (map != null) {
			LocationTask locTask = map.find(ifl);
			if (locTask != null) {
				FileVersionTask task = locTask.findVersion(sigMacros);
				if (task != null) {
					return task.fOutdated ? null : task.fIndexFile;
				}
			}
		}

		IIndexFragmentFile[] files = getAvailableIndexFiles(linkageID, ifl);
		for (IIndexFragmentFile file : files) {
			if (sigMacros.equals(file.getSignificantMacros()))
				return file;
		}
		return null;
	}

	public IIndexFile selectIndexFile(int linkageID, IIndexFileLocation ifl, IMacroDictionary md) throws CoreException {
		LinkageTask map = findRequestMap(linkageID);
		if (map != null) {
			LocationTask request = map.find(ifl);
			if (request != null) {
				for (FileVersionTask fileVersion : request.fVersionTasks) {
					final IIndexFile indexFile = fileVersion.fIndexFile;
					if (md.satisfies(indexFile.getSignificantMacros())) {
						if (fileVersion.fOutdated)
							return null;
						return indexFile;
					}
				}
			}
		}

		IIndexFile[] files = getAvailableIndexFiles(linkageID, ifl);
		for (IIndexFile indexFile : files) {
			if (md.satisfies(indexFile.getSignificantMacros())) {
				return indexFile;
			}
		}
		return null;
	}

	public IIndexFragmentFile[] getAvailableIndexFiles(int linkageID, IIndexFileLocation ifl) throws CoreException {
		IIndexFragmentFile[] files = fIndexFilesCache.get(ifl);
		if (files == null) {
			IIndexFragmentFile[] fragFiles = fIndex.getWritableFiles(linkageID, ifl);
			int j = 0;
			for (int i = 0; i < fragFiles.length; i++) {
				if (fragFiles[i].hasContent()) {
					if (j != i)
						fragFiles[j] = fragFiles[i];
					j++;
				}
			}
			if (j == fragFiles.length) {
				files = fragFiles;
			} else {
				files = new IIndexFragmentFile[j];
				System.arraycopy(fragFiles, 0, files, 0, j);
			}
			fIndexFilesCache.put(ifl, files);
		}
		return files;
	}
}
