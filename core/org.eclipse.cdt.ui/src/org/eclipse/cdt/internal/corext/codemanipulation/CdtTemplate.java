/*******************************************************************************
 * Copyright (c) 2021 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Lidia Popescu (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.corext.codemanipulation;

import java.util.Objects;

import org.eclipse.jface.text.templates.Template;

public class CdtTemplate implements Comparable<CdtTemplate> {

	private String id;
	private Template template;
	private String key;
	private String name;

	/**
	 * @param id - should be the id from TemplatePersistenceData
	 * @param template
	 */
	public CdtTemplate(String id, Template template) {
		this.id = id;
		this.template = template;
		if (id == null) {
			this.key = ""; //$NON-NLS-1$
		} else {
			this.key = id;
		}
		if (template == null || template.getName() == null) {
			this.name = ""; //$NON-NLS-1$
		} else {
			this.name = template.getName();
		}
	}

	public String getID() {
		return id;
	}

	public Template getTemplate() {
		return template;
	}

	public String getKey() {
		return key;
	}

	public String getName() {
		return name;
	}

	@Override
	public int compareTo(CdtTemplate cdtTmp) {
		int value = Objects.compare(key, cdtTmp.key, String::compareTo);
		if (value == 0) {
			return Objects.compare(name, cdtTmp.name, String.CASE_INSENSITIVE_ORDER);
		}
		return value;
	}

}
