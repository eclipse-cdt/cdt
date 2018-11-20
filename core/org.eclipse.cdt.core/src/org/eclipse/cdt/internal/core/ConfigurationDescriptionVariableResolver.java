/*******************************************************************************
 * Copyright (c) 2012 Anton Gorenkov and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;

public class ConfigurationDescriptionVariableResolver extends ConfigurationInfoVariableResolver {
	@Override
	protected String fetchConfigurationInfo(ICConfigurationDescription configuration) {
		return configuration.getDescription();
	}
}
