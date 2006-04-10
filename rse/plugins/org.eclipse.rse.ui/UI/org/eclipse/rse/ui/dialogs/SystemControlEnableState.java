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

package org.eclipse.rse.ui.dialogs;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.rse.ui.view.SystemPropertySheetForm;
import org.eclipse.rse.ui.view.SystemViewForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


/**
 * Helper class to save the enable/disable state of a control
 * including all its descendent controls.
 */
public class SystemControlEnableState 
{



	/**
	 * List of exception controls (element type: <code>Control</code>); 
	 * <code>null</code> if none.
	 */
	private List exceptions = null;

	/**
	 * List of saved states (element type: <code>ItemState</code>).
	 */
	private List states;

	/**
	 * Internal class for recording the enable/disable state of a
	 * single control.
	 */
	private class ItemState 
	{
		protected Control item;
		protected boolean state;
		public ItemState(Control item, boolean state) 
		{
			this.item = item;
			this.state = state;
		}
		public void restore() 
		{			
			if (item != null)
				item.setEnabled(state);
		}
	}

	/**
	 * Creates a new object and saves in it the current enable/disable
	 * state of the given control and its descendents; the controls 
	 * that are saved are also disabled.
	 *
	 * @param w the control
	 */
	protected SystemControlEnableState(Control w) 
	{
		this(w, null);
	}
	/**
	 * Creates a new object and saves in it the current enable/disable
	 * state of the given control and its descendents except for the 
	 * given list of exception cases; the controls that are saved
	 * are also disabled.
	 *
	 * @param w the control
	 * @param exceptions the list of controls to not disable
	 *  (element type: <code>Control</code>), or <code>null</code> if none
	 */
	protected SystemControlEnableState(Control w, List exceptions) 
	{
		super();
		states = new ArrayList();
		this.exceptions = exceptions;
		readStateForAndDisable(w);
	}
	/**
	 * Saves the current enable/disable state of the given control
	 * and its descendents in the returned object; the controls
	 * are all disabled.
	 *
	 * @param w the control
	 * @return an object capturing the enable/disable state
	 */
	public static SystemControlEnableState disable(Control w) 
	{
		return new SystemControlEnableState(w);
	}
	/**
	 * Saves the current enable/disable state of the given control
	 * and its descendents in the returned object except for the 
	 * given list of exception cases; the controls that are saved
	 * are also disabled.
	 *
	 * @param w the control
	 * @param exceptions the list of controls to not disable
	 *  (element type: <code>Control</code>)
	 * @return an object capturing the enable/disable state
	 */
	public static SystemControlEnableState disable(Control w, List exceptions) 
	{
		return new SystemControlEnableState(w, exceptions);
	}
	/**
	 * Recursively reads the enable/disable state for the given window
	 * and disables all controls.
	 */
	private void readStateForAndDisable(Control w) 
	{
		if ((exceptions != null && exceptions.contains(w)))
			return;

		if ((w instanceof Composite) && !(w instanceof SystemViewForm) && !(w instanceof SystemPropertySheetForm))
		{
			Composite c = (Composite) w;
			Control[] children = c.getChildren();
			for (int i = 0; i < children.length; i++) 
			{
				readStateForAndDisable(children[i]);
			}
		}
		// XXX: Workaround for 1G2Q8SS: ITPUI:Linux - Combo box is not enabled in "File->New->Solution"
		states.add(new ItemState(w, w.getEnabled()));
		w.setEnabled(false);
	}
	/**
	 * Restores the window enable state saved in this object.
	 */
	public void restore() 
	{
		int size = states.size();
		for (int i = 0; i < size; i++) 
		{
			((ItemState) states.get(i)).restore();
		}
	}
}