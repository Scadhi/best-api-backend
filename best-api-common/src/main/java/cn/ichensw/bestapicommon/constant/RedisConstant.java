package cn.ichensw.bestapicommon.constant;

/***
 * @title RedisConstant
 **/
public interface RedisConstant {
    String SMS_PREFIX = "sms:";
    String SMS_BUCKET_PREFIX = SMS_PREFIX + "bucket:";
    String SMS_CODE_PREFIX = SMS_PREFIX + "code:";
    String SMS_MESSAGE_PREFIX = SMS_PREFIX + "mq:messageId:";
    String MQ_PRODUCER = SMS_PREFIX + "mq:producer:fail";
}
