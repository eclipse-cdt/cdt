/*******************************************************************************
 * Copyright (c) 2007, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import org.eclipse.cdt.core.index.IIndexManager;

public class UpdateIndexWithModifiedFilesAction extends AbstractUpdateIndexAction {
	@Override
	protected int getUpdateOptions() {
		return IIndexManager.UPDATE_CHECK_TIMESTAMPS | IIndexManager.UPDATE_CHECK_CONFIGURATION
				| IIndexManager.UPDATE_EXTERNAL_FILES_FOR_PROJECT | IIndexManager.UPDATE_CHECK_CONTENTS_HASH
				| IIndexManager.UPDATE_UNRESOLVED_INCLUDES;
	}
}
