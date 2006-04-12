/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui;
import java.util.Locale;

import org.eclipse.jface.action.IAction;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.ui.widgets.InheritableEntryField;
import org.eclipse.rse.ui.widgets.SystemHistoryCombo;
import org.eclipse.rse.ui.widgets.SystemHostCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

/**
 * Static methods that can be used when writing SWT GUI code.
 * They simply make it more productive.
 */
public class SystemWidgetHelpers {
	public static boolean traceHelpIDs = false;
	public static Label previousLabel;

	/**
	 * This is the most flexible composite creation method.
	 * @param parent Parent composite
	 * @param parentSpan number of columns this is to span in the parent's composite
	 * @param numColumns number of columns this composite is to have
	 * @param border true if you want to show an etched border around the composite
	 * @param label optional label to show in the border. Forces border to true
	 * @param marginSize the number pixels around the composite. -1 means Eclipse default
	 * @param spacingSize the number pixels around the composite controls. -1 means Eclipse default
	 */
	public static Composite createComposite(Composite parent, int parentSpan, int numColumns, boolean border, String label, int marginSize, int spacingSize) {
		return createAlignedComposite(parent, parentSpan, numColumns, border, label, marginSize, spacingSize, GridData.FILL);
	}

	/**
	 * Creates a composite with vertical alignment GridData.VERTICAL_ALIGN_BEGINNING.
	 * @param parent Parent composite
	 * @param parentSpan number of columns this is to span in the parent's composite
	 * @param numColumns number of columns this composite is to have
	 * @param border true if you want to show an etched border around the composite
	 * @param label optional label to show in the border. Forces border to true
	 * @param marginSize the number pixels around the composite. -1 means Eclipse default
	 * @param spacingSize the number pixels around the composite controls. -1 means Eclipse default
	 */
	public static Composite createVerticalBeginComposite(Composite parent, int parentSpan, int numColumns, boolean border, String label, int marginSize, int spacingSize) {
		return createAlignedComposite(parent, parentSpan, numColumns, border, label, marginSize, spacingSize, GridData.VERTICAL_ALIGN_BEGINNING);
	}

	/**
	 * Created a composite
	 */
	private static Composite createAlignedComposite(Composite parent, int parentSpan, int numColumns, boolean border, String label, int marginSize, int spacingSize, int verticalAlignment) {
		//border = true;
		boolean borderNeeded = border;
		if (label != null)
			borderNeeded = true; // force the case
		int style = SWT.NULL;
		if (borderNeeded)
			style |= SWT.SHADOW_ETCHED_IN;
		Composite composite = null;
		if (borderNeeded) {
			composite = new Group(parent, style);
			if (label != null)
				 ((Group) composite).setText(label);
		} else {
			composite = new Composite(parent, style);
		}
		//GridLayout
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		if (marginSize != -1) {
			layout.marginWidth = 0;
			layout.marginHeight = 0;
		}
		if (spacingSize != -1) {
			layout.horizontalSpacing = 0;
			layout.verticalSpacing = 0;
		}
		composite.setLayout(layout);
		//GridData
		GridData data = new GridData();
		data.horizontalSpan = parentSpan;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;

		data.verticalAlignment = verticalAlignment;
		data.grabExcessVerticalSpace = false;

		composite.setLayoutData(data);
		return composite;
	}

	/**
	 * Creates composite control and sets the default layout data.
	 * @param GridLayout composite to put the new group composite into.     
	 * @param Number of columns the new group will contain.     
	 */
	public static Composite createComposite(Composite parent, int numColumns) {
		boolean testing = false; //true;
		if (testing)
			return createComposite(parent, 1, numColumns, true, Integer.toString(numColumns), -1, -1);
		else
			return createComposite(parent, 1, numColumns, false, null, -1, -1);
	}

	/**
	 * Creates group composite control and sets the default layout data.
	 * Group composites show a visible border line and optional text in it.
	 * @param GridLayout composite to put the new group composite into.     
	 * @param Number of columns the new group will contain.
	 * @param Text to display in the group border. Can be null.
	 */
	public static Group createGroupComposite(Composite parent, int numColumns, String label) {
		return (Group) createComposite(parent, 1, numColumns, true, label, -1, -1);
	}

	/**
	 * Creates "tight" composite control and sets the default layout data.
	 * A tight composite is one with no vertical or horizontal spacing, or margin spacing.
	 * @param GridLayout composite to put the new group composite into.     
	 * @param Number of columns the new group will contain.     
	 */
	public static Composite createTightComposite(Composite parent, int numColumns) {
		return createComposite(parent, 1, numColumns, false, null, 0, 0);
	}

	/**
	 * Creates "flush" composite control and sets the default layout data.
	 * A flush composite is one with no margin spacing but normal inter-component spacing
	 * @param GridLayout composite to put the new group composite into.     
	 * @param Number of columns the new group will contain.     
	 */
	public static Composite createFlushComposite(Composite parent, int numColumns) {
		return createComposite(parent, 1, numColumns, false, null, 0, -1);
	}

	/**
	 * Creates a label for use a simple filler, to eat up space. This is for a rigid
	 *  filler that doesn't consume space.
	 * @param parent Composite to put the field into.
	 * @param span Horizontal span
	 * @param widthHint How wide to make it. Pass -1 to use SWT default
	 */
	public static Label createRigidFillerLabel(Composite parent, int span, int widthHint) {
		Label label = new Label(parent, SWT.CENTER);
		label.setText(" ");
		GridData data = new GridData();
		data.horizontalSpan = span;
		//System.out.println("in createRigidFillerLabel. span = " + span);
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = false;
		if (widthHint != -1)
			data.widthHint = widthHint;
		//else
		//  data.widthHint = 5;
		label.setLayoutData(data);
		return label;
	}

	/**
	 * Creates a label instance and inserts it into a given GridLayout.
	 * @param GridLayout composite to put the field into.
	 * @param Text to display in the label.     
	 */
	public static Label createLabel(Composite parent, String text) {
		return createLabel(parent, text, 1);
	}

	/**
	 * Creates a label instance and inserts it into a given GridLayout.
	 * @param parent Composite to put the field into.
	 * @param text Text to display in the label.     
	 * @param span Horizontal span
	 */
	public static Label createLabel(Composite parent, String text, int span) {
		return createLabel(parent, text, span, false);
	}

