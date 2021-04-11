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

import org.eclipse.jface.text.templates.Template;

public class CdtTemplate implements Comparable<CdtTemplate> {

	private String id;
	private Template template;

	/**
	 * @param id - should be the id from TemplatePersistenceData
	 * @param template
	 */
	public CdtTemplate(String id, Template template) {
		this.id = id;
		this.template = template;
	}

	public String getID() {
		return id;
	}

	public Template getTemplate() {
		return template;
	}

	@Override
	public int compareTo(CdtTemplate cdtTmp) {

		if (id != null && cdtTmp.getID() != null && !id.equals(cdtTmp.getID())) {
			return id.compareTo(cdtTmp.getID());
		} else if (template != null & cdtTmp.getTemplate() != null) {
			return template.getName().compareTo(cdtTmp.getTemplate().getName());
		}
		return 0;
	}

}
