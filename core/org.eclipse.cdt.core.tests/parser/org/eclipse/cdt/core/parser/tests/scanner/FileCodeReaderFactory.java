/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.scanner;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContentProvider;

public class FileCodeReaderFactory extends InternalFileContentProvider {

    private static FileCodeReaderFactory instance;

    private FileCodeReaderFactory() {}

    
    @Override
	public InternalFileContent getContentForInclusion(String path) {
		return (InternalFileContent) FileContent.createForExternalFileLocation(path);
	}

    public static FileCodeReaderFactory getInstance() {
		if (instance == null)
			instance = new FileCodeReaderFactory();
		return instance;
	}

	@Override
	public InternalFileContent getContentForInclusion(IIndexFileLocation ifl, String astPath) {
		// not used as a delegate
		return null;
	}
}
