/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
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

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.internal.docker.launcher.ContainerPropertyVolumesModel.MountType;
import org.eclipse.core.runtime.Platform;

/**
 * Data binding model for container data volumes
 *
 */
public class DataVolumeModel extends BaseDatabindingModel implements Comparable<DataVolumeModel> {

	private static final String SEPARATOR = ":"; //$NON-NLS-1$

	public static final String CONTAINER_PATH = "containerPath"; //$NON-NLS-1$

	public static final String MOUNT_TYPE = "mountType"; //$NON-NLS-1$

	public static final String MOUNT = "mount"; //$NON-NLS-1$

	public static final String HOST_PATH_MOUNT = "hostPathMount"; //$NON-NLS-1$

	public static final String READ_ONLY_VOLUME = "readOnly"; //$NON-NLS-1$

	public static final String CONTAINER_MOUNT = "containerMount"; //$NON-NLS-1$

	public static final String SELECTED = "selected"; //$NON-NLS-1$

	private final String id = UUID.randomUUID().toString();

	private String containerPath;

	private MountType mountType;

	private String mount;

	private String hostPathMount;

	private String containerMount;

	private boolean readOnly = false;

	private boolean selected;

	/**
	 * Default constructor
	 */
	public DataVolumeModel() {
	}

	/**
	 * Constructor
	 *
	 * @param containerPath
	 *            the container path
	 */
	public DataVolumeModel(final String containerPath) {
		this.containerPath = containerPath;
		this.mountType = MountType.NONE;
	}

	public DataVolumeModel(final String containerPath, final String hostPath, final boolean readOnly) {
		this.containerPath = containerPath;
		this.mountType = MountType.HOST_FILE_SYSTEM;
		this.hostPathMount = hostPath;
		this.mount = this.hostPathMount;
		this.readOnly = readOnly;
	}

	public DataVolumeModel(final DataVolumeModel selectedDataVolume) {
		this.containerPath = selectedDataVolume.getContainerPath();
		this.mountType = selectedDataVolume.getMountType();
		if (this.mountType != null) {
			switch (this.mountType) {
			case CONTAINER:
				this.containerMount = selectedDataVolume.getMount();
				break;
			case HOST_FILE_SYSTEM:
				this.hostPathMount = selectedDataVolume.getMount();
				this.readOnly = selectedDataVolume.isReadOnly();
				break;
			case NONE:
				break;
			}
		} else {
			this.mountType = MountType.NONE;
		}
	}

	/**
	 * Create a DataVolumeModel from a toString() output.
	 *
	 * @param fromString
	 * @return DataVolumeModel
	 */
	public static DataVolumeModel parseString(final String fromString) {
		final DataVolumeModel model = new DataVolumeModel();
		final String[] items = fromString.split(SEPARATOR); // $NON-NLS-1$
		model.containerPath = items[0];
		model.mountType = MountType.valueOf(items[1]);
		switch (model.mountType) {
		case CONTAINER:
			model.setContainerMount(items[2]);
			model.setSelected(Boolean.valueOf(items[3]));
			break;
		case HOST_FILE_SYSTEM:
			// For Windows, there are multiple formats. If a user has specified
			// a windows drive using the : separator, we have to form the
			// host path by merging the path back together. If the user
			// has specified an alternate format, we don't do this.
			if (Platform.OS_WIN32.equals(Platform.getOS()) && items.length > 5) {
				model.setHostPathMount(items[2] + SEPARATOR + items[3]);
				model.setReadOnly(Boolean.valueOf(items[4]));
				model.setSelected(Boolean.valueOf(items[5]));
			} else {
				model.setHostPathMount(items[2]);
				model.setReadOnly(Boolean.valueOf(items[3]));
				model.setSelected(Boolean.valueOf(items[4]));
			}
			break;
		case NONE:
			model.setSelected(Boolean.valueOf(items[2]));
			break;
		}
		return model;
	}

	/**
	 * creates a {@link DataVolumeModel} from the 'volumeFrom' container info
	 *
	 * @param volumeFrom
	 *            the value to parse.
	 *
	 *            Format: <code>&lt;containerName&gt;</code>
	 *
	 * @See <a href="https://docs.docker.com/engine/userguide/dockervolumes/">
	 *      https://docs.docker.com/engine/userguide/dockervolumes/</a>
	 */
	public static DataVolumeModel parseVolumeFrom(String volumeFrom) {
		final DataVolumeModel model = new DataVolumeModel();
		model.mountType = MountType.CONTAINER;
		model.containerMount = volumeFrom;
		model.selected = true;
		return model;
	}

