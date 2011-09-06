/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.parser;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContentProvider;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;


public class SavedFilesProvider extends InternalFileContentProvider {
	final private static SavedFilesProvider INSTANCE= new SavedFilesProvider();

	public static InternalFileContentProvider getInstance() {
		return INSTANCE;
	}
	
	private SavedFilesProvider() {
	}
	
	@Override
	public InternalFileContent getContentForInclusion(String path,
			IMacroDictionary macroDictionary) {
		if (!getInclusionExists(path))
			return null;
		
		IResource file = ParserUtil.getResourceForFilename(path);
		if (file instanceof IFile) {
			return (InternalFileContent) FileContent.create((IFile) file);
		}
		return (InternalFileContent) FileContent.createForExternalFileLocation(path);
	}

	@Override
	public InternalFileContent getContentForInclusion(IIndexFileLocation ifl, String astPath) {
		return (InternalFileContent) FileContent.create(ifl);
	}
}
