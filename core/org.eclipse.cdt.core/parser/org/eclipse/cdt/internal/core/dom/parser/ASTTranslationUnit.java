/*******************************************************************************
 * Copyright (c) 2008, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser;

import java.util.List;
import java.util.concurrent.Semaphore;

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
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.ISignificantMacros;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.index.IndexBasedFileContentProvider;
import org.eclipse.cdt.internal.core.parser.scanner.ILocationResolver;
import org.eclipse.cdt.internal.core.parser.scanner.ISkippedIndexedFilesListener;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContentProvider;
import org.eclipse.cdt.internal.core.parser.scanner.Lexer.LexerOptions;
import org.eclipse.core.runtime.CoreException;

/**
 * Abstract base class for all translation units.
 *
 * This class and other ASTNode subclasses are not thread safe.
 * Even 'get' methods may cause changes to the underlying object.
 */
public abstract class ASTTranslationUnit extends ASTNode implements IASTTranslationUnit, ISkippedIndexedFilesListener {
	private static final IASTPreprocessorStatement[] EMPTY_PREPROCESSOR_STATEMENT_ARRAY = new IASTPreprocessorStatement[0];
	private static final IASTPreprocessorMacroDefinition[] EMPTY_PREPROCESSOR_MACRODEF_ARRAY = new IASTPreprocessorMacroDefinition[0];
	private static final IASTPreprocessorIncludeStatement[] EMPTY_PREPROCESSOR_INCLUSION_ARRAY = new IASTPreprocessorIncludeStatement[0];
	private static final IASTProblem[] EMPTY_PROBLEM_ARRAY = new IASTProblem[0];
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    private IASTDeclaration[] fAllDeclarations = null;
    private IASTDeclaration[] fActiveDeclarations= null;
	private int fLastDeclaration= -1;

	protected ILocationResolver fLocationResolver;
	private IIndex fIndex;
	private boolean fIsHeader= true;
	private IIndexFileSet fIndexFileSet;
	private IIndexFileSet fASTFileSet;
	private INodeFactory fNodeFactory;
	private boolean fForContentAssist;
	private ITranslationUnit fOriginatingTranslationUnit;
	private ISignificantMacros fSignificantMacros= ISignificantMacros.NONE;
	private boolean fPragmaOnceSemantics;
	private SizeofCalculator fSizeofCalculator;
	/** The semaphore controlling exclusive access to the AST. */
	private final Semaphore fSemaphore= new Semaphore(1);

	@Override
	public final IASTTranslationUnit getTranslationUnit() {
    	return this;
    }
    
	@Override
	public final void addDeclaration(IASTDeclaration d) {
		if (d != null) {
			d.setParent(this);
			d.setPropertyInParent(OWNED_DECLARATION);
			fAllDeclarations = (IASTDeclaration[]) ArrayUtil.append(IASTDeclaration.class,
					fAllDeclarations, ++fLastDeclaration, d);
			fActiveDeclarations= null;
		}
	}

	@Override
	public final IASTDeclaration[] getDeclarations() {
		IASTDeclaration[] active= fActiveDeclarations;
		if (active == null) {
			active = ASTQueries.extractActiveDeclarations(fAllDeclarations, fLastDeclaration+1);
			fActiveDeclarations= active;
		}
		return active;
	}

