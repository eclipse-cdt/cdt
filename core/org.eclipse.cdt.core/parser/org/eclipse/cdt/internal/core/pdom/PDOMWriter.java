/*******************************************************************************
 * Copyright (c) 2007, 2015 Wind River Systems, Inc. and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexSymbols;
import org.eclipse.cdt.core.index.IPDOMASTProcessor;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.ISignificantMacros;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalDeclaredVariable;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.index.FileContentKey;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.index.IWritableIndex.IncludeInformation;
import org.eclipse.cdt.internal.core.parser.scanner.LocationMap;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMASTAdapter;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerASTVisitor;
import org.eclipse.cdt.internal.core.util.Canceler;
import org.eclipse.cdt.internal.core.util.ICanceler;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;

/**
 * Abstract class to write information from AST.
 * @since 4.0
 */
public abstract class PDOMWriter implements IPDOMASTProcessor {
	private static final boolean REPORT_UNKNOWN_BUILTINS = false;

	public static class FileInAST {
		final IASTPreprocessorIncludeStatement includeStatement;
		final FileContentKey fileContentKey;
		final long timestamp;
		final long fileSize;
		final long contentsHash;
		final long sourceReadTime;
		final boolean hasError;

		public FileInAST(IASTPreprocessorIncludeStatement includeStmt, FileContentKey key) {
			includeStatement = includeStmt;
			fileContentKey = key;
			timestamp = includeStmt.getIncludedFileTimestamp();
			fileSize = includeStmt.getIncludedFileSize();
			contentsHash = includeStmt.getIncludedFileContentsHash();
			sourceReadTime = includeStmt.getIncludedFileReadTime();
			hasError = includeStmt.isErrorInIncludedFile();
		}

		public FileInAST(FileContentKey key, FileContent codeReader) {
			includeStatement = null;
			fileContentKey = key;
			timestamp = codeReader.getTimestamp();
			fileSize = codeReader.getFileSize();
			contentsHash = codeReader.getContentsHash();
			sourceReadTime = codeReader.getReadTime();
			hasError = codeReader.hasError();
		}

		@Override
		public String toString() {
			return fileContentKey.toString();
		}
	}

	public static class FileContext {
		final IIndexFragmentFile fContext;
		final IIndexFragmentFile fOldFile;
		IIndexFragmentFile fNewFile;
		public boolean fLostPragmaOnceSemantics;

		public FileContext(IIndexFragmentFile context, IIndexFragmentFile oldFile) {
			fContext = context;
			fOldFile = oldFile;
			fNewFile = null;
		}
	}

	public static int SKIP_ALL_REFERENCES = -1;
	public static int SKIP_TYPE_REFERENCES = 1;
	public static int SKIP_MACRO_REFERENCES = 2;
	public static int SKIP_IMPLICIT_REFERENCES = 4;
	public static int SKIP_NO_REFERENCES = 0;

	private static class Symbols {
		final ArrayList<IASTName[]> fNames = new ArrayList<>();
		final ArrayList<IASTPreprocessorStatement> fMacros = new ArrayList<>();
		final ArrayList<IASTPreprocessorIncludeStatement> fIncludes = new ArrayList<>();
	}

	protected static class Data implements IIndexSymbols {
		final IASTTranslationUnit fAST;
		final FileInAST[] fSelectedFiles;
		final IWritableIndex fIndex;
		final Map<IASTPreprocessorIncludeStatement, Symbols> fSymbolMap = new HashMap<>();
		final Set<IASTPreprocessorIncludeStatement> fContextIncludes = new HashSet<>();
		final List<IStatus> fStatuses = new ArrayList<>();
		Map<String, String> fReplacementHeaders; // Replacement headers keyed by file paths.

		public Data(IASTTranslationUnit ast, FileInAST[] selectedFiles, IWritableIndex index) {
			fAST = ast;
			fSelectedFiles = selectedFiles;
			fIndex = index;

			for (FileInAST file : selectedFiles) {
				fSymbolMap.put(file.includeStatement, new Symbols());
			}
		}

