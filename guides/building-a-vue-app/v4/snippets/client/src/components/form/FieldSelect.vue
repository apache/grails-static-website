<template id="driverSelect-template" xmlns="http://www.w3.org/1999/xhtml">
  <div class="form-group"> <!--4-->
    <select class="form-control" v-model="selected" @change="updateValue()"> <!--7-->
      <option disabled :value="null">Select a {{field}}</option> <!--2-->
      <!--1-->
      <option v-for="value in values" :value="value.id" :key="value.id">
        {{ value.name }}
      </option>
    </select>
  </div>
</template>

<script>
export default {
  // <1>
  props: ['values', 'field'], // <2>
  data: function () { // <2>
    return {
      selected: null // <3>
    }
  },
  methods: { // <5>
    updateValue: function () { // <6>
      this.$emit('input', this.selected)
    }
  }
}
</script>

<style>
  /* Add custom rules here */
</style>
