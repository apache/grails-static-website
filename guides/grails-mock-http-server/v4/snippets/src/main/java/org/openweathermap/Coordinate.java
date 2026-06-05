//tag::clazz[]
package org.openweathermap;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;

import java.math.BigDecimal;

@Introspected
public class Coordinate {

    @JsonProperty("long")
    private BigDecimal longitude;

    @JsonProperty("lat")
    private BigDecimal latitude;

    public Coordinate() {
    }
    //end::clazz[]
    //tag::gettersandsetters[]
    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }
    //end::gettersandsetters[]
}
