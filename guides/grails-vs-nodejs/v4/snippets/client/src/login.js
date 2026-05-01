import React, {Component, PropTypes as t} from 'react';
import 'whatwg-fetch';

function onChangePassword(event) {
  React.setState({password: event.target.value});
}

function onChangeUsername(event) {
  React.setState({username: event.target.value});
}

class Login extends Component {
  static propTypes = {
    password: t.string.isRequired,
    restUrl: t.string.isRequired,
    username: t.string.isRequired,
    timeoutHandler: t.func //NEW: propType for our timeoutHander function
  };

  // This is called when the "Log In" button is pressed.
  onLogin = async () => {
    const {password, restUrl, username, timeoutHandler} = this.props;
    const url = `${restUrl}/login`;

    try {
      // Send username and password to login REST service.
      const res = await fetch(url, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({username, password})
      });

      if (res.ok) { // successful login
        const text = await res.text(); // returns a promise

        //NEW: Spring Security REST returns the token in the response body, not the Authorization header
        const token = `Bearer ${JSON.parse(text).access_token}`;

        React.setState({
          authenticated: true,
          error: null, // clear previous error
          route: 'main',
          token
        });

        timeoutHandler(username) //NEW: Connects to a user-specific websocket channel

      } else { //NEW: Any error response from the server indicates a failed login
        const msg = "Invalid username or password";
        React.setState({error: msg});
      }
    } catch (e) {
      React.setState({error: `${url}; ${e.message}`});
    }
  }

  // This is called when the "Signup" button is pressed.
  onSignup = async () => {
    const {password, restUrl, username, timeoutHandler} = this.props;
    const url = `${restUrl}/signup`;

    try {
      const res = await fetch(url, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({username, password})
      });

      if (res.ok) { // successful signup
        const text = await res.text(); // returns a promise
        const token = `Bearer ${JSON.parse(text).access_token}`; //NEW: See above

        React.setState({
          authenticated: true,
          error: null, // clear previous error
          route: 'main',
          token
        });

        timeoutHandler(username) //NEW: Connect to user-specific websocket channel


      } else { // unsuccessful signup
        let text = await res.text(); // returns a promise
        if (/duplicate key/.test(text)) {
          text = `User ${username} already exists`;
        }
        React.setState({error: text});
      }
    } catch (e) {
      React.setState({error: `${url}; ${e.message}`});
    }
  };

  render() {
    const {password, username} = this.props;
    const canSubmit = username && password;

    // We are handling sending the username and password
    // to a REST service above, so we don't want
    // the HTML form to submit anything for us.
    // That is the reason for the call to preventDefault.
    return (
      <form className="login-form"
            onSubmit={event => event.preventDefault()}>
        <div className="row">
          <label>Username:</label>
          <input type="text"
                 autoFocus
                 onChange={onChangeUsername}
                 value={username}
          />
        </div>
        <div className="row">
          <label>Password:</label>
          <input type="password"
                 onChange={onChangePassword}
                 value={password}
          />
        </div>
        <div className="row submit">
          {/* Pressing enter in either input invokes the first button. */}
          <button disabled={!canSubmit} onClick={this.onLogin}>
            Log In
          </button>
          <button disabled={!canSubmit} onClick={this.onSignup}>
            Signup
          </button>
        </div>
      </form>
    );
  }
}

export default Login;
