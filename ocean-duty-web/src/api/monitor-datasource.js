import { postAxios, getAxios } from '@/lib/axios'

export const queryDatasource = (data) => postAxios('/monitor/datasource/query', data)

export const listDatasource = () => getAxios('/monitor/datasource/list')

export const getDatasource = (id) => getAxios(`/monitor/datasource/get/${id}`)

export const addDatasource = (data) => postAxios('/monitor/datasource/add', data)

export const updateDatasource = (data) => postAxios('/monitor/datasource/update', data)

export const deleteDatasource = (id) => getAxios(`/monitor/datasource/delete/${id}`)

export const testDatasource = (id) => postAxios(`/monitor/datasource/test/${id}`)
