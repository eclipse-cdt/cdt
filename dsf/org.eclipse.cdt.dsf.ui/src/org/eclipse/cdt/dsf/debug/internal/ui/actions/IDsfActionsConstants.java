/*******************************************************************************
 * Copyright (c) 2016 IAR Systems AB
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IAR Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.actions;

/**
 * Constants used by the DSF UI action adapters
 *
 * @noimplement This interface is not intended to be implemented by clients.
 */
interface IDsfActionsConstants {
	/**
	 * The timeout in ms which action adapters will wait before disabling
	 * the action itself, in order to avoid blocking the UI thread while
	 * waiting for the DSF thread to service a blocking query.
	 */
	static final int ACTION_ADAPTERS_TIMEOUT_MS = 500;

}
