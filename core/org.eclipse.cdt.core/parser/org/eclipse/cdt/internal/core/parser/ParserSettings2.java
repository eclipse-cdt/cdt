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
package org.eclipse.cdt.internal.core.parser;

import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.parser.IParserSettings2;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.core.resources.IProject;

public class ParserSettings2 extends IParserSettings2.Default {
	private final boolean limitTokensPerTU;
	private final int maxTokensPerTU;

	public ParserSettings2() {
		this((ICProject) null);
	}

	public ParserSettings2(IProject project) {
		this(CModelManager.getDefault().create(project));
	}

	/**
	 * Use the specified project when looking for preferences for the settings object that is
	 * being constructed.
	 *
	 * @param cProject The project from which the settings should be loaded, can be null.
	 */
	public ParserSettings2(ICProject cProject) {
		this.limitTokensPerTU = CCorePreferenceConstants.getPreference(
				CCorePreferenceConstants.SCALABILITY_LIMIT_TOKENS_PER_TU, cProject,
				CCorePreferenceConstants.DEFAULT_SCALABILITY_LIMIT_TOKENS_PER_TU);
		this.maxTokensPerTU = CCorePreferenceConstants.getPreference(
				CCorePreferenceConstants.SCALABILITY_MAXIMUM_TOKENS, cProject,
				CCorePreferenceConstants.DEFAULT_SCALABILITY_MAXIMUM_TOKENS);
	}

	/**
	 * Returns true if the parser should be aborted when a single translation unit has produced
	 * more than {@link #getMaximumTokensPerTranslationUnit()} tokens.
	 */
	@Override
	public boolean shouldLimitTokensPerTranslationUnit() {
		return limitTokensPerTU;
	}

	/**
	 * Returns the maximum number of tokens that should be created while parsing any one translation unit.
	 * This value will only be used when {@link #shouldLimitTokensPerTranslationUnit()} returns true.
	 */
	@Override
	public int getMaximumTokensPerTranslationUnit() {
		return maxTokensPerTU;
	}
}
