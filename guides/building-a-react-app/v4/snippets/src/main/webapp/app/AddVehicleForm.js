import React from 'react';
import {array, func} from 'prop-types';

class AddVehicleForm extends React.Component {

  constructor(props) {
    super(props);
    this.state = { // <1>
      name: '',
      make: {id: ''},
      model: {id: ''},
      driver: {id: ''}};
  }

  handleSubmit = (event) => { // <2>
    event.preventDefault();

    const {name, make, model, driver} = this.state;

    if (!name || !make.id || !model.id || !driver.id) {
      console.warn("missing required field!");
      return;
    }
    this.props.onSubmit( {name, make, model, driver} ); // <3>
    this.setState({ name: '', make: {id: ''}, model: {id: ''}, driver: {id: ''}});
  };

  handleNameChange = (event) => { //<4>
    this.setState({ name: event.target.value });
  };

  handleMakeChange = (event) => { //<4>
    this.setState({ make: {id: event.target.value} });
  };

  handleModelChange = (event) => { //<4>
    this.setState({ model: {id: event.target.value} });
  };

  handleDriverChange = (event) => { //<4>
    this.setState({ driver: {id: event.target.value} });
  };


  render() {

    function renderSelectList(item) { //<5>
      return <option key={item.id} value={item.id}>{item.name}</option>
    }

    return(
      <div>
        <h3>Add a Vehicle:</h3>
        <form className="form form-inline" onSubmit={this.handleSubmit}  >
          <label>Name</label>
          <input className="form-control" name="name" type="text" value={ this.state.name } onChange={ this.handleNameChange } />

          <label>Make</label>
          <select className="form-control" name="make" value={this.state.make.id} 
            onChange={this.handleMakeChange}>  {/*<6>*/}
            <option value={null}>Select a Make...</option>
            {this.props.makes.map(renderSelectList)}  {/*<5>*/}
          </select>

          <label>Model</label>
          <select className="form-control" name="model" value={this.state.model.id} 
            onChange={this.handleModelChange}>  {/*<6>*/}
            <option value={null}>Select a Model...</option>
            {this.props.models.map(renderSelectList)}  {/*<5>*/}
          </select>

          <label>Driver</label>
          <select className="form-control" name="driver" value={this.state.driver.id} 
            onChange={this.handleDriverChange}>  {/*<6>*/}
            <option value={null}>Select a Driver...</option>
            {this.props.drivers.map(renderSelectList)}  {/*<5>*/}
          </select>

          <input className="btn btn-success"  type="submit" value="Add to library" />
        </form>
      </div>
    );

  }
}

AddVehicleForm.propTypes = {
  makes: array,
  models: array,
  drivers: array,
  onSubmit: func
};

export default AddVehicleForm;