package com.jayden.jxlx.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * User : jianjun.xu
 * Date : 2016/8/19
 * Time : 16:19
 * Desc :
 */
public enum TopicEnum {
	TEMP_TOPIC("TEMP", "TEMP_TOPIC", "临时主题");

	/**
	 * 主题
	 */
	private String topic;
	/**
	 * 标签
	 */
	private String tags;
	/**
	 * 描述
	 */
	private String desc;

	TopicEnum(String topic, String tags, String desc) {
		this.topic = topic;
		this.tags = tags;
		this.desc = desc;
	}

	public String getTopic() {
		return topic;
	}

	public String getTags() {
		return tags;
	}

	public String getDesc() {
		return desc;
	}

	/**
	 * code和枚举转换.
	 *
	 * @return topicEnum
	 */
	public static TopicEnum findTopicEnum(String topic, String tags) {
		for (TopicEnum topicEnum : TopicEnum.class.getEnumConstants()) {
			if (StringUtils.equals(topicEnum.getTopic(), topic)
					&& StringUtils.equals(topicEnum.getTags(), tags)) {
				return topicEnum;
			}
		}
		return null;
	}

	public String getTopicStr() {
		return topic + "," + tags;
	}

	public static TopicEnum getTopicEnumFromStr(String desc) {
		String[] tags = desc.split(",");
		return findTopicEnum(tags[0], tags[1]);
	}

	public boolean equals(String topic, String tags) {
		return topic != null && tags != null &&
				this.getTags().equals(tags) && this.getTopic().equals(topic);
	}
}
