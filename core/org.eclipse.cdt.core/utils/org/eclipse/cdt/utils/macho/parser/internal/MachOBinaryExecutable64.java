/*******************************************************************************
 * Copyright (c) 2004, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.macho.parser.internal;

import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryExecutable;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.core.runtime.IPath;


/**
 *  This file is a temporary solution to work around API restrictions for CDT 6.0.X. 
 *  This class is non-API in CDT 6.0.X and is not intended to be referenced.
 */
class MachOBinaryExecutable64 extends MachOBinaryObject64 implements IBinaryExecutable {

	public MachOBinaryExecutable64(IBinaryParser parser, IPath path) {
		super(parser, path, IBinaryFile.EXECUTABLE);
	}

}
