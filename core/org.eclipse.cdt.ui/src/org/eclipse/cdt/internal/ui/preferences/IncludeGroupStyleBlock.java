/*******************************************************************************
 * Copyright (c) 2013 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import org.eclipse.cdt.utils.ui.controls.ControlFactory;

import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle;

/**
 * The preference block for configuring styles of different categories of include statements.
 */
public class IncludeGroupStyleBlock extends OptionsConfigurationBlock {
	private final String description;
	private final Key groupingKey;
	private final IncludeGroupStyle style;
	@SuppressWarnings("hiding")
	private final ArrayList<Button> fCheckBoxes = new ArrayList<Button>();
	@SuppressWarnings("hiding")
	private final ArrayList<Text> fTextBoxes = new ArrayList<Text>();
	private PixelConverter pixelConverter;

	public IncludeGroupStyleBlock(IStatusChangeListener context, IProject project,
			IWorkbenchPreferenceContainer container, String description, Key groupingKey,
			IncludeGroupStyle style) {
		super(context, project, new Key[] { groupingKey }, container);
		this.description = description;
		this.groupingKey = groupingKey;
		this.style = style;
	}

	@Override
	protected Control createContents(Composite parent) {
		pixelConverter =  new PixelConverter(parent);

		setShell(parent.getShell());

		Composite composite =  new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());

		GridLayout layout = new GridLayout();
		layout.marginHeight = pixelConverter.convertHeightInCharsToPixels(1);
		layout.marginWidth = 0;
		composite.setLayout(layout);
		composite.setFont(parent.getFont());

		Group group = ControlFactory.createGroup(composite,	description, 1);
		group.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite envelope = new Composite(group, SWT.NONE);
		layout = new GridLayout(4, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		envelope.setLayout(layout);

		addCheckBox(envelope, PreferencesMessages.IncludeGroupStyleBlock_keep_includes_together,
				groupingKey, TRUE_FALSE, 0);
		if (style != null) {
			addCheckBox(envelope, PreferencesMessages.IncludeGroupStyleBlock_use_relative_path,
					new BooleanDataSource() {
				@Override
				public boolean get() {
					return style.isRelativePath();
				}

				@Override
				public void set(boolean value) {
					style.setRelativePath(value);
				}
			});
			addCheckBox(envelope, PreferencesMessages.IncludeGroupStyleBlock_use_angle_brackets,
					new BooleanDataSource() {
				@Override
				public boolean get() {
					return style.isAngleBrackets();
				}

				@Override
				public void set(boolean value) {
					style.setAngleBrackets(value);
				}
			});
		}

		updateControls();
		return composite;
	}

	private Button addCheckBox(Composite parent, String label, BooleanDataSource dataSource) {
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 3;

		Button checkBox= new Button(parent, SWT.CHECK);
		checkBox.setFont(JFaceResources.getDialogFont());
		checkBox.setText(label);
		checkBox.setData(dataSource);
		checkBox.setLayoutData(gd);
		checkBox.addSelectionListener(getSelectionListener());

		makeScrollableCompositeAware(checkBox);

		checkBox.setSelection(dataSource.get());

		fCheckBoxes.add(checkBox);

		return checkBox;
	}

	@Override
	protected void updateControls() {
		super.updateControls();
		// XXX Implement
	}

	@Override
	public void performDefaults() {
		super.performDefaults();
		// XXX Implement
	}

	@Override
	public boolean performOk() {
		return super.performOk();
		// XXX Implement
	}

	@Override
	protected void validateSettings(Key changedKey, String oldValue, String newValue) {
		StatusInfo status = new StatusInfo();
		fContext.statusChanged(status);
	}

	private interface BooleanDataSource {
		boolean get();
		void set(boolean value);
	}

	private interface StringDataSource {
		String get();
		void set(String value);
	}
}
