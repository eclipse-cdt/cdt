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
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction;
import org.eclipse.cdt.internal.core.parser.scanner2.LocationMap.ASTInclusionStatement;

/**
 * @author dsteffle
 */
public class CPopulateASTViewAction extends CBaseVisitorAction implements IPopulateDOMASTAction {
	{
		processNames          = true;
		processDeclarations   = true;
		processInitializers   = true;
		processParameterDeclarations = true;
		processDeclarators    = true;
		processDeclSpecifiers = true;
		processDesignators 	  = true;
		processExpressions    = true;
		processStatements     = true;
		processTypeIds        = true;
		processEnumerators    = true;
	}

	TreeParent root = null; // TODO what about using a hashtable/hashmap for the tree?
	
	public CPopulateASTViewAction(IASTTranslationUnit tu) {
		root = new TreeParent(tu);
	}
	
	private void addRoot(IASTNode node) {
		if (node == null) return;
		
		IASTNodeLocation[] nodeLocations = node.getNodeLocations();
        if (!(nodeLocations.length > 0 && 
				nodeLocations[0].getNodeOffset() >= 0 &&
				nodeLocations[0].getNodeLength() > 0))
			return;
		
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
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction#processDeclaration(org.eclipse.cdt.core.dom.ast.IASTDeclaration)
	 */
	public int processDeclaration(IASTDeclaration declaration) {
		addRoot(declaration);
		return PROCESS_CONTINUE;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction#processDeclarator(org.eclipse.cdt.core.dom.ast.IASTDeclarator)
	 */
	public int processDeclarator(IASTDeclarator declarator) {
		addRoot(declarator);
		
		IASTPointerOperator[] ops = declarator.getPointerOperators();
		for(int i=0; i<ops.length; i++)
			addRoot(ops[i]);
		
		if (declarator instanceof IASTArrayDeclarator) {
			IASTArrayModifier[] mods = ((IASTArrayDeclarator)declarator).getArrayModifiers();
			for(int i=0; i<mods.length; i++)
				addRoot(mods[i]);	
		}
		
		return PROCESS_CONTINUE;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processDesignator(org.eclipse.cdt.core.dom.ast.c.ICASTDesignator)
	 */
	public int processDesignator(ICASTDesignator designator) {
		addRoot(designator);
		return PROCESS_CONTINUE;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processDeclSpecifier(org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier)
	 */
	public int processDeclSpecifier(IASTDeclSpecifier declSpec) {
		addRoot(declSpec);
		return PROCESS_CONTINUE;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processEnumerator(org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator)
	 */
	public int processEnumerator(IASTEnumerator enumerator) {
		addRoot(enumerator);
		return PROCESS_CONTINUE;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
	 */
	public int processExpression(IASTExpression expression) {
		addRoot(expression);
		return PROCESS_CONTINUE;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processInitializer(org.eclipse.cdt.core.dom.ast.IASTInitializer)
	 */
	public int processInitializer(IASTInitializer initializer) {
		addRoot(initializer);
		return PROCESS_CONTINUE;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processName(org.eclipse.cdt.core.dom.ast.IASTName)
	 */
	public int processName(IASTName name) {
		if ( name.toString() != null )
			addRoot(name);
		return PROCESS_CONTINUE;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processParameterDeclaration(org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration)
	 */
	public int processParameterDeclaration(
			IASTParameterDeclaration parameterDeclaration) {
		addRoot(parameterDeclaration);
		return PROCESS_CONTINUE;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processStatement(org.eclipse.cdt.core.dom.ast.IASTStatement)
	 */
	public int processStatement(IASTStatement statement) {
		addRoot(statement);
		return PROCESS_CONTINUE;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processTypeId(org.eclipse.cdt.core.dom.ast.IASTTypeId)
	 */
	public int processTypeId(IASTTypeId typeId) {
		addRoot(typeId);
		return PROCESS_CONTINUE;
	}
	
	private void mergeNode(ASTNode node) {
		addRoot(node);
		
		if (node instanceof IASTPreprocessorMacroDefinition )
			addRoot(((IASTPreprocessorMacroDefinition)node).getName());
	}
	
	private void mergeMacros(IASTPreprocessorMacroDefinition[] macros) {
		for(int i=0; i<macros.length; i++) {
			if (macros[i] instanceof ASTNode)
			   mergeNode((ASTNode)macros[i]);
		}
	}
	
	private void mergePreprocessorProblems(IASTProblem[] problems) {
		for(int i=0; i<problems.length; i++) {
			if (problems[i] instanceof ASTNode)
			   mergeNode((ASTNode)problems[i]);
		}
	}

	private void mergeIncludeDirectives(IASTPreprocessorIncludeStatement[] includes) {
		for(int i=0; i<includes.length; i++) {
			if (includes[i] instanceof ASTNode)
			   mergeNode((ASTNode)includes[i]);
		}
	}
	
	public TreeParent getTree() {
		if (root.getNode() instanceof IASTTranslationUnit) {
			IASTTranslationUnit tu = (IASTTranslationUnit)root.getNode();
			
			// merge macro definitions to the tree
			mergeMacros(tu.getMacroDefinitions());
			
			// merge preprocessor problems to the tree
			mergePreprocessorProblems(tu.getPreprocesorProblems());
			
			// merge include directives
			IASTPreprocessorIncludeStatement[] includes = tu.getIncludeDirectives();
			mergeIncludeDirectives(includes);
			
			// group #includes
			groupIncludes(includes);
		}
		
		return root;
	}
	
	private void groupIncludes(IASTPreprocessorIncludeStatement[] includes) {
		// get the tree model elements corresponding to the includes
		TreeParent[] treeIncludes = new TreeParent[includes.length];
		for (int i=0; i<treeIncludes.length; i++) {
			treeIncludes[i] = root.findTreeObject(includes[i]);
		}
		
		// loop through the includes and make sure that all of the nodes 
		// that are children of the TU are in the proper include (based on offset)
		TreeObject child = null;
		for (int i=treeIncludes.length-1; i>=0; i--) {
			if (treeIncludes[i] == null) continue;
			
			for(int j=root.getChildren().length-1; j>=0; j--) {
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
