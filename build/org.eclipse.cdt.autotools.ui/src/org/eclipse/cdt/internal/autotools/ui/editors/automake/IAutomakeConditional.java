/*******************************************************************************
 * Copyright (c) 2007, 2015 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.editors.automake;

public interface IAutomakeConditional {
	
	boolean isAutomake();

	void setAutomake(boolean value);

	Rule[] getRules();

	void setRules(Rule[] rules);
}
