/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.ui.actions;

import org.eclipse.cdt.core.index.IIndexManager;

public class FreshenIndexAction extends AbstractUpdateIndexAction {
	@Override
	protected int getUpdateOptions() {
		return IIndexManager.UPDATE_ALL | IIndexManager.UPDATE_EXTERNAL_FILES_FOR_PROJECT;
	}
}
