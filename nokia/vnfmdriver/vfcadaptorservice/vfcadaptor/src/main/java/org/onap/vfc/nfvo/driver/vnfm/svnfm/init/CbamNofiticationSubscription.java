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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.init;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMCreateSubscriptionRequest;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.CBAMCreateSubscriptionResponse;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.bo.entity.Subscription;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.cbam.inf.CbamMgmrInf;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.common.bo.AdaptorEnv;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.bean.VnfmSubscriptionInfo;
import org.onap.vfc.nfvo.driver.vnfm.svnfm.db.mapper.VnfmSubscriptionsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

//@Order(3)
//@Component
public class CbamNofiticationSubscription implements ApplicationRunner {
	private static final Logger logger = LoggerFactory.getLogger(CbamNofiticationSubscription.class);

	@Autowired
	AdaptorEnv adaptorEnv;
	
	@Autowired
	private CbamMgmrInf cbamMgmr;
	
	@Autowired
	private VnfmSubscriptionsMapper subscriptionsMapper;
	
	@Override
	public void run(ApplicationArguments args){
		boolean subscribed = false;
		List<VnfmSubscriptionInfo> allSubscripions = subscriptionsMapper.getAll();
		if(allSubscripions != null && !allSubscripions.isEmpty())
		{
			for(VnfmSubscriptionInfo subscriptionInfo : allSubscripions)
			{
				try {
					Subscription subscription = cbamMgmr.getSubscription(subscriptionInfo.getId());
					if(subscription != null)
					{
						subscribed = true;
						logger.info("CBAM Notification has already been subscribed with id = " + subscriptionInfo.getId());
					}
					else
					{
						subscriptionsMapper.delete(subscriptionInfo.getId());
					}
				} catch (Exception e) {
					logger.error("Query or delete subscription error.", e);
				}
			}
		}
		
		if(!subscribed)
		{
			CBAMCreateSubscriptionRequest subscriptionRequest = new CBAMCreateSubscriptionRequest();
			subscriptionRequest.setCallbackUrl(adaptorEnv.getDriverApiUriFront() + "/api/nokiavnfmdriver/v1/notifications");
			try {
				CBAMCreateSubscriptionResponse createSubscription = cbamMgmr.createSubscription(subscriptionRequest);
				if(createSubscription != null)
				{
					subscriptionsMapper.insert(createSubscription.getId());
					logger.info("CBAM Notification is successfully subscribed with id = " + createSubscription.getId());
				}
			} catch (Exception e) {
				logger.error("Subscribe notification error.", e);
			}
		}
	}
}
