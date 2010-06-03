/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import java.util.List;

public interface ICMultiResourceDescription extends ICResourceDescription, ICMultiItemsHolder {

	void setSettingEntries(ICLanguageSetting lang, int kind, List<ICLanguageSettingEntry> incs, boolean toAll);
}
