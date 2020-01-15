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
package org.eclipse.cdt.internal.core.options;

public final class BooleanOption extends BaseOption<Boolean> {

	public BooleanOption(String identifier, boolean defaultValue, String name) {
		super(Boolean.class, identifier, defaultValue, name);
	}

	public BooleanOption(String identifier, boolean defaultValue, String name, String description) {
		super(Boolean.class, identifier, defaultValue, name, description);
	}

}