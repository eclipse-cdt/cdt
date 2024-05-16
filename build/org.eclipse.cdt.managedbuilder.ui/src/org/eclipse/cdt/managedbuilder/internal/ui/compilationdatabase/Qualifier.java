/********************************************************************************
 * Copyright (c) 2023, 2024 Renesas Electronics Corp. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui.compilationdatabase;

import java.util.function.Supplier;

public final class Qualifier implements Supplier<String> {

	@Override
	public String get() {
		return "org.eclipse.cdt.managedbuilder.ui"; //$NON-NLS-1$
	}

}
