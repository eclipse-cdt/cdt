package org.eclipse.cdt.make.ui.views;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.core.resources.IContainer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class MakeLabelProvider extends LabelProvider {

	/**
	 * @see ILabelProvider#getImage(Object)
	 */
	public Image getImage(Object obj) {
		Image image = null;
		if (obj instanceof IMakeTarget) {
		} else if (obj instanceof IContainer) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
		}
		return image;
	}

	/**
	 * @see ILabelProvider#getText(Object)
	 */
	public String getText(Object obj) {
		if (obj instanceof MakeTarget) {
			return ((IMakeTarget)obj).getName();
		} else if (obj instanceof IContainer) {
			return ((IContainer)obj).getName();
		}
		return "";
	}
}
