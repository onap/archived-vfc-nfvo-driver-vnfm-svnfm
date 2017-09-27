/*
* Copyright 2016-2017 Nokia Corporation
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
package org.onap.vfc.nfvo.driver.vnfm.svnfm.constant;

public class CommonEnum {

	// Termination
	public enum TerminationType {
		GRACEFUL, FORCEFUL
	}

	// Instantiation

	public enum InstantiationState {
		NOT_INSTANTIATED, INSTANTIATED
	}
	// operation status

	public enum OperationStatus {
		STARTED, FINISHED, FAILED, OTHER
	}

	// scale
	public enum ScaleDirection {
		OUT, IN
	}

	public enum ScaleType {
		SCALE_OUT, SCALE_IN
	}

	public enum Deletionpending {
		TRUR, fALSE
	}

	// GrantVnf
	public enum LifecycleOperation {
		Instantiate, Scaleout, Scalein, Scaleup, Scaledown, Terminal
	}

	public enum type {
		VDU, VL, CP, Strorage
	}

	// NotifyLcmEvents
	public enum status {
		start, result
	}

	public enum changeType {
		vl_added, vl_removed, vl_modified

	}

	public enum resourceType

	{
		vm, volume, network, port
	}
	
	public enum AffectchangeType{
		added,removed, modified
	}

}
