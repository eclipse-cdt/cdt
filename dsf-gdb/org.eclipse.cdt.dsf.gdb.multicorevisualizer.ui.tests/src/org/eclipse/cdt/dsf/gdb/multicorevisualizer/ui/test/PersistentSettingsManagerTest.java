/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.ui.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.PersistentSettingsManager;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.PersistentSettingsManager.PersistentListParameter;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.PersistentSettingsManager.PersistentMapParameter;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.PersistentSettingsManager.PersistentParameter;
import org.junit.Test;

public class PersistentSettingsManagerTest {

	// ---- variables ----
	
	/** persistent settings manager */
	private PersistentSettingsManager m_persistentSettingsManager1;
	private PersistentSettingsManager m_persistentSettingsManager2;
	private PersistentSettingsManager m_persistentSettingsManager3;
	
	// set of persistent parameters
	private PersistentParameter<String> m_stringParam;
	private PersistentParameter<Boolean> m_booleanParam;
	private PersistentParameter<Integer> m_integerParam;
	private PersistentListParameter<String> m_listOfStringParam;
	private PersistentListParameter<Boolean> m_listOfBooleanParam;
	private PersistentListParameter<Integer> m_listOfIntegerParam;
	private PersistentMapParameter<String> m_mapOfStringParam;
	private PersistentMapParameter<Boolean> m_mapOfBooleanParam;
	private PersistentMapParameter<Integer> m_mapOfIntegerParam;
	
	// set of per-instance parameters
	private PersistentParameter<String> m_stringParamInstance1;
	private PersistentParameter<String> m_stringParamInstance2;
	private PersistentParameter<String> m_stringParamInstance3;
	
	// set of global parameters
	private PersistentParameter<String> m_stringGlobalParamInstance1;
	private PersistentParameter<String> m_stringGlobalParamInstance2;
	private PersistentParameter<String> m_stringGlobalParamInstance3;
	
	// random number generator
	private Random m_random = new Random();
	
	
	// ---- constants ----
	private final static int NUM_LIST_ELEMENTS = 10;
	
	private final static String INSTANCE_ID_1 = "instance1";
	private final static String INSTANCE_ID_2 = "instance2";
	private final static String INSTANCE_ID_3 = "instance3";

	private final static String DEFAULT_STRING = "Default String";
	private final static Boolean DEFAULT_BOOLEAN = false;
	private final static Integer DEFAULT_INTEGER = 1234321;
	
	private final static String DEFAULT_STRING_VAL_INSTANCE1 = "Default String Instance 1";
	private final static String DEFAULT_STRING_VAL_INSTANCE2 = "Default String Instance 2";
	private final static String DEFAULT_STRING_VAL_INSTANCE3 = "Default String Instance 3";
	private final static String DEFAULT_STRING_VAL_SHARED = "Default String Shared Instance";
	
	
	public PersistentSettingsManagerTest() {
		// 3 instances of managers - to simulate, for example, 3 views each having one manager
		m_persistentSettingsManager1 = new PersistentSettingsManager("PersistentSettingsManagerTest", INSTANCE_ID_1);
		m_persistentSettingsManager2 = new PersistentSettingsManager("PersistentSettingsManagerTest", INSTANCE_ID_2); 
		m_persistentSettingsManager3 = new PersistentSettingsManager("PersistentSettingsManagerTest", INSTANCE_ID_3); 
		
		// one persistent parameter for each supported type:
		// simple types
		m_stringParam = m_persistentSettingsManager1.getNewParameter(String.class, "String Parameter", false, DEFAULT_STRING);
		m_booleanParam = m_persistentSettingsManager1.getNewParameter(Boolean.class, "Boolean Parameter", false, DEFAULT_BOOLEAN);
		m_integerParam = m_persistentSettingsManager1.getNewParameter(Integer.class, "Integer Parameter", false, DEFAULT_INTEGER);
		// List<T>
		m_listOfStringParam = m_persistentSettingsManager1.getNewListParameter(String.class, "List of String Parameter", false, new ArrayList<String>());
		m_listOfBooleanParam = m_persistentSettingsManager1.getNewListParameter(Boolean.class, "List of Boolean Parameter", false, new ArrayList<Boolean>());
		m_listOfIntegerParam = m_persistentSettingsManager1.getNewListParameter(Integer.class, "List of Integer Parameter", false, new ArrayList<Integer>());
		// Map<String,T>
		m_mapOfStringParam =  m_persistentSettingsManager1.getNewMapParameter(String.class, "Map of String Parameter", true, new HashMap<String,String>());
		m_mapOfBooleanParam =  m_persistentSettingsManager1.getNewMapParameter(Boolean.class, "Map of Boolean Parameter", true, new HashMap<String,Boolean>());
		m_mapOfIntegerParam =  m_persistentSettingsManager1.getNewMapParameter(Integer.class, "Map of Integer Parameter", true, new HashMap<String,Integer>());
		
		// simulate 3 instances using the same parameter, using "per instance" persistence (i.e. they'll be persisted independently)
		m_stringParamInstance1 = m_persistentSettingsManager1.getNewParameter(String.class, "Per-instance String Parameter", true, DEFAULT_STRING_VAL_INSTANCE1);
		m_stringParamInstance2 = m_persistentSettingsManager2.getNewParameter(String.class, "Per-instance String Parameter", true, DEFAULT_STRING_VAL_INSTANCE2);
		m_stringParamInstance3 = m_persistentSettingsManager3.getNewParameter(String.class, "Per-instance String Parameter", true, DEFAULT_STRING_VAL_INSTANCE3);
		
		// This is to simulate a persistent parameter, being "shared" by 3 instances (e.g. views). So, the 3 instances are persisted as a single parameter. 
		m_stringGlobalParamInstance1 = m_persistentSettingsManager1.getNewParameter(String.class, "Global String Parameter", false, DEFAULT_STRING_VAL_SHARED);
		m_stringGlobalParamInstance2 = m_persistentSettingsManager2.getNewParameter(String.class, "Global String Parameter", false, DEFAULT_STRING_VAL_SHARED);
		m_stringGlobalParamInstance3 = m_persistentSettingsManager3.getNewParameter(String.class, "Global String Parameter", false, DEFAULT_STRING_VAL_SHARED);
	}
	
