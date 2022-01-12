/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
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
package org.eclipse.cdt.make.internal.core.makefile;

/**
 * IArchiveTarget
 */
public class ArchiveTarget extends Target {

	String member;

	public ArchiveTarget(String lib, String obj) {
		super(lib);
		member = obj;
	}

	public String getMember() {
		return member;
	}

	public String getLibaryName() {
		return toString();
	}

}
