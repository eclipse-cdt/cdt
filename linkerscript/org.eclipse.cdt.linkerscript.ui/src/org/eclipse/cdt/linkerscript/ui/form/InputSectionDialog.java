/*******************************************************************************
 * Copyright (c) 2016, 2017 Kichwa Coders Ltd (https://kichwacoders.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.linkerscript.ui.form;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class InputSectionDialog extends TrayDialog {

	private static final String[] SORT_NAMES = new String[] {"Unsorted"};

	private Text nameText;
	private Combo sortTypeCombo;

	private String sectionName;
	private String sortType;

	public InputSectionDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Create a new linker input section dialog
	 *
	 * @param parentShell
	 *            shell
	 * @param name
	 *            name of section
	 * @param type
	 *            the sort type
	 */
	public InputSectionDialog(Shell parentShell, String name, String type) {
		super(parentShell);
		this.sectionName = name;
		this.sortType = type;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Input Section Dialog");
		setHelpAvailable(false);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
		composite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		// section name
		Label label = new Label(composite, SWT.NONE);
		label.setText("Section Name:");
		nameText = new Text(composite, SWT.BORDER);
		nameText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).hint(250, -1).create());

		// section sort type
		Label sortLabel = new Label(composite, SWT.NONE);
		sortLabel.setText("Sort Type:");
		sortTypeCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		sortTypeCombo.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		sortTypeCombo.setItems(new String[] { "Sort", "It", "Out" });

		initialize();
		Dialog.applyDialogFont(container);
		return container;
	}

	private void initialize() {
		if (nameText != null && sectionName != null) {
			nameText.setText(sectionName);
		}
		if (sortTypeCombo != null && sectionName != null) {
			// TODO: iterate thru choices, if match then set
			// otherwise what?
			sortTypeCombo.select(0);
		}
	}

	@Override
	protected void okPressed() {
		sectionName = nameText.getText();
		sortType = sortTypeCombo.getText();
		super.okPressed();
	}

	/**
	 * Return name of section or null if not specified
	 *
	 * @return name
	 */
	public String getSectionName() {
		return sectionName;
	}

	/**
	 * Set the name of the section
	 *
	 * @param sectionName
	 */
	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}

	/**
	 * Get the sort type or null if not specified
	 *
	 * @return sortType
	 */
	public String getSortType() {
		return sortType;
	}

	/**
	 * Set the sort Type
	 *
	 * @param sortType
	 */
	public void setSortType(String sortType) {
		this.sortType = sortType;
	}

}
