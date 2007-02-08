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

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICElementVisitor;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMASTAdapter;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;

public abstract class PDOMIndexerTask implements IPDOMIndexerTask {
	private static final class TranslationUnitCollector implements ICElementVisitor {
		private final Collection fHeaders;
		private final boolean fAllFiles;
		private final Collection fSources;

		private TranslationUnitCollector(Collection sources, Collection headers, boolean allFiles) {
			fHeaders = headers;
			fAllFiles = allFiles;
			fSources = sources;
		}

		public boolean visit(ICElement element) throws CoreException {
			switch (element.getElementType()) {
			case ICElement.C_UNIT:
				ITranslationUnit tu = (ITranslationUnit)element;
				if (tu.isSourceUnit()) {
					if (fAllFiles || !CoreModel.isScannerInformationEmpty(tu.getResource())) {
						fSources.add(tu);
					}
				}
				else if (fHeaders != null && tu.isHeaderUnit()) {
					fHeaders.add(tu);
				}
				return false;
			case ICElement.C_CCONTAINER:
			case ICElement.C_PROJECT:
				return true;
			}
			return false;
		}
	}

	private static final Object NO_CONTEXT = new Object();
	protected static final int MAX_ERRORS = 500;

	protected volatile int fTotalSourcesEstimate= 0;
	protected volatile int fCompletedSources= 0;
	protected volatile int fCompletedHeaders= 0;
	protected Map/*<IIndexFileLocation, Object>*/ fContextMap = new HashMap/*<IIndexFileLocation, Object>*/();
	protected volatile String fMessage;

	private boolean fShowActivity;
	private boolean fShowStatistics;
	private boolean fShowProblems;
	private int fResolutionTime;
	private int fParsingTime;
	private int fAddToIndexTime;
	private int fErrorCount;
	private int fReferenceCount= 0;
	private int fDeclarationCount= 0;
	private int fProblemBindingCount= 0;

	protected PDOMIndexerTask() {
		fShowActivity= checkDebugOption("indexer/activity", "true");  //$NON-NLS-1$//$NON-NLS-2$
		fShowStatistics= checkDebugOption("indexer/statistics", "true");  //$NON-NLS-1$//$NON-NLS-2$
		fShowProblems= checkDebugOption("indexer/problems", "true");  //$NON-NLS-1$//$NON-NLS-2$
	}

	private boolean checkDebugOption(String option, String value) {
		String trace = Platform.getDebugOption(CCorePlugin.PLUGIN_ID + "/debug/" + option);  //$NON-NLS-1$
		return (trace != null && trace.equalsIgnoreCase(value));
	}

	protected void processDelta(ICElementDelta delta, Collection added, Collection changed, Collection removed) throws CoreException {
		boolean allFiles= getIndexAllFiles();
		int flags = delta.getFlags();

		if ((flags & ICElementDelta.F_CHILDREN) != 0) {
			ICElementDelta[] children = delta.getAffectedChildren();
			for (int i = 0; i < children.length; ++i) {
				processDelta(children[i], added, changed, removed);
			}
		}

		ICElement element = delta.getElement();
		switch (element.getElementType()) {
		case ICElement.C_UNIT:
			ITranslationUnit tu = (ITranslationUnit)element;
			switch (delta.getKind()) {
			case ICElementDelta.CHANGED:
				if ((flags & ICElementDelta.F_CONTENT) != 0 &&
						(allFiles || !CoreModel.isScannerInformationEmpty(tu.getResource()))) {
					changed.add(tu);
				}
				break;
			case ICElementDelta.ADDED:
				if (!tu.isWorkingCopy() &&
						(allFiles || !CoreModel.isScannerInformationEmpty(tu.getResource()))) {
					added.add(tu);
				}
				break;
			case ICElementDelta.REMOVED:
				if (!tu.isWorkingCopy())
					removed.add(tu);
				break;
			}
			break;
		case ICElement.C_CCONTAINER:
			ICContainer folder= (ICContainer) element;
			if (delta.getKind() == ICElementDelta.ADDED) {
				collectSources(folder, added, added, allFiles);
			}
			break;
		}

	}

	private void collectSources(ICContainer container, final Collection sources, final Collection headers, final boolean allFiles) throws CoreException {
		container.accept(new TranslationUnitCollector(sources, headers, allFiles));
	}

	protected void collectSources(ICProject project, final Collection sources, final Collection headers, final boolean allFiles) throws CoreException {
		fMessage= MessageFormat.format(Messages.PDOMIndexerTask_collectingFilesTask, new Object[]{project.getElementName()});
		project.accept(new TranslationUnitCollector(sources, headers, allFiles));
	}

