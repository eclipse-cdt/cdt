/********************************************************************************
 * Copyright (c) 2022 MATHEMA GmbH
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.eclipse.cdt.docker.launcher.tests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.cdt.internal.docker.launcher.ContainerLaunchUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection4;
import org.junit.jupiter.api.Test;

/**
 * These test need a Docker-Server running to work
 *
 */
@SuppressWarnings("restriction")
class DockerServerInteractions {
	// A small image that exists
	static final String okImage = "alpine:latest";
	static final String imageBad = "alasdfasdfga32e:latest";
	IDockerConnection4 cnn;

	DockerServerInteractions() {
		cnn = (IDockerConnection4) DockerConnectionManager.getInstance().getFirstConnection();
		assertThat(cnn, notNullValue());
	}

	@Test
	void doublePull() {
		// Pull twice to ensure, that the image is already there before calling.
		IStatus di;
		di = ContainerLaunchUtils.provideDockerImage(null, cnn.getName(), okImage);
		assertTrue(di.isOK());
		di = ContainerLaunchUtils.provideDockerImage(null, cnn.getName(), okImage);
		assertTrue(di.isOK());
	}

	@Test
	void ensurePull() throws DockerException, InterruptedException {
		if (cnn.getImageInfo(okImage) != null) {
			cnn.removeImage(okImage);
		}
		final IStatus di = ContainerLaunchUtils.provideDockerImage(null, cnn.getName(), okImage);
		assertTrue(di.isOK());
	}

	@Test
	void failPull() throws DockerException, InterruptedException {
		final var di = ContainerLaunchUtils.provideDockerImage(null, cnn.getName(), imageBad);
		assertFalse(di.isOK());
		assertFalse(di.getMessage().isEmpty());
	}

}
