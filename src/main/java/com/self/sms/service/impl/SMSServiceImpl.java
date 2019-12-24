package com.self.sms.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.self.sms.model.SMSRequest;
import com.self.sms.model.SMSResponse;
import com.self.sms.route.SMSRoute;
import com.self.sms.service.SMSService;

@Service
public class SMSServiceImpl implements SMSService {
	
	@Autowired
	private SMSRoute route;

	@Override
	public SMSResponse sendSMS(SMSRequest request) {
		return route.sendSMS(request);
	}

}
