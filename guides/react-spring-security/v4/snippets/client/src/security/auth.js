import {SERVER_URL} from './../config';
import {checkResponseStatus} from './../handlers/responseHandlers';
import headers from './../security/headers';
import 'whatwg-fetch';
import qs from 'qs';

//<1>
export default {
  logIn(auth) { //<2>
    localStorage.auth = JSON.stringify(auth);
  },

  logOut() { //<3>
    delete localStorage.auth;
  },

  refreshToken() { //<4>
    return fetch(
      `${SERVER_URL}/oauth/access_token`,
      { method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
        },
        body: qs.stringify({ //<5>
          grant_type: 'refresh_token',
          refresh_token: JSON.parse(localStorage.auth).refresh_token
        })
      })
      .then(checkResponseStatus)
      .then((a) => localStorage.auth = JSON.stringify(a))
      .catch(() => { throw new Error("Unable to refresh!")})
  },

  loggedIn() {  //<6>
    return localStorage.auth && fetch(
        `${SERVER_URL}/api/vehicle`, //<7>
        {headers: headers()})
        .then(checkResponseStatus)
        .then(() => { return true })
        .catch(this.refreshToken)
        .catch(() => { return false });
  }
};