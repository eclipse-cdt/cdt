/*******************************************************************************
 * Copyright (c) 2009, 2016 Red Hat Inc.
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.internal.autotools.core.configure.AutotoolsConfiguration;
import org.eclipse.cdt.internal.autotools.core.configure.IAConfiguration;
import org.eclipse.cdt.internal.autotools.core.configure.IConfigureOption;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class AutotoolsCategoryPropertyOptionPage extends AbstractConfigurePropertyOptionsPage {

	private static final int MARGIN = 3;
	private String catName = "";
	private IAConfiguration cfg;

	//  Label class for a preference page.
	static class LabelFieldEditor extends FieldEditor {
		private String fTitle;
		private Label fTitleLabel;

		public LabelFieldEditor(Composite parent, String title) {
			fTitle = title;
			this.createControl(parent);
		}

		@Override
		protected void adjustForNumColumns(int numColumns) {
			((GridData) fTitleLabel.getLayoutData()).horizontalSpan = 2;
		}

		@Override
		protected void doFillIntoGrid(Composite parent, int numColumns) {
			fTitleLabel = new Label(parent, SWT.WRAP);
			fTitleLabel.setText(fTitle);
			GridData gd = new GridData();
			gd.verticalAlignment = SWT.TOP;
			gd.grabExcessHorizontalSpace = false;
			gd.horizontalSpan = 2;
			fTitleLabel.setLayoutData(gd);
		}

		@Override
		public int getNumberOfControls() {
			return 1;
		}

		/**
		 * The label field editor is only used to present a text label on a preference page.
		 */
		@Override
		protected void doLoad() {
		}

		@Override
		protected void doLoadDefault() {
		}

		@Override
		protected void doStore() {
		}
	}

	static class VariableListEditor extends ListEditor {
		Composite fParent;
		String fName;
		String fLabelText;
		boolean isLoaded;

		public VariableListEditor(String name, String labelText, Composite parent) {
			fName = name;
			fLabelText = labelText;
			fParent = parent;
			isLoaded = false;
			init(fName, fLabelText);
			createControl(fParent);
		}

		@Override
		protected void selectionChanged() {
			super.selectionChanged();
			super.fireValueChanged(getPreferenceName(), "", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}

		@Override
		protected String createList(String[] arg0) {
			StringBuilder sb = new StringBuilder();
			for (String item : arg0) {
				sb.append(item);
				sb.append("\\s"); //$NON-NLS-1$
			}
			return sb.toString();
		}

		@Override
		protected void doLoad() {
			if (!isLoaded) {
				super.doLoad();
				isLoaded = true;
			}
		}

		public void setToolTipText(String toolTip) {
			this.getLabelControl().setToolTipText(toolTip);
		}

		/**
		 * Dialog user inputs variable's name and value.
		 */
		class DialogNewVar extends Dialog {
			private String name;
			private Text fTextName;
			private String value;
			private Text fTextValue;
			private Button fOkButton;

			public DialogNewVar(Shell shell) {
				super(shell);
			}

			@Override
			protected void configureShell(Shell newShell) {
				super.configureShell(newShell);
				newShell.setText(AutotoolsPropertyMessages.getString("NewEnvVarDialog.title"));
			}

			@Override
			protected Control createDialogArea(Composite parent) {
				Composite composite = new Composite(parent, SWT.NONE);
				GridLayout layout = new GridLayout(2, false);
				layout.marginWidth = 5;
				layout.numColumns = 2;
				composite.setLayout(layout);

				GC gc = new GC(composite);
				gc.setFont(composite.getFont());
				FontMetrics metrics = gc.getFontMetrics();
				gc.dispose();
				int fieldWidthHint = convertWidthInCharsToPixels(metrics, 50);

				Label label = new Label(composite, SWT.NONE);
				label.setText(AutotoolsPropertyMessages.getString("NewEnvVarDialog.name_field"));
				fTextName = new Text(composite, SWT.SINGLE | SWT.BORDER);
				GridData gd = new GridData(GridData.FILL_BOTH);
				gd.grabExcessHorizontalSpace = true;
				gd.widthHint = fieldWidthHint;
				fTextName.setLayoutData(gd);
				// Name field cannot be empty.
				fTextName.addModifyListener(new ModifyListener() {

					@Override
					public void modifyText(ModifyEvent e) {
						if (fOkButton != null) {
							fOkButton.setEnabled(fTextName.getText().length() > 0);
						}
					}
				});
				label = new Label(composite, SWT.NONE);
				label.setText(AutotoolsPropertyMessages.getString("NewEnvVarDialog.value_field"));
				fTextValue = new Text(composite, SWT.SINGLE | SWT.BORDER);
				gd = new GridData(GridData.FILL_BOTH);
				gd.grabExcessHorizontalSpace = true;
				gd.widthHint = fieldWidthHint;
				fTextValue.setLayoutData(gd);
				return composite;
			}

			// Obtain instance of OK button and set disabled.
			@Override
			protected Control createButtonBar(Composite parent) {
				Control control = super.createButtonBar(parent);
				fOkButton = getButton(IDialogConstants.OK_ID);
				fOkButton.setEnabled(false);
				return control;
			}

			@Override
			protected void okPressed() {
				name = fTextName.getText().trim();
				value = fTextValue.getText();
				if (value != null) {
					value = value.trim();
				} else {
					value = ""; //$NON-NLS-1$
				}
				super.okPressed();
			}

			public String getName() {
				return name;
			}

			public String getValue() {
				return value;
			}
		}

		@Override
		protected String getNewInputObject() {
			DialogNewVar newDialog = new DialogNewVar(getShell());
			newDialog.open();
			String name = newDialog.getName();

			// Create quoted string like CFLAGS="-q -O3"
			if (name != null) {
				String quote = "\""; //$NON-NLS-1$
				StringBuilder sb = new StringBuilder(name.trim());
				sb.append("="); //$NON-NLS-1$
				String value = newDialog.getValue();
				if (value != null) {
					value = value.trim();
					if (value.length() == 0) {
						// Check empty value
						sb.append(quote);
						sb.append(quote);
					} else if (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
						// Check user has already quoted it.
						sb.append(value);
					} else {
						sb.append(quote);
						sb.append(value);
						sb.append(quote);
					}
				}
				return sb.toString();
			}
			return null;
		}

		/*
		 * Expect string with format: VAR1="VALUE1" VAR2="VALUE2". Count quotes
		 * to mark end of a variable.
		 *
		 * @see
		 * org.eclipse.jface.preference.ListEditor#parseString(java.lang.String)
		 */
		@Override
		protected String[] parseString(String str) {
			if (str == null) {
				return new String[] {};
			}
			ArrayList<String> variables = new ArrayList<>();
			StringBuilder sb = new StringBuilder();
			int i = 0;
			int quote = 0; // 0 = begin variable,
							// 1 = looking for end of variable.
			while (i < str.length()) {
				char c = str.charAt(i);
				sb.append(c);
				if (c == '"') {
					quote++;
				}
				if (quote == 2) {
					// Found end of variable.
					quote = 0;
					variables.add(sb.toString());
					sb.delete(0, sb.length());
					i++; // Skip whitespace char separating variables.
				}
				i++;
			}

			return variables.toArray(new String[0]);
		}

		/**
		 * Get the list of environment variables in a single line.
		 *
		 * @return environment variables
		 */
		public String getVariablesValue() {
			org.eclipse.swt.widgets.List list = super.getList();
			StringBuilder sb = new StringBuilder();
			for (String var : list.getItems()) {
				sb.append(var);
				sb.append(" "); //$NON-NLS-1$
			}
			return sb.toString().trim();
		}
	}

	private List<FieldEditor> fieldEditors;

	public AutotoolsCategoryPropertyOptionPage(ToolListElement element, IAConfiguration cfg) {
		super(element.getName());
		this.catName = element.getName();
		this.cfg = cfg;
		fieldEditors = new ArrayList<>();
	}

	@Override
	protected void createFieldEditors() {
		super.createFieldEditors();
		Composite parent = getFieldEditorParent();

		// Add margin
		parent.setLayout(new GridLayout(1, false));
		Composite area = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout(1, false);
		gl.marginTop = MARGIN;
		gl.marginLeft = MARGIN;
		gl.marginRight = MARGIN;
		area.setLayout(gl);
		area.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		//		FontMetrics fm = AbstractCPropertyTab.getFontMetrics(parent);
		AutotoolsConfiguration.Option[] options = AutotoolsConfiguration.getChildOptions(catName);
		for (int i = 0; i < options.length; ++i) {
			AutotoolsConfiguration.Option option = options[i];
			switch (option.getType()) {
			case IConfigureOption.STRING:
			case IConfigureOption.INTERNAL:
			case IConfigureOption.MULTIARG:
				StringFieldEditor f = new StringFieldEditor(option.getName(), option.getDescription(), area);
				f.getLabelControl(area).setToolTipText(option.getToolTip());
				addField(f);
				fieldEditors.add(f);
				break;
			case IConfigureOption.BIN:
			case IConfigureOption.FLAGVALUE:
				BooleanFieldEditor b = new BooleanFieldEditor(option.getName(), option.getDescription(), area);
				b.getDescriptionControl(area).setToolTipText(option.getToolTip());
				addField(b);
				fieldEditors.add(b);
				break;
			case IConfigureOption.FLAG:
				FieldEditor l = new LabelFieldEditor(area, option.getDescription());
				addField(l);
				fieldEditors.add(l);
				break;
			case IConfigureOption.ENVVAR:
				VariableListEditor listEditor = new VariableListEditor(option.getName(), option.getDescription(), area);
				listEditor.setToolTipText(option.getToolTip());
				addField(listEditor);
				fieldEditors.add(listEditor);
				break;
			}
		}
	}

	/**
	 * Update the field editor that displays all the build options
	 */
	@Override
	public void updateFields() {
		setValues();
	}

	@Override
	public void setValues() {
		for (int i = 0; i < fieldEditors.size(); ++i) {
			fieldEditors.get(i).load();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		// allow superclass to handle as well
		super.propertyChange(event);

		if (event.getSource() instanceof StringFieldEditor) {
			StringFieldEditor f = (StringFieldEditor) event.getSource();
			cfg.setOption(f.getPreferenceName(), f.getStringValue());
		} else if (event.getSource() instanceof BooleanFieldEditor) {
			BooleanFieldEditor b = (BooleanFieldEditor) event.getSource();
			cfg.setOption(b.getPreferenceName(), Boolean.toString(b.getBooleanValue()));
		} else if (event.getSource() instanceof VariableListEditor) {
			VariableListEditor v = (VariableListEditor) event.getSource();
			cfg.setOption(v.getPreferenceName(), v.getVariablesValue());
		}
	}

}
