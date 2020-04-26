/*******************************************************************************
 * Copyright (c) 2020 ArSysOp and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov (ArSysOp) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.language.settings.providers;

import java.util.function.Function;

import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.osgi.util.NLS;

/**
 * Encapsulates processing for not found file extension
 *
 */
public final class NotFoundFileExtension implements Function<String, String> {

	@Override
	public String apply(String languageId) {
		ManagedBuilderCorePlugin.error(NLS.bind("Unable to find file extension for language {0}", languageId)); //$NON-NLS-1$
		return null;
	}

}
