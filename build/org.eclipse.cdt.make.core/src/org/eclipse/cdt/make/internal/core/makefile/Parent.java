/*******************************************************************************
 * Copyright (c) 2000, 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.makefile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.IParent;

/**
 * IParent
 */

public abstract class Parent extends Directive implements IParent {

	ArrayList<Directive> children = new ArrayList<Directive>();

	public Parent(Directive parent) {
		super(parent);
	}

	public IDirective[] getDirectives(boolean expand) {
		if (expand) {
			List<IDirective> directives = new ArrayList<IDirective>();
			getDirectives(); // populates children for class Include
			for (IDirective directive : children) {
				directives.add(directive);
				if (directive instanceof Parent) {
					directives.addAll(Arrays.asList(((Parent) directive).getDirectives(expand)));
				}
			}
			return directives.toArray(new IDirective[directives.size()]);
		}
		return getDirectives();
	}

	@Override
	public IDirective[] getDirectives() {
		children.trimToSize();
		return children.toArray(new IDirective[0]);
	}

	public void addDirective(Directive directive) {
		children.add(directive);
		// reparent
		directive.setParent(this);
	}

	public void addDirectives(Directive[] directives) {
		children.addAll(Arrays.asList(directives));
		// reparent
		for (int i = 0; i < directives.length; i++) {
			directives[i].setParent(this);
		}
	}

	public void clearDirectives() {
		children.clear();
	}

	public Directive[] getStatements() {
		children.trimToSize();
		return children.toArray(new Directive[0]);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		IDirective[] directives = getDirectives();
		for (int i = 0; i < directives.length; i++) {
			sb.append(directives[i]);
		}
		return sb.toString();
	}

}
