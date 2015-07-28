package org.eclipse.cdt.arduino.core.internal.board;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.arduino.core.internal.ArduinoPreferences;
import org.eclipse.cdt.arduino.core.internal.HierarchicalProperties;
import org.eclipse.cdt.arduino.core.internal.Messages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

@SuppressWarnings("restriction")
public class Platform {

	private String name;
	private String architecture;
	private String version;
	private String category;
	private String url;
	private String archiveFileName;
	private String checksum;
	private String size;
	private List<Board> boards;
	private List<ToolDependency> toolsDependencies;

	private transient BoardPackage pkg;
	private transient HierarchicalProperties boardsFile;

	void setOwners(BoardPackage pkg) {
		this.pkg = pkg;
		for (Board board : boards) {
			board.setOwners(this);
		}
	}

	public BoardPackage getPackage() {
		return pkg;
	}

	public String getName() {
		return name;
	}

	public String getArchitecture() {
		return architecture;
	}

	public String getVersion() {
		return version;
	}

	public String getCategory() {
		return category;
	}

	public String getUrl() {
		return url;
	}

	public String getArchiveFileName() {
		return archiveFileName;
	}

	public String getChecksum() {
		return checksum;
	}

	public String getSize() {
		return size;
	}

	public List<Board> getBoards() throws CoreException {
		if (isInstalled() && boardsFile == null) {
			Properties boardProps = new Properties();
			try (Reader reader = new FileReader(getInstallPath().resolve("boards.txt").toFile())) { //$NON-NLS-1$
				boardProps.load(reader);
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "Loading boards", e));
			}

			boardsFile = new HierarchicalProperties(boardProps);

			// Replace the boards with a real ones
			boards = new ArrayList<>();
			for (HierarchicalProperties child : boardsFile.getChildren().values()) {
				if (child.getChild("name") != null) { //$NON-NLS-1$
					// assume things with names are boards
					boards.add(new Board(child).setOwners(this));
				}
			}
		}
		return boards;
	}

	public Board getBoard(String boardId) throws CoreException {
		for (Board board : getBoards()) {
			if (boardId.equals(board.getId())) {
				return board;
			}
		}
		return null;
	}

	public List<ToolDependency> getToolsDependencies() {
		return toolsDependencies;
	}

	public boolean isInstalled() {
		return getInstallPath().resolve("boards.txt").toFile().exists(); //$NON-NLS-1$
	}

	private Path getInstallPath() {
		return ArduinoPreferences.getArduinoHome().resolve("hardware").resolve(pkg.getName()).resolve(architecture) //$NON-NLS-1$
				.resolve(version);
	}

	public IStatus install(IProgressMonitor monitor) throws CoreException {
		try {
			try (CloseableHttpClient client = HttpClients.createDefault()) {
				HttpGet get = new HttpGet(url);
				try (CloseableHttpResponse response = client.execute(get)) {
					if (response.getStatusLine().getStatusCode() >= 400) {
						return new Status(IStatus.ERROR, Activator.getId(), response.getStatusLine().getReasonPhrase());
					} else {
						HttpEntity entity = response.getEntity();
						if (entity == null) {
							return new Status(IStatus.ERROR, Activator.getId(), Messages.ArduinoBoardManager_1);
						}
						// the archive has the version number as the root
						// directory
						Path installPath = getInstallPath().getParent();
						Files.createDirectories(installPath);
						Path archivePath = installPath.resolve(archiveFileName);
						Files.copy(entity.getContent(), archivePath, StandardCopyOption.REPLACE_EXISTING);

						// extract
						ArchiveInputStream archiveIn = null;
						try {
							String compressor = null;
							String archiver = null;
							if (archiveFileName.endsWith("tar.bz2")) { //$NON-NLS-1$
								compressor = CompressorStreamFactory.BZIP2;
								archiver = ArchiveStreamFactory.TAR;
							} else if (archiveFileName.endsWith(".tar.gz") || archiveFileName.endsWith(".tgz")) { //$NON-NLS-1$ //$NON-NLS-2$
								compressor = CompressorStreamFactory.GZIP;
								archiver = ArchiveStreamFactory.TAR;
							} else if (archiveFileName.endsWith(".tar.xz")) { //$NON-NLS-1$
								compressor = CompressorStreamFactory.XZ;
								archiver = ArchiveStreamFactory.TAR;
							} else if (archiveFileName.endsWith(".zip")) { //$NON-NLS-1$
								archiver = ArchiveStreamFactory.ZIP;
							}

							InputStream in = new BufferedInputStream(new FileInputStream(archivePath.toFile()));
							if (compressor != null) {
								in = new CompressorStreamFactory().createCompressorInputStream(compressor, in);
							}
							archiveIn = new ArchiveStreamFactory().createArchiveInputStream(archiver, in);

							for (ArchiveEntry entry = archiveIn.getNextEntry(); entry != null; entry = archiveIn
									.getNextEntry()) {
								if (entry.isDirectory()) {
									continue;
								}

								// TODO check for soft links in tar files.
								Path entryPath = installPath.resolve(entry.getName());
								Files.createDirectories(entryPath.getParent());
								Files.copy(archiveIn, entryPath, StandardCopyOption.REPLACE_EXISTING);
							}
						} finally {
							if (archiveIn != null) {
								archiveIn.close();
							}
						}
					}
				}
			}
			return Status.OK_STATUS;
		} catch (IOException | CompressorException | ArchiveException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getId(), "Installing Platform", e));
		}
	}

}
