/*******************************************************************************
 * Copyright (c) 2013, 2014 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

/**
 * Handler for {@link org.eclipse.cdt.internal.ui.actions.UpdateIndexWithModifiedFilesAction}
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class UpdateUnresolvedIncludesHandler extends AbstractUpdateIndexHandler {
	private final UpdateUnresolvedIncludesAction updateAction = new UpdateUnresolvedIncludesAction();

	@Override
	public AbstractUpdateIndexAction getAction() {
		return updateAction;
	}
}
