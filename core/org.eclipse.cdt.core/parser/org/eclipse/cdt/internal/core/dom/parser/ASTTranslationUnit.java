/*******************************************************************************
 * Copyright (c) 2008, 2016 Wind River Systems, Inc. and others.
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
 *     Sergey Prigogin (Google)
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.ASTGenericVisitor;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.parser.IBuiltinBindingsProvider;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.ISignificantMacros;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.index.IndexBasedFileContentProvider;
import org.eclipse.cdt.internal.core.index.IndexFileSet;
import org.eclipse.cdt.internal.core.parser.scanner.ILocationResolver;
import org.eclipse.cdt.internal.core.parser.scanner.ISkippedIndexedFilesListener;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContentProvider;
import org.eclipse.core.runtime.CoreException;

/**
 * Abstract base class for all translation units.
 *
 * This class and other ASTNode subclasses are not thread safe.
 * Even 'get' methods may cause changes to the underlying object.
 */
public abstract class ASTTranslationUnit extends ASTNode implements IASTTranslationUnit, ISkippedIndexedFilesListener {
	private static final IASTPreprocessorStatement[] EMPTY_PREPROCESSOR_STATEMENT_ARRAY = {};
	private static final IASTPreprocessorMacroDefinition[] EMPTY_PREPROCESSOR_MACRODEF_ARRAY = {};
	private static final IASTPreprocessorIncludeStatement[] EMPTY_PREPROCESSOR_INCLUSION_ARRAY = {};
	private static final IASTProblem[] EMPTY_PROBLEM_ARRAY = {};
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private IASTDeclaration[] fAllDeclarations;
	private IASTDeclaration[] fActiveDeclarations;
	private int fLastDeclaration = -1;

	protected ILocationResolver fLocationResolver;
	private IIndex fIndex;
	private boolean fIsHeader = true;
	private IIndexFileSet fIndexFileSet;
	private IIndexFileSet fASTFileSet;
	private INodeFactory fNodeFactory;
	private boolean fForContentAssist;
	private ITranslationUnit fOriginatingTranslationUnit;
	private ISignificantMacros fSignificantMacros = ISignificantMacros.NONE;
	private boolean fPragmaOnceSemantics;
	private SizeofCalculator fSizeofCalculator;
	/** The semaphore controlling exclusive access to the AST. */
	private final Semaphore fSemaphore = new Semaphore(1);
	private boolean fBasedOnIncompleteIndex;
	private boolean fNodesOmitted;
	private IBuiltinBindingsProvider fBuiltinBindingsProvider;

	// Caches
	private final ThreadLocal<WeakHashMap<IType, String>> fUnnormalizedTypeStringCache = new ThreadLocal<WeakHashMap<IType, String>>() {
		@Override
		protected WeakHashMap<IType, String> initialValue() {
			return new WeakHashMap<>();
		}
	};
	private final ThreadLocal<WeakHashMap<IType, String>> fNormalizedTypeStringCache = new ThreadLocal<WeakHashMap<IType, String>>() {
		@Override
		protected WeakHashMap<IType, String> initialValue() {
			return new WeakHashMap<>();
		}
	};

	@Override
	public final IASTTranslationUnit getTranslationUnit() {
		return this;
	}

	@Override
	public final void addDeclaration(IASTDeclaration d) {
		if (d != null) {
			d.setParent(this);
			d.setPropertyInParent(OWNED_DECLARATION);
			fAllDeclarations = ArrayUtil.appendAt(IASTDeclaration.class, fAllDeclarations, ++fLastDeclaration, d);
			fActiveDeclarations = null;
		}
	}

	@Override
	public final IASTDeclaration[] getDeclarations() {
		IASTDeclaration[] active = fActiveDeclarations;
		if (active == null) {
			active = ASTQueries.extractActiveDeclarations(fAllDeclarations, fLastDeclaration + 1);
			fActiveDeclarations = active;
		}
		return active;
	}

	@Override
	public final IASTDeclaration[] getDeclarations(boolean includeInactive) {
		if (includeInactive) {
			fAllDeclarations = ArrayUtil.trimAt(IASTDeclaration.class, fAllDeclarations, fLastDeclaration);
			return fAllDeclarations;
		}
		return getDeclarations();
	}

	public final void replace(IASTNode child, IASTNode other) {
		assert child.isActive() == other.isActive();
		for (int i = 0; i <= fLastDeclaration; ++i) {
			if (fAllDeclarations[i] == child) {
				other.setParent(child.getParent());
				other.setPropertyInParent(child.getPropertyInParent());
				fAllDeclarations[i] = (IASTDeclaration) other;
				fActiveDeclarations = null;
				return;
			}
		}
	}

