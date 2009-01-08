/*******************************************************************************
 * Copyright (c) 2000, 2005, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     ERicsson             - Updated
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * 
 *      -environment-directory [-r] PATHDIR
 *
 *   Add directory PATHDIR to beginning of search path for source files.
 *   -r will first reset the path to its default
 * 
 */
public class MIEnvironmentDirectory extends MICommand<MIInfo> {
	
	public MIEnvironmentDirectory(IDMContext ctx, String[] paths, boolean reset) {
		super(ctx, "-environment-directory"); //$NON-NLS-1$

		String[] options;
		if (reset) {
			if (paths == null) {
				options = new String[] {"-r"}; //$NON-NLS-1$
			} else {
				options = new String[paths.length + 1];
				options[0] = "-r"; //$NON-NLS-1$
				for (int i = 1; i < options.length; i++) {
					options[i] = paths[i-1];
				}
			}
		} else {
			options = paths;
		}
		
		setOptions(options);
	}
}