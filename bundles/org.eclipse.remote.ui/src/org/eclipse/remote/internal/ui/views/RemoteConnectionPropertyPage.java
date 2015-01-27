package org.eclipse.remote.internal.ui.views;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.internal.ui.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

public class RemoteConnectionPropertyPage extends PropertyPage {

	@Override
	protected Control createContents(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));

		IRemoteConnection connection = null;
		Object element = getElement();
		if (element instanceof IRemoteConnection) {
			connection = (IRemoteConnection) element;
		} else if (element instanceof IAdaptable) {
			connection = (IRemoteConnection) ((IAdaptable) element).getAdapter(IRemoteConnection.class);
		}

		if (connection != null) {
			Label nameLabel = new Label(comp, SWT.NONE);
			nameLabel.setText(Messages.RemoteConnectionPropertyPage_ConnectionName);

			Text nameText = new Text(comp, SWT.BORDER);
			nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			nameText.setText(connection.getName());
		}

		return comp;
	}

	@Override
	public boolean performOk() {
		// TODO, change the name if it needs changing
		return true;
	}

}
