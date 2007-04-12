/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.wizards;

import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.ui.wizards.ICWizardHandler;

/**
 * Build-system specific methods for Wizard Handler
 */
public interface ICBuildWizardHandler extends ICWizardHandler {

	public IToolChain[] getSelectedToolChains();
	public int getToolChainsCount();
	public String getPropertyId();
	public IProjectType getProjectType();
}
