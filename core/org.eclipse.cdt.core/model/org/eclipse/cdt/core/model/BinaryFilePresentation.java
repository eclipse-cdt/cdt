/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
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
package org.eclipse.cdt.core.model;

import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;

/**
 * Allows to configure the presentation of binaries.
 *
 * <p> Clients may create subclasses. </p>
 * @since 4.0.1.
 */
public abstract class BinaryFilePresentation {

	final protected IBinaryFile fBinaryFile;

	/**
	 * Constructs the presentation object for a binary file.
	 */
	public BinaryFilePresentation(IBinaryFile binFile) {
		fBinaryFile = binFile;
	}

	/**
	 * Default implementation for showing binaries as part of the binary container.
	 * It is used whenever a IBinaryFile is not adaptable to BinaryFilePresentation.
	 */
	public static boolean showInBinaryContainer(IBinaryFile bin) {
		switch (bin.getType()) {
		case IBinaryFile.EXECUTABLE:
		case IBinaryFile.SHARED:
			return true;
		}
		return false;
	}

	/**
	 * Determines whether a binary is to be shown as part of the binary container.
	 * The default implementation returns <code>true</code> for executables and
	 * dynamic libraries.
	 */
	public boolean showInBinaryContainer() {
		return showInBinaryContainer(fBinaryFile);
	}
}
