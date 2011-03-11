/*******************************************************************************
 * Copyright (c) 2009, 2011 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

/**
 * A listener used to receive changes to problem profile
 * <p>
 * Clients may implement this interface.
 * </p>
 * 
 * @since 2.0
 */
public interface IProblemProfileChangeListener {
	/**
	 * Notification that a profile value has changed.
	 * The given event object describes the change details and must not
	 * be <code>null</code>.
	 * 
	 * @param event the event details
	 * @see ProblemProfileChangeEvent
	 * @see IProblemProfile#addProfileChangeListener(IProblemProfileChangeListener)
	 * @see IProblemProfile#removeProfileChangeListener(IProblemProfileChangeListener)
	 */
	public void profileChange(ProblemProfileChangeEvent event);
}