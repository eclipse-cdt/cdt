/*******************************************************************************
 * Copyright (c) 2006, 2007 Symbian Software Ltd. and others.
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
import org.eclipse.core.runtime.Path;

/**
 * The standard location converter used by the per-project PDOM
 */
public class PDOMProjectIndexLocationConverter implements IIndexLocationConverter {
	IWorkspaceRoot root;
	private static final String EXTERNAL = "<EXT>"; //$NON-NLS-1$
	
	public PDOMProjectIndexLocationConverter(IProject project) {
		this.root = ResourcesPlugin.getWorkspace().getRoot();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.pdom.dom.IIndexLocationConverter#fromInternalFormat(java.lang.String)
	 */
	public IIndexFileLocation fromInternalFormat(String raw) {
		String fullPath = null;
		URI uri;
		if(raw.startsWith(EXTERNAL)) {
			try {
				uri = new URI(raw.substring(EXTERNAL.length()));
			} catch(URISyntaxException use) {
				uri = null;
			}
		} else {
			fullPath = raw;  
			IResource member= root.getFile(new Path(raw));
			uri = member.getLocationURI();
		}		
		return uri == null ? null : new IndexFileLocation(uri, fullPath);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.pdom.dom.IIndexLocationConverter#toRaw(java.net.URI)
	 */
	public String toInternalFormat(IIndexFileLocation location) {
		String result;
		if(location.getFullPath()!=null) {
			result = new Path(location.getFullPath()).toString();
		} else {
			result = EXTERNAL+location.getURI().toString();
		}
		return result;
	}
}
