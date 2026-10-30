/*
 * Copyright 2019-2025 52°North Spatial Information Research GmbH
 * Modified for uetlib Jackson integration and the ch.ubique.uetlib package namespace.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.ubique.uetlib.geo.jackson.jts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

class DeserializeTest {
    private final ObjectMapper mapper;

    public DeserializeTest() {
        mapper = JsonMapper.builder().addModule(new JtsModule()).build();
    }

    @Test
    void throws_for_invalid_coordinates() {
        // GIVEN
        final var json =
                "{\"multiPolygon\":{\"type\":\"MultiPolygon\","
                        + "\"coordinates\":\"[[["
                        + "[102.01234567890,2.01234567890],"
                        + "[103.01234567890,2.01234567890],"
                        + "[103.01234567890,3.01234567890],"
                        + "[102.01234567890,3.01234567890],"
                        + "[102.01234567890,2.01234567890]]]]\"}}}";

        // WHEN / THEN
        assertThatThrownBy(() -> mapper.readValue(json, TestObject.class))
                .isInstanceOf(DatabindException.class)
                .hasMessageStartingWith("Invalid coordinates, expecting an array but got: STRING");
    }

    @Test
    void throws_for_invalid_coordinate_elements() {
        // GIVEN
        final var json = "{\"point\":{\"type\":\"Point\",\"coordinates\":[[1,2],[3,4]]}}}";

        // WHEN / THEN
        assertThatThrownBy(() -> mapper.readValue(json, TestObject.class))
                .isInstanceOf(DatabindException.class)
                .hasMessageStartingWith("Invalid coordinates, expecting numbers but got: ARRAY");
    }

    public static class TestObject {
        private Point point;
        private MultiPolygon multiPolygon;

        public MultiPolygon getMultiPolygon() {
            return multiPolygon;
        }

        public void setMultiPolygon(final MultiPolygon multiPolygon) {
            this.multiPolygon = multiPolygon;
        }

        public Point getPoint() {
            return point;
        }

        public void setPoint(final Point point) {
            this.point = point;
        }
    }
}
