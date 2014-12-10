package org.eclipse.launchbar.ui.internal;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.launchbar.core.ILaunchTarget;


public class LocalTargetLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof ILaunchTarget) {
			return ((ILaunchTarget) element).getName();
		}
		return super.getText(element);
	}

}
