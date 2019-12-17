package com.self.sms.service;

import com.self.sms.model.SMSRequest;
import com.self.sms.model.SMSResponse;

public interface SMSService {

	public SMSResponse sendSMS(SMSRequest request);
}