import React from 'react';
import ReactDOM from 'react-dom';
import App from './App';
import './css/bootstrap.css';
import './css/App.css';
import './css/grails.css';
import './css/main.css';
import {ApolloProvider} from 'react-apollo';
import {ApolloClient} from 'apollo-client';
import {createHttpLink} from 'apollo-link-http';
import {InMemoryCache} from 'apollo-cache-inmemory';
import { SERVER_URL} from "./config";

// <1>
const client = new ApolloClient({
  link: createHttpLink({ uri: `${SERVER_URL}/graphql` }), // <2>
  cache: new InMemoryCache()
});

// <3>
ReactDOM.render(< ApolloProvider client={client}>
  <App />
</ApolloProvider>, document.getElementById('root'));