/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import java.io.IOException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.PlatformObject;

/**
 */
public class NullBinaryParser extends PlatformObject implements IBinaryParser {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser#getBinary(org.eclipse.core.runtime.IPath)
	 */
	public IBinaryFile getBinary(byte[] data, IPath path) throws IOException {
		throw new IOException(CCorePlugin.getResourceString("CoreModel.NullBinaryParser.Not_binary_file")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser#getBinary(org.eclipse.core.runtime.IPath)
	 */
	public IBinaryFile getBinary(IPath path) throws IOException {
		throw new IOException(CCorePlugin.getResourceString("CoreModel.NullBinaryParser.Not_binary_file")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser#getFormat()
	 */
	public String getFormat() {
		return CCorePlugin.getResourceString("CoreModel.NullBinaryParser.Null_Format"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser#isBinary(byte[], org.eclipse.core.runtime.IPath)
	 */
	public boolean isBinary(byte[] array, IPath path) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser#getBufferSize()
	 */
	public int getHintBufferSize() {
		return 0;
	}

}
