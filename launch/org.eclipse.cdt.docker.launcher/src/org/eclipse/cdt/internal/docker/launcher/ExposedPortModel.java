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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.runtime.Assert;

public class ExposedPortModel extends BaseDatabindingModel implements Comparable<ExposedPortModel> {

	private static final String SEPARATOR = ":"; //$NON-NLS-1$

	private static final String CONTAINER_TYPE_SEPARATOR = "/"; //$NON-NLS-1$

	public static final String SELECTED = "selected"; //$NON-NLS-1$

	public static final String CONTAINER_PORT = "containerPort"; //$NON-NLS-1$

	public static final String PORT_TYPE = "portType"; //$NON-NLS-1$

	public static final String HOST_ADDRESS = "hostAddress"; //$NON-NLS-1$

	public static final String HOST_PORT = "hostPort"; //$NON-NLS-1$

	private final String id = UUID.randomUUID().toString();

	private boolean selected;

	private String containerPort;

	private String portType;

	private String hostAddress;

	private String hostPort;

	/**
	 * Parses and converts the {@link List} of the given {@link String} values
	 * into a {@link List} of {@link ExposedPortModel}
	 *
	 * @param exposedPortInfos
	 *            the input values
	 * @return the corresponding {@link ExposedPortModel}s
	 */
	public static List<ExposedPortModel> fromStrings(final Collection<String> exposedPortInfos) {
		final List<ExposedPortModel> exposedPorts = new ArrayList<>();
		for (String exposedPortInfo : exposedPortInfos) {
			final ExposedPortModel exposedPort = ExposedPortModel.fromString(exposedPortInfo);
			if (exposedPort != null) {
				exposedPorts.add(exposedPort);
			}
		}
		return exposedPorts;
	}

	/**
	 * Converts a collection of ExposedPortModel to a {@link List} of
	 * {@link String} values
	 *
	 *
	 * @param exposedPorts
	 *            collection of ExposedPortModel instances
	 * @return the corresponding {@link List} of {@link String}s
	 */
	public static List<String> toArrayString(final Collection<ExposedPortModel> exposedPorts) {
		final List<String> exposedPortList = new ArrayList<>();
		for (ExposedPortModel exposedPort : exposedPorts) {
			final String exposedPortString = exposedPort.toString();
			if (exposedPort != null) {
				exposedPortList.add(exposedPortString);
			}
		}
		return exposedPortList;
	}

	/**
	 * Parse the given value and returns an instance of
	 * {@link ExposedPortModel}.
	 *
	 * @param exposedPortInfo
	 *            the value to parse
	 * @return the corresponding {@link ExposedPortModel}
	 */
	public static ExposedPortModel fromString(final String exposedPortInfo) {
		final String privatePort = exposedPortInfo.substring(0, exposedPortInfo.indexOf(CONTAINER_TYPE_SEPARATOR));
		// exposed ports without host IP/port info
		final int firstColumnSeparator = exposedPortInfo.indexOf(SEPARATOR);
		if (firstColumnSeparator == -1 && exposedPortInfo.indexOf(CONTAINER_TYPE_SEPARATOR) != -1) {
			final String type = exposedPortInfo.substring(exposedPortInfo.indexOf(CONTAINER_TYPE_SEPARATOR)); // $NON-NLS-1$
			final ExposedPortModel exposedPort = new ExposedPortModel(privatePort, type, "", privatePort); // $NON-NLS-1$
			return exposedPort; // $NON-NLS-1$
		} else {
			final int secondColumnSeparator = exposedPortInfo.indexOf(SEPARATOR, firstColumnSeparator + 1);
			final String type = exposedPortInfo.substring(exposedPortInfo.indexOf(CONTAINER_TYPE_SEPARATOR), // $NON-NLS-1$
					firstColumnSeparator); // $NON-NLS-1$
			final String hostIP = exposedPortInfo.substring(firstColumnSeparator + 1, secondColumnSeparator);
			final String hostPort = exposedPortInfo.substring(secondColumnSeparator + 1);
			final ExposedPortModel exposedPort = new ExposedPortModel(privatePort, type, hostIP, hostPort); // $NON-NLS-1$
			return exposedPort; // $NON-NLS-1$
		}
	}

	/**
	 * Full constructor
	 *
	 * @param privatePort
	 * @param portType
	 * @param hostAddress
	 * @param hostPort
	 */
	public ExposedPortModel(final String privatePort, final String type, final String hostAddress,
			final String hostPort) {
		Assert.isNotNull(privatePort, "Port Mapping privatePort cannot be null"); //$NON-NLS-1$
		Assert.isNotNull(type, "Port Mapping portType cannot be null"); //$NON-NLS-1$
		this.containerPort = privatePort;
		this.hostPort = hostPort;
		this.portType = type;
		this.hostAddress = hostAddress;
	}

	/**
	 * Create an ExposedPortModel from its toString output
	 *
	 * @param stringValue
	 * @return ExposedPortModel
	 */
	static public ExposedPortModel createPortModel(String stringValue) {
		final String[] elements = stringValue.split(SEPARATOR);
		final String[] containerPortElements = elements[0].split(CONTAINER_TYPE_SEPARATOR);
		ExposedPortModel model = new ExposedPortModel(containerPortElements[0], containerPortElements[1], elements[1],
				elements[2]);
		// check the last argument if exists otherwise assume 'true'
		model.selected = (elements.length == 4) ? Boolean.valueOf(elements[3]) : true;
		return model;
	}

	public String getContainerPort() {
		return containerPort;
	}

	public void setContainerPort(final String containerPort) {
		firePropertyChange(CONTAINER_PORT, this.containerPort, this.containerPort = containerPort);
	}

	public String getPortType() {
		return portType;
	}

	public void setPortType(final String type) {
		firePropertyChange(PORT_TYPE, this.portType, this.portType = type);
	}

	public boolean getSelected() {
		return selected;
	}

	public void setSelected(final boolean selected) {
		firePropertyChange(SELECTED, this.selected, this.selected = selected);
	}

	public String getHostPort() {
		return hostPort;
	}

	public void setHostPort(final String hostPort) {
		firePropertyChange(HOST_PORT, this.hostPort, this.hostPort = hostPort);
	}

	public String getHostAddress() {
		return hostAddress;
	}

	public void setHostAddress(final String hostAddress) {
		firePropertyChange(HOST_ADDRESS, this.hostAddress, this.hostAddress = hostAddress);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExposedPortModel other = (ExposedPortModel) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public int compareTo(final ExposedPortModel other) {
		return this.containerPort.compareTo(other.containerPort);
	}

	// FIXME we should have a dedicated method to serialize the bean
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(containerPort + CONTAINER_TYPE_SEPARATOR + portType + SEPARATOR
				+ (hostAddress != null ? hostAddress : "") + SEPARATOR + hostPort + SEPARATOR + selected);
		return buffer.toString();
	}

}
