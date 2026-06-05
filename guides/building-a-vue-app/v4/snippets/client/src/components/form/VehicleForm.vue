<template id="add-vehicle-template" xmlns="http://www.w3.org/1999/xhtml">
  <div>
    <vehicle-form-header/> <!--1-->

    <div id="inputRow" class="row">
      <div class="col-sm-3">
        <div class="input-group">
          <input type="text" class="form-control" placeholder="Enter a name..." v-model="vehicle.name"> <!--2-->
        </div>
      </div>
      <div class="col-sm-7">
        <div class="row">
          <div class="col-sm-4">
            <field-select v-model="vehicle.make" :field="'Make'" :values="makes"></field-select> <!--3-->
          </div>
          <div class="col-sm-4">
            <field-select v-model="vehicle.model" :field="'Model'" :values="models"></field-select>  <!--4-->
          </div>
          <div class="col-sm-4">
            <field-select v-model="vehicle.driver" :field="'Driver'" :values="drivers"></field-select>
          </div>
        </div>
      </div>
      <div class="col-sm-2">
        <div class="btn-group" role="group" aria-label="Add new vehicle">
          <button type="button" class="btn btn-success" @click="submit()">Add to Garage</button> <!--5-->
        </div>
      </div>
    </div>
  </div>

</template>

<script>
import VehicleFormHeader from './VehicleFormHeader'
import FieldSelect from './FieldSelect'

export default {
  props: ['vehicle', 'makes', 'models', 'drivers'], // <6>
  model: {
    prop: 'vehicle', // <4>
    event: 'change'
  },
  components: {
    VehicleFormHeader,
    FieldSelect
  },
  methods: {
    submit () { // <5>
      this.$emit('submit')
    }
  }
}
</script>

<style>
  /* Add custom rules here */
</style>
