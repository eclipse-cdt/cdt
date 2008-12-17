/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser;

import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IName;
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
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.parser.scanner.ILocationResolver;
import org.eclipse.cdt.internal.core.parser.scanner.ISkippedIndexedFilesListener;
import org.eclipse.cdt.internal.core.parser.scanner.IncludeFileContent;
import org.eclipse.cdt.internal.core.parser.scanner.Lexer.LexerOptions;
import org.eclipse.core.runtime.CoreException;

/**
 * Abstract base class for all translation units.
 */
public abstract class ASTTranslationUnit extends ASTNode implements IASTTranslationUnit, ISkippedIndexedFilesListener {

	private static final IASTPreprocessorStatement[] EMPTY_PREPROCESSOR_STATEMENT_ARRAY = new IASTPreprocessorStatement[0];
	private static final IASTPreprocessorMacroDefinition[] EMPTY_PREPROCESSOR_MACRODEF_ARRAY = new IASTPreprocessorMacroDefinition[0];
	private static final IASTPreprocessorIncludeStatement[] EMPTY_PREPROCESSOR_INCLUSION_ARRAY = new IASTPreprocessorIncludeStatement[0];
	private static final IASTProblem[] EMPTY_PROBLEM_ARRAY = new IASTProblem[0];
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    protected IASTDeclaration[] fDeclarations = null;
	protected int fLastDeclaration=-1;

	protected ILocationResolver fLocationResolver;
	private IIndex fIndex;
	private boolean fIsHeader= true;
	private IIndexFileSet fIndexFileSet;
	private INodeFactory fNodeFactory;
	
	
    @Override
	public final IASTTranslationUnit getTranslationUnit() {
    	return this;
    }
    
	public final void addDeclaration(IASTDeclaration d) {
		if (d != null) {
			d.setParent(this);
			d.setPropertyInParent(OWNED_DECLARATION);
			fDeclarations = (IASTDeclaration[]) ArrayUtil.append( IASTDeclaration.class, fDeclarations, ++fLastDeclaration, d);	
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getDeclarations()
	 */
	public final IASTDeclaration[] getDeclarations() {
		if (fDeclarations == null) return IASTDeclaration.EMPTY_DECLARATION_ARRAY;
		fDeclarations= (IASTDeclaration[]) ArrayUtil.removeNullsAfter( IASTDeclaration.class, fDeclarations, fLastDeclaration);
		return fDeclarations;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getDeclarations(org.eclipse.cdt.core.dom.ast.IBinding)
	 */
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
	public final IASTPreprocessorMacroDefinition[] getMacroDefinitions() {
		if (fLocationResolver == null)
			return EMPTY_PREPROCESSOR_MACRODEF_ARRAY;
		return fLocationResolver.getMacroDefinitions();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getMacroExpansions()
	 */
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

	
	public final int getPreprocessorProblemsCount() {
		return fLocationResolver == null ? 0 : fLocationResolver.getScannerProblemsCount();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getFilePath()
	 */
	public final String getFilePath() {
		if (fLocationResolver == null)
			return EMPTY_STRING;
		return new String(fLocationResolver.getTranslationUnitPath());
	}
	
	 @Override
	public final boolean accept( ASTVisitor action ){
        if( action.shouldVisitTranslationUnit){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        IASTDeclaration [] ds = getDeclarations();
        for( int i = 0; i < ds.length; i++ ){
            if( !ds[i].accept( action ) ) return false;
        }
        if( action.shouldVisitTranslationUnit){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }

	public final IASTFileLocation flattenLocationsToFile(IASTNodeLocation[] nodeLocations) {
        if (fLocationResolver == null)
            return null;
        return fLocationResolver.flattenLocations( nodeLocations );
    }

    public final IDependencyTree getDependencyTree() {
        if (fLocationResolver == null)
            return null;
        return fLocationResolver.getDependencyTree();
    }

	public final String getContainingFilename(int offset) {
		if (fLocationResolver == null)
			return EMPTY_STRING;
		return fLocationResolver.getContainingFilePath( offset );
	}

    public final IIndex getIndex() {
    	return fIndex;
    }
    
    public final void setIndex(IIndex index) {
    	this.fIndex = index;
    	if (index != null) {
    		fIndexFileSet= index.createFileSet();
    	}
    }

    public final INodeFactory getASTNodeFactory() {
    	return fNodeFactory;
    }
    
    public final void setASTNodeFactory(INodeFactory nodeFactory) {
    	this.fNodeFactory = nodeFactory;
    }
    
	public final IASTComment[] getComments() {
		if (fLocationResolver != null) {
			return fLocationResolver.getComments();
		}
		return new IASTComment[0];
	}

	@SuppressWarnings("unchecked")
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

	public final boolean isHeaderUnit() {
		return fIsHeader;
	}

	public final void setIsHeaderUnit(boolean headerUnit) {
		fIsHeader= headerUnit;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.scanner.ISkippedIndexedFilesListener#skippedFile(org.eclipse.cdt.internal.core.parser.scanner.IncludeFileContent)
	 */
	public void skippedFile(int offset, IncludeFileContent fileContent) {
		if (fIndexFileSet != null) {
			List<IIndexFile> files= fileContent.getFilesIncluded();
			for (IIndexFile indexFile : files) {
				fIndexFileSet.add(indexFile);
			}
		}
	}	
	
	public final IIndexFileSet getIndexFileSet() {
		return fIndexFileSet;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getNodeForLocation(org.eclipse.cdt.core.dom.ast.IASTNodeLocation)
	 */
	public final IASTNode selectNodeForLocation(String path, int realOffset, int realLength) {
		return getNodeSelector(path).findNode(realOffset, realLength);
	}
	
	public final IASTNodeSelector getNodeSelector(String filePath) {
		return new ASTNodeSelector(this, fLocationResolver, filePath);
	}

	/**
	 * @since 5.0
	 */
	public void cleanupAfterAmbiguityResolution() {
		// clear bindings (see bug 232811)
		accept(new ASTVisitor(){
			{shouldVisitNames= true;}
			@Override
			public int visit(IASTName name) {
				name.setBinding(null);
				return PROCESS_CONTINUE;
			}
		});
	}
	
	
	protected void copyAbstractTU(ASTTranslationUnit copy) {
		copy.setIndex(fIndex);
		copy.setIsHeaderUnit(fIsHeader);
		copy.setASTNodeFactory(fNodeFactory);
		copy.setLocationResolver(fLocationResolver);
		
		for(IASTDeclaration declaration : getDeclarations())
			copy.addDeclaration(declaration == null ? null : declaration.copy());
		
		copy.setOffsetAndLength(this);
	}
}
