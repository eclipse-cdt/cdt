/*******************************************************************************
 * Copyright (c) 2017 IAR Systems AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jesper Eskilson (IAR Systems AB) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.cmake.core.server;

import java.util.List;

/**
 * Listener interface for clients wishing to be notified about cmake-server
 * signals/messages.
 *
 * See https://cmake.org/cmake/help/latest/manual/cmake-server.7.html for more
 * details on the semantics of the method calls.
 */
public interface ICMakeServerListener {

	void onFileChange(String path, List<String> properties);

	void onMessage(String title, String message);

	void onProgress(CMakeProgress progress);

	void onSignal(String name);
}
