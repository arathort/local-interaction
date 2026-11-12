package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.model.Category
import com.arathort.localinteraction.domain.repository.AdminRepository
import javax.inject.Inject

class UpdateCategoryUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(categoryId: String, name: String): Result<Category> =
        adminRepository.updateCategory(categoryId, name)
}
