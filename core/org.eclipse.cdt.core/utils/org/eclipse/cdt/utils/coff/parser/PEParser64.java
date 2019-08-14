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
 *     QNX Software Systems - Initial PEParser class
 *     Space Codesign Systems - Support for 64 bit executables
 *******************************************************************************/

package org.eclipse.cdt.utils.coff.parser;

import java.io.IOException;

import org.eclipse.core.runtime.IPath;

/**
 * @since 6.9
 */
public class PEParser64 extends PEParser {

	@Override
	protected IBinaryExecutable createBinaryExecutable(IPath path) {
		return new PEBinaryExecutable64(this, path);
	}

	@Override
	protected IBinaryObject createBinaryCore(IPath path) {
		return new PEBinaryObject64(this, path, IBinaryFile.CORE);
	}

	@Override
	protected IBinaryObject createBinaryObject(IPath path) {
		return new PEBinaryObject64(this, path, IBinaryFile.OBJECT);
	}

	@Override
	protected IBinaryShared createBinaryShared(IPath path) {
		return new PEBinaryShared64(this, path);
	}

	@Override
	protected IBinaryArchive createBinaryArchive(IPath path) throws IOException {
		return new PEBinaryArchive64(this, path);
	}

}
