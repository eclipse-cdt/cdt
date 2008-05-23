/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Institute for Software - initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.commenthandler;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * The NodeCommentMap is the map where all the comments are assigned to a node. For better 
 * performance the comments are stored in three different maps which have the same name as 
 * the relative position of the comment.
 * 
 * @author Guido Zgraggen IFS
 */
public class NodeCommentMap {
	protected final HashMap<IASTNode, ArrayList<IASTComment>> leadingMap = new HashMap<IASTNode, ArrayList<IASTComment>>();
	protected final HashMap<IASTNode, ArrayList<IASTComment>> trailingMap = new HashMap<IASTNode, ArrayList<IASTComment>>();
	protected final HashMap<IASTNode, ArrayList<IASTComment>> freestandingMap = new HashMap<IASTNode, ArrayList<IASTComment>>();
	
	/**
	 * Add a comment to the map with the trailing comments.
	 * @param node The node is the key.
	 * @param comment The comment is the value
	 */
	public void addTrailingCommentToNode(IASTNode node, IASTComment comment){
		ArrayList<IASTComment> comments = trailingMap.get(node);
		if(comments == null){
			comments = new ArrayList<IASTComment>();
		}
		comments.add(comment);
		trailingMap.put(node, comments);
	}
	
	/**
	 * Returns an ArrayList for the given node. This ArrayList contains all the comments 
	 * which are assigned to this specific node. If no comments are available an empty
	 * ArrayList is returned.
	 * @param node The key to fetch the associated comments.
	 * @return ArrayList
	 */
	public ArrayList<IASTComment> getTrailingCommentsForNode(IASTNode node){
		if(trailingMap.get(node) == null) {
			return new ArrayList<IASTComment>();
		}
		return trailingMap.get(node);
	}
	
	/**
	 * Add a comment to the map with the leading comments.
	 * @param node The node is the key.
	 * @param comment The comment is the value
	 */
	public void addLeadingCommentToNode(IASTNode node, IASTComment comment){
		ArrayList<IASTComment> comments = leadingMap.get(node);
		if(comments == null){
			comments = new ArrayList<IASTComment>();
		}
		comments.add(comment);
		leadingMap.put(node, comments);
	}
	
	/**
	 * Returns an ArrayList for the given node. This ArrayList contains all the comments 
	 * which are assigned to this specific node. If no comments are available an empty
	 * ArrayList is returned.
	 * @param node The key to fetch the associated comments.
	 * @return ArrayList
	 */
	public ArrayList<IASTComment> getLeadingCommentsForNode(IASTNode node){
		if(leadingMap.get(node) == null) {
			return new ArrayList<IASTComment>();
		}
		return leadingMap.get(node);
	}
	
	/**
	 * Add a comment to the map with the freestanding comments.
	 * @param node The node is the key.
	 * @param comment The comment is the value
	 */
	public void addFreestandingCommentToNode(IASTNode node, IASTComment comment){
		ArrayList<IASTComment> comments = freestandingMap.get(node);
		if(comments == null){
			comments = new ArrayList<IASTComment>();
		}
		comments.add(comment);
		freestandingMap.put(node, comments);
	}
	
	/**
	 * Returns an ArrayList for the given node. This ArrayList contains all the comments 
	 * which are assigned to this specific node. If no comments are available an empty
	 * ArrayList is returned.
	 * @param node The key to fetch the associated comments.
	 * @return ArrayList
	 */
	public ArrayList<IASTComment> getFreestandingCommentsForNode(IASTNode node){
		if(freestandingMap.get(node) == null) {
			return new ArrayList<IASTComment>();
		}
		return freestandingMap.get(node);
	}
	
	
	/**
	 * Returns the HashMap with all leading maps. Used only for test purpose
	 * @return HashMap of all leading comments
	 */
	public HashMap<IASTNode, ArrayList<IASTComment>> getLeadingMap() {
		return leadingMap;
	}
	/**
	 * Returns the HashMap with all trailing maps. Used only for test purpose
	 * @return HashMap of all trailing comments
	 */
	public HashMap<IASTNode, ArrayList<IASTComment>> getTrailingMap() {
		return trailingMap;
	}
	/**
	 * Returns the HashMap with all freestanding maps. Used only for test purpose
	 * @return HashMap of all freestanding comments
	 */
	public HashMap<IASTNode, ArrayList<IASTComment>> getFreestandingMap() {
		return freestandingMap;
	}

	/**
	 * Returns an ArrayList for the given node. This ArrayList contains all the comments 
	 * which are assigned to this specific node. If no comments are available an empty
	 * ArrayList is returned.
	 * @param node The key to fetch the associated comments.
	 * @return ArrayList
	 */
	public ArrayList<IASTComment> getAllCommentsForNode(IASTNode node) {
		ArrayList<IASTComment> comment = new ArrayList<IASTComment>();
		comment.addAll(getFreestandingCommentsForNode(node));
		comment.addAll(getLeadingCommentsForNode(node));
		comment.addAll(getTrailingCommentsForNode(node));
		return comment;
	}
}
