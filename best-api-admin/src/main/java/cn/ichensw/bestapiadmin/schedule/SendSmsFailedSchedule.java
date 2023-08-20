package cn.ichensw.bestapiadmin.schedule;

import cn.ichensw.bestapiadmin.utils.SmsRabbitMqUtils;
import cn.ichensw.bestapicommon.constant.LockConstant;
import cn.ichensw.bestapicommon.constant.RedisConstant;
import cn.ichensw.bestapicommon.model.dto.sms.SmsDTO;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/***
 * 支短信消息重发任务
 **/
@Slf4j
@Component
@EnableAsync
@EnableScheduling
public class SendSmsFailedSchedule {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private SmsRabbitMqUtils rabbitMqUtils;

    /**
     * 消息可靠性保证（发送端可靠性保证）
     * 每分钟从生产者redis中重新发送发送发送短信的消息
     */
    @Scheduled(cron = "*/60 * * * * ?")
    public void sendFailedSms(){
        RLock lock = redissonClient.getLock(LockConstant.SMS_SEND_SUCCESS);
        try {
            // 为加锁等待20秒时间，并在加锁成功10秒钟后自动解开
            boolean tryLock = lock.tryLock(20, 10, TimeUnit.SECONDS);
            if (tryLock){
                //重新向mq中发送订单消息
                Set keys = redisTemplate.keys(RedisConstant.MQ_PRODUCER + "*");
                for (Object key : keys) {
                    SmsDTO smsDTO = (SmsDTO) redisTemplate.opsForValue().get(key);
                    //删除reids中的该条记录
                    redisTemplate.delete(key.toString());
                    rabbitMqUtils.sendSmsAsync(smsDTO);
                }
            }
        } catch (InterruptedException e) {
            log.error("===定时任务:获取失败生产者发送消息redis出现bug===");
            throw new RuntimeException(e);
        }finally {
            lock.unlock();
        }
    }
}
