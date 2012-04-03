/*******************************************************************************
 * Copyright (c) 2012 Anton Gorenkov and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;

public class ConfigurationNameVariableResolver extends ConfigurationInfoVariableResolver {
	@Override
	protected String fetchConfigurationInfo(ICConfigurationDescription configuration) {
		return configuration.getName();
	}
}
