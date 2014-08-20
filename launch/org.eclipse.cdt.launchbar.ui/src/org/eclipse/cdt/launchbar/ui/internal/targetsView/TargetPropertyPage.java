package org.eclipse.cdt.launchbar.ui.internal.targetsView;

import org.eclipse.cdt.launchbar.core.ILaunchTarget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

public class TargetPropertyPage extends PropertyPage {

	private Text nameText;
	
	@Override
	protected Control createContents(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		comp.setLayout(layout);

		ILaunchTarget target = (ILaunchTarget) getElement().getAdapter(ILaunchTarget.class);
		
		Label nameLabel = new Label(comp, SWT.NONE);
		nameLabel.setText("Target Name:");

		nameText = new Text(comp, SWT.BORDER);
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nameText.setText(target.getName());
		
		Label targetLabel = new Label(comp, SWT.NONE);
		targetLabel.setText("Target Id:");
		
		Label targetId = new Label(comp, SWT.NONE);
		targetId.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		targetId.setText(target.getId());
		
		Label typeLabel = new Label(comp, SWT.NONE);
		typeLabel.setText("Target Type:");
		
		Label typeId = new Label(comp, SWT.NONE);
		typeId.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		typeId.setText(target.getType().getId());
		
		return comp;
	}

	@Override
	public boolean performOk() {
		System.out.println("Would change name to " + nameText.getText());
		return true;
	}

}
