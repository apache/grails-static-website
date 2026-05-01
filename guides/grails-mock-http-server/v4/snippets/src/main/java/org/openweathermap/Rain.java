//tag::clazz[]
package org.openweathermap;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class Rain {
    private Integer lastThreeHours;

    public Rain() {
    }
    //end::clazz[]
    //tag::gettersandsetters[]
    public Integer getLastThreeHours() {
        return lastThreeHours;
    }

    public void setLastThreeHours(Integer lastThreeHours) {
        this.lastThreeHours = lastThreeHours;
    }
    //end::gettersandsetters[]

}
