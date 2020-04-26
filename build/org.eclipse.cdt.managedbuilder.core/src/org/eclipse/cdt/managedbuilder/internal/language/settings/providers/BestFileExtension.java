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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public final class BestFileExtension implements Function<List<String>, Optional<String>> {

	@Override
	public Optional<String> apply(List<String> list) {
		return list.stream()//
				.filter(Objects::nonNull)//
				.filter(s -> !s.isEmpty())//
				.findFirst().map(e -> preferCpp(e, list));
	}

	private String preferCpp(String ext, List<String> extensions) {
		// Bug 562452: Special case where we prefer not to use .C for c++ files.
		if ("C".equals(ext) && extensions.contains("cpp")) { //$NON-NLS-1$//$NON-NLS-2$
			return "cpp"; //$NON-NLS-1$
		}
		return ext;
	}

}
