package com.arathort.localinteraction.domain.usecase

import com.arathort.localinteraction.domain.repository.AdminRepository
import javax.inject.Inject

class DeleteCategoryUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(categoryId: String): Result<String> =
        adminRepository.deleteCategory(categoryId)
}
