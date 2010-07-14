/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.tcmodification.extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.internal.core.IRealBuildObjectAssociation;
import org.eclipse.cdt.managedbuilder.internal.tcmodification.IObjectSet;
import org.eclipse.cdt.managedbuilder.internal.tcmodification.Messages;
import org.eclipse.cdt.managedbuilder.internal.tcmodification.ObjectSet;
import org.eclipse.cdt.managedbuilder.internal.tcmodification.ObjectSetList;
import org.eclipse.cdt.managedbuilder.internal.tcmodification.PerTypeMapStorage;
import org.eclipse.cdt.managedbuilder.internal.tcmodification.TcModificationUtil;
import org.eclipse.cdt.managedbuilder.internal.tcmodification.extension.MatchObjectElement.PatternElement;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;


public class RulesManager {
	private static RulesManager fInstance;
	private static final String EXTENSION_POINT_ID = ManagedBuilderCorePlugin.getUniqueIdentifier() + ".tcModificationInfo"; //$NON-NLS-1$
	
	private ConflictDefinition[] fConflictDefinitions;
	
	private Map fMatchObjectMap = new HashMap();
	private PerTypeMapStorage fObjToChildSuperClassMap;
	private StarterJob fStarter;
	private boolean fIsStartInited; 

	private class StarterJob extends Job {

