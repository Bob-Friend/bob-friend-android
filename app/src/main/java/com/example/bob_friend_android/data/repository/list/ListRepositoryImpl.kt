package com.example.bob_friend_android.data.repository.list

import com.example.bob_friend_android.data.entity.AppointmentList
import com.example.bob_friend_android.data.entity.ErrorResponse
import com.example.bob_friend_android.data.network.NetworkResponse
import com.example.bob_friend_android.data.network.api.ApiService
import com.example.bob_friend_android.model.LocationList
import javax.inject.Inject

class ListRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ListRepository {
    override suspend fun setAppointmentList(
        page: Int,
        type: String?,
        address: String?
    ): NetworkResponse<AppointmentList, ErrorResponse> {
        return apiService.getAppointmentListResponse(page, type, address)
    }

    override suspend fun searchAppointmentList(
        page: Int,
        category: String,
        keyword: String,
        start: String?,
        end: String?
    ): NetworkResponse<AppointmentList, ErrorResponse> {
        return if (start != null && end != null) {
            apiService.searchAppointmentListTimeLimitResponse(page, category, keyword, start, end)
        } else {
            apiService.searchAppointmentListResponse(page, category, keyword)
        }
    }

    override suspend fun setAppointmentLocationList(
        zoom: Int,
        longitude: Double,
        latitude: Double
    ): NetworkResponse<LocationList, ErrorResponse> {
        return apiService.getAppointmentLocationsResponse(zoom, longitude, latitude)
    }
}