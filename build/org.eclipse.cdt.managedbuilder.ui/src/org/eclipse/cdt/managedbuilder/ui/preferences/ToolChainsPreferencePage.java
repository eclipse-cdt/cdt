package org.eclipse.cdt.managedbuilder.ui.preferences;

import org.eclipse.cdt.managedbuilder.internal.ui.Messages;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ToolChainsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public ToolChainsPreferencePage() {
		this(Messages.ToolChainPrefsTitle);
	}
	
	public ToolChainsPreferencePage(String title) {
		super(title);
	}

	public ToolChainsPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.ToolChainPrefsMessage);
		return label;
	}

}
