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
package org.envirocar.server.core.entities;

import org.locationtech.jts.geom.Geometry;

public interface TrackSummary {
    String getIdentifier();

    void setIdentifier(String id);

    boolean hasIdentifier();

    Geometry getStartPosition();

    void setStartPosition(Geometry startPosition);

    boolean hasStartPosition();

    Geometry getEndPosition();

    void setEndPosition(Geometry endPosition);

    boolean hasEndPosition();
}
