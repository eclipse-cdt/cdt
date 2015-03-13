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

package org.eclipse.cdt.dsf.gdb.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.PersistentSettingsManager;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.PersistentSettingsManager.PersistentListParameter;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.PersistentSettingsManager.PersistentParameter;
import org.junit.Test;

@SuppressWarnings("restriction")
public class PersistentSettingsManagerTest {

	// variables
	
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
	
	
	// constants
	private final static int NUM_LIST_ELEMENTS = 10;
	
	private final static String INSTANCE_ID_1 = "instance1";
	private final static String INSTANCE_ID_2 = "instance2";
	private final static String INSTANCE_ID_3 = "instance3";

	private final static String DEFAULT_STRING = "Default String";
	private final static Boolean DEFAULT_BOOLEAN = false;
	private final static Integer DEFAULT_INTEGER = 1234321;
	
	private final static String DEFAULT_STRING_INSTANCE1 = "Default String Instance 1";
	private final static String DEFAULT_STRING_INSTANCE2 = "Default String Instance 2";
	private final static String DEFAULT_STRING_INSTANCE3 = "Default String Instance 3";
	
	
	public PersistentSettingsManagerTest() {
		// 3 instances of managers - to simulate, for example, 3 views each having one manager
		m_persistentSettingsManager1 = new PersistentSettingsManager("PersistentSettingsManagerTest", INSTANCE_ID_1);
		m_persistentSettingsManager2 = new PersistentSettingsManager("PersistentSettingsManagerTest", INSTANCE_ID_2); 
		m_persistentSettingsManager3 = new PersistentSettingsManager("PersistentSettingsManagerTest", INSTANCE_ID_3); 
		
		// one persistent parameter for each supported type, 
		m_stringParam = m_persistentSettingsManager1.getNewParameter(String.class, "String Parameter", false, DEFAULT_STRING);
		m_booleanParam = m_persistentSettingsManager1.getNewParameter(Boolean.class, "Boolean Parameter", false, DEFAULT_BOOLEAN);
		m_integerParam = m_persistentSettingsManager1.getNewParameter(Integer.class, "Boolean Parameter", false, DEFAULT_INTEGER);
		m_listOfStringParam = m_persistentSettingsManager1.getNewListParameter(String.class, "List of String Parameter", false, new ArrayList<String>());
		m_listOfBooleanParam = m_persistentSettingsManager1.getNewListParameter(Boolean.class, "List of Boolean Parameter", false, new ArrayList<Boolean>());
		m_listOfIntegerParam = m_persistentSettingsManager1.getNewListParameter(Integer.class, "List of Integer Parameter", false, new ArrayList<Integer>());
		
		// simulate 3 instances using the same parameter, using "per instance" persistence (i.e. they'll be persisted independently)
		m_stringParamInstance1 = m_persistentSettingsManager1.getNewParameter(String.class, "Per-instance String Parameter", true, DEFAULT_STRING_INSTANCE1);
		m_stringParamInstance2 = m_persistentSettingsManager2.getNewParameter(String.class, "Per-instance String Parameter", true, DEFAULT_STRING_INSTANCE2);
		m_stringParamInstance3 = m_persistentSettingsManager3.getNewParameter(String.class, "Per-instance String Parameter", true, DEFAULT_STRING_INSTANCE3);
		
		// simulate 3 instances using the same parameter, using "global" persistence (i.e. they will share the persisted value
		m_stringGlobalParamInstance1 = m_persistentSettingsManager1.getNewParameter(String.class, "Global String Parameter", false, DEFAULT_STRING_INSTANCE1);
		m_stringGlobalParamInstance2 = m_persistentSettingsManager2.getNewParameter(String.class, "Global String Parameter", false, DEFAULT_STRING_INSTANCE1);
		m_stringGlobalParamInstance3 = m_persistentSettingsManager3.getNewParameter(String.class, "Global String Parameter", false, DEFAULT_STRING_INSTANCE1);
	}
	
	// testcases
	
	/** Test Un-supported base type - we expect an Exception */
	@Test(expected = Exception.class)
	public void testUnsupportedBaseType() throws Exception {
		PersistentParameter<Float> persistentFloat = m_persistentSettingsManager1.getNewParameter(Float.class, "Float Parameter", false, 1.0f);
	}
	
	/** Test Un-supported List type - we expect an Exception */
	@Test(expected = Exception.class)
	public void testUnsupportedListType() throws Exception {
		PersistentListParameter<Float> persistentFloatList = m_persistentSettingsManager1.getNewListParameter(Float.class, "Float Parameter", false, new ArrayList<Float>());
	}
	
