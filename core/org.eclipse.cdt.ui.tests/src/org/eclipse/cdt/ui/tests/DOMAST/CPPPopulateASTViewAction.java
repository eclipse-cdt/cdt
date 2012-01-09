/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Rational Software - Initial API and implementation 
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.DOMAST;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.cdt.core.dom.ast.ASTGenericVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemHolder;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.util.ArrayUtil;

import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * @author dsteffle
 */
public class CPPPopulateASTViewAction extends ASTGenericVisitor implements IPopulateDOMASTAction {
	private static final int INITIAL_PROBLEM_SIZE = 4;

	DOMASTNodeParent root = null;
	IProgressMonitor monitor = null;
	IASTProblem[] astProblems = new IASTProblem[INITIAL_PROBLEM_SIZE];
	
	public CPPPopulateASTViewAction(IASTTranslationUnit tu, IProgressMonitor monitor) {
		super(true);
		shouldVisitTranslationUnit= false;
		root = new DOMASTNodeParent(tu);
		this.monitor = monitor;
	}
	
	private class DOMASTNodeLeafContinue extends DOMASTNodeLeaf {
		public DOMASTNodeLeafContinue(IASTNode node) {
			super(node);
		}
	}

	/** 
	 * return null if the algorithm should stop (monitor was cancelled)
	 * return DOMASTNodeLeafContinue if the algorithm should continue but no valid DOMASTNodeLeaf was added (i.e. node was null
	 * return the DOMASTNodeLeaf added to the DOM AST View's model otherwise 
	 * 
	 * @param node
	 * @return
	 */
	private DOMASTNodeLeaf addRoot(IASTNode node) {
        if (monitor != null && monitor.isCanceled()) return null;
        if (node == null) return new DOMASTNodeLeafContinue(null);
        
        // only do length check for ASTNode (getNodeLocations on PreprocessorStatements is very expensive)
        if (node instanceof ASTNode && ((ASTNode)node).getLength() <= 0 && !(node instanceof IASTProblemHolder))
            return new DOMASTNodeLeafContinue(null);
        
        DOMASTNodeParent parent = null;
        
        // if it's a preprocessor statement being merged then do a special search for parent (no search)
        if (node instanceof IASTPreprocessorStatement) {
            parent = root;  
        } else {
            IASTNode tempParent = node.getParent();
            if (tempParent instanceof IASTPreprocessorStatement) {
                parent = root.findTreeParentForMergedNode(node);
            } else {
                parent = root.findTreeParentForNode(node);              
            }
        }
        
        if (parent == null)
            parent = root;
        
        return createNode(parent, node);
	}
    
    private DOMASTNodeLeaf createNode(DOMASTNodeParent parent, IASTNode node) {
        DOMASTNodeParent tree = new DOMASTNodeParent(node);
        parent.addChild(tree);
        
        // set filter flags
        if (node instanceof IASTProblemHolder || node instanceof IASTProblem) { 
            tree.setFiltersFlag(DOMASTNodeLeaf.FLAG_PROBLEM);
            
            if (node instanceof IASTProblemHolder)
                astProblems = ArrayUtil.append(IASTProblem.class, astProblems, ((IASTProblemHolder) node).getProblem());
            else
                astProblems = ArrayUtil.append(IASTProblem.class, astProblems, (IASTProblem) node);
        }
        if (node instanceof IASTPreprocessorStatement)
            tree.setFiltersFlag(DOMASTNodeLeaf.FLAG_PREPROCESSOR);
        if (node instanceof IASTPreprocessorIncludeStatement)
            tree.setFiltersFlag(DOMASTNodeLeaf.FLAG_INCLUDE_STATEMENTS);
		
		return tree;
    }
	
	@Override
	public int genericVisit(IASTNode declaration) {
		if (addRoot(declaration) == null)
			return PROCESS_ABORT;

		return PROCESS_CONTINUE;
	}
	
	@Override
	public int visit(IASTName name) {
		if (name.toString() != null) {
			return genericVisit(name);
		}
		return PROCESS_CONTINUE;
	}
	

	private DOMASTNodeLeaf mergeNode(ASTNode node) {
		DOMASTNodeLeaf leaf = addRoot(node);
		
		if (node instanceof IASTPreprocessorMacroDefinition)
			addRoot(((IASTPreprocessorMacroDefinition)node).getName());
		
		return leaf;
	}
	
	@Override
	public DOMASTNodeLeaf[] mergePreprocessorStatements(IASTPreprocessorStatement[] statements) {
		DOMASTNodeLeaf[] leaves = new DOMASTNodeLeaf[statements.length];
		for(int i=0; i<statements.length; i++) {
			if (monitor != null && monitor.isCanceled()) return leaves;
			
			if (statements[i] instanceof ASTNode)
				leaves[i] = mergeNode((ASTNode)statements[i]);
		}
		
		return leaves;
	}
	
	@Override
	public void mergePreprocessorProblems(IASTProblem[] problems) {
		for (IASTProblem problem : problems) {
			if (monitor != null && monitor.isCanceled()) return;
			
			if (problem instanceof ASTNode)
			   mergeNode((ASTNode)problem);
		}
	}
	
	@Override
	public DOMASTNodeParent getTree() {
		return root;
	}
	
	@Override
	public void groupIncludes(DOMASTNodeLeaf[] treeIncludes) {
		// loop through the includes and make sure that all of the nodes 
		// that are children of the TU are in the proper include (based on offset)
		for (int i=treeIncludes.length - 1; i >= 0; i-- ) {
			final DOMASTNodeLeaf nodeLeaf = treeIncludes[i];
			if (nodeLeaf == null || !(nodeLeaf.getNode() instanceof IASTPreprocessorIncludeStatement)) continue;

			final String path= ((IASTPreprocessorIncludeStatement) nodeLeaf.getNode()).getPath();
			final DOMASTNodeLeaf[] children = root.getChildren(false);
			for (final DOMASTNodeLeaf child : children) {
				if (child != null && child != nodeLeaf && 
						child.getNode().getContainingFilename().equals(path)) {
					root.removeChild(child);
					((DOMASTNodeParent)nodeLeaf).addChild(child);
				}
			}
		}
	}
	
	public IASTProblem[] getASTProblems() {
		return astProblems;
	}
}
