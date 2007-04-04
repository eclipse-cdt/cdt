/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.pdom.indexer;

import java.net.URI;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.AbstractLanguage;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.CContentTypes;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.pdom.IndexerProgress;
import org.eclipse.cdt.internal.core.pdom.PDOMWriter;
import org.eclipse.cdt.internal.core.pdom.db.ChunkCache;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;

public abstract class PDOMIndexerTask extends PDOMWriter implements IPDOMIndexerTask {
	private static final Object NO_CONTEXT = new Object();
	private static final int MAX_ERRORS = 500;
	private static final String TRUE = "true"; //$NON-NLS-1$
	
	private AbstractPDOMIndexer fIndexer;
	protected Map/*<IIndexFileLocation, Object>*/ fContextMap = new HashMap/*<IIndexFileLocation, Object>*/();
	private boolean fCheckTimestamps= false;
	private List fFilesUpFront= new ArrayList();
	private String fDummyFileName;
	private URI fDummyFileURI;

	protected PDOMIndexerTask(AbstractPDOMIndexer indexer) {
		fIndexer= indexer;
		setShowActivity(checkDebugOption(TRACE_ACTIVITY, TRUE));
		setShowProblems(checkDebugOption(TRACE_PROBLEMS, TRUE));
		if (checkProperty(IndexerPreferences.KEY_SKIP_ALL_REFERENCES)) {
			setSkipReferences(SKIP_ALL_REFERENCES);
		}
		else if (checkProperty(IndexerPreferences.KEY_SKIP_TYPE_REFERENCES)) {
			setSkipReferences(SKIP_TYPE_REFERENCES);
		}
	}

	final public IPDOMIndexer getIndexer() {
		return fIndexer;
	}
	
	final public ICProject getProject() {
		return fIndexer.getProject();
	}
	
	final public IndexerProgress getProgressInformation() {
		return super.getProgressInformation();
	}
	
	final public void setCheckTimestamps(boolean val) {
		fCheckTimestamps= val;
	}
	
