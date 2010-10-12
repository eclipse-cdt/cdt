/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    IBM Corporation
 *    Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorUndefStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.index.IWritableIndex.IncludeInformation;
import org.eclipse.cdt.internal.core.parser.scanner.LocationMap;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMASTAdapter;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerASTVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

/**
 * Abstract class to write information from AST
 * @since 4.0
 */
abstract public class PDOMWriter {
	public static int SKIP_ALL_REFERENCES= -1;
	public static int SKIP_TYPE_REFERENCES= 1;
	public static int SKIP_MACRO_REFERENCES= 2;
	public static int SKIP_IMPLICIT_REFERENCES= 4;
	public static int SKIP_NO_REFERENCES= 0;

	private static class Symbols {
		ArrayList<IASTName[]> fNames= new ArrayList<IASTName[]>();
		ArrayList<IASTPreprocessorStatement> fMacros= new ArrayList<IASTPreprocessorStatement>();
		ArrayList<IASTPreprocessorIncludeStatement> fIncludes= new ArrayList<IASTPreprocessorIncludeStatement>();
	}
	private boolean fShowProblems;
	protected boolean fShowInclusionProblems;
	private boolean fShowScannerProblems;
	private boolean fShowSyntaxProblems;
	protected boolean fShowActivity;
	protected final IndexerStatistics fStatistics;
	protected final IndexerInputAdapter fResolver;

	private IndexerProgress fInfo= new IndexerProgress();
	private int fSkipReferences= SKIP_NO_REFERENCES;

	public PDOMWriter(IndexerInputAdapter resolver) {
		fStatistics= new IndexerStatistics();
		fResolver= resolver;
	}

	protected IndexerInputAdapter getInputAdapter() {
		return fResolver;
	}

	public void setShowActivity(boolean val) {
		fShowActivity= val;
	}

	public void setShowInclusionProblems(boolean val) {
		fShowInclusionProblems= val;
	}

	public void setShowScannerProblems(boolean val) {
		fShowScannerProblems= val;
	}

	public void setShowSyntaxProblems(boolean val) {
		fShowSyntaxProblems= val;
	}

	public void setShowProblems(boolean val) {
		fShowProblems= val;
	}

	/**
	 * Determines whether references are skipped or not. Provide one of
	 * {@link #SKIP_ALL_REFERENCES}, {@link #SKIP_NO_REFERENCES} or a combination of
	 * {@link #SKIP_IMPLICIT_REFERENCES}, {@link #SKIP_TYPE_REFERENCES} and {@link #SKIP_MACRO_REFERENCES}.
	 */
	public void setSkipReferences(int options) {
		fSkipReferences= options;
	}

	public int getSkipReferences() {
		return fSkipReferences;
	}

