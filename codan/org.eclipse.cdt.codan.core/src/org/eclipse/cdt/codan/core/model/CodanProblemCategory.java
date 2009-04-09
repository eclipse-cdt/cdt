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

import java.util.ArrayList;


public class CodanProblemCategory implements IProblemCategory {
	private String id;
	private String name;

	private ArrayList list = new ArrayList();
	public CodanProblemCategory(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return name;
	}
	public Object[] getChildren() {
		return list.toArray();
	}

	public void addChild(IProblem p) {
		list.add(p);
	}

	public IProblemCategory getParent() {
		// TODO Auto-generated method stub
		return null;
	}


}
