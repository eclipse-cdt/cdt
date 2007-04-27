/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.model.AbstractLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.index.IndexFileLocation;
import org.eclipse.cdt.internal.core.pdom.IndexerProgress;
import org.eclipse.cdt.internal.core.pdom.PDOMWriter;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

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
public abstract class StandaloneIndexerTask extends PDOMWriter{
	private static final Object NO_CONTEXT = new Object();
	
	protected StandaloneIndexer fIndexer;
	protected Map/*<IIndexFileLocation, Object>*/ fContextMap = new HashMap/*<IIndexFileLocation, Object>*/();
	private List fFilesUpFront= new ArrayList();
	private String fDummyFileName;
	private URI fDummyFileURI;
	private int fUpdateFlags= StandaloneIndexer.UPDATE_ALL;

	protected StandaloneIndexerTask(StandaloneIndexer indexer) {
		fIndexer= indexer;
		setShowActivity(fIndexer.getShowActivity());
		setShowProblems(fIndexer.getShowProblems());
		setSkipReferences(fIndexer.getSkipReferences());
	}
	
	/**
	 * Return the indexer.
	 * @return
	 */
	final public StandaloneIndexer getIndexer() {
		return fIndexer;
	}
	
	/**
	 * Return indexer's progress information.
	 */
	final public IndexerProgress getProgressInformation() {
		return super.getProgressInformation();
	}
	
	public void setUpdateFlags(int flags) {
		fUpdateFlags= flags;
	}
	
	final public boolean updateAll() {
		return (fUpdateFlags & StandaloneIndexer.UPDATE_ALL) != 0;
	}
	
	final public boolean updateChangedTimestamps() {
		return (fUpdateFlags & StandaloneIndexer.UPDATE_CHECK_TIMESTAMPS) != 0;
	}
	
	/**
	 * Tells the parser which files to parse first
	 */
	final public void setParseUpFront() {
		String[] files = fIndexer.getFilesToParseUpFront();
		for (int i = 0; i < files.length; i++) {
			fFilesUpFront.add((String) files[i]);
		}
	}
	
	/**
	 * Figurues out whether all files (sources without config, headers not included)
	 * should be parsed.
	 * @since 4.0
	 */
	final protected boolean getIndexAllFiles() {
		return getIndexer().getIndexAllFiles();
	}
		
