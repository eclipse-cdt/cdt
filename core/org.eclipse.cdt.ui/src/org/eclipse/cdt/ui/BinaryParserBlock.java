package org.eclipse.cdt.ui;
/***********************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class BinaryParserBlock extends AbstractCOptionPage {

	private static String[][] radios;

	protected Combo comboBox;
	private HashMap idMap = new HashMap();
	private String initial;
	private Preferences fPrefs;
	
	public BinaryParserBlock(Preferences prefs) {
		super("Binary Parser");
		setDescription("Set required binary parser for this project");
		fPrefs = prefs;
	}

	public void createControl(Composite parent) {		
		Composite control = ControlFactory.createComposite(parent, 2);
		((GridLayout)control.getLayout()).makeColumnsEqualWidth = false;
		((GridLayout)control.getLayout()).marginWidth = 5;

		ControlFactory.createEmptySpace(control, 2);

		Label label = ControlFactory.createLabel(control, "Binary Parser:");
		label.setLayoutData(new GridData());
		comboBox = new Combo(control, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		comboBox.setLayoutData(gd);
		comboBox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getContainer().updateContainer();
			}
		});
		Iterator items =  idMap.keySet().iterator();
		while( items.hasNext()) {
			comboBox.add((String)items.next());
		}

		if (initial != null) {
			comboBox.setText(initial);
		}
		setControl(control);
	}

	public void performApply(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("Parsers", 1);
		if (getContainer().getProject() != null) {
			ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(getContainer().getProject());
			String selected = comboBox.getText();
			if (selected != null) {
				if (initial == null || !selected.equals(initial)) {
					desc.remove(CCorePlugin.BINARY_PARSER_UNIQ_ID);
					desc.create(CCorePlugin.BINARY_PARSER_UNIQ_ID, (String)idMap.get(initial));
					CCorePlugin.getDefault().getCoreModel().resetBinaryParser(getContainer().getProject());
					initial = selected;
				}
			}
		} else {
			fPrefs.setDefault(CCorePlugin.PREF_BINARY_PARSER, (String)idMap.get(initial));
		}
	}

	public void setContainer(ICOptionContainer container) {
		super.setContainer(container);
		IExtensionPoint point = CCorePlugin.getDefault().getDescriptor().getExtensionPoint(CCorePlugin.BINARY_PARSER_SIMPLE_ID);
		if (point != null) {
			IExtension[] exts = point.getExtensions();
			radios = new String[exts.length][2];
			for (int i = 0; i < exts.length; i++) {
				idMap.put(exts[i].getLabel(), exts[i].getUniqueIdentifier());
			}
		}
		if (getContainer().getProject() != null) {
			try {
				ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(getContainer().getProject());
				ICExtensionReference[] ref = desc.get(CCorePlugin.BINARY_PARSER_UNIQ_ID);
				if (ref.length > 0) {
					initial = point.getExtension(ref[0].getID()).getLabel();
				}

			} catch (CoreException e) {
			}
		}
		if (initial == null) {
			String id = fPrefs.getString(CCorePlugin.PREF_BINARY_PARSER);
			if (id == null || id.length() == 0) {
				initial = point.getExtension(CCorePlugin.DEFAULT_BINARY_PARSER_UNIQ_ID).getLabel();
			} else {
				initial = point.getExtension(id).getLabel();
			}
		}

	}

	public void performDefaults() {
		IExtensionPoint point = CCorePlugin.getDefault().getDescriptor().getExtensionPoint(CCorePlugin.BINARY_PARSER_SIMPLE_ID);
		String id;
		if (getContainer().getProject() != null) {
			id = fPrefs.getString(CCorePlugin.PREF_BINARY_PARSER);
		} else {
			id = fPrefs.getDefaultString(CCorePlugin.PREF_BINARY_PARSER);
		}
		if (id == null || id.length() == 0) {
			initial = point.getExtension(CCorePlugin.DEFAULT_BINARY_PARSER_UNIQ_ID).getLabel();
		} else {
			initial = point.getExtension(id).getLabel();
		}
		comboBox.setText(initial);
		getContainer().updateContainer();
	}

}
