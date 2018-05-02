/*******************************************************************************
 * Copyright (c) 2018, 2019 Kichwa Coders and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Baha El-Kassaby - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.debugsources;

import java.io.File;
import java.util.Set;

import org.eclipse.cdt.dsf.gdb.internal.ui.debugsources.DebugSourcesTreeElement.FileExist;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

public class DebugSourcesLabelProvider extends ColumnLabelProvider {
	private int index;
	private boolean flattenFoldersWithNoFiles = true;
	private boolean showExistingFilesOnly = true;

	/**
	 *
	 * @param index
	 *            of column
	 */
	public DebugSourcesLabelProvider(int index) {
		this.index = index;
	}

	public void setFlattenFoldersWithNoFiles(boolean flattenFoldersWithNoFiles) {
		this.flattenFoldersWithNoFiles = flattenFoldersWithNoFiles;
	}

	public boolean isFlattenFoldersWithNoFiles() {
		return flattenFoldersWithNoFiles;
	}

	public void setShowExistingFilesOnly(boolean showExistingFilesOnly) {
		this.showExistingFilesOnly = showExistingFilesOnly;
	}

	public boolean isShowExistingFilesOnly() {
		return showExistingFilesOnly;
	}

	@Override
	public String getText(Object element) {
		return getLabel(element, index);
	}

	private String getLabel(Object element, int columnIdx) {
		String emptyString = ""; //$NON-NLS-1$
		if (element instanceof DebugSourcesTreeElement) {
			DebugSourcesTreeElement node = (DebugSourcesTreeElement) element;
			if (columnIdx == 0) {
				Set<DebugSourcesTreeElement> children;
				StringBuilder sb = new StringBuilder();
				sb.append(node.getName());
				if (flattenFoldersWithNoFiles) {
					while (true) {
						if (node.getFullPath() != null) {
							break;
						}
						children = node.getChildren(showExistingFilesOnly);

						if (children.size() != 1) {
							break;
						}
						DebugSourcesTreeElement child = children.iterator().next();
						if (child.getFullPath() != null) {
							break;
						}

						node = child;
						if (sb.length() > 0 && sb.charAt(sb.length() - 1) != File.separatorChar) {
							sb.append(File.separatorChar);
						}
						sb.append(node.getName());
					}
				}
				return sb.toString();
			}
			if (columnIdx == 1) {
				return node.hasChildren() ? emptyString : (String) node.getFullPath();
			}
		}
		return emptyString;
	}

	@Override
	public Color getForeground(Object element) {
		if (index == 1) {
			if (element instanceof DebugSourcesTreeElement) {
				DebugSourcesTreeElement node = (DebugSourcesTreeElement) element;
				if (node.getExists() == FileExist.NO) {
					return Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
				}
			}
		}

		return super.getForeground(element);
	}

	@Override
	public Font getFont(Object element) {
		// TODO Auto-generated method stub
		return super.getFont(element);
	}
}
