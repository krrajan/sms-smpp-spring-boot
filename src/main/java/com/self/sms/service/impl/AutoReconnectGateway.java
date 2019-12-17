package com.self.sms.service.impl;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.extra.SessionState;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.session.Session;
import org.jsmpp.session.SessionStateListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.self.sms.util.Constants;

/**
 * This are implementation of Gateway. This gateway will reconnect for a
 * specified interval if the session are closed.
 *
 * @author Narotam
 */
@Component
// @Qualifier("smsGateway")
// @PropertySource({ "classpath:smpp.properties" })
public class AutoReconnectGateway implements Constants {

    private static Log logger = LogFactory.getLog(AutoReconnectGateway.class);

    @Value("${notification.smpp.smsc.host}")
    private String SMSC_HOST;
    @Value("${notification.smpp.smsc.port}")
    private String SMSC_PORT;
    @Value("${notification.smpp.smsc.username}")
    private String SMSC_USER_NAME;
    @Value("${notification.smpp.smsc.password}")
    private String SMSC_PWD;
    @Value("${notification.smpp.smsc.source}")
    private String SOURCE;
    @Value("${notification.smpp.smsc.system.type}")
    private String SYSTEM_TYPE;
    @Value("${notification.smpp.smsc.country_code}")
    private String ISD_CODE;

    private SMPPSession session = null;
    private static BindParameter bindParam;
    private static long reconnectInterval = 5000L; // 5 seconds
    private String countryCode;

    public String printProperties() {
        return "AutoReconnectGateway [" + "session=" + session + ", remoteIpAddress=" + SMSC_HOST
                + ", remotePort=" + SMSC_PORT + ", SMSC_USER_NAME=" + SMSC_USER_NAME + ", SMSC_PWD=" + SMSC_PWD
                + ", SYSTEM_TYPE=" + SYSTEM_TYPE + ", SOURCE=" + SOURCE + ", ISD_CODE=" + ISD_CODE + "]";
    }

    @PostConstruct
    public void init() {
        try {
            logger.info("smpp-session Initilazing..... ");
            bindParam = new BindParameter(BindType.BIND_TX, SMSC_USER_NAME, SMSC_PWD, SYSTEM_TYPE,
                    TypeOfNumber.INTERNATIONAL, NumberingPlanIndicator.ISDN, "");
            logger.info("smpp-session : bind successfully");
        } catch (Exception ex) {
            logger.error("Exception in smpp-session Initilazing..... " + ex.getMessage());
            if (logger.isDebugEnabled())
                ex.printStackTrace();
        }
    }

    public void initSMPP() {
        try {
            logger.info("smpp-session Initilazing..... ");
            bindParam = new BindParameter(BindType.BIND_TX, SMSC_USER_NAME, SMSC_PWD, SYSTEM_TYPE,
                    TypeOfNumber.INTERNATIONAL, NumberingPlanIndicator.ISDN, "");
            logger.info("smpp-session : bind successfully");
            logger.info(printProperties());
        } catch (Exception ex) {
            logger.error("Exception in smpp-session Initilazing..... " + ex.getMessage());
            if (logger.isDebugEnabled())
                ex.printStackTrace();
        }
    }

    @PreDestroy
    public void destroy() {
        logger.info("smpp-session destroying...");
        if (session != null) {
            session.close();
            logger.info("smpp-session destroyed.");
        }
    }

    /**
     * Create new {@link SMPPSession} complete with the
     * {@link SessionStateListenerImpl}.
     *
     * @return the {@link SMPPSession}.
     * @throws IOException if the creation of new session failed.
     */
    @Bean
    private void newSession() throws IOException {
        session = new SMPPSession();
        session.connectAndBind(SMSC_HOST, Integer.parseInt(SMSC_PORT), bindParam);
        session.addSessionStateListener(new SessionStateListenerImpl());
        session.setMessageReceiverListener(new MessageReceiverListenerImpl());
    }

    /**
     * Get the session. If the session still null or not in bound state, then IO
     * exception will be thrown.
     *
     * @return the valid session.
     * @throws IOException if there is no valid session or session creation is invalid.
     */
    public SMPPSession getSession() throws IOException {
        if (session == null || session.getSessionState().equals(SessionState.CLOSED)) {
            logger.info("Initiate session for the first time to " + SMSC_HOST + ":" + SMSC_PORT);
            newSession();
        } else if (!session.getSessionState().isBound()) {
            logger.error("session state : " + session.getSessionState());
            throw new IOException("We have no valid session yet");
        }
        return session;
    }

    public SMPPSession getSession(String countryCode) throws IOException {
        initSMPP();

        if (session == null || session.getSessionState().equals(SessionState.CLOSED) || !countryCode
                .equals(this.countryCode)) {
            this.countryCode = countryCode;
            logger.info("Initiate session for the first time to " + SMSC_HOST + ":" + SMSC_PORT);

            newSession();
        } else if (!session.getSessionState().isBound()) {
            logger.error("session state : " + session.getSessionState());
            throw new IOException("We have no valid session yet");
        }
        return session;
    }

    /**
     * Reconnect session after specified interval.
     *
     * @param timeInMillis is the interval.
     */
    private void reconnectAfter(final long timeInMillis) {
        new Thread() {

            @Override
            public void run() {
                logger.info("Schedule reconnect after " + timeInMillis + " millis");
                try {
                    Thread.sleep(timeInMillis);
                } catch (InterruptedException e) {
                }

                int attempt = 0;
                while (session == null || session.getSessionState().equals(SessionState.CLOSED)) {
                    try {
                        logger.info("Reconnecting attempt #" + (++attempt) + "...");
                        newSession();
                    } catch (IOException e) {
                        logger.error("Failed opening connection and bind to " + SMSC_HOST + ":" + SMSC_PORT, e);
                        // wait for a second
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ee) {
                        }
                    }
                }
            }
        }.start();
    }

    /**
     * This class will receive the notification from {@link SMPPSession} for the
     * state changes. It will schedule to re-initialize session.
     *
     * @author uudashr
     */
    private class SessionStateListenerImpl implements SessionStateListener {

        public void onStateChange(SessionState newState, SessionState oldState, Session source) {
            if (newState.equals(SessionState.CLOSED)) {
                logger.info("Session closed");
                reconnectAfter(reconnectInterval);
            }
        }
    }

    public String getSource() {
        return SOURCE;
    }

    public String getISD() throws IOException {
        return ISD_CODE;
    }
}

