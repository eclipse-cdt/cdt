/*******************************************************************************
 * Copyright (c) 2015, 2016 Red Hat.
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerImageInfo;

/**
 * Databinding model for the {@link ContainerPropertyTab}
 *
 */
public class ContainerPropertyVolumesModel extends BaseDatabindingModel {

	public enum MountType {
		NONE, HOST_FILE_SYSTEM, CONTAINER;
	}

	public static final String DATA_VOLUMES = "dataVolumes"; //$NON-NLS-1$

	public static final String SELECTED_DATA_VOLUMES = "selectedDataVolumes"; //$NON-NLS-1$

	private IDockerConnection connection;

	private IDockerImageInfo imageInfo = null;

	private Set<DataVolumeModel> selectedDataVolumes = new HashSet<>();

	private WritableList<DataVolumeModel> dataVolumes = new WritableList<>();

	private List<DataVolumeModel> previousVolumes = new ArrayList<>();

	private IDockerImage selectedImage;

	public ContainerPropertyVolumesModel(final IDockerConnection connection) {
		this.connection = connection;
	}

	public ContainerPropertyVolumesModel(final IDockerImage selectedImage) throws DockerException {
		this(selectedImage.getConnection());
		this.selectedImage = selectedImage;
	}

	public void setConnection(IDockerConnection connection) {
		this.connection = connection;
		setSelectedImage(null);
	}

	public IDockerConnection getConnection() {
		return connection;
	}

	/**
	 * Refreshes the list of Volumes to display in the for the given
	 *
	 * @param selectedImage
	 */
	public void setSelectedImage(final IDockerImage selectedImage) {
		if (this.selectedImage == null || !this.selectedImage.equals(selectedImage)) {
			this.selectedImage = selectedImage;
			if (selectedImage != null) {
				this.imageInfo = selectedImage.getConnection().getImageInfo(selectedImage.id());
				if (this.imageInfo.config() != null && this.imageInfo.config().volumes() != null) {
					for (DataVolumeModel dvm : previousVolumes) {
						removeDataVolume(dvm);
						selectedDataVolumes.remove(dvm);
					}
					final List<DataVolumeModel> volumes = new ArrayList<>();
					for (String volume : this.imageInfo.config().volumes().keySet()) {
						volumes.add(new DataVolumeModel(volume));
					}
					setDataVolumes(volumes);
					previousVolumes = volumes;
				}
			} else {
				setDataVolumes(Collections.<DataVolumeModel>emptyList());
			}
		}

	}

	public IDockerImage getSelectedImage() {
		return selectedImage;
	}

	public IDockerImageInfo getSelectedImageInfo() {
		return imageInfo;
	}

	public WritableList<DataVolumeModel> getDataVolumes() {
		return dataVolumes;
	}

	public void setDataVolumes(final Collection<DataVolumeModel> volumes) {
		if (volumes != null) {
			this.dataVolumes.addAll(volumes);
		}
	}

	public void clearDataVolumes() {
		this.dataVolumes.clear();
	}

	public void removeDataVolume(final DataVolumeModel dataVolume) {
		this.dataVolumes.remove(dataVolume);
	}

	public Set<DataVolumeModel> getSelectedDataVolumes() {
		return selectedDataVolumes;
	}

	public void setSelectedDataVolumes(final Set<DataVolumeModel> selectedDataVolumes) {
		firePropertyChange(SELECTED_DATA_VOLUMES, this.selectedDataVolumes,
				this.selectedDataVolumes = selectedDataVolumes);
	}

}
