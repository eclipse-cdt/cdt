/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation 
 *******************************************************************************/
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
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.parser.scanner2.LocationMap.ASTInclusionStatement;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author dsteffle
 */
public class CPopulateASTViewAction extends CASTVisitor implements IPopulateDOMASTAction {
	private static final int INITIAL_PROBLEM_SIZE = 4;
	{
		shouldVisitNames          = true;
		shouldVisitDeclarations   = true;
		shouldVisitInitializers   = true;
		shouldVisitParameterDeclarations = true;
		shouldVisitDeclarators    = true;
		shouldVisitDeclSpecifiers = true;
		shouldVisitDesignators 	  = true;
		shouldVisitExpressions    = true;
		shouldVisitStatements     = true;
		shouldVisitTypeIds        = true;
		shouldVisitEnumerators    = true;
	}

	DOMASTNodeParent root = null;
	IProgressMonitor monitor = null;
	IASTProblem[] astProblems = new IASTProblem[INITIAL_PROBLEM_SIZE];
	
	public CPopulateASTViewAction(IASTTranslationUnit tu, IProgressMonitor monitor) {
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
        if (node instanceof ASTNode && ((ASTNode)node).getLength() <= 0)
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
                astProblems = (IASTProblem[])ArrayUtil.append(IASTProblem.class, astProblems, ((IASTProblemHolder)node).getProblem());
            else
                astProblems = (IASTProblem[])ArrayUtil.append(IASTProblem.class, astProblems, node);
        }
        if (node instanceof IASTPreprocessorStatement)
            tree.setFiltersFlag(DOMASTNodeLeaf.FLAG_PREPROCESSOR);
        if (node instanceof IASTPreprocessorIncludeStatement)
            tree.setFiltersFlag(DOMASTNodeLeaf.FLAG_INCLUDE_STATEMENTS);
		
