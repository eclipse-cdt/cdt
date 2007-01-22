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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.model.ICElement;

class THGraphNode {
	private static final Object[] NO_MEMBERS= new Object[0];
	private List fOutgoing= Collections.EMPTY_LIST;
	private List fIncoming= Collections.EMPTY_LIST;
	private ICElement fElement;
	private Object[] fMembers= NO_MEMBERS;
	
	THGraphNode(ICElement element) {
		fElement= element;
	}
	
	void startEdge(THGraphEdge outgoing) {
		fOutgoing= addElement(fOutgoing, outgoing);
	}

	void endEdge(THGraphEdge incoming) {
		fIncoming= addElement(fIncoming, incoming);
	}
	
	ICElement getElement() {
		return fElement;
	}

	private List addElement(List list, Object elem) {
		switch (list.size()) {
		case 0:
			return Collections.singletonList(elem);
		case 1:
			list= new ArrayList(list);
			list.add(elem);
			return list;
		}
		list.add(elem);
		return list;
	}

	List getOutgoing() {
		return fOutgoing;
	}
	
	List getIncoming() {
		return fIncoming;
	}

	public void setMembers(Object[] array) {
		fMembers= array;
	}
	
	public Object[] getMembers(boolean addInherited) {
		if (!addInherited) {
			return fMembers;
		}
		ArrayList list= new ArrayList();
		collectMembers(new HashSet(), list);
		return list.toArray();
	}

	private void collectMembers(HashSet visited, List list) {
		if (visited.add(this)) {
			list.addAll(Arrays.asList(fMembers));
			List bases= getOutgoing();
			for (Iterator iterator = bases.iterator(); iterator.hasNext();) {
				THGraphEdge edge = (THGraphEdge) iterator.next();
				edge.getEndNode().collectMembers(visited, list);
			}
		}
	}
}
