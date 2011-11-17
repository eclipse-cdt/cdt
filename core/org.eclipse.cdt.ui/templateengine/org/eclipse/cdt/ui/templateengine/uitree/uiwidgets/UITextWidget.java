/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.templateengine.uitree.uiwidgets;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.cdt.core.templateengine.TemplateEngineHelper;
import org.eclipse.cdt.ui.templateengine.Messages;
import org.eclipse.cdt.ui.templateengine.event.PatternEvent;
import org.eclipse.cdt.ui.templateengine.uitree.IPatternMatchingTable;
import org.eclipse.cdt.ui.templateengine.uitree.InputUIElement;
import org.eclipse.cdt.ui.templateengine.uitree.UIAttributes;
import org.eclipse.cdt.ui.templateengine.uitree.UIElement;


/**
 * This gives a Label and Text widget. The Text widget can be SINGLE type of
 * MULTI type. This depends on the input type in TemplateDescriptor. The data
 * entered by the user is verified against an expected pattern. If the user
 * entered data doesn't confirms to the expected pattern, a PatternEvent is
 * fired to UIComposite.
 *
 * The UI***Widget classes which needs to handle patterns, can inherit the same
 * from this class. The inheriting class need not cache UIComposite instance
 * but should set the same for UITextWidget(super).
 */
public class UITextWidget extends InputUIElement implements ModifyListener {

	/**
	 * Text widget.
	 */
	protected Text text;

	/**
	 * Label of this widget.
	 */
	protected Label label;
	private String patternValue;

	/**
	 * Composite to which this widget control is added. Classes extending this class, should make sure that they initialize this from the respective class createWidgets method.
	 */
	protected UIComposite uiComposite;

	protected String textValue;

	/**
	 * Constructor.
	 *
	 * @param uiAttribute
	 *            attribute associated with this widget.
	 */
	public UITextWidget(UIAttributes uiAttribute) {
		super(uiAttribute);
		this.textValue = new String();
	}

	/**
	 * @return String, value contained in the Text Widget.
	 */
	@Override
	public Map<String, String> getValues() {
		Map<String, String> retMap = new HashMap<String, String>();
		retMap.put(uiAttributes.get(InputUIElement.ID), textValue);

		return retMap;
	}

	/**
	 * Set the Text widget with new value.
	 *
	 * @param valueMap
	 */
	@Override
	public void setValues(Map<String, String> valueMap) {
		String val = valueMap.get(uiAttributes.get(InputUIElement.ID));
		String key = null;
		String subString = null;
		if (val != null) {
			if (val.indexOf(TemplateEngineHelper.OPEN_MARKER) != -1) {
				key = TemplateEngineHelper.getFirstMarkerID(val);
				subString = val.substring(key.length() + 3, val.length());
				if (valueMap.get(key) != null)
					val = valueMap.get(key) + subString;
				else
					val = subString;
			}
			val = val.trim();
			textValue = val;
		}
	}

