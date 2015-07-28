/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.board;

import java.util.List;

public class PackageIndex {

	private List<BoardPackage> packages;

	public List<BoardPackage> getPackages() {
		return packages;
	}

	public BoardPackage getPackage(String packageName) {
		for (BoardPackage pkg : packages) {
			if (pkg.getName().equals(packageName)) {
				return pkg;
			}
		}
		return null;
	}

	void setOwners(ArduinoBoardManager manager) {
		for (BoardPackage pkg : packages) {
			pkg.setOwners(manager);
		}
	}

}
