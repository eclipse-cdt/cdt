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
package org.eclipse.cdt.ui.dialogs;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.help.WorkbenchHelp;

public class BinaryParserBlock extends AbstractBinaryParserPage {

	private static final String PREFIX = "BinaryParserBlock"; // $NON-NLS-1$ //$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label"; // $NON-NLS-1$ //$NON-NLS-1$
	private static final String DESC = PREFIX + ".desc"; // $NON-NLS-1$ //$NON-NLS-1$

	private static String[][] radios;
	protected Combo comboBox;
	private HashMap idMap = new HashMap();
	private String initial;
	private Preferences fPrefs;

	public BinaryParserBlock(Preferences prefs) {
		super(CUIPlugin.getResourceString(LABEL));
		setDescription(CUIPlugin.getResourceString(DESC));
		fPrefs = prefs;
	}

	public void createControl(Composite parent) {
		Composite control = ControlFactory.createComposite(parent, 2);
		((GridLayout) control.getLayout()).makeColumnsEqualWidth = false;
		((GridLayout) control.getLayout()).marginWidth = 5;
		setControl(control);

		WorkbenchHelp.setHelp(getControl(), ICHelpContextIds.BINARY_PARSER_PAGE);

		ControlFactory.createEmptySpace(control, 2);

		Label label = ControlFactory.createLabel(control, CUIMessages.getString("BinaryParserBlock.binaryParser")); //$NON-NLS-1$
		label.setLayoutData(new GridData());
		comboBox = new Combo(control, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		comboBox.setLayoutData(gd);
		comboBox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getContainer().updateContainer();
				handleBinaryParserChanged();
			}
		});
		Iterator items = idMap.keySet().iterator();
		while (items.hasNext()) {
			comboBox.add((String) items.next());
		}

		if (initial != null) {
			comboBox.setText(initial);
		}

		// Add the Parser UI contribution.
		Group parserGroup = new Group(control, SWT.SHADOW_ETCHED_IN);
		parserGroup.setText(CUIMessages.getString("BinaryParserBlock.binaryParserOptions")); //$NON-NLS-1$
		GridLayout tabHolderLayout = new GridLayout();
		tabHolderLayout.marginHeight = 0;
		tabHolderLayout.marginWidth = 0;
		tabHolderLayout.numColumns = 1;
		parserGroup.setLayout(tabHolderLayout);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		parserGroup.setLayoutData(gd);
		// Must set the composite parent to super class.
		setCompositeParent(parserGroup);
		// fire a change event, to quick start.
		handleBinaryParserChanged();

	}

	public void performApply(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(CUIMessages.getString("BinaryParserBlock.settingBinaryParser"), 2); //$NON-NLS-1$
		String selected = comboBox.getText();
		if (selected != null) {
			if (initial == null || !selected.equals(initial)) {
				if (getContainer().getProject() != null) {
					ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(getContainer().getProject());
					desc.remove(CCorePlugin.BINARY_PARSER_UNIQ_ID);
					desc.create(CCorePlugin.BINARY_PARSER_UNIQ_ID, (String) idMap.get(selected));
				} else {
					fPrefs.setValue(CCorePlugin.PREF_BINARY_PARSER, (String) idMap.get(selected));
				}
				initial = selected;
			}
		}
		monitor.worked(1);
		// Give a chance to the contributions to save.
		// We have to do it last to make sure the parser id is save
		// in .cdtproject
		super.performApply(new SubProgressMonitor(monitor, 1));

		// Reset the binary parser the paths may have change.
		if (getContainer().getProject() != null)
			CCorePlugin.getDefault().getCoreModel().resetBinaryParser(getContainer().getProject());

		monitor.done();
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
					IExtension ext = point.getExtension(ref[0].getID());
					if (ext != null) {
						initial = ext.getLabel();
					}
				}

			} catch (CoreException e) {
			}
		}
		if (initial == null) {
			String id = fPrefs.getString(CCorePlugin.PREF_BINARY_PARSER);
			if (id == null || id.length() == 0) {
				initial = point.getExtension(CCorePlugin.DEFAULT_BINARY_PARSER_UNIQ_ID).getLabel();
			} else {
				IExtension ext = point.getExtension(id);
				if (ext != null) {
					initial = ext.getLabel();
				}

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
		String selected;
		if (id == null || id.length() == 0) {
			selected = point.getExtension(CCorePlugin.DEFAULT_BINARY_PARSER_UNIQ_ID).getLabel();
		} else {
			selected = point.getExtension(id).getLabel();
		}
		comboBox.setText(selected);
		// Give a change to the UI contributors to react.
		// But do it last after the comboBox is set.
		handleBinaryParserChanged();
		super.performDefaults();
		getContainer().updateContainer();
	}

	protected String getCurrentBinaryParserID() {
		String selected = comboBox.getText();
		return (String) idMap.get(selected);
	}

}
