/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.pdom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.index.IndexFileLocation;
import org.eclipse.cdt.internal.core.index.IWritableIndex.IncludeInformation;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMASTAdapter;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerASTVisitor;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerStatistics;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.osgi.util.NLS;

/**
 * Abstract class to write information from AST 
 * @since 4.0
 */
abstract public class PDOMWriter {
	public static int SKIP_ALL_REFERENCES= -1;
	public static int SKIP_TYPE_REFERENCES= 1;
	public static int SKIP_NO_REFERENCES= 0;
	
	protected boolean fShowActivity;
	protected boolean fShowProblems;
	protected IndexerStatistics fStatistics;
	
	private IndexerProgress fInfo= new IndexerProgress();
	private int fSkipReferences= SKIP_NO_REFERENCES;
	
	public PDOMWriter() {
		fStatistics= new IndexerStatistics();
	}
	
	public void setShowActivity(boolean val) {
		fShowActivity= val;
	}
	
	public void setShowProblems(boolean val) {
		fShowProblems= val;
	}
	
	/**
	 * Determines whether references are skipped or not. Provide one of 
	 * {@link #SKIP_ALL_REFERENCES}, {@link #SKIP_TYPE_REFERENCES} or {@link #SKIP_NO_REFERENCES}.
	 */
	public void setSkipReferences(int options) {
		fSkipReferences= options;
	}
	
	/**
	 * Called to check whether a translation unit still needs to be updated.
	 * @see #addSymbols(IASTTranslationUnit, IWritableIndex, int, IProgressMonitor)
	 * @since 4.0
	 */
	protected abstract boolean needToUpdate(IIndexFileLocation location, int confighash) throws CoreException;

	/**
	 * Called after a file was added to the index. 
	 * @return whether the file was actually requested by the indexer.
	 * @see #addSymbols(IASTTranslationUnit, IWritableIndex, int, IProgressMonitor)
	 * @since 4.0
	 */
	protected abstract boolean postAddToIndex(IIndexFileLocation location, IIndexFile file) throws CoreException;

	/**
	 * Called to resolve an absolute path to an index file location. 
	 * @since 4.0
	 */
	protected abstract IIndexFileLocation findLocation(String absolutePath);
		
	/**
	 * Fully equivalent to 
	 * <code>addSymbols(IASTTranslationUnit, IWritableIndex, int, true, int, null, IProgressMonitor)</code>.
	 * @since 4.0
	 */
	public void addSymbols(IASTTranslationUnit ast, 
			IWritableIndex index, int readlockCount,
			int configHash, IProgressMonitor pm) throws InterruptedException, CoreException {
		addSymbols(ast, index, readlockCount, true, configHash, null, pm);
	}

