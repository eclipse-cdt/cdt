/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.board;

import java.util.List;

public class PackageIndex {

	private List<Package> packages;

	public List<Package> getPackages() {
		return packages;
	}

	public Package getPackage(String packageName) {
		for (Package pkg : packages) {
			if (pkg.getName().equals(packageName)) {
				return pkg;
			}
		}
		return null;
	}

}
