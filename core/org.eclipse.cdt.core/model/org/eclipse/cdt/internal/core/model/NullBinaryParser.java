package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.core.runtime.IPath;

/**
 */
public class NullBinaryParser implements IBinaryParser {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser#getBinary(org.eclipse.core.runtime.IPath)
	 */
	public IBinaryFile getBinary(byte[] data, IPath path) throws IOException {
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