		@Override
		public boolean isEmpty() {
			if (fSymbolMap.isEmpty())
				return true;

			for (Symbols symbols : fSymbolMap.values()) {
				if (!symbols.fNames.isEmpty() || !symbols.fIncludes.isEmpty() || !symbols.fMacros.isEmpty()) {
					return false;
				}
			}

			return true;
		}

		@Override
		public void add(IASTPreprocessorIncludeStatement owner, IASTName name, IASTName caller) {
			Symbols lists = fSymbolMap.get(owner);
			if (lists != null)
				lists.fNames.add(new IASTName[] { name, caller });
		}

		@Override
		public void add(IASTPreprocessorIncludeStatement owner, IASTPreprocessorIncludeStatement thing) {
			Symbols lists = fSymbolMap.get(owner);
			if (lists != null)
				lists.fIncludes.add(thing);
		}

		@Override
		public void add(IASTPreprocessorIncludeStatement owner, IASTPreprocessorStatement thing) {
			Symbols lists = fSymbolMap.get(owner);
			if (lists != null)
				lists.fMacros.add(thing);
		}
	}

	protected boolean fShowProblems;
	protected boolean fShowInclusionProblems;
	private boolean fShowScannerProblems;
	private boolean fShowSyntaxProblems;
	protected boolean fShowActivity;
	protected final IndexerStatistics fStatistics;
	protected final IndexerInputAdapter fResolver;
	protected final ICanceler fCancelState = new Canceler();

	private int fSkipReferences = SKIP_NO_REFERENCES;

	public PDOMWriter(IndexerInputAdapter resolver) {
		fStatistics = new IndexerStatistics();
		fResolver = resolver;
	}

	protected IndexerInputAdapter getInputAdapter() {
		return fResolver;
	}

	public void setShowActivity(boolean val) {
		fShowActivity = val;
	}

	public void setShowInclusionProblems(boolean val) {
		fShowInclusionProblems = val;
	}

	public void setShowScannerProblems(boolean val) {
		fShowScannerProblems = val;
	}

	public void setShowSyntaxProblems(boolean val) {
		fShowSyntaxProblems = val;
	}

	public void setShowProblems(boolean val) {
		fShowProblems = val;
	}

	/**
	 * Determines whether references are skipped or not. Provide one of
	 * {@link #SKIP_ALL_REFERENCES}, {@link #SKIP_NO_REFERENCES} or a combination of
	 * {@link #SKIP_IMPLICIT_REFERENCES}, {@link #SKIP_TYPE_REFERENCES} and {@link #SKIP_MACRO_REFERENCES}.
	 */
	public void setSkipReferences(int options) {
		fSkipReferences = options;
	}

	public int getSkipReferences() {
		return fSkipReferences;
	}

	/**
	 * Extracts symbols from the given AST and adds them to the index.  Ignores Data maps that are
	 * empty and ones where storageLinkageID == {@link ILinkage#NO_LINKAGE_ID}.
	 * <p>
	 * When flushIndex is set to <code>false</code>, you must make sure to flush
	 * the index after your last write operation.
	 */
	final protected void addSymbols(Data data, int storageLinkageID, FileContext ctx, IProgressMonitor monitor)
			throws InterruptedException, CoreException {
		if (data.isEmpty() || storageLinkageID == ILinkage.NO_LINKAGE_ID)
			return;

		if (fShowProblems) {
			fShowInclusionProblems = true;
			fShowScannerProblems = true;
			fShowSyntaxProblems = true;
		}

		SubMonitor progress = SubMonitor.convert(monitor, 2);
		// Name resolution.
		resolveNames(data, progress.split(1));

		// Index update.
		storeSymbolsInIndex(data, storageLinkageID, ctx, progress.split(1));

		if (!data.fStatuses.isEmpty()) {
			List<IStatus> statuses = data.fStatuses;
			String path = null;
			if (data.fSelectedFiles.length > 0) {
				path = data.fSelectedFiles[data.fSelectedFiles.length - 1].fileContentKey.getLocation().getURI()
						.getPath();
			} else {
				path = data.fAST.getFilePath().toString();
			}
			String msg = NLS.bind(Messages.PDOMWriter_errorWhileParsing, path);
			if (statuses.size() == 1) {
				IStatus status = statuses.get(0);
				if (msg.equals(status.getMessage())) {
					throw new CoreException(status);
				}
				throw new CoreException(new Status(status.getSeverity(), status.getPlugin(), status.getCode(),
						msg + ':' + status.getMessage(), status.getException()));
			}
			throw new CoreException(new MultiStatus(CCorePlugin.PLUGIN_ID, 0,
					statuses.toArray(new IStatus[statuses.size()]), msg, null));
		}
	}

