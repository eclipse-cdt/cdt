package org.eclipse.cdt.internal.ui.viewsupport;

import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.ICFileImageDescriptor;
import org.eclipse.cdt.ui.SharedImagesFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;

public class DefaultCFileImageDescriptor implements ICFileImageDescriptor {
	private SharedImagesFactory imagesFactory = new SharedImagesFactory(CUIPlugin.getDefault());

	@Override
	public ImageDescriptor getCImageDescriptor() {
		return imagesFactory.getImageDescriptor(CDTSharedImages.IMG_OBJS_TUNIT);
	}

	@Override
	public ImageDescriptor getCXXImageDescriptor() {
		return imagesFactory.getImageDescriptor(CDTSharedImages.IMG_OBJS_TUNIT);
	}

	@Override
	public ImageDescriptor getHeaderImageDescriptor() {
		return imagesFactory.getImageDescriptor(CDTSharedImages.IMG_OBJS_TUNIT_HEADER);
	}

	@Override
	public boolean isEnabled(IProject project) {
		return true;
	}
}
