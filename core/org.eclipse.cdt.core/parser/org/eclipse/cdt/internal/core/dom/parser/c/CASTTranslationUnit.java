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

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorSelectionResult;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTVisitor;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IRequiresLocationInformation;
import org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver;
import org.eclipse.cdt.internal.core.parser.scanner2.InvalidPreprocessorNodeException;

/**
 * @author jcamelon
 */
public class CASTTranslationUnit extends CASTNode implements
		IASTTranslationUnit, IRequiresLocationInformation {

	private IASTDeclaration[] decls = null;

	private static final int DEFAULT_CHILDREN_LIST_SIZE = 8;

	private int currentIndex = 0;

	// Binding
	private CScope compilationUnit = null;

	private CVisitor visitor = null;

	private ILocationResolver resolver;

	private static final IASTPreprocessorStatement[] EMPTY_PREPROCESSOR_STATEMENT_ARRAY = new IASTPreprocessorStatement[0];

	private static final IASTNodeLocation[] EMPTY_PREPROCESSOR_LOCATION_ARRAY = new IASTNodeLocation[0];

	private static final IASTPreprocessorMacroDefinition[] EMPTY_PREPROCESSOR_MACRODEF_ARRAY = new IASTPreprocessorMacroDefinition[0];

	private static final IASTPreprocessorIncludeStatement[] EMPTY_PREPROCESSOR_INCLUSION_ARRAY = new IASTPreprocessorIncludeStatement[0];

	private static final IASTProblem[] EMPTY_PROBLEM_ARRAY = new IASTProblem[0];

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	public void addDeclaration(IASTDeclaration d) {
		if (decls == null) {
			decls = new IASTDeclaration[DEFAULT_CHILDREN_LIST_SIZE];
			currentIndex = 0;
		}
		if (decls.length == currentIndex) {
			IASTDeclaration[] old = decls;
			decls = new IASTDeclaration[old.length * 2];
			for (int i = 0; i < old.length; ++i)
				decls[i] = old[i];
		}
		decls[currentIndex++] = d;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getDeclarations()
	 */
	public IASTDeclaration[] getDeclarations() {
		if (decls == null)
			return IASTDeclaration.EMPTY_DECLARATION_ARRAY;
		removeNullDeclarations();
		return decls;
	}

	/**
	 * @param decls2
	 */
	private void removeNullDeclarations() {
		int nullCount = 0;
		for (int i = 0; i < decls.length; ++i)
			if (decls[i] == null)
				++nullCount;
		if (nullCount == 0)
			return;
		IASTDeclaration[] old = decls;
		int newSize = old.length - nullCount;
		decls = new IASTDeclaration[newSize];
		for (int i = 0; i < newSize; ++i)
			decls[i] = old[i];
		currentIndex = newSize;
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
		// TODO if binding is macro, circumvent the visitor
		return CVisitor.getDeclarations(this, binding);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getReferences(org.eclipse.cdt.core.dom.ast.IBinding)
	 */
	public IASTName[] getReferences(IBinding binding) {
		// TODO if binding is macro, circumvent the visitor
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

	private class CFindNodeForOffsetAction extends CVisitor.CBaseVisitorAction {
		{
			processNames = true;
			processDeclarations = true;
			processInitializers = true;
			processParameterDeclarations = true;
			processDeclarators = true;
			processDeclSpecifiers = true;
			processDesignators = true;
			processExpressions = true;
			processStatements = true;
			processTypeIds = true;
			processEnumerators = true;
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
		public int processDeclaration(IASTDeclaration declaration) {
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
		public int processDeclarator(IASTDeclarator declarator) {
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
		public int processDesignator(ICASTDesignator designator) {
			return processNode(designator);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processDeclSpecifier(org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier)
		 */
		public int processDeclSpecifier(IASTDeclSpecifier declSpec) {
			return processNode(declSpec);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processEnumerator(org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator)
		 */
		public int processEnumerator(IASTEnumerator enumerator) {
			return processNode((IASTNode) enumerator);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
		 */
		public int processExpression(IASTExpression expression) {
			return processNode(expression);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processInitializer(org.eclipse.cdt.core.dom.ast.IASTInitializer)
		 */
		public int processInitializer(IASTInitializer initializer) {
			return processNode(initializer);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processName(org.eclipse.cdt.core.dom.ast.IASTName)
		 */
		public int processName(IASTName name) {
			if (name.toString() != null)
				return processNode(name);
			return PROCESS_CONTINUE;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processParameterDeclaration(org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration)
		 */
		public int processParameterDeclaration(
				IASTParameterDeclaration parameterDeclaration) {
			return processNode(parameterDeclaration);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processStatement(org.eclipse.cdt.core.dom.ast.IASTStatement)
		 */
		public int processStatement(IASTStatement statement) {
			return processNode(statement);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processTypeId(org.eclipse.cdt.core.dom.ast.IASTTypeId)
		 */
		public int processTypeId(IASTTypeId typeId) {
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
		IASTPreprocessorSelectionResult result = null;
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
	    		getVisitor().visitTranslationUnit(nodeFinder);
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
		setParentRelationship(result,
				IASTTranslationUnit.PREPROCESSOR_STATEMENT);
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
		setParentRelationship(result,
				IASTTranslationUnit.PREPROCESSOR_STATEMENT);
		return result;
	}

	/**
	 * @param result
	 * @param preprocessor_statement
	 */
	protected void setParentRelationship(IASTNode[] result,
			ASTNodeProperty property) {
		for (int i = 0; i < result.length; ++i) {
			result[i].setParent(this);
			result[i].setPropertyInParent(property);
		}
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
		setParentRelationship(result,
				IASTTranslationUnit.PREPROCESSOR_STATEMENT);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.internal.core.parser2.IRequiresLocationInformation#setLocationResolver(org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver)
	 */
	public void setLocationResolver(ILocationResolver resolver) {
		this.resolver = resolver;
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
	public IASTProblem[] getPreprocesorProblems() {
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
	 * @see org.eclipse.cdt.core.dom.ast.IASTTranslationUnit#getVisitor()
	 */
	public IASTVisitor getVisitor() {
		if (visitor == null)
			visitor = new CVisitor(this);
		return visitor;
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
}