	/**
	 * Extracts symbols from the given ast and adds them to the index. It will
	 * make calls to 	  
	 * {@link #needToUpdate(IIndexFileLocation)},
	 * {@link #postAddToIndex(IIndexFileLocation, IIndexFile)},
	 * {@link #getLastModified(IIndexFileLocation)} and
	 * {@link #findLocation(String)} to obtain further information.
	 * 
	 * When flushIndex is set to <code>false</code>, you must make sure to flush the 
	 * index after your last write operation.
	 * @since 4.0
	 */
	public void addSymbols(IASTTranslationUnit ast, 
			IWritableIndex index, int readlockCount, boolean flushIndex,
			int configHash, ITodoTaskUpdater taskUpdater, IProgressMonitor pm) throws InterruptedException, CoreException {
		final Map symbolMap= new HashMap();
		ArrayList stati= new ArrayList();
		IIndexFileLocation[] orderedPaths= null;
		try {
			HashSet contextIncludes= new HashSet();
			orderedPaths= extractSymbols(ast, symbolMap, configHash, contextIncludes);
			for (int i=0; i<orderedPaths.length; i++) {
				if (pm.isCanceled()) {
					return;
				}
				IIndexFileLocation path= orderedPaths[i];
				ArrayList[] arrayLists = ((ArrayList[]) symbolMap.get(path));

				// resolve the names
				long start= System.currentTimeMillis();
				ArrayList names= arrayLists[2];
				boolean reported= false;
				for (Iterator j = names.iterator(); j.hasNext();) {
					final IASTName[] na= (IASTName[]) j.next();
					final IASTName name = na[0];
					try {
						final IBinding binding = name.resolveBinding();
						if (binding instanceof IProblemBinding)
							reportProblem((IProblemBinding) binding);
						else if (name.isReference()) {
							if (fSkipReferences == SKIP_TYPE_REFERENCES) {
								if (isTypeReferenceBinding(binding) && !isInheritanceSpec(name)) {
									na[0]= null;
									fStatistics.fReferenceCount--;
								}
							}
							fStatistics.fReferenceCount++;
						}
						else {
							fStatistics.fDeclarationCount++;
						}
					} catch (RuntimeException e) {
						if (!reported) {
							stati.add(CCorePlugin.createStatus(
								NLS.bind(Messages.PDOMWriter_errorResolvingName, name.toString(), path.getURI().getPath()), e));
						}
						reported= true;
						j.remove();
					} catch (PDOMNotImplementedError e) {
						if (!reported) {
							stati.add(CCorePlugin.createStatus(
									NLS.bind(Messages.PDOMWriter_errorResolvingName, name.toString(), path.getURI().getPath()), e));
						}
						reported= true;
						j.remove();
					}
				}
				fStatistics.fResolutionTime += System.currentTimeMillis()-start;
			}

			boolean isFirstRequest= true;
			boolean isFirstAddition= true;
			index.acquireWriteLock(readlockCount);
			long start= System.currentTimeMillis();
			try {
				for (int i=0; i<orderedPaths.length; i++) {
					if (pm.isCanceled()) 
						return;

					IIndexFileLocation path = orderedPaths[i];
					if (path != null) {
						if (fShowActivity) {
							System.out.println("Indexer: adding " + path.getURI());  //$NON-NLS-1$
						}
						IIndexFile file;
						try {
							file = addToIndex(index, path, symbolMap, configHash, contextIncludes);
						} catch (RuntimeException e) {
							stati.add(CCorePlugin.createStatus(
									NLS.bind(Messages.PDOMWriter_errorWhileParsing, path.getURI().getPath()), e));
							break;
						} catch (PDOMNotImplementedError e) {
							stati.add(CCorePlugin.createStatus(
									NLS.bind(Messages.PDOMWriter_errorWhileParsing, path.getURI().getPath()), e));
							break;
						}
						boolean wasRequested= postAddToIndex(path, file);

						synchronized(fInfo) {
							if (wasRequested) {
								if (isFirstRequest) 
									isFirstRequest= false;
								else 
									fInfo.fTotalSourcesEstimate--;
							}
							if (isFirstAddition) 
								isFirstAddition= false;
							else
								fInfo.fCompletedHeaders++;
						}
					}
				}
			} finally {
				index.releaseWriteLock(readlockCount, flushIndex);
			}
			fStatistics.fAddToIndexTime+= System.currentTimeMillis()-start;
			
			if (taskUpdater != null) {
				taskUpdater.updateTasks(ast.getComments(), orderedPaths);
			}
		}
		finally {
			synchronized(fInfo) {
				fInfo.fCompletedSources++;
			}
		}
		if (!stati.isEmpty()) {
			String path= null;
			if (orderedPaths != null && orderedPaths.length > 0) {
				path= orderedPaths[orderedPaths.length-1].getURI().getPath();
			}
			else {
				path= ast.getFilePath().toString();
			}
			String msg= NLS.bind(Messages.PDOMWriter_errorWhileParsing, path);
			if (stati.size() == 1) {
				IStatus status= (IStatus) stati.get(0);
				if (msg.equals(status.getMessage())) {
					throw new CoreException(status);
				}
			}
			throw new CoreException(new MultiStatus(CCorePlugin.PLUGIN_ID, 0, 
					(IStatus[]) stati.toArray(new IStatus[stati.size()]), msg, null));
		}
	}

