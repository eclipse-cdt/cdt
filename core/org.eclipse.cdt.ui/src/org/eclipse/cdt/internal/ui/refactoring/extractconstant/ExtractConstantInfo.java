/*******************************************************************************
 * Copyright (c) 2008, 2016 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Institute for Software (IFS) - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Thomas Corbat (IFS)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractconstant;

import java.util.function.Predicate;

import org.eclipse.cdt.internal.ui.refactoring.MethodContext;
import org.eclipse.cdt.internal.ui.refactoring.VariableNameInformation;
import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;

/**
 * @author Emanuel Graf IFS
 */
public class ExtractConstantInfo extends VariableNameInformation {
	private VisibilityEnum visibility = VisibilityEnum.v_private;
	private MethodContext methodContext;
	private Predicate<String> nameUsedChecker = (String) -> false;
	private boolean replaceAllLiterals = true;

	public boolean isReplaceAllOccurences() {
		return replaceAllLiterals;
	}

	public void setReplaceAllLiterals(boolean replaceAllLiterals) {
		this.replaceAllLiterals = replaceAllLiterals;
	}

	public VisibilityEnum getVisibility() {
		return visibility;
	}

	public void setVisibility(VisibilityEnum visibility) {
		this.visibility = visibility;
	}

	public MethodContext getMethodContext() {
		return methodContext;
	}
	public void setMethodContext(MethodContext context) {
		methodContext = context;
	}

	public void setNameUsedChecker(Predicate<String> nameOccupiedChecker) {
		this.nameUsedChecker = nameOccupiedChecker;
	}

	public boolean isNameUsed(String name) {
		return nameUsedChecker.test(name);
	}
}
