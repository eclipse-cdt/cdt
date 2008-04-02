/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.makefile.gnu;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.gnu.IInclude;
import org.eclipse.cdt.make.internal.core.makefile.Directive;
import org.eclipse.cdt.make.internal.core.makefile.Parent;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class Include extends Parent implements IInclude {

	String[] filenames;
	String[] dirs;

	public Include(Directive parent, String[] files, String[] directories) {
		super(parent);
		filenames = files;
		dirs = directories;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer(GNUMakefileConstants.DIRECTIVE_INCLUDE);
		for (int i = 0; i < filenames.length; i++) {
			sb.append(' ').append(filenames[i]);
		}
		return sb.toString();
	}

	public String[] getFilenames() {
		return filenames;
	}

	public IDirective[] getDirectives() {
		clearDirectives();
		URI uri = getMakefile().getFileURI();
		for (int i = 0; i < filenames.length; i++) {
			IPath includeFilePath = new Path(filenames[i]);
			if (includeFilePath.isAbsolute()) {
				// Try to set the device to that of the parent makefile.
				final IPath path = URIUtil.toPath(uri);
				if (path != null) {
					String device = path.getDevice();
					if (device != null && includeFilePath.getDevice() == null) {
						includeFilePath = includeFilePath.setDevice(device);
					}
					try {
						GNUMakefile gnu = new GNUMakefile();
						final InputStreamReader reader = new InputStreamReader(new FileInputStream(includeFilePath.toFile()));
						gnu.parse(includeFilePath.toOSString(), reader);
						addDirective(gnu);
						continue;
					} catch (IOException e) {
					}
				}
			} else if (dirs != null) {
				for (int j = 0; j < dirs.length; j++) {
					try {
						includeFilePath= new Path(dirs[j]).append(includeFilePath);
						String uriPath = includeFilePath.toString();
						if (includeFilePath.getDevice() != null) {
							// special case: device prefix is seen as relative path by URI
							uriPath = '/' + uriPath;
						}
						GNUMakefile gnu = new GNUMakefile();
						URI includeURI = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uriPath, null, null);
						IFileStore store = EFS.getStore(includeURI);
						gnu.parse(includeURI, new InputStreamReader(store.openInputStream(0, null)));
						addDirective(gnu);
						break;
					} catch (IOException e) {
					} catch (URISyntaxException exc) {
					} catch (CoreException exc) {
					}
				}
			}
		}
		return super.getDirectives();
	}
}
