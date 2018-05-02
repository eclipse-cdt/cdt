/*******************************************************************************
 * Copyright (c) 2018 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Baha El-Kassaby - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.debugsources;

import org.eclipse.cdt.dsf.gdb.internal.ui.debugsources.tree.DebugTree;
import org.eclipse.jface.viewers.ITreeContentProvider;

public class DebugSourcesTreeContentProvider implements ITreeContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof String) {
			return new String[] { (String) parentElement };
		}
		if (parentElement instanceof DebugTree) {
			DebugTree<?> tree = (DebugTree<?>) parentElement;
			return tree.getChildren().toArray();
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		if (element == null)
			return null;
		if (element instanceof String) {
			return null;
		}
		if (element instanceof DebugTree) {
			DebugTree<?> node = (DebugTree<?>) element;
			return node.getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof String) {
			return false;
		}
		if (element instanceof DebugTree) {
			DebugTree<?> node = (DebugTree<?>) element;
			return node.hasChildren();
		}
		return false;
	}

}