		return tree;
    }
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction#processDeclaration(org.eclipse.cdt.core.dom.ast.IASTDeclaration)
	 */
	public int visit(IASTDeclaration declaration) {
		DOMASTNodeLeaf temp = addRoot(declaration);
		if (temp == null)
			return PROCESS_ABORT;
		else if (temp instanceof DOMASTNodeLeafContinue)
			return PROCESS_CONTINUE;
		else
			return PROCESS_CONTINUE;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction#processDeclarator(org.eclipse.cdt.core.dom.ast.IASTDeclarator)
	 */
	public int visit(IASTDeclarator declarator) {
		DOMASTNodeLeaf temp = addRoot(declarator);
		
		IASTPointerOperator[] ops = declarator.getPointerOperators();
		for(int i=0; i<ops.length; i++)
			addRoot(ops[i]);
		
		if (declarator instanceof IASTArrayDeclarator) {
			IASTArrayModifier[] mods = ((IASTArrayDeclarator)declarator).getArrayModifiers();
			for(int i=0; i<mods.length; i++)
				addRoot(mods[i]);	
		}
		
		if (temp == null)
			return PROCESS_ABORT;
		else if (temp instanceof DOMASTNodeLeafContinue)
			return PROCESS_CONTINUE;
		else
			return PROCESS_CONTINUE;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processDesignator(org.eclipse.cdt.core.dom.ast.c.ICASTDesignator)
	 */
	public int visit(ICASTDesignator designator) {
		DOMASTNodeLeaf temp = addRoot(designator);
		if (temp == null)
			return PROCESS_ABORT;
		else if (temp instanceof DOMASTNodeLeafContinue)
			return PROCESS_CONTINUE;
		else
			return PROCESS_CONTINUE;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processDeclSpecifier(org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier)
	 */
	public int visit(IASTDeclSpecifier declSpec) {
		DOMASTNodeLeaf temp = addRoot(declSpec);
		if (temp == null)
			return PROCESS_ABORT;
		else if (temp instanceof DOMASTNodeLeafContinue)
			return PROCESS_CONTINUE;
		else
			return PROCESS_CONTINUE;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processEnumerator(org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator)
	 */
	public int visit(IASTEnumerator enumerator) {
		DOMASTNodeLeaf temp = addRoot(enumerator);
		if (temp == null)
			return PROCESS_ABORT;
		else if (temp instanceof DOMASTNodeLeafContinue)
			return PROCESS_CONTINUE;
		else
			return PROCESS_CONTINUE;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
	 */
	public int visit(IASTExpression expression) {
		DOMASTNodeLeaf temp = addRoot(expression);
		if (temp == null)
			return PROCESS_ABORT;
		else if (temp instanceof DOMASTNodeLeafContinue)
			return PROCESS_CONTINUE;
		else
			return PROCESS_CONTINUE;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processInitializer(org.eclipse.cdt.core.dom.ast.IASTInitializer)
	 */
	public int visit(IASTInitializer initializer) {
		DOMASTNodeLeaf temp = addRoot(initializer);
		if (temp == null)
			return PROCESS_ABORT;
		else if (temp instanceof DOMASTNodeLeafContinue)
			return PROCESS_CONTINUE;
		else
			return PROCESS_CONTINUE;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processName(org.eclipse.cdt.core.dom.ast.IASTName)
	 */
	public int visit(IASTName name) {
		DOMASTNodeLeaf temp = null;
		if ( name.toString() != null )
			temp = addRoot(name);
		else
			return PROCESS_CONTINUE;
		if (temp == null)
			return PROCESS_ABORT;
		else if (temp instanceof DOMASTNodeLeafContinue)
			return PROCESS_CONTINUE;
		else
			return PROCESS_CONTINUE;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processParameterDeclaration(org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration)
	 */
	public int visit(
			IASTParameterDeclaration parameterDeclaration) {
		DOMASTNodeLeaf temp = addRoot(parameterDeclaration);
		if (temp == null)
			return PROCESS_ABORT;
		else if (temp instanceof DOMASTNodeLeafContinue)
			return PROCESS_CONTINUE;
		else
			return PROCESS_CONTINUE;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processStatement(org.eclipse.cdt.core.dom.ast.IASTStatement)
	 */
	public int visit(IASTStatement statement) {
		DOMASTNodeLeaf temp = addRoot(statement);
		if (temp == null)
			return PROCESS_ABORT;
		else if (temp instanceof DOMASTNodeLeafContinue)
			return PROCESS_CONTINUE;
		else
			return PROCESS_CONTINUE;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processTypeId(org.eclipse.cdt.core.dom.ast.IASTTypeId)
	 */
	public int visit(IASTTypeId typeId) {
		DOMASTNodeLeaf temp = addRoot(typeId);
		if (temp == null)
			return PROCESS_ABORT;
		else if (temp instanceof DOMASTNodeLeafContinue)
			return PROCESS_CONTINUE;
		else
			return PROCESS_CONTINUE;
	}
	
	private DOMASTNodeLeaf mergeNode(ASTNode node) {
		DOMASTNodeLeaf temp = addRoot(node);
		
		if (node instanceof IASTPreprocessorMacroDefinition )
			addRoot(((IASTPreprocessorMacroDefinition)node).getName());
		
		return temp;
	}
	
	public DOMASTNodeLeaf[] mergePreprocessorStatements(IASTPreprocessorStatement[] statements) {
		DOMASTNodeLeaf[] leaves = new DOMASTNodeLeaf[statements.length];
		for(int i=0; i<statements.length; i++) {
			if (monitor != null && monitor.isCanceled()) return leaves;
			
			if (statements[i] instanceof ASTNode)
				leaves[i] = mergeNode((ASTNode)statements[i]);
		}
		
		return leaves;
	}
	
	public void mergePreprocessorProblems(IASTProblem[] problems) {
		for(int i=0; i<problems.length; i++) {
			if (monitor != null && monitor.isCanceled()) return;
			
			if (problems[i] instanceof ASTNode)
			   mergeNode((ASTNode)problems[i]);
		}
	}
	
	public DOMASTNodeParent getTree() {
		return root;
	}
	
	public void groupIncludes(DOMASTNodeLeaf[] treeIncludes) {
		// loop through the includes and make sure that all of the nodes 
		// that are children of the TU are in the proper include (based on offset)
		DOMASTNodeLeaf child = null;
		outerLoop: for (int i=treeIncludes.length-1; i>=0; i--) {
			if (treeIncludes[i] == null) continue;

			IASTNode node = null;
			for(int j=0; j < root.getChildren(false).length; j++) {
//				if (monitor != null && monitor.isCanceled()) return; // this causes a deadlock when checked here
				child = root.getChildren(false)[j];
				
				node = treeIncludes[i].getNode();
				if (child != null && 
						treeIncludes[i] != child &&
						node instanceof ASTInclusionStatement &&
						((ASTNode)child.getNode()).getOffset() >= ((ASTInclusionStatement)node).startOffset &&
						((ASTNode)child.getNode()).getOffset() <= ((ASTInclusionStatement)node).endOffset) {
					root.removeChild(child);
					((DOMASTNodeParent)treeIncludes[i]).addChild(child);
				}
			}
		}
	}
	
	public IASTProblem[] getASTProblems() {
		return astProblems;
	}
}
