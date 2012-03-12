/*******************************************************************************
 * Copyright (c) 2009 Red Hat Inc.
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class AutotoolsToolPropertyOptionPage extends
		AbstractConfigurePropertyOptionsPage {

	private ToolListElement element;
	private String toolName = "";
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
	
	public AutotoolsToolPropertyOptionPage(ToolListElement element, IAConfiguration cfg) {
 		super(element.getName());
 		this.element = element;
 		this.toolName = element.getName();
 		this.cfg = cfg;
	}

	public String getName() {
		return super.getName();
	}

	public ToolListElement getElement() {
		return element;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {
		super.createFieldEditors();
		// Add a string editor to edit the tool command
		Composite parent = getFieldEditorParent();
		FontMetrics fm = AbstractCPropertyTab.getFontMetrics(parent);
		commandStringField = new StringFieldEditor(toolName,
				ConfigureMessages.getString(COMMAND),
				parent);
		commandStringField.setEmptyStringAllowed(false);
		GridData gd = ((GridData)commandStringField.getTextControl(parent).getLayoutData());
		gd.grabExcessHorizontalSpace = true;
		gd.minimumWidth = Dialog.convertWidthInCharsToPixels(fm, 3);
		addField(commandStringField);
		// Add a field editor that displays overall build options
		Composite par = getFieldEditorParent();
		allOptionFieldEditor = new MultiLineTextFieldEditor(AutotoolsConfigurePrefStore.ALL_OPTIONS_ID,
				ConfigureMessages.getString(ALL_OPTIONS), par);
		allOptionFieldEditor.getTextControl(par).setEditable(false);
//		gd = ((GridData)allOptionFieldEditor.getTextControl().getLayoutData());
		gd.grabExcessHorizontalSpace = true;
		gd.minimumWidth = Dialog.convertWidthInCharsToPixels(fm, 20);
		addField(allOptionFieldEditor);
	}

	protected FieldEditor createLabelEditor( Composite parent, String title ) {
		return new LabelFieldEditor( parent, title );
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
	public void updateFields() {
		allOptionFieldEditor.load();
	}
	
	public void setValues(){
		commandStringField.load();
		updateFields();
	}
	
	public void propertyChange(PropertyChangeEvent event) {
		// allow superclass to handle as well
		super.propertyChange(event);

		if(event.getSource() == commandStringField){
			cfg.setOption(toolName, commandStringField.getStringValue());
			updateFields();
		}
	}
	
}
