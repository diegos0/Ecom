package com.ecom.service;

import com.ecom.model.Category;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CategoryService {
    Category saveCategory(Category category);
    List<Category> getAllCategory();
    Boolean existCategory(String name);
    Boolean deleteCategory(int id);
    Category getCategoryById(int id);
    List<Category> getAllActiveCategory();
    Page<Category> getAllCategorPagination(Integer pageNo, Integer pageSize);
}
