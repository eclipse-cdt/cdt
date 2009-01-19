/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * David Dykstal (IBM) - [226561] Add API markup to RSE javadocs for extend / implement
 * Martin Oberhuber (Wind River) - [261486][api][cleanup] Mark @noimplement interfaces as @noextend
 *******************************************************************************/

package org.eclipse.rse.core.model;

/**
 * This interface represents a message we wish to display as child node in the
 * tree view.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients. The
 *           standard implementations are included in the framework.
 */
public interface ISystemMessageObject {
	/**
	 * 0. An error occurred.
	 */
	public static final int MSGTYPE_ERROR = 0;
	/**
	 * 1. User cancelled
	 */
	public static final int MSGTYPE_CANCEL = 1;
	/**
	 * 3. Informational text
	 */
	public static final int MSGTYPE_INFO = 2;
	/**
	 * 4. Empty list. Eg "Nothing meets subset criteria"
	 */
	public static final int MSGTYPE_EMPTY = 3;
	/**
	 * 5. Object created successfully.
	 */
	public static final int MSGTYPE_OBJECTCREATED = 4;

	/**
	 * Return the message text shown for the label.
	 * The translated text is pre-determined from the message type.
	 */
	public String getMessage();

	/**
	 * Return the type of message:
	 * <ul>
	 *  <li>{@link #MSGTYPE_ERROR}
	 *  <li>{@link #MSGTYPE_CANCEL}
	 *  <li>{@link #MSGTYPE_INFO}
	 *  <li>{@link #MSGTYPE_EMPTY}
	 *  <li>{@link #MSGTYPE_OBJECTCREATED}
	 * </ul>
	 */
	public int getType();

	/**
	 * Return the parent object in the tree. That is, what was expanded to produce this message
	 */
	public Object getParent();

	/**
	 * isTransient determines if the message should be removed from the
	 * tree when the parent item in the tree is collapsed.
	 *
	 * @return true if the item should be removed, false if it should not
	 */
	public boolean isTransient();
}
