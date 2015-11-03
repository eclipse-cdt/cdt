package org.eclipse.cdt.arduino.ui.internal.remote;

import org.eclipse.cdt.arduino.core.internal.remote.ArduinoRemoteConnection;
import org.eclipse.cdt.arduino.ui.internal.Activator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.swt.graphics.Image;

public class ArduinoLaunchTargetLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof ILaunchTarget) {
			return ((ILaunchTarget) element).getName();
		}
		return super.getText(element);
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof ILaunchTarget
				&& ((ILaunchTarget) element).getTypeId().equals(ArduinoRemoteConnection.TYPE_ID)) {
			return Activator.getDefault().getImageRegistry().get(Activator.IMG_ARDUINO);
		}
		return super.getImage(element);
	}

}