	@Override
	public final IName[] getDeclarations(IBinding binding) {
		IName[] names = getDeclarationsInAST(binding);
		if (names.length == 0 && fIndex != null) {
			try {
				names = fIndex.findDeclarations(binding);
			} catch (CoreException e) {
				CCorePlugin.log(e);
				return names;
			}
		}

		return names;
	}

	protected final IASTName[] getMacroDefinitionsInAST(IMacroBinding binding) {
		if (fLocationResolver == null)
			return IASTName.EMPTY_NAME_ARRAY;

		IASTName[] declarations = fLocationResolver.getDeclarations(binding);
		int j = 0;
		for (int i = 0; i < declarations.length; i++) {
			IASTName name = declarations[i];
			if (name.isPartOfTranslationUnitFile()) {
				declarations[j++] = name;
			}
		}
		if (j < declarations.length)
			return j > 0 ? Arrays.copyOf(declarations, j) : IASTName.EMPTY_NAME_ARRAY;
		return declarations;
	}

	protected final IASTName[] getMacroReferencesInAST(IMacroBinding binding) {
		if (fLocationResolver == null)
			return IASTName.EMPTY_NAME_ARRAY;
		return fLocationResolver.getReferences(binding);
	}

	@Override
	public final IName[] getDefinitions(IBinding binding) {
		IName[] names = getDefinitionsInAST(binding);
		if (names.length == 0 && fIndex != null) {
			try {
				names = fIndex.findDefinitions(binding);
			} catch (CoreException e) {
				CCorePlugin.log(e);
				return names;
			}
		}
		return names;
	}

	@Override
	public final IASTPreprocessorMacroDefinition[] getMacroDefinitions() {
		if (fLocationResolver == null)
			return EMPTY_PREPROCESSOR_MACRODEF_ARRAY;
		return fLocationResolver.getMacroDefinitions();
	}

	@Override
	public IASTPreprocessorMacroExpansion[] getMacroExpansions() {
		if (fLocationResolver == null)
			return IASTPreprocessorMacroExpansion.EMPTY_ARRAY;
		return fLocationResolver.getMacroExpansions(getFileLocation());
	}

	@Override
	public final IASTPreprocessorMacroDefinition[] getBuiltinMacroDefinitions() {
		if (fLocationResolver == null)
			return EMPTY_PREPROCESSOR_MACRODEF_ARRAY;
		return fLocationResolver.getBuiltinMacroDefinitions();
	}

	@Override
	public final IASTPreprocessorIncludeStatement[] getIncludeDirectives() {
		if (fLocationResolver == null)
			return EMPTY_PREPROCESSOR_INCLUSION_ARRAY;
		return fLocationResolver.getIncludeDirectives();
	}

	@Override
	public final IASTPreprocessorStatement[] getAllPreprocessorStatements() {
		if (fLocationResolver == null)
			return EMPTY_PREPROCESSOR_STATEMENT_ARRAY;
		return fLocationResolver.getAllPreprocessorStatements();
	}

	public final void setLocationResolver(ILocationResolver resolver) {
		fLocationResolver = resolver;
		resolver.setRootNode(this);
	}

	@Override
	public final IASTProblem[] getPreprocessorProblems() {
		if (fLocationResolver == null)
			return EMPTY_PROBLEM_ARRAY;
		IASTProblem[] result = fLocationResolver.getScannerProblems();
		for (int i = 0; i < result.length; ++i) {
			IASTProblem p = result[i];
			p.setParent(this);
			p.setPropertyInParent(IASTTranslationUnit.SCANNER_PROBLEM);
		}
		return result;
	}

	@Override
	public final int getPreprocessorProblemsCount() {
		return fLocationResolver == null ? 0 : fLocationResolver.getScannerProblemsCount();
	}

	@Override
	public final String getFilePath() {
		if (fLocationResolver == null)
			return EMPTY_STRING;
		return fLocationResolver.getTranslationUnitPath();
	}

	@Override
	public final boolean accept(ASTVisitor action) {
		if (action.shouldVisitTranslationUnit) {
			switch (action.visit(this)) {
			case ASTVisitor.PROCESS_ABORT:
				return false;
			case ASTVisitor.PROCESS_SKIP:
				return true;
			default:
				break;
			}
		}
		IASTDeclaration[] decls = getDeclarations(action.includeInactiveNodes);
		for (IASTDeclaration decl : decls) {
			if (!decl.accept(action))
				return false;
		}

		if (action.shouldVisitTranslationUnit && action.leave(this) == ASTVisitor.PROCESS_ABORT)
			return false;

		return true;
	}

