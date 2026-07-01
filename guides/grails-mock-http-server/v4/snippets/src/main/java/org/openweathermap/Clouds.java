//tag::clazz[]
package org.openweathermap;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;

@Introspected
public class Clouds {
    @JsonProperty("all")
    private Integer cloudiness;

    public Clouds() {
    }
    //end::clazz[]
    //tag::gettersandsetters[]
    public Integer getCloudiness() {
        return cloudiness;
    }

    public void setCloudiness(Integer cloudiness) {
        this.cloudiness = cloudiness;
    }
    //end::gettersandsetters[]
}
