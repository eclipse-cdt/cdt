/*******************************************************************************
 * Copyright (c) 2021 Kichwa Coders Canada Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.core.scannerinfo;

import java.util.regex.Pattern;

/**
 * Shadow version of the classes we are trying to serialize that contain only the info we need.
 *
 * To match original implementation of the serialize/deserialize which used {@link Pattern} directly
 * we replicate the structure. This is why we don't just use String instead of ShadowPattern
 *
 * @see Pattern
 * @see IExtendedScannerInfoDeserializer
 * @see ExtendedScannerInfoSerializer
 */
public class ShadowPattern {
	public ShadowPattern(String pattern) {
		this.pattern = pattern;
	}

	String pattern;
}
