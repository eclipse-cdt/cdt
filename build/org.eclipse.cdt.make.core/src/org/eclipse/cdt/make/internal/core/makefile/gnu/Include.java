/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.core.makefile.gnu;

import java.io.IOException;

import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.internal.core.makefile.Parent;

public class Include extends Parent {

	String[] filenames;
	String[] dirs;

	public Include(String[] files, String[] directories) {
		filenames = files;
		dirs = directories;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("include");
		for (int i = 0; i < filenames.length; i++) {
			sb.append(' ').append(filenames[i]);
		}
		return sb.toString();
	}

	public String[] getFilenames() {
		return filenames;
	}

	public IDirective[] getStatements() {
		GNUMakefile gnu = new GNUMakefile();
		clearStatements();
		for (int i = 0; i < filenames.length; i++) {
			// Try the current directory.
			try {
				gnu.parse(filenames[i]);
				addStatements(gnu.getStatements());
				continue;
			} catch (IOException e) {
			}
			if (!filenames[i].startsWith(GNUMakefile.FILE_SEPARATOR) && dirs != null) {
				for (int j = 0; j < dirs.length; j++) {
					try {
						String filename =  dirs[j] + GNUMakefile.FILE_SEPARATOR + filenames[i];
						gnu.parse(filename);
						addStatements(gnu.getStatements());
						break;
					} catch (IOException e) {
					}
				}
			}
		}
		return super.getStatements();
	}
}
