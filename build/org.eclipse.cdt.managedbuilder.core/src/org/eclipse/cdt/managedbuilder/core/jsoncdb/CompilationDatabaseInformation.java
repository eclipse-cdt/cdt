/********************************************************************************
 * Copyright (c) 2023, 2024 Renesas Electronics Corp. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.cdt.managedbuilder.core.jsoncdb;

/**
 * The compilation database array of “command objects” members to be used for
 * the extension point
 * directory: The working directory of the compilation.
 * command: The compile command. file: The main translation unit source
 * processed by this compilation step.
 * @since 9.7
 */

public record CompilationDatabaseInformation(String directory, String command, String file) {
}
