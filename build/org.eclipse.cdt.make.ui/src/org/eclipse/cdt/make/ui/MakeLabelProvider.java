/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Andrew Gvozdev - some improvements such as adding source folders bug 339015
 *******************************************************************************/
package org.eclipse.cdt.make.ui;


import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.internal.ui.MakeUIImages;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Label provider for Make Targets view and for Make Targets dialog from
 * "Make Targets"->"Build..." in project context menu.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class MakeLabelProvider extends LabelProvider implements ITableLabelProvider {
	private IPath pathPrefix;
	private WorkbenchLabelProvider fLableProvider = new WorkbenchLabelProvider();

	public MakeLabelProvider() {
		this(null);
	}

	public MakeLabelProvider(IPath removePrefix) {
		pathPrefix = removePrefix;
	}
	/**
	 * @see ILabelProvider#getImage(Object)
	 */
	@Override
	public Image getImage(Object obj) {
		Image image = null;
		if (obj instanceof TargetSourceContainer) {
			return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_SOURCE_ROOT);
		} else if (obj instanceof IContainer) {
			if (!(obj instanceof IProject) && MakeContentProvider.isSourceEntry((IContainer) obj))
				return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_SOURCE_ROOT);
			return fLableProvider.getImage(obj);
		} else if (obj instanceof IMakeTarget) {
			return MakeUIImages.getImage(MakeUIImages.IMG_OBJS_BUILD_TARGET);
		}
		return image;
	}

	/**
	 * @see ILabelProvider#getText(Object)
	 */
	@Override
	public String getText(Object obj) {
		if (obj instanceof TargetSourceContainer) {
			IContainer container = ((TargetSourceContainer) obj).getContainer();
			IPath path = container.getFullPath();
			// remove leading project name
			path = path.removeFirstSegments(1);
			return path.toString();
		} else if (obj instanceof IContainer) {
			return fLableProvider.getText(obj);
		} else if (obj instanceof IMakeTarget) {
			return ((IMakeTarget) obj).getName();
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	public void dispose() {
		super.dispose();
		fLableProvider.dispose();
	}

	@Override
	public Image getColumnImage(Object obj, int columnIndex) {
		return columnIndex == 0 ? getImage(obj) : null;
	}

	@Override
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
