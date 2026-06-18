package com.kawai.services.impl;

import com.kawai.models.RoomCategory;
import com.kawai.repositories.RoomCategoryRepository;
import com.kawai.repositories.RoomRepository;
import com.kawai.services.interfaces.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final RoomCategoryRepository categoryRepository;
    private final RoomRepository roomRepository;

    public CategoryServiceImpl(RoomCategoryRepository categoryRepository, RoomRepository roomRepository) {
        this.categoryRepository = categoryRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        // Soft delete logic
        RoomCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        // Check if there are physical rooms linked to this category
        long activeRoomsCount = roomRepository.findAll().stream()
                .filter(room -> room.getCategory() != null && room.getCategory().getId().equals(id))
                .count();

        if (activeRoomsCount > 0) {
            throw new IllegalStateException("RESOURCE_IN_USE: Cannot delete category containing active physical rooms.");
        }

        category.setIsActive(false);
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public RoomCategory updateCategory(Long id, BigDecimal newBasePrice) {
        RoomCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        category.setBasePrice(newBasePrice);
        return categoryRepository.save(category);
    }
}
