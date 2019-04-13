/*******************************************************************************
 * Copyright (c) 2016, 2019 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Red Hat Inc. - modified for use with Autotools builder
 *******************************************************************************/
package org.eclipse.cdt.core.autotools.core;

import org.eclipse.cdt.core.autotools.core.internal.AutotoolsToolChainEvent;

/**
 * Listener for toolchain events.
 */
public interface IAutotoolsToolChainListener {

	void handleAutotoolsToolChainEvent(AutotoolsToolChainEvent event);

}
