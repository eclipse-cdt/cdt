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

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * @author dsteffle
 */
public class TreeParent extends TreeObject {
	private static final TreeObject[] EMPTY_CHILDREN_ARRAY = new TreeObject[0];
	private static final int DEFAULT_NODE_CHAIN_SIZE = 4;
	private static final int DEFAULT_CHILDREN_SIZE = 4;
	int index=0;
	private TreeObject[] children;
	boolean cleanupedElements = false;
	
	public int getStartSearch() {
		return index;
	}

	public TreeParent() {
		super(null);
		children = EMPTY_CHILDREN_ARRAY;
	}
	
	public TreeParent(IASTNode node) {
		super(node);
		children = new TreeObject[DEFAULT_CHILDREN_SIZE];
	}
	public void addChild(TreeObject child) {
		if (index==children.length) {
			children = (TreeObject[])ArrayUtil.append(TreeObject.class, children, child);
			index++;
		} else {
			children[index++] = child;
		}
		
		child.setParent(this);
	}
	public void removeChild(TreeObject child) {
		for(int i=0; i<children.length; i++) {
			if (children[i] == child) {
				children[i] = null; 
				break;
			}
		}
		child.setParent(null);
	}
	
	public TreeObject[] getChildren(boolean cleanupElements) {
		if (cleanupElements) {
			return getChildren();
		} else {
			return children;
		}
	}
	
	public TreeObject [] getChildren() {
		// remove null children from the array (if not already done so)
		if (!cleanupedElements) {
			// remove null elements
			children = (TreeObject[])ArrayUtil.removeNulls(TreeObject.class, children);
			
			// sort the elements
			Arrays.sort(children, new Comparator() {
	            public int compare(Object a, Object b) {
	                if(a instanceof TreeObject && b instanceof TreeObject &&
							((TreeObject)a).getNode() instanceof ASTNode &&
							((TreeObject)b).getNode() instanceof ASTNode) {
						return ((ASTNode)((TreeObject)a).getNode()).getOffset() - ((ASTNode)((TreeObject)b).getNode()).getOffset();
	                }
					
					return 0;
	            }
	        });
			
			// need to also clean up the children's children, to make sure all nulls are removed (prevent expansion sign when there isn't one)
			for(int i=0; i<children.length; i++) {
				if (children[i] instanceof TreeParent) {
					((TreeParent)children[i]).setChildren((TreeObject[])ArrayUtil.removeNulls(TreeObject.class, ((TreeParent)children[i]).getChildren()));
				}
			}
			
			cleanupedElements = true;
		}
		
		return children;
	}
	public boolean hasChildren() {
		return children.length>0;
	}

