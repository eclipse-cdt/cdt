/*******************************************************************************
 * Copyright (c) 2012, 2016 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.autotools.core;

/**
 * @since 1.2
 */
public class AutotoolsOptionConstants {
	// IAutotoolOption Names
	public static final String TOOL_CONFIGURE = "configure"; //$NON-NLS-1$
	public static final String CATEGORY_GENERAL = "general"; //$NON-NLS-1$
	public static final String OPT_CONFIGDIR = "configdir"; //$NON-NLS-1$
	public static final String OPT_CACHE_FILE = "cache-file"; //$NON-NLS-1$
	public static final String OPT_HELP = "help"; //$NON-NLS-1$
	public static final String OPT_NO_CREATE = "no-create"; //$NON-NLS-1$
	public static final String OPT_QUIET = "quiet"; //$NON-NLS-1$
	public static final String OPT_VERSION = "version"; //$NON-NLS-1$
	public static final String CATEGORY_PLATFORM = "platform"; //$NON-NLS-1$
	public static final String OPT_HOST = "host"; //$NON-NLS-1$
	public static final String OPT_BUILD = "build"; //$NON-NLS-1$
	public static final String OPT_TARGET = "target"; //$NON-NLS-1$
	public static final String CATEGORY_DIRECTORIES = "directories"; //$NON-NLS-1$
	public static final String OPT_PREFIX = "prefix"; //$NON-NLS-1$
	public static final String OPT_EXEC_PREFIX = "exec-prefix"; //$NON-NLS-1$
	public static final String OPT_LIBDIR = "libdir"; //$NON-NLS-1$
	public static final String OPT_BINDIR = "bindir"; //$NON-NLS-1$
	public static final String OPT_SBINDIR = "sbindir"; //$NON-NLS-1$
	public static final String OPT_INCLUDEDIR = "includedir"; //$NON-NLS-1$
	public static final String OPT_DATADIR = "datadir"; //$NON-NLS-1$
	public static final String OPT_SYSCONFDIR = "sysconfdir"; //$NON-NLS-1$
	public static final String OPT_INFODIR = "infodir"; //$NON-NLS-1$
	public static final String OPT_MANDIR = "mandir"; //$NON-NLS-1$
	public static final String OPT_SRCDIR = "srcdir"; //$NON-NLS-1$
	public static final String OPT_LOCALSTATEDIR = "localstatedir"; //$NON-NLS-1$
	public static final String OPT_SHAREDSTATEDIR = "sharedstatedir"; //$NON-NLS-1$
	public static final String OPT_LIBEXECDIR = "libexecdir"; //$NON-NLS-1$
	public static final String OPT_OLDINCLUDEDIR = "oldincludedir"; //$NON-NLS-1$
	public static final String CATEGORY_FILENAMES = "filenames"; //$NON-NLS-1$
	public static final String OPT_PROGRAM_PREFIX = "program-prefix"; //$NON-NLS-1$
	public static final String OPT_PROGRAM_SUFFIX = "program-suffix"; //$NON-NLS-1$
	public static final String OPT_PROGRAM_TRANSFORM_NAME = "program-transform-name"; //$NON-NLS-1$
	public static final String CATEGORY_FEATURES = "features"; //$NON-NLS-1$
	public static final String OPT_ENABLE_MAINTAINER_MODE = "enable-maintainer-mode"; //$NON-NLS-1$
	public static final String FLAG_CFLAGS = "CFLAGS"; //$NON-NLS-1$
	/**
	 * @since 1.4
	 */
	public static final String FLAG_CFLAGS_FLAGS = "CFLAGS|CXXFLAGS"; //$NON-NLS-1$
	public static final String OPT_CFLAGS_DEBUG = "cflags-debug"; //$NON-NLS-1$
	public static final String OPT_CFLAGS_GPROF = "cflags-gprof"; //$NON-NLS-1$
	public static final String OPT_CFLAGS_GCOV = "cflags-gcov"; //$NON-NLS-1$
	public static final String OPT_USER = "user"; //$NON-NLS-1$
	public static final String TOOL_AUTOGEN = "autogen"; //$NON-NLS-1$
	public static final String CATEGORY_OPTIONS = "options"; //$NON-NLS-1$
	public static final String OPT_AUTOGENOPTS = "autogenOpts"; //$NON-NLS-1$
	/**
	 * @since 2.0
	 */
	public static final String CATEGORY_ENVVAR = "cat_envvar"; //$NON-NLS-1$
	/**
	 * @since 2.0
	 */
	public final static String OPT_ENVVAR = "env_vars"; //$NON-NLS-1$

}