	private void storeSymbolsInIndex(final Data data, int storageLinkageID, FileContext ctx, IProgressMonitor monitor)
			throws InterruptedException, CoreException {
		final IIndexFragmentFile newFile = ctx == null ? null : ctx.fNewFile;
		SubMonitor progress = SubMonitor.convert(monitor, data.fSelectedFiles.length * 10);
		for (int i = 0; i < data.fSelectedFiles.length; i++) {
			final FileInAST fileInAST = data.fSelectedFiles[i];
			if (fileInAST != null) {
				if (fShowActivity) {
					trace("Indexer: adding " + fileInAST.fileContentKey.getLocation().getURI()); //$NON-NLS-1$
				}
				Throwable th = null;
				YieldableIndexLock lock = new YieldableIndexLock(data.fIndex, false, progress.split(1));
				lock.acquire();
				try {
					final boolean isReplacement = ctx != null && fileInAST.includeStatement == null;
					IIndexFragmentFile ifile = null;
					if (!isReplacement || newFile == null) {
						ifile = storeFileInIndex(data, fileInAST, storageLinkageID, lock, progress.split(9));
						reportFileWrittenToIndex(fileInAST, ifile);
					}

					if (isReplacement) {
						if (ifile == null)
							ifile = newFile;
						if (ctx != null && !ctx.fOldFile.equals(ifile) && ifile != null) {
							if (ctx.fOldFile.hasPragmaOnceSemantics() && !ifile.hasPragmaOnceSemantics()) {
								data.fIndex.transferContext(ctx.fOldFile, ifile);
								ctx.fLostPragmaOnceSemantics = true;
							} else {
								data.fIndex.transferIncluders(ctx.fOldFile, ifile);
							}
						}
					}
				} catch (OperationCanceledException e) {
					throw e;
				} catch (RuntimeException | StackOverflowError | AssertionError e) {
					th = e;
				} finally {
					// Because the caller holds a read-lock, the result cache of the index is never cleared.
					// Before releasing the lock for the last time in this AST, we clear the result cache.
					if (i == data.fSelectedFiles.length - 1) {
						data.fIndex.clearResultCache();
					}
					lock.release();
				}
				if (th != null) {
					data.fStatuses.add(createStatus(NLS.bind(Messages.PDOMWriter_errorWhileParsing,
							fileInAST.fileContentKey.getLocation().getURI().getPath()), th));
				}
				fStatistics.fAddToIndexTime += lock.getCumulativeLockTime();
			}
		}
	}

