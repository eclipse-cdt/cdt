/*******************************************************************************
 * Copyright (c) 2006, 2008 Symbian Software Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/ 
 package org.eclipse.cdt.internal.core.pdom.dom;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.internal.core.index.IndexFileLocation;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * The standard location converter used by the per-project PDOM
 */
public class PDOMProjectIndexLocationConverter implements IIndexLocationConverter {
	private static final String EXTERNAL = "<EXT>"; //$NON-NLS-1$
	private static final String WS = "<WS>"; //$NON-NLS-1$
	
	final private IWorkspaceRoot fRoot;
	final private String fFullPathPrefix;
	final private boolean fIgnoreExternal;

	public PDOMProjectIndexLocationConverter(IProject project) {
		this(project, false);
	}

	public PDOMProjectIndexLocationConverter(IProject project, boolean ignoreWSExternal) {
		fRoot= (IWorkspaceRoot) project.getParent();
		fFullPathPrefix= project.getFullPath().toString() + IPath.SEPARATOR;
		fIgnoreExternal= ignoreWSExternal;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.pdom.dom.IIndexLocationConverter#fromInternalFormat(java.lang.String)
	 */
	@Override
	public IIndexFileLocation fromInternalFormat(String raw) {
		String fullPath = null;
		URI uri= null;
		if(raw.startsWith(EXTERNAL)) {
			try {
				uri = new URI(raw.substring(EXTERNAL.length()));
			} catch(URISyntaxException use) {
			}
		} else {
			if (raw.startsWith(WS)) {
				fullPath= raw.substring(WS.length());
			} else if (raw.startsWith(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString())){
				// bug 421037: if raw is a absolute path, for example, when calling for local type hierarchy, need to retrieve the
				// file name by get the last segment of its path, and then appended it to the fFullPathPrefix to get the fullPath.
				IPath rawPath = new Path(raw); 
				fullPath = fFullPathPrefix + rawPath.lastSegment();
			} else {
				fullPath= fFullPathPrefix+raw;  
			}
			final IPath path= new Path(fullPath);
			if (path.segmentCount() > 1) {
				IResource member= fRoot.getFile(path);
				uri = member.getLocationURI();
			}
		} 
		return uri == null ? null : new IndexFileLocation(uri, fullPath);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.pdom.dom.IIndexLocationConverter#toRaw(java.net.URI)
	 */
	@Override
	public String toInternalFormat(IIndexFileLocation location) {
		String fullPath= location.getFullPath();
		if(fullPath!=null) {
			if (fullPath.startsWith(fFullPathPrefix)) {
				return fullPath.substring(fFullPathPrefix.length());
			} 
			return WS + fullPath;
		}
	
		if (fIgnoreExternal)
			return null;
		
		return EXTERNAL+location.getURI().toString();
	}
}
