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
package org.eclipse.cdt.make.internal.core.makefile;

import java.io.File;

import org.eclipse.cdt.make.core.makefile.ITarget;

public class Target implements ITarget {

	String target;

	public Target(String t) {
		target = t;
	}

	@Override
	public String toString() {
		return target;
	}

	public boolean exits() {
		return new File(target).exists();
	}

	public long lastModified() {
		return new File(target).lastModified();
	}
}