	/**
	 * Extracts symbols from the given AST and adds them to the index.
	 * 
	 * When flushIndex is set to <code>false</code>, you must make sure to flush 
	 * the index after your last write operation.
	 * @since 4.0
	 */
	public void addSymbols(IASTTranslationUnit ast, IIndexFileLocation[] ifls, IWritableIndex index,
			int readlockCount, boolean flushIndex, long fileContentsHash, int configHash,
			ITodoTaskUpdater taskUpdater, IProgressMonitor pm) throws InterruptedException, CoreException {
		if (fShowProblems) {
			fShowInclusionProblems= true;
			fShowScannerProblems= true;
			fShowSyntaxProblems= true;
		}
		final Map<IIndexFileLocation, Symbols> symbolMap= new HashMap<IIndexFileLocation, Symbols>();
		for (IIndexFileLocation ifl : ifls) {
			prepareInMap(symbolMap, ifl);
		}
		ArrayList<IStatus> stati= new ArrayList<IStatus>();

		HashSet<IASTPreprocessorIncludeStatement> contextIncludes= new HashSet<IASTPreprocessorIncludeStatement>();
		extractSymbols(ast, symbolMap, contextIncludes);

		// name resolution
		resolveNames(symbolMap, ifls, stati, pm);

		// index update
		storeSymbolsInIndex(symbolMap, ifls, ast.getLinkage().getLinkageID(), fileContentsHash,
				configHash, contextIncludes, index, readlockCount, flushIndex, stati, pm);

		if (taskUpdater != null) {
			taskUpdater.updateTasks(ast.getComments(), ifls);
		}
		if (!stati.isEmpty()) {
			String path= null;
			if (ifls.length > 0) {
				path= ifls[ifls.length - 1].getURI().getPath();
			} else {
				path= ast.getFilePath().toString();
			}
			String msg= NLS.bind(Messages.PDOMWriter_errorWhileParsing, path);
			if (stati.size() == 1) {
				IStatus status= stati.get(0);
				if (msg.equals(status.getMessage())) {
					throw new CoreException(status);
				}
				throw new CoreException(new Status(status.getSeverity(), status.getPlugin(), status.getCode(),
						msg + ':' + status.getMessage(), status.getException()));
			}
			throw new CoreException(new MultiStatus(CCorePlugin.PLUGIN_ID, 0,
					stati.toArray(new IStatus[stati.size()]), msg, null));
		}
	}

	private void storeSymbolsInIndex(final Map<IIndexFileLocation, Symbols> symbolMap, IIndexFileLocation[] ifls,
			int linkageID, long fileContentsHash, int configHash,
			HashSet<IASTPreprocessorIncludeStatement> contextIncludes, IWritableIndex index, int readlockCount,
			boolean flushIndex, ArrayList<IStatus> stati, IProgressMonitor pm)
			throws InterruptedException, CoreException {
		for (int i= 0; i < ifls.length; i++) {
			if (pm.isCanceled())
				return;

			final IIndexFileLocation ifl= ifls[i];
			if (ifl != null) {
				if (fShowActivity) {
					trace("Indexer: adding " + ifl.getURI());  //$NON-NLS-1$
				}
				Throwable th= null;
				YieldableIndexLock lock = new YieldableIndexLock(index, readlockCount, flushIndex);
				lock.acquire();
				try {
					storeFileInIndex(index, ifl, symbolMap, linkageID, fileContentsHash, configHash,
							contextIncludes, lock);
				} catch (RuntimeException e) {
					th= e;
				} catch (StackOverflowError e) {
					th= e;
				} catch (AssertionError e) {
					th= e;
				} finally {
					// When the caller holds a read-lock, the result cache of the index is never cleared.
					// ==> Before releasing the lock for the last time in this ast, we clear the result cache.
					if (readlockCount > 0  && i == ifls.length-1) {
						index.clearResultCache();
					}
					lock.release();
				}
				if (th != null) {
					stati.add(createStatus(NLS.bind(Messages.PDOMWriter_errorWhileParsing,
							ifl.getURI().getPath()), th));
				}
				if (i < ifls.length - 1) {
					updateFileCount(0, 0, 1); // update header count
				}
				fStatistics.fAddToIndexTime += lock.getCumulativeLockTime();
			}
		}
	}

