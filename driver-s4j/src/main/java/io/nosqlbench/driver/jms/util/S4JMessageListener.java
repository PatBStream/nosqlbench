package io.nosqlbench.driver.jms.util;

/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import io.nosqlbench.driver.jms.S4JActivity;
import io.nosqlbench.driver.jms.S4JSpace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

public class S4JMessageListener implements MessageListener {

    private final static Logger logger = LogManager.getLogger(S4JSpace.class);

    private final float msgAckRatio;
    private JMSContext jmsContext;
    private S4JSpace s4JSpace;
    private S4JActivity s4JActivity;

    public S4JMessageListener(JMSContext jmsContext, S4JSpace s4JSpace, float msgAckRatio) {
        this.jmsContext = jmsContext;
        this.s4JSpace = s4JSpace;
        this.s4JActivity = s4JSpace.getS4JActivity();
        this.msgAckRatio = msgAckRatio;
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message != null) {
                s4JActivity.processMsgAck(jmsContext.getSessionMode(), message, msgAckRatio);

                int msgSize = message.getIntProperty(S4JActivityUtil.NB_MSG_SIZE_PROP);
                Counter bytesCounter = this.s4JActivity.getBytesCounter();
                bytesCounter.inc(msgSize);
                Histogram messageSizeHistogram = this.s4JActivity.getMessagesizeHistogram();
                messageSizeHistogram.update(msgSize);

                if (logger.isDebugEnabled()) {
                    // for testing purpose
                    String myMsgSeq = message.getStringProperty(S4JActivityUtil.NB_MSG_SEQ_PROP);

                    logger.debug("onMessage::Async message receive successful - message ID {} ({}) "
                        , message.getJMSMessageID(), myMsgSeq);
                }

                s4JSpace.incTotalOpResponseCnt();
            }
        }
        catch (JMSException jmsException) {
            logger.warn("onMessage::Unexpected error:" + jmsException.getMessage());
        }
    }
}