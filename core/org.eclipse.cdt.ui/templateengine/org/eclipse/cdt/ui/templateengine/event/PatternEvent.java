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
package org.eclipse.cdt.ui.templateengine.event;

import java.util.EventObject;

/**
 * 
 * PatternEvent class instances are created, when there is an unexpected input
 * in the InputUIElement. Which results in a mismatch to the pattern of input
 * data expected. This has to be updated in the UIPage. To do this,
 * PatternEvent is fired. For which, the UIPage will be the registered listener.
 * Here UIPage stands for WizardPage.
 * 
 * @since 4.0
 */

public class PatternEvent extends EventObject {

	private static final long serialVersionUID = 0000000000L;

	/**
	 * The description of this Event instance.
	 */
	String eventMessage;

	/**
	 * true indicates whether the user input is valid(according to pattern). This is useful, to update the UIPage with error messages. false otherwise.
	 */
	private boolean valid;

	/**
	 * The PatternEvent gets the Object source of this event, the same is passed
	 * to the EventObject.
	 * 
	 * @param source
	 */
	private PatternEvent(Object source) {
		super(source);
	}

	/**
	 * Overloaded constructor, the Object source of this event and the String
	 * message is paramete. Object source is passed as parameter to EventObject,
	 * the event description is initialized to eventMessage.
	 * 
	 * @param source
	 * @param eventMessage
	 */
	public PatternEvent(Object source, String eventMessage, boolean valid) {
		this(source);
		this.eventMessage = eventMessage;
		this.valid = valid;
	}

	/**
	 * return the String description of this Event instance.
	 * 
	 * @return String
	 */
	@Override
	public String toString() {
		return eventMessage;
	}

	/**
	 * returns the valid flag.
	 * 
	 * @return boolean
	 */
	public boolean getValid() {
		return valid;
	}

}