		private StarterJob() {
			super(Messages.getString("RulesManager.1")); //$NON-NLS-1$
			setSystem(true);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				loadExtensions();
			} finally {
				fStarter = null;
			}
			return Status.OK_STATUS;
		}
		
	}

	private RulesManager(){
	}
	
	public static RulesManager getInstance(){
		if(fInstance == null)
			fInstance = getInstanceSynch();
		return fInstance;
	}

	public synchronized static RulesManager getInstanceSynch(){
		if(fInstance == null)
			fInstance = new RulesManager();
		return fInstance;
	}
	
	public void start(){
		if(fIsStartInited)
			return;

		synchronized (this) {
			if(fIsStartInited)
				return;
			
			fIsStartInited = true;
		}
		
		fStarter = new StarterJob();
		fStarter.schedule();
	}
	
	private void loadExtensions(){
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT_ID);
		if(extensionPoint == null){
			fConflictDefinitions = new ConflictDefinition[0];
		} else {
			IExtension[] extensions = extensionPoint.getExtensions();
			List conflictDefs = new ArrayList();
			for (int i = 0; i < extensions.length; ++i) {
				IExtension extension = extensions[i];
				IConfigurationElement[] elements = extension.getConfigurationElements();
				for(int k = 0; k < elements.length; k++){
					IConfigurationElement el = elements[k];
					String elName = el.getName();
					if(ConflictDefinitionElement.ELEMENT_NAME.equals(elName)){
						try {
							ConflictDefinitionElement cde = new ConflictDefinitionElement(el);
							ConflictDefinition cd = resolve(cde);
							if(cd != null)
								conflictDefs.add(cd);
						} catch (IllegalArgumentException e){
							ManagedBuilderCorePlugin.log(e);
						}
					}
				}
			}
			
			fConflictDefinitions = (ConflictDefinition[])conflictDefs.toArray(new ConflictDefinition[conflictDefs.size()]);
		}
	}
	
	private ConflictDefinition resolve(ConflictDefinitionElement el) throws IllegalArgumentException {
		MatchObjectElement mos[] = el.getMatchObjects();
		if(mos.length != 2)
			throw new IllegalArgumentException();
		
		IObjectSet oss[] = new IObjectSet[mos.length];
		
		for(int i = 0; i < mos.length; i++){
			oss[i] = resolve(mos[i]);
			if(oss[i].getNumObjects() == 0){
				//no conflicts
				return null;
			}
		}
		
		return new ConflictDefinition(new ObjectSetList(oss));
	}
	
	private IObjectSet resolve(MatchObjectElement el){
		IObjectSet oSet = (IObjectSet)fMatchObjectMap.get(el);
		if(oSet == null){
			int type = el.getObjectType();
			PatternElement[] patterns = el.getPatterns();
			HashSet objectsSet = new HashSet(); 
			for(int i = 0; i < patterns.length; i++){
				PatternElement pattern = patterns[i];
				processPattern(type, pattern, objectsSet);
			}
			oSet = new ObjectSet(type, objectsSet);
			fMatchObjectMap.put(el, oSet);
		}
		return oSet;
	}
	
	private IRealBuildObjectAssociation[] getObjectsForId(int objType, String id, int idType){
		if(idType == PatternElement.TYPE_ID_EXACT_MATCH){
			IRealBuildObjectAssociation obj = TcModificationUtil.getObjectById(objType, id);
			if(obj != null)
				return new IRealBuildObjectAssociation[]{obj};
			return new IRealBuildObjectAssociation[0];
		}
		
		IRealBuildObjectAssociation[] allObjs = TcModificationUtil.getExtensionObjects(objType);
		Pattern pattern = Pattern.compile(id);
		List list = new ArrayList();
		
		for(int i = 0; i < allObjs.length; i++){
			if(pattern.matcher(allObjs[i].getId()).matches())
				list.add(allObjs[i]);
		}
		
		return (IRealBuildObjectAssociation[])list.toArray(new IRealBuildObjectAssociation[list.size()]);
	}
	
	private Set processPattern(int objType, PatternElement el, Set set){
		if(set == null)
			set = new HashSet();
		
		String ids[] = el.getIds();
		if(el.getSearchType() == PatternElement.TYPE_SEARCH_EXTENSION_OBJECT){
			for(int i = 0; i < ids.length; i++){
				IRealBuildObjectAssociation objs[] = getObjectsForId(objType, ids[i], el.getIdType());
				for(int k = 0; k < objs.length; k++){
					set.add(objs[k].getRealBuildObject());
				}
			}
		} else if (el.getSearchType() == PatternElement.TYPE_SEARCH_ALL_EXTENSION_SUPERCLASSES){
			IRealBuildObjectAssociation[] allReal = TcModificationUtil.getRealObjects(objType);
			for(int i = 0; i < ids.length; i++){
				IRealBuildObjectAssociation []objs = getObjectsForId(objType, ids[i], el.getIdType());
				for(int k = 0; k < objs.length; k++){
					IRealBuildObjectAssociation obj = objs[k];
					
					set.add(obj.getRealBuildObject());

					Set childRealSet = getChildSuperClassRealSet(obj, allReal);
					
					set.addAll(childRealSet);
//					for(int k = 0; k < allReal.length; k++){
//						IRealBuildObjectAssociation otherReal = allReal[k];
//						if(otherReal == obj || set.contains(otherReal))
//							continue;
//						
//						if("tcm.derive.tc1".equals(otherReal.getId())){
//							int f = 0; f++;
//						}
//						IRealBuildObjectAssociation[] identics = otherReal.getIdenticBuildObjects();
//						for(int j = 0; j < identics.length; j++){
//							IRealBuildObjectAssociation identic = identics[j];
//							for(identic = identic.getSuperClassObject(); identic != null; identic = identic.getSuperClassObject()){
//								if(identic == obj){
//									set.add(identic.getRealBuildObject());
//								}
//							}
//						}
//					}
				}
			}
		}
		
		return set;
	}
	
	private Set getChildSuperClassRealSet(IRealBuildObjectAssociation obj, IRealBuildObjectAssociation[] all){
		if(fObjToChildSuperClassMap == null)
			fObjToChildSuperClassMap = new PerTypeMapStorage();

		if(all == null)
			all = TcModificationUtil.getExtensionObjects(obj.getType());

		Map map = fObjToChildSuperClassMap.getMap(obj.getType(), true);
		Set set = (Set)map.get(obj);
		if(set == null){
			set = createChildSuperClassRealSet(obj, all, null);
			map.put(obj, set);
		}
		
		return set;
	}
	
	private static Set createChildSuperClassRealSet(IRealBuildObjectAssociation obj, IRealBuildObjectAssociation[] all, Set set){
		if(set == null)
			set = new HashSet();
		
		if(all == null)
			all = TcModificationUtil.getExtensionObjects(obj.getType());
		
		for(int i = 0; i < all.length; i++){
			IRealBuildObjectAssociation cur = all[i];
			for(IRealBuildObjectAssociation el = cur.getSuperClassObject(); el != null; el = el.getSuperClassObject()){
				if(el == obj){
					IRealBuildObjectAssociation realQuickTest = null;
					for(IRealBuildObjectAssociation found = cur; found != obj; found = found.getSuperClassObject()){
						IRealBuildObjectAssociation real = found.getRealBuildObject();
						if(real != realQuickTest){
							set.add(real);
							realQuickTest = real;
						}
					}
				}
			}
		}
		
		return set;
	}
	
	public ObjectSetListBasedDefinition[] getRules(int ruleType){
		checkInitialization();
		return (ConflictDefinition[])fConflictDefinitions.clone();
	}
	
	private void checkInitialization(){
		if(!fIsStartInited)
			throw new IllegalStateException();
		
		StarterJob starter = fStarter; 
		
		if(starter != null){
			try {
				starter.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
