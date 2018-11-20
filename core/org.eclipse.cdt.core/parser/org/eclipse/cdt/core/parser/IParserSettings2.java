/*
 * Copyright (c) 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.core.parser;

/**
 * Interface for providing settings for the parser.
 * <p>
 * The first version of the interface was not marked with no-implement, so methods
 * cannot be added to it.  This version should be used going forward.  It is marked
 * no-implement and a Default implementation is provided.  Clients should base their
 * own implementations on Default in order to avoid being broken by futured additions
 * to this interface.
 *
 * @since 5.7
 * @noimplement Extend {@link IParserSettings2.Default} instead.
 */
public interface IParserSettings2 extends IParserSettings {
	/**
	 * An default implementation to be used as a base class by clients that want to
	 * contribute parser settings.  This base provides default values for all methods
	 * so that clients will still compile when methods are added to the interface.
	 *
	 * @noinstantiate This class is not intended to be instantiated by clients.
	 */
	public static class Default extends ParserSettings implements IParserSettings2 {
		@Override
		public boolean shouldLimitTokensPerTranslationUnit() {
			return false;
		}

		@Override
		public int getMaximumTokensPerTranslationUnit() {
			return 0;
		}
	}

	/**
	 * Returns true if the parser should be aborted when a single translation unit has produced
	 * more than {@link #getMaximumTokensPerTranslationUnit()} tokens.
	 */
	public boolean shouldLimitTokensPerTranslationUnit();

	/**
	 * Returns the maximum number of tokens that should be created while parsing any one translation unit.
	 * This value is used only when {@link #shouldLimitTokensPerTranslationUnit()} returns true.
	 */
	public int getMaximumTokensPerTranslationUnit();
}