	@Override
	public final IASTDeclaration[] getDeclarations(boolean includeInactive) {
		if (includeInactive) {
			fAllDeclarations= (IASTDeclaration[]) ArrayUtil.removeNullsAfter(IASTDeclaration.class,
					fAllDeclarations, fLastDeclaration);
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
				fActiveDeclarations= null;
				return;
			}
		}
	}
    
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getDeclarations(org.eclipse.cdt.core.dom.ast.IBinding)
	 */
	@Override
	public final IName[] getDeclarations(IBinding binding) {
    	IName[] names= getDeclarationsInAST(binding);
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
		return fLocationResolver.getDeclarations(binding);
	}

	protected final IASTName[] getMacroReferencesInAST(IMacroBinding binding) {
		if (fLocationResolver == null)
			return IASTName.EMPTY_NAME_ARRAY;
        return fLocationResolver.getReferences(binding);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getDefinitions(org.eclipse.cdt.core.dom.ast.IBinding)
     */
    @Override
	public final IName[] getDefinitions(IBinding binding) {
    	IName[] names= getDefinitionsInAST(binding);
        if (names.length == 0 && fIndex != null) {
        	try {
        		names= fIndex.findDefinitions(binding);
        	} catch (CoreException e) {
        		CCorePlugin.log(e);
        		return names;
        	}
        }
        return names;
    }
    
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getMacroDefinitions()
	 */
	@Override
	public final IASTPreprocessorMacroDefinition[] getMacroDefinitions() {
		if (fLocationResolver == null)
			return EMPTY_PREPROCESSOR_MACRODEF_ARRAY;
		return fLocationResolver.getMacroDefinitions();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getMacroExpansions()
	 */
	@Override
	public IASTPreprocessorMacroExpansion[] getMacroExpansions() {
		if (fLocationResolver == null)
			return IASTPreprocessorMacroExpansion.EMPTY_ARRAY;
		return fLocationResolver.getMacroExpansions(getFileLocation());
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getBuiltinMacroDefinitions()
	 */
	@Override
	public final IASTPreprocessorMacroDefinition[] getBuiltinMacroDefinitions() {
		if (fLocationResolver == null)
			return EMPTY_PREPROCESSOR_MACRODEF_ARRAY;
		return fLocationResolver.getBuiltinMacroDefinitions();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getIncludeDirectives()
	 */
	@Override
	public final IASTPreprocessorIncludeStatement[] getIncludeDirectives() {
		if (fLocationResolver == null)
			return EMPTY_PREPROCESSOR_INCLUSION_ARRAY;
		return fLocationResolver.getIncludeDirectives();
	}

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getAllPreprocessorStatements()
	 */
	@Override
	public final IASTPreprocessorStatement[] getAllPreprocessorStatements() {
		if (fLocationResolver == null)
			return EMPTY_PREPROCESSOR_STATEMENT_ARRAY;
		return fLocationResolver.getAllPreprocessorStatements();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.internal.core.parser2.IRequiresLocationInformation#setLocationResolver(org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver)
	 */
	public final void setLocationResolver(ILocationResolver resolver) {
		fLocationResolver= resolver;
        resolver.setRootNode(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getPreprocesorProblems()
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getFilePath()
	 */
	@Override
	public final String getFilePath() {
		if (fLocationResolver == null)
			return EMPTY_STRING;
		return new String(fLocationResolver.getTranslationUnitPath());
	}
	
	 @Override
	public final boolean accept(ASTVisitor action) {
		if (action.shouldVisitTranslationUnit) {
			switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
		IASTDeclaration[] decls = getDeclarations(action.includeInactiveNodes);
		for (IASTDeclaration decl : decls) {
			if (!decl.accept(action)) return false;
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
    		fIndexFileSet= index.createFileSet();
    		fASTFileSet= index.createFileSet();
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
		return new IASTComment[0];
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(fLocationResolver.getClass())) {
			return fLocationResolver;
		}
		if (adapter.isAssignableFrom(IIndexFileSet.class)) {
			return fIndexFileSet;
		}
		if (adapter.isAssignableFrom(LexerOptions.class)) {
			return fLocationResolver.getLexerOptions();
		}
		return null;
	}

	@Override
	public final boolean isHeaderUnit() {
		return fIsHeader;
	}

	@Override
	public final void setIsHeaderUnit(boolean headerUnit) {
		fIsHeader= headerUnit;
	}
	
	public boolean isForContentAssist() {
		return fForContentAssist;
	}

	public final void setIsForContentAssist(boolean forContentAssist) {
		fForContentAssist= forContentAssist;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.ISkippedIndexedFilesListener#skippedFile(org.eclipse.cdt.internal.core.parser.scanner.IncludeFileContent)
	 */
	@Override
	public void skippedFile(int offset, InternalFileContent fileContent) {
		if (fIndexFileSet != null) {
			List<IIndexFile> files= fileContent.getFilesIncluded();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getNodeForLocation(org.eclipse.cdt.core.dom.ast.IASTNodeLocation)
	 */
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
	abstract protected IType createType(IASTTypeId typeid);

	protected void copyAbstractTU(ASTTranslationUnit copy, CopyStyle style) {
		copy.setIndex(fIndex);
		copy.fIsHeader = fIsHeader;
		copy.fNodeFactory = fNodeFactory;
		copy.setLocationResolver(fLocationResolver);
		copy.fForContentAssist = fForContentAssist;
		copy.fOriginatingTranslationUnit = fOriginatingTranslationUnit;
		
		for (IASTDeclaration declaration : getDeclarations())
			copy.addDeclaration(declaration == null ? null : declaration.copy(style));

		copy.setOffsetAndLength(this);
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getOriginatingTranslationUnit()
	 */
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
			fSignificantMacros= sigMacros;
	}
	
	@Override
	public boolean hasPragmaOnceSemantics() {
		return fPragmaOnceSemantics;
	}
	
	@Override
	public void setPragmaOnceSemantics(boolean value) {
		assertNotFrozen();
		fPragmaOnceSemantics= value;
	}

	/**
	 * Starts exclusive access 
	 * @throws InterruptedException
	 */
	public void beginExclusiveAccess() throws InterruptedException {
		fSemaphore.acquire();
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
}
