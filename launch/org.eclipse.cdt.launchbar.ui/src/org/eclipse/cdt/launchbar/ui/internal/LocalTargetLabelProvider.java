package org.eclipse.cdt.launchbar.ui.internal;
import org.eclipse.cdt.launchbar.core.ILaunchTarget;
import org.eclipse.jface.viewers.LabelProvider;


public class LocalTargetLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof ILaunchTarget) {
			return ((ILaunchTarget) element).getName();
		}
		return super.getText(element);
	}

}
