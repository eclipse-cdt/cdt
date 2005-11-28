/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ICodeReaderCache;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMCodeReaderFactory implements ICodeReaderFactory {

	private final PDOMDatabase pdom;
	private List workingCopies;
	
	public PDOMCodeReaderFactory(PDOMDatabase pdom) {
		this.pdom = pdom;
	}

	public PDOMCodeReaderFactory(PDOMDatabase pdom, IWorkingCopy workingCopy) {
		this(pdom);
		workingCopies = new ArrayList(1);
		workingCopies.add(workingCopy);
	}
	
	public int getUniqueIdentifier() {
		return 0;
	}

	public CodeReader createCodeReaderForTranslationUnit(String path) {
		return ParserUtil.createReader(path,
				workingCopies != null ? workingCopies.iterator() : null);
	}

	public CodeReader createCodeReaderForTranslationUnit(ITranslationUnit tu) {
		return new CodeReader(tu.getPath().toOSString(), tu.getContents());
	}
	
	public CodeReader createCodeReaderForInclusion(String path) {
		// Don't parse inclusion if it is already captured
		try {
			try {
				path = new File(path).getCanonicalPath();
			} catch (IOException e) {
				// ignore and use the path we were passed in
			}
			if (PDOMFile.find(pdom, path) != null)
				return null;
		} catch (CoreException e) {
			CCorePlugin.log(new CoreException(new Status(IStatus.ERROR,
					CCorePlugin.PLUGIN_ID, 0, "PDOM Exception", e)));
		}
		
		return ParserUtil.createReader(path, null);
	}

	public ICodeReaderCache getCodeReaderCache() {
		// No need for cache here
		return null;
	}

}
