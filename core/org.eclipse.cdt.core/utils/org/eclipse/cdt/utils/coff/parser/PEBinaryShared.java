/*******************************************************************************
 * Copyright (c) 2004, 2008 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.coff.parser;

import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryShared;
import org.eclipse.core.runtime.IPath;

/**
 * @deprecated. Deprecated as of CDT 6.9. Use 64 bit version {@link PEBinaryShared64}.
 * This class is planned for removal in next major release.
 */
@Deprecated
public class PEBinaryShared extends PEBinaryObject implements IBinaryShared {

	public PEBinaryShared(IBinaryParser parser, IPath p) {
		super(parser, p, IBinaryFile.SHARED);
	}

}
