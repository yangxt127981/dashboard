import http from './axios.js'

export const getList = (params) => http.get('/requirements', {
  params,
  paramsSerializer: (p) => {
    const qs = new URLSearchParams()
    Object.entries(p).forEach(([key, val]) => {
      if (Array.isArray(val)) {
        val.forEach(v => qs.append(key, v))
      } else if (val !== '' && val !== null && val !== undefined) {
        qs.append(key, val)
      }
    })
    return qs.toString()
  }
})
export const create = (data) => http.post('/requirements', data)
export const update = (id, data) => http.put(`/requirements/${id}`, data)
export const remove = (id) => http.delete(`/requirements/${id}`)