	private void resolveNames(final Map<IIndexFileLocation, Symbols> symbolMap, IIndexFileLocation[] ifls,
			ArrayList<IStatus> stati, IProgressMonitor pm) {
		long start= System.currentTimeMillis();
		for (IIndexFileLocation path : ifls) {
			if (pm.isCanceled()) {
				return;
			}
			Symbols symbols= symbolMap.get(path);

			final ArrayList<IASTName[]> names= symbols.fNames;
			boolean reported= false;
			for (Iterator<IASTName[]> j = names.iterator(); j.hasNext();) {
				final IASTName[] na= j.next();
				final IASTName name = na[0];
				if (name != null) { // should not be null, just be defensive.
					Throwable th= null;
					try {
						final IBinding binding = name.resolveBinding();
						if (name.getPropertyInParent() == ICPPASTTemplateId.TEMPLATE_NAME &&
								((IASTName) name.getParent()).getBinding() == binding) {
								na[0]= null;
						} else if (binding instanceof IProblemBinding) {
							fStatistics.fProblemBindingCount++;
							if (fShowProblems) {
								reportProblem((IProblemBinding) binding);
							}
						} else if (name.isReference()) {
							if (binding instanceof ICPPTemplateParameter ||
									binding instanceof ICPPUnknownBinding ||
									((fSkipReferences & SKIP_TYPE_REFERENCES) != 0 &&
											isTypeReferenceBinding(binding))) {
								if (!isRequiredReference(name)) {
									na[0]= null;
								} else {
									fStatistics.fReferenceCount++;
								}
							} else {
								fStatistics.fReferenceCount++;
							}
						} else {
							fStatistics.fDeclarationCount++;
						}
					} catch (RuntimeException e) {
						th= e;
					} catch (StackOverflowError e) {
						th= e;
					}
					if (th != null) {
						if (!reported) {
							stati.add(CCorePlugin.createStatus(NLS.bind(Messages.PDOMWriter_errorResolvingName,
									name.toString(), path.getURI().getPath()), th));
						}
						reported= true;
						j.remove();
					}
				}
			}
		}
		fStatistics.fResolutionTime += System.currentTimeMillis()-start;
	}

	private void extractSymbols(IASTTranslationUnit ast, final Map<IIndexFileLocation, Symbols> symbolMap,
			Collection<IASTPreprocessorIncludeStatement> contextIncludes) throws CoreException {
		final HashSet<IIndexFileLocation> contextIFLs= new HashSet<IIndexFileLocation>();
		final IIndexFileLocation astIFL = fResolver.resolveASTPath(ast.getFilePath());

		int unresolvedIncludes= 0;
		IASTPreprocessorStatement[] stmts = ast.getAllPreprocessorStatements();
		for (final IASTPreprocessorStatement stmt : stmts) {
			// includes
			if (stmt instanceof IASTPreprocessorIncludeStatement) {
				IASTPreprocessorIncludeStatement include= (IASTPreprocessorIncludeStatement) stmt;

				final IASTFileLocation astLoc= include.getFileLocation();
				final IIndexFileLocation sourceIFL= astLoc != null ? fResolver.resolveASTPath(astLoc.getFileName()) : astIFL; // command-line includes
				final boolean updateSource= symbolMap.containsKey(sourceIFL);
				if (updateSource) {
					addToMap(symbolMap, sourceIFL, include);
				}
				if (include.isActive()) {
					if (!include.isResolved()) {
						unresolvedIncludes++;
					} else if (updateSource) {
						// the include was parsed, check if we want to update the included file in the index
						final IIndexFileLocation targetIFL= fResolver.resolveASTPath(include.getPath());
						if (symbolMap.containsKey(targetIFL) && contextIFLs.add(targetIFL)) {
							contextIncludes.add(include);
						}
					}
				}
			} else if (stmt.isActive() && (stmt instanceof IASTPreprocessorUndefStatement || stmt instanceof IASTPreprocessorMacroDefinition)) {
				IASTFileLocation sourceLoc = stmt.getFileLocation();
				if (sourceLoc != null) { // skip built-ins and command line macros
					IIndexFileLocation path2 = fResolver.resolveASTPath(sourceLoc.getFileName());
					addToMap(symbolMap, path2, stmt);
				}
			}
		}

		// names
		final IndexerASTVisitor visitor = new IndexerASTVisitor((fSkipReferences & SKIP_IMPLICIT_REFERENCES) == 0) {
			@Override
			public void visit(IASTName name, IASTName caller) {
				if (fSkipReferences == SKIP_ALL_REFERENCES) {
					if (name.isReference()) {
						if (!isRequiredReference(name)) {
							return;
						}
					}
				}

				// assign a location to anonymous types.
				name= PDOMASTAdapter.getAdapterIfAnonymous(name);
				if (name != null) {
					IASTFileLocation nameLoc = name.getFileLocation();
					if (nameLoc != null) {
						IIndexFileLocation location = fResolver.resolveASTPath(nameLoc.getFileName());
						addToMap(symbolMap, location, new IASTName[]{name, caller});
					}
				}
			}
		};
		ast.accept(visitor);

		if ((fSkipReferences & SKIP_MACRO_REFERENCES) == 0) {
			LocationMap lm= (LocationMap) ast.getAdapter(LocationMap.class);
			if (lm != null) {
				IASTName[] refs= lm.getMacroReferences();
				for (IASTName name : refs) {
					IASTFileLocation nameLoc = name.getFileLocation();
					if (nameLoc != null) {
						IIndexFileLocation location = fResolver.resolveASTPath(nameLoc.getFileName());
						addToMap(symbolMap, location, new IASTName[]{name, null});
					}
				}
			}
		}

		fStatistics.fUnresolvedIncludesCount += unresolvedIncludes;
		fStatistics.fPreprocessorProblemCount += ast.getPreprocessorProblemsCount() - unresolvedIncludes;
		if (fShowScannerProblems || fShowInclusionProblems) {
			final boolean reportAll= fShowScannerProblems && fShowInclusionProblems;
			IASTProblem[] scannerProblems= ast.getPreprocessorProblems();
			for (IASTProblem problem : scannerProblems) {
				if (reportAll || (problem.getID() == IProblem.PREPROCESSOR_INCLUSION_NOT_FOUND) == fShowInclusionProblems) {
					reportProblem(problem);
				}
			}
		}

		final List<IASTProblem> problems= visitor.getProblems();
		fStatistics.fSyntaxProblemsCount += problems.size();
		if (fShowSyntaxProblems) {
			for (IASTProblem problem : problems) {
				reportProblem(problem);
			}
		}
	}

