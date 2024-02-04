package ru.otus.geo

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.otus.domain.location.LocationService
import ru.otus.geomock.LocationServiceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface LocationModule {
    @Binds
    @Singleton
    fun bindLocationService(impl: LocationServiceImpl): LocationService
}