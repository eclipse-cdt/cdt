/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Sergey Prigogin, Google
 *******************************************************************************/
package org.eclipse.cdt.internal.formatter;

import java.util.Map;

import org.eclipse.cdt.core.formatter.CodeFormatter;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.text.edits.TextEdit;


public class CCodeFormatter extends CodeFormatter {
  
  private DefaultCodeFormatterOptions preferences;
  
  public CCodeFormatter() {
    this(new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getEclipseDefaultSettings()), null);
  }
  
  public CCodeFormatter(DefaultCodeFormatterOptions preferences) {
    this(preferences, null);
  }

  public CCodeFormatter(DefaultCodeFormatterOptions defaultCodeFormatterOptions, Map options) {
    setOptions(options);
    if (defaultCodeFormatterOptions != null) {
      preferences.set(defaultCodeFormatterOptions.getMap());
    }
  }

  public CCodeFormatter(Map options) {
    this(null, options);
  }
  
  public String createIndentationString(final int indentationLevel) {
    if (indentationLevel < 0) {
      throw new IllegalArgumentException();
    }
    
    int tabs = 0;
    int spaces = 0;
    switch (preferences.tab_char) {
      case DefaultCodeFormatterOptions.SPACE :
        spaces = indentationLevel * preferences.tab_size;
        break;
        
      case DefaultCodeFormatterOptions.TAB :
        tabs = indentationLevel;
        break;
        
      case DefaultCodeFormatterOptions.MIXED :
        int tabSize = preferences.tab_size;
        int spaceEquivalents = indentationLevel * preferences.indentation_size;
        tabs = spaceEquivalents / tabSize;
        spaces = spaceEquivalents % tabSize;
        break;
        
      default:
        return EMPTY_STRING;
    }
    
    if (tabs == 0 && spaces == 0) {
      return EMPTY_STRING;
    }
    StringBuffer buffer = new StringBuffer(tabs + spaces);
    for (int i = 0; i < tabs; i++) {
      buffer.append('\t');
    }
    for(int i = 0; i < spaces; i++) {
      buffer.append(' ');
    }
    return buffer.toString();
  }

  public void setOptions(Map options) {
    if (options != null) {
      preferences = new DefaultCodeFormatterOptions(options);
    } else {
      preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getEclipseDefaultSettings());
    }
  }

  public TextEdit format(int kind, String source, int offset, int length, int indentationLevel, String lineSeparator) {
    // TODO Not implemented yet
    return null;
  }
}
