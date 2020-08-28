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
package org.eclipse.cdt.internal.ui.editor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.function.Predicate;

/**
 *
 * Isolates the knowledge about marker types to ignore
 *
 */
final class IgnoredMarkerTypes implements Predicate<String> {

	private final Collection<String> ignored;

	IgnoredMarkerTypes() {
		//FIXME: read from extensions to avoid implicit dependency
		this(Collections.singletonList("org.eclipse.lsp4e.diagnostic")); //$NON-NLS-1$
	}

	IgnoredMarkerTypes(Collection<String> ignored) {
		this.ignored = new HashSet<>(ignored);
	}

	@Override
	public boolean test(String type) {
		return ignored.contains(type);
	}

}