	private IASTTranslationUnit createAST(IIndexFileLocation location, IScannerInfo scannerInfo, int options, IProgressMonitor pm) throws IOException, CoreException {
		String path = location.getFullPath();
		if (path == null) {
			return null;
		}
		ILanguage language = fIndexer.getLanguageMapper().getLanguage(path);
		if (language == null)
			return null;

		CodeReader codeReader = new CodeReader(path);	
		if (codeReader == null) {
			return null;
		}

		return createAST((AbstractLanguage)language, codeReader, scannerInfo, options, pm);
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
	 * {@link #createAST(IIndexFileLocation, IProgressMonitor)}, 
	 * {@link #needToUpdate(IIndexFileLocation)}, 
	 * {@link #addSymbols(IASTTranslationUnit, IWritableIndex, int, IProgressMonitor)}
	 * {@link #postAddToIndex(IIndexFileLocation, IIndexFile)} and
	 * {@link #findLocation(String)}
	 * @since 4.0
	 */
	protected void parseTUs(IWritableIndex index, int readlockCount, Collection sources, Collection headers, IProgressMonitor monitor) throws IOException, CoreException, InterruptedException {
		int options= 0;
		if (fIndexer.getSkipReferences() == StandaloneIndexer.SKIP_ALL_REFERENCES) {
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
			String sourcePath = (String) iter.next();
			String path = new File(sourcePath).getCanonicalPath();	
			final IIndexFileLocation ifl = getIndexFileLocation(path);

			if (needToUpdate(ifl, 0)) {
				parseTU(ifl, options, index, readlockCount, monitor);
			}
		}

		// headers with context
		for (Iterator iter = headers.iterator(); iter.hasNext();) {
			if (monitor.isCanceled()) 
				return;
			String sourcePath = (String) iter.next();
			String path = new File(sourcePath).getCanonicalPath();	
			final IIndexFileLocation location = getIndexFileLocation(path);
			
			if (!needToUpdate(location, 0)) {
				iter.remove();
			} 
			else {
				IIndexFileLocation context= findContext(index, location);
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

				String sourcePath = (String) iter.next();
				String path = new File(sourcePath).getCanonicalPath();	
				final IIndexFileLocation ifl = getIndexFileLocation(path);

				if (!needToUpdate(ifl, 0)) {
					iter.remove();
				}
				else {
					parseTU(ifl, options, index, readlockCount, monitor);
				}
			}
		}
	}
	
	final protected boolean isOutdated(IIndexFileLocation ifl, IIndexFile indexFile) throws CoreException {
		if (indexFile == null) {
			return true;
		}
		File res = new File(ifl.getFullPath());
		if (res != null) {
			if (indexFile != null) {
				if (res.lastModified() == indexFile.getTimestamp()) {
					return false;
				}
			}
			return true;
		}
		return false;
	}


	private void parseTU(IIndexFileLocation location, int options, IWritableIndex index, int readlockCount, IProgressMonitor pm) throws IOException, CoreException, InterruptedException {
		String path = location.getFullPath();
				
		try {
			// skip if no scanner info
			IScannerInfo scanner= fIndexer.getScannerInfo();
			if (scanner == null) {
				updateInfo(0, 0, -1);
			}
			else {
				final int configHash = computeHashCode(scanner);
				if (needToUpdate(location, configHash)) {
					if (fShowActivity) {
						System.out.println("Indexer: parsing " + path); //$NON-NLS-1$
					}
					long start= System.currentTimeMillis();
					IASTTranslationUnit ast= createAST(location, scanner, options, pm);
					fStatistics.fParsingTime += System.currentTimeMillis()-start;
					if (ast != null) {
						addSymbols(ast, index, readlockCount, configHash, pm);
					}
				}
			}
		}
		catch (CoreException e) {
			e.printStackTrace();
		}
		catch (RuntimeException e) {
			e.printStackTrace();
		}
		catch (Error e) {
			e.printStackTrace();
		}
	}
	
	private void parseUpFront(String file, int options, IWritableIndex index, int readlockCount, IProgressMonitor pm) throws CoreException, InterruptedException {
		file= file.trim();
		if (file.length() == 0) {
			return;
		}
		try {
			if (fShowActivity) {
				System.out.println("Indexer: parsing " + file + " up front"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			long start= System.currentTimeMillis();

			IASTTranslationUnit ast= null;
			ILanguage l = fIndexer.getLanguageMapper().getLanguage(file);
			if (l instanceof AbstractLanguage) {
				AbstractLanguage lang= (AbstractLanguage) l;
				IScannerInfo scanInfo = fIndexer.getScannerInfo();
				String code= "#include \"" + file + "\"\n"; //$NON-NLS-1$ //$NON-NLS-2$
				if (fDummyFileName == null) {
					fDummyFileName= file + "___"; //$NON-NLS-1$
					fDummyFileURI= findLocation(fDummyFileName).getURI();
				}
				CodeReader codeReader= new CodeReader(fDummyFileName, code.toCharArray());
				ast= createAST(lang, codeReader, scanInfo, options, pm);
			}
			fStatistics.fParsingTime += System.currentTimeMillis()-start;
			if (ast != null) {
				addSymbols(ast, index, readlockCount, 0, pm);
				updateInfo(-1, +1, 0);
			}
		}
		catch (CoreException e) {
			e.printStackTrace(); 
		}
		catch (RuntimeException e) {
			e.printStackTrace();
		}
		catch (Error e) {
			e.printStackTrace(); 
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
	protected boolean needToUpdate(IIndexFileLocation fileLoc, int configHash) throws CoreException {
		return fDummyFileURI==null || !fDummyFileURI.equals(fileLoc.getURI());
	}

	private IIndexFileLocation findContext(IIndex index, IIndexFileLocation location) {
		Object cachedContext= fContextMap.get(location);
		if (cachedContext != null) {
			return cachedContext == NO_CONTEXT ? null : (IIndexFileLocation) cachedContext;
		}
		IIndexFileLocation context= null;
		fContextMap.put(location, NO_CONTEXT); // prevent recursion
		IIndexFile pdomFile;
		try {
			pdomFile = index.getFile(location);
			if (pdomFile != null) {
				IIndexInclude[] includedBy = index.findIncludedBy(pdomFile, IIndex.DEPTH_ZERO);
				ArrayList/*<IIndexFileLocation>*/ paths= new ArrayList/*<IIndexFileLocation>*/(includedBy.length);
				for (int i = 0; i < includedBy.length; i++) {
					IIndexInclude include = includedBy[i];
					IIndexFileLocation incLocation = include.getIncludedByLocation();
					if (isValidSourceUnitName(incLocation.getFullPath())) {
						context = incLocation;
						if (context != null) {
							fContextMap.put(location, context);
							return context;
						}
					}
					paths.add(incLocation);
				}
				for (Iterator/*<IIndexFileLocation>*/ iter = paths.iterator(); iter.hasNext();) {
					IIndexFileLocation nextLevel = (IIndexFileLocation) iter.next();
					context = findContext(index, nextLevel);
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
	protected void removeTU(IWritableIndex index, IIndexFileLocation ifl, int readlocks) throws CoreException, InterruptedException {
		index.acquireWriteLock(readlocks);
		try {
			IIndexFragmentFile file = (IIndexFragmentFile) index.getFile(ifl);
			if (file != null)
				index.clearFile(file, null);
		} finally {
			index.releaseWriteLock(readlocks);
		}
	}

	protected void traceEnd(long start) {
		if (fIndexer.getTraceStatistics()) {
			IndexerProgress info= getProgressInformation();
			String name= getClass().getName();
			name= name.substring(name.lastIndexOf('.')+1);

			System.out.println(name + " "  //$NON-NLS-1$
					+ " (" + info.fCompletedSources + " sources, "  //$NON-NLS-1$ //$NON-NLS-2$
					+ info.fCompletedHeaders + " headers)"); //$NON-NLS-1$
			
			boolean allFiles= getIndexAllFiles();
			boolean skipRefs= fIndexer.getSkipReferences() == StandaloneIndexer.SKIP_ALL_REFERENCES;
			boolean skipTypeRefs= skipRefs || fIndexer.getSkipReferences() == StandaloneIndexer.SKIP_TYPE_REFERENCES;
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
			int sum= fStatistics.fDeclarationCount+fStatistics.fReferenceCount+fStatistics.fProblemBindingCount;
			double problemPct= sum==0 ? 0.0 : (double) fStatistics.fProblemBindingCount / (double) sum;
			NumberFormat nf= NumberFormat.getPercentInstance();
			nf.setMaximumFractionDigits(2);
			nf.setMinimumFractionDigits(2);
			System.out.println(name + " Result: " //$NON-NLS-1$
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
				System.out.println(name + " Cache: " //$NON-NLS-1$
					+ hits + " hits, "  //$NON-NLS-1$
					+ misses + "(" + nf.format(missPct)+ ") misses."); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}
	
	public abstract void run(IProgressMonitor monitor) throws IOException;
	
	protected IIndexFileLocation getIndexFileLocation(String path) {
		String absolutePath = new File(path).getAbsolutePath();
		//Standalone indexing stores the absolute paths of files being indexed
		return new IndexFileLocation(URIUtil.toURI(absolutePath),absolutePath); 
	}
	
	protected boolean isValidSourceUnitName(String filename) {
		IPath path = new Path(filename);
		if (fIndexer.getValidSourceUnitNames() == null || fIndexer.getValidSourceUnitNames().size() == 0)
			return true;
		return fIndexer.getValidSourceUnitNames().contains(path.getFileExtension());
	}
	
	protected long getLastModified(IIndexFileLocation location) {
		return new File(location.getFullPath()).lastModified();
	}
	
	protected static int computeHashCode(IScannerInfo scannerInfo) {
		int result= 0;
		Map macros= scannerInfo.getDefinedSymbols();
		if (macros != null) {
			for (Iterator i = macros.entrySet().iterator(); i.hasNext();) {
				Map.Entry entry = (Map.Entry) i.next();
				String key = (String) entry.getKey();
				String value = (String) entry.getValue();
				result= addToHashcode(result, key);
				if (value != null && value.length() > 0) {
					result= addToHashcode(result, value);
				}
			}
		}
		String[] a= scannerInfo.getIncludePaths();
		if (a != null) {
			for (int i = 0; i < a.length; i++) {
				result= addToHashcode(result, a[i]);

			}
		}
		if (scannerInfo instanceof IExtendedScannerInfo) {
			IExtendedScannerInfo esi= (IExtendedScannerInfo) scannerInfo;
			a= esi.getIncludeFiles();
			if (a != null) {
				for (int i = 0; i < a.length; i++) {
					result= addToHashcode(result, a[i]);

				}
			}			
			a= esi.getLocalIncludePath();
			if (a != null) {
				for (int i = 0; i < a.length; i++) {
					result= addToHashcode(result, a[i]);

				}
			}		
			a= esi.getMacroFiles();
			if (a != null) {
				for (int i = 0; i < a.length; i++) {
					result= addToHashcode(result, a[i]);

				}
			}		
		}
		return result;
	}

	private static int addToHashcode(int result, String key) {
		return result*31 + key.hashCode();
	}
}