	/**
	 * Returns the TreeParent whose IASTNode is the parent of the IASTNode.
	 * 
	 * @param node
	 * @return
	 */
	public TreeParent findTreeParentForNode(IASTNode node) {
		if (node == null || node.getParent() == null) return null;
		
		IASTNode parentToFind = node.getParent();
		
		// first check this node before checking children
		if (this.getNode() == parentToFind) {
			return this;
		}
		
		// build the chain of nodes... and use it to search the tree for the TreeParent that owns the node's parent
		IASTNode[] nodeChain = new IASTNode[DEFAULT_NODE_CHAIN_SIZE];
		IASTNode topNode = node.getParent();
		ArrayUtil.append(IASTNode.class, nodeChain, node);
		nodeChain = (IASTNode[])ArrayUtil.append(IASTNode.class, nodeChain, topNode);
		while(topNode.getParent() != null && !(topNode.getParent() instanceof IASTTranslationUnit)) {
			topNode = topNode.getParent();
			nodeChain = (IASTNode[])ArrayUtil.append(IASTNode.class, nodeChain, topNode);
		}
		
		// loop through the chain of nodes and use it to only search the necessary children required to find the node
		TreeObject[] childrenToSearch = children;
		int j=getStartSearch();
		outerLoop: for(int i=nodeChain.length-1; i>=0; i--) {
			if (nodeChain[i] != null) {
				parentToFind = nodeChain[i];
				
				for(; j>=0; j--) { // use the TreeParent's index to start searching at the end of it's children (performance optimization)
					if (j<childrenToSearch.length && childrenToSearch[j] instanceof TreeParent) {
						if ( childrenToSearch[j].getNode() == node.getParent() ) {
							return (TreeParent)childrenToSearch[j];
						}
												
						if (childrenToSearch[j].getNode() == parentToFind) {
							int pos = j;
							j = ((TreeParent)childrenToSearch[pos]).getStartSearch();
							childrenToSearch = ((TreeParent)childrenToSearch[pos]).getChildren(false);
							continue outerLoop;
						}
					}
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
	 * @param node
	 * @return
	 */
	public TreeParent findTreeObject(IASTNode node, boolean useOffset) {
		if (node == null) return null;
		
		IASTNode nodeToFind = node;
		
		// first check this node before checking children
		if (equalNodes(node, this.getNode(), useOffset)) {
			return this;
		}
		
		// build the chain of nodes... and use it to search the tree for the TreeParent that contains the node
		IASTNode[] nodeChain = new IASTNode[DEFAULT_NODE_CHAIN_SIZE];
		IASTNode topNode = node;
		nodeChain = (IASTNode[])ArrayUtil.append(IASTNode.class, nodeChain, topNode);
		while(topNode.getParent() != null && !(topNode.getParent() instanceof IASTTranslationUnit)) {
			topNode = topNode.getParent();
			nodeChain = (IASTNode[])ArrayUtil.append(IASTNode.class, nodeChain, topNode);
		}
		
		// loop through the chain of nodes and use it to only search the necessary children required to find the node
		TreeObject[] childrenToSearch = children;
		outerLoop: for(int i=nodeChain.length-1; i>=0; i--) {
			if (nodeChain[i] != null) {
				nodeToFind = nodeChain[i];
				
				for(int j=0; j<childrenToSearch.length; j++) {
					if (childrenToSearch[j] instanceof TreeParent) {
						
						if ( equalNodes(childrenToSearch[j].getNode(), node, useOffset) ) { 
							return (TreeParent)childrenToSearch[j];
						}						
						
						if ( equalNodes(childrenToSearch[j].getNode(), nodeToFind, useOffset) ) {
							childrenToSearch = ((TreeParent)childrenToSearch[j]).getChildren(false);
							continue outerLoop;
						}
						
						// since the nodeChain doesn't include #includes, if an #include is encountered then search it's children
						if (childrenToSearch[j].getNode() instanceof IASTPreprocessorIncludeStatement) {
							TreeParent foundParentInInclude = ((TreeParent)childrenToSearch[j]).findTreeObject(node, useOffset);
							if(foundParentInInclude != null) {
								return foundParentInInclude;
							}
						}
					}
				}
			}
		}
		
		return null; // nothing found
	}
	
	private boolean equalNodes(IASTNode node1, IASTNode node2, boolean useOffset) {
		if (useOffset) {
			if (node1 instanceof ASTNode && node2 instanceof ASTNode) {
				if (((ASTNode)node1).getOffset() == ((ASTNode)node2).getOffset() &&
						((ASTNode)node1).getLength() == ((ASTNode)node2).getLength()) {
					if (node1.getClass().equals(node2.getClass()))
						return true;
					else
						return false;
				}
			} else {
				IASTNodeLocation[] locs1 = node1.getNodeLocations();
				IASTNodeLocation[] locs2 = node2.getNodeLocations();
				for(int i=0; i<locs1.length && i<locs2.length; i++) {
					if (locs1[i].getNodeOffset() != locs2[i].getNodeOffset() ||
							locs1[i].getNodeLength() != locs2[i].getNodeLength())
						return false;
				}
				
				if (node1.getClass().equals(node2.getClass()))
					return true;
				else
					return false;
			}
		} else {
			if ( node1 == node2 ) 
				return true;
		}
		
		return false;
	}
	
	public void setChildren(TreeObject[] children) {
		this.children = children;
	}
	
}