/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.typehierarchy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.model.ICElement;

public class THGraph {
	private HashSet fRootNodes= new HashSet();
	private HashSet fLeaveNodes= new HashSet();
	private HashMap fNodes= new HashMap();
	
	public void clear() {
		fRootNodes.clear();
		fLeaveNodes.clear();
		fNodes.clear();
	}

	public THGraphNode getNode(ICElement elem) {
		return (THGraphNode) fNodes.get(elem);
	}

	public THGraphNode addNode(ICElement elem) {
		THGraphNode node= (THGraphNode) fNodes.get(elem); 

		if (node == null) {
			node= new THGraphNode(elem);
			fNodes.put(elem, node);
			fRootNodes.add(node);
			fLeaveNodes.add(node);
		}
		return node;
	}
	
	public THGraphEdge addEdge(THGraphNode from, THGraphNode to) {
		if (createsLoop(from, to)) {
			return null;
		}
		THGraphEdge edge= new THGraphEdge(from, to);
		from.startEdge(edge);
		to.endEdge(edge);
		fRootNodes.remove(to);
		fLeaveNodes.remove(from);
		return edge;
	}

	private boolean createsLoop(THGraphNode from, THGraphNode to) {
		if (to.getOutgoing().isEmpty() || from.getIncoming().isEmpty()) {
			return false;
		}
		
		HashSet checked= new HashSet();
		ArrayList stack= new ArrayList();
		stack.add(to);
		
		while (!stack.isEmpty()) {
			THGraphNode node= (THGraphNode) stack.remove(stack.size()-1);
			List out= node.getOutgoing();
			for (Iterator iterator = out.iterator(); iterator.hasNext();) {
				THGraphEdge	edge= (THGraphEdge) iterator.next();
				node= edge.getEndNode();
				if (node == from) {
					return true;
				}
				if (checked.add(node)) {
					stack.add(node);
				}
			}
		}
		return false;
	}

	public Collection getRootNodes() {
		return fRootNodes;
	}

	public Collection getLeaveNodes() {
		return fLeaveNodes;
	}
}