	private IIndexFileLocation[] extractSymbols(IASTTranslationUnit ast, 
			final Map symbolMap, int confighash, Collection contextIncludes) throws CoreException {
		LinkedHashSet/*<IIndexFileLocation>*/ orderedIFLs= new LinkedHashSet/*<IIndexFileLocation>*/();
		ArrayList/*<IIndexFileLocation>*/ iflStack= new ArrayList/*<IIndexFileLocation>*/();
		HashMap firstIncludePerTarget= new HashMap();
		
		final IIndexFileLocation astLocation = findLocation(ast.getFilePath());
		IIndexFileLocation aboveStackIFL = astLocation;

		IASTPreprocessorIncludeStatement[] includes = ast.getIncludeDirectives();
		for (int i= 0; i < includes.length; i++) {
			final IASTPreprocessorIncludeStatement include = includes[i];
			
			// check if we have left a file.
			final IASTFileLocation tmpLoc = include.getFileLocation();
			final IIndexFileLocation nextIFL= tmpLoc != null ? findLocation(tmpLoc.getFileName()) : astLocation; // command-line includes
			while (!aboveStackIFL.equals(nextIFL)) {
				if (!iflStack.isEmpty()) { 
					if (needToUpdate(aboveStackIFL, confighash)) {
						prepareInMap(symbolMap, aboveStackIFL);
						orderedIFLs.add(aboveStackIFL);
					}
					aboveStackIFL= (IIndexFileLocation) iflStack.remove(iflStack.size()-1);
				}
				else {
					assert false; // logics in parser is broken, still do something useful
					iflStack.add(aboveStackIFL);
					aboveStackIFL= nextIFL;
					break;
				}
			}
			
			// save include in map
			if (needToUpdate(nextIFL, confighash)) {
				prepareInMap(symbolMap, nextIFL);
				addToMap(symbolMap, 0, nextIFL, include);
			}
			
			// prepare to go into next level
			if (include.isResolved()) {
				iflStack.add(nextIFL);
				aboveStackIFL= findLocation(include.getPath());
				if (include.isActive()) {
					if (!firstIncludePerTarget.containsKey(aboveStackIFL)) {
						firstIncludePerTarget.put(aboveStackIFL, include);
					}
				}
			}
			else if (include.isActive()) {
				reportProblem(include);
			}
		}
		iflStack.add(aboveStackIFL);
		while (!iflStack.isEmpty()) {
			aboveStackIFL= (IIndexFileLocation) iflStack.remove(iflStack.size()-1);
			if (needToUpdate(aboveStackIFL, confighash)) {
				prepareInMap(symbolMap, aboveStackIFL);
				orderedIFLs.add(aboveStackIFL);
			}
		}

		// extract context includes
		for (Iterator iterator = orderedIFLs.iterator(); iterator.hasNext();) {
			IndexFileLocation loc = (IndexFileLocation) iterator.next();
			Object contextInclude= firstIncludePerTarget.get(loc);
			if (contextInclude != null) {
				contextIncludes.add(contextInclude);
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
				if (fSkipReferences == SKIP_ALL_REFERENCES) {
					if (name.isReference()) {
						if (!isInheritanceSpec(name)) {
							return;
						}
					}
				}
					
				// assign a location to anonymous types.
				name= PDOMASTAdapter.getAdapterIfAnonymous(name);
				IASTFileLocation nameLoc = name.getFileLocation();
				if (nameLoc != null) {
					IIndexFileLocation location = findLocation(nameLoc.getFileName());
					addToMap(symbolMap, 2, location, new IASTName[]{name, caller});
				}
			}
		});
		return (IIndexFileLocation[]) orderedIFLs.toArray(new IIndexFileLocation[orderedIFLs.size()]);
	}
	
	protected boolean isInheritanceSpec(IASTName name) {
		IASTNode parentNode= name.getParent();
		if (parentNode instanceof ICPPASTBaseSpecifier) {
			return true;
		}
		else if (parentNode instanceof IASTDeclSpecifier) {
			IASTDeclSpecifier ds= (IASTDeclSpecifier) parentNode;
			return ds.getStorageClass() == IASTDeclSpecifier.sc_typedef;
		}
		return false;
	}

	private boolean isTypeReferenceBinding(IBinding binding) {
		if (binding instanceof ICompositeType ||
				binding instanceof IEnumeration ||
				binding instanceof ITypedef ||
				binding instanceof ICPPNamespace ||
				binding instanceof ICPPNamespaceAlias ||
				binding instanceof ICPPClassTemplate) {
			return true;
		}
		return false;
	}


	private void reportProblem(IASTPreprocessorIncludeStatement problem) {
		fStatistics.fUnresolvedIncludes++;
		if (fShowProblems) {
			String msg= "Indexer: unresolved include"; //$NON-NLS-1$
			IASTFileLocation loc= problem.getFileLocation();
			if (loc != null && loc.getFileName() != null) {
				msg += " at " + loc.getFileName() + ": " + loc.getStartingLineNumber();  //$NON-NLS-1$ //$NON-NLS-2$
			}
			System.out.println(msg);
		}
	}
	
	private void reportProblem(IProblemBinding problem) {
		fStatistics.fProblemBindingCount++;
		if (fShowProblems) {
			String msg= "Indexer: problem at "+ problem.getFileName() + ": " + problem.getLineNumber();  //$NON-NLS-1$//$NON-NLS-2$
			String pmsg= problem.getMessage();
			if (pmsg != null && pmsg.length() > 0) 
				msg+= "; " + problem.getMessage(); //$NON-NLS-1$
			System.out.println(msg);
		}
	}

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

	private IIndexFragmentFile addToIndex(IWritableIndex index, IIndexFileLocation location, Map symbolMap, int configHash, Set contextIncludes) throws CoreException {
		Set clearedContexts= Collections.EMPTY_SET;
		IIndexFragmentFile file= (IIndexFragmentFile) index.getFile(location);
		if (file != null) {
			clearedContexts= new HashSet();
			index.clearFile(file, clearedContexts);
		} else {
			file= index.addFile(location);
		}
		file.setTimestamp(getLastModified(location));
		file.setScannerConfigurationHashcode(configHash);
		ArrayList[] lists= (ArrayList[]) symbolMap.get(location);
		if (lists != null) {
			ArrayList list= lists[1];
			IASTPreprocessorMacroDefinition[] macros= (IASTPreprocessorMacroDefinition[]) list.toArray(new IASTPreprocessorMacroDefinition[list.size()]);
			list= lists[2];
			IASTName[][] names= (IASTName[][]) list.toArray(new IASTName[list.size()][]);

			list= lists[0];
			IncludeInformation[] includeInfos= new IncludeInformation[list.size()];
			for (int i=0; i<list.size(); i++) {
				final IASTPreprocessorIncludeStatement include = (IASTPreprocessorIncludeStatement) list.get(i);
				final IncludeInformation info= includeInfos[i]= new IncludeInformation();
				info.fStatement= include;
				if (include.isResolved()) {
					info.fLocation= findLocation(include.getPath());
					info.fIsContext= contextIncludes.contains(include) ||
						clearedContexts.contains(info.fLocation);
				}
			}
			index.setFileContent(file, includeInfos, macros, names);
		}
		return file;
	}
	
	/**
	 * Makes a copy of the current progress information and returns it.
	 * @since 4.0
	 */
	protected IndexerProgress getProgressInformation() {
		synchronized (fInfo) {
			return new IndexerProgress(fInfo);
		}
	}

	/**
	 * Updates current progress information with the provided delta.
	 * @since 4.0
	 */
	protected void updateInfo(int completedSources, int completedHeaders, int totalEstimate) {
		synchronized(fInfo) {
			fInfo.fCompletedHeaders+= completedHeaders;
			fInfo.fCompletedSources+= completedSources;
			fInfo.fTotalSourcesEstimate+= totalEstimate;
		}
	}
	
	/**
	 * Obtains the timestamp of an index file location.
	 * @param location the location for which the timestamp is obtained.
	 * @return the timestamp.
	 * @throws CoreException
	 * @since 4.0
	 */
	protected long getLastModified(IIndexFileLocation location) throws CoreException {
		return EFS.getStore(location.getURI()).fetchInfo().getLastModified();
	}
}
