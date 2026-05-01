package example

import geb.Page

class ShowVehiclePage extends Page {

    static at = { title.contains('Show Vehicle') }

    static content = {
        listButton { $('a', text: 'Vehicle List') }
    }

    void vehicleList() {
        listButton.click()
    }
}
