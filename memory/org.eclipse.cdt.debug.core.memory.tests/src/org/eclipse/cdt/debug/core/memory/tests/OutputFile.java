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
package org.eclipse.cdt.debug.core.memory.tests;

import java.io.File;
import java.util.function.Supplier;

final class OutputFile implements Supplier<File> {

	private final String base;
	private final String relative;

	public OutputFile(String relative) {
		this.relative = relative;
		this.base = System.getProperty("project.build.directory", //$NON-NLS-1$
				System.getProperty("user.dir") + File.separator + "target"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public File get() {
		return new File(base + File.separator + relative);
	}

}
