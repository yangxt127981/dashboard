import http from './axios.js'

export const getInboxList = (params) => http.get('/inbox', {
  params,
  paramsSerializer: (p) => {
    const qs = new URLSearchParams()
    Object.entries(p).forEach(([key, val]) => {
      if (Array.isArray(val)) val.forEach(v => qs.append(key, v))
      else if (val !== '' && val !== null && val !== undefined) qs.append(key, val)
    })
    return qs.toString()
  }
})
export const getInboxTabCounts = () => http.get('/inbox/tab-counts')
export const createInbox = (data) => http.post('/inbox', data)
export const updateInbox = (id, data) => http.put(`/inbox/${id}`, data)
export const deleteInbox = (id) => http.delete(`/inbox/${id}`)
export const submitInbox = (id) => http.post(`/inbox/${id}/submit`)
export const withdrawInbox = (id) => http.post(`/inbox/${id}/withdraw`)
export const evaluateInbox = (id, pass, rejectReason) => http.post(`/inbox/${id}/evaluate`, { pass: String(pass), rejectReason })
export const archiveInbox = (id) => http.post(`/inbox/${id}/archive`)
export const getInboxLogs = (id) => http.get(`/inbox/${id}/logs`)
