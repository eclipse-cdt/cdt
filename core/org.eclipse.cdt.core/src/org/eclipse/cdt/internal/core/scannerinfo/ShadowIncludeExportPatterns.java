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

import org.eclipse.cdt.core.parser.IncludeExportPatterns;

/**
 * Shadow version of the classes we are trying to serialize that contain only the info we need.
 *
 * @see IncludeExportPatterns
 * @see IExtendedScannerInfoDeserializer
 * @see ExtendedScannerInfoSerializer
 */
public class ShadowIncludeExportPatterns {
	ShadowPattern includeExportPattern;
	ShadowPattern includeBeginExportPattern;
	ShadowPattern includeEndExportPattern;
}
