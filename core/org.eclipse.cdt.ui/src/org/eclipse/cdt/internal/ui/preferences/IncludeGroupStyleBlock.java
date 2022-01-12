/*******************************************************************************
 * Copyright (c) 2013, 2014 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import java.util.ArrayList;

import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
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
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

/**
 * The preference block for configuring styles of different categories of include statements.
 */
public class IncludeGroupStyleBlock extends OptionsConfigurationBlock {
	private final String description;
	private IncludeGroupStyle style;
	private final ArrayList<Button> checkBoxes = new ArrayList<>();
	private final ArrayList<Text> textBoxes = new ArrayList<>();
	private PixelConverter pixelConverter;
	private Button checkBoxBlankLine;
	private static final Key[] EMPTY_KEY_ARRAY = {};

	public IncludeGroupStyleBlock(IStatusChangeListener context, IProject project,
			IWorkbenchPreferenceContainer container, String description) {
		super(context, project, EMPTY_KEY_ARRAY, container);
		this.description = description;
	}

	public IncludeGroupStyle getStyle() {
		return style;
	}

	public void setStyle(IncludeGroupStyle style) {
		this.style = style;
	}

	@Override
	protected Control createContents(Composite parent) {
		pixelConverter = new PixelConverter(parent);

		setShell(parent.getShell());

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());

		GridLayout layout = new GridLayout();
		layout.marginHeight = pixelConverter.convertHeightInCharsToPixels(1);
		layout.marginWidth = 0;
		composite.setLayout(layout);
		composite.setFont(parent.getFont());

		Group group = ControlFactory.createGroup(composite, description, 1);
		group.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite envelope = new Composite(group, SWT.NONE);
		layout = new GridLayout(4, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		envelope.setLayout(layout);

		addCheckBox(envelope, PreferencesMessages.IncludeGroupStyleBlock_keep_includes_together, 0,
				new BooleanDataSource() {
					@Override
					public boolean get() {
						return style.isKeepTogether();
					}

					@Override
					public void set(boolean value) {
						style.setKeepTogether(value);
					}
				});
		checkBoxBlankLine = addCheckBox(envelope, PreferencesMessages.IncludeGroupStyleBlock_blank_line_before,
				pixelConverter.convertHorizontalDLUsToPixels(10), new BooleanDataSource() {
					@Override
					public boolean get() {
						return style.isBlankLineBefore();
					}

					@Override
					public void set(boolean value) {
						style.setBlankLineBefore(value);
					}
				});
		if (!style.getIncludeKind().hasChildren()) {
			addCheckBox(envelope, PreferencesMessages.IncludeGroupStyleBlock_use_relative_path, 0,
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
			addCheckBox(envelope, PreferencesMessages.IncludeGroupStyleBlock_use_angle_brackets, 0,
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
		updateDependent();
		return composite;
	}

	private Button addCheckBox(Composite parent, String label, int indent, BooleanDataSource dataSource) {
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 3;
		gd.horizontalIndent = indent;

		Button checkBox = new Button(parent, SWT.CHECK);
		checkBox.setFont(JFaceResources.getDialogFont());
		checkBox.setText(label);
		checkBox.setData(dataSource);
		checkBox.setLayoutData(gd);
		checkBox.addSelectionListener(getSelectionListener());

		makeScrollableCompositeAware(checkBox);

		checkBox.setSelection(dataSource.get());

		checkBoxes.add(checkBox);

		return checkBox;
	}

	@Override
	protected void updateControls() {
		super.updateControls();
		for (int i = checkBoxes.size() - 1; i >= 0; i--) {
			updateCheckBox(checkBoxes.get(i));
		}
	}

	@Override
	protected void updateCheckBox(Button checkBox) {
		BooleanDataSource dataSource = (BooleanDataSource) checkBox.getData();
		checkBox.setSelection(dataSource.get());
	}

	@Override
	protected void controlChanged(Widget widget) {
		if (widget instanceof Button) {
			BooleanDataSource dataSource = (BooleanDataSource) widget.getData();
			dataSource.set(((Button) widget).getSelection());
		}
		updateDependent();
	}

	private void updateDependent() {
		checkBoxBlankLine.setEnabled(style.isKeepTogether());
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
