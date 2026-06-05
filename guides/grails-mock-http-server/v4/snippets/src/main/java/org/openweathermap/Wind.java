//tag::clazz[]

package org.openweathermap;

import io.micronaut.core.annotation.Introspected;

import java.math.BigDecimal;

@Introspected
public class Wind {
    private BigDecimal speed;
    private BigDecimal deg;

    public Wind() {

    }
//end::clazz[]
    //tag::gettersandsetters[]

    public BigDecimal getSpeed() {
        return speed;
    }

    public void setSpeed(BigDecimal speed) {
        this.speed = speed;
    }

    public BigDecimal getDeg() {
        return deg;
    }

    public void setDeg(BigDecimal deg) {
        this.deg = deg;
    }
    //end::gettersandsetters[]

}
