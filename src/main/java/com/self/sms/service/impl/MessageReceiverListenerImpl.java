package com.self.sms.service.impl;

import org.jsmpp.bean.*;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.session.DataSmResult;
import org.jsmpp.session.MessageReceiverListener;
import org.jsmpp.util.InvalidDeliveryReceiptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MessageReceiverListenerImpl implements MessageReceiverListener {

    private static final Logger logger = LoggerFactory.getLogger(MessageReceiverListenerImpl.class);

    public void onAcceptDeliverSm(DeliverSm deliverSm) throws ProcessRequestException {
        if (MessageType.SMSC_DEL_RECEIPT.containedIn(deliverSm.getEsmClass())) {
            // delivery receipt
            try {
                DeliveryReceipt delReceipt = deliverSm.getShortMessageAsDeliveryReceipt();
                //long id = Long.parseLong(delReceipt.getId()) & 0xffffffff;
                //String messageId = Long.toString(id, 16).toUpperCase();
                //logger.info("Received '" + messageId + "' : " + delReceipt);
            } catch (InvalidDeliveryReceiptException e) {
                logger.info("Receiving faild." + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Regular short message
            logger.info("Receiving message : " + new String(deliverSm.getShortMessage()));
        }
    }

    public void onAcceptAlertNotification(AlertNotification alertNotification) {
        logger.info("onAcceptAlertNotification");
    }

    public DataSmResult onAcceptDataSm(DataSm dataSm, org.jsmpp.session.Session arg1) throws ProcessRequestException {
        logger.info("onAcceptDataSm");
        return null;
    }
}