	/**
	 * Creates a label instance and inserts it into a given GridLayout, optionally
	 *  with a border style
	 * @param parent Composite to put the field into.
	 * @param text Text to display in the label.     
	 * @param span Horizontal span
	 * @param wantBorder true to place border around the label
	 */
	public static Label createLabel(Composite parent, String text, int span, boolean wantBorder) {
		int style = SWT.LEFT;
		if (wantBorder)
			style |= SWT.BORDER;
		if (span > 1)
			style |= SWT.WRAP;
		Label label = new Label(parent, style);
		label.setText(text);
		GridData data = new GridData();
		data.horizontalSpan = span;
		data.horizontalAlignment = GridData.FILL;
		//data.grabExcessHorizontalSpace = true;
		label.setLayoutData(data);
		return label;
	}

	/**
	 * Creates a label instance and inserts it into a given GridLayout. Supports tooltip text
	 * @param parent GridLayout composite to put the field into.
	 * @param label
	 * @param tooltip
	 */
	public static Label createLabel(Composite parent, String text, String tooltip) 
	{
		Label label = createLabel(parent, text);
		setToolTipText(label, tooltip);
		return label;
	}

	/**
	 * Creates a label instance and inserts it into a given GridLayout.
	 * @param parent Composite to put the field into.
	 * @param text
	 * @param tooltip
	 * @param span Horizontal span
	 * @param wantBorder true to place border around the label
	 */
	public static Label createLabel(Composite parent, String text, String tooltip, int span, boolean wantBorder) 
	{
		Label label = createLabel(parent, text, span, wantBorder);
		setToolTipText(label, tooltip);
		return label;
	}

	/**
	 * Create a pair of labels, the first being a prompt and the second being a value. A colon
	 *  is appended to the text of the first label. The text value of the second will be set to "".
	 * <p>
	 * The first label is set to not grab excess horizontal space, while the second one is, since
	 *   its contents are variable. 
	 * <p>
	 * To help with initial sizing, the widthHint of the second is set to 100.
	 * <p>
	 * If you need a handle to the prompting label, immediately call {@link #getLastLabel()}
	 * 
	 * @param parent composite to put the fields into. Will be added sequentially
	 * @param label
	 * @param tooltip
	 * @param wantBorder true if a border is desired around the second label (the value vs the prompt)
	 * @return the second label created. Use setText to place the value in it.
	 */
	public static Label createLabeledLabel(Composite parent, String label, String tooltip, boolean wantBorder) {
		previousLabel = createLabel(parent, label);
		String text = previousLabel.getText();
		previousLabel.setText(appendColon(text));
		((GridData) previousLabel.getLayoutData()).grabExcessHorizontalSpace = false;
		Label label2 = createLabel(parent, "", 1, wantBorder);
		((GridData) label2.getLayoutData()).grabExcessHorizontalSpace = true;
		((GridData) label2.getLayoutData()).widthHint = 100;
		setToolTipText(label2, tooltip);
		return label2;
	}

	/**
	 * Return the prompting label from the last call to createLabeledXXXX.
	 * These methods only return the second control, but we sometimes need access to the label.
	 */
	public static Label getLastLabel() {
		return previousLabel;
	}

	/**
	 * Create a spacer line. No widget returned so we have the freedom to change it over time
	 */
	public static void createSpacerLine(Composite parent, int columnSpan, boolean wantBorder) {
		int style = SWT.LEFT; // | SWT.SEPARATOR;
		if (wantBorder)
			style |= SWT.BORDER | SWT.LINE_SOLID;
		if (columnSpan > 1)
			style |= SWT.WRAP;
		Label label = new Label(parent, style);
		//label.setText(text);
		GridData data = new GridData();
		data.horizontalSpan = columnSpan;
		data.horizontalAlignment = GridData.FILL;
		//data.grabExcessHorizontalSpace = true;
		label.setLayoutData(data);
	}

	

	/**
	 * Creates a widget for displaying text verbage that spans multiple lines. Takes resolved text vs resource bundle id.
	 * The returned widget is not typed so we can easily change it in the future if we decide on a better widget.
	 * @param parent Composite to put the field into.
	 * @param text String is the verbage text to display
	 * @param span Horizontal span
	 * @param border true if you want a border around the verbage
	 * @param widthHint number of pixels to limit width to before wrapping. 200 is a reasonable number
	 * @return the Label widget, in case you want to tweak it
	 */
	public static Label createVerbage(Composite parent, String text, int span, boolean border, int widthHint) {
		Label widget = new Label(parent, border ? (SWT.LEFT | SWT.WRAP | SWT.BORDER) : (SWT.LEFT | SWT.WRAP));
		widget.setText(text);
		GridData data = new GridData();
		data.horizontalSpan = span;
		data.horizontalAlignment = GridData.FILL;
		data.widthHint = widthHint;
		data.grabExcessHorizontalSpace = true;
		widget.setLayoutData(data);
		return widget;
	}

	/**
	 * Create a labeled verbage (wrappable label) field and insert it into a GridLayout, and assign tooltip text.
	 * After calling this, you must call setText on the result to set its contents.
	 * <p>
	 * If you need a handle to the prompting label, immediately call {@link #getLastLabel()}
	 *
	 * @param parent composite to put the field into.
	 * @param labelText
	 * @param tooltip
	 * @param span Horizontal span
	 * @param border true if you want a border around the verbage
	 * @param widthHint number of pixels to limit width to before wrapping. 200 is a reasonable number
	 * @return Label created.
	 */
	public static Label createLabeledVerbage(Composite parent, String labelText, String tooltip, int span, boolean border, int widthHint) {
		previousLabel = createLabel(parent, appendColon(labelText));
		Label verbage = createVerbage(parent, labelText, span, border, widthHint);
		setToolTipText(previousLabel, tooltip);
		setToolTipText(verbage, tooltip);
		return verbage;
	}

	/**
	 * Create a label to show a command string as it is being built-up in a dialog
	 * This version uses a default height of 3 normal lines.
	 */
	public static Label createCommandStatusLine(Composite parent, int horizontalSpan) {
		return createCommandStatusLine(parent, horizontalSpan, 3);
	}

	/**
	 * Create a label to show a command string as it is being built-up in a dialog.
	 * This version allows you specify how tall to make it, in terms of normal line height. 
	 */
	public static Label createCommandStatusLine(Composite parent, int horizontalSpan, int heightInLines) {
		Label commandSoFar = new Label(parent, SWT.LEFT | SWT.WRAP);
		int dx = commandSoFar.getBounds().height;
		//System.out.println("Default label height = " + dx); ALWAYS 0!
		if (dx == 0)
			//dx = 12; // what else?
			dx = 15; // d47377   
		GridData data = new GridData();
		data.horizontalSpan = horizontalSpan;
		data.horizontalAlignment = GridData.FILL;
		//data.widthHint = 300;
		data.heightHint = heightInLines * dx;
		data.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
		//data.grabExcessVerticalSpace = true;
		commandSoFar.setLayoutData(data);
		return commandSoFar;
	}

