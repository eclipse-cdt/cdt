/**********************************************************************
 * Copyright (c) 2005 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation 
 **********************************************************************/
package org.eclipse.cdt.ui.tests.DOMAST;

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
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemHolder;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionTryBlockDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisitor.CPPBaseVisitorAction;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.parser.scanner2.LocationMap.ASTInclusionStatement;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author dsteffle
 */
public class CPPPopulateASTViewAction extends CPPBaseVisitorAction implements IPopulateDOMASTAction {
	private static final int INITIAL_INCLUDE_STATEMENT_SIZE = 8;
	{
		processNames          = true;
		processDeclarations   = true;
		processInitializers   = true;
		processParameterDeclarations = true;
		processDeclarators    = true;
		processDeclSpecifiers = true;
		processExpressions    = true;
		processStatements     = true;
		processTypeIds        = true;
		processEnumerators    = true;
		processBaseSpecifiers = true;
		processNamespaces     = true;
	}

	TreeParent root = null;
	IProgressMonitor monitor = null;
	
	public CPPPopulateASTViewAction(IASTTranslationUnit tu, IProgressMonitor monitor) {
		root = new TreeParent(tu);
		this.monitor = monitor;
	}
	
	private int addRoot(IASTNode node) {
		if (monitor != null && monitor.isCanceled()) return PROCESS_ABORT;
		if (node == null) return PROCESS_CONTINUE;
		
		IASTNodeLocation[] nodeLocations = node.getNodeLocations();
        if (!(nodeLocations.length > 0 && 
				nodeLocations[0].getNodeOffset() >= 0 &&
				nodeLocations[0].getNodeLength() > 0))
			return PROCESS_CONTINUE;
		
		TreeParent parent = root.findTreeParentForNode(node);
		
		if (parent == null)
			parent = root;
		
		TreeParent tree = new TreeParent(node);
		parent.addChild(tree);
		
		// set filter flags
		if (node instanceof IASTProblemHolder || node instanceof IASTProblem) 
			tree.setFiltersFlag(TreeObject.FLAG_PROBLEM);
		if (node instanceof IASTPreprocessorStatement)
			tree.setFiltersFlag(TreeObject.FLAG_PREPROCESSOR);
		if (node instanceof IASTPreprocessorIncludeStatement)
			tree.setFiltersFlag(TreeObject.FLAG_INCLUDE_STATEMENTS);
		
		return PROCESS_CONTINUE;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction#processDeclaration(org.eclipse.cdt.core.dom.ast.IASTDeclaration)
	 */
	public int processDeclaration(IASTDeclaration declaration) {
		return addRoot(declaration);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction#processDeclarator(org.eclipse.cdt.core.dom.ast.IASTDeclarator)
	 */
	public int processDeclarator(IASTDeclarator declarator) {
		int ret = addRoot(declarator);
		
		IASTPointerOperator[] ops = declarator.getPointerOperators();
		for(int i=0; i<ops.length; i++)
			addRoot(ops[i]);
		
		if (declarator instanceof IASTArrayDeclarator) {
			IASTArrayModifier[] mods = ((IASTArrayDeclarator)declarator).getArrayModifiers();
			for(int i=0; i<mods.length; i++)
				addRoot(mods[i]);	
		}
		
		if (declarator instanceof ICPPASTFunctionDeclarator) {
			ICPPASTConstructorChainInitializer[] chainInit = ((ICPPASTFunctionDeclarator)declarator).getConstructorChain();
			for(int i=0; i<chainInit.length; i++) {
				addRoot(chainInit[i]);
			}
			
			if( declarator instanceof ICPPASTFunctionTryBlockDeclarator ){
				ICPPASTCatchHandler [] catchHandlers = ((ICPPASTFunctionTryBlockDeclarator)declarator).getCatchHandlers();
				for( int i = 0; i < catchHandlers.length; i++ ){
					addRoot(catchHandlers[i]);
				}
			}	
		}
		
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction#processBaseSpecifier(org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier)
	 */
	public int processBaseSpecifier(ICPPASTBaseSpecifier specifier) {
		return addRoot(specifier);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction#processDeclSpecifier(org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier)
	 */
	public int processDeclSpecifier(IASTDeclSpecifier declSpec) {
		return addRoot(declSpec);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction#processEnumerator(org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator)
	 */
	public int processEnumerator(IASTEnumerator enumerator) {
		return addRoot(enumerator);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction#processExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
	 */
	public int processExpression(IASTExpression expression) {
		return addRoot(expression);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction#processInitializer(org.eclipse.cdt.core.dom.ast.IASTInitializer)
	 */
	public int processInitializer(IASTInitializer initializer) {
		return addRoot(initializer);
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction#processName(org.eclipse.cdt.core.dom.ast.IASTName)
	 */
	public int processName(IASTName name) {
		if (name.toString() != null)
			return addRoot(name);
		return PROCESS_CONTINUE;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction#processNamespace(org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition)
	 */
	public int processNamespace(ICPPASTNamespaceDefinition namespace) {
		return addRoot(namespace);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction#processParameterDeclaration(org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration)
	 */
	public int processParameterDeclaration(
			IASTParameterDeclaration parameterDeclaration) {
		return addRoot(parameterDeclaration);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction#processStatement(org.eclipse.cdt.core.dom.ast.IASTStatement)
	 */
	public int processStatement(IASTStatement statement) {
		return addRoot(statement);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction#processTypeId(org.eclipse.cdt.core.dom.ast.IASTTypeId)
	 */
	public int processTypeId(IASTTypeId typeId) {
		return addRoot(typeId);
	}

	private void mergeNode(ASTNode node) {
		addRoot(node);
		
		if (node instanceof IASTPreprocessorMacroDefinition)
			addRoot(((IASTPreprocessorMacroDefinition)node).getName());
	}
	
	public void mergePreprocessorStatements(IASTPreprocessorStatement[] statements) {
		for(int i=0; i<statements.length; i++) {
			if (monitor != null && monitor.isCanceled()) return;
			
			if (statements[i] instanceof ASTNode)
				mergeNode((ASTNode)statements[i]);
		}
	}
	
	public void mergePreprocessorProblems(IASTProblem[] problems) {
		for(int i=0; i<problems.length; i++) {
			if (monitor != null && monitor.isCanceled()) return;
			
			if (problems[i] instanceof ASTNode)
			   mergeNode((ASTNode)problems[i]);
		}
	}
	
	public TreeParent getTree() {
		return root;
	}
	
	public void groupIncludes(IASTPreprocessorStatement[] statements) {
		// get all of the includes from the preprocessor statements (need the object since .equals isn't implemented)
		IASTPreprocessorIncludeStatement[] includes = new IASTPreprocessorIncludeStatement[INITIAL_INCLUDE_STATEMENT_SIZE];
		int index = 0;
		for(int i=0; i<statements.length; i++) {
			if (monitor != null && monitor.isCanceled()) return;
			if (index+1 > includes.length) {
				IASTPreprocessorIncludeStatement[] newIncludes = new IASTPreprocessorIncludeStatement[includes.length * 2];
				for (int j=0; j<includes.length; j++) {
					newIncludes[j] = includes[j];
				}
				includes = newIncludes;
			}
			
			if (statements[i] instanceof IASTPreprocessorIncludeStatement)
				includes[index++] = (IASTPreprocessorIncludeStatement)statements[i];
		}
		
		// get the tree model elements corresponding to the includes
		TreeParent[] treeIncludes = new TreeParent[includes.length];
		for (int i=0; i<treeIncludes.length; i++) {
			if (monitor != null && monitor.isCanceled()) return;
			treeIncludes[i] = root.findTreeObject(includes[i]);
		}
		
		// loop through the includes and make sure that all of the nodes 
		// that are children of the TU are in the proper include (based on offset)
		TreeObject child = null;
		for (int i=treeIncludes.length-1; i>=0; i--) {
			if (treeIncludes[i] == null) continue;

			for(int j=root.getChildren().length-1; j>=0; j--) {
				if (monitor != null && monitor.isCanceled()) return;
				child = root.getChildren()[j]; 
			
				if (treeIncludes[i] != child &&
						includes[i] instanceof ASTInclusionStatement &&
						((ASTNode)child.getNode()).getOffset() >= ((ASTInclusionStatement)includes[i]).startOffset &&
						((ASTNode)child.getNode()).getOffset() <= ((ASTInclusionStatement)includes[i]).endOffset) {
					root.removeChild(child);
					treeIncludes[i].addChild(child);
				}
			}
		}
	}
}
