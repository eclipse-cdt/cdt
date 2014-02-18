/*******************************************************************************
 * Copyright (c) 2013 Simon Taddiken
 * University of Bremen.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Simon Taddiken (University of Bremen)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.pullup.ui;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.cdt.internal.ui.refactoring.pullup.InheritanceLevel;

public class SubClassContentProvider implements ITreeContentProvider {

	private final Map<InheritanceLevel, List<SubClassTreeEntry>> tree;

	public SubClassContentProvider(Map<InheritanceLevel, List<SubClassTreeEntry>> tree) {
		this.tree = tree;
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getChildren(Object element) {
		if (element instanceof InheritanceLevel) {
			final List<SubClassTreeEntry> children = this.tree.get(element);
			if (children != null) {
				return children.toArray();
			}
		}
		return new SubClassTreeEntry[0];
	}

	@Override
	public Object[] getElements(Object element) {
		return (InheritanceLevel[]) element;
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof SubClassTreeEntry) {
			return ((SubClassTreeEntry) element).getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof InheritanceLevel) {
			final List<SubClassTreeEntry> elements = this.tree.get(element);
			return elements != null && !elements.isEmpty();
		}
		return false;
	}
}
