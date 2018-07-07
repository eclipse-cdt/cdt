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

import java.io.File;
import java.util.Set;

import org.eclipse.cdt.dsf.gdb.internal.ui.debugsources.tree.DebugTree;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class DebugSourcesLabelProvider extends ColumnLabelProvider {
	public static final DebugSourcesLabelProvider[] FLATTENED = new DebugSourcesLabelProvider[] {
			new DebugSourcesLabelProvider(true, 0), new DebugSourcesLabelProvider(true, 1) };
	public static final DebugSourcesLabelProvider[] NORMAL = new DebugSourcesLabelProvider[] {
			new DebugSourcesLabelProvider(false, 0), new DebugSourcesLabelProvider(false, 1) };

	private int index;
	private boolean flattenFoldersWithNoFiles;

	/**
	 * 
	 * @param index
	 *            of column
	 */
	private DebugSourcesLabelProvider(boolean flattenFoldersWithNoFiles, int index) {
		this.flattenFoldersWithNoFiles = flattenFoldersWithNoFiles;
		this.index = index;
	}

	@Override
	public String getText(Object element) {
		return getLabel(element, index);
	}

	private String getLabel(Object element, int columnIdx) {
		String emptyString = ""; //$NON-NLS-1$
		if (element instanceof DebugTree) {
			@SuppressWarnings("unchecked")
			DebugTree<Comparable<?>> node = (DebugTree<Comparable<?>>) element;
			if (columnIdx == 0) {
				Set<DebugTree<Comparable<?>>> children = node.getChildren();
				StringBuilder sb = new StringBuilder((String) node.getData());
				if (flattenFoldersWithNoFiles) {
					while (true) {
						if (node.getLeafData() != null) {
							break;
						}

						if (children.size() != 1) {
							break;
						}
						DebugTree<Comparable<?>> child = children.iterator().next();
						if (child.getLeafData() != null) {
							break;
						}

						node = child;
						children = node.getChildren();
						if (sb.length() > 0 && sb.charAt(sb.length() - 1) != File.separatorChar) {
							sb.append(File.separatorChar);
						}
						sb.append(node.getData());
					}
				}
				return sb.toString();
			}
			if (columnIdx == 1) {
				return node.hasChildren() ? emptyString : (String) node.getLeafData();
			}
		}
		return emptyString;
	}

	@Override
	public Color getForeground(Object element) {
		if (index == 1) {
			if (element instanceof DebugTree) {
				@SuppressWarnings("unchecked")
				DebugTree<Comparable<?>> node = (DebugTree<Comparable<?>>) element;
				if (!node.getExists()) {
					return Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
				}
			}
		}

		return super.getForeground(element);
	}
}