	/**
	 * creates a {@link DataVolumeModel} from the 'volumeFrom' container info
	 *
	 * @param volumeFrom
	 *            the value to parse. Format:
	 *            <code>&lt;host_path&gt;:&lt;container_path&gt;:&lt;label_suffix_flag&gt;</code>
	 *
	 * @See <a href="https://docs.docker.com/engine/userguide/dockervolumes/">
	 *      https://docs.docker.com/engine/userguide/dockervolumes/</a>
	 */
	public static DataVolumeModel parseHostBinding(String volumeFrom) {
		final DataVolumeModel model = new DataVolumeModel();
		final String[] items = volumeFrom.split(SEPARATOR); // $NON-NLS-1$
		// converts the host path to a valid Win32 path if Platform OS is Win32
		model.setHostPathMount(convertToWin32Path(Platform.getOS(), items[0]));
		model.containerPath = items[1];
		model.mountType = MountType.HOST_FILE_SYSTEM;
		if (items[2].equals("ro")) {
			model.setReadOnly(true);
		} else {
			model.setReadOnly(false);
		}
		model.selected = true;
		return model;
	}

	/**
	 * Converts the given path to a portable form, replacing all "\" and ": "
	 * with "/" if the given <code>os</code> is {@link Platform#OS_WIN32}.
	 *
	 * @param os
	 *            the current OS
	 * @param path
	 *            the path to convert
	 * @return the converted path or the given path
	 * @see {@link Platform#getOS()}
	 */
	public static String convertToWin32Path(final String os, final String path) {
		if (os != null && os.equals(Platform.OS_WIN32)) {
			// replace all "/" with "\" and then drive info (eg "/c/" to "C:/")
			final Matcher m = Pattern.compile("^/([a-zA-Z])/").matcher(path); //$NON-NLS-1$
			if (m.find()) {
				final StringBuffer b = new StringBuffer();
				m.appendReplacement(b, m.group(1).toUpperCase());
				b.append(":\\"); //$NON-NLS-1$
				m.appendTail(b);
				return b.toString().replace('/', '\\'); // $NON-NLS-1$
														// //$NON-NLS-2$
			}
		}
		return path;
	}

	public String getContainerPath() {
		return this.containerPath;
	}

	public void setContainerPath(final String containerPath) {
		firePropertyChange(CONTAINER_PATH, this.containerPath, this.containerPath = containerPath);
	}

	public String getMount() {
		return mount;
	}

	public void setMount(final String mount) {
		firePropertyChange(MOUNT, this.mount, this.mount = mount);
	}

	public MountType getMountType() {
		return mountType;
	}

	public void setMountType(final MountType mountType) {
		// ignore 'null' assignments that may come from the UpdateStrategy
		// in
		// the EditDataVolumePage when a radion button is unselected.
		if (mountType == null) {
			return;
		}
		firePropertyChange(MOUNT_TYPE, this.mountType, this.mountType = mountType);
		if (this.mountType == MountType.NONE) {
			setMount("");
		}

	}

	public String getHostPathMount() {
		return hostPathMount;
	}

	public void setHostPathMount(final String hostPathMount) {
		firePropertyChange(HOST_PATH_MOUNT, this.hostPathMount, this.hostPathMount = hostPathMount);
		if (this.mountType == MountType.HOST_FILE_SYSTEM) {
			setMount(this.hostPathMount);
		}
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(final boolean readOnly) {
		firePropertyChange(READ_ONLY_VOLUME, this.readOnly, this.readOnly = readOnly);
	}

	public String getContainerMount() {
		return this.containerMount;
	}

	public void setContainerMount(final String containerMount) {
		firePropertyChange(CONTAINER_MOUNT, this.containerMount, this.containerMount = containerMount);
		if (this.mountType == MountType.CONTAINER) {
			setMount(this.containerMount);
		}
	}

	public boolean getSelected() {
		return selected;
	}

	public void setSelected(final boolean selected) {
		firePropertyChange(SELECTED, this.selected, this.selected = selected);
	}

	@Override
	public int compareTo(final DataVolumeModel other) {
		return this.getContainerPath().compareTo(other.getContainerPath());
	}

	// FIXME we should have a dedicated method to serialize the bean
	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append(this.containerPath + SEPARATOR + getMountType() + SEPARATOR);
		switch (getMountType()) {
		case CONTAINER:
			buffer.append(getContainerMount());
			break;
		case HOST_FILE_SYSTEM:
			buffer.append(getHostPathMount() + SEPARATOR); // $NON-NLS-1$
			buffer.append(isReadOnly());
			break;
		case NONE:
			break;
		}
		buffer.append(SEPARATOR).append(this.selected);
		return buffer.toString();
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
		DataVolumeModel other = (DataVolumeModel) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