	private void resolveNames(Data data, IProgressMonitor monitor) {
		long start = System.currentTimeMillis();
		Set<ICPPInternalDeclaredVariable> variables = new HashSet<>();
		SubMonitor progress = SubMonitor.convert(monitor, data.fSelectedFiles.length);
		for (FileInAST file : data.fSelectedFiles) {
			Symbols symbols = data.fSymbolMap.get(file.includeStatement);

			final ArrayList<IASTName[]> names = symbols.fNames;
			SubMonitor progress2 = SubMonitor.convert(progress, names.size());
			boolean reported = false;
			for (Iterator<IASTName[]> j = names.iterator(); j.hasNext();) {
				final IASTName[] na = j.next();
				final IASTName name = na[0];
				progress2.split(1);
				if (name != null) { // Should not be null, just be defensive.
					try {
						final IBinding binding = name.resolveBinding();
						if (binding instanceof ICPPInternalDeclaredVariable) {
							ICPPInternalDeclaredVariable variable = (ICPPInternalDeclaredVariable) binding;
							if (variables.add(variable))
								variable.allDeclarationsDefinitionsAdded();
						}

						if (name.getPropertyInParent() == ICPPASTTemplateId.TEMPLATE_NAME
								&& (((IASTName) name.getParent()).getBinding() == binding
										|| binding instanceof ICPPFunctionTemplate)) {
							na[0] = null;
						} else if (binding instanceof IProblemBinding) {
							IProblemBinding problemBinding = (IProblemBinding) binding;
							if (REPORT_UNKNOWN_BUILTINS || problemBinding.getID() != IProblemBinding.BINDING_NOT_FOUND
									|| !CharArrayUtils.startsWith(problemBinding.getNameCharArray(), "__builtin_")) { //$NON-NLS-1$
								fStatistics.fProblemBindingCount++;
								if (fShowProblems) {
									reportProblem(problemBinding);
								}
							}
						} else if (name.isReference()) {
							if (binding instanceof ICPPTemplateParameter || binding instanceof ICPPUnknownBinding
									|| ((fSkipReferences & SKIP_TYPE_REFERENCES) != 0
											&& isTypeReferenceBinding(binding))) {
								if (!isRequiredReference(name)) {
									na[0] = null;
								} else {
									fStatistics.fReferenceCount++;
								}
							} else {
								fStatistics.fReferenceCount++;
							}
						} else {
							fStatistics.fDeclarationCount++;
						}
					} catch (RuntimeException | StackOverflowError e) {
						if (!reported) {
							data.fStatuses.add(CCorePlugin.createStatus(NLS.bind(Messages.PDOMWriter_errorResolvingName,
									name.toString(), file.fileContentKey.getLocation().getURI().getPath()), e));
						}
						reported = true;
						j.remove();
					}
				}
			}
		}

		// Precalculate types and initial values of all indexed variables to avoid doing it later when writing
		// to the index.
		for (ICPPInternalDeclaredVariable variable : variables) {
			if (isVariableIndexed(variable)) {
				IASTNode lookupPoint = variable.getDefinition() != null ? variable.getDefinition()
						: (variable.getDeclarations() != null && variable.getDeclarations().length > 0)
								? variable.getDeclarations()[0]
								: data.fAST;
				CPPSemantics.pushLookupPoint(lookupPoint);
				try {
					// Type and initial value will be cached by the variable.
					variable.getType();
					variable.getInitialValue();
				} finally {
					CPPSemantics.popLookupPoint();
				}
			}
		}

		fStatistics.fResolutionTime += System.currentTimeMillis() - start;
	}

	private boolean isVariableIndexed(ICPPVariable variable) {
		if (variable instanceof ICPPField)
			return true;
		IBinding owner = variable.getOwner();
		if (owner == null || owner instanceof ICPPNamespace)
			return true;
		return owner instanceof ICPPFunction && ((ICPPFunction) owner).isConstexpr();
	}

