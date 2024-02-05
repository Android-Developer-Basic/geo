package ru.otus.geo

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.otus.domain.track.UserTrackingService
import ru.otus.net.UserTrackingServiceImpl

@Module
@InstallIn(SingletonComponent::class)
interface AppModule {
    @Binds
    fun trackingService(iml: UserTrackingServiceImpl): UserTrackingService
}