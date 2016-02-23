package org.eclipse.cdt.internal.qt.ui.launch;

import org.eclipse.cdt.internal.qt.ui.Activator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.swt.graphics.Image;

public class QtLaunchDescriptorLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof ILaunchDescriptor) {
			return ((ILaunchDescriptor) element).getName();
		}
		return super.getText(element);
	}

	@Override
	public Image getImage(Object element) {
		return Activator.getImage(Activator.IMG_QT_16);
	}

}
