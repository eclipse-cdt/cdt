package org.eclipse.cdt.core.model;

/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
 
import org.eclipse.cdt.internal.core.model.IConstants;

/**
 * Utility class for decoding modifier flags in C elements.
 * <p>
 * This class provides static methods only; it is not intended to be
 * instantiated or subclassed by clients.
 * </p>
 *
 */
public final class Flags {
	/**
	 * Not instantiable.
	 */
	private Flags() {}

	/**
	 * Returns whether the given integer includes the <code>abstract</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>abstract</code> modifier is included
	 */
	public static boolean isAbstract(int flags) {
		return (flags & IConstants.AccAbstract) != 0;
	}

	/**
	 *
	 * Return whether the give integer include the keyword <code>export</code> modifier.
	 * @param flags the flags
	 * @return <code>true</code> if the element is <code>export</code>
	 */
	public static boolean isExport(int flags) {
		return (flags & IConstants.AccExport) != 0;
	}

	/**
	 * Returns whether the given integer includes the <code>inline</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>inline</code> modifier is included
	 */
	public static boolean isInline(int flags) {
		return (flags & IConstants.AccInline) != 0;
	}

	/**
	 * Returns whether the given integer includes the <code>explicit</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if <code>explicit</code> modifier is included
	 */
	public static boolean isExplicit(int flags) {
		return (flags & IConstants.AccExplicit) != 0;
	}

	/**
	 * Returns whether the given integer includes the <code>private</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>private</code> modifier is included
	 */
	public static boolean isPrivate(int flags) {
		return (flags & IConstants.AccPrivate) != 0;
	}

	/**
	 * Returns whether the given integer includes the <code>protected</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>protected</code> modifier is included
	 */
	public static boolean isProtected(int flags) {
		return (flags & IConstants.AccProtected) != 0;
	}

	/**
	 * Returns whether the given integer includes the <code>public</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>public</code> modifier is included
	 */
	public static boolean isPublic(int flags) {
		return (flags & IConstants.AccPublic) != 0;
	}

	/**
	 * Returns whether the given integer includes the <code>static</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>static</code> modifier is included
	 */
	public static boolean isStatic(int flags) {
		return (flags & IConstants.AccStatic) != 0;
	}

	/**
	 * Returns whether the given integer includes the <code>extern</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>extern</code> modifier is included
	 */
	public static boolean isExtern(int flags) {
		return (flags & IConstants.AccExtern) != 0;
	}
	/**
	 * Returns whether the given integer includes the <code>mutable</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>mutable</code> modifier is included
	 */
	public static boolean isMutable(int flags) {
		return (flags & IConstants.AccMutable) != 0;
	}

	/**
	 * Returns whether the given integer includes the indication that the 
	 * element is a register storage specifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the element is marked register storage specifier
	 */
	public static boolean isRegister(int flags) {
		return (flags & IConstants.AccRegister) != 0;
	}
	/**
	 * Returns whether the given integer includes the <code>virtual</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>virtual</code> modifier is included
	 */
	public static boolean isVirtual(int flags) {
		return (flags &  IConstants.AccVirtual) != 0;
	}

	/**
	 * Returns whether the given integer includes the <code>volatile</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>volatile</code> modifier is included
	 */
	public static boolean isVolatile(int flags) {
		return (flags & IConstants.AccVolatile) != 0;
	}

	/**
	 * Returns a standard string describing the given modifier flags.
	 * Only modifier flags are included in the output; the deprecated and
	 * synthetic flags are ignored if set.
	 * <p>
	 * Examples results:
	 * <pre>
	 *	  <code>"public static"</code>
	 *	  <code>"private"</code>
	 * </pre>
	 * </p>
	 *
	 * @param flags the flags
	 * @return the standard string representation of the given flags
	 */
	public static String toString(int flags) {
		StringBuffer sb = new StringBuffer();

		if (isPublic(flags))	sb.append("public "); //$NON-NLS-1$
		if (isProtected(flags)) sb.append("protected "); //$NON-NLS-1$
		if (isPrivate(flags))	sb.append("private "); //$NON-NLS-1$
		if (isStatic(flags)) sb.append("static "); //$NON-NLS-1$
		if (isAbstract(flags)) sb.append("abstract "); //$NON-NLS-1$
		if (isVirtual(flags)) sb.append("virtual "); //$NON-NLS-1$
		if (isInline(flags)) sb.append("inline "); //$NON-NLS-1$
		if (isExtern(flags)) sb.append("extern "); //$NON-NLS-1$
		if (isExport(flags)) sb.append("export "); //$NON-NLS-1$
		if (isVolatile(flags)) sb.append("volatile "); //$NON-NLS-1$
		if (isExplicit(flags)) sb.append("explicit "); //$NON-NLS-1$

		int len = sb.length();
		if (len == 0) return ""; //$NON-NLS-1$
		sb.setLength(len-1);
		return sb.toString();
	}
}
