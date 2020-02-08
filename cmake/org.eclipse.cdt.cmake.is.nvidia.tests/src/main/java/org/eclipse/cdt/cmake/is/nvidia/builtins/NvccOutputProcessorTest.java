/*******************************************************************************
 * Copyright (c) 2018 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.is.nvidia.builtins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.cmake.is.core.builtins.GccOutputProcessor;
import org.eclipse.cdt.cmake.is.core.builtins.IBuiltinsOutputProcessor;
import org.eclipse.cdt.cmake.is.core.builtins.OutputSniffer;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Martin Weber
 *
 */
public class NvccOutputProcessorTest {

  private GccOutputProcessor testee;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    testee = new GccOutputProcessor();
  }

  @Test
  public void testProcessFile() throws IOException {
    // pass resource content line-wise to the testee...
    ProcessingContextMock pc = new ProcessingContextMock();
    try (InputStream is = getClass().getResourceAsStream("cbd-nvcc.output.txt");
        OutputSniffer os = new OutputSniffer(testee, null, pc)) {
      byte[] buffer = new byte[1024];
      int length;
      while ((length = is.read(buffer)) > 0) {
        os.write(buffer, 0, length);
      }
    }

    // check __GNUC__
    for (ICLanguageSettingEntry entry : pc.entries) {
      if (entry.getKind() == ICLanguageSettingEntry.MACRO) {
        if ("__CUDACC_VER_BUILD__".equals(entry.getName()))
          assertEquals("value (" + entry.getName() + ")", "85", entry.getValue());
      }
    }

    int inc = 0;
    int macro = 0;
    for (ICLanguageSettingEntry entry : pc.entries) {
      if (entry.getKind() == ICLanguageSettingEntry.INCLUDE_PATH) {
        inc++;
        assertTrue("path", !"".equals(entry.getName()));
      } else if (entry.getKind() == ICLanguageSettingEntry.MACRO) {
        macro++;
        assertTrue("macro", !"".equals(entry.getName()));
        assertTrue("value (" + entry.getName() + ")", entry.getValue() != null);
      }
    }
    assertEquals("# include paths", 6, inc);
    assertEquals("# macros", 242, macro);
  }

  private static class ProcessingContextMock
      implements IBuiltinsOutputProcessor.IProcessingContext {

    private final List<ICLanguageSettingEntry> entries = Collections
        .synchronizedList(new ArrayList<ICLanguageSettingEntry>());

    @Override
    public boolean addSettingEntry(ICLanguageSettingEntry entry) {
      if (entry != null) {
        entries.add(entry);
        return true;
      }
      return false;
    }
  }
}
