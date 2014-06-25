/*******************************************************************************
 * Copyright (c) 2009, 2011 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.properties;

import java.util.ArrayList;

import org.eclipse.cdt.internal.autotools.core.configure.AutotoolsConfiguration;
import org.eclipse.cdt.internal.autotools.core.configure.IAConfiguration;
import org.eclipse.cdt.internal.autotools.core.configure.IConfigureOption;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class AutotoolsCategoryPropertyOptionPage extends
		AbstractConfigurePropertyOptionsPage {

	private String catName = "";
	private IAConfiguration cfg;
	//  Label class for a preference page.
	static class LabelFieldEditor extends FieldEditor {
		private String fTitle;
		private Label fTitleLabel;

		public LabelFieldEditor( Composite parent, String title ) {
			fTitle = title;
			this.createControl( parent );
		}

		protected void adjustForNumColumns( int numColumns ) {
			((GridData)fTitleLabel.getLayoutData()).horizontalSpan = 2;
		}

		protected void doFillIntoGrid( Composite parent, int numColumns ) {
			fTitleLabel = new Label( parent, SWT.WRAP );
			fTitleLabel.setText( fTitle );
			GridData gd = new GridData();
			gd.verticalAlignment = SWT.TOP;
			gd.grabExcessHorizontalSpace = false;
			gd.horizontalSpan = 2;
			fTitleLabel.setLayoutData( gd );
		}

		public int getNumberOfControls() {	return 1; }
		/**
		 * The label field editor is only used to present a text label on a preference page.
		 */
		protected void doLoad() {}
		protected void doLoadDefault() {}
		protected void doStore() {}
	}
	
	private ArrayList<FieldEditor> fieldEditors;
	
	public AutotoolsCategoryPropertyOptionPage(ToolListElement element, IAConfiguration cfg) {
 		super(element.getName());
 		this.catName = element.getName();
 		this.cfg = cfg;
 		fieldEditors = new ArrayList<FieldEditor>();
	}

	public String getName() {
		return super.getName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {
		super.createFieldEditors();
		Composite parent = getFieldEditorParent();
//		FontMetrics fm = AbstractCPropertyTab.getFontMetrics(parent);
		AutotoolsConfiguration.Option[] options = AutotoolsConfiguration.getChildOptions(catName);
		for (int i = 0; i < options.length; ++i) {
			AutotoolsConfiguration.Option option = options[i];
			switch (option.getType()) {
			case IConfigureOption.STRING:
			case IConfigureOption.INTERNAL:
			case IConfigureOption.MULTIARG:
				parent = getFieldEditorParent();
				StringFieldEditor f = new StringFieldEditor(option.getName(), option.getDescription(), 20, parent);
				f.getLabelControl(parent).setToolTipText(option.getToolTip());
				addField(f);
				fieldEditors.add(f);
				break;
			case IConfigureOption.BIN:
			case IConfigureOption.FLAGVALUE:
				parent = getFieldEditorParent();
				BooleanFieldEditor b = new BooleanFieldEditor(option.getName(), option.getDescription(), parent);
				b.getDescriptionControl(parent).setToolTipText(option.getToolTip());
				addField(b);
				fieldEditors.add(b);
				break;
			case IConfigureOption.FLAG:
				parent = getFieldEditorParent();
				FieldEditor l = createLabelEditor(parent, option.getDescription());
				addField(l);
				fieldEditors.add(l);
				break;
			}
		}
	}

	protected FieldEditor createLabelEditor( Composite parent, String title ) {
		return new LabelFieldEditor( parent, title );
	}

	/**
	 * Update the field editor that displays all the build options
	 */
	public void updateFields() {
		setValues();
	}
	
	public void setValues() {
		for (int i = 0; i < fieldEditors.size(); ++i) {
			fieldEditors.get(i).load();
		}
	}
	
	public void propertyChange(PropertyChangeEvent event) {
		// allow superclass to handle as well
		super.propertyChange(event);

		if (event.getSource() instanceof StringFieldEditor) {
			StringFieldEditor f = (StringFieldEditor)event.getSource();
			cfg.setOption(f.getPreferenceName(), f.getStringValue());
		} else if (event.getSource() instanceof BooleanFieldEditor) {
			BooleanFieldEditor b = (BooleanFieldEditor)event.getSource();
			cfg.setOption(b.getPreferenceName(), Boolean.toString(b.getBooleanValue()));
		}
	}
	
}
