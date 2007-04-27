/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

public class TriButton extends Composite implements SelectionListener {
	private static final String[] ITEMS = {"No", "Yes", "?"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	public static final int NO  = 0;
	public static final int YES = 1;
	public static final int UNKNOWN = 2;
	
	private boolean triMode = false;
	private Button button = null;
	private Combo combo = null;
	private Label label = null;
	private List listeners = new LinkedList();

	public TriButton(Composite parent, int style) {
		this(parent, style, true);
	}

	public TriButton(Composite parent, int style, boolean _triMode) {
		super(parent, style);
		triMode = _triMode;
		if (triMode) {
			setLayout(new GridLayout(2, false));
			combo = new Combo(this, style | SWT.READ_ONLY | SWT.DROP_DOWN);
			combo.setLayoutData(new GridData(GridData.BEGINNING));
			combo.setItems(ITEMS);
			combo.addSelectionListener(this);
			label = new Label(this, SWT.NONE);
			label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			label.addMouseListener(new MouseListener() {
				public void mouseDoubleClick(MouseEvent e) {}
				public void mouseDown(MouseEvent e) {}
				public void mouseUp(MouseEvent e) {
					processMouseUp(e);
				}});
		} else {
			setLayout(new GridLayout());
			button = new Button(this, style | SWT.CHECK);
			button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			button.addSelectionListener(this);
		}
	}

	public String getText () {
		return triMode ? label.getText() : button.getText(); 
	}
	public void setText (String string) {
		if (triMode) label.setText(string); 
		else button.setText(string);
	}

	public String getToolTipText () {
		return triMode ? label.getToolTipText() : button.getToolTipText(); 
	}
	public void setToolTipText (String string) {
		if (triMode) {
			label.setToolTipText(string); 
			combo.setToolTipText(string); 
		} else button.setToolTipText(string);
	}
	
	public boolean getSelection () {
		return triMode ? (combo.getSelectionIndex() == YES) : button.getSelection();
	}
	public void setSelection (boolean selected) {
		if (triMode) combo.select(selected ? YES : NO);
		else button.setSelection(selected);
	}
	
	public int getTriSelection () {
		return triMode ? combo.getSelectionIndex(): (button.getSelection() ? YES : NO);
	}
	public void setTriSelection(int selection) {
		if (selection != NO && 
			selection != YES && 
			selection != UNKNOWN) 
			selection = NO;
		if (triMode) combo.select(selection);
		else button.setSelection(selection == 1);
	}
	
	public int getAlignment () {
		return triMode ? label.getAlignment() : button.getAlignment();
	}
	public void setAlignment (int alignment) {
		if (triMode) label.setAlignment(alignment);
		else button.setAlignment(alignment);
	}

	public Image getImage () {
		return triMode ? label.getImage() : button.getImage();
	}
	public void setImage (Image image) {
		if (triMode) label.setImage(image);
		else button.setImage(image);
	}
	
	public void addSelectionListener (SelectionListener listener) {
		listeners.add(listener);
	}
	public void removeSelectionListener (SelectionListener listener) {
		listeners.remove(listener);
	}

	public void widgetDefaultSelected(SelectionEvent e) {
		e.widget = this;
		Iterator it = listeners.iterator();
		while (it.hasNext())
			((SelectionListener)it.next()).widgetDefaultSelected(e);
	}
	public void widgetSelected(SelectionEvent e) {
		e.widget = this;
		Iterator it = listeners.iterator();
		while (it.hasNext())
			((SelectionListener)it.next()).widgetSelected(e);
	}
	
	private void processMouseUp(MouseEvent me) {
		int x = combo.getSelectionIndex() + 1;
		if (x < 0 || x > 2) x = 0;
		combo.select(x);
		
		Event e = new Event();
		e.button = me.button;
		e.count = me.count;
		e.data = me.data;
		e.display = me.display;
		e.stateMask = me.stateMask;
		e.time = me.time;
		e.x = me.x;		
		e.y = me.y;
		e.type = SWT.Selection;
		e.doit = true;
		e.item = this;
		e.widget = this;
		SelectionEvent se = new SelectionEvent(e);
		widgetSelected(se);
	}
}
