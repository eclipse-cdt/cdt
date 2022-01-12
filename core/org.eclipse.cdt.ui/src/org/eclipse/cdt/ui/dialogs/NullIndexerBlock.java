/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui.dialogs;

import java.util.Properties;

import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Bogdan Gheorghe
 * @noextend This class is not intended to be subclassed by clients.
 */
public class NullIndexerBlock extends AbstractIndexerPage {

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		ControlEnableState.disable(getControl());
	}

	@Override
	public Properties getProperties() {
		return new Properties();
	}

	@Override
	public void setProperties(Properties properties) {
	}
}
