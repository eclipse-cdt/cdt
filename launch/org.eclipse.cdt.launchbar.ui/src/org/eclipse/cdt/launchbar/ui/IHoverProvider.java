/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.cdt.launchbar.ui;

public interface IHoverProvider {

	/**
	 * Display the hover item.
	 *
	 * @return true if hover item was displayed, otherwise false
	 */
	public abstract boolean displayHover(Object element);

	/**
	 * Dismiss the hover item.
	 *
	 * @param immediate
	 *            if true, the hover item will be immediately dismissed, otherwise it may be be dismissed at a later time.
	 */
	public abstract void dismissHover(Object element, boolean immediate);

}
