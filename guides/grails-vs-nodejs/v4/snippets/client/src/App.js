import React, {Component} from 'react';
import Login from './login';
import Main from './main';
import 'whatwg-fetch'; // for REST calls
import {SERVER_URL} from './config'; //NEW: Base url for REST calls
import './App.css';

// This allows the client to listen to sockJS events
// emitted from the server.  It is used to proactively
// terminate sessions when the session timeout expires.
import SockJS from 'sockjs-client'; //NEW: SockJS & Stomp instead of socket.io
import Stomp from 'stompjs';

class App extends Component {
  constructor() {
    super();

    // Redux is a popular library for managing state in a React application.
    // This application, being somewhat small, opts for a simpler approach
    // where the top-most component manages all of the state.
    // Placing a bound version of the setState method on the React object
    // allows other components to call it in order to modify state.
    // Each call causes the UI to re-render,
    // using the "Virtual DOM" to make this efficient.
    React.setState = this.setState.bind(this);
  }

  //NEW: Remove the top-level websocket config in favor of user-specific channels (set on login)

  // This is the initial state of the application.
  state = {
    authenticated: false,
    error: '',
    flavor: '',
    iceCreamMap: {},
    password: '',
    restUrl: SERVER_URL, //NEW: Use our SERVER_URL variable
    route: 'login', // controls the current page
    token: '',
    username: ''
  };

  /**
   * NEW: Clears the token and redirects to the login page.
   * No logout API call is needed because JWT tokens
   * expire w/o state changes on the server */
  logout = async () => {
      React.setState({
        authenticated: false,
        route: 'login',
        password: '',
        username: ''
      });
  };

  /**
  * NEW: This function will be passed into the Login component as a prop
  * This gets a SockJS connection from the server
  * and subscribes to a "topic/[username]" channel for timeout events.
  * If one is received, the user is logged out. */
  timeoutHandler = (username) => {
    const socket = new SockJS(`${SERVER_URL}/stomp`);
    const client = Stomp.over(socket);

    client.connect({}, () => {
      client.subscribe(`/topic/${username}`, () => {
        alert('Your session timed out.');
        this.logout();
      });
    }, () => {
      console.error('unable to connect');
    });
  };

  render() {
    // Use destructuring to extract data from the state object.
    const {
      authenticated, error, flavor, iceCreamMap,
      password, restUrl, route, token, username
    } = this.state;

    return (
      <div className="App">
        <header>
          <img className="header-img" src="ice-cream.png" alt="ice cream" />
          Ice cream, we all scream for it!
          {
            authenticated ?
              <button onClick={this.logout}>Log out</button> :
              null
          }
        </header>
        <div className="App-body">
          {
            // This is an alternative to controlling routing to pages
            // that is far simpler than more full-blown solutions
            // like react-router.
            route === 'login' ?
              <Login
                username={username}
                password={password}
                restUrl={restUrl}
                timeoutHandler={this.timeoutHandler} //NEW: Pass our timeoutHandler as a prop to <Login>
              /> :
            route === 'main' ?
              <Main
                flavor={flavor}
                iceCreamMap={iceCreamMap}
                restUrl={restUrl}
                token={token}
                username={username}
              /> :
              <div>Unknown route {route}</div>
          }
          {
            // If an error has occurred, render it at the bottom of any page.
            error ? <div className="error">{error}</div> : null
          }
        </div>
      </div>
    );
  }
}

export default App;
