/*******************************************************************************
 * Copyright (c) 2009, 2015 Red Hat Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.properties;

import org.eclipse.cdt.internal.autotools.core.configure.ConfigureMessages;
import org.eclipse.cdt.internal.autotools.core.configure.IAConfiguration;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.cdt.ui.newui.MultiLineTextFieldEditor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class AutotoolsToolPropertyOptionPage extends AbstractConfigurePropertyOptionsPage {

	private static final int MARGIN = 3;
	private String toolName = "";
	private IAConfiguration cfg;

	public AutotoolsToolPropertyOptionPage(ToolListElement element, IAConfiguration cfg) {
		super(element.getName());
		this.toolName = element.getName();
		this.cfg = cfg;
	}

	@Override
	protected void createFieldEditors() {
		super.createFieldEditors();
		// Add a string editor to edit the tool command
		Composite parent = getFieldEditorParent();
		parent.setLayout(new GridLayout(1, false));
		Composite area = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout(1, false);
		gl.marginTop = MARGIN;
		gl.marginLeft = MARGIN;
		gl.marginRight = MARGIN;
		area.setLayout(gl);
		area.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		FontMetrics fm = AbstractCPropertyTab.getFontMetrics(area);
		commandStringField = new StringFieldEditor(toolName, ConfigureMessages.getString(COMMAND), area);
		commandStringField.setEmptyStringAllowed(false);
		GridData gd = ((GridData) commandStringField.getTextControl(area).getLayoutData());
		gd.grabExcessHorizontalSpace = true;
		gd.minimumWidth = Dialog.convertWidthInCharsToPixels(fm, 3);
		addField(commandStringField);
		// Add a field editor that displays overall build options
		allOptionFieldEditor = new MultiLineTextFieldEditor(AutotoolsConfigurePrefStore.ALL_OPTIONS_ID,
				ConfigureMessages.getString(ALL_OPTIONS), area);
		allOptionFieldEditor.getTextControl(area).setEditable(false);
		//		gd = ((GridData)allOptionFieldEditor.getTextControl().getLayoutData());
		gd.grabExcessHorizontalSpace = true;
		gd.minimumWidth = Dialog.convertWidthInCharsToPixels(fm, 20);
		addField(allOptionFieldEditor);
	}

	// field editor that displays all the build options for a particular tool
	private MultiLineTextFieldEditor allOptionFieldEditor;
	//tool command field
	private StringFieldEditor commandStringField;
	// all build options field editor label
	private static final String ALL_OPTIONS = "Tool.allopts"; //$NON-NLS-1$
	// Field editor label for tool command
	private static final String COMMAND = "Tool.command"; //$NON-NLS-1$

	/**
	 * Update the field editor that displays all the build options
	 */
	@Override
	public void updateFields() {
		allOptionFieldEditor.load();
	}

	@Override
	public void setValues() {
		commandStringField.load();
		updateFields();
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		// allow superclass to handle as well
		super.propertyChange(event);

		if (event.getSource() == commandStringField) {
			cfg.setOption(toolName, commandStringField.getStringValue());
			updateFields();
		}
	}

}
