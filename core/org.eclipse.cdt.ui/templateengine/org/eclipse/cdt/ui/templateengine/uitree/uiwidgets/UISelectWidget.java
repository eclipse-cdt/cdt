/*******************************************************************************
 * Copyright (c) 2007, 2009 Symbian Software Limited and others.
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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.ibm.icu.text.MessageFormat;

import org.eclipse.cdt.ui.templateengine.Messages;
import org.eclipse.cdt.ui.templateengine.event.PatternEvent;
import org.eclipse.cdt.ui.templateengine.uitree.InputUIElement;
import org.eclipse.cdt.ui.templateengine.uitree.UIAttributes;
import org.eclipse.cdt.ui.templateengine.uitree.UIElement;

/**
 * This gives a Label and Combo widget.
 */
public class UISelectWidget extends InputUIElement {
	protected Label label;
	protected Combo combo;

	/**
	 * Mapping from values stored by this combo, to their associated names in UI
	 */
	protected Map<String, String> value2name;

	/**
	 * The default name to select
	 */
	protected String defaultValue;

	/**
	 * The currently selected name. May be null.
	 */
	protected String currentValue;

	/**
	 * Constructor for Select Widget.
	 * 
	 * @param attribute
	 *            attribute associated with this widget.
	 */
	public UISelectWidget(UIAttributes attribute, Map<String, String> value2name, String defaultValue) {
		super(attribute);
		this.value2name= value2name;
		this.defaultValue= defaultValue;
	}

	/*
	 * @see org.eclipse.cdt.ui.templateengine.uitree.UIElement#getValues()
	 */
	@Override
	public Map<String, String> getValues() {
		Map<String, String> values = new HashMap<String, String>();
		if(currentValue != null) {
			values.put(uiAttributes.get(InputUIElement.ID), currentValue);
		}
		return values;
	}

	/*
	 * @see org.eclipse.cdt.ui.templateengine.uitree.UIElement#setValues(java.util.Map)
	 */
	@Override
	public void setValues(Map<String, String> valueMap) {
		defaultValue= valueMap.get(uiAttributes.get(InputUIElement.ID));
		if (combo != null) {
			String[] items= combo.getItems();
			for (int i=0; i < items.length; i++) {
				if (items[i].equals(defaultValue)) {
					combo.select(i);
					break;
				}
			}
		}
	}

	/*
	 * @see org.eclipse.cdt.ui.templateengine.uitree.UIElement#createWidgets(org.eclipse.cdt.ui.templateengine.uitree.uiwidgets.UIComposite)
	 */
	@Override
	public void createWidgets(final UIComposite uiComposite) {
		label= new Label(uiComposite, SWT.LEFT);
		label.setText(uiAttributes.get(InputUIElement.WIDGETLABEL));

		Composite comboComposite = new Composite(uiComposite, SWT.NONE);
		comboComposite.setLayout(GridLayoutFactory.swtDefaults().create());
		comboComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());

		combo= new Combo(comboComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());		
		combo.setData(".uid", uiAttributes.get(UIElement.ID)); //$NON-NLS-1$

		// populate combo
		int index= 0, defaultIndex= 0;
		for(String value : value2name.keySet()) {			
			combo.add(value2name.get(value));
			if(value.equals(defaultValue)) {
				defaultIndex= index;
			}
			index++;

		}
		combo.select(defaultIndex);
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				currentValue= getValue(combo.getItem(combo.getSelectionIndex()));
				uiComposite.firePatternEvent(createPatternEvent());
			}
		});
		uiComposite.firePatternEvent(createPatternEvent());
	}

	private PatternEvent createPatternEvent() {
		String msg= MessageFormat.format(Messages.getString("UISelectWidget_ErrorNoneSelected0"), new String[] {label.getText()}); //$NON-NLS-1$
		return new PatternEvent(this, msg, isValid());
	}

	/**
	 * @return whether this widget has been set to a valid state. For this 
	 * widget type that means whether the user has selected a non-empty string name.
	 */
	@Override
	public boolean isValid() {
		boolean retVal = true;
		if(Boolean.parseBoolean(uiAttributes.get(InputUIElement.MANDATORY))
				&& ! InputUIElement.SELECTTYPE.equals(uiAttributes.get(InputUIElement.TYPE)) ) {
			retVal= currentValue!= null && currentValue.trim().length()>0;
		}
		return retVal;
	}

	private String getValue(String name) {
		for(String value : value2name.keySet()) {
			if(value2name.get(value).equals(name)) {
				return value;
			}
		}
		throw new IllegalStateException();
	}
	
	
	/*
	 * @see org.eclipse.cdt.ui.templateengine.uitree.UIElement#disposeWidget()
	 */
	@Override
	public void disposeWidget() {
		label.dispose();
		combo.dispose();
	}
}