	protected void removeTU(IWritableIndex index, ITranslationUnit tu, int readlocks) throws CoreException, InterruptedException {
		index.acquireWriteLock(readlocks);
		try {
			IIndexFragmentFile file = (IIndexFragmentFile) index.getFile(getIndexFileLocation(tu));
			if (file != null)
				index.clearFile(file);
		} finally {
			index.releaseWriteLock(readlocks);
		}
	}

	protected void parseTUs(Collection sources, Collection headers, IProgressMonitor monitor) throws CoreException, InterruptedException {
		// sources first
		for (Iterator iter = sources.iterator(); iter.hasNext();) {
			if (monitor.isCanceled()) 
				return;
			ITranslationUnit tu = (ITranslationUnit) iter.next();
			if (needToUpdate(getIndexFileLocation(tu))) {
				parseTU(tu, monitor);
			}
		}

		// headers with context
		for (Iterator iter = headers.iterator(); iter.hasNext();) {
			if (monitor.isCanceled()) 
				return;
			ITranslationUnit tu = (ITranslationUnit) iter.next();
			IIndexFileLocation location = getIndexFileLocation(tu);
			if (!needToUpdate(location)) {
				iter.remove();
			} else {
				ITranslationUnit context= findContext(getIndex(), location);
				if (context != null) {
					parseTU(context, monitor);
				}
			}
		}

		// headers without context
		if (getIndexAllFiles()) {
			for (Iterator iter = headers.iterator(); iter.hasNext();) {
				if (monitor.isCanceled()) 
					return;
				ITranslationUnit tu = (ITranslationUnit) iter.next();
				if (!needToUpdate(getIndexFileLocation(tu))) {
					iter.remove();
				}
				else {
					parseTU(tu, monitor);
				}
			}
		}
	}

