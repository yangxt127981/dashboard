import http from './axios.js'

export const getAttachments = (requirementId) =>
  http.get(`/requirements/${requirementId}/attachments`)

export const addAttachment = (requirementId, data) =>
  http.post(`/requirements/${requirementId}/attachments`, data)

export const deleteAttachment = (id) =>
  http.delete(`/attachments/${id}`)
