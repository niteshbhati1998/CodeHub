package com.cognizant.assessment.service;

import constant.AppConstants;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class LocationService {

    @KafkaListener(topics = AppConstants.TOPIC_DRIVER_LOCATION, groupId = AppConstants.GROUP_ID)
    public void updatedLocation(String location) {
        System.out.println("current location: " + location);
    }

    @PostConstruct
    public void performDataTransformation() {

        //kafka streams configuration
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "location-streams-app");              //unique id for stream app (equivalent to group-id)
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass()); //key, value serialize/deserialize
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> location = builder.stream(AppConstants.TOPIC_DRIVER_LOCATION);
        location.filter((key,value)-> value!=null && !value.isEmpty())
                .mapValues(value-> value.toUpperCase())
                .to(AppConstants.STEAM_TOPIC_DRIVER_LOCATION);

        //build and start stream
        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();

        //close stream when app stops
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
