/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.parser;

import java.io.IOException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.internal.core.dom.AbstractCodeReaderFactory;
import org.eclipse.cdt.internal.core.dom.IIncludeFileResolutionHeuristics;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContentProvider;
import org.eclipse.core.runtime.CoreException;

@Deprecated
public class FileContentProviderAdapter extends InternalFileContentProvider {

	/**
	 * @deprecated avoid using the adapter, it's for backwards compatibility, only.
	 */
	@Deprecated
	public static InternalFileContentProvider adapt(ICodeReaderFactory fileCreator) {
		if (fileCreator == null)
			return null;
		
		if (!(fileCreator instanceof AbstractCodeReaderFactory))
			throw new IllegalArgumentException("Invalid code reader factory"); //$NON-NLS-1$
		
		if (fileCreator instanceof CodeReaderFactoryAdapter) {
			return ((CodeReaderFactoryAdapter) fileCreator).getFileContentProvider();
		}
		return new FileContentProviderAdapter((AbstractCodeReaderFactory) fileCreator);
	}

	private AbstractCodeReaderFactory fDelegate;
	private FileContentProviderAdapter(AbstractCodeReaderFactory factory) {
		fDelegate= factory;
		setIncludeResolutionHeuristics((IIncludeFileResolutionHeuristics) factory.getAdapter(IIncludeFileResolutionHeuristics.class));
	}

	/**
	 * @deprecated avoid using the adapter, its for backwards compatibility, only.
	 */
	@Deprecated
	public org.eclipse.cdt.core.dom.ICodeReaderFactory getCodeReaderFactory() {
		return fDelegate;
	}

	@Override
	public InternalFileContent getContentForInclusion(String path, IMacroDictionary macroDictionary) {
		return (InternalFileContent) FileContent.adapt(fDelegate.createCodeReaderForInclusion(path));
	}

	@Override
	public InternalFileContent getContentForInclusion(IIndexFileLocation ifl, String astPath) {
		try {
			return (InternalFileContent) FileContent.adapt(fDelegate.createCodeReaderForInclusion(ifl, astPath));
		} catch (CoreException e) {
			CCorePlugin.log(e);
		} catch (IOException e) {
			CCorePlugin.log(e);
		}
		return null;
	}
}
