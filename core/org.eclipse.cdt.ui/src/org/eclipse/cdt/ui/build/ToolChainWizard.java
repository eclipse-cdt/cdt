/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.ui.build;

import org.eclipse.jface.wizard.Wizard;

import org.eclipse.cdt.core.build.IToolChain;

/**
 * Parent class for toolChain wizards that create or edit toolchains.
 * 
 * @since 6.3
 */
public abstract class ToolChainWizard extends Wizard {

	protected IToolChain toolChain;

	public void setToolChain(IToolChain toolChain) {
		this.toolChain = toolChain;
	}

	public IToolChain getToolChain() {
		return toolChain;
	}

}
