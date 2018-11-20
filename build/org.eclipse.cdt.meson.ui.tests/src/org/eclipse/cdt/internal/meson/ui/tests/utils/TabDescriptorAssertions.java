/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.cdt.internal.meson.ui.tests.utils;

import static org.hamcrest.Matchers.notNullValue;

import org.assertj.core.api.AbstractAssert;
import org.eclipse.ui.views.properties.tabbed.ITabDescriptor;

/**
 * Custom assertions on a given {@link ITabDescriptor}.
 */
public class TabDescriptorAssertions extends AbstractAssert<TabDescriptorAssertions, ITabDescriptor> {

	protected TabDescriptorAssertions(final ITabDescriptor actual) {
		super(actual, TabDescriptorAssertions.class);
	}

	public static TabDescriptorAssertions assertThat(final ITabDescriptor actual) {
		return new TabDescriptorAssertions(actual);
	}

	public TabDescriptorAssertions hasId(final String id) {
		notNullValue();
		if (!actual.getId().equals(id)) {
			failWithMessage("Expected tab section with id '%s' to be selected but it was '%s'", id, actual.getId());
		}
		return this;
	}

}
