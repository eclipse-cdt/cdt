/*******************************************************************************
 * Copyright (c) 2023 ArSysOp.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov (ArSysOp) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui.compilationdatabase;

import java.net.URI;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.Adapters;

public final class ResolveProject implements Function<Object, Optional<IProject>> {

	private final ExistingResource resolve;

	public ResolveProject(IWorkspace workspace) {
		this.resolve = new ExistingResource(workspace);
	}

	@Override
	public Optional<IProject> apply(Object context) {
		return Optional.ofNullable(context)//
				.flatMap(this::resource)//
				.map(IResource::getProject);
	}

	private Optional<IResource> resource(Object object) {
		return uri(object).or(() -> adapt(object));
	}

	private Optional<IResource> uri(Object object) {
		return Optional.ofNullable(object)//
				.filter(URI.class::isInstance)//
				.map(URI.class::cast)//
				.flatMap(resolve);
	}

	private Optional<IResource> adapt(Object object) {
		return Optional.ofNullable(Adapters.adapt(object, IResource.class));
	}
}
