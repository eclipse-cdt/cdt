package org.eclipse.cdt.debug.internal.ui.launch;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class GenericTargetPropertiesBlock extends Composite {

	private Text nameText;
	private Text osText;
	private Text archText;

	public GenericTargetPropertiesBlock(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(2, false));

		Label label = new Label(this, SWT.NONE);
		label.setText("Name:");

		nameText = new Text(this, SWT.BORDER);
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		label = new Label(this, SWT.NONE);
		label.setText("Operating System:");

		osText = new Text(this, SWT.BORDER);
		osText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		label = new Label(this, SWT.NONE);
		label.setText("CPU Architecture:");

		archText = new Text(this, SWT.BORDER);
		archText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	}

	public String getTargetName() {
		return nameText.getText();
	}

	public String getOS() {
		return osText.getText();
	}

	public String getArch() {
		return archText.getText();
	}

}
