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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.options.BaseOption;
import org.eclipse.cdt.core.options.OptionMetadata;
import org.eclipse.cdt.doxygen.DoxygenMetadata;

final class DoxygenMetadataDefaults implements DoxygenMetadata {

	private final OptionMetadata<Boolean> useBriefTagOption;
	private final OptionMetadata<Boolean> useStructuralCommandsOption;
	private final OptionMetadata<Boolean> useJavadocStyleOption;
	private final OptionMetadata<Boolean> newLineAfterBriefOption;
	private final OptionMetadata<Boolean> usePrePostTagOption;
	private final List<OptionMetadata<Boolean>> booleanOptions;

	public DoxygenMetadataDefaults() {
		this.useBriefTagOption = new BaseOption<>(Boolean.class, "doxygen_use_brief_tag", false, //$NON-NLS-1$
				DoxygenCoreMessages.DoxygenMetadataDefaults_use_brief_tag_name,
				DoxygenCoreMessages.DoxygenMetadataDefaults_use_brief_tag_description);
		this.useStructuralCommandsOption = new BaseOption<>(Boolean.class, "doxygen_use_structural_commands", false, //$NON-NLS-1$
				DoxygenCoreMessages.DoxygenMetadataDefaults_use_structured_commands_name,
				DoxygenCoreMessages.DoxygenMetadataDefaults_use_structured_commands_description);
		this.useJavadocStyleOption = new BaseOption<>(Boolean.class, "doxygen_use_javadoc_tags", true, //$NON-NLS-1$
				DoxygenCoreMessages.DoxygenMetadataDefaults_use_javadoc_style_name,
				DoxygenCoreMessages.DoxygenMetadataDefaults_use_javadoc_style_description);
		this.newLineAfterBriefOption = new BaseOption<>(Boolean.class, "doxygen_new_line_after_brief", true, //$NON-NLS-1$
				DoxygenCoreMessages.DoxygenMetadataDefaults_new_line_after_brief_name,
				DoxygenCoreMessages.DoxygenMetadataDefaults_new_line_after_brief_description);
		this.usePrePostTagOption = new BaseOption<>(Boolean.class, "doxygen_use_pre_tag", false, //$NON-NLS-1$
				DoxygenCoreMessages.DoxygenMetadataDefaults_use_pre_post_tags_name,
				DoxygenCoreMessages.DoxygenMetadataDefaults_use_pre_post_tags_description);
		this.booleanOptions = new ArrayList<>();
		booleanOptions.add(useBriefTagOption);
		booleanOptions.add(useStructuralCommandsOption);
		booleanOptions.add(useJavadocStyleOption);
		booleanOptions.add(newLineAfterBriefOption);
		booleanOptions.add(usePrePostTagOption);
	}

	@Override
	public OptionMetadata<Boolean> useBriefTagOption() {
		return useBriefTagOption;
	}

	@Override
	public OptionMetadata<Boolean> useStructuralCommandsOption() {
		return useStructuralCommandsOption;
	}

	@Override
	public OptionMetadata<Boolean> useJavadocStyleOption() {
		return useJavadocStyleOption;
	}

	@Override
	public OptionMetadata<Boolean> newLineAfterBriefOption() {
		return newLineAfterBriefOption;
	}

	@Override
	public OptionMetadata<Boolean> usePrePostTagOption() {
		return usePrePostTagOption;
	}

	@Override
	public List<OptionMetadata<Boolean>> booleanOptions() {
		return new ArrayList<>(booleanOptions);
	}

}
