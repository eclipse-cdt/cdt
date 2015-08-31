package org.eclipse.cdt.core.build;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;

/**
 * The GCC toolchain. Placing it in cdt.core for now.
 * 
 * TODO move to it's own plug-in.
 * 
 * @since 5.12
 */
public class GCCToolChain extends CToolChain {

	public static final String ID = "org.eclipse.cdt.core.gcc"; //$NON-NLS-1$

	public GCCToolChain(CBuildConfiguration config) {
		super(config);
	}

	private static Map<CBuildConfiguration, GCCToolChain> cache = new HashMap<>();

	public static class Factory implements IAdapterFactory {
		@SuppressWarnings("unchecked")
		@Override
		public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
			if (adapterType.equals(GCCToolChain.class) && adaptableObject instanceof CBuildConfiguration) {
				CBuildConfiguration config = (CBuildConfiguration) adaptableObject;
				GCCToolChain toolChain = cache.get(config);
				if (toolChain == null) {
					toolChain = new GCCToolChain(config);
					cache.put(config, toolChain);
				}
				return (T) toolChain;
			}
			return null;
		}

		@Override
		public Class<?>[] getAdapterList() {
			return new Class<?>[] { GCCToolChain.class };
		}
	}

	@Override
	public void scanBuildOutput(IFolder buildFolder, String commandLine, boolean perProject)
			throws CoreException {
		try {
			if (Platform.getOS().equals(Platform.OS_WIN32)) {
				// Need to flip over the slashes on Windows
				commandLine = commandLine.replace('\\', '/');
			}
			String[] command = splitCommand(commandLine);

			// Change output to stdout
			for (int i = 0; i < command.length - 1; ++i) {
				if (command[i].equals("-o")) { //$NON-NLS-1$
					command[i + 1] = "-"; //$NON-NLS-1$
					break;
				}
			}

			// Change source file to a tmp file (needs to be empty)
			Path tmpFile = null;
			IFile file = null;
			for (int i = 1; i < command.length; ++i) {
				if (!command[i].startsWith("-")) { //$NON-NLS-1$
					// TODO optimize by dealing with multi arg options like -o
					IFile f = buildFolder.getFile(command[i]);
					if (f.exists() && CoreModel.isTranslationUnit(f)) {
						// replace it with a temp file
						Path parentPath = new File(((IFolder) f.getParent()).getLocationURI()).toPath();
						int n = 0;
						while (true) {
							tmpFile = parentPath.resolve(".sc" + n + "." + f.getFileExtension()); //$NON-NLS-1$ //$NON-NLS-2$
							command[i] = tmpFile.toString();
							try {
								Files.createFile(tmpFile);
								break;
							} catch (FileAlreadyExistsException e) {
								// try again
								++n;
							}
						}
						file = f;
						break;
					}
				}
			}

			if (file == null) {
				// can't do much without the source file
				CCorePlugin.log("No source file for scanner discovery"); //$NON-NLS-1$
				return;
			}

			// Add in the magic potion: -E -P -v -dD
			String[] fullCmd = new String[command.length + 4];
			fullCmd[0] = command[0];
			fullCmd[1] = "-E"; //$NON-NLS-1$
			fullCmd[2] = "-P"; //$NON-NLS-1$
			fullCmd[3] = "-v"; //$NON-NLS-1$
			fullCmd[4] = "-dD"; //$NON-NLS-1$
			System.arraycopy(command, 1, fullCmd, 5, command.length - 1);

			File buildDir = new File(buildFolder.getLocationURI());
			Files.createDirectories(buildDir.toPath());

			// Startup the command
			ProcessBuilder processBuilder = new ProcessBuilder(fullCmd).directory(buildDir)
					.redirectErrorStream(true);
			setEnvironment(processBuilder.environment());
			Process process = processBuilder.start();

			// Scan for the scanner info
			Map<String, String> symbols = new HashMap<>();
			List<String> includePath = new ArrayList<>();
			Pattern definePattern = Pattern.compile("#define (.*)\\s(.*)"); //$NON-NLS-1$
			boolean inIncludePaths = false;
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream()))) {
				for (String line = reader.readLine(); line != null; line = reader.readLine()) {
					if (inIncludePaths) {
						if (line.equals("End of search list.")) { //$NON-NLS-1$
							inIncludePaths = false;
						} else {
							includePath.add(line.trim());
						}
					} else if (line.startsWith("#define ")) { //$NON-NLS-1$
						Matcher matcher = definePattern.matcher(line);
						if (matcher.matches()) {
							symbols.put(matcher.group(1), matcher.group(2));
						}
					} else if (line.equals("#include <...> search starts here:")) { //$NON-NLS-1$
						inIncludePaths = true;
					}
				}
			}

			if (perProject) {
				IProject project = buildFolder.getProject();
				IContentType contentType = CCorePlugin.getContentType(project, file.getName());
				if (contentType != null) {
					ILanguage language = LanguageManager.getInstance().getLanguage(contentType, project);
					putScannerInfo(language, symbols, includePath, null, null, null);
				}
			} else {
				putScannerInfo(file, symbols, includePath, null, null, null);
			}

			if (tmpFile != null) {
				Files.delete(tmpFile);
			}
		} catch (IOException e) {
			throw new CoreException(
					new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, "Scanning build output", e)); //$NON-NLS-1$
		}
	}

	@Override
	public CConsoleParser[] getConsoleParsers() {
		// ../src/Test.cpp:4:1: error: 'x' was not declared in this scope

		return new CConsoleParser[] { new CConsoleParser("(.*?):(\\d+):(\\d+:)? (fatal )?error: (.*)") { //$NON-NLS-1$
			@Override
			protected int getSeverity(Matcher matcher) {
				return IMarker.SEVERITY_ERROR;
			}

			@Override
			protected String getMessage(Matcher matcher) {
				return matcher.group(5);
			}

			@Override
			protected int getLineNumber(Matcher matcher) {
				return Integer.parseInt(matcher.group(2));
			}

			@Override
			protected String getFileName(Matcher matcher) {
				return matcher.group(1);
			}

			@Override
			protected int getLinkOffset(Matcher matcher) {
				return 0;
			}

			@Override
			protected int getLinkLength(Matcher matcher) {
				return matcher.group(1).length() + 1 + matcher.group(2).length() + 1
						+ matcher.group(3).length();
			}
		} };
	}

}
