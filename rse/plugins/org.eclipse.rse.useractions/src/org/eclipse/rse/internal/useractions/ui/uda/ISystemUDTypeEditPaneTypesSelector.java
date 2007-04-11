package org.eclipse.rse.internal.useractions.ui.uda;

/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Control;

/**
 * @author coulthar
 *
 * Within the Work With Files Types dialog is an edit pane
 *  ({@link org.eclipse.rse.internal.useractions.ui.uda.SystemUDTypeEditPane})
 *  that contains an entry field for the name, and then one or 
 *  more widgets that prompt for the file types that constitute
 *  this named type.
 * <p>
 * This is interface abstracts out the minimal requirements for that
 *  set of widgets, so that it can be pluggable by subsystems wishing
 *  to supply customer widgets. These could be as simple as an entry
 *  field or as complex as a checkbox viewer with add and remove
 *  buttons. As far as the edit pane class is concerned, it need only
 *  be able to set the inputs, get the outputs and listen for changes.
 * 
 */
public interface ISystemUDTypeEditPaneTypesSelector {
	/**
	 * Set domain.
	 * The edit pane may possibly appear differently, depending on the domain.
	 * When the domain changes (either in "new" or "edit" mode) this method is called.
	 */
	public void setDomain(int domain);

	/**
	 * Set the msg line in case this composite widget needs to issue an error msg
	 */
	public void setMessageLine(ISystemMessageLine msgLine);

	/**
	 * Initialize the types. These are stored as a single string using 
	 *  a subsystem-decidable delimiter character. This is called when
	 *  entering "edit" mode.
	 */
	public void setTypes(String types);

	/**
	 * Clear the types. That is, make sure none are selected. This is 
	 *  called when entering "new" mode.
	 */
	public void clearTypes();

	/**
	 * Retrieve the types as a single string. The delimiter used is up to
	 *  the implementor, as long as it knows how to parse and assemble the
	 *  types list as a single string.
	 */
	public String getTypes();

	/**
	 * Allow the edit pane (or any consumer) to be informed as
	 *  changes are made to the list. When events are fired, the consumer
	 *  will call getTypes() to get the new list.
	 */
	public void addModifyListener(ModifyListener listener);

	/**
	 * Allow the edit pane (or any consumer) to stop listening as
	 *  changes are made to the list. 
	 */
	public void removeModifyListener(ModifyListener listener);

	/**
	 * Validate input, and return the error message if an error is found.
	 * This is called by the consumer upon receipt of a modify event, to
	 * show any error messages and to know if there are errors pending or
	 * not.
	 */
	public SystemMessage validate();

	/**
	 * Return the primary input-capable control.
	 * Used to set focus, among other things.
	 */
	public Control getControl();

	/**
	 * Enable or disable the input-capability of the constituent controls
	 */
	public void setEnabled(boolean enable);

	/**
	 * We want to disable editing of IBM or vendor-supplied 
	 * types, so when one of these is selected, this method is
	 * called to enter non-editable mode. 
	 * @param editable Whether to disable editing of this type or not
	 * @param vendor When disabling, it contains the name of the vendor for substitution purposes
	 */
	public void setEditable(boolean editable, String vendor);
}
