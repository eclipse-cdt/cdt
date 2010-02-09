/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Red Hat Inc. - Allow multiple targets
 *******************************************************************************/
package org.eclipse.cdt.make.core;

import java.util.EventObject;

import org.eclipse.core.resources.IProject;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class MakeTargetEvent extends EventObject {
	private static final long serialVersionUID = 1L;

	public static final int TARGET_ADD = 1;
	public static final int TARGET_CHANGED = 2;
	public static final int TARGET_REMOVED = 3;
	public static final int PROJECT_ADDED = 4;
	public static final int PROJECT_REMOVED = 5;

	IMakeTarget[] targets;
	IProject project;
	int type;

	/**
	 * @param source
	 */
	public MakeTargetEvent(Object source, int type, IMakeTarget target) {
		super(source);
		this.type = type;
		this.targets = new IMakeTarget[] {target};
	}

	/**
	 * @param source
	 * @param type event type (e.g. TARGET_ADD, TARGET_CHANGED)
	 * @param targets array of MakeTargets
	 * @since 7.0
	 * 
	 */
	public MakeTargetEvent(Object source, int type, IMakeTarget[] targets) {
		super(source);
		this.type = type;
		this.targets = new IMakeTarget[targets.length];
		System.arraycopy(targets, 0, this.targets, 0, targets.length);
	}

	public MakeTargetEvent(Object source, int type, IProject project) {
		super(source);
		this.type = type;
		this.project = project;
	}

	public int getType() {
		return type;
	}

	/**
	 * @deprecated
	 * Use getTargets() instead.
	 */
	@Deprecated
	public IMakeTarget getTarget() {
		return targets[0];
	}
	
	/**
	 * @since 7.0
	 */
	public IMakeTarget[] getTargets() {
		return targets;
	}
}
