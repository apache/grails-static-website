<template id="fulltable-template" xmlns="http://www.w3.org/1999/xhtml">
  <table class="table">
    <thead class="thead-inverse">
      <tr>
        <th>ID</th>
        <th>Name</th>
        <th>Make</th>
        <th>Model</th>
        <th>Driver</th>
      </tr>
    </thead> <!--1-->
      <table-row v-for="vehicle in vehicles"
                 :item="vehicle" :key="vehicle.id"></table-row> <!--2-->
  </table>
</template>

<script>
import TableRow from './TableRow.vue' // <3>

export default {
  props: ['vehicles'],
  components: { // <3>
    TableRow
  }
}
</script>

<style>
  /* Add custom rules here */
</style>