	protected void parseTU(ITranslationUnit tu, IProgressMonitor pm) throws CoreException, InterruptedException {
		IPath path= tu.getPath();
		try {
			if (fShowActivity) {
				System.out.println("Indexer: parsing " + path.toOSString()); //$NON-NLS-1$
			}
			fMessage= MessageFormat.format(Messages.PDOMIndexerTask_parsingFileTask,
					new Object[]{path.lastSegment(), path.removeLastSegments(1).toString()});
			long start= System.currentTimeMillis();
			doParseTU(tu, pm);
			fParsingTime += System.currentTimeMillis()-start;
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

	private void swallowError(IPath file, Throwable e) throws CoreException {
		IStatus status= CCorePlugin.createStatus(
				MessageFormat.format(Messages.PDOMIndexerTask_errorWhileParsing, new Object[]{file}), e);
		CCorePlugin.log(status);
		if (++fErrorCount > MAX_ERRORS) {
			throw new CoreException(CCorePlugin.createStatus(
					MessageFormat.format(Messages.PDOMIndexerTask_tooManyIndexProblems, new Object[]{getIndexer().getProject().getElementName()})));
		}
	}

	abstract protected void doParseTU(ITranslationUnit tu, IProgressMonitor pm) throws CoreException, InterruptedException;

	protected void clearIndex(IWritableIndex index) throws InterruptedException, CoreException {
		// reset error count
		fErrorCount= 0;
		// First clear the pdom
		index.acquireWriteLock(0);
		try {
			index.clear();
		}
		finally {
			index.releaseWriteLock(0);
		}
	}

	protected boolean getIndexAllFiles() {
		return getIndexer().getIndexAllFiles();
	}

	protected ITranslationUnit findContext(IIndex index, IIndexFileLocation location) {
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

	public String getMonitorMessageDetail() {
		return fMessage;
	}


	final public int estimateRemainingSources() {
		return fTotalSourcesEstimate-fCompletedSources;
	}

	public int getCompletedHeadersCount() {
		return fCompletedHeaders;
	}

	public int getCompletedSourcesCount() {
		return fCompletedSources;
	}


	protected void addSymbols(IASTTranslationUnit ast, IProgressMonitor pm) throws InterruptedException, CoreException {
		final Map symbolMap= new HashMap();
		try {
			IIndexFileLocation[] orderedPaths= extractSymbols(ast, symbolMap);
			for (int i=0; i<orderedPaths.length; i++) {
				if (pm.isCanceled()) {
					return;
				}
				IIndexFileLocation path= orderedPaths[i];
				ArrayList[] arrayLists = ((ArrayList[]) symbolMap.get(path));

				// resolve the names
				long start= System.currentTimeMillis();
				ArrayList names= arrayLists[2];
				for (int j=0; j<names.size(); j++) {
					final IASTName name = ((IASTName[]) names.get(j))[0];
					final IBinding binding= name.resolveBinding();
					if (fShowStatistics) {
						if (binding instanceof IProblemBinding)
							reportProblem((IProblemBinding) binding);
						else if (name.isReference()) 
							fReferenceCount++;
						else 
							fDeclarationCount++;
					}
				}
				fResolutionTime += System.currentTimeMillis()-start;
			}

			boolean isFirstRequest= true;
			boolean isFirstAddition= true;
			IWritableIndex index= getIndex();
			index.acquireWriteLock(getReadlockCount());
			long start= System.currentTimeMillis();
			try {
				for (int i=0; i<orderedPaths.length; i++) {
					if (pm.isCanceled()) 
						return;

					IIndexFileLocation path = orderedPaths[i];
					if (path != null) {
						if (fShowActivity) {
							System.out.println("Indexer: adding " + path.getURI()); //$NON-NLS-1$
						}
						IIndexFile file= addToIndex(index, path, symbolMap);
						if (postAddToIndex(path, file)) {
							if (isFirstRequest) 
								isFirstRequest= false;
							else 
								fTotalSourcesEstimate--;
						}
						if (isFirstAddition) 
							isFirstAddition= false;
						else
							fCompletedHeaders++;
					}
				}
			} finally {
				index.releaseWriteLock(getReadlockCount());
			}
			fAddToIndexTime+= System.currentTimeMillis()-start;
		}
		finally {
			fCompletedSources++;
		}
	}

	private void reportProblem(IProblemBinding problem) {
		fProblemBindingCount++;
		if (fShowProblems) {
			String msg= "Indexer problem at "+ problem.getFileName() + ": " + problem.getLineNumber();  //$NON-NLS-1$//$NON-NLS-2$
			String pmsg= problem.getMessage();
			if (pmsg != null && pmsg.length() > 0) 
				msg+= "; " + problem.getMessage(); //$NON-NLS-1$
			System.out.println(msg);
		}
	}

	private IIndexFileLocation[] extractSymbols(IASTTranslationUnit ast, final Map symbolMap) throws CoreException {
		LinkedHashSet/*<IIndexFileLocation>*/ orderedIncludes= new LinkedHashSet/*<IIndexFileLocation>*/();
		ArrayList/*<IIndexFileLocation>*/ stack= new ArrayList/*<IIndexFileLocation>*/();


		final IIndexFileLocation astLocation = findLocation(ast.getFilePath());
		IIndexFileLocation currentPath = astLocation;

		IASTPreprocessorIncludeStatement[] includes = ast.getIncludeDirectives();
		for (int i= 0; i < includes.length; i++) {
			IASTPreprocessorIncludeStatement include = includes[i];
			IASTFileLocation sourceLoc = include.getFileLocation();
			IIndexFileLocation newPath= sourceLoc != null ? findLocation(sourceLoc.getFileName()) : astLocation; // command-line includes
			while (!stack.isEmpty() && !currentPath.equals(newPath)) {
				if (needToUpdate(currentPath)) {
					prepareInMap(symbolMap, currentPath);
					orderedIncludes.add(currentPath);
				}
				currentPath= (IIndexFileLocation) stack.remove(stack.size()-1);
			}
			if (needToUpdate(newPath)) {
				prepareInMap(symbolMap, newPath);
				addToMap(symbolMap, 0, newPath, include);
			}
			stack.add(currentPath);
			currentPath= findLocation(include.getPath());
		}
		stack.add(currentPath);
		while (!stack.isEmpty()) {
			currentPath= (IIndexFileLocation) stack.remove(stack.size()-1);
			if (needToUpdate(currentPath)) {
				prepareInMap(symbolMap, currentPath);
				orderedIncludes.add(currentPath);
			}
		}

		// macros
		IASTPreprocessorMacroDefinition[] macros = ast.getMacroDefinitions();
		for (int i2 = 0; i2 < macros.length; ++i2) {
			IASTPreprocessorMacroDefinition macro = macros[i2];
			IASTFileLocation sourceLoc = macro.getFileLocation();
			if (sourceLoc != null) { // skip built-ins and command line macros
				IIndexFileLocation path2 = findLocation(sourceLoc.getFileName());
				addToMap(symbolMap, 1, path2, macro);
			}
		}

		// names
		ast.accept(new IndexerASTVisitor() {
			public void visit(IASTName name, IASTName caller) {
				// assign a location to anonymous types.
				name= PDOMASTAdapter.getAdapterIfAnonymous(name);
				IASTFileLocation nameLoc = name.getFileLocation();
				
				if (nameLoc != null) {
					IIndexFileLocation location = findLocation(nameLoc.getFileName());
					addToMap(symbolMap, 2, location, new IASTName[]{name, caller});
				}
			}
		});
		return (IIndexFileLocation[]) orderedIncludes.toArray(new IIndexFileLocation[orderedIncludes.size()]);
	}

	protected abstract IWritableIndex getIndex();
	protected abstract int getReadlockCount();
	protected abstract boolean needToUpdate(IIndexFileLocation location) throws CoreException;
	protected abstract boolean postAddToIndex(IIndexFileLocation location, IIndexFile file) throws CoreException;

	private void addToMap(Map map, int idx, IIndexFileLocation location, Object thing) {
		List[] lists= (List[]) map.get(location);
		if (lists != null) 
			lists[idx].add(thing);
	}		

	private boolean prepareInMap(Map map, IIndexFileLocation location) {
		if (map.get(location) == null) {
			Object lists= new ArrayList[]{new ArrayList(), new ArrayList(), new ArrayList()};
			map.put(location, lists);
		}
		return false;
	}

	private IIndexFragmentFile addToIndex(IWritableIndex index, IIndexFileLocation location, Map symbolMap) throws CoreException {
		IIndexFragmentFile file= (IIndexFragmentFile) index.getFile(location);
		if (file != null) {
			index.clearFile(file);
		} else {
			file= index.addFile(location);
		}
		file.setTimestamp(EFS.getStore(location.getURI()).fetchInfo().getLastModified());
		ArrayList[] lists= (ArrayList[]) symbolMap.get(location);
		if (lists != null) {
			ArrayList list= lists[0];
			IASTPreprocessorIncludeStatement[] includes= (IASTPreprocessorIncludeStatement[]) list.toArray(new IASTPreprocessorIncludeStatement[list.size()]);
			list= lists[1];
			IASTPreprocessorMacroDefinition[] macros= (IASTPreprocessorMacroDefinition[]) list.toArray(new IASTPreprocessorMacroDefinition[list.size()]);
			list= lists[2];
			IASTName[][] names= (IASTName[][]) list.toArray(new IASTName[list.size()][]);

			IIndexFileLocation[] includeLocations = new IIndexFileLocation[includes.length];
			for(int i=0; i<includes.length; i++) {
				includeLocations[i] = findLocation(includes[i].getPath());
			}
			index.setFileContent(file, includes, includeLocations, macros, names);
		}
		return file;
	}

	protected void traceEnd(long start) {
		if (fShowStatistics) {
			String name= getClass().getName();
			name= name.substring(name.lastIndexOf('.')+1);

			System.out.println(name + " " + getIndexer().getProject().getElementName()  //$NON-NLS-1$
					+ " (" + fCompletedSources + " sources, "  //$NON-NLS-1$ //$NON-NLS-2$
					+ fCompletedHeaders + " headers)"); //$NON-NLS-1$

			System.out.println(name + " Timings: "  //$NON-NLS-1$
					+ (System.currentTimeMillis() - start) + " total, " //$NON-NLS-1$
					+ (fParsingTime-fResolutionTime-fAddToIndexTime) + " parser, " //$NON-NLS-1$
					+ fResolutionTime + " resolution, " //$NON-NLS-1$
					+ fAddToIndexTime + " index update."); //$NON-NLS-1$
			int sum= fDeclarationCount+fReferenceCount+fProblemBindingCount;
			double problemPct= sum==0 ? 0.0 : (double) fProblemBindingCount / (double) sum;
			NumberFormat nf= NumberFormat.getPercentInstance();
			nf.setMaximumFractionDigits(2);
			nf.setMinimumFractionDigits(2);
			System.out.println(name + " Result: " //$NON-NLS-1$
					+ fDeclarationCount + " declarations, " //$NON-NLS-1$
					+ fReferenceCount + " references, " //$NON-NLS-1$
					+ fErrorCount + " errors, " //$NON-NLS-1$
					+ fProblemBindingCount + "(" + nf.format(problemPct) + ") problems.");  //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	protected IIndexFileLocation getIndexFileLocation(ITranslationUnit tu) {
		if(tu.getResource()!=null)
			return IndexLocationFactory.getWorkspaceIFL((IFile)tu.getResource());
		else
			return IndexLocationFactory.getExternalIFL(tu.getLocation());
	}

	protected abstract IIndexFileLocation findLocation(String absolutePath);
}
