/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser;

import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.internal.core.parser.scanner.AbstractCharArray;
import org.eclipse.cdt.internal.core.parser.scanner.CharArray;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent;

public abstract class CodeReaderAdapter {
	/**
	 * @deprecated avoid using the adapter, its for backwards compatibility, only.
	 */
	@Deprecated
	public static org.eclipse.cdt.core.parser.CodeReader adapt(FileContent content) {
		if (content == null)
			return null;
		return new org.eclipse.cdt.core.parser.CodeReader(content.getFileLocation(), extractBuffer(content));
	}

	private static char[] extractBuffer(FileContent content) {
		if (!(content instanceof InternalFileContent)) {
			throw new IllegalArgumentException("Invalid file content object!"); //$NON-NLS-1$
		}
		AbstractCharArray source = ((InternalFileContent) content).getSource();
		if (source instanceof CharArray) {
			return ((CharArray) source).getArray();
		}
		int len = source.tryGetLength();
		if (len < 0) {
			len = 0;
			while (source.isValidOffset(len))
				len++;
		}
		char[] result = new char[len];
		source.arraycopy(0, result, 0, len);
		return result;
	}
}
