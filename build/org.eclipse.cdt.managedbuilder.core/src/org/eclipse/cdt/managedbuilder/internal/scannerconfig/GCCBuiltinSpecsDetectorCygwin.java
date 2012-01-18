/*******************************************************************************
 * Copyright (c) 2009, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.internal.scannerconfig;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.core.resources.IResource;

/**
 * Class to detect built-in compiler settings.
 * The paths are converted to cygwin "filesystem" representation. Then 
 *
 */
public class GCCBuiltinSpecsDetectorCygwin extends GCCBuiltinSpecsDetector {
	private static final URI CYGWIN_ROOT;
	static {
		try {
			CYGWIN_ROOT = new URI("cygwin:/"); //$NON-NLS-1$
		} catch (URISyntaxException e) {
			// hey we know this works
			throw new IllegalStateException(e);
		}
	}
	
	@SuppressWarnings("nls")
	private static final AbstractOptionParser[] optionParsers = {
			new IncludePathOptionParser("#include \"(\\S.*)\"", "$1", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY | ICSettingEntry.LOCAL),
			new IncludePathOptionParser("#include <(\\S.*)>", "$1", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
			new MacroOptionParser("#define (\\S*\\(.*?\\)) *(.*)", "$1", "$2", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
			new MacroOptionParser("#define (\\S*) *(.*)", "$1", "$2", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
	};

	@Override
	protected AbstractOptionParser[] getOptionParsers() {
		return optionParsers;
	}

	@Override
	protected URI getMappedRootURI(IResource sourceFile, String parsedResourceName) {
		if (mappedRootURI==null) {
			mappedRootURI = super.getMappedRootURI(sourceFile, parsedResourceName);
			if (mappedRootURI==null) {
				mappedRootURI = CYGWIN_ROOT;
			}
		}
		return mappedRootURI;
	}
	
	@Override
	protected URI getBuildDirURI(URI mappedRootURI) {
		if (buildDirURI==null) {
			buildDirURI = super.getBuildDirURI(mappedRootURI);
			if (buildDirURI==null) {
				buildDirURI = CYGWIN_ROOT;
			}
		}
		return buildDirURI;
	}

	@Override
	public GCCBuiltinSpecsDetectorCygwin cloneShallow() throws CloneNotSupportedException {
		return (GCCBuiltinSpecsDetectorCygwin) super.cloneShallow();
	}

	@Override
	public GCCBuiltinSpecsDetectorCygwin clone() throws CloneNotSupportedException {
		return (GCCBuiltinSpecsDetectorCygwin) super.clone();
	}

}
