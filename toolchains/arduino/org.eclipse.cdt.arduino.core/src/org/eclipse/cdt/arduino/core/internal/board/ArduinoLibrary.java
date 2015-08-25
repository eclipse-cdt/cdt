package org.eclipse.cdt.arduino.core.internal.board;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.arduino.core.internal.ArduinoPreferences;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class ArduinoLibrary {

	private String name;
	private String version;
	private String author;
	private String maintainer;
	private String sentence;
	private String paragraph;
	private String website;
	private String category;
	private List<String> architectures;
	private List<String> types;
	private String url;
	private String archiveFileName;
	private int size;
	private String checksum;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getMaintainer() {
		return maintainer;
	}

	public void setMaintainer(String maintainer) {
		this.maintainer = maintainer;
	}

	public String getSentence() {
		return sentence;
	}

	public void setSentence(String sentence) {
		this.sentence = sentence;
	}

	public String getParagraph() {
		return paragraph;
	}

	public void setParagraph(String paragraph) {
		this.paragraph = paragraph;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public List<String> getArchitectures() {
		return architectures;
	}

	public void setArchitectures(List<String> architectures) {
		this.architectures = architectures;
	}

	public List<String> getTypes() {
		return types;
	}

	public void setTypes(List<String> types) {
		this.types = types;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getArchiveFileName() {
		return archiveFileName;
	}

	public void setArchiveFileName(String archiveFileName) {
		this.archiveFileName = archiveFileName;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public Path getInstallPath() {
		return ArduinoPreferences.getArduinoHome().resolve("libraries").resolve(name.replace(' ', '_')) //$NON-NLS-1$
				.resolve(version);
	}

	public boolean isInstalled() {
		return getInstallPath().toFile().exists();
	}

	public IStatus install(IProgressMonitor monitor) {
		if (isInstalled()) {
			return Status.OK_STATUS;
		}

		return ArduinoManager.downloadAndInstall(url, archiveFileName, getInstallPath(), monitor);
	}

	public Collection<Path> getIncludePath() {
		Path installPath = getInstallPath();
		Path srcPath = installPath.resolve("src"); //$NON-NLS-1$
		if (srcPath.toFile().isDirectory()) {
			return Collections.singletonList(srcPath);
		} else {
			// TODO do I need the 'utility' directory?
			return Collections.singletonList(installPath);
		}
	}

	private void getSources(IProject project, Collection<Path> sources, Path dir, boolean recurse) {
		for (File file : dir.toFile().listFiles()) {
			if (file.isDirectory()) {
				if (recurse) {
					getSources(project, sources, file.toPath(), recurse);
				}
			} else {
				if (CoreModel.isValidSourceUnitName(project, file.getName())) {
					sources.add(file.toPath());
				}
			}
		}
	}

	public Collection<Path> getSources(IProject project) {
		List<Path> sources = new ArrayList<>();
		Path installPath = getInstallPath();
		Path srcPath = installPath.resolve("src"); //$NON-NLS-1$
		if (srcPath.toFile().isDirectory()) {
			getSources(project, sources, srcPath, true);
		} else {
			getSources(project, sources, installPath, false);
			Path utilityPath = installPath.resolve("utility"); //$NON-NLS-1$
			if (utilityPath.toFile().isDirectory()) {
				getSources(project, sources, utilityPath, false);
			}
		}
		return sources;
	}

}
