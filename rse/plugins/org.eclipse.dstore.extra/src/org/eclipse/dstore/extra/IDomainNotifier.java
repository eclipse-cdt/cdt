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
 * David McKnight   (IBM) - [225507][api][breaking] RSE dstore API leaks non-API types
 * Martin Oberhuber (Wind River) - [261486][api][cleanup] Mark @noimplement interfaces as @noextend
 *******************************************************************************/

package org.eclipse.dstore.extra;

/**
 * For DataStore domain notification
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IDomainNotifier {
	public void addDomainListener(IDomainListener listener);

	public void fireDomainChanged(DomainEvent event);

	public boolean hasDomainListener(IDomainListener listener);

	public void removeDomainListener(IDomainListener listener);

	/**
	 * @since 2.1
	 */
	public void enable(boolean on);

	/**
	 * @since 2.1
	 */
	public boolean isEnabled();
}
