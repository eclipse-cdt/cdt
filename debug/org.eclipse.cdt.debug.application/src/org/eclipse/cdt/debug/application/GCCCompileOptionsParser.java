/*******************************************************************************
 * Copyright (c) 2013, 2014 Red Hat, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.application;

import org.eclipse.cdt.managedbuilder.language.settings.providers.GCCBuildCommandParser;

public class GCCCompileOptionsParser extends GCCBuildCommandParser {

	private String currentResourceName;

	public GCCCompileOptionsParser() {
		super();
	}

	public String getCurrentResourceName() {
		return currentResourceName;
	}

	public void setCurrentResourceName(String name) {
		currentResourceName = name;
	}

	@Override
	protected String parseResourceName(String line) {
		return getCurrentResourceName();
	}

	@Override
	public GCCCompileOptionsParser cloneShallow() throws CloneNotSupportedException {
		return (GCCCompileOptionsParser) super.cloneShallow();
	}

	@Override
	public GCCCompileOptionsParser clone() throws CloneNotSupportedException {
		return (GCCCompileOptionsParser) super.clone();
	}

}
