/*******************************************************************************
 * Copyright (c) 2016-2019 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.is.core;

import java.util.List;

import org.eclipse.cdt.cmake.is.core.builtins.IBuiltinsDetectionBehavior;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.core.runtime.IPath;

/**
 * Parses the command-line produced by a specific tool invocation and detects LanguageSettings.
 *
 * @author Martin Weber
 */
public interface IToolCommandlineParser {
  /**
   * Parses all arguments given to the tool.
   *
   * @param cwd
   *          the current working directory of the compiler at the time of its invocation
   * @param args
   *          the command line arguments to process
   *
   * @throws NullPointerException
   *           if any of the arguments is {@code null}
   */
  public IResult processArgs(IPath cwd, String args);

  /**
   * Gets the language ID of the language that the tool compiles. If the tool is able to compile for multiple
   * programming languages, the specified {@code sourceFileExtension} may be used to compute the Language ID.
   * <p>
   * NOTE: CDT expects "org.eclipse.cdt.core.gcc" for the C language and "org.eclipse.cdt.core.g++" for the C++
   * language, so one of that IDs should be returned here. (Some extension to CDT may recognize different language IDs,
   * such as "com.nvidia.cuda.toolchain.language.cuda.cu".)
   * </p>
   *
   * @param sourceFileExtension
   *          the extension of the source file name
   *
   * @return a valid language ID, or {@code null}. If {@code null} or an invalid language ID, the results of argument
   *         processing will be ignored by CDT.
   */
  public String getLanguageId(String sourceFileExtension);

  /**
   * Gets the custom language IDs that the tool compiles. Some CDT based IDEs use language IDs of their own, for example
   * "com.nvidia.cuda.toolchain.language.cuda.cu" for CUDA. A custom language ID is an ID other than CDT's default IDs
   * for the C language ("org.eclipse.cdt.core.gcc") and the C++ ("org.eclipse.cdt.core.g++") language,
   *
   * @return the custom language IDs or {@code null} if the tool does not compile for languages other than C and C++
   */
  public List<String> getCustomLanguageIds();

  /**
   * Gets the {@code IBuiltinsDetectionBehavior} which specifies how built-in compiler macros and include path detection
   * is handled for a specific compiler.
   *
   * @return the {@code IBuiltinsDetectionBehavior} or {@code null} if the compiler does not support built-in detection
   */
  public IBuiltinsDetectionBehavior getIBuiltinsDetectionBehavior();

  /**
   * The result of processing a compiler command-line.
   *
   * @author Martin Weber
   *
   * @see IToolCommandlineParser#processArgs(IPath, String)
   */
  interface IResult {
    /**
     * Gets the language setting entries produced during processing.
     *
     * @return the language setting entries
     */
    List<ICLanguageSettingEntry> getSettingEntries();

    /**
     * Gets the compiler arguments from the command-line that affect built-in detection. For the GNU compilers, these
     * are options like {@code --sysroot} and options that specify the language's standard ({@code -std=c++17}.
     */
    List<String> getBuiltinDetectionArgs();
  } // IResult
}