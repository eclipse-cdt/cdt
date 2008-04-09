/*******************************************************************************
 * Copyright (c) 2008 Radoslav Gerganov
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Radoslav Gerganov - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.rapi;

import java.util.Date;

/**
 * This class describes a file found by <code>CeFindFirstFile</code>
 * or <code>CeFindAllFiles</code>
 * 
 * @author Radoslav Gerganov
 */
public class RapiFindData {
  public int fileAttributes;
  public long creationTime;
  public long lastAccessTime;
  public long lastWriteTime;
  public long fileSize;
  public int oid;
  public String fileName;
  
  public Date getCreationTime() {
    return new Date((creationTime / 10000) - Rapi.TIME_DIFF);
  }

  public Date getLastAccessTime() {
    return new Date((lastAccessTime / 10000) - Rapi.TIME_DIFF);
  }

  public Date getLastWriteTime() {
    return new Date((lastWriteTime / 10000) - Rapi.TIME_DIFF);
  }
}
