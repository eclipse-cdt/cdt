/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    QNX Software Systems (Alena Laskavaia)  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.breakpointactions;

/**
 * This interface intended to pass arbitrary debugger command to backend,
 * usually intended for command line debugger or scripting. Debugger can interpret this as it see fit
 * (including splitting this into multiple commands)
 *
 * @since 8.0
 */
public interface ICLIDebugActionEnabler {
	void execute(String command) throws Exception;
}
