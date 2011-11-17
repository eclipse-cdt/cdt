/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom;

import java.io.IOException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.CodeReaderCache;
import org.eclipse.cdt.core.parser.ICodeReaderCache;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences;

/**
 * @author jcamelon
 */
@Deprecated
public class SavedCodeReaderFactory extends AbstractCodeReaderFactory {

	private static ICodeReaderCache cache;
    private static SavedCodeReaderFactory instance = new SavedCodeReaderFactory(null);

    public static SavedCodeReaderFactory getInstance() {
        return instance;
    }

    public static SavedCodeReaderFactory createInstance(IIncludeFileResolutionHeuristics heuristics) {
    	return new SavedCodeReaderFactory(heuristics);
    }


    private SavedCodeReaderFactory(IIncludeFileResolutionHeuristics heuristics) {
    	super(heuristics);

		if (cache == null) {
			int size= CodeReaderCache.DEFAULT_CACHE_SIZE_IN_MB;
			final CCorePlugin corePlugin = CCorePlugin.getDefault();
			if (corePlugin != null) {
				Preferences pluginPreferences = corePlugin.getPluginPreferences();
				if (pluginPreferences != null) {
					size = pluginPreferences.getInt(CodeReaderCache.CODE_READER_BUFFER);
					if (size == 0) {
						String [] properties = pluginPreferences.propertyNames();
						boolean found = false;
						for (int j = 0; j < properties.length; ++j) {
							if (properties[j].equals( CodeReaderCache.CODE_READER_BUFFER)) {
								found = true;
								break;
							}
						}
						if (!found) {
							size= CodeReaderCache.DEFAULT_CACHE_SIZE_IN_MB;
						}
					} else if (size < 0) {
						size= CodeReaderCache.DEFAULT_CACHE_SIZE_IN_MB;
					}
				}
			}
			cache= new CodeReaderCache(size);
		}
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ICodeReaderFactory#getUniqueIdentifier()
     */
    @Override
	public int getUniqueIdentifier() {
        return CDOM.PARSE_SAVED_RESOURCES;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ICodeReaderFactory#createCodeReaderForTranslationUnit(java.lang.String)
     */
    @Override
	public CodeReader createCodeReaderForTranslationUnit(String path) {
		return cache.get(path);
    }

    public CodeReader createCodeReaderForTranslationUnit(ITranslationUnit tu) {
		return new CodeReader(tu.getResource().getLocation().toOSString(), tu.getContents());
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ICodeReaderFactory#createCodeReaderForInclusion(java.lang.String)
     */
    @Override
	public CodeReader createCodeReaderForInclusion(String path) {
		return cache.get(path);
    }

	@Override
	public CodeReader createCodeReaderForInclusion(IIndexFileLocation ifl, String astPath)
			throws CoreException, IOException {
		return cache.get(astPath, ifl);
	}

	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ICodeReaderFactory#createCodeReaderForInclusion(java.lang.String)
     */
	@Override
	public ICodeReaderCache getCodeReaderCache() {
		return cache;
	}
}
