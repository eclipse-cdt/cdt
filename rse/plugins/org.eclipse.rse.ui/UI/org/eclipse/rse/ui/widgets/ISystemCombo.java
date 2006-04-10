/********************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation. All rights reserved.
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
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;


/**
 * We have a number of composite widgets that include a combo-box. This
 *  interface enforces some common methods on all of them to make it easier
 *  to code to this in a consistent manner.
 */
public interface ISystemCombo 
{



	/**
	 * Return the embedded combo box widget
	 */
	public Combo getCombo();
	/**
	 * Set auto-uppercase. When enabled, all non-quoted values are uppercased when appropriate
	 */
	public void setAutoUpperCase(boolean enable);
	/**
	 * Set the width hint for the combo box widget (in pixels).
	 * A rule of thumb is 10 pixels per character, but allow 15 for the litte button on the right.
	 * You must call this versus setting it yourself, else you may see truncation.
	 */
	public void setWidthHint(int widthHint);
	/**
	 * Query the combo field's current contents
	 */
	public String getText();
	/**
	 * Disable/Enable all the child controls
	 */
	public void setEnabled(boolean enabled);
	/**
	 * Set the tooltip text for the combo field
	 */
	public void setToolTipText(String tip);
	/**
	 * Set the tooltip text for the history button
	 */
	public void setButtonToolTipText(String tip);
	/**
	 * Set the focus to the combo field
	 */
	public boolean setFocus();        
    /**
     * Select the combo dropdown list entry at the given index
     */
    public void select(int selIdx);
    /**
     * Same as {@link #select(int)}
     */
    public void setSelectionIndex(int selIdx);    
    /**
     * Clear the entered/selected contents of the combo box. Clears the text selection and the list selection
     */
    public void clearSelection();
    /**
     * Clear the entered/selected contents of the combo box. Clears only the text selection, not the list selection
     */
    public void clearTextSelection();
    /**
     * Get the index number of the currently selected item. 
     */
    public int getSelectionIndex();
	/**
	 * Register a listener interested in an item is selected in the combo box
     * @see #removeSelectionListener(SelectionListener)
     */
    public void addSelectionListener(SelectionListener listener); 
    /** 
     * Remove a previously set combo box selection listener.
     * @see #addSelectionListener(SelectionListener)
     */
    public void removeSelectionListener(SelectionListener listener);
}