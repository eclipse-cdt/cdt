package org.eclipse.cdt.ui.wizards;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParserConfiguration;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.cdt.utils.ui.controls.RadioButtonsArea;
import org.eclipse.cdt.utils.ui.swt.IValidation;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;


public class BinaryParserBlock implements IWizardTab {

	private static final String PREFIX = "BinaryParserBlock"; //$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label"; //$NON-NLS-1$
	
	private static String[][] radios;
	private IProject project;

	protected RadioButtonsArea radioButtons;
	private String defaultFormat;
//	protected Button defButton;

	IValidation page;

	public BinaryParserBlock(IValidation valid) {
		this(valid, null);
	}

	public BinaryParserBlock(IValidation valid, IProject p) {
		page = valid;
		project = p;
		IBinaryParserConfiguration[] configs = CCorePlugin.getDefault().getBinaryParserConfigurations();
		radios = new String[configs.length][2];
		for (int i = 0; i < configs.length; i++) {
			radios[i] = new String[] {configs[i].getName(), configs[i].getFormat()};
		}
		CoreModel model = CCorePlugin.getDefault().getCoreModel();
		if (project == null) {
			defaultFormat = model.getDefaultBinaryParserFormat();
		} else {
			defaultFormat = model.getBinaryParserFormat(project);
		}
	}

	public String getLabel() {
		//return CUIPlugin.getResourceString(LABEL);
		return "Binary Parser";
	}

	public Image getImage() {
		return null;
	}

	public Composite getControl(Composite parent) {
		Composite composite = ControlFactory.createComposite(parent, 1);

		radioButtons = new RadioButtonsArea(composite, "Parsers", 1, radios);
		radioButtons.setEnabled(true);
		if (defaultFormat != null) {
			radioButtons.setSelectValue(defaultFormat);
		}
		return composite;
	}

	public boolean isValid() {
		return true;
	}
	
	public void setVisible(boolean visible) {
	}

	public void doRun(IProject project, IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("Parsers", 1);
		CoreModel model = CCorePlugin.getDefault().getCoreModel();
		String format = radioButtons.getSelectedValue();
		if (format != null) {
			if (defaultFormat == null || !format.equals(defaultFormat)) {
				model.setBinaryParserFormat(project, format, monitor);
				defaultFormat = format;
			}
		}
	}

}
