/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui;


import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.internal.ui.MakeUIImages;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class MakeLabelProvider extends LabelProvider implements ITableLabelProvider {
	private IPath pathPrefix;

	WorkbenchLabelProvider fLableProvider = new WorkbenchLabelProvider();

	public MakeLabelProvider() {
		this(null);
	}

	public MakeLabelProvider(IPath removePrefix) {
		pathPrefix = removePrefix;
	}
	/**
	 * @see ILabelProvider#getImage(Object)
	 */
	public Image getImage(Object obj) {
		Image image = null;
		if (obj instanceof IMakeTarget) {
			return MakeUIImages.getImage(MakeUIImages.IMG_OBJS_BUILD_TARGET);
		} else if (obj instanceof IContainer) {
			return fLableProvider.getImage(obj);
		}
		return image;
	}

	/**
	 * @see ILabelProvider#getText(Object)
	 */
	public String getText(Object obj) {
		if (obj instanceof IMakeTarget) {
			return ((IMakeTarget) obj).getName();
		} else if (obj instanceof IContainer) {
			return fLableProvider.getText(obj);
		}
		return ""; //$NON-NLS-1$
	}

	public void dispose() {
		super.dispose();
		fLableProvider.dispose();
	}

	public Image getColumnImage(Object obj, int columnIndex) {
		return columnIndex == 0 ? getImage(obj) : null;
	}

	public String getColumnText(Object obj, int columnIndex) {
		switch (columnIndex) {
			case 0 :
				return getText(obj);
			case 1 :
				if (obj instanceof IMakeTarget) {
					if (pathPrefix != null) {
						IPath targetPath = ((IMakeTarget) obj).getContainer().getProjectRelativePath();
						if (pathPrefix.isPrefixOf(targetPath)) {
							targetPath = targetPath.removeFirstSegments(pathPrefix.segmentCount());
						}
						if (targetPath.segmentCount() > 0) {
							return targetPath.toString();
						}
					}
				}
		}
		return ""; //$NON-NLS-1$
	}
}
