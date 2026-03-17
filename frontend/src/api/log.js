import http from './axios.js'

export const getLogs = (requirementId) =>
  http.get(`/requirements/${requirementId}/logs`)