	/**
	 * create a Label and Text widget, add it to UIComposite. set Layout for the
	 * widgets to be added to UIComposite. set required parameters to the
	 * Widgets.
	 *
	 * @param uiComposite
	 */
	@Override
	public void createWidgets(UIComposite uiComposite) {

		GridData gd = new GridData();
		this.uiComposite = uiComposite;
		label = new Label(uiComposite, SWT.LEFT);

		label.setText(uiAttributes.get(InputUIElement.WIDGETLABEL));
		if ((uiAttributes.get(UIElement.TYPE)).equalsIgnoreCase(InputUIElement.MULTILINETYPE)) {
			gd = new GridData();
			gd.verticalAlignment = SWT.BEGINNING;
			gd.verticalIndent = 5;
			label.setLayoutData(gd);
		}

		if (uiAttributes.get(UIElement.DESCRIPTION) != null){
			String tipText = uiAttributes.get(UIElement.DESCRIPTION);
			tipText = tipText.replaceAll("\\\\r\\\\n", "\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
			label.setToolTipText(tipText);
		}
		text = getTextWidget(uiAttributes.get(UIElement.TYPE));
		text.addModifyListener(this);
		text.setData(".uid", uiAttributes.get(UIElement.ID)); //$NON-NLS-1$
		text.setText(textValue);
	}

	/**
	 * call the dispose method on the widgets. This is to ensure that the
	 * widgets are properly disposed.
	 */
	@Override
	public void disposeWidget() {
		label.dispose();
		text.dispose();
	}

	/**
	 * evaluate the text entered by the user against the pattern associated with
	 * this widget. checks the text entered. On violation of pattern associated
	 * for this widget, by the user entered text. A pattern event is fired to
	 * the container. If this widget has attribute 'checkproject' set to true,
	 * the value entered in this widget is treated as project name. The same is
	 * verified if there is a directory by the same name in workspace,
	 * PatternEvent is thrown to Container.
	 *
	 * @param pattern
	 */
	public void evaluatePattern(String labelText, String userInputText, String pattern) {
		String message = labelText + InputUIElement.CONTENTS;
		Pattern pattern2 = Pattern.compile(pattern);
		Matcher matcher = pattern2.matcher(userInputText);
		if (!matcher.matches()) {
			String[] failed = pattern2.split(userInputText);
			for (int i = 1; i < failed.length; i++)
				message = message + " " + failed[i]; //$NON-NLS-1$
			message += InputUIElement.ISINVALID;
			message += " Expected pattern is \"" + pattern + "\""; //$NON-NLS-1$ //$NON-NLS-2$
			if (uiComposite != null)
				uiComposite.firePatternEvent(new PatternEvent(this, message, false));

		} else {
			String checkproject = uiAttributes.get(InputUIElement.CHECKPROJECT);
			if ((checkproject != null) && (checkproject.equalsIgnoreCase(TemplateEngineHelper.BOOLTRUE))
					&& TemplateEngineHelper.checkDirectoryInWorkspace(userInputText)) {

				message = userInputText + Messages.getString("UITextWidget.0"); //$NON-NLS-1$
				uiComposite.firePatternEvent(new PatternEvent(this, message, false));
			} else {
				if (uiComposite != null)
					uiComposite.firePatternEvent(new PatternEvent(this, message, true));
			}
		}
	}

	/**
	 * Method from ModifyListener. Extracts the Text from the widget. calls
	 * evaluatePattern.
	 */
	@Override
	public void modifyText(ModifyEvent e) {
		String patternName = uiAttributes.get(InputUIElement.INPUTPATTERN);

		if (patternName == null) {
			patternValue = null;
		} else if (patternName.equals(IPatternMatchingTable.FREETEXT) ||
				 patternName.equals(IPatternMatchingTable.TEXT) ||
				 patternName.equals(IPatternMatchingTable.FILENAME)) {

			patternValue = getPatternValue(patternName);
		} else {
			patternValue = patternName;
		}

		// Get the source from event. This is done because this class can be
		// extended by
		// other classes, having Text widget. They can just make use of
		// modifyText,
		// evaluatePattern and isValid.
		textValue = text.getText();

		if ((patternValue == null) || (textValue == null))
			return;

		String mandatory = uiAttributes.get(InputUIElement.MANDATORY);
		if ((mandatory == null || !mandatory.equalsIgnoreCase("true")) && textValue.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		evaluatePattern(label.getText(), textValue, patternValue);
	}

	/**
	 * Returns the Pattern Value for the widget.
	 * @param patternName
	 * @return
	 */
	private String getPatternValue(String patternName) {

		if (patternName.equals(IPatternMatchingTable.TEXT)) {
			patternValue = IPatternMatchingTable.TEXTPATTERNVALUE;
		}

		if (patternName.equals(IPatternMatchingTable.FREETEXT)) {
			patternValue = IPatternMatchingTable.FREETEXTPATTERNVALUE;
		}

		if (patternName.equals(IPatternMatchingTable.FILENAME)) {
			patternValue = IPatternMatchingTable.FILEPATTERNVALUE;
		}
		return patternValue;

	}

	/**
	 * Based on the sate of this Widget return true or false. This return value
	 * will be used by the UIPage to update its(UIPage) state. Return value
	 * depends on the value contained in TextWidget. If value contained is null, ""
	 * and Mandatory value from attributes.
	 *
	 * @return boolean.
	 */
	@Override
	public boolean isValid() {
		boolean retVal = true;
		String mandatory = uiAttributes.get(InputUIElement.MANDATORY);

		if (((mandatory != null) && (mandatory.equalsIgnoreCase(TemplateEngineHelper.BOOLTRUE)))
				&& ((textValue == null) || (textValue.equals("")) || //$NON-NLS-1$
				(textValue.trim().length() < 1))) {

			retVal = false;
		}
		return retVal;
	}

	/**
	 * Based on Input Type Text widget is created. The Text widget created can
	 * be of Type SINGLE or MULTI.
	 *
	 * @param type
	 *            of Text widget required.
	 * @return Text.
	 */
	private Text getTextWidget(String type) {
		Text retTextWidget = null;

		Composite textConatiner = new Composite(uiComposite, SWT.NONE | SWT.NO_REDRAW_RESIZE);

		textConatiner.setLayout(new GridLayout());

		if (type.equalsIgnoreCase(InputUIElement.INPUTTYPE)) {
			GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.widthHint = 70;
			textConatiner.setLayoutData(gridData);
			retTextWidget = new Text(textConatiner, SWT.SINGLE | SWT.BORDER);
			retTextWidget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
		if (type.equalsIgnoreCase(InputUIElement.MULTILINETYPE)) {

			GridData multiTextData = new GridData(GridData.FILL_HORIZONTAL);
			multiTextData.widthHint = 70;
			String line = uiAttributes.get(InputUIElement.SIZE);
			int cnt = 1;
			if (line != null) {
				cnt = Integer.parseInt(line);
				if (cnt <= 0)
					cnt = 1;

			}
			multiTextData.heightHint = 30 + 12 * cnt;
			textConatiner.setLayoutData(multiTextData);

			retTextWidget = new Text(textConatiner, SWT.WRAP | SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
			GridData textData = new GridData(SWT.FILL, SWT.FILL, true, true);
			retTextWidget.setLayoutData(textData);

		}

		return retTextWidget;
	}

}
