/*******************************************************************************
 * Copyright (c) 2019 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

/**
 * Classes and interfaces to parse a file 'compile_commands.json' produced by
 * cmake and to generate information about preprocessor symbols and include
 * paths of the files being compiled in order to support the CDT indexer/ syntax
 * highlighting.
 *
 * @author Martin Weber
 */
package org.eclipse.cdt.cmake.is.core;