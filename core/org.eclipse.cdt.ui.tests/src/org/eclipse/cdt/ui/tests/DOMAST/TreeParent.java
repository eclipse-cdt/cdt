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
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
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
	private TreeParent findTreeObject(TreeObject[] trees, IASTNode node, boolean useOffset, boolean useName) {
		for (int i=0; i<trees.length; i++) {
			
			if (trees[i] != null && trees[i] instanceof TreeParent) {
				if ( equalNodes( ((TreeParent)trees[i]).getNode(), node, useOffset, useName ) ) {
					return (TreeParent)trees[i];
				} else if ( ((TreeParent)trees[i]).hasChildren() ){
					TreeParent tree = findTreeObject( ((TreeParent)trees[i]).getChildren(), node, useOffset, useName );
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
	public TreeParent findTreeObject(IASTNode node, boolean useOffset) {
		return findTreeObject(node, useOffset, false);
	}
	
	/**
	 * Returns the TreeParent that corresponds to the IASTNode.  This is the TreeParent
	 * that represents the IASTNode in the DOM AST View.
	 * 
	 * @param node
	 * @return
	 */
	public TreeParent findTreeObject(IASTNode node, boolean useOffset, boolean useName) {
		if (node == null) return null;
		
		Iterator itr = children.iterator();
		while (itr.hasNext()) {
			Object o = itr.next();
			if (o != null && o instanceof TreeParent) {
				IASTNode treeNode = ((TreeParent)o).getNode(); 
				
				if (equalNodes(node, ((TreeParent)o).getNode(), useOffset, useName))
					return (TreeParent)o;
				
				// search the children
				if ( ((TreeParent)o).hasChildren() ){
					TreeParent tree = findTreeObject( ((TreeParent)o).getChildren(), node, useOffset, useName );
					if (tree != null) return tree;
				}
			}
		}
		
		return null; // nothing found
	}
		
	private boolean equalNodes(IASTNode node1, IASTNode node2, boolean useOffset, boolean useName) {
		if (useName && 
				(!(node1 instanceof IASTName) || !(node2 instanceof IASTName) ||
			!(((IASTName)node1).toString().equals(((IASTName)node2).toString())))) return false;
			
		if (useOffset) {
			if (node1 instanceof ASTNode && node2 instanceof ASTNode) {
				if (((ASTNode)node1).getOffset() == ((ASTNode)node2).getOffset() &&
						((ASTNode)node1).getLength() == ((ASTNode)node2).getLength())
					return true;
			} else {
				IASTNodeLocation[] locs1 = node1.getNodeLocations();
				IASTNodeLocation[] locs2 = node2.getNodeLocations();
				for(int i=0; i<locs1.length && i<locs2.length; i++) {
					if (locs1[i].getNodeOffset() != locs2[i].getNodeOffset() ||
							locs1[i].getNodeLength() != locs2[i].getNodeLength())
						return false;
				}
				
				return true;
			}
		} else {
			if ( node1 == node2 ) 
				return true;
		}
		
		return false;
	}
	
}