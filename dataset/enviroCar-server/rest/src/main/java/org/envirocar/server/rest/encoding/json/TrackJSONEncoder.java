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
package org.envirocar.server.rest.encoding.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import org.envirocar.server.core.DataService;
import org.envirocar.server.core.entities.Measurements;
import org.envirocar.server.core.entities.Sensor;
import org.envirocar.server.core.entities.Track;
import org.envirocar.server.core.entities.TrackStatus;
import org.envirocar.server.core.entities.User;
import org.envirocar.server.core.filter.MeasurementFilter;
import org.envirocar.server.core.util.GeoJSONConstants;
import org.envirocar.server.rest.JSONConstants;
import org.envirocar.server.rest.Schemas;
import org.envirocar.server.rest.encoding.JSONEntityEncoder;
import org.envirocar.server.rest.rights.AccessRights;

import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
@Provider
@Singleton
public class TrackJSONEncoder extends AbstractJSONEntityEncoder<Track> {
    private final JSONEntityEncoder<Sensor> sensorEncoder;
    private final JSONEntityEncoder<Measurements> measurementsEncoder;
    private final JSONEntityEncoder<User> userEncoder;
    private final DataService dataService;

    @Inject
    public TrackJSONEncoder(JSONEntityEncoder<Sensor> sensorEncoder,
                            JSONEntityEncoder<Measurements> measurementsEncoder,
                            JSONEntityEncoder<User> userEncoder,
                            DataService dataService) {
        super(Track.class);
        this.sensorEncoder = sensorEncoder;
        this.userEncoder = userEncoder;
        this.measurementsEncoder = measurementsEncoder;
        this.dataService = dataService;
    }

    @Override
    public ObjectNode encodeJSON(Track entity, AccessRights rights, MediaType mediaType) {
        ObjectNode track = getJsonFactory().objectNode();
        if (getSchemaUriConfiguration().isSchema(mediaType, Schemas.TRACK)) {
            track.put(GeoJSONConstants.TYPE_KEY, GeoJSONConstants.FEATURE_COLLECTION_TYPE);
            ObjectNode properties = track.putObject(GeoJSONConstants.PROPERTIES_KEY);
            if (entity.hasDescription() && rights.canSeeDescriptionOf(entity)) {
                properties.put(JSONConstants.DESCRIPTION_KEY, entity.getDescription());
            }
            if (entity.hasCreationTime() && rights.canSeeCreationTimeOf(entity)) {
                properties.put(JSONConstants.CREATED_KEY, getDateTimeFormat().print(entity.getCreationTime()));
            }
            if (entity.hasUser() && rights.canSeeUserOf(entity)) {
                properties.set(JSONConstants.USER_KEY, this.userEncoder.encodeJSON(entity.getUser(), rights, mediaType));
            }
            if (entity.hasAppVersion() && rights.canSeeAppVersionOf(entity)) {
                properties.put(JSONConstants.APP_VERSION_KEY, entity.getAppVersion());
            }

            if (entity.hasTouVersion() && rights.canSeeTouVersionOf(entity)) {
                properties.put(JSONConstants.TOU_VERSION_KEY, entity.getTouVersion());
            }
            if (entity.hasMeasurementProfile() && rights.canSeeMeasurementProfile(entity)) {
                properties.put(JSONConstants.MEASUREMENT_PROFILE, entity.getMeasurementProfile());
            }
            addCommonProperties(entity, rights, properties, mediaType);
            JsonNode features;
            if (rights.canSeeMeasurementsOf(entity)) {
                Measurements values = this.dataService.getMeasurements(new MeasurementFilter(entity));
                features = this.measurementsEncoder.encodeJSON(values, rights, mediaType)
                                                   .path(GeoJSONConstants.FEATURES_KEY);
            } else {
                features = track.arrayNode();
            }
            track.set(GeoJSONConstants.FEATURES_KEY, features);
        } else {
            addCommonProperties(entity, rights, track, mediaType);
        }
        return track;
    }

    private void addCommonProperties(Track entity, AccessRights rights, ObjectNode track, MediaType mediaType) {
        if (entity.hasBegin()) {
            track.put(JSONConstants.BEGIN_KEY, getDateTimeFormat().print(entity.getBegin()));
        }
        if (entity.hasEnd()) {
            track.put(JSONConstants.END_KEY, getDateTimeFormat().print(entity.getEnd()));
        }
        if (entity.hasIdentifier()) {
            track.put(JSONConstants.IDENTIFIER_KEY, entity.getIdentifier());
        }
        if (entity.hasModificationTime() && rights.canSeeModificationTimeOf(entity)) {
            track.put(JSONConstants.MODIFIED_KEY, getDateTimeFormat().print(entity.getModificationTime()));
        }
        if (entity.hasName() && rights.canSeeNameOf(entity)) {
            track.put(JSONConstants.NAME_KEY, entity.getName());
        }
        if (entity.hasLength() && rights.canSeeLengthOf(entity)) {
            track.put(JSONConstants.LENGTH_KEY, entity.getLength());
        }
        if (entity.hasSensor() && rights.canSeeSensorOf(entity)) {
            track.set(JSONConstants.SENSOR_KEY, this.sensorEncoder.encodeJSON(entity.getSensor(), rights, mediaType));
        }
        if (rights.canSeeStatusOf(entity)) {
            track.put(JSONConstants.STATUS_KEY, (entity.hasStatus() ? entity.getStatus()
                                                                    : TrackStatus.FINISHED).toString());
        }
    }
}
