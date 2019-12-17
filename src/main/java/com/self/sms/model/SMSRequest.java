package com.self.sms.model;

import javax.validation.constraints.NotNull;

public class SMSRequest {

	@NotNull(message="Destination Number can't be blank!")
	private String destinationNumber;
	private String smsBody;

	public String getDestinationNumber() {
		return destinationNumber;
	}

	public void setDestinationNumber(String destinationNumber) {
		this.destinationNumber = destinationNumber;
	}

	public String getSmsBody() {
		return smsBody;
	}

	public void setSmsBody(String smsBody) {
		this.smsBody = smsBody;
	}

}
