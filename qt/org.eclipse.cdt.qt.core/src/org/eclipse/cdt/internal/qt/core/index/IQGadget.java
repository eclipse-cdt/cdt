/*
 * Copyright (c) 2014, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.index;

import java.util.Collection;

/**
 * Represents classes that have been tagged as Q_GADGETs.  Q_GADGETs are normal
 * C++ class declarations that have been tagged with the Q_GADGET macro and are
 * therefore able to add enums and flags to the Qt meta-object system.
 */
public interface IQGadget {
	/**
	 * Returns the name of the class.
	 */
	public String getName();

	/**
	 * Returns an unsorted collection of all Q_ENUMS macro expansions within this QObject's class
	 * declaration.
	 * @see IQEnum
	 */
	public Collection<IQEnum> getEnums();
}
