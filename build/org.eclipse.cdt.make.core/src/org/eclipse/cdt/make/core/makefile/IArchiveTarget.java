/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core.makefile;

/**
 * IArchiveTarget
 * Archive files, are files maintained by the program "ar".
 * They contain objects, the members of the Archive.
 * For example:
 *      foolib(hack.o) : hack.o
 *            ar cr foolib hack.o
 *  ArchiveTarget(member)  -- foolib(hack.o);
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IArchiveTarget extends ITarget {

	/**
	 * Returns the members the point by archive target.
	 * @return String
	 */
	String[] getMembers();
}
