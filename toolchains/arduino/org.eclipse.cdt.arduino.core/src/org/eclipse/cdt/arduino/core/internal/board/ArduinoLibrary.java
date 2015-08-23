package org.eclipse.cdt.arduino.core.internal.board;

import java.nio.file.Path;
import java.util.List;

import org.eclipse.cdt.arduino.core.internal.ArduinoPreferences;
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
		return ArduinoPreferences.getArduinoHome().resolve("libraries").resolve(name).resolve(version); //$NON-NLS-1$
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

}
