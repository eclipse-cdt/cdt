/*******************************************************************************
 * Copyright (c) 2006, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.includebrowser;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.viewsupport.ImageImageDescriptor;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CElementImageDescriptor;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

public class IBLabelProvider extends LabelProvider implements IColorProvider {
	private CElementLabelProvider fCLabelProvider = new CElementLabelProvider();
	private Color fColorInactive;
	private IBContentProvider fContentProvider;
	private HashMap<String, Image> fCachedImages = new HashMap<>();
	private boolean fShowFolders;

	public IBLabelProvider(Display display, IBContentProvider cp) {
		fColorInactive = display.getSystemColor(SWT.COLOR_DARK_GRAY);
		fContentProvider = cp;
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof IBNode) {
			IBNode node = (IBNode) element;
			ITranslationUnit tu = node.getRepresentedTranslationUnit();
			Image image = tu != null ? fCLabelProvider.getImage(tu)
					: CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_TUNIT_HEADER);
			return decorateImage(image, node);
		}
		return super.getImage(element);
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IBNode) {
			IBNode node = (IBNode) element;
			IPath path = node.getRepresentedPath();
			if (path != null) {
				if (fShowFolders) {
					return path.lastSegment() + " (" + path.removeLastSegments(1) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
				}
				return path.lastSegment();
			}
			return node.getDirectiveName();
		}
		return super.getText(element);
	}

	@Override
	public void dispose() {
		fCLabelProvider.dispose();
		for (Iterator<Image> iter = fCachedImages.values().iterator(); iter.hasNext();) {
			Image image = iter.next();
			image.dispose();
		}
		fCachedImages.clear();
		super.dispose();
	}

	private Image decorateImage(Image image, IBNode node) {
		int flags = 0;
		if (node.isSystemInclude()) {
			flags |= CElementImageDescriptor.SYSTEM_INCLUDE;
		}
		if (!node.isActiveCode()) {
			flags |= CElementImageDescriptor.INACTIVE;
		}

		if (node.isRecursive()) {
			flags |= CElementImageDescriptor.RECURSIVE_RELATION;
		} else if (fContentProvider.hasChildren(node)) {
			if (fContentProvider.getComputeIncludedBy()) {
				flags |= CElementImageDescriptor.REFERENCED_BY;
			} else {
				flags |= CElementImageDescriptor.RELATES_TO;
			}
		}

		if (node.isActiveCode() && node.getRepresentedIFL() == null) {
			flags |= CElementImageDescriptor.WARNING;
		}

		if (flags == 0) {
			return image;
		}
		String key = image.toString() + String.valueOf(flags);
		Image result = fCachedImages.get(key);
		if (result == null) {
			ImageDescriptor desc = new CElementImageDescriptor(new ImageImageDescriptor(image), flags,
					new Point(20, 16));
			result = desc.createImage();
			fCachedImages.put(key, result);
		}
		return result;
	}

	@Override
	public Color getBackground(Object element) {
		return null;
	}

	@Override
	public Color getForeground(Object element) {
		if (element instanceof IBNode) {
			IBNode node = (IBNode) element;
			if (!node.isActiveCode()) {
				return fColorInactive;
			}
		}
		return null;
	}

	public void setShowFolders(boolean show) {
		fShowFolders = show;
	}
}
