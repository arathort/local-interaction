package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.Category
import com.arathort.localinteraction.domain.repository.AdminRepository
import javax.inject.Inject

class GetAdminCategoriesUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(): Result<List<Category>> =
        adminRepository.getAdminCategories()
}
