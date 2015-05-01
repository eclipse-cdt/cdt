package org.eclipse.launchbar.ui.internal.controls;

import org.eclipse.launchbar.ui.internal.Activator;
import org.eclipse.launchbar.ui.internal.Messages;
import org.eclipse.swt.widgets.Composite;

public class EditButton extends CButton {
	public EditButton(Composite parent, int style) {
		super(parent, style);
		setHotImage(Activator.getDefault().getImage("icons/config_config.png")); //$NON-NLS-1$
		setColdImage(Activator.getDefault().getImage("icons/edit_cold.png")); //$NON-NLS-1$
		setToolTipText(Messages.EditButton_0);
	}
}
