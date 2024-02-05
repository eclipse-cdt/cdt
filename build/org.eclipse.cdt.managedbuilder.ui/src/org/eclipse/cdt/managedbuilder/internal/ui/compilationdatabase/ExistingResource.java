/*******************************************************************************
 * Copyright (c) 2023 ArSysOp and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Alexander Fedorov (ArSysOp) - initial API
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui.compilationdatabase;

import java.net.URI;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;

public final class ExistingResource implements Function<URI, Optional<IResource>> {

	private final IWorkspace workspace;

	public ExistingResource(IWorkspace workspace) {
		this.workspace = Objects.requireNonNull(workspace);
	}

	@Override
	public Optional<IResource> apply(URI uri) {
		return forContainer(uri).or(() -> forFile(uri));
	}

	private Optional<IResource> forContainer(URI uri) {
		return Arrays.stream(workspace.getRoot().findContainersForLocationURI(uri))//
				.map(IResource.class::cast)//
				.filter(c -> c.exists())//
				.findFirst();
	}

	private Optional<IResource> forFile(URI uri) {
		return Arrays.stream(workspace.getRoot().findFilesForLocationURI(uri))//
				.map(IResource.class::cast)//
				.filter(c -> c.exists())//
				.findFirst();
	}
}
