/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.ui.build;

import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.jface.wizard.Wizard;

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
