/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.ui.typehierarchy;

class THGraphEdge {
	private THGraphNode fFrom;
	private THGraphNode fTo;
	
	THGraphEdge(THGraphNode from, THGraphNode to) {
		fFrom= from;
		fTo= to;
	}
	
	THGraphNode getStartNode() {
		return fFrom;
	}
	
	THGraphNode getEndNode() {
		return fTo;
	}
}
