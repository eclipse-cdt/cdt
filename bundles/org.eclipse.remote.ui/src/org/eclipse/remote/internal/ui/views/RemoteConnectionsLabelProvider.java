package org.eclipse.remote.internal.ui.views;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.swt.graphics.Image;


public class RemoteConnectionsLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof IRemoteConnection) {
			return ((IRemoteConnection) element).getName();
		} else {
			return super.getText(element);
		}
	}

	@Override
	public Image getImage(Object element) {
		// TODO Need a method to get icons for the UI connection managers.
		return super.getImage(element);
	}

}
