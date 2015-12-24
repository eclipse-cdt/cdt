/*******************************************************************************
 * Copyright (c) 2008, 2014 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Institute for Software - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.commenthandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.rewrite.util.ASTNodes;

/**
 * The NodeCommentMap is the map where all the comments are assigned to a node. For better 
 * performance the comments are stored in three different maps which have the same name as 
 * the relative position of the comment.
 * 
 * @author Guido Zgraggen IFS
 */
public class NodeCommentMap {
	protected final Map<IASTNode, List<IASTComment>> leadingMap = new HashMap<IASTNode, List<IASTComment>>();
	protected final Map<IASTNode, List<IASTComment>> trailingMap = new HashMap<IASTNode, List<IASTComment>>();
	protected final Map<IASTNode, List<IASTComment>> freestandingMap = new HashMap<IASTNode, List<IASTComment>>();
	protected final List<IASTTranslationUnit> coveredUnits = new ArrayList<IASTTranslationUnit>();
	
	/**
	 * Add a comment to the map with the trailing comments.
	 * @param node The node is the key.
	 * @param comment The comment is the value
	 */
	public void addTrailingCommentToNode(IASTNode node, IASTComment comment) {
		List<IASTComment> comments = trailingMap.get(node);
		if (comments == null) {
			comments = new ArrayList<IASTComment>();
		}
		comments.add(comment);
		trailingMap.put(node, comments);
	}
	
	/**
	 * Returns a List for the given node. This List contains all the comments 
	 * which are assigned to this specific node. If no comments are available an empty
	 * List is returned.
	 * @param node The key to fetch the associated comments.
	 * @return List
	 */
	public List<IASTComment> getTrailingCommentsForNode(IASTNode node) {
		List<IASTComment> list = trailingMap.get(node);
		return list != null ? list : new ArrayList<IASTComment>();
	}
	
	/**
	 * Add a comment to the map with the leading comments.
	 * @param node The node is the key.
	 * @param comment The comment is the value
	 */
	public void addLeadingCommentToNode(IASTNode node, IASTComment comment) {
		List<IASTComment> comments = leadingMap.get(node);
		if (comments == null) {
			comments = new ArrayList<IASTComment>();
		}
		comments.add(comment);
		leadingMap.put(node, comments);
	}
	
	/**
	 * Returns a List for the given node. This List contains all the comments 
	 * which are assigned to this specific node. If no comments are available an empty
	 * List is returned.
	 * @param node The key to fetch the associated comments.
	 * @return List
	 */
	public List<IASTComment> getLeadingCommentsForNode(IASTNode node) {
		List<IASTComment> list = leadingMap.get(node);
		return list != null ? list : new ArrayList<IASTComment>();
	}
	
	/**
	 * Add a comment to the map with the freestanding comments.
	 * @param node The node is the key.
	 * @param comment The comment is the value
	 */
	public void addFreestandingCommentToNode(IASTNode node, IASTComment comment) {
		List<IASTComment> comments = freestandingMap.get(node);
		if (comments == null) {
			comments = new ArrayList<IASTComment>();
		}
		comments.add(comment);
		freestandingMap.put(node, comments);
	}
	
	/**
	 * Returns a List for the given node. This List contains all the comments 
	 * which are assigned to this specific node. If no comments are available an empty
	 * List is returned.
	 * @param node The key to fetch the associated comments.
	 * @return List
	 */
	public List<IASTComment> getFreestandingCommentsForNode(IASTNode node) {
		List<IASTComment> list = freestandingMap.get(node);
		return list != null ? list : new ArrayList<IASTComment>();
	}

	/**
	 * Returns the Map with all leading maps. Used only for test purpose
	 * @return Map of all leading comments
	 */
	public Map<IASTNode, List<IASTComment>> getLeadingMap() {
		return leadingMap;
	}

	/**
	 * Returns the Map with all trailing maps. Used only for test purpose
	 * @return Map of all trailing comments
	 */
	public Map<IASTNode, List<IASTComment>> getTrailingMap() {
		return trailingMap;
	}

	/**
	 * Returns the Map with all freestanding maps. Used only for test purpose
	 * @return Map of all freestanding comments
	 */
	public Map<IASTNode, List<IASTComment>> getFreestandingMap() {
		return freestandingMap;
	}

	/**
	 * Returns an List for the given node. This List contains all the comments 
	 * which are assigned to this specific node. If no comments are available an empty
	 * List is returned.
	 * @param node The key to fetch the associated comments.
	 * @return List
	 */
	public List<IASTComment> getAllCommentsForNode(IASTNode node) {
		List<IASTComment> comment = new ArrayList<IASTComment>();
		comment.addAll(getFreestandingCommentsForNode(node));
		comment.addAll(getLeadingCommentsForNode(node));
		comment.addAll(getTrailingCommentsForNode(node));
		return comment;
	}

	public int getOffsetIncludingComments(IASTNode node) {
		int offset = ASTNodes.offset(node);

		// TODO(sprigogin): Iterate backwards and stop at the first blank line.
		List<IASTComment> comments = leadingMap.get(node);
		if (comments != null && !comments.isEmpty()) {
			for (IASTComment comment : comments) {
				int commentOffset = ASTNodes.offset(comment);
				if (commentOffset < offset) {
					offset = commentOffset;
				}
			}
		}
		return offset;
	}

	public int getEndOffsetIncludingComments(IASTNode node) {
		int endOffset = 0;
		while (true) {
			IASTFileLocation fileLocation = node.getFileLocation();
			if (fileLocation != null)
				endOffset = Math.max(endOffset, fileLocation.getNodeOffset() + fileLocation.getNodeLength());
			List<IASTComment> comments = trailingMap.get(node);
			if (comments != null && !comments.isEmpty()) {
				for (IASTComment comment : comments) {
					int commentEndOffset = ASTNodes.endOffset(comment);
					if (commentEndOffset >= endOffset) {
						endOffset = commentEndOffset;
					}
				}
			}
			IASTNode[] children = node.getChildren();
			if (children.length == 0)
				break;
			node = children[children.length - 1];
		}
		return endOffset;
	}

	/**
	 * Makes this comment map aware that comments of the given {@code ast} are already contained in the map.
	 * This can be used to make sure no-one accidentally tries to re-add already contained comments.
	 */
	public void setASTCovered(IASTTranslationUnit ast) {
		coveredUnits.add(ast);
	}

	/**
	 * Checks whether comments of the {@code ast} are already present in the map.
	 */
	public boolean isASTCovered(IASTTranslationUnit ast) {
		return coveredUnits.contains(ast);
	}
}
