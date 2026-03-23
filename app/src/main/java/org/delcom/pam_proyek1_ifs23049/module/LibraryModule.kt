package org.delcom.pam_proyek1_ifs23049.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.delcom.pam_proyek1_ifs23049.network.library.service.ILibraryAppContainer
import org.delcom.pam_proyek1_ifs23049.network.library.service.ILibraryRepository
import org.delcom.pam_proyek1_ifs23049.network.library.service.LibraryAppContainer

@Module
@InstallIn(SingletonComponent::class)
object LibraryModule {
    @Provides
    fun provideLibraryContainer(): ILibraryAppContainer {
        return LibraryAppContainer()
    }

    @Provides
    fun provideLibraryRepository(
        container: ILibraryAppContainer
    ): ILibraryRepository {
        return container.repository
    }
}