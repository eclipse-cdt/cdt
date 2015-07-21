package org.eclipse.cdt.arduino.core.board;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Platform {

	private String name;
	private String architecture;
	private String version;
	private String category;
	private String url;
	private String archiveName;
	private String checksum;
	private String size;
	private List<Board> boards;
	private List<ToolDependency> toolsDependencies;

	private transient Package pkg;
	private transient Properties boardsFile;

	void setOwners(Package pkg) {
		this.pkg = pkg;
		for (Board board : boards) {
			board.setOwners(this);
		}
	}

	public Package getPackage() {
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

	public String getArchiveName() {
		return archiveName;
	}

	public String getChecksum() {
		return checksum;
	}

	public String getSize() {
		return size;
	}

	public List<Board> getBoards() {
		return boards;
	}

	public Board getBoard(String boardId) {
		for (Board board : boards) {
			if (boardId.equals(board.getId())) {
				return board;
			}
		}
		return null;
	}

	public List<ToolDependency> getToolsDependencies() {
		return toolsDependencies;
	}

	void install() throws IOException {
		Path boardPath = pkg.getManager().getArduinoHome().resolve("hardware").resolve(pkg.getName()) //$NON-NLS-1$
				.resolve(architecture).resolve(version);
		boardsFile = new Properties();
		try (Reader reader = new FileReader(boardPath.toFile())) {
			boardsFile.load(reader);
		}

		// Replace the boards with a real ones
		boards = new ArrayList<>();
		for (Map.Entry<Object, Object> entry : boardsFile.entrySet()) {
			String key = (String) entry.getKey();
			String[] fragments = key.split("."); //$NON-NLS-1$
			if (fragments.length == 2 && "name".equals(fragments[1])) { //$NON-NLS-1$
				boards.add(new Board().setId(fragments[0]).setName((String) entry.getValue()).setOwners(this));
			}
		}
	}

	Properties getBoardsFile() {
		return boardsFile;
	}

}
