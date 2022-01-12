/*******************************************************************************
 * Copyright (c) 2000, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation in CRegisterManager.java and CRegisterGroup.java
 *     Alvaro Sanchez-Leon (Ericsson) - Integrated from files above for Bug 235747
 *     Bruno Medeiros (Renesas) - Persistence of register groups per process (449104)
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.model.IRegisterDescriptor;
import org.eclipse.cdt.debug.internal.core.model.CoreModelMessages;
import org.eclipse.cdt.debug.internal.core.model.IRegisterGroupDescriptor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class RegisterGroupsPersistance {
	private static final String DEFAULT_ATTR_DEBUGGER_REGISTER_GROUPS_VALUE = ""; //$NON-NLS-1$
	private static final String DEFAULT_LAUNCH_CONFIGURATION_TARGET_ATTRIBUTE = ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_REGISTER_GROUPS;
	private static final String ELEMENT_REGISTER_GROUP_LIST = "registerGroups"; //$NON-NLS-1$
	private static final String ATTR_REGISTER_GROUP_MEMENTO = "memento"; //$NON-NLS-1$

	private static final String ELEMENT_GROUP = "group"; //$NON-NLS-1$
	private static final String ELEMENT_REGISTER_GROUP = "registerGroup"; //$NON-NLS-1$
	private static final String ATTR_REGISTER_GROUP_NAME = "name"; //$NON-NLS-1$
	private static final String ATTR_REGISTER_CONTAINER_ID = "parent_id"; //$NON-NLS-1$
	private static final String ATTR_REGISTER_GROUP_ENABLED = "enabled"; //$NON-NLS-1$

	private static final String ELEMENT_REGISTER = "register"; //$NON-NLS-1$

	private static final String ATTR_REGISTER_NAME = "name"; //$NON-NLS-1$
	private static final String ATTR_REGISTER_ORIGINAL_GROUP_NAME = "originalGroupName"; //$NON-NLS-1$

	private final ILaunchConfiguration fLaunchConfig;

	private String fLaunchConfigTargetAttribute = DEFAULT_LAUNCH_CONFIGURATION_TARGET_ATTRIBUTE;

	// Constructor
	public RegisterGroupsPersistance(ILaunchConfiguration configuration) {
		fLaunchConfig = configuration;
	}

	private class RegisterGroupDescriptor implements IRegisterGroupDescriptor {

		private final String fMemento;
		private final String fName;
		private final boolean fEnabled;
		private final String fContainerId;
		IRegisterDescriptor[] fRegisterDescriptors = null;

		public RegisterGroupDescriptor(String memento, String groupName, boolean enabled, String containerId) {
			fMemento = memento;
			fName = groupName;
			fEnabled = enabled;
			if (containerId != null && containerId.length() == 0) {
				containerId = null;
			}
			fContainerId = containerId;
		}

		@Override
		public String getName() {
			return fName;
		}

		@Override
		public boolean isEnabled() {
			return fEnabled;
		}

		@Override
		public String getContainerId() {
			return fContainerId;
		}

		@Override
		public IRegisterDescriptor[] getChildren() throws CoreException {
			if (fRegisterDescriptors == null) {

				Node node = DebugPlugin.parseDocument(fMemento);
				Element element = (Element) node;

				List<IRegisterDescriptor> list = new ArrayList<>();
				Node childNode = element.getFirstChild();
				while (childNode != null) {
					if (childNode.getNodeType() == Node.ELEMENT_NODE) {
						Element child = (Element) childNode;
						if (ELEMENT_REGISTER.equals(child.getNodeName())) {
							String name = child.getAttribute(ATTR_REGISTER_NAME);
							String originalGroupName = child.getAttribute(ATTR_REGISTER_ORIGINAL_GROUP_NAME);
							if (name == null || name.length() == 0 || originalGroupName == null
									|| originalGroupName.length() == 0) {
								abort(CoreModelMessages.getString("CRegisterGroup.3"), null); //$NON-NLS-1$
							} else {
								IRegisterDescriptor d = findDescriptor(originalGroupName, name);
								if (d != null)
									list.add(d);
								else
									CDebugCorePlugin.log(CoreModelMessages.getString("CRegisterGroup.4")); //$NON-NLS-1$
							}
						}
					}
					childNode = childNode.getNextSibling();
				}

				fRegisterDescriptors = list.toArray(new IRegisterDescriptor[list.size()]);
			}

			return fRegisterDescriptors;
		}

	}

	class RegisterDescriptor implements IRegisterDescriptor {
		private final String fGroupName;
		private final String fName;

		private RegisterDescriptor(String oGroupName, String rname) {
			fGroupName = oGroupName;
			fName = rname;
		}

		@Override
		public String getName() {
			return fName;
		}

		@Override
		public String getGroupName() {
			return fGroupName;
		}
	}

	public void setLaunchConfigurationTargetAttribute(String launchConfigurationTargetAttribute) {
		fLaunchConfigTargetAttribute = launchConfigurationTargetAttribute;
	}

	public String getLaunchConfigurationTargetAttribute() {

		return fLaunchConfigTargetAttribute;
	}

	/**
	 * Parse register groups.
	 * If given containerId is not null, the only returned register groups are the one
	 * whose container id matches given containerId
	 */
	public IRegisterGroupDescriptor[] parseGroups(String containerId) throws CoreException {
		List<IRegisterGroupDescriptor> groups = new ArrayList<>();
		String memento;

		memento = fLaunchConfig.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_REGISTER_GROUPS,
				DEFAULT_ATTR_DEBUGGER_REGISTER_GROUPS_VALUE);

		if (memento != null && memento.length() > 0) {
			Node node = DebugPlugin.parseDocument(memento);

			if (node.getNodeType() != Node.ELEMENT_NODE) {
				abort("Unable to restore register groups - invalid memento.", null); //$NON-NLS-1$
			}
			Element element = (Element) node;
			if (!ELEMENT_REGISTER_GROUP_LIST.equals(element.getNodeName())) {
				abort("Unable to restore register groups - expecting register group list element.", null); //$NON-NLS-1$
			}

			Node childNode = element.getFirstChild();
			while (childNode != null) {
				if (childNode.getNodeType() == Node.ELEMENT_NODE) {
					Element child = (Element) childNode;
					if (ELEMENT_GROUP.equals(child.getNodeName())) {
						String groupMemento = child.getAttribute(ATTR_REGISTER_GROUP_MEMENTO);

						IRegisterGroupDescriptor groupdesc = createGroupFromMemento(groupMemento);
						if (groupdesc != null) {
							if (containerId == null || containerId.equals(groupdesc.getContainerId())) {
								groups.add(groupdesc);
							}
						}
					}
				}
				childNode = childNode.getNextSibling();
			}

		}

		return groups.toArray(new IRegisterGroupDescriptor[groups.size()]);

	}

	public void saveGroups(IRegisterGroupDescriptor[] groups) throws CoreException {
		try {
			ILaunchConfigurationWorkingCopy wc = fLaunchConfig.getWorkingCopy();

			// if no groups present, save to a blank string, i.e. expected by CDI and
			// handled by DSF
			String newValue = (groups.length > 0) ? getMemento(groups) : DEFAULT_ATTR_DEBUGGER_REGISTER_GROUPS_VALUE;
			String oldValue = DEFAULT_ATTR_DEBUGGER_REGISTER_GROUPS_VALUE;
			try {
				oldValue = wc.getAttribute(fLaunchConfigTargetAttribute, DEFAULT_ATTR_DEBUGGER_REGISTER_GROUPS_VALUE);
			} catch (CoreException e) {
				// ignored, treat as default
			}
			if (!Objects.equals(oldValue, newValue)) {
				wc.setAttribute(fLaunchConfigTargetAttribute, newValue);
				wc.doSave();
			}
		} catch (CoreException e) {
			abort(e.getMessage() + ", cause: " + e.getCause(), e); //$NON-NLS-1$
		}
	}

	protected IRegisterDescriptor findDescriptor(String originalGroupName, String name) {
		return new RegisterDescriptor(originalGroupName, name);
	}

	private IRegisterGroupDescriptor createGroupFromMemento(String memento) throws CoreException {
		Node node = DebugPlugin.parseDocument(memento);
		if (node.getNodeType() != Node.ELEMENT_NODE) {
			abort(CoreModelMessages.getString("CRegisterGroup.0"), null); //$NON-NLS-1$
		}
		Element element = (Element) node;
		if (!ELEMENT_REGISTER_GROUP.equals(element.getNodeName())) {
			abort(CoreModelMessages.getString("CRegisterGroup.1"), null); //$NON-NLS-1$
		}
		String groupName = element.getAttribute(ATTR_REGISTER_GROUP_NAME);
		if (groupName == null || groupName.length() == 0) {
			abort(CoreModelMessages.getString("CRegisterGroup.2"), null); //$NON-NLS-1$
		}
		String containerId = element.getAttribute(ATTR_REGISTER_CONTAINER_ID);
		String e = element.getAttribute(ATTR_REGISTER_GROUP_ENABLED);
		boolean enabled = Boolean.parseBoolean(e);

		return new RegisterGroupDescriptor(memento, groupName, enabled, containerId);
	}

	private String getMemento(IRegisterGroupDescriptor[] groups) throws CoreException {
		Document document = DebugPlugin.newDocument();
		Element element = document.createElement(ELEMENT_REGISTER_GROUP_LIST);
		for (IRegisterGroupDescriptor group : groups) {
			Element child = document.createElement(ELEMENT_GROUP);
			child.setAttribute(ATTR_REGISTER_GROUP_MEMENTO, getMemento(group));
			element.appendChild(child);
		}

		document.appendChild(element);
		return DebugPlugin.serializeDocument(document);
	}

	private String getMemento(IRegisterGroupDescriptor group) throws CoreException {
		Document document = DebugPlugin.newDocument();
		Element element = document.createElement(ELEMENT_REGISTER_GROUP);
		element.setAttribute(ATTR_REGISTER_GROUP_NAME, group.getName());
		element.setAttribute(ATTR_REGISTER_GROUP_ENABLED, String.valueOf(group.isEnabled()));
		element.setAttribute(ATTR_REGISTER_CONTAINER_ID, group.getContainerId());
		IRegisterDescriptor[] registerDescriptors = group.getChildren();
		for (int i = 0; i < registerDescriptors.length; ++i) {
			Element child = document.createElement(ELEMENT_REGISTER);
			child.setAttribute(ATTR_REGISTER_NAME, registerDescriptors[i].getName());
			child.setAttribute(ATTR_REGISTER_ORIGINAL_GROUP_NAME, registerDescriptors[i].getGroupName());
			element.appendChild(child);
		}

		document.appendChild(element);
		return DebugPlugin.serializeDocument(document);
	}

	private void abort(String message, Throwable exception) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, CDebugCorePlugin.PLUGIN_ID, IStatus.ERROR, message, exception);
		throw new CoreException(status);
	}

}
