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

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * @author dsteffle
 */
public class TreeParent extends TreeObject {
	private ArrayList children;

	public TreeParent(IASTNode node) {
		super(node);
		children = new ArrayList();
	}
	public void addChild(TreeObject child) {
		int index = 0;
		for(int i=0; i<children.size(); i++) {
			TreeObject treeObj = (TreeObject)children.get(i);
			int treeObjOffset = 0;
			int childObjOffset = 0;
			if( treeObj.getNode() instanceof ASTNode )
				treeObjOffset = ((ASTNode)treeObj.getNode()).getOffset();			
			
			if( child.getNode() instanceof ASTNode )
				childObjOffset = ((ASTNode)child.getNode()).getOffset();
			
			if ( treeObjOffset < childObjOffset ){ 
				index = i+1;
			} else {
				break;
			}
		}
		
		children.add(index, child);
					
		child.setParent(this);
	}
	public void removeChild(TreeObject child) {
		children.remove(child);
		child.setParent(null);
	}
	public TreeObject [] getChildren() {
		return (TreeObject [])children.toArray(new TreeObject[children.size()]);
	}
	public boolean hasChildren() {
		return children.size()>0;
	}

	/**
	 * Returns the TreeParent whose IASTNode is the parent of the IASTNode.
	 * 
	 * @param trees
	 * @param node
	 * @return
	 */
	private TreeParent findTreeParentForNode(TreeObject[] trees, IASTNode node) {
		for (int i=0; i<trees.length; i++) {
			
			if (trees[i] != null && trees[i] instanceof TreeParent) {
				if ( ((TreeParent)trees[i]).getNode() == node.getParent() ) {
					return (TreeParent)trees[i];
				} else if ( ((TreeParent)trees[i]).hasChildren() ){
					TreeParent tree = findTreeParentForNode( ((TreeParent)trees[i]).getChildren(), node );
					if (tree != null) return tree;
				}
			}
		}
		
		return null; // nothing found
	}
	
	/**
	 * Returns the TreeParent whose IASTNode is the parent of the IASTNode.
	 * 
	 * @param node
	 * @return
	 */
	public TreeParent findTreeParentForNode(IASTNode node) {
		if (node == null || node.getParent() == null) return null;
		
		Iterator itr = children.iterator();
		while (itr.hasNext()) {
			Object o = itr.next();
			if (o != null && o instanceof TreeParent) {
				if ( ((TreeParent)o).getNode() == node.getParent() ) {
					return (TreeParent)o;
				} else if ( ((TreeParent)o).hasChildren() ){
					TreeParent tree = findTreeParentForNode( ((TreeParent)o).getChildren(), node );
					if (tree != null) return tree;
				}
			}
		}
		
		// try finding the best parent possible
		IASTNode parent = node.getParent();
		TreeParent tree = null;
		while (parent != null && tree == null) {
			tree = findTreeParentForNode(parent);
			if (tree != null) return tree;
			
			parent = parent.getParent();
		}
		
		return null; // nothing found
	}

	/**
	 * Returns the TreeParent that corresponds to the IASTNode.  This is the TreeParent
	 * that represents the IASTNode in the DOM AST View.
	 * 
	 * @param trees
	 * @param node
	 * @return
	 */
	private TreeParent findTreeObject(TreeObject[] trees, IASTNode node) {
		for (int i=0; i<trees.length; i++) {
			
			if (trees[i] != null && trees[i] instanceof TreeParent) {
				if ( ((TreeParent)trees[i]).getNode() == node ) {
					return (TreeParent)trees[i];
				} else if ( ((TreeParent)trees[i]).hasChildren() ){
					TreeParent tree = findTreeObject( ((TreeParent)trees[i]).getChildren(), node );
					if (tree != null) return tree;
				}
			}
		}
		
		return null; // nothing found
	}
	
	/**
	 * Returns the TreeParent that corresponds to the IASTNode.  This is the TreeParent
	 * that represents the IASTNode in the DOM AST View.
	 * 
	 * @param node
	 * @return
	 */
	public TreeParent findTreeObject(IASTNode node) {
		if (node == null) return null;
		
		Iterator itr = children.iterator();
		while (itr.hasNext()) {
			Object o = itr.next();
			if (o != null && o instanceof TreeParent) {
				if ( ((TreeParent)o).getNode() == node ) {
					return (TreeParent)o;
				} else if ( ((TreeParent)o).hasChildren() ){
					TreeParent tree = findTreeObject( ((TreeParent)o).getChildren(), node );
					if (tree != null) return tree;
				}
			}
		}
		
		return null; // nothing found
	}
	
	/**
	 * Returns the TreeParent that corresponds to the IASTName.  This is based on string and offset equality.
	 * 
	 * @param node
	 * @return
	 */
	public TreeParent findTreeObjectForIASTName(IASTName name) {
		if (name == null) return null;
		
		Iterator itr = children.iterator();
		while (itr.hasNext()) {
			Object o = itr.next();
			if (o != null && o instanceof TreeParent) {
				if (treeParentHasName((TreeParent)o, name)) return (TreeParent)o;
			
				if ( ((TreeParent)o).hasChildren() ){
					TreeParent tree = findTreeObjectForIASTName( ((TreeParent)o).getChildren(), name );
					if (tree != null) return tree;
				}
			}
		}
		
		return null; // nothing found
	}
	
	/**
	 * Returns the TreeParent that corresponds to the IASTName.  This is based on string and offset equality.
	 * 
	 * @param trees
	 * @param node
	 * @return
	 */
	private TreeParent findTreeObjectForIASTName(TreeObject[] trees, IASTName name) {
		for (int i=0; i<trees.length; i++) {
			
			if (trees[i] != null && trees[i] instanceof TreeParent) {
				if (treeParentHasName((TreeParent)trees[i], name)) return (TreeParent)trees[i];
				
				if ( ((TreeParent)trees[i]).hasChildren() ){
					TreeParent tree = findTreeObjectForIASTName( ((TreeParent)trees[i]).getChildren(), name );
					if (tree != null) return tree;
				}
			}
		}
		
		return null; // nothing found
	}

	private boolean treeParentHasName(TreeParent tp, IASTName name) {
		if ( tp.getNode() instanceof IASTName && 
				tp.getNode() instanceof ASTNode &&
				name instanceof ASTNode) {
			IASTName treeName = (IASTName)tp.getNode();
			ASTNode treeNode = (ASTNode)tp.getNode();
			if (treeName.toString().equals(name.toString()) && treeNode.getOffset() == ((ASTNode)name).getOffset() ) {
				return true;
			}
		}
		
		return false;
	}
	
}