package example

import geb.Page

class VehiclesPage extends Page {

    static at = { title.contains('Vehicle List') }

    static content = {
        newVehicleLink { $('a', text: 'New Vehicle') }
        vehiclesRows { $('tbody tr') }
    }

    void newVehicle() {
        newVehicleLink.click()
    }

    int numberOfVehicles() {
        vehiclesRows.size()
    }
}
