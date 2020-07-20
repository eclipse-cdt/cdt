/*******************************************************************************
 * Copyright (c) 2020 Sergey Vladimirov
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Sergey Vladimirov - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.eclipse.osgi.util.NLS;

/**
 * Mark any checker field with this annotation to be able to auto-register and auto-populate such field
 * using {@link AbstractCheckerWithProblemPreferences#addPreferencesForAnnotatedFields}
 * and {@link AbstractCheckerWithProblemPreferences#loadPreferencesForAnnotatedFields} methods.
 *
 * @author Sergey Vladimirov
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface ProblemPreference {

	/**
	 * Preference key used both for preference registration, label lookup and preference lookup
	 */
	String key();

	/**
	 * Class that holds problem preference labels. Labels must be static String fields
	 * with names in form <tt>ClassName_key</tt> where ClassName is the class name after last dot.
	 */
	Class<? extends NLS> nls();

}