	// testcases
	
	/** Test Un-supported base type - we expect an Exception */
	@Test(expected = Exception.class)
	public void testUnsupportedBaseType() throws Exception {
		m_persistentSettingsManager1.getNewParameter(Float.class, "Float Parameter", false, 1.0f);
	}
	
	/** Test Un-supported List type - we expect an Exception */
	@Test(expected = Exception.class)
	public void testUnsupportedListType() throws Exception {
		m_persistentSettingsManager1.getNewListParameter(Float.class, "List of Float Parameter", false, new ArrayList<Float>());
	}
	
	/** Test Un-supported Map type - we expect an Exception */
	@Test(expected = Exception.class)
	public void testUnsupportedMapType() throws Exception {
		m_persistentSettingsManager1.getNewMapParameter(Float.class, "Map of Float Parameter", false, new HashMap<String,Float>());
	}
	
	/** Test persisting one String value */
	@Test
	public void testPersistentParamString() throws Exception {
		// no value persisted yet - should return default 
		assertEquals(DEFAULT_STRING, m_stringParam.value());
		
		// set a value
		String randomString = getRandomString();
		m_stringParam.set(randomString);
		// get cached value
		assertEquals(randomString, m_stringParam.value());
		// force re-read from storage
		assertEquals(randomString, m_stringParam.value(true));
		
		// set a different value
		randomString = getRandomString();
		m_stringParam.set(randomString);
		// get cached value
		assertEquals(randomString, m_stringParam.value());
		// force re-read from storage
		assertEquals(randomString, m_stringParam.value(true));
	}
	
	/** Test persisting one Boolean value */
	@Test
	public void testPersistentParamBoolean() throws Exception {
		// no value persisted yet - should return default 
		assertEquals(DEFAULT_BOOLEAN, m_booleanParam.value());
		
		// set a value
		m_booleanParam.set(!DEFAULT_BOOLEAN);
		// get cached value
		assertEquals(!DEFAULT_BOOLEAN, m_booleanParam.value());
		// force re-read from storage
		assertEquals(!DEFAULT_BOOLEAN, m_booleanParam.value(true));
	}
	
	/** Test persisting one Integer value */
	@Test
	public void testPersistentParamInteger() throws Exception {
		// no value persisted yet - should return default 
		assertEquals(DEFAULT_INTEGER, m_integerParam.value());

		// set a value
		Integer randomInt = getRandomInt();
		m_integerParam.set(randomInt);
		// get cached value
		assertEquals(randomInt, m_integerParam.value());
		// force re-read from storage
		assertEquals(randomInt, m_integerParam.value(true));

		// set a different value
		randomInt = getRandomInt();
		m_integerParam.set(randomInt);
		// get cached value
		assertEquals(randomInt, m_integerParam.value());
		// force re-read from storage
		assertEquals(randomInt, m_integerParam.value(true));
	}
	
