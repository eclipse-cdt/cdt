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

import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * @author dsteffle
 */
public class TreeParent extends TreeObject {
	private ArrayList children; // TODO might want to use a HashTable/HashMap or wrap one so finding the parent node is faster

	public TreeParent(IASTNode node) {
		super(node);
		children = new ArrayList();
	}
	public void addChild(TreeObject child) {
		children.add(child);
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

	private TreeParent findParentOfNode(TreeObject[] trees, IASTNode node) {
		for (int i=0; i<trees.length; i++) {
			
			if (trees[i] != null && trees[i] instanceof TreeParent) {
				if ( ((TreeParent)trees[i]).getNode() == node.getParent() ) {
					return (TreeParent)trees[i];
				} else if ( ((TreeParent)trees[i]).hasChildren() ){
					TreeParent tree = findParentOfNode( ((TreeParent)trees[i]).getChildren(), node );
					if (tree != null) return tree;
				}
			}
		}
		
		return null; // nothing found
	}
	
	public TreeParent findParentOfNode(IASTNode node) {
		if (node == null || node.getParent() == null) return null;
		
		Iterator itr = children.iterator();
		while (itr.hasNext()) {
			Object o = itr.next();
			if (o != null && o instanceof TreeParent) {
				if ( ((TreeParent)o).getNode() == node.getParent() ) {
					return (TreeParent)o;
				} else if ( ((TreeParent)o).hasChildren() ){
					TreeParent tree = findParentOfNode( ((TreeParent)o).getChildren(), node );
					if (tree != null) return tree;
				}
			}
		}
		
		return null; // nothing found
	}
	

}