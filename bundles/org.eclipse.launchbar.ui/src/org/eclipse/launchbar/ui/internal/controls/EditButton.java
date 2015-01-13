package org.eclipse.launchbar.ui.internal.controls;

import org.eclipse.launchbar.ui.internal.Activator;
import org.eclipse.swt.widgets.Composite;

public class EditButton extends CButton {
	public EditButton(Composite parent, int style) {
		super(parent, style);
		setHotImage(Activator.getDefault().getImage("icons/config_config.png"));
		setColdImage(Activator.getDefault().getImage("icons/edit_cold.png"));
		setToolTipText("Edit");
	}
}
