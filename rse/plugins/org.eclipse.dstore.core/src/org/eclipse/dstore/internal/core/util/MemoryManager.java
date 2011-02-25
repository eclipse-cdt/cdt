/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight
 *
 * Contributors:
 * David McKnight  (IBM)  - [261644] [dstore] remote search improvements
 * David McKnight  (IBM)  - [277764] [dstore][regression] IllegalAccessException thrown when connecting to a running server
 * David McKnight   (IBM) - [283613] [dstore] Create a Constants File for all System Properties we support
 ********************************************************************************/

package org.eclipse.dstore.internal.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.server.SystemServiceManager;
import org.eclipse.dstore.internal.core.model.IDataStoreSystemProperties;

public class MemoryManager {
	private Object mbean;
	private static MemoryManager _instance;
	private DataStore _dataStore;

	private MemoryManager(DataStore dataStore) {
		init();
		_dataStore = dataStore;
	}

	public static MemoryManager getInstance(DataStore dataStore){
		if (_instance == null){
			_instance = new MemoryManager(dataStore);
		}
		return _instance;
	}

	private void init(){
		String thresholdString = System.getProperty(IDataStoreSystemProperties.SEARCH_THRESHOLD); 
		double threshold = 0.8;
		if(thresholdString != null && thresholdString.length() > 0) {
			threshold = Integer.parseInt(thresholdString) / 100.0;
		}

		// do we have java 1.5?
		try {
			Class factoryClass = Class.forName("java.lang.management.ManagementFactory"); //$NON-NLS-1$
			Method method = factoryClass.getDeclaredMethod("getMemoryPoolMXBeans", new Class[0]);  //$NON-NLS-1$

			List list = (List)method.invoke(null, null);

			for(int i = 0;i < list.size(); i++) {
				Object mbObj = list.get(i);
				Class mbClass = mbObj.getClass();

				
				Method getSupportedMethod = mbClass.getDeclaredMethod("isUsageThresholdSupported", new Class[0]);  //$NON-NLS-1$
				getSupportedMethod.setAccessible(true);
				Boolean usageThresholdSupported = (Boolean)getSupportedMethod.invoke(mbObj, null);
				if (usageThresholdSupported.booleanValue()){

					Method getTypeMethod = mbClass.getDeclaredMethod("getType", new Class[0]);  //$NON-NLS-1$

					getTypeMethod.setAccessible(true);
					Object typeObj = getTypeMethod.invoke(mbObj, null);
					Class memoryType = Class.forName("java.lang.management.MemoryType");  //$NON-NLS-1$
					Field field = memoryType.getField("HEAP");  //$NON-NLS-1$
					Object fieldObj = field.get(typeObj);

					if (fieldObj.equals(typeObj)){
						Method getUsageMethod = mbClass.getDeclaredMethod("getUsage", new Class[0]);  //$NON-NLS-1$
						getUsageMethod.setAccessible(true);
						Object usageObj = getUsageMethod.invoke(mbObj, null);

						Class usageClass = usageObj.getClass();
						Method getMaxMethod = usageClass.getDeclaredMethod("getMax", new Class[0]);  //$NON-NLS-1$
						getMaxMethod.setAccessible(true);
						Long maxObj = (Long)getMaxMethod.invoke(usageObj, null);

						Method setThresholdMethod = mbClass.getDeclaredMethod("setUsageThreshold", new Class[] { long.class });  //$NON-NLS-1$
						Object[] args = new Object[1];
						args[0] = new Long((long)(maxObj.longValue() * threshold));

						setThresholdMethod.setAccessible(true);
						setThresholdMethod.invoke(mbObj, args);
						mbean = mbObj;
						break;
					}
				}
			}
		}
		catch (Exception e){
			// java version to old so no mbean created - will use fallback
		}
	}

	public boolean isThresholdExceeded() {

		if (mbean != null){
			try {
				Method method = mbean.getClass().getMethod("isUsageThresholdExceeded", new Class[0]);  //$NON-NLS-1$
				Boolean exceeded = (Boolean)method.invoke(mbean, null);
				return exceeded.booleanValue();
			}
			catch (Exception e){
				return false;
			}
		}
		else {
			// no Java 1.5 available, so this is the fallback
			Runtime runtime = Runtime.getRuntime();
			long freeMem = runtime.freeMemory();

			if (freeMem < 10000){

				return true;
			}
			return false;
		}
	}

	public void checkAndClearupMemory()
	{
		int count = 0;
		while(count < 5 && isThresholdExceeded()) {
			System.gc();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
			count ++;
			_dataStore.trace("CLEAN free mem="+Runtime.getRuntime().freeMemory());  //$NON-NLS-1$
		}
		if(count == 5) {


			_dataStore.trace("Out of memory - shutting down"); //$NON-NLS-1$

			Exception e = new Exception();
			_dataStore.trace(e);

			/*
			 * show the end of the log
			DataElement logRoot = _dataStore.getLogRoot();
			List nestedData = logRoot.getNestedData();
			for (int i = nestedData.size() - 10; i < nestedData.size(); i++){
				DataElement cmd = (DataElement)nestedData.get(i);
				System.out.println(cmd);
			}
			*/

			if (SystemServiceManager.getInstance().getSystemService() == null)
				System.exit(-1);
		}
	}
}
