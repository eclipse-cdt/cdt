package org.eclipse.cdt.make.ui;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.internal.ui.MakeUIImages;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class MakeLabelProvider extends LabelProvider {
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
		StringBuffer str = new StringBuffer();
		if (obj instanceof IMakeTarget) {
			if ( pathPrefix != null) {
				IPath targetPath = ((IMakeTarget)obj).getContainer().getProjectRelativePath();
				if ( pathPrefix.isPrefixOf(targetPath) ) {
					targetPath = targetPath.removeFirstSegments(pathPrefix.segmentCount());
				}
				str.append(targetPath.toString());
				if (targetPath.segmentCount() > 0) {
					str.append("/");
				}
			}
			str.append(((IMakeTarget)obj).getName());
		} else if (obj instanceof IContainer) {
			if ( pathPrefix != null ) {
				IPath targetPath = ((IContainer)obj).getProjectRelativePath();
				if ( pathPrefix.isPrefixOf(targetPath) ) {
					targetPath = targetPath.removeFirstSegments(pathPrefix.segmentCount());
				}
				str.append(targetPath.toString());
				str.append("/");
			} else {
				return fLableProvider.getText(obj);
			}			
		}
		return str.toString();
	}
	
	public void dispose() {
		super.dispose();
		fLableProvider.dispose();
	}

}
