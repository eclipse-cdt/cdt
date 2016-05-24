/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.board;

import java.util.Collection;
import java.util.List;

public class PackageIndex {

	private List<ArduinoPackage> packages;

	public Collection<ArduinoPackage> getPackages() {
		return packages;
	}

}