	/** Test persisting a List of String */
	@Test
	public void testPersistentParamListOfString() throws Exception {
		// no value persisted yet - should return default (empty list)
		List<String> list = m_listOfStringParam.value();
		assertEquals(0,list.size());
		// generate list of random elements
		for(int i = 0; i < NUM_LIST_ELEMENTS; i++) {
			list.add(getRandomString());
		}
		m_listOfStringParam.set(list);
		
		// get cached value
		List<String> list2 = m_listOfStringParam.value();
		for(int i = 0; i < NUM_LIST_ELEMENTS; i++) {
			assertEquals(list.get(i), list2.get(i));
		}
		// force re-read from storage
		list2 = m_listOfStringParam.value(true);
		for(int i = 0; i < NUM_LIST_ELEMENTS; i++) {
			assertEquals(list.get(i), list2.get(i));
		}
	}
	
	/** Test persisting a List of Boolean */
	@Test
	public void testPersistentParamListOfBoolean() throws Exception {
		// no value persisted yet - should return default (empty list)
		List<Boolean> list = m_listOfBooleanParam.value();
		assertEquals(0,list.size());
		// generate list of random elements
		for(int i = 0; i < NUM_LIST_ELEMENTS; i++) {
			list.add(getRandomBoolean());
		}
		m_listOfBooleanParam.set(list);
		
		// get cached value
		List<Boolean> list2 = m_listOfBooleanParam.value();
		for(int i = 0; i < NUM_LIST_ELEMENTS; i++) {
			assertEquals(list.get(i), list2.get(i));
		}
		// force re-read from storage
		list2 = m_listOfBooleanParam.value(true);
		for(int i = 0; i < NUM_LIST_ELEMENTS; i++) {
			assertEquals(list.get(i), list2.get(i));
		}
	}
	
	/** Test persisting a List of Integer */
	@Test
	public void testPersistentParamListofInteger() throws Exception {
		// no value persisted yet - should return default (empty list)
		List<Integer> list = m_listOfIntegerParam.value();
		assertEquals(0,list.size());
		// generate list of random elements
		for(int i = 0; i < NUM_LIST_ELEMENTS; i++) {
			list.add(getRandomInt());
		}
		m_listOfIntegerParam.set(list);
		
		List<Integer> list2;
		// get cached value
		list2 = m_listOfIntegerParam.value();
		for(int i = 0; i < NUM_LIST_ELEMENTS; i++) {
			assertEquals(list.get(i), list2.get(i));
		}
		// force re-read from storage
		list2 = m_listOfIntegerParam.value(true);
		for(int i = 0; i < NUM_LIST_ELEMENTS; i++) {
			assertEquals(list.get(i), list2.get(i));
		}
	}
	
	
	/** Test persisting a Map of String */
	@Test
	public void testPersistentParamMapOfString() throws Exception {
		// no value persisted yet - should return default (empty Map)
		Map<String,String> map = m_mapOfStringParam.value();
		assertEquals(0,map.size());
		
		// generate random elements
		for(int i = 0; i < NUM_LIST_ELEMENTS; i++) {
			map.put(getRandomString(), getRandomString());
		}
		m_mapOfStringParam.set(map);
		
		// get cached value
		Map<String,String> map2 = m_mapOfStringParam.value();
		assertEquals(map.size(), map2.size());
		for(String key : map2.keySet()) {
			assertEquals(map2.get(key), map.get(key));
		}
		
		// force re-read from storage
		map2 = m_mapOfStringParam.value(true);
		assertEquals(map.size(), map2.size());
		for(String key : map2.keySet()) {
			assertEquals(map2.get(key), map.get(key));
		}
	}
	
	/** Test persisting a Map of Boolean*/
	@Test
	public void testPersistentParamMapOfBoolean() throws Exception {
		// no value persisted yet - should return default (empty Map)
		Map<String,Boolean> map = m_mapOfBooleanParam.value();
		assertEquals(0,map.size());
		
		// generate random elements
		for(int i = 0; i < NUM_LIST_ELEMENTS; i++) {
			map.put(getRandomString(), getRandomBoolean());
		}
		m_mapOfBooleanParam.set(map);
		
		// get cached value
		Map<String,Boolean> map2 = m_mapOfBooleanParam.value();
		assertEquals(map.size(), map2.size());
		for(String key : map2.keySet()) {
			assertEquals(map2.get(key), map.get(key));
		}
		
		// force re-read from storage
		map2 = m_mapOfBooleanParam.value(true);
		assertEquals(map.size(), map2.size());
		for(String key : map2.keySet()) {
			assertEquals(map2.get(key), map.get(key));
		}
	}
	
