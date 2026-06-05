package example

import geb.Page

class NewVehiclePage extends Page {

    static at = { title.contains('Create Vehicle') }

    static content = {
        inputModel { $('input', name: 'model') }
        inputYear { $('input', name: 'year') }
        createButton { $('input', name: 'create') }
    }

    void newVehicle(String model, int year) {
        inputModel << model
        inputYear = year
        createButton.click()
    }
}
