/*******************************************************************************
 * Copyright (c) 2018 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Lidia Popescu - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.callhierarchy.extension;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.ICHENode;
import org.eclipse.core.runtime.IAdaptable;

/**
 * The dsl node sample
 * */
public class DslNode implements IAdaptable, ICHENode {

	private ICElement fRepresentedDecl;
	private ICProject mProject;
	private String mDslNodeName;

	/**
	 * Constructor used for Open Call Hierarchy command
	 * */
	public DslNode(ICElement decl) {
		this.fRepresentedDecl = decl;
	}

	/**
	 * Constructor used for Open Dsl declaration command
	 * */
	public DslNode() {

	}

	public ICProject getProject() {
		return mProject;
	}

	public void setProject(ICProject mProject) {
		this.mProject = mProject;
	}

	@Override
	public ICElement getRepresentedDeclaration() {
		return fRepresentedDecl;
	}

	@Override
	public <T> T getAdapter(Class<T> adapterClass) {
		if (adapterClass == ICElement.class) {
			return (T) getRepresentedDeclaration();
		}
		return null;
	}

	/**
	 * Should be displayed with an indication that this is the dsl,
	 * e.g. "Java function <function_name>".
	 * */
	public String getDslNodeName() {
		if (mDslNodeName == null) {
			mDslNodeName = "JAVA function " + fRepresentedDecl.getElementName() + "()";
		}
		return mDslNodeName;
	}
}