	/** Test persisting a List of Integer */
	@Test
	public void testPersistentParamMapOfInteger() throws Exception {
		// no value persisted yet - should return default (empty Map)
		Map<String,Integer> map = m_mapOfIntegerParam.value();
		assertEquals(0,map.size());
		
		// generate random elements
		for(int i = 0; i < NUM_LIST_ELEMENTS; i++) {
			map.put(getRandomString(), getRandomInt());
		}
		m_mapOfIntegerParam.set(map);
		
		// get cached value
		Map<String,Integer> map2 = m_mapOfIntegerParam.value();
		assertEquals(map.size(), map2.size());
		for(String key : map2.keySet()) {
			assertEquals(map2.get(key), map.get(key));
		}
		
		// force re-read from storage
		map2 = m_mapOfIntegerParam.value(true);
		assertEquals(map.size(), map2.size());
		for(String key : map2.keySet()) {
			assertEquals(map2.get(key), map.get(key));
		}
	}
	
	/** This test simulates 3 different instances (e.g. views) writing and reading the same persistent
	 * parameter. In this case the parameter is defined as "per-instance" (vs "global"), so each 
	 * instance has it's own independent copy of the persistent parameter. */
	@Test
	public void testMultipleInstances() throws Exception {
		// no values persisted yet - should return defaults 
		assertEquals(DEFAULT_STRING_VAL_INSTANCE1, m_stringParamInstance1.value());
		assertEquals(DEFAULT_STRING_VAL_INSTANCE2, m_stringParamInstance2.value());
		assertEquals(DEFAULT_STRING_VAL_INSTANCE3, m_stringParamInstance3.value());
		
		// set values - since the parameters were defined to save values per-instance, they should be 
		// persisted independently (i.e. not overwrite each other)
		String randomString1 = getRandomString();
		String randomString2 = getRandomString();
		String randomString3 = getRandomString();
		m_stringParamInstance1.set(randomString1);
		m_stringParamInstance2.set(randomString2);
		m_stringParamInstance3.set(randomString3);
		assertEquals(randomString1, m_stringParamInstance1.value(true));
		assertEquals(randomString2, m_stringParamInstance2.value(true));
		assertEquals(randomString3, m_stringParamInstance3.value(true));
		
		// set different values
		randomString1 = getRandomString();
		randomString2 = getRandomString();
		randomString3 = getRandomString();
		m_stringParamInstance1.set(randomString1);
		m_stringParamInstance2.set(randomString2);
		m_stringParamInstance3.set(randomString3);
		assertEquals(randomString1, m_stringParamInstance1.value(true));
		assertEquals(randomString2, m_stringParamInstance2.value(true));
		assertEquals(randomString3, m_stringParamInstance3.value(true));
	}
	
	
	/** This test simulates 3 different instances (e.g. views) writing and reading the same persistent
	 * parameter. In this case the parameter is defined as "global" (vs "per-instance"), so only one
	 * copy is shared between the instances. */
	@Test
	public void testGlobalParamsWithMultipleInstances() throws Exception {
		// no values persisted yet - should return defaults 
		assertEquals(DEFAULT_STRING_VAL_SHARED, m_stringGlobalParamInstance1.value());
		assertEquals(DEFAULT_STRING_VAL_SHARED, m_stringGlobalParamInstance2.value());
		assertEquals(DEFAULT_STRING_VAL_SHARED, m_stringGlobalParamInstance3.value());
		
		// set values - since the parameters were defined to save values per-instance, they should be 
		// persisted independently
		String randomString1 = getRandomString();
		String randomString2 = getRandomString();
		String randomString3 = getRandomString();
		m_stringGlobalParamInstance1.set(randomString1);
		m_stringGlobalParamInstance2.set(randomString2);
		m_stringGlobalParamInstance3.set(randomString3);
		// since the parameters are global, they share the same storage... So the last value written
		// will be persisted
		assertEquals(randomString3, m_stringGlobalParamInstance1.value(true));
		assertEquals(randomString3, m_stringGlobalParamInstance2.value(true));
		assertEquals(randomString3, m_stringGlobalParamInstance3.value(true));
		
		// set different values
		randomString1 = getRandomString();
		randomString2 = getRandomString();
		randomString3 = getRandomString();
		m_stringGlobalParamInstance1.set(randomString1);
		m_stringGlobalParamInstance2.set(randomString2);
		m_stringGlobalParamInstance3.set(randomString3);
		// since the parameters are global, they share the same storage... So the last value written
		// will be persisted
		assertEquals(randomString3, m_stringGlobalParamInstance1.value(true));
		assertEquals(randomString3, m_stringGlobalParamInstance2.value(true));
		assertEquals(randomString3, m_stringGlobalParamInstance3.value(true));
	}
	
	// utility methods
	
	private int getRandomInt() {
		return m_random.nextInt();
	}
	
	private String getRandomString() {
		return Integer.toString(getRandomInt(), 16) + Integer.toString(getRandomInt(), 16);
	}
	
	private boolean getRandomBoolean() {
		return getRandomInt() % 2 == 0;
	}

}

