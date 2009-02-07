/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Takuya Miyamoto - Adapted from org.eclipse.team.examples.filesystem / Policy
 *******************************************************************************/
package org.eclipse.rse.internal.synchronize.filesystem;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * This class is a clone of the Policy classes many Eclipse plugin use to
 * provide NLSing of strings and aid in proper progress monitoring.
 */
public class Policy {

	private static ResourceBundle bundle = null;
	//private static final String bundleName = "org.eclipse.rse.internal.synchronize.filesystem.messages"; //$NON-NLS-1$
	private static final String bundleName = Policy.class.getPackage().getName() + ".messages";

	/*
	 * Returns a resource bundle, creating one if it none is available.
	 */
	private static ResourceBundle getResourceBundle() {
		// thread safety
		ResourceBundle tmpBundle = bundle;
		if (tmpBundle != null)
			return tmpBundle;
		// always create a new classloader to be passed in
		// in order to prevent ResourceBundle caching
		return bundle = ResourceBundle.getBundle(bundleName);
		// return bundle = ResourceBundle.getBundle("");
	}

	/**
	 * Lookup the message with the given ID in this catalog and bind its
	 * substitution locations with the given string.
	 */
	public static String bind(String id, String binding) {
		return bind(id, new String[] { binding });
	}

	/**
	 * Lookup the message with the given ID in this catalog and bind its
	 * substitution locations with the given strings.
	 */
	public static String bind(String id, String binding1, String binding2) {
		return bind(id, new String[] { binding1, binding2 });
	}

	/**
	 * Gets a string from the resource bundle. We don't want to crash because of
	 * a missing String. Returns the key if not found.
	 */
	public static String bind(String key) {
		try {
			return getResourceBundle().getString(key);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Gets a string from the resource bundle and binds it with the given
	 * arguments. If the key is not found, return the key.
	 */
	public static String bind(String key, Object[] args) {
		try {
			return MessageFormat.format(bind(key), args);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Progress monitor helpers
	 */
	public static void checkCanceled(IProgressMonitor monitor) {
		if (monitor.isCanceled())
			throw new OperationCanceledException();
	}

	public static IProgressMonitor monitorFor(IProgressMonitor monitor) {
		if (monitor == null)
			return new NullProgressMonitor();
		return monitor;
	}
}
