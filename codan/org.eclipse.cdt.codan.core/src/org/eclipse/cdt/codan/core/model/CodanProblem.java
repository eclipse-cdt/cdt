/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

public class CodanProblem implements IProblem {
	private String id;
	private String name;
	private CodanSeverity severity = CodanSeverity.Warning;
	private boolean enabled = true;

	public CodanSeverity getSeverity() {
		return severity;
	}

	public CodanProblem(String id2, String name2) {
		this.id = id2;
		this.name = name2;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public IProblemCategory getCategory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		return name;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setSeverity(CodanSeverity sev) {
		if (sev == null)
			throw new NullPointerException();
		this.severity = sev;
	}

	public void setEnabled(boolean checked) {
		this.enabled = checked;
	}
}
