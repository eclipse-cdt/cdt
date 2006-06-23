/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.elf.parser;

import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryShared;
import org.eclipse.core.runtime.IPath;


public class ElfBinaryShared extends ElfBinaryObject implements IBinaryShared {

	/**
	 * @param parser
	 * @param p
	 */
	public ElfBinaryShared(ElfParser parser, IPath p) {
		super(parser, p, IBinaryFile.SHARED);
	}
}