	@Override
	public final IASTFileLocation flattenLocationsToFile(IASTNodeLocation[] nodeLocations) {
		if (fLocationResolver == null)
			return null;
		return fLocationResolver.flattenLocations(nodeLocations);
	}

	@Override
	public final IDependencyTree getDependencyTree() {
		if (fLocationResolver == null)
			return null;
		return fLocationResolver.getDependencyTree();
	}

	@Override
	public final String getContainingFilename(int offset) {
		if (fLocationResolver == null)
			return EMPTY_STRING;
		return fLocationResolver.getContainingFilePath(offset);
	}

	@Override
	public final IIndex getIndex() {
		return fIndex;
	}

	@Override
	public final void setIndex(IIndex index) {
		this.fIndex = index;
		if (index != null) {
			fIndexFileSet = index.createFileSet();
			fASTFileSet = index.createFileSet();
		}
	}

	@Override
	public final INodeFactory getASTNodeFactory() {
		return fNodeFactory;
	}

	public final void setASTNodeFactory(INodeFactory nodeFactory) {
		this.fNodeFactory = nodeFactory;
	}

	@Override
	public final IASTComment[] getComments() {
		if (fLocationResolver != null) {
			return fLocationResolver.getComments();
		}
		return IASTComment.EMPTY_COMMENT_ARRAY;
	}

	@Override
	@SuppressWarnings("unchecked")
	public final <T> T getAdapter(Class<T> adapter) {
		if (adapter.isInstance(fLocationResolver)) {
			return (T) fLocationResolver;
		}
		if (adapter.isInstance(fIndexFileSet)) {
			return (T) fIndexFileSet;
		}
		if (fLocationResolver != null && adapter.isInstance(fLocationResolver.getLexerOptions())) {
			return (T) fLocationResolver.getLexerOptions();
		}
		return null;
	}

	@Override
	public final boolean isHeaderUnit() {
		return fIsHeader;
	}

	@Override
	public final void setIsHeaderUnit(boolean headerUnit) {
		fIsHeader = headerUnit;
	}

	public boolean isForContentAssist() {
		return fForContentAssist;
	}

	public final void setIsForContentAssist(boolean forContentAssist) {
		fForContentAssist = forContentAssist;
	}

	@Override
	public boolean isBasedOnIncompleteIndex() {
		return fBasedOnIncompleteIndex;
	}

	public void setBasedOnIncompleteIndex(boolean basedOnIncompleteIndex) {
		fBasedOnIncompleteIndex = basedOnIncompleteIndex;
	}

	@Override
	public void skippedFile(int offset, InternalFileContent fileContent) {
		if (fIndexFileSet != null) {
			List<IIndexFile> files = fileContent.getFilesIncluded();
			for (IIndexFile indexFile : files) {
				fASTFileSet.remove(indexFile);
				fIndexFileSet.add(indexFile);
			}
		}
	}

	@Override
	public final IIndexFileSet getIndexFileSet() {
		return fIndexFileSet;
	}

	@Override
	public void parsingFile(InternalFileContentProvider provider, InternalFileContent fc) {
		if (fASTFileSet != null) {
			if (provider instanceof IndexBasedFileContentProvider) {
				try {
					for (IIndexFile file : ((IndexBasedFileContentProvider) provider).findIndexFiles(fc)) {
						if (!fIndexFileSet.contains(file)) {
							fASTFileSet.add(file);
						}
					}
				} catch (CoreException e) {
					// Ignore, tracking of replaced files fails.
				}
			}
		}
	}

	@Override
	public final IIndexFileSet getASTFileSet() {
		return fASTFileSet;
	}

	@Override
	public final IASTNode selectNodeForLocation(String path, int realOffset, int realLength) {
		return getNodeSelector(path).findNode(realOffset, realLength);
	}

	@Override
	public final IASTNodeSelector getNodeSelector(String filePath) {
		return new ASTNodeSelector(this, fLocationResolver, filePath);
	}

	/**
	 * Must be called by the parser, before the ast is passed to the clients.
	 */
	public abstract void resolveAmbiguities();

	/**
	 * Can be called to create a type for a type-id.
	 */
	protected abstract IType createType(IASTTypeId typeid);

	/**
	 * Maps an index scope to the AST.
	 *
	 * @param scope a scope, possibly from index
	 * @return the corresponding scope in the AST, or the original scope if it doesn't have
	 *     a counterpart in the AST.
	 */
	public abstract IScope mapToASTScope(IScope scope);