	@Override
	public int process(final IASTTranslationUnit ast, final IIndexSymbols symbols) throws CoreException {
		if (!(symbols instanceof Data)) {
			// TODO Fix this case -- the old implementation relies on the symbol map being exactly
			//      as expected.
			CCorePlugin.log(IStatus.ERROR, "Default processor must receive expected Data type"); //$NON-NLS-1$
			return ILinkage.NO_LINKAGE_ID;
		}

		int unresolvedIncludes = 0;
		Data data = (Data) symbols;
		final Map<IASTPreprocessorIncludeStatement, Symbols> symbolMap = data.fSymbolMap;

		IASTPreprocessorStatement[] stmts = ast.getAllPreprocessorStatements();
		for (final IASTPreprocessorStatement stmt : stmts) {
			// Includes.
			if (stmt instanceof IASTPreprocessorIncludeStatement) {
				IASTPreprocessorIncludeStatement include = (IASTPreprocessorIncludeStatement) stmt;

				final IASTFileLocation astLoc = include.getFileLocation();
				IASTPreprocessorIncludeStatement owner = astLoc.getContextInclusionStatement();
				final boolean updateSource = symbolMap.containsKey(owner);
				if (updateSource) {
					symbols.add(owner, include);
				}
				if (include.isActive()) {
					if (!include.isResolved()) {
						unresolvedIncludes++;
					} else if (updateSource) {
						// The include was parsed, check if we want to update the included file in the index.
						if (symbolMap.containsKey(include)) {
							data.fContextIncludes.add(include);
						}
					}
				}
			} else if (stmt.isActive() && (stmt instanceof IASTPreprocessorUndefStatement
					|| stmt instanceof IASTPreprocessorMacroDefinition)) {
				IASTFileLocation sourceLoc = stmt.getFileLocation();
				if (sourceLoc != null) { // skip built-ins and command line macros
					IASTPreprocessorIncludeStatement owner = sourceLoc.getContextInclusionStatement();
					symbols.add(owner, stmt);
				}
			}
		}

		// Names.
		final IndexerASTVisitor visitor = new IndexerASTVisitor((fSkipReferences & SKIP_IMPLICIT_REFERENCES) == 0) {
			private int cancelationCheckThrottler;

			@Override
			public void visit(IASTName name, IASTName caller) {
				checkForCancellation();

				if (fSkipReferences == SKIP_ALL_REFERENCES) {
					if (name.isReference()) {
						if (!isRequiredReference(name)) {
							return;
						}
					}
				}

				// Assign a location to anonymous types.
				name = PDOMASTAdapter.getAdapterIfAnonymous(name);
				if (name != null) {
					IASTFileLocation nameLoc = name.getFileLocation();
					if (nameLoc != null) {
						IASTPreprocessorIncludeStatement owner = nameLoc.getContextInclusionStatement();
						symbols.add(owner, name, caller);
					}
				}
			}

			private void checkForCancellation() {
				if (cancelationCheckThrottler <= 0) {
					if (fCancelState.isCanceled())
						throw new OperationCanceledException();
					cancelationCheckThrottler = 100;
				} else {
					cancelationCheckThrottler--;
				}
			}
		};
		CPPSemantics.pushLookupPoint(ast);
		try {
			ast.accept(visitor);
		} finally {
			CPPSemantics.popLookupPoint();
		}

		if ((fSkipReferences & SKIP_MACRO_REFERENCES) == 0) {

			// Get a tree of definitions built by IndexerASTVisitor during its traversal.
			// This is used to find enclosing definitions for macro references.
			IndexerASTVisitor.Definition definitionTree = visitor.getDefinitionTree();

			LocationMap lm = ast.getAdapter(LocationMap.class);
			if (lm != null) {
				IASTName[] refs = lm.getMacroReferences();
				for (IASTName name : refs) {
					IASTFileLocation nameLoc = name.getFileLocation();
					if (nameLoc != null) {
						IASTPreprocessorIncludeStatement owner = nameLoc.getContextInclusionStatement();
						IASTName enclosingDefinition = definitionTree.search(nameLoc.getNodeOffset(),
								nameLoc.getNodeLength());
						symbols.add(owner, name, enclosingDefinition);
					}
				}
			}
		}

		fStatistics.fUnresolvedIncludesCount += unresolvedIncludes;
		fStatistics.fPreprocessorProblemCount += ast.getPreprocessorProblemsCount() - unresolvedIncludes;
		if (fShowScannerProblems || fShowInclusionProblems) {
			final boolean reportAll = fShowScannerProblems && fShowInclusionProblems;
			IASTProblem[] scannerProblems = ast.getPreprocessorProblems();
			for (IASTProblem problem : scannerProblems) {
				if (reportAll
						|| (problem.getID() == IProblem.PREPROCESSOR_INCLUSION_NOT_FOUND) == fShowInclusionProblems) {
					reportProblem(problem);
				}
			}
		}

		final List<IASTProblem> problems = visitor.getProblems();
		fStatistics.fSyntaxProblemsCount += problems.size();
		if (fShowSyntaxProblems) {
			for (IASTProblem problem : problems) {
				reportProblem(problem);
			}
		}

		return ast.getLinkage().getLinkageID();
	}

