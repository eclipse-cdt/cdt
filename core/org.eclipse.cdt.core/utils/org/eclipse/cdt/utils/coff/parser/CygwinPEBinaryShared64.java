/*******************************************************************************
 * Copyright (c) 2004, 2019 Space Codesign Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Space Codesign Systems - Initial API and implementation
 *     QNX Software Systems - initial CygwinPEBinaryShared class
 *******************************************************************************/
package org.eclipse.cdt.utils.coff.parser;

import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryShared;
import org.eclipse.core.runtime.IPath;

/**
 * @since 6.9
 */
public class CygwinPEBinaryShared64 extends CygwinPEBinaryObject64 implements IBinaryShared {

	protected CygwinPEBinaryShared64(IBinaryParser parser, IPath path) {
		super(parser, path, IBinaryFile.SHARED);
	}

}