	protected <T extends ASTTranslationUnit> T copy(T copy, CopyStyle style) {
		copy.setIndex(fIndex);
		ASTTranslationUnit target = copy;
		target.fIsHeader = fIsHeader;
		target.fNodeFactory = fNodeFactory;
		target.setLocationResolver(fLocationResolver);
		target.fForContentAssist = fForContentAssist;
		target.fOriginatingTranslationUnit = fOriginatingTranslationUnit;
		target.fNodesOmitted = fNodesOmitted;

		for (IASTDeclaration declaration : getDeclarations()) {
			copy.addDeclaration(declaration == null ? null : declaration.copy(style));
		}

		return super.copy(copy, style);
	}

	@Override
	public final void freeze() {
		accept(new ASTGenericVisitor(true) {
			@Override
			protected int genericVisit(IASTNode node) {
				((ASTNode) node).setIsFrozen();
				return PROCESS_CONTINUE;
			}
		});

		if (IndexFileSet.sDEBUG && fIndexFileSet != null && fASTFileSet != null) {
			long t = ((IndexFileSet) fIndexFileSet).getTimingContainsDeclarationNanos()
					+ ((IndexFileSet) fASTFileSet).getTimingContainsDeclarationNanos();
			String forName = fOriginatingTranslationUnit == null ? "" //$NON-NLS-1$
					: " for " + fOriginatingTranslationUnit.getElementName(); //$NON-NLS-1$
			System.out.println(String.format("IndexFileSet.containsDeclaration%s took %.2g ms", forName, t / 1.e6)); //$NON-NLS-1$
		}
	}

	@Override
	public ITranslationUnit getOriginatingTranslationUnit() {
		return fOriginatingTranslationUnit;
	}

	public void setOriginatingTranslationUnit(ITranslationUnit tu) {
		this.fOriginatingTranslationUnit = tu;
	}

	@Override
	public ISignificantMacros getSignificantMacros() {
		return fSignificantMacros;
	}

	@Override
	public void setSignificantMacros(ISignificantMacros sigMacros) {
		assertNotFrozen();
		if (sigMacros != null)
			fSignificantMacros = sigMacros;
	}

	@Override
	public boolean hasPragmaOnceSemantics() {
		return fPragmaOnceSemantics;
	}

	@Override
	public void setPragmaOnceSemantics(boolean value) {
		assertNotFrozen();
		fPragmaOnceSemantics = value;
	}

	/**
	 * Starts exclusive access.
	 *
	 * @throws InterruptedException if the current thread is interrupted
	 */
	public void beginExclusiveAccess() throws InterruptedException {
		fSemaphore.acquire();
	}

	/**
	 * Starts exclusive access.
	 *
	 * @param timeoutMillis the maximum time to wait in milliseconds
	 * @return {@code true} if exclusive access was acquired, or {@code false} if it
	 *     was not possible to acquire exclusive access before the timeout expired
	 * @throws InterruptedException if the current thread is interrupted
	 */
	public boolean tryBeginExclusiveAccess(long timeoutMillis) throws InterruptedException {
		return fSemaphore.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
	}

	public void endExclusiveAccess() {
		fSemaphore.release();
	}

	public SizeofCalculator getSizeofCalculator() {
		if (fSizeofCalculator == null) {
			fSizeofCalculator = new SizeofCalculator(this);
		}
		return fSizeofCalculator;
	}

	@Override
	public boolean hasNodesOmitted() {
		return fNodesOmitted;
	}

	@Override
	public void setHasNodesOmitted(boolean hasNodesOmitted) {
		assertNotFrozen();
		fNodesOmitted = hasNodesOmitted;
	}

	/**
	 * If ambiguity resolution is in progress, and processing of 'node' has been deferred,
	 * process it now. Has no effect if ambiguity resolution is not in progress.
	 */
	public void resolvePendingAmbiguities(IASTNode node) {
	}

	public void setupBuiltinBindings(IBuiltinBindingsProvider builtinBindingsProvider) {
		IScope tuScope = getScope();

		IBinding[] bindings = builtinBindingsProvider.getBuiltinBindings(tuScope);
		for (IBinding binding : bindings) {
			ASTInternal.addBinding(tuScope, binding);
		}

		// Save the builtin bindings provider for later use by isKnownBuiltin().
		fBuiltinBindingsProvider = builtinBindingsProvider;
	}

	public boolean isKnownBuiltin(char[] builtinName) {
		if (fBuiltinBindingsProvider != null) {
			return fBuiltinBindingsProvider.isKnownBuiltin(builtinName);
		}
		return false;
	}

	public Map<IType, String> getTypeStringCache(boolean normalized) {
		return normalized ? fNormalizedTypeStringCache.get() : fUnnormalizedTypeStringCache.get();
	}
}
