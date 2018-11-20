/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.board;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.arduino.core.internal.ArduinoPreferences;
import org.eclipse.cdt.arduino.core.internal.build.ArduinoBuildConfiguration;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class ArduinoLibrary {

	// JSON fields
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
	// end JSON fields

	private Path installPath;
	private ArduinoPlatform platform;

	public ArduinoLibrary() {
	}

	public ArduinoLibrary(Path propertiesFile) throws CoreException {
		installPath = propertiesFile.getParent();

		Properties props = new Properties();
		try (FileReader reader = new FileReader(propertiesFile.toFile())) {
			props.load(reader);
		} catch (IOException e) {
			throw Activator.coreException(e);
		}

		name = props.getProperty("name"); //$NON-NLS-1$
		version = props.getProperty("version"); //$NON-NLS-1$
		author = props.getProperty("author"); //$NON-NLS-1$
		maintainer = props.getProperty("maintainer"); //$NON-NLS-1$
		sentence = props.getProperty("sentence"); //$NON-NLS-1$
		paragraph = props.getProperty("paragraph"); //$NON-NLS-1$
		category = props.getProperty("category"); //$NON-NLS-1$
		architectures = Arrays.asList(props.getProperty("architectures").split(",")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public ArduinoLibrary(Path propertiesFile, ArduinoPlatform platform) throws CoreException {
		this(propertiesFile);
		this.platform = platform;
	}

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
		return installPath == null
				? ArduinoPreferences.getArduinoHome().resolve("libraries").resolve(name.replaceAll("[ ()]", "_")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						.resolve(version)
				: installPath;
	}

	public ArduinoPlatform getPlatform() {
		return platform;
	}

	public void install(IProgressMonitor monitor) throws CoreException {
		if (Files.exists(getInstallPath())) {
			uninstall(monitor);
		}

		try {
			ArduinoManager.downloadAndInstall(url, archiveFileName, getInstallPath(), monitor);
		} catch (IOException e) {
			throw Activator.coreException(e);
		}
	}

	public void uninstall(IProgressMonitor monitor) throws CoreException {
		try {
			ArduinoManager.recursiveDelete(getInstallPath());
		} catch (IOException e) {
			throw Activator.coreException(e);
		}
	}

	public Collection<Path> getIncludePath() {
		Path installPath = getInstallPath();
		Path srcPath = installPath.resolve("src"); //$NON-NLS-1$
		if (srcPath.toFile().isDirectory()) {
			return Collections.singletonList(srcPath);
		} else {
			Path utilityPath = installPath.resolve("utility"); //$NON-NLS-1$
			if (utilityPath.toFile().isDirectory()) {
				return Arrays.asList(installPath, utilityPath);
			} else {
				return Collections.singletonList(installPath);
			}
		}
	}

	private void getSources(Collection<String> sources, Path dir, boolean recurse) {
		for (File file : dir.toFile().listFiles()) {
			if (file.isDirectory()) {
				if (recurse) {
					getSources(sources, file.toPath(), recurse);
				}
			} else {
				if (ArduinoBuildConfiguration.isSource(file.getName())) {
					sources.add(ArduinoBuildConfiguration.pathString(file.toPath()));
				}
			}
		}
	}

	public Collection<String> getSources() {
		List<String> sources = new ArrayList<>();
		Path installPath = getInstallPath();
		Path srcPath = installPath.resolve("src"); //$NON-NLS-1$
		if (srcPath.toFile().isDirectory()) {
			getSources(sources, srcPath, true);
		} else {
			getSources(sources, installPath, false);
			Path utilityPath = installPath.resolve("utility"); //$NON-NLS-1$
			if (utilityPath.toFile().isDirectory()) {
				getSources(sources, utilityPath, false);
			}
		}
		return sources;
	}

	@Override
	public String toString() {
		return getName();
	}

	private String fixText(String text) {
		String fixed = text.replaceAll("&", "&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
		fixed = fixed.replaceAll("<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
		return fixed;
	}

	public String toFormText() {
		StringBuilder text = new StringBuilder();

		text.append("<form>"); //$NON-NLS-1$
		text.append(String.format("<p><b>%s: %s</b></p>", "Library", fixText(getName()))); //$NON-NLS-1$

		if (getMaintainer() != null) {
			text.append(String.format("<p>%s: %s</p>", "Maintainer", fixText(getMaintainer()))); //$NON-NLS-1$
		}

		if (getWebsite() != null) {
			text.append(String.format("<p><a href=\"%s\">%s</a></p>", getWebsite(), "Online help")); //$NON-NLS-1$
		}

		text.append(String.format("<p>%s</p>", getSentence()));
		if (getParagraph() != null && !getParagraph().equals(getSentence())) {
			text.append(String.format("<p>%s</p>", getParagraph()));
		}

		text.append("</form>");

		return text.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((platform == null) ? 0 : platform.hashCode());
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
		ArduinoLibrary other = (ArduinoLibrary) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (platform == null) {
			if (other.platform != null)
				return false;
		} else if (!platform.equals(other.platform))
			return false;
		return true;
	}

}
