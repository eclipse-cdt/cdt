/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Institute for Software (IFS)- initial API and implementation 
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.implementmethod;

import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;

/**
 * @author Emanuel Graf IFS
 *
 */
public class MethodToImplementConfig {
	
	private IASTSimpleDeclaration declaration;
	private ParameterHandler paraHandler;
	private boolean checked;
	
	public MethodToImplementConfig(IASTSimpleDeclaration declaration,
			ParameterHandler paraHandler) {
		super();
		this.declaration = declaration;
		this.paraHandler = paraHandler;
	}

	public IASTSimpleDeclaration getDeclaration() {
		return declaration;
	}

	public ParameterHandler getParaHandler() {
		return paraHandler;
	}
	
	@Override
	public String toString() {
		return declaration.getRawSignature();
	}
	
	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

}
