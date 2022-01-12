/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.makefile.gnu;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.cdt.make.core.makefile.IMakefileReaderProvider;
import org.eclipse.cdt.make.core.makefile.gnu.IInclude;
import org.eclipse.cdt.make.internal.core.makefile.Directive;
import org.eclipse.cdt.make.internal.core.makefile.Parent;
import org.eclipse.core.filesystem.URIUtil;
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(GNUMakefileConstants.DIRECTIVE_INCLUDE);
		for (int i = 0; i < filenames.length; i++) {
			sb.append(' ').append(filenames[i]);
		}
		return sb.toString();
	}

	@Override
	public String[] getFilenames() {
		return filenames;
	}

	private IMakefileReaderProvider getCurrentMakefileReaderProvider() {
		IDirective directive = this;
		while (directive != null) {
			if (directive instanceof IMakefile) {
				IMakefileReaderProvider makefileReaderProvider = ((IMakefile) directive).getMakefileReaderProvider();
				if (makefileReaderProvider != null)
					return makefileReaderProvider;
			}
			directive = directive.getParent();
		}
		return null;
	}

	@Override
	public IDirective[] getDirectives() {
		clearDirectives();
		URI uri = getMakefile().getFileURI();
		IMakefileReaderProvider makefileReaderProvider = getCurrentMakefileReaderProvider();
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
						URI includeURI = URIUtil.toURI(includeFilePath);
						if (!isAlreadyIncluded(includeURI)) {
							GNUMakefile gnu = new GNUMakefile();
							gnu.parse(includeURI, makefileReaderProvider);
							addDirective(gnu);
						}
						continue;
					} catch (IOException e) {
					}
				}
			} else if (dirs != null) {
				for (int j = 0; j < dirs.length; j++) {
					try {
						IPath testIncludeFilePath = new Path(dirs[j]).append(includeFilePath);
						String uriPath = testIncludeFilePath.toString();
						if (testIncludeFilePath.getDevice() != null) {
							// special case: device prefix is seen as relative path by URI
							uriPath = '/' + uriPath;
						}
						URI includeURI = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(),
								uriPath, null, null);
						if (!isAlreadyIncluded(includeURI)) {
							GNUMakefile gnu = new GNUMakefile();
							gnu.parse(includeURI, makefileReaderProvider);
							addDirective(gnu);
						}
						break;
					} catch (IOException e) {
					} catch (URISyntaxException exc) {
					}
				}
			}
		}
		return super.getDirectives();
	}

	private boolean isAlreadyIncluded(URI includeURI) {
		for (IDirective parent = getParent(); parent != null; parent = parent.getParent()) {
			if (parent instanceof IMakefile) {
				if (includeURI.equals(((IMakefile) parent).getFileURI())) {
					return true;
				}
			}
		}
		return false;
	}
}
