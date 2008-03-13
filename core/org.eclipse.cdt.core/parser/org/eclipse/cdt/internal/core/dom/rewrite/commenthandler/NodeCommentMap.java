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
 * @author Guido Zgraggen IFS
 *
 */
public class NodeCommentMap {
	protected final HashMap<IASTNode, ArrayList<IASTComment>> leadingMap = new HashMap<IASTNode, ArrayList<IASTComment>>();
	protected final HashMap<IASTNode, ArrayList<IASTComment>> trailingMap = new HashMap<IASTNode, ArrayList<IASTComment>>();
	protected final HashMap<IASTNode, ArrayList<IASTComment>> freestandingMap = new HashMap<IASTNode, ArrayList<IASTComment>>();
	
	public void addTrailingCommentToNode(IASTNode node, IASTComment comment){
		ArrayList<IASTComment> comments = trailingMap.get(node);
		if(comments == null){
			comments = new ArrayList<IASTComment>();
		}
		comments.add(comment);
		trailingMap.put(node, comments);
	}
	
	public ArrayList<IASTComment> getTrailingCommentsForNode(IASTNode node){
		if(trailingMap.get(node) == null) {
			return new ArrayList<IASTComment>();
		}
		return trailingMap.get(node);
	}
	
	public void addLeadingCommentToNode(IASTNode node, IASTComment comment){
		ArrayList<IASTComment> comments = leadingMap.get(node);
		if(comments == null){
			comments = new ArrayList<IASTComment>();
		}
		comments.add(comment);
		leadingMap.put(node, comments);
	}
	
	public ArrayList<IASTComment> getLeadingCommentsForNode(IASTNode node){
		if(leadingMap.get(node) == null) {
			return new ArrayList<IASTComment>();
		}
		return leadingMap.get(node);
	}
	
	public void addFreestandingCommentToNode(IASTNode node, IASTComment comment){
		ArrayList<IASTComment> comments = freestandingMap.get(node);
		if(comments == null){
			comments = new ArrayList<IASTComment>();
		}
		comments.add(comment);
		freestandingMap.put(node, comments);
	}
	
	public ArrayList<IASTComment> getFreestandingCommentsForNode(IASTNode node){
		if(freestandingMap.get(node) == null) {
			return new ArrayList<IASTComment>();
		}
		return freestandingMap.get(node);
	}
	
	public HashMap<IASTNode, ArrayList<IASTComment>> getLeadingMap() {
		return leadingMap;
	}
	public HashMap<IASTNode, ArrayList<IASTComment>> getTrailingMap() {
		return trailingMap;
	}
	public HashMap<IASTNode, ArrayList<IASTComment>> getFreestandingMap() {
		return freestandingMap;
	}
	
	
}