	/**
	 * Create a text field and insert it into a GridLayout.
	 * Assign the listener to the passed in implementer of Listener.
	 * @param GridLayout composite to put the field into.
	 * @param Listener object to listen for events. Can be null.
	 */
	public static Text createTextField(Composite parent, Listener listener) {
		Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
		if (listener != null)
			text.addListener(SWT.Modify, listener);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.widthHint = 150;
		data.verticalAlignment = GridData.CENTER;
		data.grabExcessVerticalSpace = false;
		text.setLayoutData(data);
		return text;
	}

	/**
	 * Create a text field and insert it into a GridLayout, and assign tooltip text.
	 * Assign the listener to the passed in implementer of Listener.
	 * @param parent composite to put the field into.
	 * @param listener object to listen for events. Can be null.
	 * @param tooltip tooltip text
	 */
	public static Text createTextField(Composite parent, Listener listener, String toolTip) {
		Text text = createTextField(parent, listener);
		setToolTipText(text, toolTip);
		return text;
	}

	/**
	 * Create a labeled text field and insert it into a GridLayout, and assign tooltip text.
	 * Assign the listener to the passed in implementer of Listener.
	 * <p>
	 * If you need a handle to the prompting label, immediately call {@link #getLastLabel()}
	 *
	 * @param parent composite to put the field into.
	 * @param listener object to listen for events. Can be null.
	 * @param labelText the label
	 * @param tooltip the tooltip
	 * @return TextField created.
	 */
	public static Text createLabeledTextField(Composite parent, Listener listener, String labelText, String tooltip) {
		previousLabel = createLabel(parent, appendColon(labelText));
		Text entry = createTextField(parent, listener, tooltip);
		setToolTipText(previousLabel, tooltip);
		return entry;
	}

	/**
	 * Create a readonly text field and insert it into a GridLayout.
	 * @param GridLayout composite to put the field into.
	 */
	public static Text createReadonlyTextField(Composite parent) {
		Text text = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		text.setEnabled(false);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.widthHint = 150; // defect 45789      
		data.verticalAlignment = GridData.CENTER;
		data.grabExcessVerticalSpace = false;
		text.setLayoutData(data);
		return text;
	}

	/**
	 * Create a readonly text field and insert it into a GridLayout,
	 *  and assign tooltip text.
	 * @param parent composite to put the field into.
	 * @param tooltip
	 */
	public static Text createReadonlyTextField(Composite parent, String toolTip) 
	{
		Text text = createReadonlyTextField(parent);
		setToolTipText(text, toolTip);
		return text;
	}

	/**
	 * Create a labeled readonly text field and insert it into a GridLayout, and assign tooltip text.
	 * Assign the listener to the passed in implementer of Listener.
	 * <p>
	 * If you need a handle to the prompting label, immediately call {@link #getLastLabel()}
	 *
	 * @param parent composite to put the field into.
	 * @param text the label
	 * @param tooltip the tooltip
	 * @return TextField created.
	 */
	public static Text createLabeledReadonlyTextField(Composite parent, String text, String tooltip) {
		previousLabel = createLabel(parent, appendColon(text));
		Text entry = createReadonlyTextField(parent, tooltip);
		setToolTipText(previousLabel, tooltip);
		return entry;
	}

	/**
	 * Create a multiline text field and insert it into a GridLayout.
	 * Assign the listener to the passed in implementer of Listener.
	 * @param GridLayout composite to put the field into.
	 * @param Listener object to listen for events. Can be null.
	 */
	public static Text createMultiLineTextField(Composite parent, Listener listener, int heightHint) {
		Text text = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);