	/** Test persisting one String value */
	@Test
	public void testPersistentParamString() throws Exception {
		// no value persisted yet - should return default 
		org.junit.Assert.assertEquals(DEFAULT_STRING, m_stringParam.value());
		
		// set a value
		String randomString = getRandomString();
		m_stringParam.set(randomString);
		// get cached value
		org.junit.Assert.assertEquals(randomString, m_stringParam.value());
		// force re-read from storage
		org.junit.Assert.assertEquals(randomString, m_stringParam.value(true));
		
		// set a different value
		randomString = getRandomString();
		m_stringParam.set(randomString);
		// get cached value
		org.junit.Assert.assertEquals(randomString, m_stringParam.value());
		// force re-read from storage
		org.junit.Assert.assertEquals(randomString, m_stringParam.value(true));
	}
	
	/** Test persisting one Boolean value */
	@Test
	public void testPersistentParamBoolean() throws Exception {
		// no value persisted yet - should return default 
		org.junit.Assert.assertEquals(DEFAULT_BOOLEAN, m_booleanParam.value());
		
		// set a value
		m_booleanParam.set(!DEFAULT_BOOLEAN);
		// get cached value
		org.junit.Assert.assertEquals(!DEFAULT_BOOLEAN, m_booleanParam.value());
		// force re-read from storage
		org.junit.Assert.assertEquals(!DEFAULT_BOOLEAN, m_booleanParam.value(true));
	}
	
	/** Test persisting one Integer value */
	@Test
	public void testPersistentParamInteger() throws Exception {
		// no value persisted yet - should return default 
		org.junit.Assert.assertEquals(DEFAULT_INTEGER, m_integerParam.value());

		// set a value
		Integer randomInt = getRandomInt();
		m_integerParam.set(randomInt);
		// get cached value
		org.junit.Assert.assertEquals(randomInt, m_integerParam.value());
		// force re-read from storage
		org.junit.Assert.assertEquals(randomInt, m_integerParam.value(true));

		// set a different value
		randomInt = getRandomInt();
		m_integerParam.set(randomInt);
		// get cached value
		org.junit.Assert.assertEquals(randomInt, m_integerParam.value());
		// force re-read from storage
		org.junit.Assert.assertEquals(randomInt, m_integerParam.value(true));
	}
	
	/** Test persisting a List of String */
	@Test
	public void testPersistentParamListOfString() throws Exception {
		// no value persisted yet - should return default (empty list)
		List<String> list = m_listOfStringParam.value();
		org.junit.Assert.assertEquals(0,list.size());
		// generate list of random elements
		for(int i = 0; i < NUM_LIST_ELEMENTS; i++) {
			list.add(getRandomString());
		}
		m_listOfStringParam.set(list);
		
		// get cached value
		List<String> list2 = m_listOfStringParam.value();
		for(int i = 0; i < NUM_LIST_ELEMENTS; i++) {
			org.junit.Assert.assertEquals(list.get(i), list2.get(i));
		}
		// force re-read from storage
		list2 = m_listOfStringParam.value(true);
		for(int i = 0; i < NUM_LIST_ELEMENTS; i++) {
			org.junit.Assert.assertEquals(list.get(i), list2.get(i));
		}
	}
	
	/** Test persisting a List of Boolean */
	@Test
	public void testPersistentParamListOfBoolean() throws Exception {
		// no value persisted yet - should return default (empty list)
		List<Boolean> list = m_listOfBooleanParam.value();
		org.junit.Assert.assertEquals(0,list.size());
		// generate list of random elements
		for(int i = 0; i < NUM_LIST_ELEMENTS; i++) {
			list.add(getRandomBoolean());
		}
		m_listOfBooleanParam.set(list);
		
		// get cached value
		List<Boolean> list2 = m_listOfBooleanParam.value();
		for(int i = 0; i < NUM_LIST_ELEMENTS; i++) {
			org.junit.Assert.assertEquals(list.get(i), list2.get(i));
		}
		// force re-read from storage
		list2 = m_listOfBooleanParam.value(true);
		for(int i = 0; i < NUM_LIST_ELEMENTS; i++) {
			org.junit.Assert.assertEquals(list.get(i), list2.get(i));
		}
	}
	