	protected final boolean isRequiredReference(IASTName name) {
		IASTNode parentNode = name.getParent();
		if (parentNode instanceof ICPPASTQualifiedName) {
			if (name != ((ICPPASTQualifiedName) parentNode).getLastName())
				return false;
			parentNode = parentNode.getParent();
		}
		if (parentNode instanceof ICPPASTBaseSpecifier) {
			return true;
		} else if (parentNode instanceof IASTDeclSpecifier) {
			IASTDeclSpecifier ds = (IASTDeclSpecifier) parentNode;
			return ds.getStorageClass() == IASTDeclSpecifier.sc_typedef;
		} else if (parentNode instanceof ICPPASTUsingDirective) {
			return true;
		}
		return false;
	}

	private boolean isTypeReferenceBinding(IBinding binding) {
		if (binding instanceof ICompositeType || binding instanceof IEnumeration || binding instanceof ITypedef
				|| binding instanceof ICPPNamespace || binding instanceof ICPPNamespaceAlias
				|| binding instanceof ICPPClassTemplate) {
			return true;
		}
		return false;
	}

	private IIndexFragmentFile storeFileInIndex(Data data, FileInAST astFile, int storageLinkageID,
			YieldableIndexLock lock, IProgressMonitor monitor) throws CoreException, InterruptedException {
		final IWritableIndex index = data.fIndex;
		IIndexFragmentFile file;
		// We create a temporary PDOMFile with zero timestamp, add names to it, then replace
		// contents of the old file from the temporary one, then delete the temporary file.
		// The write lock on the index can be yielded between adding names to the temporary file,
		// if another thread is waiting for a read lock.
		final FileContentKey fileKey = astFile.fileContentKey;
		final IASTPreprocessorIncludeStatement owner = astFile.includeStatement;

		IIndexFileLocation location = fileKey.getLocation();
		ISignificantMacros significantMacros = fileKey.getSignificantMacros();
		IIndexFragmentFile oldFile = index.getWritableFile(storageLinkageID, location, significantMacros);
		file = index.addUncommittedFile(storageLinkageID, location, significantMacros);
		try {
			boolean pragmaOnce = owner != null ? owner.hasPragmaOnceSemantics() : data.fAST.hasPragmaOnceSemantics();
			file.setPragmaOnceSemantics(pragmaOnce);

			if (data.fReplacementHeaders != null) {
				String headerKey = IndexLocationFactory.getAbsolutePath(location).toOSString();
				String replacementHeader = data.fReplacementHeaders.get(headerKey);
				if (replacementHeader != null)
					file.setReplacementHeader(replacementHeader);
			}

			Symbols lists = data.fSymbolMap.get(owner);
			if (lists != null) {
				IASTPreprocessorStatement[] macros = lists.fMacros
						.toArray(new IASTPreprocessorStatement[lists.fMacros.size()]);
				IASTName[][] names = lists.fNames.toArray(new IASTName[lists.fNames.size()][]);
				for (IASTName[] name2 : names) {
					final IASTName name = name2[0];
					if (name != null) {
						ASTInternal.setFullyResolved(name.getBinding(), true);
					}
				}

				List<IncludeInformation> includeInfos = new ArrayList<>();
				for (int i = 0; i < lists.fIncludes.size(); i++) {
					final IASTPreprocessorIncludeStatement stmt = lists.fIncludes.get(i);
					if (!stmt.isResolved()) {
						includeInfos.add(new IncludeInformation(stmt, null, ISignificantMacros.NONE, false));
					} else {
						IIndexFileLocation targetLoc = fResolver.resolveASTPath(stmt.getPath());
						ISignificantMacros mainSig = stmt.getSignificantMacros();
						for (ISignificantMacros sig : stmt.getLoadedVersions()) {
							if (!sig.equals(mainSig)) {
								includeInfos.add(new IncludeInformation(stmt, targetLoc, sig, false));
							}
						}
						final boolean isContext = stmt.isActive() && stmt.isResolved()
								&& (data.fContextIncludes.contains(stmt) || isContextFor(oldFile, stmt));
						includeInfos.add(new IncludeInformation(stmt, targetLoc, mainSig, isContext));
					}
				}
				IncludeInformation[] includeInfoArray = includeInfos
						.toArray(new IncludeInformation[includeInfos.size()]);
				index.setFileContent(file, storageLinkageID, includeInfoArray, macros, names, fResolver, lock);
			}
			file.setTimestamp(astFile.hasError ? 0 : astFile.timestamp);
			file.setSourceReadTime(astFile.sourceReadTime);
			file.setSizeAndEncodingHashcode(computeFileSizeAndEncodingHashcode(astFile.fileSize, location));
			file.setContentsHash(astFile.contentsHash);
			file = index.commitUncommittedFile();
		} finally {
			index.clearUncommittedFile();
		}
		return file;
	}

