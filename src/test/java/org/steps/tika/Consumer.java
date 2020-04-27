//package org.pentaho.di.sdk.steps.tika;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.Properties;
//
//import org.apache.kafka.clients.consumer.ConsumerConfig;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.apache.kafka.clients.consumer.ConsumerRecords;
//import org.apache.kafka.clients.consumer.KafkaConsumer;
//import org.apache.kafka.clients.producer.Producer;
//import org.apache.kafka.common.serialization.StringDeserializer;
///**
// * @program: kettle-sdk-step-plugin
// * @description: ${description}
// * @author: Gou Ding Cheng
// * @create: 2019-09-16 17:18
// **/
//public class Consumer {
//    public static void main(String[] args) {
//
//        Properties props = new Properties();
//        props.put("bootstrap.servers", "192.168.30.9:9092");
//        props.put("group.id", "test-consumer-group");
//        props.put("enable.auto.commit", "true");
//        props.put("auto.commit.interval.ms", "1000");
//        props.put("auto.offset.reset", "earliest");
//        props.put("session.timeout.ms", "30000");
//        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
//        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
//
//        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<String, String>(props);
//        kafkaConsumer.subscribe(Arrays.asList("fwd-test"));
//
//        while(true)
//        {
//            System.out.println("nothing available...");
//            ConsumerRecords<String, String> records = kafkaConsumer.poll(1000);
//            for(ConsumerRecord<String, String> record : records)
//            {
//                System.out.printf("offset = %d, value = %s", record.offset(), record.value());
//                System.out.println();
//            }
//        }
//    }
//}