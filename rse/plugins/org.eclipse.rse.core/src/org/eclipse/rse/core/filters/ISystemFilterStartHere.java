/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [cleanup] Add API "since" Javadoc tags
 * David Dykstal (IBM) - [226561] Add API markup to RSE Javadocs where extend / implement is allowed
 * David Dykstal (IBM) - [261486][api] add noextend to interfaces that require it
 *******************************************************************************/
package org.eclipse.rse.core.filters;

import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.logging.Logger;

/**
 * @noimplement This interface is not intended to be implemented by clients.
 * The allowable implementations are already present in the framework.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISystemFilterStartHere {

	/**
	 * Factory to create a filter pool manager, when you do NOT want it to worry about
	 *  saving and restoring the filter data to disk. Rather, you will save and restore
	 *  yourself.
	 * @param logger A logging object into which to log errors as they happen in the framework
	 * @param caller Objects which instantiate this class should implement the
	 *   SystemFilterPoolManagerProvider interface, and pass "this" for this parameter.
	 *   Given any filter framework object, it is possible to retrieve the caller's
	 *   object via the getProvider method call.
	 * @param name the name of the filter pool manager. Not currently used but you may
	 *   find a use for it.
	 * @param allowNestedFilters true if filters inside filter pools in this manager are
	 *   to allow nested filters. This is the default, but can be overridden at the
	 *   individual filter pool level.
	 */
	public ISystemFilterPoolManager createSystemFilterPoolManager(ISystemProfile profile, Logger logger, ISystemFilterPoolManagerProvider caller, String name, boolean allowNestedFilters);

	/**
	 * Create a SystemFilterPoolReferenceManager instance, when you do NOT want
	 * it to be saved and restored to its own file. Rather, you will save and
	 * restore it yourself.
	 * 
	 * @param caller Objects which instantiate this class should implement the
	 *            SystemFilterPoolReferenceManagerProvider interface, and pass
	 *            "this" for this parameter. Given any filter framework object,
	 *            it is possible to retrieve the caller's object via the
	 *            getProvider method call.
	 * @param relatedPoolMgrProvider The creator of the managers that own the
	 *            master list of filter pools that this manager will contain
	 *            references to.
	 * @param name the name of the filter pool reference manager. This is not
	 *            currently used, but you may find a use for it.
	 * @since org.eclipse.rse.core 3.0
	 */
	public ISystemFilterPoolReferenceManager createSystemFilterPoolReferenceManager(ISystemFilterPoolReferenceManagerProvider caller, ISystemFilterPoolManagerProvider relatedPoolMgrProvider,
			String name);
}