	protected int computeFileSizeAndEncodingHashcode(IIndexFileLocation location) {
		return computeFileSizeAndEncodingHashcode((int) fResolver.getFileSize(location), location);
	}

	private int computeFileSizeAndEncodingHashcode(long size, IIndexFileLocation location) {
		return (int) size + 31 * fResolver.getEncoding(location).hashCode();
	}

	private boolean isContextFor(IIndexFragmentFile oldFile, IASTPreprocessorIncludeStatement stmt)
			throws CoreException {
		IIndexFile target = stmt.getImportedIndexFile();
		if (oldFile != null && target != null) {
			IIndexInclude ctxInclude = target.getParsedInContext();
			if (ctxInclude != null && oldFile.equals(ctxInclude.getIncludedBy()))
				return true;
		}
		return false;
	}

	/**
	 * Informs the subclass that a file has been stored in the index.
	 */
	protected abstract void reportFileWrittenToIndex(FileInAST file, IIndexFragmentFile iFile) throws CoreException;

	private String getLocationInfo(String filename, int lineNumber) {
		return " at " + filename + "(" + lineNumber + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	private void reportProblem(IProblemBinding problem) {
		String msg = "Indexer: unresolved name" + getLocationInfo(problem.getFileName(), problem.getLineNumber()); //$NON-NLS-1$
		String pmsg = problem.getMessage();
		if (pmsg != null && pmsg.length() > 0)
			msg += "; " + problem.getMessage(); //$NON-NLS-1$
		trace(msg);
	}

	private void reportProblem(IASTProblem problem) {
		String msg = "Indexer: " + problem.getMessageWithLocation(); //$NON-NLS-1$
		trace(msg);
	}

	protected void reportException(Throwable th) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		th.printStackTrace(pw);
		String msg = "Indexer: exception: " + sw.toString(); //$NON-NLS-1$
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

	public void cancel() {
		fCancelState.setCanceled(true);
	}
}
