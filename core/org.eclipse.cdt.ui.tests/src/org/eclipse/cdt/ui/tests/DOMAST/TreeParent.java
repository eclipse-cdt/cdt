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
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.parser.scanner2.LocationMap;

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
		int index = 0;
		for(int i=0; i<children.size(); i++) {
			TreeObject treeObj = (TreeObject)children.get(i);
			int treeObjOffset = 0;
			int childObjOffset = 0;
			if( treeObj.getNode() instanceof ASTNode )
				treeObjOffset = ((ASTNode)treeObj.getNode()).getOffset();
			else if( treeObj.getNode() instanceof LocationMap.ScannerASTNode )
				treeObjOffset = ((LocationMap.ScannerASTNode)child.getNode()).getOffset();
			
			if( child.getNode() instanceof ASTNode )
				childObjOffset = ((ASTNode)child.getNode()).getOffset();
			else if( child.getNode() instanceof LocationMap.ScannerASTNode )
				childObjOffset = ((LocationMap.ScannerASTNode)child.getNode()).getOffset();
			
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