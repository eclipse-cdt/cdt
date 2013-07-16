package org.eclipse.cdt.thirdParty.tests.languageSettingsProviders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.managedbuilder.language.settings.providers.ToolchainBuiltinSpecsDetector;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

public class ThirdPartyBuiltinSpecsDetector extends ToolchainBuiltinSpecsDetector implements ILanguageSettingsEditableProvider {

	private static final String THIRD_PARTY_TOOLCHAIN_ID = "org.eclipse.cdt.thirdParty.tests.ThirdPartyToolChain";  //$NON-NLS-1$
	
	private enum State {NONE, EXPECTING_LOCAL_INCLUDE, EXPECTING_SYSTEM_INCLUDE}
	private State state = State.NONE;
	
	@SuppressWarnings("nls")
	private static final AbstractOptionParser[] optionParsers = {
		new IncludePathOptionParser("#include \"(\\S.*)\"", "$1", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY | ICSettingEntry.LOCAL),
		new IncludePathOptionParser("#include <(\\S.*)>", "$1", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
		new MacroOptionParser("#define\\s+(\\S*\\(.*?\\))\\s*(.*)", "$1", "$2", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
		new MacroOptionParser("#define\\s+(\\S*)\\s*(\\S*)", "$1", "$2", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
	};

	@Override
	public String getToolchainId() {
		return THIRD_PARTY_TOOLCHAIN_ID;
	}
	
	@Override
	protected AbstractOptionParser[] getOptionParsers() {
		return optionParsers;
	}

	@Override
	protected List<String> parseOptions(String line) {
		line = line.trim();
		// contribution of -dD option
		if (line.startsWith("#define")) { //$NON-NLS-1$
			return makeList(line);
		}

		// contribution of includes
		if (line.equals("#include \"...\" search starts here:")) { //$NON-NLS-1$
			state = State.EXPECTING_LOCAL_INCLUDE;
		} else if (line.equals("#include <...> search starts here:")) { //$NON-NLS-1$
			state = State.EXPECTING_SYSTEM_INCLUDE;
		} else if (line.startsWith("End of search list.")) { //$NON-NLS-1$
			state = State.NONE;
		} else if (line.startsWith("End of framework search list.")) { //$NON-NLS-1$
			state = State.NONE;
		} else if (state==State.EXPECTING_LOCAL_INCLUDE) {
			// making that up for the parser to figure out
			line = "#include \""+line+"\""; //$NON-NLS-1$ //$NON-NLS-2$
			return makeList(line);
		} else if (state==State.EXPECTING_SYSTEM_INCLUDE) {
			line = "#include <"+line+">"; //$NON-NLS-1$ //$NON-NLS-2$
			return makeList(line);
		}
		return null;
	}

	private List<String> makeList(String line) {
		List<String> list = new ArrayList<String>();
		list.add(line);
		return list;
	}

	@Override
	public ThirdPartyBuiltinSpecsDetector cloneShallow() throws CloneNotSupportedException {
		return (ThirdPartyBuiltinSpecsDetector) super.cloneShallow();
	}

	@Override
	public ThirdPartyBuiltinSpecsDetector clone() throws CloneNotSupportedException {
		return (ThirdPartyBuiltinSpecsDetector) super.clone();
	}

	/**
	 * Returns list of environment variables to be used during execution of provider's command.
	 * Implementers are expected to add their variables to the end of the list.
	 *
	 * @return list of environment variables.
	 * @since 8.2
	 */
	@Override
	protected List<IEnvironmentVariable> getEnvironmentVariables() {
		List<IEnvironmentVariable> parentEnv = super.getEnvironmentVariables();
		List<IEnvironmentVariable> vars = new ArrayList<IEnvironmentVariable>();	
		if (currentProject != null) {
			vars.add(new EnvironmentVariable("TEST_VARIABLE", "1")); //$NON-NLS-1$ //$NON-NLS-2$
			vars.add(new EnvironmentVariable("LANGUAGE", "en")); //$NON-NLS-1$ //$NON-NLS-2$
			vars.add(new EnvironmentVariable("LC_ALL", "C.UTF-8")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if(vars.size() == 0) {
			return parentEnv;
		}
		return vars;
	}

	@Override
	protected void setSettingEntries(List<ICLanguageSettingEntry> entries) {
		if (entries != null) {
			for (ICLanguageSettingEntry entry : entries) {
				if(!detectedSettingEntries.contains(entry)) {
					detectedSettingEntries.add(entry);
				}
			}
		}
	}

	@Override
	public boolean isConsoleEnabled() {
		return true;
	}

	protected static String resolveString(String string) {
		String result = string.replaceAll("%[-+_a-zA-Z0-9]*%", ""); //$NON-NLS-1$ //$NON-NLS-2$
		String previous;
		do {
			previous = result;
			result = result.replaceAll("\\$\\([^()]+\\)", ""); //$NON-NLS-1$ //$NON-NLS-2$
			result = result.replaceAll("\\$\\{[^{}]+\\}", ""); //$NON-NLS-1$ //$NON-NLS-2$
		} while(!result.equals(previous));
		result = result.replaceAll("\\$[-+_a-zA-Z0-9]*", ""); //$NON-NLS-1$ //$NON-NLS-2$
		return result;
	}

	protected static void addPathString(List<String> pathList, String pathString) {
		pathString = unquote(pathString);
		boolean isWindows = Platform.getOS().equals(Platform.OS_WIN32);
		int split = pathString.indexOf(':', isWindows ? 2 : 0);
		if(split == -1) {
			split = pathString.indexOf(';');
		}
		if(split != -1) {
			addPathString(pathList, pathString.substring(0, split));
			addPathString(pathList, pathString.substring(split + 1));
		} else {
			if(Path.EMPTY.isValidPath(pathString)) {
				try {
					IPath path = new Path(pathString);
					if(path.isAbsolute()) {
						path = new Path(path.toFile().getCanonicalPath());
					}
					pathString = path.toString();
					if(!pathList.contains(pathString) && path.toFile().exists()) {
						pathList.add(pathString);
					}
				} catch (IOException e) {
				}
			}
		}
	}
	
	public static String unquote(String str) {
		str = str.trim();
		if (str.length() == 0 || str.charAt(0) != '"') {
			return str;
		}
		int len = str.length();
		char[] source = str.toCharArray();
		char[] target = new char [len+1];
		boolean escaped = false;
		int tIndx = 0;
		for (int sIndx=0; sIndx<len; sIndx++) {
			char ch = source[sIndx];
			if (ch != '"' || escaped) {
				if (ch == '\\' && !escaped) {
					escaped = true;
				}
				else {
					if (ch != '"' && ch != '\\' && escaped) {  // retain single unescaped backslash
						target[tIndx++] = '\\';
					}
					escaped = false;
					target[tIndx++] = ch;
				}
			}
		}
		target[tIndx++] = '\0';
		return new String(target, 0, tIndx-1);
	}
}
