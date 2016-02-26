/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Dumais (Ericsson) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.traditional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;

/** 
 * This class encapsulates the messy details of dealing with preferences 
 * entries that have unpredictable key names. This is necessary because the 
 * preference store does not allow getting a list of the keys, just a lokup
 * by exact key. So the work-around is to use one key to save a cvs string, 
 * containing the information necessary to reconstruct the keys for the 
 * unpredictable entries. 
 * @since 1.3
 */
public class MemorySpacePreferencesUtil {
  /** Reference to the plugin's preference store */
  IPreferenceStore fStore;
  
  // List of RGB colors that we can use, by default, for memory space backgrounds
  private final String[] colorPool = {
      "238,192,192", "250,238,195", "255,179,0", 
      "122,245,0", "184,242,255", "166,189,215", 
      "206,162,98", "245,138,157", "244,200,0", 
      "255,136,56", "244,255,128"
  };
  
  /** Constructor */
  MemorySpacePreferencesUtil() {
    fStore = TraditionalRenderingPlugin.getDefault().getPreferenceStore();
  }
  
  /**
   * @return an array of the currently known memory spaces ids, for which
   * a background color preference was created.
   */
  public String[] getMemorySpaceIds() {
    String csv = fStore.getString(TraditionalRenderingPreferenceConstants.MEM_KNOWN_MEMORY_SPACE_ID_LIST_CSV);
    return csv.isEmpty() ? new String[0] : csv.split(",");
  }
  
  /**
   * Updates the list of known memory space ids. For each new memory space,
   * a preference is set, assigning it a distinct background color, from 
   * a pool. Ids that are already known will be ignored.
   * @param ids an array of memory spaces ids, for the current platform.
   */
  public void updateMemorySpaces(String[] ids) {
    List<String> inputIdList = new ArrayList<String>(Arrays.asList(ids));         
    List<String> knownIdList = new ArrayList<String>(Arrays.asList(getMemorySpaceIds()));
    int nextIdIndex = knownIdList.size();
    boolean newIds;
    
    // Remove ids already known
    inputIdList.removeAll(knownIdList);
    newIds = inputIdList.size() > 0 ? true : false;
    
    // remaining ids are new
    for (String id : inputIdList) {
      knownIdList.add(id);
      // set default color for this memory space id
      setDefaultColorPreference(id, nextIdIndex);
      nextIdIndex++;
    }
    // Save set of known memory space ids, if new ones were added
    if (newIds) {
      setMemorySpaceIds(knownIdList.toArray(new String[knownIdList.size()]));
    }
  }
  

  /**
   * @return whether the memory space id given as parameter 
   * is already known. 
   */
  public boolean isMemorySpaceKnown(String id) {
    boolean known = false;
    String[] knownSpaces = getMemorySpaceIds();
    for (int i = 0; i < knownSpaces.length; i++) {
      if (knownSpaces[i].equals(id)) {
        known = true;
        break;
      }
    }
    return known;
  }
  
  /**
   * Saves a set of memory space ids, as a CSV string, into the 
   * preferences store
   */
  private void setMemorySpaceIds(String[] memorySpaces) {
    StringBuffer csv = new StringBuffer();
    for (int i = 0; i < memorySpaces.length; i++) {
      csv.append(memorySpaces[i]);
      if (i < memorySpaces.length - 1) {
        csv.append(",");
      }
    }
    
    fStore.setValue(TraditionalRenderingPreferenceConstants.MEM_KNOWN_MEMORY_SPACE_ID_LIST_CSV, 
        csv.toString());
  }
  
  /**
   * @return an array of keys, used to access preference in the store, one key per
   * known memory space.
   * Note: the keys are returned in the same order as the labels returned by
   * getMemorySpacesLabels()
   */
  public String[] getMemorySpaceKeys() {
    String prefix = TraditionalRenderingPreferenceConstants.MEM_MEMORY_SPACE_ID_PREFIX;
    String[] ids = getMemorySpaceIds();
    String[] keys = new String[ids.length];
    
    for (int i = 0; i < ids.length; i++) {
      keys[i] = prefix + ids[i];
    }
    return keys;
  }
  
  /**
   * @return the preference store key to lookup the default color for a 
   * given memory space id
   */
  public String getMemorySpaceKey(String id) {
    return TraditionalRenderingPreferenceConstants.MEM_MEMORY_SPACE_ID_PREFIX + id;
  }

  /**
   * @return an array of labels, corresponding to the preference entries, one for
   * each known memory space. 
   * Note: the labels are returned in the same order and the keys returned by
   * getMemorySpaceKeys().
   */
  public String[] getMemorySpacesLabels() {
    String labelPrefix = TraditionalRenderingMessages.getString(
        "TraditionalRenderingPreferencePage_BackgroundColorMemorySpacePrefix");
    String[] ids = getMemorySpaceIds();
    String[] labels = new String[ids.length];;

    for (int i = 0; i < ids.length; i++) {
      labels[i] = labelPrefix + " " + ids[i];
    }
    return labels;
  }
  
  /**
   * @return an array of string, each a csv representation of a RGB color, one for
   * each known memory space. 
   * Note: the colors are returned in the same order as the keys returned by
   * getMemorySpaceKeys().
   */
  public String[] getMemorySpaceDefaultColors() {
    String[] ids = getMemorySpaceIds();
    String[] colors = new String[ids.length];
    for (int i = 0; i < ids.length; i++) {
      colors[i] = getColor(i);
    }
    return colors;
  }
    
  /** Adds a preference for a memory space id and assign it a unique color */
  private void setDefaultColorPreference(String id, int index) {
    String prefix = TraditionalRenderingPreferenceConstants.MEM_MEMORY_SPACE_ID_PREFIX;
    String key = prefix + id;
    fStore.setValue(key, getColor(index));
    // Setting the default here prevents not having a default defined at first.
    fStore.setDefault(key, getColor(index));
  }

  /**
   * @return a csv string representation of a color. A color array is defined
   *  in this class. The entry returned corresponds to the index parameter, and
   *  wraps around if the index is greater than the number defined colors 
   */
  private String getColor(int index) {
    // wrap-around if we have exhausted the pool
    if (index >= colorPool.length) {
      index = index % (colorPool.length);
    }
    return colorPool[index];
  }
    
}
