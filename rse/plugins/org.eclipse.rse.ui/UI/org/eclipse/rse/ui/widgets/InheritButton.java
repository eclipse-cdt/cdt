/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * An InheritButton is a specialized control that 
 * wraps a push button control with two states:
 * "inherit" and "local".  The initial state is "inherit".  The button is
 * painted with arrowhead image that points either left or right if the 
 * button is in "inherit" or "local" state respectively.  
 * <p>
 * Pressing the button will trigger a SelectionEvent which the client
 * can listen for.  Typically the client will use this to change the 
 * button state.
 * <p>
 * An InheritButton is assumed to exist inside a composite control with a GridLayout.  
 * There is no need to set its layout data unless you wish to override the 
 * default characteristics.
 * <p>
 * Although this control extends Composite, it does not make sense to 
 * add children to this control or to set a layout on it.
 */
public class InheritButton extends Composite {

	/**
	 * Value is 12 pixels.
	 */
	public static final int DEFAULT_WIDTH = 12;

	/**
	 * Value is 20 pixels.
	 */
	public static final int DEFAULT_HEIGHT = 20;
	
	private Image leftArrow = null; // arrow points left, value is inherited
	private Image rightArrow = null; // arrow points right, value is the local value 
	private boolean isLocal = false; // default is "inherit"
	private Button toggle = null; 
	
	/**
	 * Create a new InheritButton.
	 * @param parent
	 */
	public InheritButton(Composite parent) {
		super(parent, SWT.NONE);
		GridData data = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		data.widthHint = DEFAULT_WIDTH;
		data.heightHint = DEFAULT_HEIGHT;
		setLayoutData(data);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		setLayout(layout);
		initializeToggle(this);
	}

	private void initializeToggle(Composite parent) {
		toggle = new Button(parent, SWT.PUSH);
		createToggleImages(toggle.getBackground());
		toggle.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			public void getHelp(AccessibleEvent e) { // this is the one that should supply the text heard.
				e.result = getToolTipText();
			}
			public void getName(AccessibleEvent e) { // this is the one that apparently does supply the text heard.
				e.result = getToolTipText();
			}
		});
		toggle.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				disposeToggleImages();
			}
		});
		toggle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setToggleImage();
	}

	/**
	 * Set the inherit/local state.  
	 * In the "local" state, the arrow image points to the right.  
	 * In the "inherit" state, the arrow image points to the left.
	 * @param isLocal true if the button should be in "local" state.  false if the
	 * button should be in "inherit" state.
	 */
	public void setLocal(boolean isLocal) {
		this.isLocal = isLocal;
		setToggleImage();
	}
	
	/**
	 * Query the inherit/local state.
	 * @return true if the button is in local state
	 */
	public boolean isLocal() {
		return isLocal;
	}
	
	/**
	 * Register a listener interested in when the button is pressed.
	 * <p>
	 * @see InheritButton#removeSelectionListener(SelectionListener)
	 */
	public void addSelectionListener(SelectionListener listener) {
		if (toggle == null) return;
		toggle.addSelectionListener(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#addKeyListener(org.eclipse.swt.events.KeyListener)
	 */
	public void addKeyListener(KeyListener listener) {
		if (toggle == null) return;
		toggle.addKeyListener(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#removeKeyListener(org.eclipse.swt.events.KeyListener)
	 */
	public void removeKeyListener(KeyListener listener) {
		if (toggle == null) return;
		toggle.removeKeyListener(listener);
	}

	/** 
	 * Remove a previously set selection listener.
	 * @see InheritButton#addSelectionListener(SelectionListener)
	 */
	public void removeSelectionListener(SelectionListener listener) {
		if (toggle == null) return;
		toggle.removeSelectionListener(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setFocus()
	 */
	public boolean setFocus() {
		return toggle.setFocus();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#isFocusControl()
	 */
	public boolean isFocusControl() {
		return toggle.isFocusControl();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setToolTipText(java.lang.String)
	 */
	public void setToolTipText(String string) {
		toggle.setToolTipText(string);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#getToolTipText()
	 */
	public String getToolTipText() {
		return toggle.getToolTipText();
	}
	
	/**
	 * Places the correct graphic on the button depending on the current
	 * button state.  
	 * In the "local" state, the arrow image points to the right.
	 * In the "inherit" state, the arrow image points to the left.
	 */
	private void setToggleImage() {
		toggle.setImage(isLocal ? rightArrow : leftArrow);
	}

	/**
	 * Creates the images used for the button graphics.  This should be done 
	 * when the button is created.
	 * @param backgroundColor The background color with which the arrow images
	 * should be painted.  The foreground color is black.
	 */
	private void createToggleImages(Color backgroundColor) {
		Display display = Display.getCurrent();
		GC gc = null;
		if (display != null) {
			leftArrow = new Image(display, 3, 5);
			gc = new GC(leftArrow);
			gc.setBackground(backgroundColor);
			gc.fillRectangle(leftArrow.getBounds());
			gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
			gc.drawLine(0, 2, 0, 2);
			gc.drawLine(1, 1, 1, 3);
			gc.drawLine(2, 0, 2, 4);
			gc.dispose();
			rightArrow = new Image(display, 3, 5);
			gc = new GC(rightArrow);
			gc.setBackground(backgroundColor);
			gc.fillRectangle(rightArrow.getBounds());
			gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
			gc.drawLine(0, 0, 0, 4);
			gc.drawLine(1, 1, 1, 3);
			gc.drawLine(2, 2, 2, 2);
			gc.dispose();
		}
	}

	/**
	 * Dispose of the images used for the arrow graphics.  Should be invoked
	 * when the button is disposed.
	 */
	private void disposeToggleImages() {
		if (leftArrow != null) leftArrow.dispose();
		if (rightArrow != null) rightArrow.dispose();
	}
	
}