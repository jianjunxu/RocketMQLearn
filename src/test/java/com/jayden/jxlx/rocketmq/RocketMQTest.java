package com.jayden.jxlx.rocketmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.jayden.jxlx.enums.TopicEnum;
import com.jayden.jxlx.rocketmq.producer.TransactionProducer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * User : jianjun.xu
 * Date : 2016/8/19
 * Time : 17:39
 * Desc :
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:application-test.xml")
public class RocketMQTest {
	@Autowired
	private TransactionProducer transactionProducer;

	@Test
	public void testSendMessage() {
		SendResult sendResult = transactionProducer.sendMessage("testSendMessage", TopicEnum.TEMP_TOPIC.getTopic(), TopicEnum.TEMP_TOPIC.getTags());
		System.out.println(JSON.toJSONString(sendResult));
	}

	@Test
	public void testSendMessageInTransaction() {
		SendResult sendResult = transactionProducer.sendMessage("testSendMessageInTransaction", TopicEnum.TEMP_TOPIC.getTopic(), TopicEnum.TEMP_TOPIC.getTags(), null);
		System.out.println(JSON.toJSONString(sendResult));
	}
}
