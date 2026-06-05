<!--tag::template[]-->
<template>
  <div id="garage">
    <app-header></app-header>
    <vehicle-form v-model="vehicle"
                  :makes="makes"
                  :models="models"
                  :drivers="drivers"
                  @submit="submitNewVehicle()"> <!--1-->

    </vehicle-form>
    <vehicle-table :vehicles="vehicles"></vehicle-table> <!--2-->
  </div>
</template>
<!--end::template[]-->
<!--tag::component[]-->
<script>
import AppHeader from './AppHeader' // <1>
import VehicleForm from './form/VehicleForm'
import VehicleTable from './table/VehicleTable'

export default {
  components: { // <1>
    AppHeader,
    VehicleForm,
    VehicleTable
  },
  data: function () { // <2>
    return {
      vehicles: [],
      vehicle: {name: '', make: null, model: null, driver: null},
      models: [],
      makes: [],
      drivers: [],
      serverURL: process.env.SERVER_URL // <3>
    }
  },
  // end::component[]
  // tag::fetch[]
  created () { // <1>
    this.fetchData()
  },
  methods: {
    fetchData: async function () { // <2>
      try {
        Promise.all([// <3>
          this.fetchVehicles(),
          this.fetchModels(),
          this.fetchModels(),
          this.fetchMakes(),
          this.fetchDrivers()
        ])
      } catch (error) {
        console.log(error)
      }
    },
    // end::fetch[]
    // tag::methods[]
    fetchVehicles: function () { // <1>
      fetch(`${this.serverURL}/vehicle`)
        .then(r => r.json())
        .then(json => { this.vehicles = json })
        .catch(error => console.error('Error retrieving vehicles: ' + error))
    },
    fetchModels: function () {
      fetch(`${this.serverURL}/model`)
        .then(r => r.json())
        .then(json => { this.models = json })
        .catch(error => console.error('Error retrieving models: ' + error))
    },
    fetchMakes: function () {
      fetch(`${this.serverURL}/make`)
        .then(r => r.json())
        .then(json => { this.makes = json })
        .catch(error => console.error('Error retrieving makes: ' + error))
    },
    fetchDrivers: function () {
      fetch(`${this.serverURL}/driver`)
        .then(r => r.json())
        .then(json => { this.drivers = json })
        .catch(error => console.error('Error retrieving drivers: ' + error))
    },
    // end::methods[]
    // tag::submit[]
    submitNewVehicle: function () {
      const vehicle = this.vehicle // <1>
      fetch(`${this.serverURL}/vehicle`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(vehicle)
      }).then(r => r.json())
        .then(json => {
          this.vehicles.push(json) // <2>
          this.vehicle = {name: '', make: null, model: null, driver: null} // <3>
        })
        .catch(ex => console.error('Unable to save vehicle', ex))
    }
  }
}
</script>
<!--end::submit[]-->

<!--tag::css[]-->
<!-- Per Component Custom CSS Rules -->
<!--1-->
<style>
  #garage {
    font-family: 'Avenir', Helvetica, Arial, sans-serif;
    text-align: center;
    color: #2c3e50;
  }
</style>
<!--end::css[]-->
