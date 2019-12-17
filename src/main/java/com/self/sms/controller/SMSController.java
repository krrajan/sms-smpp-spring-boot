package com.self.sms.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.self.sms.model.SMSRequest;
import com.self.sms.model.SMSResponse;
import com.self.sms.service.SMSService;
import com.self.sms.util.Constants;

@RestController
@RequestMapping(value = Constants.SERVICE_BASE_URL)
public class SMSController {

	@Autowired
	private SMSService smsService;

	@PostMapping(value = Constants.SEND_SMS_URL)
	public SMSResponse sendSMS(@Valid @RequestBody SMSRequest request) {
		return smsService.sendSMS(request);
	}
}
