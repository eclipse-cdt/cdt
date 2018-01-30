/*******************************************************************************
 * Copyright (c) 2016, 2018 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Red Hat Inc. - modified for use in Meson build
 *******************************************************************************/
package org.eclipse.cdt.meson.core;

public interface IMesonToolChainProvider {

	void init(IMesonToolChainManager manager);

}
