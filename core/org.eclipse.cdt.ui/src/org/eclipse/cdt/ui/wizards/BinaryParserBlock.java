package org.eclipse.cdt.ui.wizards;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.cdt.utils.ui.controls.RadioButtonsArea;
import org.eclipse.cdt.utils.ui.swt.IValidation;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public class BinaryParserBlock implements IWizardTab {

	//private static final String PREFIX = "BinaryParserBlock"; //$NON-NLS-1$
	//private static final String LABEL = PREFIX + ".label"; //$NON-NLS-1$

	private static String[][] radios;
	private IProject project;

	protected RadioButtonsArea radioButtons;
	private String id;
	//	protected Button defButton;

	IValidation page;

	public BinaryParserBlock(IValidation valid) {
		this(valid, null);
	}

	public BinaryParserBlock(IValidation valid, IProject p) {
		page = valid;
		project = p;
		IExtensionPoint point = CCorePlugin.getDefault().getDescriptor().getExtensionPoint(CCorePlugin.BINARY_PARSER_SIMPLE_ID);
		if (point != null) {
			IExtension[] exts = point.getExtensions();
			radios = new String[exts.length][2];
			for (int i = 0; i < exts.length; i++) {
				radios[i] = new String[] { exts[i].getLabel(), exts[i].getUniqueIdentifier()};
			}
		}
		try {
			ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(p);
			ICExtensionReference[] ref = desc.get(CCorePlugin.BINARY_PARSER_UNIQ_ID);
			if (ref.length > 0)
				id = ref[0].getID();

		} catch (CoreException e) {
			//e.printStackTrace();
		}
		if (id == null) {
			id = CCorePlugin.getDefault().getPluginPreferences().getDefaultString(CCorePlugin.PREF_BINARY_PARSER);
			if (id == null || id.length() == 0) {
				id = CCorePlugin.DEFAULT_BINARY_PARSER_UNIQ_ID;
			}
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
		if (id != null) {
			radioButtons.setSelectValue(id);
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
		try {
			ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(project);
			String identity = radioButtons.getSelectedValue();
			if (identity != null) {
				if (id == null || !identity.equals(id)) {
					desc.remove(CCorePlugin.BINARY_PARSER_UNIQ_ID);
					desc.create(CCorePlugin.BINARY_PARSER_UNIQ_ID, identity);
					CCorePlugin.getDefault().getCoreModel().resetBinaryParser(project);
					id = identity;
				}
			}
		} catch (CoreException e) {
		}
	}

}