	final public void setParseUpFront() {
		fFilesUpFront.addAll(Arrays.asList(fIndexer.getFilesToParseUpFront()));
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

	/**
	 * Figurues out whether all files (sources without config, headers not included)
	 * should be parsed.
	 * @since 4.0
	 */
	final protected boolean getIndexAllFiles() {
		return checkProperty(IndexerPreferences.KEY_INDEX_ALL_FILES);
	}

	private boolean checkProperty(String key) {
		return TRUE.equals(getIndexer().getProperty(key));
	}

	private IASTTranslationUnit createAST(ITranslationUnit tu, int options, IProgressMonitor pm) throws CoreException {
		IPath path = tu.getLocation();
		if (path == null) {
			return null;
		}
		ILanguage language = tu.getLanguage();
		if (! (language instanceof AbstractLanguage))
			return null;

		// skip if no scanner info
		IScannerInfo scanner= tu.getScannerInfo(getIndexAllFiles());
		if (scanner == null) {
			return null;
		}
		CodeReader codeReader = tu.getCodeReader();
		if (codeReader == null) {
			return null;
		}

		return createAST((AbstractLanguage)language, codeReader, scanner, options, pm);
	}

	/**
	 * Called to create the ast for a translation unit or a pre-parsed file. 
	 * May return <code>null</code>.
	 * @see #parseTUs(IWritableIndex, int, Collection, Collection, IProgressMonitor)
	 * @since 4.0
	 */
	abstract protected IASTTranslationUnit createAST(AbstractLanguage lang, CodeReader codeReader, IScannerInfo scanInfo, int options, IProgressMonitor pm) throws CoreException;

	/**
	 * Convenience method for subclasses, parses the files calling out to the methods 
	 * {@link #createAST(AbstractLanguage, CodeReader, IScannerInfo, int, IProgressMonitor)}, 
	 * {@link #needToUpdate(IIndexFileLocation)}, 
	 * {@link #addSymbols(IASTTranslationUnit, IWritableIndex, int, IProgressMonitor)}
	 * {@link #postAddToIndex(IIndexFileLocation, IIndexFile)},
	 * {@link #getLastModified(IIndexFileLocation)} and
	 * {@link #findLocation(String)}
	 * @since 4.0
	 */
	protected void parseTUs(IWritableIndex index, int readlockCount, Collection sources, Collection headers, IProgressMonitor monitor) throws CoreException, InterruptedException {
		int options= 0;
		if (checkProperty(IndexerPreferences.KEY_SKIP_ALL_REFERENCES)) {
			options |= AbstractLanguage.OPTION_SKIP_FUNCTION_BODIES;
		}
		for (Iterator iter = fFilesUpFront.iterator(); iter.hasNext();) {
			String upfront= (String) iter.next();
			parseUpFront(upfront, options, index, readlockCount, monitor);
		}
		
		// sources first
		for (Iterator iter = sources.iterator(); iter.hasNext();) {
			if (monitor.isCanceled()) 
				return;
			ITranslationUnit tu = (ITranslationUnit) iter.next();
			final IIndexFileLocation ifl = IndexLocationFactory.getIFL(tu);
			if (fCheckTimestamps && !isOutdated(tu, ifl, index)) {
				updateInfo(0,0,-1);
			}
			else if (needToUpdate(ifl)) {
				parseTU(tu, options, index, readlockCount, monitor);
			}
		}

		// headers with context
		for (Iterator iter = headers.iterator(); iter.hasNext();) {
			if (monitor.isCanceled()) 
				return;
			ITranslationUnit tu = (ITranslationUnit) iter.next();
			IIndexFileLocation location = IndexLocationFactory.getIFL(tu);
			if (fCheckTimestamps && !isOutdated(tu, location, index)) {
				updateInfo(0,0,-1);
				iter.remove();
			}
			else if (!needToUpdate(location)) {
				iter.remove();
			} 
			else {
				ITranslationUnit context= findContext(index, location);
				if (context != null) {
					parseTU(context, options, index, readlockCount, monitor);
				}
			}
		}

		// headers without context
		if (getIndexAllFiles()) {
			for (Iterator iter = headers.iterator(); iter.hasNext();) {
				if (monitor.isCanceled()) 
					return;
				ITranslationUnit tu = (ITranslationUnit) iter.next();
				final IIndexFileLocation ifl = IndexLocationFactory.getIFL(tu);
				if (fCheckTimestamps && !isOutdated(tu, ifl, index)) {
					updateInfo(0,0,-1);
					iter.remove();
				} 
				else if (!needToUpdate(ifl)) {
					iter.remove();
				}
				else {
					parseTU(tu, options, index, readlockCount, monitor);
				}
			}
		}
	}
	
	private boolean isOutdated(ITranslationUnit tu, IIndexFileLocation ifl, IIndex index) throws CoreException {
		boolean outofdate= true;
		IResource res= tu.getResource();
		if (res != null) {
			IIndexFile indexFile= index.getFile(ifl);
			if (indexFile != null) {
				if (res.getLocalTimeStamp() == indexFile.getTimestamp()) {
					outofdate= false;
				}
			}
		}
		return outofdate;
	}


	private void parseTU(ITranslationUnit tu, int options, IWritableIndex index, int readlockCount, IProgressMonitor pm) throws CoreException, InterruptedException {
		IPath path= tu.getPath();
		try {
			if (fShowActivity) {
				System.out.println("Indexer: parsing " + path.toOSString()); //$NON-NLS-1$
			}
			pm.subTask(MessageFormat.format(Messages.PDOMIndexerTask_parsingFileTask,
					new Object[]{path.lastSegment(), path.removeLastSegments(1).toString()}));
			long start= System.currentTimeMillis();
			IASTTranslationUnit ast= createAST(tu, options, pm);
			fStatistics.fParsingTime += System.currentTimeMillis()-start;
			if (ast != null) {
				addSymbols(ast, index, readlockCount, pm);
			}
		}
		catch (CoreException e) {
			swallowError(path, e); 
		}
		catch (RuntimeException e) {
			swallowError(path, e); 
		}
		catch (Error e) {
			swallowError(path, e); 
		}
	}

	private void parseUpFront(String file, int options, IWritableIndex index, int readlockCount, IProgressMonitor pm) throws CoreException, InterruptedException {
		file= file.trim();
		if (file.length() == 0) {
			return;
		}
		IPath path= new Path(file);
		try {
			if (fShowActivity) {
				System.out.println("Indexer: parsing " + file + " up front"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			pm.subTask(MessageFormat.format(Messages.PDOMIndexerTask_parsingFileTask,
					new Object[]{path.lastSegment(), path.removeLastSegments(1).toString()}));
			long start= System.currentTimeMillis();

			IASTTranslationUnit ast= null;
			final IProject project = getProject().getProject();
			IContentType ct= CContentTypes.getContentType(project, file);
			if (ct != null) {
				ILanguage l = LanguageManager.getInstance().getLanguage(ct);
				if (l instanceof AbstractLanguage) {
					AbstractLanguage lang= (AbstractLanguage) l;
					IScannerInfoProvider provider= CCorePlugin.getDefault().getScannerInfoProvider(project);
					IScannerInfo scanInfo;
					if (provider != null) { 
						scanInfo= provider.getScannerInformation(project);
					}
					else {
						scanInfo= new ScannerInfo();
					}
					String code= "#include \"" + file + "\"\n"; //$NON-NLS-1$ //$NON-NLS-2$
					if (fDummyFileName == null) {
						fDummyFileName= project.getLocation().append("___").toString(); //$NON-NLS-1$
						fDummyFileURI= findLocation(fDummyFileName).getURI();
					}
					CodeReader codeReader= new CodeReader(fDummyFileName, code.toCharArray());
					ast= createAST(lang, codeReader, scanInfo, options, pm);
				}
			}
				
			fStatistics.fParsingTime += System.currentTimeMillis()-start;
			if (ast != null) {
				addSymbols(ast, index, readlockCount, pm);
				updateInfo(-1, +1, 0);
			}
		}
		catch (CoreException e) {
			swallowError(path, e); 
		}
		catch (RuntimeException e) {
			swallowError(path, e); 
		}
		catch (Error e) {
			swallowError(path, e); 
		}
	}

	/**
	 * Overriders must call super.needToUpdate(). If <code>false</code> is returned
	 * this must be passed on to their caller:
	 * <pre>
	 *   if (super.needToUpdate()) {
	 *      // your code
	 *   }
	 *   return false;
	 */
	protected boolean needToUpdate(IIndexFileLocation fileLoc) throws CoreException {
		return fDummyFileURI==null || !fDummyFileURI.equals(fileLoc.getURI());
	}
	
	private void swallowError(IPath file, Throwable e) throws CoreException {
		IStatus status= CCorePlugin.createStatus(
				MessageFormat.format(Messages.PDOMIndexerTask_errorWhileParsing, new Object[]{file}), e);
		CCorePlugin.log(status);
		if (++fStatistics.fErrorCount > MAX_ERRORS) {
			throw new CoreException(CCorePlugin.createStatus(
					MessageFormat.format(Messages.PDOMIndexerTask_tooManyIndexProblems, new Object[]{getIndexer().getProject().getElementName()})));
		}
	}

	private ITranslationUnit findContext(IIndex index, IIndexFileLocation location) {
		Object cachedContext= fContextMap.get(location);
		if (cachedContext != null) {
			return cachedContext == NO_CONTEXT ? null : (ITranslationUnit) cachedContext;
		}

		fContextMap.put(location, NO_CONTEXT); // prevent recursion
		IIndexFile pdomFile;
		try {
			pdomFile = index.getFile(location);
			if (pdomFile != null) {
				ICProject project= getIndexer().getProject();
				IIndexInclude[] includedBy = index.findIncludedBy(pdomFile, IIndex.DEPTH_ZERO);
				ArrayList/*<IIndexFileLocation>*/ paths= new ArrayList/*<IIndexFileLocation>*/(includedBy.length);
				for (int i = 0; i < includedBy.length; i++) {
					IIndexInclude include = includedBy[i];
					IIndexFileLocation incLocation = include.getIncludedByLocation();
					if (CoreModel.isValidSourceUnitName(project.getProject(), incLocation.getURI().toString())) { // FIXME - is this ok?
						ITranslationUnit context = CoreModelUtil.findTranslationUnitForLocation(IndexLocationFactory.getAbsolutePath(incLocation), project);
						if (context != null) {
							fContextMap.put(location, context);
							return context;
						}
					}
					paths.add(incLocation);
				}
				for (Iterator/*<IIndexFileLocation>*/ iter = paths.iterator(); iter.hasNext();) {
					IIndexFileLocation nextLevel = (IIndexFileLocation) iter.next();
					ITranslationUnit context = findContext(index, nextLevel);
					if (context != null) {
						fContextMap.put(location, context);
						return context;
					}
				}
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	/**
	 * Conveninence method for subclasses, removes a translation unit from the index.
	 * @since 4.0
	 */
	protected void removeTU(IWritableIndex index, ITranslationUnit tu, int readlocks) throws CoreException, InterruptedException {
		index.acquireWriteLock(readlocks);
		try {
			IIndexFragmentFile file = (IIndexFragmentFile) index.getFile(IndexLocationFactory.getIFL(tu));
			if (file != null)
				index.clearFile(file);
		} finally {
			index.releaseWriteLock(readlocks);
		}
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
}