		if (listener != null)
			text.addListener(SWT.Modify, listener);

		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.heightHint = heightHint;
		data.verticalAlignment = GridData.CENTER;
		data.grabExcessVerticalSpace = false;
		text.setLayoutData(data);
		return text;
	} // end createMultiLineTextField()

	/**
		 * Create a multiline labeled text field and insert it into a GridLayout, and assign tooltip text.
		 * Assign the listener to the passed in implementer of Listener.
		 * @param parent composite to put the field into.
		 * @param listener object to listen for events. Can be null.
		 * @param labelString the label
		 * @param tooltip the tooltip
		 * @return TextField created.
		 */
	public static Text createMultiLineLabeledTextField(Composite parent, Listener listener, String labelString, String tooltip, int heightHint) {
		Label label = createLabel(parent, appendColon(labelString));
		Text text = createMultiLineTextField(parent, listener, heightHint);
		setToolTipText(label, tooltip);
		return text;
	} // end createMultiLineLabeledTextField()

	/**
	 * Creates a new checkbox instance and sets the default
	 *  layout data. Spans 1 column horizontally.
	 * Assign the listener to the passed in implementer of Listener.
	 * @param group composite to put the checkbox into.
	 * @param label to display in the checkbox.
	 * @param listener object to listen for events. Can be null.     
	 */
	public static Button createCheckBox(Composite group, String label, Listener listener) {
		return createCheckBox(group, 1, label, listener);
	}

	/**
	 * Creates a new checkbox instance with the given horizontal span and sets the default
	 * layout data.
	 * Assign the listener to the passed in implementer of Listener.
	 * @param group composite to put the checkbox into.
	 * @param horizontalSpan number of columns this checkbox is to span.
	 * @param label to display in the checkbox.
	 * @param listener object to listen for events. Can be null.     
	 */
	public static Button createCheckBox(Composite group, int horizontalSpan, String label, Listener listener) {
		Button button = new Button(group, SWT.CHECK | SWT.LEFT);
		button.setText(label);
		if (listener != null)
			button.addListener(SWT.Selection, listener);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.horizontalSpan = horizontalSpan;
		button.setLayoutData(data);
		return button;
	}

	/**
	 * Creates a new checkbox instance and sets the default
	 * layout data, and sets the tooltip text.
	 * Assign the listener to the passed in implementer of Listener.
	 * @param group composite to put the checkbox into.
	 * @param listener object to listen for events. Can be null.     
	 * @param label the label
	 * @param tooltip the tooltip
	 */
	public static Button createCheckBox(Composite group, Listener listener, String label, String tooltip) 
	{
		Button button = createCheckBox(group, label, listener);
		setToolTipText(button, tooltip);
		return button;
	}

	/**
	 * Creates a new checkbox instance with the given horizontal span and sets the default
	 * layout data, and sets the tooltip text.
	 * Assign the listener to the passed in implementer of Listener.
	 * @param group composite to put the checkbox into.
	 * @param horizontalSpan number of columns to span. 
	 * @param listener object to listen for events. Can be null.     
	 * @param label the label
	 * @param tooltip the tooltip
	 */
	public static Button createCheckBox(Composite group, int horizontalSpan, Listener listener, String label, String tooltip) 
	{
		Button button = createCheckBox(group, horizontalSpan, label, listener);
		setToolTipText(button, tooltip);
		return button;
	}

	/**
	 * Creates a new radiobutton instance and sets the default
	 * layout data.
	 * Assign the listener to the passed in implementer of Listener.
	 * @param group composite to put the button into.
	 * @param label to display in the button
	 * @param listener object to listen for events. Can be null.          
	 */
	public static Button createRadioButton(Composite group, String label, Listener listener) {
		Button button = new Button(group, SWT.RADIO | SWT.LEFT);
		button.setText(label);
		if (listener != null)
			button.addListener(SWT.Selection, listener);
		GridData data = new GridData();
		// following 2 lines added in R2 by Phil, to be consistent with checkboxes
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		button.setLayoutData(data);
		return button;
	}

	/**
	 * Creates a new radiobutton instance and sets the default
	 * layout data, and assigns tooltip text.
	 * Assign the listener to the passed in implementer of Listener.
	 * @param group composite to put the button into.
	 * @param listener object to listen for events. Can be null.          
	 * @param label the label
	 * @param tooltip the tooltip
	 */
	public static Button createRadioButton(Composite group, Listener listener, String label, String tooltip) 
	{
		Button button = createRadioButton(group, label, listener);
		setToolTipText(button, tooltip);
		return button;
	}
	
	/**
	 * Creates a new radiobutton instance and sets the default
	 * layout data, and assigns tooltip text.
	 * Assign the listener to the passed in implementer of Listener.
	 * @param group composite to put the button into.
	 * @param listener object to listen for events. Can be null.          
	 * @param label the label
	 */
	public static Button createRadioButton(Composite group, Listener listener, String label) 
	{
		Button button = createRadioButton(group, label, listener);
		return button;
	}

	/**
	 * Creates a new pushbutton instance with an image, vs text.
	 * SWT does not allow both image and text on a button.
	 * Assign the listener to the passed in implementer of Listener.
	 * @param group The composite to put the button into.
	 * @param image The image to display in the button
	 * @param listener The object to listen for events. Can be null.               
	 */
	public static Button createImageButton(Composite group, Image image, Listener listener) {
		Button button = new Button(group, SWT.PUSH);
		button.setImage(image);
		if (listener != null)
			button.addListener(SWT.Selection, listener);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		button.setLayoutData(data);
		return button;
	}

	/**
	 * Creates a new pushbutton instance and sets the default
	 * layout data.
	 * Assign the listener to the passed in implementer of Listener.
	 * @param group composite to put the button into.
	 * @param label to display in the button
	 * @param listener object to listen for events. Can be null.               
	 */
	public static Button createPushButton(Composite group, String label, Listener listener) {
		Button button = new Button(group, SWT.PUSH);
		button.setText(label);
		if (listener != null)
			button.addListener(SWT.Selection, listener);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		button.setLayoutData(data);
		return button;
	}

	/**
	 * Creates a new pushbutton instance and sets the default
	 * layout data, and assign tooltip text
	 * Assign the listener to the passed in implementer of Listener.
	 * @param group composite to put the button into.
	 * @param label to display in the button
	 * @param listener object to listen for events. Can be null.               
	 * @param tooltip the tooltip
	 */
	public static Button createPushButton(Composite group, String label, Listener listener, String tooltip) {
		Button button = createPushButton(group, label, listener);
		setToolTipText(button, tooltip);
		return button;
	}

	/**
	 * This one takes the resource bundle key and appends "label" and "tooltip" to it to 
	 *  get the label and tooltip text.
	 * @param group composite to put the button into.
	 * @param listener object to listen for events. Can be null.               
	 * @param label the label
	 * @param tooltip the tooltip
	 */
	public static Button createPushButton(Composite group, Listener listener, String label, String tooltip) 
	{
		Button button = createPushButton(group, label, listener);
		setToolTipText(button, tooltip);
		return button;
	}

	/**
	 * Creates a new "Browse..." pushbutton instance and sets the default
	 * layout data.
	 * Assign the listener to the passed in implementer of Listener.
	 * @param group composite to put the button into.
	 * @param listener object to listen for events. Can be null.               
	 */
	public static Button createBrowseButton(Composite group, Listener listener) 
	{
		String label = SystemResources.BUTTON_BROWSE;
		return createPushButton(group, label, listener);
	}
	/**
	 * Creates a new "Browse..." pushbutton instance and sets the default
	 * layout data, with tooltip text.
	 * Assign the listener to the passed in implementer of Listener.
	 * @param group composite to put the button into.
	 * @param listener object to listen for events. Can be null.               
	 * @param bundle ResourceBundle of tooltip text
	 * @param id bundle key for tooltip text 
	 * @deprecated
	 */
	public static Button createBrowseButton(Composite group, Listener listener, String tooltip) {
		String label = SystemResources.BUTTON_BROWSE;
		return createPushButton(group, label, listener, tooltip);
	}

	/**
	 * Creates a new listbox instance and sets the default
	 * layout data.
	 * Assign the listener to the passed in implementer of Listener.
	 * @param group composite to put the button into.
	 * @param label to display above the list box (can be null).
	 * @param listener object to listen for events. Can be null.               
	 * @param multiSelect true if this is to be a multiple selection list. False for single selection.
	 */
	public static List createListBox(Composite group, String label, Listener listener, boolean multiSelect) {
		return createListBox(group, label, listener, multiSelect, 1);
	}

	/**
	 * Creates a new listbox instance and sets the default
	 * layout data.
	 * Assign the listener to the passed in implementer of Listener.
	 * @param group composite to put the button into.
	 * @param label to display above the list box (can be null).
	 * @param listener object to listen for events. Can be null.               
	 * @param multiSelect true if this is to be a multiple selection list. False for single selection.
	 * @param columnSpan number of columns this should span
	 */
	public static List createListBox(Composite group, String label, Listener listener, boolean multiSelect, int columnSpan) {
		Composite composite_list = null;
		if (label != null) {
			composite_list = createComposite(group, 1);
			((GridLayout) composite_list.getLayout()).marginWidth = 0;
			GridData data = new GridData();
			data.horizontalSpan = columnSpan;
			data.grabExcessVerticalSpace = true;
			data.verticalAlignment = GridData.FILL;
			data.grabExcessHorizontalSpace = true;
			data.horizontalAlignment = GridData.FILL;
			composite_list.setLayoutData(data);
			previousLabel = createLabel(composite_list, label);
		}
		int styles = SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER;
		List list = new List((composite_list != null) ? composite_list : group, multiSelect ? (SWT.MULTI | styles) : (SWT.SINGLE | styles));
		if (listener != null)
			list.addListener(SWT.Selection, listener);
		GridData data = new GridData();
		data.widthHint = 100;
		data.heightHint = 150;
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessVerticalSpace = true;
		data.verticalAlignment = GridData.FILL;
		list.setLayoutData(data);
		return list;
	}

	/**
	 * Creates a new listbox instance and sets the default
	 * layout data.
	 * Assign the listener to the passed in implementer of Listener.
	 * @param group composite to put the button into.
	 * @param label to display above the list box (can be null).
	 * @param listener object to listen for events. Can be null.               
	 * @param multiSelect true if this is to be a multiple selection list. False for single selection.
	 * @param tooltip the tooltip
	 */
	public static List createListBox(Composite group, String label, Listener listener, boolean multiSelect, String tooltip) {
		List list = createListBox(group, label, listener, multiSelect);
		setToolTipText(list, tooltip);
		return list;
	}

	/**
	 * Creates a new listbox instance and sets the default
	 * layout data.
	 * Assign the listener to the passed in implementer of Listener.
	 * @param group composite to put the button into.
	 * @param listener object to listen for events. Can be null.               
	 * @param multiSelect true if this is to be a multiple selection list. False for single selection.
	 * @param label the label
	 * @param tooltip the tooltip
	 */
	public static List createListBox(Composite group, Listener listener, boolean multiSelect, String label, String tooltip) {
		List list = createListBox(group, label, listener, multiSelect);
		setToolTipText(list, tooltip);
		return list;
	}

	/**
	 * Creates a new combobox instance and sets the default
	 * layout data.
	 * Assign the listener to the passed in implementer of Listener.
	 * @param parent composite to put the button into.
	 * @param listener object to listen for selection events. Can be null.          
	 *   If you want to listen for modify events, call addListener(SWT.Modify,this) on your own.
	 */
	public static Combo createCombo(Composite parent, Listener listener) {
		Combo combo = createCombo(parent, SWT.DROP_DOWN);
		if (listener != null)
			combo.addListener(SWT.Selection, listener);
		return combo;
	}

	/**
	 * private method for re-use
	 */
	private static Combo createCombo(Composite parent, int style) {
		Combo combo = new Combo(parent, style);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.widthHint = 150;
		data.verticalAlignment = GridData.CENTER;
		data.grabExcessVerticalSpace = false;
		combo.setLayoutData(data);
		return combo;
	}

	/**
	 * Creates a new combobox instance and sets the default
	 * layout data, with tooltip text.
	 * Assign the listener to the passed in implementer of Listener.
	 * @param parent composite to put the combo into.
	 * @param listener object to listen for selection events. Can be null.          
	 *   If you want to listen for modify events, call addListener(SWT.Modify,this) on your own.
	 * @param tooltip tooltip text 
	 */
	public static Combo createCombo(Composite parent, Listener listener, String toolTip) 
	{
		Combo combo = createCombo(parent, listener);
		setToolTipText(combo, toolTip);
		return combo;
	}

	/**
	 * Create a labeled combo field and insert it into a GridLayout, and assign tooltip text.
	 * Assign the listener to the passed in implementer of Listener.
	 * <p>
	 * If you need a handle to the prompting label, immediately call {@link #getLastLabel()}
	 *
	 * @param parent composite to put the field into.
	 * @param listener object to listen for selection events. Can be null.          
	 *   If you want to listen for modify events, call addListener(SWT.Modify,this) on your own.
	 * @param label the label text
	 * @param tooltip the tooltip for the combo field
	 * @return Combo created.
	 */
	public static Combo createLabeledCombo(Composite parent, Listener listener, String label, String tooltip) 
	{
		previousLabel = createLabel(parent, appendColon(label));
		Combo entry = createCombo(parent, listener, tooltip);
		setToolTipText(previousLabel, tooltip);
		return entry;
	}

	/**
	 * Creates a new readonly combobox instance and sets the default
	 * layout data.
	 * Assign the listener to the passed in implementer of Listener.
	 * @param parent composite to put the button into.
	 * @param listener object to listen for selection events. Can be null.          
	 *   If you want to listen for modify events, call addListener(SWT.Modify,this) on your own.
	 */
	public static Combo createReadonlyCombo(Composite parent, Listener listener) {
		Combo combo = createCombo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		if (listener != null)
			combo.addListener(SWT.Selection, listener);
		return combo;
	}

	/**
	 * Creates a new readonly combobox instance and sets the default
	 * layout data, with tooltip text.
	 * Assign the listener to the passed in implementer of Listener.
	 * @param parent composite to put the button into.
	 * @param listener object to listen for selection events. Can be null.          
	 *   If you want to listen for modify events, call addListener(SWT.Modify,this) on your own.
	 * @param tooltip
	 */
	public static Combo createReadonlyCombo(Composite parent, Listener listener, String tooltip) 
	{
		Combo combo = createReadonlyCombo(parent, listener);
		setToolTipText(combo, tooltip);
		return combo;
	}

	/**
	 * Create a labeled readonly combo field and insert it into a GridLayout, and assign tooltip text.
	 * Assign the listener to the passed in implementer of Listener.
	 * <p>
	 * If you need a handle to the prompting label, immediately call {@link #getLastLabel()}
	 *
	 * @param parent composite to put the field into.
	 * @param listener object to listen for selection events. Can be null.          
	 *   If you want to listen for modify events, call addListener(SWT.Modify,this) on your own.
	 * @param labelText the label
	 * @param tooltip the tooltip
	 * @return Combo created.
	 */
	public static Combo createLabeledReadonlyCombo(Composite parent, Listener listener, String labelText, String tooltip) 
	{
		labelText = appendColon(labelText);
		previousLabel = createLabel(parent, labelText);
		Combo entry = createReadonlyCombo(parent, listener, tooltip);
		setToolTipText(previousLabel, tooltip);
		return entry;
	}

	/**
	 * Creates a new historical combobox instance and sets the default
	 * layout data, with tooltip text.
	 * <p>
	 * Assign the listener to the passed in implementer of Listener.
	 * <p>
	 * A historical combobox is one that persists its contents between sessions. The management
	 *  of that persistence is handled for you!.
	 * <p>
	 * @param parent composite to put the combo into.
	 * @param listener object to listen for selection events. Can be null.          
	 *   If you want to listen for modify events, call addListener(SWT.Modify,this) on your own.
	 * @param historykey the preferences key (any unique string) to use to persist this combo's history
	 * @param readonly true if this combo is to be readonly, forcing user to select from the history
	 * @param tooltip the tooltip
	 */
	public static SystemHistoryCombo createHistoryCombo(Composite parent, SelectionListener listener, String historyKey, boolean readonly, String tooltip) 
	{
		SystemHistoryCombo combo = new SystemHistoryCombo(parent, SWT.NULL, historyKey, readonly);
		if (listener != null)
			combo.addSelectionListener(listener);
		boolean hasGridData = (combo.getLayoutData() != null) && (combo.getLayoutData() instanceof GridData);
		//System.out.println("history combo griddata non-null? " + hasGridData);
		int minwidth = 150;
		if (!hasGridData) {
			GridData data = new GridData();
			data.horizontalAlignment = GridData.FILL;
			data.grabExcessHorizontalSpace = true;
			data.widthHint = minwidth;
			data.verticalAlignment = GridData.CENTER;
			data.grabExcessVerticalSpace = false;
			combo.setLayoutData(data);
		} else {
			((GridData) combo.getLayoutData()).horizontalAlignment = GridData.FILL;
			((GridData) combo.getLayoutData()).grabExcessHorizontalSpace = true;
			((GridData) combo.getLayoutData()).widthHint = minwidth;
		}
		setToolTipText(combo, tooltip);
		return combo;
	}




	/**
	 * Creates a new remote system connection combobox instance and sets the default
	 * layout data, with tooltip text.
	 * <p>
	 * Assign the listener to the passed in implementer of Listener.
	 * <p>
	 * A remote system connection combobox is one that allows users to select a connection. The connection
	 * list can be subsetted by system type, subsystem factory or subsystem factory category.
	 * It has a "Connection:" prompt in front of it and optionally a "New..." button beside it.
	 * <p>
	 * @param parent composite to put the combo into.
	 * @param listener object to listen for selection events. Can be null.          
	 *   If you want to listen for modify events, call addSelectionListener(...) on your own.
	 * @param systemTypes array of system types to subset connection list by. Specify a single entry of '*' for 
	 *   all system types. Specify this <i>OR</i> specify factory <i>OR</i> specify factoryCategory 
	 *   <i>OR</i> specify factory Id
	 * @param factory the subsystem factory to subset connection list by. Only connections with a subsystem
	 *   owned by this factory are listed. Specify this <i>OR</i> specify systemTypes <i>OR</i> specify factoryCategory
	 *   <i>OR</i> specify factory Id
	 * @param factoryId the subsystem factory id to subset connection list by. Only connections with a
	 *   subsystem owned by this factory are listed, where id is a string specified in the
	 *   plugin.xml file for the subsystem factory extension point definition. 
	 *   Specify this <i>OR</i> specify factory <i>OR</i> specify systemTypes <i>OR</i> specify factory category
	 * @param factoryCategory the subsystem factory category to subset connection list by. Only connections with a
	 *   subsystem owned by a factory of this category are listed, where category is a string specified in the
	 *   plugin.xml file for the subsystem factory extension point definition. 
	 *   Specify this <i>OR</i> specify factory <i>OR</i> specify factory Id <i>OR</i> specify systemTypes
	 * @param defaultConnection the connection to pre-select. Can be null.
	 * @param horizontalSpan number of columns this should span
	 * @param newButton true if the combo is to have a "New..." button beside it
	 */
	public static SystemHostCombo createConnectionCombo(Composite parent, SelectionListener listener, String[] systemTypes, ISubSystemConfiguration factory, String factoryId, String factoryCategory, IHost defaultConnection, int horizontalSpan, boolean newButton) {
		SystemHostCombo combo = null;
		if (systemTypes != null)
			combo = new SystemHostCombo(parent, SWT.NULL, systemTypes, defaultConnection, newButton);
		else if (factory != null)
			combo = new SystemHostCombo(parent, SWT.NULL, factory, defaultConnection, newButton);
		else if (factoryId != null)
			combo = new SystemHostCombo(parent, SWT.NULL, defaultConnection, factoryId, newButton);
		else if (factoryCategory != null)
			combo = new SystemHostCombo(parent, SWT.NULL, defaultConnection, newButton, factoryCategory);
		if (listener != null)
			combo.addSelectionListener(listener);
		boolean hasGridData = (combo.getLayoutData() != null) && (combo.getLayoutData() instanceof GridData);
		//System.out.println("history directory griddata non-null? " + hasGridData);
		int minwidth = 250; // todo: tweak this?
		if (!hasGridData) {
			GridData data = new GridData();
			data.horizontalAlignment = GridData.FILL;
			data.grabExcessHorizontalSpace = true;
			data.widthHint = minwidth;
			data.verticalAlignment = GridData.CENTER;
			data.grabExcessVerticalSpace = false;
			data.horizontalSpan = horizontalSpan;
			combo.setLayoutData(data);
		} else {
			((GridData) combo.getLayoutData()).horizontalSpan = horizontalSpan;
			((GridData) combo.getLayoutData()).horizontalAlignment = GridData.FILL;
			((GridData) combo.getLayoutData()).grabExcessHorizontalSpace = true;
			((GridData) combo.getLayoutData()).widthHint = minwidth;
		}
		return combo;
	}

	/**
	 * Creates a readonly system type combination box.
	 * Does NOT create the leading prompt or anything except the combo.
	 */
	public static Combo createSystemTypeCombo(Composite parent, Listener listener) {
		return createSystemTypeCombo(parent, listener, null);
	}

	/**
	 * Creates a readonly system type combination box with the given system types.
	 * Does NOT create the leading prompt or anything except the combo.
	 */
	public static Combo createSystemTypeCombo(Composite parent, Listener listener, String[] systemTypes) {
		Combo combo = createReadonlyCombo(parent, listener, SystemResources.RESID_CONNECTION_SYSTEMTYPE_TIP);
			String[] typeItems = ((systemTypes == null) ? RSECorePlugin.getDefault().getRegistry().getSystemTypeNames() :
	systemTypes);
		combo.setItems(typeItems);
		combo.select(0);
		return combo;
	}

	/**
	 * Creates a hostname combination box. It if prefilled with all previously specified hostnames
	 *  for the given system type.
	 * <p>
	 * Does NOT create the leading prompt or anything except the combo.
	 */
	public static Combo createHostNameCombo(Composite parent, Listener listener, String systemType) {
		//System.out.println("TipId: " + ISystemConstants.RESID_HOSTNAME_TIP);		
		Combo combo = createCombo(parent, listener, SystemResources.RESID_CONNECTION_HOSTNAME_TIP);
		//System.out.println("Tip  : " + combo.getToolTipText());
		combo.setItems(RSEUIPlugin.getTheSystemRegistry().getHostNames(systemType));
		combo.select(0);
		return combo;
	}





	/**
	 * Create an entry field controlled by an inherit/override switch button
	 * <p>
	 * After creating the widget, call setLocal to set initial state, and setInheritedText/setLocalText to set inherited/local text
	 * @param parent composite to put the button into.
	 * @param tooltip text for the toggle. Can be null
	 * @param tooltip text for the entry field. Can be null
	 * @return The text field widget
	 */
	public static InheritableEntryField createInheritableTextField(Composite parent, String toggleToolTip, String entryToolTip) 
	{
		InheritableEntryField entryField = new InheritableEntryField(parent, SWT.NULL);
		if (toggleToolTip != null)
			entryField.setToggleToolTipText(toggleToolTip);
		if (entryToolTip != null)
			entryField.setTextFieldToolTipText(entryToolTip);
		return entryField;
	}

	/**
	 * Helper method to line up the leading prompts in a composite, taking 
	 * into account composite prompts nested within.
	 */
	public static void lineUpPrompts(Composite composite) {
		//System.out.println("Inside lineUpPrompts:");
		composite.layout(true);
		// FIND SIZE OF FIRST LABEL IN FIRST COLUMN (WILL ALL BE SAME SIZE)...
		Label firstLabel = getFirstColumnOneLabel(composite);
		// FIND MAX SIZE OF FIRST LABEL IN ALL NESTED COMPOSITES (WILL ALL BE DIFFERENT SIZES)...
		//System.out.println("Scanning nested composites:");
		int nbrColumns = ((GridLayout) composite.getLayout()).numColumns;
		Control[] childControls = composite.getChildren();
		int maxNestedLabelWidth = 0;
		int currColumn = 0;
		if ((childControls != null) && (childControls.length > 0)) {
			for (int idx = 0;(idx < childControls.length); idx++) {
				int rem = currColumn % nbrColumns;
				//System.out.println("...1.rem = " + rem);
				if ((currColumn == 0) || (rem == 0)) {
					if (childControls[idx] instanceof Composite) {
						Label firstNestedLabel = getFirstColumnOneLabel((Composite) childControls[idx]);
						if (firstNestedLabel != null) {
							if (firstNestedLabel.getSize().x > maxNestedLabelWidth)
								maxNestedLabelWidth = firstNestedLabel.getSize().x;
						}
					}
				}
				currColumn += ((GridData) childControls[idx].getLayoutData()).horizontalSpan;
			}
			//System.out.println("Max nested label size = " + maxNestedLabelWidth);
		}

		// DECIDE WHAT MAXIMUM WIDTH IS
		int columnOneWidth = 0;
		if (firstLabel != null)
			columnOneWidth = firstLabel.getSize().x;
		if (maxNestedLabelWidth > columnOneWidth)
			columnOneWidth = maxNestedLabelWidth;
		//System.out.println("Calculated column one width = " + columnOneWidth);    	  
		// APPLY NEW WIDTH TO FIRST COLUMN ONE LABEL
		if (firstLabel != null)
			 ((GridData) firstLabel.getLayoutData()).widthHint = columnOneWidth;
		// APPLY NEW WIDTH TO FIRST COLUMN ONE LABEL OF ALL NESTED COMPOSITES...
		currColumn = 0;
		if ((childControls != null) && (childControls.length > 0)) {
			for (int idx = 0;(idx < childControls.length); idx++) {
				int rem = currColumn % nbrColumns;
				if ((currColumn == 0) || (rem == 0)) {
					if (childControls[idx] instanceof Composite) {
						Label firstNestedLabel = getFirstColumnOneLabel((Composite) childControls[idx]);
						if (firstNestedLabel != null)
							 ((GridData) firstNestedLabel.getLayoutData()).widthHint = columnOneWidth;
					}
				}
				currColumn += ((GridData) childControls[idx].getLayoutData()).horizontalSpan;
			}
		}
		composite.layout(true);
	}

	/**
	 * Given a composite that has been layed out, return the first label found in the first column.
	 */
	public static Label getFirstColumnOneLabel(Composite composite) {
		//System.out.println("...Inside getFirstColumnOneLabel:");
		int nbrColumns = ((GridLayout) composite.getLayout()).numColumns;
		Control[] childControls = composite.getChildren();
		Label firstLabel = null;
		int currColumn = 0;
		if ((childControls != null) && (childControls.length > 0)) {
			for (int idx = 0;(firstLabel == null) && (idx < childControls.length); idx++) {
				int rem = currColumn % nbrColumns;
				//System.out.println("......0.rem = " + rem);
				if ((currColumn == 0) || (rem == 0)) {
					if (childControls[idx] instanceof Label) {
						firstLabel = (Label) childControls[idx];
						if (firstLabel.getText().trim().length() == 0)
							firstLabel = null; // skip it. Only a filler.
					}
				}
				currColumn += ((GridData) childControls[idx].getLayoutData()).horizontalSpan;
			}
		}
		//if (firstLabel != null)
		//  System.out.println("...returning first label of '"+firstLabel.getText()+"', width = " + firstLabel.getSize().x);
		//else
		//  System.out.println("...no first label found");
		return firstLabel;
	}

	/**
	 * Given a Composite, this method walks all the children recursively and 
	 *  and sets the mnemonics uniquely for each child control where a 
	 *  mnemonic makes sense (eg, buttons).
	 *  The letter/digit chosen for the mnemonic is unique for this Composite, 
	 *  so you should call this on as high a level of a composite as possible 
	 *  per Window.
	 * Call this after populating your controls.
	 * @return mnemonics object used for recording used-mnemonics. Use this
	 *    as input to subsequent calls to setMnemonics for the same window/dialog.
	 */
	public static Mnemonics setMnemonics(Composite parent) {
		Mnemonics mnemonics = new Mnemonics(); // instance of this class to get unique mnemonics for composite and nested composites
		mnemonics.setMnemonics(parent);
		return mnemonics;
	}
	
	/**
	 * Same as above but also whether to apply mnemonics to labels preceding text fields, combos and inheritable entry fields.
	 */
	public static Mnemonics setMnemonics(Composite parent, boolean applyToPrecedingLabels) {
		Mnemonics mnemonics = new Mnemonics(); // instance of this class to get unique mnemonics for composite and nested composites
		mnemonics.setApplyMnemonicsToPrecedingLabels(applyToPrecedingLabels);
		mnemonics.setMnemonics(parent);
		return mnemonics;
	}

	/**
	 * Same as above but specifically for wizard pages
	 */
	public static Mnemonics setWizardPageMnemonics(Composite parent) {
		Mnemonics mnemonics = new Mnemonics(); // instance of this class to get unique mnemonics for composite and nested composites
		mnemonics.setOnWizardPage(true);
		mnemonics.setMnemonics(parent);
		return mnemonics;
	}
	
	/**
	 * Same as above but also whether to apply mnemonics to labels preceding text fields, combos and inheritable entry fields.
	 */
	public static Mnemonics setWizardPageMnemonics(Composite parent, boolean applyToPrecedingLabels) {
		Mnemonics mnemonics = new Mnemonics(); // instance of this class to get unique mnemonics for composite and nested composites
		mnemonics.setOnWizardPage(true);
		mnemonics.setApplyMnemonicsToPrecedingLabels(applyToPrecedingLabels);
		mnemonics.setMnemonics(parent);
		return mnemonics;
	}

	/**
	 * Same as above but specifically for preference pages
	 */
	public static Mnemonics setPreferencePageMnemonics(Composite parent) {
		Mnemonics mnemonics = new Mnemonics(); // instance of this class to get unique mnemonics for composite and nested composites
		mnemonics.setOnPreferencePage(true);
		mnemonics.setMnemonics(parent);
		return mnemonics;
	}
	
	/**
	 * Same as above but also whether to apply mnemonics to labels preceding text fields, combos and inheritable entry fields.
	 */
	public static Mnemonics setPreferencePageMnemonics(Composite parent, boolean applyToPrecedingLabels) {
		Mnemonics mnemonics = new Mnemonics(); // instance of this class to get unique mnemonics for composite and nested composites
		mnemonics.setOnPreferencePage(true);
		mnemonics.setApplyMnemonicsToPrecedingLabels(applyToPrecedingLabels);
		mnemonics.setMnemonics(parent);
		return mnemonics;
	}

	/**
	 * Same as above but takes as input a previously populated mnemonics object,
	 *   which records already-used mnemonics for whatever scope you want (a dialog usually).
	 */
	public static Mnemonics setMnemonics(Mnemonics mnemonics, Composite parent) {
		mnemonics.setMnemonics(parent);
		return mnemonics;
	}
	/**
	 * Given an SWT Menu, "walk it" and automatically assign unique
	 * mnemonics for every menu item in it, and then for each 
	 * submenu, do so for it too.
	 * @param the menubar to add mnemonics for
	 */
	public static void setMnemonics(Menu menu) {
		Mnemonics mnemonics = new Mnemonics(); // instance of this class to get unique mnemonics FOR THIS MENU ONLY
		// walk the menu bar getting each menu...
		MenuItem menuItems[] = menu.getItems();
		for (int idx = 0; idx < menuItems.length; idx++) {
			MenuItem currMenuItem = menuItems[idx];
			// assign unique mnemonic from characters in menu text...
			currMenuItem.setText(mnemonics.setUniqueMnemonic(currMenuItem.getText()));
			// for a cascade or popup, this menuitem is itself a menu
			Menu nestedMenu = currMenuItem.getMenu();
			if (nestedMenu != null)
				setMnemonics(nestedMenu);
		} // end for all menus loop
	} // end addMnemonicsForMenuBar

	/**
	 * Given a Composite, this method walks all the children recursively and 
	 *  and sets the infopop help id for each child control where help 
	 *  makes sense (eg, buttons, combos, entry fields, lists, trees).
	 * <p>
	 * Call this after populating your controls.
	 */
	public static void setCompositeHelp(Composite parent, String helpID) {
		//setCompositeHelp(parent, helpID, (Hashtable)null);
		setHelp(parent, helpID);
	}

	/**
	 * Set the context id for a control on a view part
	 * @deprecated
	 */
	public static void setHelp(Control c, IViewPart view, Object id) {
		//ViewContextComputer comp = new ViewContextComputer(view, id);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(c, id.toString());
		if (traceHelpIDs)
			SystemBasePlugin.logInfo("Setting help id: " + id);
	}

	/**
	 * Set the context id for a control
	 */
	public static void setHelp(Control c, String id) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(c, id);
	}

	/**
	 * Set the context id for an action
	 */
	public static void setHelp(IAction c, String id) {
		String[] ids = new String[1];
		ids[0] = id;
		PlatformUI.getWorkbench().getHelpSystem().setHelp(c, id);
	}

	/**
	 * Set the context id for a menu item
	 */
	public static void setHelp(MenuItem c, String id) {
		String[] ids = new String[1];
		ids[0] = id;
		PlatformUI.getWorkbench().getHelpSystem().setHelp(c, id);
		//setHelp(c, ids);		
	}

	private static char STANDARD_COLON = ':';
	private static char WIDE_COLON = '\uFF1A';
	/**
	 * Appends a colon to a label, if the label doesn't already end in a colon of the proper size.
	 * If the wrong size colon is already there, it strips it first.
	 * @param label
	 * @return the label ending with a colon of the appropriate size
	 */
	public static String appendColon(String label) {
		/* Added for Defect 47275 */
		String result = label;
		boolean append = false;
		boolean strip = false;
		Locale currentLocale = Locale.getDefault();
		String language = currentLocale.getLanguage();
		boolean cjk = language.equals("zh") || language.equals("ja") || language.equals("ko");
		int n = result.length();
		if (n > 0) {
			char lastCharacter = label.charAt(n - 1);
			if (cjk) {
				strip = (lastCharacter == STANDARD_COLON);
				append = (lastCharacter != WIDE_COLON);
			} else {
				strip = (lastCharacter == WIDE_COLON);
				append = (lastCharacter != STANDARD_COLON);
			}
		} else {
			strip = false;
			append = true;
		}
		if (strip) {
			result = result.substring(0, n - 1);
		}
		if (append) {
			result += (cjk ? WIDE_COLON : STANDARD_COLON);
		}
		return result;
	}




	/**
	 * Set tooltip text
	 * If key does not end in "tooltip", then this is appended to it
	 */
	private static void setToolTipText(Control widget, String tooltip) {
		if (tooltip != null)
			widget.setToolTipText(tooltip);
	}
}