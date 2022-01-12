/*******************************************************************************
 * Copyright (c) 2012 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.core;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;

public interface IConfigurationCloneListener {
	/**
	 * Notified when a configuration gets cloned.
	 * @param cloneName - name of the cloned configuration
	 * @param c - the clone
	 */
	void cloneCfg(String cloneName, IConfiguration c);
}
