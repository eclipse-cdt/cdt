/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTPreprocessorSelectionResult;
import org.eclipse.cdt.internal.core.dom.parser.IRequiresLocationInformation;
import org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver;
import org.eclipse.cdt.internal.core.parser.scanner2.InvalidPreprocessorNodeException;

/**
 * @author jcamelon
 */
public class CASTTranslationUnit extends CASTNode implements
		IASTTranslationUnit, IRequiresLocationInformation {

	private IASTDeclaration[] decls = null;

	// Binding
	private CScope compilationUnit = null;

	private ILocationResolver resolver;

	private static final IASTPreprocessorStatement[] EMPTY_PREPROCESSOR_STATEMENT_ARRAY = new IASTPreprocessorStatement[0];

	private static final IASTNodeLocation[] EMPTY_PREPROCESSOR_LOCATION_ARRAY = new IASTNodeLocation[0];

	private static final IASTPreprocessorMacroDefinition[] EMPTY_PREPROCESSOR_MACRODEF_ARRAY = new IASTPreprocessorMacroDefinition[0];

	private static final IASTPreprocessorIncludeStatement[] EMPTY_PREPROCESSOR_INCLUSION_ARRAY = new IASTPreprocessorIncludeStatement[0];

	private static final IASTProblem[] EMPTY_PROBLEM_ARRAY = new IASTProblem[0];

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    private static final IASTName[] EMPTY_NAME_ARRAY = new IASTName[0];

	public void addDeclaration(IASTDeclaration d) {
        decls = (IASTDeclaration[]) ArrayUtil.append( IASTDeclaration.class, decls, d );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getDeclarations()
	 */
	public IASTDeclaration[] getDeclarations() {
		if (decls == null) return IASTDeclaration.EMPTY_DECLARATION_ARRAY;
		return (IASTDeclaration[]) ArrayUtil.removeNulls( IASTDeclaration.class, decls );
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getScope()
	 */
	public IScope getScope() {
		if (compilationUnit == null)
			compilationUnit = new CScope(this);
		return compilationUnit;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getDeclarations(org.eclipse.cdt.core.dom.ast.IBinding)
	 */
	public IASTName[] getDeclarations(IBinding binding) {
		if( binding instanceof IMacroBinding )
        {
            if( resolver == null )
                return EMPTY_NAME_ARRAY;
            return resolver.getDeclarations( (IMacroBinding)binding );
        }
		return CVisitor.getDeclarations(this, binding);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getReferences(org.eclipse.cdt.core.dom.ast.IBinding)
	 */
	public IASTName[] getReferences(IBinding binding) {
        if( binding instanceof IMacroBinding )
        {
            if( resolver == null )
                return EMPTY_NAME_ARRAY;
            return resolver.getReferences( (IMacroBinding)binding );
        }
		return CVisitor.getReferences(this, binding);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getLocationInfo(int,
	 *      int)
	 */
	public IASTNodeLocation[] getLocationInfo(int offset, int length) {
		if (resolver == null)
			return EMPTY_PREPROCESSOR_LOCATION_ARRAY;
		return resolver.getLocations(offset, length);
	}

	private class CFindNodeForOffsetAction extends CASTVisitor {
		{
			shouldVisitNames = true;
			shouldVisitDeclarations = true;
			shouldVisitInitializers = true;
			shouldVisitParameterDeclarations = true;
			shouldVisitDeclarators = true;
			shouldVisitDeclSpecifiers = true;
			shouldVisitDesignators = true;
			shouldVisitExpressions = true;
			shouldVisitStatements = true;
			shouldVisitTypeIds = true;
			shouldVisitEnumerators = true;
		}

		IASTNode foundNode = null;

		int offset = 0;

		int length = 0;

		/**
		 * 
		 */
		public CFindNodeForOffsetAction(int offset, int length) {
			this.offset = offset;
			this.length = length;
		}

		public int processNode(IASTNode node) {
			if (foundNode != null)
				return PROCESS_ABORT;

			if (node instanceof ASTNode
					&& ((ASTNode) node).getOffset() == offset
					&& ((ASTNode) node).getLength() == length) {
				foundNode = node;
				return PROCESS_ABORT;
			}

			// skip the rest of this node if the selection is outside of its
			// bounds
			if (node instanceof ASTNode
					&& offset > ((ASTNode) node).getOffset()
							+ ((ASTNode) node).getLength())
				return PROCESS_SKIP;

			return PROCESS_CONTINUE;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction#processDeclaration(org.eclipse.cdt.core.dom.ast.IASTDeclaration)
		 */
		public int visit(IASTDeclaration declaration) {
			// use declarations to determine if the search has gone past the
			// offset (i.e. don't know the order the visitor visits the nodes)
			if (declaration instanceof ASTNode
					&& ((ASTNode) declaration).getOffset() > offset)
				return PROCESS_ABORT;

			return processNode(declaration);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction#processDeclarator(org.eclipse.cdt.core.dom.ast.IASTDeclarator)
		 */
		public int visit(IASTDeclarator declarator) {
			int ret = processNode(declarator);

			IASTPointerOperator[] ops = declarator.getPointerOperators();
			for (int i = 0; i < ops.length; i++)
				processNode(ops[i]);

			if (declarator instanceof IASTArrayDeclarator) {
				IASTArrayModifier[] mods = ((IASTArrayDeclarator) declarator)
						.getArrayModifiers();
				for (int i = 0; i < mods.length; i++)
					processNode(mods[i]);
			}

			return ret;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processDesignator(org.eclipse.cdt.core.dom.ast.c.ICASTDesignator)
		 */
		public int visit(ICASTDesignator designator) {
			return processNode(designator);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processDeclSpecifier(org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier)
		 */
		public int visit(IASTDeclSpecifier declSpec) {
			return processNode(declSpec);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processEnumerator(org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator)
		 */
		public int visit(IASTEnumerator enumerator) {
			return processNode((IASTNode) enumerator);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
		 */
		public int visit(IASTExpression expression) {
			return processNode(expression);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processInitializer(org.eclipse.cdt.core.dom.ast.IASTInitializer)
		 */
		public int visit(IASTInitializer initializer) {
			return processNode(initializer);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processName(org.eclipse.cdt.core.dom.ast.IASTName)
		 */
		public int visit(IASTName name) {
			if (name.toString() != null)
				return processNode(name);
			return PROCESS_CONTINUE;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processParameterDeclaration(org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration)
		 */
		public int visit(
				IASTParameterDeclaration parameterDeclaration) {
			return processNode(parameterDeclaration);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processStatement(org.eclipse.cdt.core.dom.ast.IASTStatement)
		 */
		public int visit(IASTStatement statement) {
			return processNode(statement);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processTypeId(org.eclipse.cdt.core.dom.ast.IASTTypeId)
		 */
		public int visit(IASTTypeId typeId) {
			return processNode(typeId);
		}

		public IASTNode getNode() {
			return foundNode;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getNodeForLocation(org.eclipse.cdt.core.dom.ast.IASTNodeLocation)
	 */
	public IASTNode selectNodeForLocation(String path, int realOffset,
			int realLength) {
    	IASTNode node = null;
		ASTPreprocessorSelectionResult result = null;
		int globalOffset = 0;
		
		try {
			result = resolver.getPreprocessorNode(path, realOffset, realLength);
		} catch (InvalidPreprocessorNodeException ipne) {
			globalOffset = ipne.getGlobalOffset(); 
		}
    	
		if (result != null && result.getSelectedNode() != null) {
			node = result.getSelectedNode();
		} else {
			// use the globalOffset to get the node from the AST if it's valid
			globalOffset = result == null ? globalOffset : result.getGlobalOffset();
    		if (globalOffset >= 0) {
	    		CFindNodeForOffsetAction nodeFinder = new CFindNodeForOffsetAction(globalOffset, realLength);
	    		accept(nodeFinder);
	    		node = nodeFinder.getNode();
    		}
		}
    	
        return node;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getMacroDefinitions()
	 */
	public IASTPreprocessorMacroDefinition[] getMacroDefinitions() {
		if (resolver == null)
			return EMPTY_PREPROCESSOR_MACRODEF_ARRAY;
		IASTPreprocessorMacroDefinition[] result = resolver
				.getMacroDefinitions();
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getIncludeDirectives()
	 */
	public IASTPreprocessorIncludeStatement[] getIncludeDirectives() {
		if (resolver == null)
			return EMPTY_PREPROCESSOR_INCLUSION_ARRAY;
		IASTPreprocessorIncludeStatement[] result = resolver
				.getIncludeDirectives();
		return result;
	}

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getAllPreprocessorStatements()
	 */
	public IASTPreprocessorStatement[] getAllPreprocessorStatements() {
		if (resolver == null)
			return EMPTY_PREPROCESSOR_STATEMENT_ARRAY;
		IASTPreprocessorStatement[] result = resolver
				.getAllPreprocessorStatements();
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.internal.core.parser2.IRequiresLocationInformation#setLocationResolver(org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver)
	 */
	public void setLocationResolver(ILocationResolver resolver) {
		this.resolver = resolver;
        resolver.setRootNode( this );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		if (resolver != null)
			resolver.cleanup();
		super.finalize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getPreprocesorProblems()
	 */
	public IASTProblem[] getPreprocessorProblems() {
		if (resolver == null)
			return EMPTY_PROBLEM_ARRAY;
		IASTProblem[] result = resolver.getScannerProblems();
		for (int i = 0; i < result.length; ++i) {
			IASTProblem p = result[i];
			p.setParent(this);
			p.setPropertyInParent(IASTTranslationUnit.SCANNER_PROBLEM);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getUnpreprocessedSignature(org.eclipse.cdt.core.dom.ast.IASTNodeLocation[])
	 */
	public String getUnpreprocessedSignature(IASTNodeLocation[] locations) {
		if (resolver == null)
			return EMPTY_STRING;
		return new String(resolver.getUnpreprocessedSignature(locations));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getFilePath()
	 */
	public String getFilePath() {
		if (resolver == null)
			return EMPTY_STRING;
		return new String(resolver.getTranslationUnitPath());
	}
	
	 public boolean accept( ASTVisitor action ){
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
        return true;
    }

    public IASTFileLocation flattenLocationsToFile(IASTNodeLocation[] nodeLocations) {
        if( resolver == null )
            return null;
        return resolver.flattenLocations( nodeLocations );
    }

    public IASTName[] getMacroExpansions() {
        if( resolver == null )
            return EMPTY_NAME_ARRAY;
        return resolver.getMacroExpansions();
    }

    public IDependencyTree getDependencyTree() {
        if( resolver == null )
            return null;
        return resolver.getDependencyTree();
    }

	public String getContainingFilename(int offset) {
		if( resolver == null )
			return EMPTY_STRING;
		return resolver.getContainingFilename( offset );
	}
}
