package com.self.sms.route;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.ESMClass;
import org.jsmpp.bean.GeneralDataCoding;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.RegisteredDelivery;
import org.jsmpp.bean.SMSCDeliveryReceipt;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.self.sms.model.SMSRequest;
import com.self.sms.model.SMSResponse;
import com.self.sms.service.impl.AutoReconnectGateway;
import com.self.sms.util.Constants;

/**
 * Routes the API to client server to trigger SMS/Email notification to user.
 */
@Component
public class SMSRoute {

	@Value("${notification.sms.opco}")
	private String opco;

	@Value("${notification.sms.is-isdn:false}")
	private boolean isISDN;

	@Value("${notification.smpp.smsc.country_code}")
	private String countryCode;

	@Autowired
	private AutoReconnectGateway autoReconnectGateway;

	private static Log log = LogFactory.getLog(SMSRoute.class);

	NumberingPlanIndicator numberingPlanIndicator;

	/**
	 * Invokes client SMS API to trigger SMS to given destination number and
	 * validates the response.
	 *
	 * @param smsRequest Object contains Source, Destination and Message details
	 * @return SMS request status will be returned
	 */
	public SMSResponse sendSMS(SMSRequest smsRequest) {
		SMSResponse smsResponse = new SMSResponse();
		String destinationNumber = smsRequest.getDestinationNumber();
		String message = smsRequest.getSmsBody();
		String msisdn = countryCode + destinationNumber;
		log.debug("Sending sms to : " + msisdn);
		final RegisteredDelivery registeredDelivery = new RegisteredDelivery();
		registeredDelivery.setSMSCDeliveryReceipt(SMSCDeliveryReceipt.SUCCESS_FAILURE);
		String source = autoReconnectGateway.getSource();

		// currently isISDN should be true only for MW
		if (isISDN) {
			numberingPlanIndicator = NumberingPlanIndicator.ISDN;
		} else {
			numberingPlanIndicator = NumberingPlanIndicator.UNKNOWN;
		}

		try {
			String messageId = autoReconnectGateway.getSession(countryCode).submitShortMessage("CMT",
					TypeOfNumber.ALPHANUMERIC, numberingPlanIndicator, source, TypeOfNumber.INTERNATIONAL,
					NumberingPlanIndicator.ISDN, msisdn, new ESMClass(), (byte) 0, (byte) 1, null, null,
					registeredDelivery, (byte) 0, new GeneralDataCoding(), (byte) 0, message.getBytes());
			log.debug("SMS sent successfully to msisdn : " + msisdn + " with messageId : " + messageId);
			smsResponse.setMessage(Constants.SUCCESS);
			smsResponse.setStatus(Constants.STATUS_CODE_200);
		} catch (IllegalArgumentException | PDUException | ResponseTimeoutException | InvalidResponseException
				| NegativeResponseException | IOException e) {
			log.error("Failed to send SMS to msisdn : " + msisdn + " : " + e.getMessage());
		}
		return smsResponse;
	}

}
