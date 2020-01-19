/*******************************************************************************
 * Copyright (c) 2020 ArSysOp and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.doxygen.internal.core;

import org.eclipse.cdt.core.options.OptionStorage;
import org.eclipse.cdt.doxygen.DoxygenMetadata;
import org.eclipse.cdt.doxygen.DoxygenOptions;

final class DoxygenOptionsAccess implements DoxygenOptions {

	private final OptionStorage optionStorage;
	private final DoxygenMetadata doxygenMetadata;

	public DoxygenOptionsAccess(OptionStorage optionStorage, DoxygenMetadata doxygenMetadata) {
		this.optionStorage = optionStorage;
		this.doxygenMetadata = doxygenMetadata;
	}

	@Override
	public boolean useBriefTags() {
		return optionStorage.load(doxygenMetadata.useBriefTagOption());
	}

	@Override
	public boolean useStructuralCommands() {
		return optionStorage.load(doxygenMetadata.useStructuralCommandsOption());
	}

	@Override
	public boolean useJavadocStyle() {
		return optionStorage.load(doxygenMetadata.useJavadocStyleOption());
	}

	@Override
	public boolean newLineAfterBrief() {
		return optionStorage.load(doxygenMetadata.newLineAfterBriefOption());
	}

	@Override
	public boolean usePrePostTag() {
		return optionStorage.load(doxygenMetadata.usePrePostTagOption());
	}

}
