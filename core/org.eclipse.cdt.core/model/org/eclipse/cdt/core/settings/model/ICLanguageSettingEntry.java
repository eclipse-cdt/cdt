/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

/**
 * Representation in the project model of language settings entries
 * such as include paths (-I) or preprocessor defines (-D) and others (see
 * {@link ICSettingEntry#INCLUDE_PATH} and other kinds).
 */
public interface ICLanguageSettingEntry extends ICSettingEntry {
}
