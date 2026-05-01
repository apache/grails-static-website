export default () => { //<1>
  return {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${localStorage.auth ? JSON.parse(localStorage.auth).access_token : null}`
  }
}