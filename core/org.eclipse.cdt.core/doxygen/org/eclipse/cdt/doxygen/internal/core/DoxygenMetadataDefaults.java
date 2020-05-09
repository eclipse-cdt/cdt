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

import org.eclipse.cdt.doxygen.DoxygenMetadata;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;

final class DoxygenMetadataDefaults implements DoxygenMetadata {

	private final PreferenceMetadata<Boolean> useBriefTagOption;
	private final PreferenceMetadata<Boolean> useStructuralCommandsOption;
	private final PreferenceMetadata<Boolean> useJavadocStyleOption;
	private final PreferenceMetadata<Boolean> newLineAfterBriefOption;
	private final PreferenceMetadata<Boolean> usePrePostTagOption;
	private final List<PreferenceMetadata<Boolean>> booleanOptions;

	public DoxygenMetadataDefaults() {
		this.useBriefTagOption = new PreferenceMetadata<>(Boolean.class, "doxygen_use_brief_tag", false, //$NON-NLS-1$
				DoxygenCoreMessages.DoxygenMetadataDefaults_use_brief_tag_name,
				DoxygenCoreMessages.DoxygenMetadataDefaults_use_brief_tag_description);
		this.useStructuralCommandsOption = new PreferenceMetadata<>(Boolean.class, "doxygen_use_structural_commands", //$NON-NLS-1$
				false, DoxygenCoreMessages.DoxygenMetadataDefaults_use_structured_commands_name,
				DoxygenCoreMessages.DoxygenMetadataDefaults_use_structured_commands_description);
		this.useJavadocStyleOption = new PreferenceMetadata<>(Boolean.class, "doxygen_use_javadoc_tags", true, //$NON-NLS-1$
				DoxygenCoreMessages.DoxygenMetadataDefaults_use_javadoc_style_name,
				DoxygenCoreMessages.DoxygenMetadataDefaults_use_javadoc_style_description);
		this.newLineAfterBriefOption = new PreferenceMetadata<>(Boolean.class, "doxygen_new_line_after_brief", true, //$NON-NLS-1$
				DoxygenCoreMessages.DoxygenMetadataDefaults_new_line_after_brief_name,
				DoxygenCoreMessages.DoxygenMetadataDefaults_new_line_after_brief_description);
		this.usePrePostTagOption = new PreferenceMetadata<>(Boolean.class, "doxygen_use_pre_tag", false, //$NON-NLS-1$
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
	public PreferenceMetadata<Boolean> useBriefTagOption() {
		return useBriefTagOption;
	}

	@Override
	public PreferenceMetadata<Boolean> useStructuralCommandsOption() {
		return useStructuralCommandsOption;
	}

	@Override
	public PreferenceMetadata<Boolean> useJavadocStyleOption() {
		return useJavadocStyleOption;
	}

	@Override
	public PreferenceMetadata<Boolean> newLineAfterBriefOption() {
		return newLineAfterBriefOption;
	}

	@Override
	public PreferenceMetadata<Boolean> usePrePostTagOption() {
		return usePrePostTagOption;
	}

	@Override
	public List<PreferenceMetadata<Boolean>> booleanOptions() {
		return new ArrayList<>(booleanOptions);
	}

}
