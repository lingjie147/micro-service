package com.fukun.es.consumer;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.fukun.commons.constants.RabbitMqConstants;
import com.fukun.es.util.EsUtil;
import com.fukun.syn.model.MessageEntry;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

/**
 * rabbitMq消费端的消费消息的逻辑
 * <p>
 * 注：实际使用中最好生产端与消费端分开，不要写在一块，这样可以多增加消费端的实例，增大消费端的消费速率，避免消息积压
 * 还可以充分体现rabbitmq的异步解耦能力，这里只进行测试使用，所以写在一块
 *
 * @author tangyifei
 * @date 2019年7月18日15:04:19
 */
@Component
@Slf4j
public class RabbitMqConsumer {

    private static final String DELIMITER = ":";

    @Resource
    private EsUtil esUtil;

    @RabbitListener(queues = {RabbitMqConstants.FANOUT_QUEUE_NAME})
    public void handleObjectMessage(Message message, Channel channel) throws IOException {
        try {
            // 为了测试死信队列，就是说消息处理失败，处理失败的消息会进入死信队列
            // int a = 1 / 0;
            if (log.isInfoEnabled()) {
                // 处理消息
                log.info("消费者处理消息成功，消息是：{}", new String(message.getBody()));
            }
            if (null != message) {
                MessageEntry messageEntry = new Gson().fromJson(new String(message.getBody()), MessageEntry.class);
                synToElasticSearch(messageEntry);
            }
            // 手动签收，multiple为false，只确认当前消息；multiple为true，不仅确认当前消息，还可以确认之前接受到的消息
            // 如果 channel.basicAck   channel.basicNack  channel.basicReject 这三个方法都不执行，消息也会被确认 【这个其实并没有在官方看到，不过自己测试的确是这样哈】
            // 所以，正常情况下一般不需要执行 channel.basicAck
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.error("消费者处理消息失败，消息是：{}，异常是：{}", new String(message.getBody()), e);
            }
            // 拒绝消费消息（丢失消息） 给死信队列处理消息失败。
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        }
    }

    /**
     * 监听替补队列 来验证死信.
     *
     * @param message the message
     * @param channel the channel
     * @throws IOException the io exception  这里异常需要处理
     */
    @RabbitListener(queues = {RabbitMqConstants.DEAD_LETTER_QUEUE_NAME})
    public void redirect(Message message, Channel channel) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("dead message  10s 后 消费消息 {}", new String(message.getBody()));
        }
        if (null != message) {
            MessageEntry messageEntry = new Gson().fromJson(new String(message.getBody()), MessageEntry.class);
            synToElasticSearch(messageEntry);
        }
        if (log.isInfoEnabled()) {
            // 处理消息
            log.info("消费者处理消息成功！");
        }
        // 手动签收，multiple为false，只确认当前消息；multiple为true，不仅确认当前消息，还可以确认之前接受到的消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
    }

    /**
     * 同步到es中,7.x版本的es已经不存在type了,type就相当于数据库中相关的表名.
     * 当数据库的数据同步到es时,确保之前的数据库中的记录的主键的唯一性,相同数据库中
     * 不同的表的记录的主键不能有相同的,否则当在同一个库,行记录主键相同,
     * 在es中就会存在索引覆盖问题.
     *
     * @param messageEntry binlog消息事件实体
     */
    private void synToElasticSearch(MessageEntry messageEntry) throws Exception {
        CanalEntry.EventType eventType = messageEntry.getEventType();
        String dataBase = messageEntry.getSchemaName();
        Map<String, Object> map = messageEntry.getAfter();
        String id = String.valueOf(map.get("id"));
        if (CanalEntry.EventType.DELETE == eventType) {
            esUtil.deleteDocument(dataBase, id);
        } else if (CanalEntry.EventType.INSERT == eventType) {
            esUtil.addDocument(dataBase, id, map);
        } else if (CanalEntry.EventType.UPDATE == eventType) {
            esUtil.updateDocument(dataBase, id, map);
        }
    }

}
