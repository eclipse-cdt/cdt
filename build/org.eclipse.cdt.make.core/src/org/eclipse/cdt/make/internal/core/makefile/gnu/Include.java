/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.makefile.gnu;

import java.io.IOException;

import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.gnu.IInclude;
import org.eclipse.cdt.make.internal.core.makefile.Directive;
import org.eclipse.cdt.make.internal.core.makefile.Parent;

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
		GNUMakefile gnu = new GNUMakefile();
		clearDirectives();
		for (int i = 0; i < filenames.length; i++) {
			// Try the current directory.
			try {
				gnu.parse(filenames[i]);
				Directive[] subdirs = gnu.getStatements();
				addDirectives(subdirs);
				for (int k = 0; k < subdirs.length; ++k) {
					subdirs[k].setFilename(filenames[i]);
				}				
				continue;
			} catch (IOException e) {
			}
			if (!filenames[i].startsWith(GNUMakefile.FILE_SEPARATOR) && dirs != null) {
				for (int j = 0; j < dirs.length; j++) {
					try {
						String filename =  dirs[j] + GNUMakefile.FILE_SEPARATOR + filenames[i];
						gnu.parse(filename);
						Directive[] subdirs = gnu.getStatements();
						addDirectives(subdirs);
						for (int k = 0; k < subdirs.length; ++k) {
							subdirs[k].setFilename(filename);
						}
						break;
					} catch (IOException e) {
					}
				}
			}
		}
		return super.getDirectives();
	}
}