	/** Test persisting a List of Integer */
	@Test
	public void testPersistentParamListofInteger() throws Exception {
		// no value persisted yet - should return default (empty list)
		List<Integer> list = m_listOfIntegerParam.value();
		org.junit.Assert.assertEquals(0,list.size());
		// generate list of random elements
		for(int i = 0; i < NUM_LIST_ELEMENTS; i++) {
			list.add(getRandomInt());
		}
		m_listOfIntegerParam.set(list);
		
		List<Integer> list2;
		// get cached value
		list2 = m_listOfIntegerParam.value();
		for(int i = 0; i < NUM_LIST_ELEMENTS; i++) {
			org.junit.Assert.assertEquals(list.get(i), list2.get(i));
		}
		// force re-read from storage
		list2 = m_listOfIntegerParam.value(true);
		for(int i = 0; i < NUM_LIST_ELEMENTS; i++) {
			org.junit.Assert.assertEquals(list.get(i), list2.get(i));
		}
	}
	
	/** This test simulates 3 different instances (e.g. views) writing and reading the same persistent
	 * parameter. In this case the parameter is defined as "per-instance" (vs "global"), so each 
	 * instance has it's own independent copy of the persistent parameter. */
	@Test
	public void testMultipleInstances() throws Exception {
		// no values persisted yet - should return defaults 
		org.junit.Assert.assertEquals(DEFAULT_STRING_INSTANCE1, m_stringParamInstance1.value());
		org.junit.Assert.assertEquals(DEFAULT_STRING_INSTANCE2, m_stringParamInstance2.value());
		org.junit.Assert.assertEquals(DEFAULT_STRING_INSTANCE3, m_stringParamInstance3.value());
		
		// set values - since the parameters were defined to save values per-instance, they should be 
		// persisted independently (i.e. not overwrite each other)
		String randomString1 = getRandomString();
		String randomString2 = getRandomString();
		String randomString3 = getRandomString();
		m_stringParamInstance1.set(randomString1);
		m_stringParamInstance2.set(randomString2);
		m_stringParamInstance3.set(randomString3);
		org.junit.Assert.assertEquals(randomString1, m_stringParamInstance1.value(true));
		org.junit.Assert.assertEquals(randomString2, m_stringParamInstance2.value(true));
		org.junit.Assert.assertEquals(randomString3, m_stringParamInstance3.value(true));
		
		// set different values
		randomString1 = getRandomString();
		randomString2 = getRandomString();
		randomString3 = getRandomString();
		m_stringParamInstance1.set(randomString1);
		m_stringParamInstance2.set(randomString2);
		m_stringParamInstance3.set(randomString3);
		org.junit.Assert.assertEquals(randomString1, m_stringParamInstance1.value(true));
		org.junit.Assert.assertEquals(randomString2, m_stringParamInstance2.value(true));
		org.junit.Assert.assertEquals(randomString3, m_stringParamInstance3.value(true));
	}
	
	
	/** This test simulates 3 different instances (e.g. views) writing and reading the same persistent
	 * parameter. In this case the parameter is defined as "global" (vs "per-instance"), so only one
	 * copy is shared between the instances. */
	@Test
	public void testGlobalParamsWithMultipleInstances() throws Exception {
		// no values persisted yet - should return defaults 
		org.junit.Assert.assertEquals(DEFAULT_STRING_INSTANCE1, m_stringGlobalParamInstance1.value());
		org.junit.Assert.assertEquals(DEFAULT_STRING_INSTANCE1, m_stringGlobalParamInstance2.value());
		org.junit.Assert.assertEquals(DEFAULT_STRING_INSTANCE1, m_stringGlobalParamInstance3.value());
		
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
		org.junit.Assert.assertEquals(randomString3, m_stringGlobalParamInstance1.value(true));
		org.junit.Assert.assertEquals(randomString3, m_stringGlobalParamInstance2.value(true));
		org.junit.Assert.assertEquals(randomString3, m_stringGlobalParamInstance3.value(true));
		
		// set different values
		randomString1 = getRandomString();
		randomString2 = getRandomString();
		randomString3 = getRandomString();
		m_stringGlobalParamInstance1.set(randomString1);
		m_stringGlobalParamInstance2.set(randomString2);
		m_stringGlobalParamInstance3.set(randomString3);
		// since the parameters are global, they share the same storage... So the last value written
		// will be persisted
		org.junit.Assert.assertEquals(randomString3, m_stringGlobalParamInstance1.value(true));
		org.junit.Assert.assertEquals(randomString3, m_stringGlobalParamInstance2.value(true));
		org.junit.Assert.assertEquals(randomString3, m_stringGlobalParamInstance3.value(true));
	}
	
	// utility methods
	
	private int getRandomInt() {
		return m_random.nextInt();
	}
	
	private String getRandomString() {
		return Integer.toString(getRandomInt(), 16) + Integer.toString(getRandomInt(), 16);
	}
	
	private boolean getRandomBoolean() {
		if (getRandomInt() % 2 == 0) {
			return true;
		}
		else {
			return false;
		}
	}
	
}
