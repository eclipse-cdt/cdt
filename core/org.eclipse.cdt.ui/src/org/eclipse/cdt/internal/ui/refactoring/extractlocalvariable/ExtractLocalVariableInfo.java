/*******************************************************************************
 * Copyright (c) 2018 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Institute for Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractlocalvariable;

import org.eclipse.cdt.internal.ui.refactoring.VariableNameInformation;



public class ExtractLocalVariableInfo extends VariableNameInformation {
	private boolean replaceAll = true;

	public boolean isReplaceAllOccurrences() {
		return replaceAll;
	}

	public void setReplaceAllOccurrences(boolean replaceAllLiterals) {
		this.replaceAll = replaceAllLiterals;
	}
}
