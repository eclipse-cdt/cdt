package org.eclipse.cdt.make.ui.views;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.internal.ui.MakeUIImages;
import org.eclipse.core.resources.IContainer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class MakeLabelProvider extends LabelProvider {
	WorkbenchLabelProvider fLableProvider = new WorkbenchLabelProvider();
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
			return ((IMakeTarget)obj).getName();
		} else if (obj instanceof IContainer) {
			return fLableProvider.getText(obj);			
		}
		return ""; //$NON-NLS-1$
	}
	
	public void dispose() {
		super.dispose();
		fLableProvider.dispose();
	}

}
