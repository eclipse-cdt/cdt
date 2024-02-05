/********************************************************************************
 * Copyright (c) 2023, 2024 Renesas Electronics Europe. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.cdt.managedbuilder.core.jsoncdb;

import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.jdt.annotation.NonNull;

/**
 * @since 9.7
 */
public interface ICompilationDatabaseContributor {

	/**
	 * @param config
	 *            Adds a new list of files to the compilation database
	 *            Implementors should provide concrete implementations of this
	 *            interface. IConfiguration will be taken as input, accessing the project and will generate a list of
	 *            additional files to be added to compile_commands.json
	 * @return A non-null list of files that will be added to compilation database
	 */
	@NonNull
	public List<CompilationDatabaseInformation> getAdditionalFiles(@NonNull IConfiguration config);

}
