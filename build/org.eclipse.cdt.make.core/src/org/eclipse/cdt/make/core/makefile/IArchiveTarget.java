/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 */
public interface IArchiveTarget extends ITarget {

	/**
	 * Returns the members the point by archive target.
	 * @return String
	 */
	String[] getMembers();
}
