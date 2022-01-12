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
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import java.io.IOException;

import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.gnu.IInclude;

public class Include extends Parent implements IInclude {

	String[] filenames;
	String[] dirs;

	public Include(Directive parent, String[] files, String[] directories) {
		super(parent);
		filenames = files.clone();
		dirs = directories.clone();
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
		return filenames.clone();
	}

	@Override
	public IDirective[] getDirectives() {
		clearDirectives();
		for (int i = 0; i < filenames.length; i++) {
			// Try the current directory.
			GNUAutomakefile gnu = new GNUAutomakefile();
			try {
				gnu.parse(filenames[i]);
				addDirective(gnu);
				continue;
			} catch (IOException e) {
			}
			if (!filenames[i].startsWith(GNUAutomakefile.FILE_SEPARATOR) && dirs != null) {
				for (int j = 0; j < dirs.length; j++) {
					try {
						String filename = dirs[j] + GNUAutomakefile.FILE_SEPARATOR + filenames[i];
						gnu = new GNUAutomakefile();
						gnu.parse(filename);
						addDirective(gnu);
						break;
					} catch (IOException e) {
					}
				}
			}
		}
		return super.getDirectives();
	}
}