	protected final boolean isRequiredReference(IASTName name) {
		IASTNode parentNode= name.getParent();
		if (parentNode instanceof ICPPASTQualifiedName) {
			if (name != ((ICPPASTQualifiedName) parentNode).getLastName())
				return false;
			parentNode= parentNode.getParent();
		}
		if (parentNode instanceof ICPPASTBaseSpecifier) {
			return true;
		} else if (parentNode instanceof IASTDeclSpecifier) {
			IASTDeclSpecifier ds= (IASTDeclSpecifier) parentNode;
			return ds.getStorageClass() == IASTDeclSpecifier.sc_typedef;
		} else if (parentNode instanceof ICPPASTUsingDirective) {
			return true;
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

	private void addToMap(Map<IIndexFileLocation, Symbols> map, IIndexFileLocation location, IASTName[] thing) {
		Symbols lists= map.get(location);
		if (lists != null)
			lists.fNames.add(thing);
	}

	private void addToMap(Map<IIndexFileLocation, Symbols> map, IIndexFileLocation location,
			IASTPreprocessorIncludeStatement thing) {
		Symbols lists= map.get(location);
		if (lists != null)
			lists.fIncludes.add(thing);
	}

	private void addToMap(Map<IIndexFileLocation, Symbols> map, IIndexFileLocation location,
			IASTPreprocessorStatement thing) {
		Symbols lists= map.get(location);
		if (lists != null)
			lists.fMacros.add(thing);
	}

	private boolean prepareInMap(Map<IIndexFileLocation, Symbols> map, IIndexFileLocation location) {
		if (map.get(location) == null) {
			map.put(location, new Symbols());
		}
		return false;
	}

	private IIndexFragmentFile storeFileInIndex(IWritableIndex index, IIndexFileLocation location,
			Map<IIndexFileLocation, Symbols> symbolMap, int linkageID, long fileContentsHash,
			int configHash, Set<IASTPreprocessorIncludeStatement> contextIncludes,
			YieldableIndexLock lock) throws CoreException, InterruptedException {
		Set<IIndexFileLocation> clearedContexts= Collections.emptySet();
		IIndexFragmentFile file;
		// We create a temporary PDOMFile with zero timestamp, add names to it, then replace contents
		// of the old file from the temporary one, then delete the temporary file. The write lock on
		// the index can be yielded between adding names to the temporary file, if another thread
		// is waiting for a read lock.
		IIndexFragmentFile oldFile = index.getWritableFile(linkageID, location);
		if (oldFile != null) {
			IIndexInclude[] includedBy = index.findIncludedBy(oldFile);
			if (includedBy.length > 0) {
				clearedContexts= new HashSet<IIndexFileLocation>();
				for (IIndexInclude include : includedBy) {
					clearedContexts.add(include.getIncludedByLocation());
				}
			}
		}
		file= index.addUncommittedFile(linkageID, location);
		try {
			file.setScannerConfigurationHashcode(configHash);
			Symbols lists= symbolMap.get(location);
			if (lists != null) {
				IASTPreprocessorStatement[] macros= lists.fMacros.toArray(new IASTPreprocessorStatement[lists.fMacros.size()]);
				IASTName[][] names= lists.fNames.toArray(new IASTName[lists.fNames.size()][]);
				for (IASTName[] name2 : names) {
					final IASTName name= name2[0];
					if (name != null) {
						ASTInternal.setFullyResolved(name.getBinding(), true);
					}
				}

				IncludeInformation[] includeInfos= new IncludeInformation[lists.fIncludes.size()];
				for (int i= 0; i < lists.fIncludes.size(); i++) {
					final IASTPreprocessorIncludeStatement include = lists.fIncludes.get(i);
					final IncludeInformation info= includeInfos[i]= new IncludeInformation();
					info.fStatement= include;
					if (include.isResolved()) {
						info.fLocation= fResolver.resolveASTPath(include.getPath());
						info.fIsContext= include.isActive() &&
							(contextIncludes.contains(include) || clearedContexts.contains(info.fLocation));
					}
				}
				index.setFileContent(file, linkageID, includeInfos, macros, names, fResolver, lock);
			}
			file.setTimestamp(fResolver.getLastModified(location));
			file.setEncodingHashcode(fResolver.getEncoding(location).hashCode());
			file.setContentsHash(fileContentsHash);
			file = index.commitUncommittedFile();
		} finally {
			index.clearUncommittedFile();
		}
		return file;
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
	protected final void updateFileCount(int sources, int primaryHeader, int header) {
		synchronized (fInfo) {
			fInfo.fCompletedSources += sources;
			fInfo.fPrimaryHeaderCount += primaryHeader;
			fInfo.fCompletedHeaders += header;
		}
	}

	/**
	 * Updates current progress information with the provided delta.
	 */
	protected final void updateRequestedFiles(int delta) {
		synchronized (fInfo) {
			fInfo.fRequestedFilesCount += delta;
		}
	}

	private String getLocationInfo(String filename, int lineNumber) {
		return " at " + filename + "(" + lineNumber + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	private void reportProblem(IProblemBinding problem) {
		String msg= "Indexer: unresolved name" + getLocationInfo(problem.getFileName(), problem.getLineNumber()); //$NON-NLS-1$
		String pmsg= problem.getMessage();
		if (pmsg != null && pmsg.length() > 0)
			msg += "; " + problem.getMessage(); //$NON-NLS-1$
		trace(msg);
	}

	private void reportProblem(IASTProblem problem) {
		String msg= "Indexer: " + problem.getMessageWithLocation(); //$NON-NLS-1$
		trace(msg);
	}

	protected void trace(String message) {
		System.out.println(message);
	}

	protected IStatus createStatus(String msg) {
		return CCorePlugin.createStatus(msg);
	}

	protected IStatus createStatus(String msg, Throwable e) {
		return CCorePlugin.createStatus(msg, e);
	}
}
