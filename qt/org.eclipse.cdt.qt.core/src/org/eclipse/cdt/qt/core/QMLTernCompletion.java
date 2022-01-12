/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.qt.core;

public class QMLTernCompletion {

	private final String name;
	private final String type;
	private final String origin;

	public QMLTernCompletion(String name, String type, String origin) {
		this.name = name;
		this.type = type;
		this.origin = origin;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getOrigin() {
		return origin;
	}

}
