/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.model;

/**
 * Interface supported by model builders for contributed languages.
 *
 * Model builders parse a <code>TranslationUnit</code> (i.e., a file) and
 * return a hierarchy of <code>ICElement</code>s which represent the high-level
 * structure of that file (what modules, classes, functions, and similar
 * constructs are contained in it, and on what line(s) the definition occurs).
 *
 * The translation unit to parse and the initial element map are given to
 * {@link ILanguage#createModelBuilder}, which will presumably
 * pass that information on to the model builder constructor.
 *
 * @author Jeff Overbey
 */
public interface IContributedModelBuilder {

	/**
	 * A factory to create a model builder for a translation unit.
	 *
	 * @since 5.0
	 */
	public interface Factory {
		/**
		 * Create a model builder for the given translation unit.
		 *
		 * @param tu  the translation unit
		 * @return the model builder or <code>null</code> if no model builder could be created
		 */
		IContributedModelBuilder create(ITranslationUnit tu);
	}

	/**
	 * Callback used when a <code>TranslationUnit</code> needs to be parsed.
	 *
	 * The translation unit to parse is given to
	 * {@link ILanguage#createModelBuilder}, which will presumably
	 * pass it on to the model builder constructor.
	 */
	public abstract void parse(boolean quickParseMode) throws Exception;
}
