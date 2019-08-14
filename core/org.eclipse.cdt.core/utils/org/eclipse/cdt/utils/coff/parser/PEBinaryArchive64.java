/*******************************************************************************
 * Copyright (c) 2019 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial PEBinaryArchive class
 *     Space Codesign Systems - Support for 64 bit executables
 *******************************************************************************/
package org.eclipse.cdt.utils.coff.parser;

import java.io.IOException;

import org.eclipse.core.runtime.IPath;

/**
 * @since 6.9
 */
public class PEBinaryArchive64 extends PEBinaryArchive {
	public PEBinaryArchive64(PEParser parser, IPath path) throws IOException {
		super(parser, path);
	}
}
