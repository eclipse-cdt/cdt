/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.core.builder.model;

/**
 * Standard build configuration constants.
 */
public interface ICPosixBuildConstants {
	public final static String CPP_INCLUDES = "posix.cpp.includes"; //$NON-NLS-1$
	public final static String CPP_DEFINITIONS = "posix.cpp.definitions"; //$NON-NLS-1$

	public final static String CC_ENABLE_PROFILE = "posix.cc.profile"; //$NON-NLS-1$
	public final static String CC_ENABLE_DEBUG = "posix.cc.debug"; //$NON-NLS-1$
	public final static String CC_ENABLE_OPTIMIZE = "posix.cc.optimize"; //$NON-NLS-1$
	public final static String CC_OPTIMZE_LEVEL = "posix.cc.optimize.level"; //$NON-NLS-1$
	public final static String CC_USER_ARGS = "posix.cc.user"; //$NON-NLS-1$

	public final static String CC_OPTIMIZE_NONE = "none"; //$NON-NLS-1$
	public final static String CC_OPTIMIZE_SOME = "some"; //$NON-NLS-1$
	public final static String CC_OPTIMIZE_FULL = "full"; //$NON-NLS-1$

	public final static String CC_WARN_ALL = "posix.cc.warn.all"; //$NON-NLS-1$
	public final static String CC_WARN_ASERROR = "posix.cc.warn.aserror"; //$NON-NLS-1$
	public final static String CC_WARN_FORMAT = "posix.cc.warn.format"; //$NON-NLS-1$
	public final static String CC_WARN_POINTERAR = "posix.cc.warn.pointerar"; //$NON-NLS-1$
	public final static String CC_WARN_SWITCH = "posix.cc.warn.switch"; //$NON-NLS-1$
	public final static String CC_WARN_UNREACH = "posix.cc.warn.unreach"; //$NON-NLS-1$
	public final static String CC_WARN_UNUSED = "posix.cc.warn.unused"; //$NON-NLS-1$

	public final static String LD_OUTPUT = "posix.ld.output"; //$NON-NLS-1$
	public final static String LD_USER_ARGS = "posix.ld.user"; //$NON-NLS-1$
	public final static String LD_LINK_STATIC = "posix.ld.link.static"; //$NON-NLS-1$
	public final static String LD_LINK_AS_PROGRAM = "posix.ld.link.as.program"; //$NON-NLS-1$
	public final static String LD_LINK_AS_SHARED = "posix.ld.link.as.shared"; //$NON-NLS-1$
	public final static String LD_LINK_AS_ARCHIVE = "posix.ld.link.as.archive"; //$NON-NLS-1$
	public final static String LD_STRIP = "posix.ld.strip"; //$NON-NLS-1$
	public final static String LD_LIBS = "posix.ld.libs"; //$NON-NLS-1$
	public final static String LD_LIBPATHS = "posix.ld.libpaths"; //$NON-NLS-1$
}
