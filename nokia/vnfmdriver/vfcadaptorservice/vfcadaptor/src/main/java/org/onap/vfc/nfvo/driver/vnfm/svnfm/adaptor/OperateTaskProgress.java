/*
 * Copyright 2016-2017, Nokia Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.vfc.nfvo.driver.vnfm.svnfm.adaptor;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.onap.vfc.nfvo.driver.vnfm.svnfm.nslcm.bo.entity.AffectedVnfc;

public class OperateTaskProgress {
	private static AtomicInteger instantiate_progress = new AtomicInteger(10);
	private static AtomicInteger terminate_progress = new AtomicInteger(20);
	
	private static Timer instantiateTimer;
	private static Timer terminateTimer;
	
	private static List<AffectedVnfc> affectedVnfc;
	
	public static int getInstantiateProgress() {
		return instantiate_progress.intValue();
	}
	
	public static int getTerminateProgress() {
		return terminate_progress.intValue();
	}
	
	public static void incrementInstantiateProgress() {
		instantiate_progress.incrementAndGet();
	}
	
	public static void incrementTerminateProgress() {
		terminate_progress.incrementAndGet();
	}
	
	public static void startInstantiateTimerTask() {
		instantiateTimer = new Timer();
		instantiate_progress.set(10);
		instantiateTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				if(instantiate_progress.intValue() < 96) {
					instantiate_progress.incrementAndGet();
				}
			}
			
		}, 1000, 60000);
	}
	
	public static void startTerminateTimerTask() {
		terminateTimer = new Timer();
		terminate_progress.set(20);
		terminateTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				if(terminate_progress.intValue() < 96) {
				    terminate_progress.incrementAndGet();
				}
			}
			
		}, 1000, 8000);
	} 
	
	public static void stopTerminateTimerTask() {
		terminateTimer.cancel();
		terminate_progress.set(100);
	}
	
	public static void stopInstantiateTimerTask() {
		instantiateTimer.cancel();
		instantiate_progress.set(100);
	}

	public static List<AffectedVnfc> getAffectedVnfc() {
		return affectedVnfc;
	}

	public static void setAffectedVnfc(List<AffectedVnfc> affectedVnfc) {
		OperateTaskProgress.affectedVnfc = affectedVnfc;
	}
}
