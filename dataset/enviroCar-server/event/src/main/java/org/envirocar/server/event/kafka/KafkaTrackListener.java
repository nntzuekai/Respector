/*
 * Copyright (C) 2013-2022 The enviroCar project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.envirocar.server.event.kafka;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.envirocar.server.core.entities.Track;
import org.envirocar.server.core.entities.TrackStatus;
import org.envirocar.server.core.event.ChangedTrackEvent;
import org.envirocar.server.core.event.ChangedTrackStatusEvent;
import org.envirocar.server.core.event.CreatedTrackEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.util.Objects;

public class KafkaTrackListener {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaTrackListener.class);
    private final Producer<String, Track> producer;
    private final String topicName;

    @Inject
    public KafkaTrackListener(Producer<String, Track> producer,
                              @Named(KafkaConstants.KAFKA_TRACK_TOPIC) String topic) {
        this.producer = Objects.requireNonNull(producer);
        this.topicName = Objects.requireNonNull(topic);
    }

    @Subscribe
    public void onCreatedTrackEvent(CreatedTrackEvent e) {
        if (e.getTrack().getStatus() == TrackStatus.ONGOING) {
            LOG.debug("Created ongoing track, won't publish");
        } else {
            publish(e.getTrack());
        }
    }

    @Subscribe
    public void onModifiedTrackEvent(ChangedTrackEvent e) {
        if (e instanceof ChangedTrackStatusEvent) {
            if (((ChangedTrackStatusEvent) e).matches(TrackStatus.ONGOING, TrackStatus.FINISHED)) {
                LOG.debug("Finished ongoing track, publish");
                publish(e.getTrack());
            }
        }
    }

    private void publish(Track track) {
        ProducerRecord<String, Track> record = new ProducerRecord<>(this.topicName, track.getIdentifier(), track);
        LOG.info("Publishing track {} to kafka", record.key());
        this.producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                LOG.error("Error publishing track to kafka", exception);
            }
        });
    }

}
