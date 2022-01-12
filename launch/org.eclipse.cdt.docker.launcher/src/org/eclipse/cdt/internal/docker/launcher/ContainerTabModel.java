/*******************************************************************************
 * Copyright (c) 2018 Red Hat.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.cdt.internal.docker.launcher;

import java.util.List;
import java.util.Set;

import org.eclipse.core.databinding.observable.list.WritableList;

/**
 * @since 1.2.1
 * @author jjohnstn
 *
 */
public class ContainerTabModel extends BaseDatabindingModel {

	public static final String PUBLISH_ALL_PORTS = "publishAllPorts"; //$NON-NLS-1$

	public static final String EXPOSED_PORTS = "exposedPorts"; //$NON-NLS-1$

	public static final String SELECTED_PORTS = "selectedPorts"; //$NON-NLS-1$

	private boolean publishAllPorts = true;

	private final WritableList<ExposedPortModel> exposedPorts = new WritableList<>();

	private Set<ExposedPortModel> selectedPorts;

	public boolean isPublishAllPorts() {
		return publishAllPorts;
	}

	public void setPublishAllPorts(boolean publishAllPorts) {
		firePropertyChange(PUBLISH_ALL_PORTS, this.publishAllPorts, this.publishAllPorts = publishAllPorts);
	}

	public WritableList<ExposedPortModel> getExposedPorts() {
		return exposedPorts;
	}

	public void addAvailablePort(final ExposedPortModel port) {
		this.exposedPorts.add(port);
	}

	public void removeAvailablePort(final ExposedPortModel port) {
		this.exposedPorts.remove(port);
	}

	public void setExposedPorts(final List<ExposedPortModel> exposedPorts) {
		this.exposedPorts.clear();
		this.exposedPorts.addAll(exposedPorts);
		// FIXME: also add all given exposedPorts to selectedExposedPorts ?
	}

	public void addExposedPort(final ExposedPortModel exposedPort) {
		if (!this.exposedPorts.contains(exposedPort)) {
			this.exposedPorts.add(exposedPort);
		}
	}

	public void removeExposedPort(final ExposedPortModel exposedPort) {
		this.exposedPorts.remove(exposedPort);
	}

	public void removeExposedPorts() {
		this.exposedPorts.clear();
	}

	public Set<ExposedPortModel> getSelectedPorts() {
		return this.selectedPorts;
	}

	public void setSelectedPorts(final Set<ExposedPortModel> ports) {
		firePropertyChange(SELECTED_PORTS, this.selectedPorts, this.selectedPorts = ports);
	}

}
