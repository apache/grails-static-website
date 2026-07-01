//tag::clazz[]
package org.openweathermap;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class Sys {
    private Long id;
    private String type;
    private String message;
    private String country;
    private Integer sunrise;
    private Integer sunset;

    public Sys() {

    }
    //end::clazz[]
    //tag::gettersandsetters[]
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Integer getSunrise() {
        return sunrise;
    }

    public void setSunrise(Integer sunrise) {
        this.sunrise = sunrise;
    }

    public Integer getSunset() {
        return sunset;
    }

    public void setSunset(Integer sunset) {
        this.sunset = sunset;
    }
    //end::gettersandsetters[]
}
