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
package org.eclipse.cdt.lsp.internal.core;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.osgi.util.NLS;

public final class ShowStatus implements Consumer<Supplier<Integer>> {

	private final Supplier<String> name;
	private final Consumer<String> target;

	public ShowStatus(Supplier<String> name, Consumer<String> target) {
		this.name = name;
		this.target = target;
	}

	@Override
	public void accept(Supplier<Integer> jobs) {
		target.accept(message(jobs.get()));
	}

	private String message(int total) {
		return total > 0 ? //
				NLS.bind(LspCoreMessages.ShowStatus_busy, name.get(), total) : //
				NLS.bind(LspCoreMessages.ShowStatus_idle, name.get());
	}

}
