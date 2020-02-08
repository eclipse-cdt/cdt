/*******************************************************************************
 * Copyright (c) 2018 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.is.core.language.settings.providers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.eclipse.cdt.build.core.scannerconfig.ScannerConfigNature;
import org.eclipse.cdt.cmake.is.core.internal.Plugin;
import org.eclipse.cdt.cmake.is.core.internal.ParserDetection;
import org.eclipse.cdt.cmake.is.core.internal.builtins.CompilerBuiltinsDetector;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.language.settings.providers.ICBuildOutputParser;
import org.eclipse.cdt.core.language.settings.providers.ICListenerAgent;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.language.settings.providers.IWorkingDirectoryTracker;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializableProvider;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Element;

/**
 * A ILanguageSettingsProvider that detects the include paths and symbols built-in to the compilers found in the file
 * 'compile_commands.json' produced by cmake.<br>
 * NOTE: This class misuses interface ICBuildOutputParser to detect when a build did finish.<br>
 * NOTE: This class misuses interface ICListenerAgent to populate the {@link #getSettingEntries setting entries} on
 * workbench startup.
 *
 * @author Martin Weber
 */
/* TODO delete this after integration into core build
 */
public class BuiltinsCompileCommandsJsonParser extends LanguageSettingsSerializableProvider
    implements ILanguageSettingsEditableProvider, ICListenerAgent, ICBuildOutputParser, Cloneable {

  public static final String PROVIDER_ID = "org.eclipse.cdt.cmake.is.core.language.settings.providers.BuiltinsCompileCommandsJsonParser";
  private static final ILog log = Plugin.getDefault().getLog();

  /** storage key for with console */
  private static final String ATTR_WITH_CONSOLE = "console";

  private ICConfigurationDescription currentCfgDescription;

  @Override
  public void configureProvider(String id, String name, List<String> languages, List<ICLanguageSettingEntry> entries,
      Map<String, String> properties) {
    ArrayList<String> scope = new ArrayList<>();
    scope.add("org.eclipse.cdt.core.gcc");
    scope.add("org.eclipse.cdt.core.g++");
    scope.addAll(ParserDetection.getCustomLanguages());
    super.configureProvider(id, name, scope, entries, properties);
  }

  /**
   * Detects the compiler built-in include paths and symbols. Uses {@link CompileCommandsJsonParser} for parsing of the
   * json file and caching.
   *
   * @param initializingWorkbench
   *          {@code true} if the workbench is starting up. If {@code true}, this method will not trigger UI update to
   *          show newly detected include paths nor will it complain if a "compile_commands.json" file does not exist.
   * @throws CoreException
   */
  private void detectBuiltins(boolean initializingWorkbench) throws CoreException {
    if (currentCfgDescription instanceof ILanguageSettingsProvidersKeeper) {
      Iterable<CompilerBuiltinsDetector> detectors;

      final List<ILanguageSettingsProvider> lsps = ((ILanguageSettingsProvidersKeeper) currentCfgDescription)
          .getLanguageSettingProviders();
      // get the CompileCommandsJsonParser object, if the settings provider is enabled on the configuration
      final CompileCommandsJsonParser lsp;
      Optional<ILanguageSettingsProvider> lspO = lsps.stream()
          .filter(p -> CompileCommandsJsonParser.PROVIDER_ID.equals(p.getId())).findAny();
      if (lspO.isPresent()) {
        // CompileCommandsJsonParser is there, trigger it, regardless of provider order
        lsp = (CompileCommandsJsonParser) LanguageSettingsManager.getRawProvider(lspO.get());
        detectors = lsp.determineBuiltinDetectors(currentCfgDescription, true, initializingWorkbench);
      } else {
        // get a CompileCommandsJsonParser configured with the workspace default settings
        lsp = (CompileCommandsJsonParser) LanguageSettingsManager
            .getExtensionProviderCopy(CompileCommandsJsonParser.PROVIDER_ID, false);
        detectors = lsp.determineBuiltinDetectors(currentCfgDescription, false, initializingWorkbench);
      }

      if (initializingWorkbench && detectors == null) {
        // if initializing, always get the detectors
        detectors = lsp.getBuiltinDetectors(currentCfgDescription);
      }
      if (detectors != null) {
        // run each detector and gather the entries per language
        HashMap<String, Set<ICLanguageSettingEntry>> langMap = new HashMap<>(2, 1.0f);
        for (CompilerBuiltinsDetector detector : detectors) {
          final String languageId = detector.getLanguageId();
          // entries per language
          List<ICLanguageSettingEntry> entries = detector.run(isWithConsole());
          // use a Set here to avoid duplicates by name and kind ..
          Set<ICLanguageSettingEntry> allEntries = langMap.get(languageId);
          if (allEntries == null) {
            allEntries = new HashSet<>();
            langMap.put(languageId, allEntries);
          }
          allEntries.addAll(entries);
        }
        // store the entries per language
        for (Entry<String, Set<ICLanguageSettingEntry>> entry : langMap.entrySet()) {
          super.setSettingEntries(currentCfgDescription, null, entry.getKey(),
              Arrays.asList(entry.getValue().toArray(new ICLanguageSettingEntry[entry.getValue().size()])));
        }
      }
    }
  }

  /*-
   * interface ICBuildOutputParser
   */
  @Override
  public void startup(ICConfigurationDescription cfgDescription, IWorkingDirectoryTracker cwdTracker)
      throws CoreException {
    currentCfgDescription = cfgDescription;
  }

  /**
   * Invoked for each line in the build output.
   */
  // interface ICBuildOutputParser
  @Override
  public boolean processLine(String line) {
    // nothing to do
    return false;
  }

  /*-
   * interface ICBuildOutputParser
   */
  @Override
  public void shutdown() {
    try {
      detectBuiltins(false);
    } catch (CoreException ex) {
      log.log(new Status(IStatus.ERROR, Plugin.PLUGIN_ID, "shutdown()", ex));
    }
    // release resources for garbage collector
    currentCfgDescription = null;
  }

  @Override
  public BuiltinsCompileCommandsJsonParser clone() throws CloneNotSupportedException {
    return (BuiltinsCompileCommandsJsonParser) super.clone();
  }

  @Override
  public BuiltinsCompileCommandsJsonParser cloneShallow() throws CloneNotSupportedException {
    return (BuiltinsCompileCommandsJsonParser) super.cloneShallow();
  }

  /**
   * Overridden to misuse this to populate the {@link #getSettingEntries setting entries} on startup.<br>
   * {@inheritDoc}
   */
  @Override
  public void registerListener(ICConfigurationDescription cfgDescription) {
    if (cfgDescription != null) {
      // per-project or null if the user just added this provider on the provider tab
      currentCfgDescription = cfgDescription;
      try {
        detectBuiltins(true);
      } catch (CoreException ex) {
        log.log(new Status(IStatus.ERROR, Plugin.PLUGIN_ID, "registerListener()", ex));
      }
    } else {
      // per workspace (to populate on startup)
      Display.getDefault().asyncExec(() -> {
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        IProject[] projects = workspaceRoot.getProjects();
        CCorePlugin ccp = CCorePlugin.getDefault();
        // detect built-ins for any opened project that has a ScannerConfigNature...
        for (IProject project : projects) {
          try {
            if (project.isOpen() && project.hasNature(ScannerConfigNature.NATURE_ID)) {
              ICProjectDescription projectDescription = ccp.getProjectDescription(project, false);
              if (projectDescription != null) {
                ICConfigurationDescription activeConfiguration = projectDescription.getActiveConfiguration();
                if (activeConfiguration instanceof ILanguageSettingsProvidersKeeper) {
                  final List<ILanguageSettingsProvider> lsps = ((ILanguageSettingsProvidersKeeper) activeConfiguration)
                      .getLanguageSettingProviders();
                  for (ILanguageSettingsProvider lsp : lsps) {
                    if (PROVIDER_ID.equals(lsp.getId())) {
                      currentCfgDescription = activeConfiguration;
                      detectBuiltins(true);
                      break;
                    }
                  }
                }
              }
            }
          } catch (CoreException ex) {
            log.log(new Status(IStatus.ERROR, Plugin.PLUGIN_ID, "registerListener()", ex));
          }
        }
      });
    }
    // release resources for garbage collector
    currentCfgDescription = null;
  }

  /*-
   * @see org.eclipse.cdt.core.language.settings.providers.ICListenerAgent#unregisterListener()
   */
  @Override
  public void unregisterListener() {
  }

  /**
   * Gets whether a console in the console view should be allocated during detection.
   */
  public boolean isWithConsole() {
    return getPropertyBool(ATTR_WITH_CONSOLE);
  }

  /** Sets whether a console in the console view should be allocated during detection.
   */
  public void setWithConsole(boolean enabled) {
    if(enabled) {
      setPropertyBool(ATTR_WITH_CONSOLE, enabled);
    } else {
      properties.remove(ATTR_WITH_CONSOLE);
    }
  }

  @Override
  public void serializeEntries(Element elementProvider) {
    // no language setting entries to serialize, since entries come from the compile_commands.json file
  }

}
