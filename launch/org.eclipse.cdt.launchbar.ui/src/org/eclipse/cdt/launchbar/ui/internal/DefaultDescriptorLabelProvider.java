package org.eclipse.cdt.launchbar.ui.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.launchbar.core.ILaunchBarManager;
import org.eclipse.cdt.launchbar.core.ILaunchDescriptor;
import org.eclipse.cdt.launchbar.core.ILaunchTarget;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class DefaultDescriptorLabelProvider extends LabelProvider {
	
	private Map<ImageDescriptor, Image> images = new HashMap<>();

	@Override
	public void dispose() {
		super.dispose();
		for (Image image : images.values()) {
			image.dispose();
		}
	}
	
	@Override
	public Image getImage(Object element) {
		if (element instanceof ILaunchDescriptor) {
			try {
				ILaunchDescriptor desc = (ILaunchDescriptor) element;
				ILaunchBarManager manager = desc.getType().getManager();
				ILaunchTarget target = manager.getActiveLaunchTarget();
				ILaunchConfigurationType type = manager.getLaunchConfigurationType(desc, target);
				ImageDescriptor imageDescriptor = DebugUITools.getDefaultImageDescriptor(type);
				if (imageDescriptor != null) {
					Image image = images.get(imageDescriptor);
					if (image == null) {
						image = imageDescriptor.createImage();
						images.put(imageDescriptor, image);
					}
					return image;
				}
			} catch (CoreException e) {
				Activator.log(e);
			}
		}
		return super.getImage(element);
	}

	@Override
	public String getText(Object element) {
		if (element instanceof ILaunchDescriptor) {
			return ((ILaunchDescriptor) element).getName();
		}
		return super.getText(element);
	}

}
