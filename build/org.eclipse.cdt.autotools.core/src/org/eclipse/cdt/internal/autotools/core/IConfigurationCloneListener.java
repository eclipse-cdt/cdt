/*******************************************************************************
 * Copyright (c) 2012 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
