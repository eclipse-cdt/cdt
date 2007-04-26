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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

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
import org.eclipse.cdt.internal.core.pdom.dom.PDOMASTAdapter;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerASTVisitor;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerStatistics;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Abstract class to write information from AST 
 * @since 4.0
 */
abstract public class PDOMWriter {
	private static final IASTPreprocessorIncludeStatement[] NO_INCLUDES = {};
	private static final IIndexFileLocation[] NO_LOCATIONS = {};
	
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
	 * Extracts symbols from the given ast and adds them to the index. It will
	 * make calls to 	  
	 * {@link #needToUpdate(IIndexFileLocation)},
	 * {@link #postAddToIndex(IIndexFileLocation, IIndexFile)},
	 * {@link #getLastModified(IIndexFileLocation)} and
	 * {@link #findLocation(String)} to obtain further information.
	 * @since 4.0
	 */
	public void addSymbols(IASTTranslationUnit ast, IWritableIndex index, int readlockCount, int configHash,
			IProgressMonitor pm) throws InterruptedException, CoreException {
		final Map symbolMap= new HashMap();
		try {
			IIndexFileLocation[] orderedPaths= extractSymbols(ast, symbolMap, configHash);
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
					final IASTName[] na= (IASTName[]) names.get(j);
					final IASTName name = na[0];
					final IBinding binding= name.resolveBinding();
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
						IIndexFile file= addToIndex(index, path, symbolMap, configHash);
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
				index.releaseWriteLock(readlockCount);
			}
			fStatistics.fAddToIndexTime+= System.currentTimeMillis()-start;
		}
		finally {
			synchronized(fInfo) {
				fInfo.fCompletedSources++;
			}
		}
	}

	private IIndexFileLocation[] extractSymbols(IASTTranslationUnit ast, final Map symbolMap, int confighash) throws CoreException {
		LinkedHashSet/*<IIndexFileLocation>*/ orderedIFLs= new LinkedHashSet/*<IIndexFileLocation>*/();
		ArrayList/*<IIndexFileLocation>*/ iflStack= new ArrayList/*<IIndexFileLocation>*/();


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

	private IIndexFragmentFile addToIndex(IWritableIndex index, IIndexFileLocation location, Map symbolMap, int configHash) throws CoreException {
		ArrayList[] lists= (ArrayList[]) symbolMap.get(location);
		IASTPreprocessorIncludeStatement[] includes= NO_INCLUDES;
		IASTPreprocessorMacroDefinition[] macros= null;
		IASTName[][] names= null;
		IIndexFileLocation[] includeLocations= NO_LOCATIONS;
		if (lists != null) {
			ArrayList list= lists[0];
			includes= (IASTPreprocessorIncludeStatement[]) list.toArray(new IASTPreprocessorIncludeStatement[list.size()]);
			list= lists[1];
			macros= (IASTPreprocessorMacroDefinition[]) list.toArray(new IASTPreprocessorMacroDefinition[list.size()]);
			list= lists[2];
			names= (IASTName[][]) list.toArray(new IASTName[list.size()][]);

			includeLocations = new IIndexFileLocation[includes.length];
			for(int i=0; i<includes.length; i++) {
				if (includes[i].isResolved()) {
					includeLocations[i] = findLocation(includes[i].getPath());
				}
			}
		}
		IIndexFragmentFile file= (IIndexFragmentFile) index.getFile(location);
		if (file == null) {
			file= index.addFile(location);
		}
		index.clearFile(file, includes, includeLocations);
		file.setTimestamp(getLastModified(location));
		file.setScannerConfigurationHashcode(configHash);
		if (lists != null) {
			index.setFileContent(file, macros, names);
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

	/**
	 * Obtains the scanner configuration hash code for an index file location. With
	 * that it can be determined at a later point if the scanner configuration has
	 * potentially been changed for a particular file.
	 * @param location the location for which the scanner configuration hash code is obtained.
	 * @return the hashcode or <code>0</code>, if unknown.
	 * @throws CoreException
	 * @since 4.0
	 */
	protected int getScannerConfigurationHashcode(IIndexFileLocation location) throws CoreException {
		return 0;
	}
}
